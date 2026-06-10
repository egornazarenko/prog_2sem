package server;

import common.Request;
import common.Response;
import db.DatabaseManager;
import db.LogManager;
import managers.CollectionManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.util.Iterator;
import java.util.Scanner;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Многопоточный сервер с поддержкой распределённой транзакции.
 *
 * Многопоточность:
 *   - Чтение запросов    → новый Thread на каждое соединение
 *   - Обработка команд   → FixedThreadPool (4 потока)
 *   - Отправка ответов   → CachedThreadPool
 *
 * Распределённая транзакция:
 *   - Логирование в первой БД (logs_db на 5432)
 *   - Выполнение команды во второй БД (data_db на 5433)
 *   - Откат обеих БД при ошибке
 */
public class Server {

    private static final Logger logger = LoggerFactory.getLogger(Server.class);
    private static final int PORT = 45678;

    private final CollectionManager collectionManager;
    private final CommandHandler commandHandler;
    private final DatabaseManager db;
    private final LogManager logManager;
    private volatile boolean running = true;

    /** Fixed thread pool для обработки команд. */
    private final ExecutorService processingPool = Executors.newFixedThreadPool(4);

    /** Cached thread pool для отправки ответов. */
    private final ExecutorService sendingPool = Executors.newCachedThreadPool();

    public Server(CollectionManager collectionManager, DatabaseManager db, LogManager logManager) {
        this.collectionManager = collectionManager;
        this.db = db;
        this.logManager = logManager;
        this.commandHandler = new CommandHandler(collectionManager, db, logManager);
    }

    public void start() {
        Thread consoleThread = new Thread(this::runServerConsole, "server-console");
        consoleThread.setDaemon(true);
        consoleThread.start();

        try (ServerSocketChannel serverChannel = ServerSocketChannel.open();
             Selector selector = Selector.open()) {

            serverChannel.configureBlocking(false);
            serverChannel.bind(new InetSocketAddress("0.0.0.0", PORT));
            serverChannel.register(selector, SelectionKey.OP_ACCEPT);

            logger.info("Сервер запущен на порту {}. Ожидание подключений...", PORT);

            while (running) {
                int ready = selector.select(500);
                if (ready == 0) continue;

                Set<SelectionKey> keys = selector.selectedKeys();
                Iterator<SelectionKey> iter = keys.iterator();

                while (iter.hasNext()) {
                    SelectionKey key = iter.next();
                    iter.remove();

                    if (key.isAcceptable()) {
                        acceptConnection(serverChannel, selector);
                    } else if (key.isReadable()) {
                        key.interestOps(0);
                        SocketChannel channel = (SocketChannel) key.channel();
                        Thread readerThread = new Thread(() -> handleClientInThread(channel, key, selector),
                                "reader-" + channel.hashCode());
                        readerThread.start();
                    }
                }
            }

        } catch (IOException e) {
            logger.error("Критическая ошибка сервера: {}", e.getMessage());
        } finally {
            processingPool.shutdown();
            sendingPool.shutdown();
            logger.info("Сервер остановлен.");
        }
    }

    /**
     * Модуль чтения запроса (выполняется в отдельном Thread).
     * После чтения передаёт обработку в Fixed thread pool.
     */
    private void handleClientInThread(SocketChannel channel, SelectionKey key, Selector selector) {
        try {
            Request request = readRequest(channel);
            if (request == null) {
                logger.info("Клиент отключился.");
                closeChannel(channel);
                return;
            }

            logger.info("Получен запрос: {} от пользователя '{}'",
                    request.getCommandName(), request.getLogin());

            processingPool.submit(() -> {
                Response response = commandHandler.handle(request);

                sendingPool.submit(() -> {
                    try {
                        sendResponse(channel, response);
                        logger.info("Ответ отправлен: статус={}", response.getStatus());

                        if (response.getStatus() == Response.Status.EXIT) {
                            closeChannel(channel);
                        } else {
                            key.interestOps(SelectionKey.OP_READ);
                            selector.wakeup();
                        }
                    } catch (IOException e) {
                        logger.error("Ошибка отправки ответа: {}", e.getMessage());
                        closeChannel(channel);
                    }
                });
            });

        } catch (IOException e) {
            logger.error("Ошибка чтения запроса: {}", e.getMessage());
            closeChannel(channel);
        }
    }

    /** Модуль приёма подключений. */
    private void acceptConnection(ServerSocketChannel serverChannel, Selector selector) throws IOException {
        SocketChannel clientChannel = serverChannel.accept();
        if (clientChannel == null) return;
        clientChannel.configureBlocking(false);
        clientChannel.register(selector, SelectionKey.OP_READ);
        logger.info("Новое подключение: {}", clientChannel.getRemoteAddress());
    }

    /** Модуль чтения запроса. Протокол: 4 байта длина + данные. */
    private Request readRequest(SocketChannel channel) throws IOException {
        ByteBuffer lenBuf = ByteBuffer.allocate(4);
        if (readFully(channel, lenBuf) == -1) return null;
        lenBuf.flip();
        int len = lenBuf.getInt();
        if (len <= 0 || len > 10 * 1024 * 1024) throw new IOException("Некорректная длина: " + len);

        ByteBuffer dataBuf = ByteBuffer.allocate(len);
        if (readFully(channel, dataBuf) == -1) return null;
        dataBuf.flip();

        byte[] bytes = new byte[dataBuf.remaining()];
        dataBuf.get(bytes);

        try (ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(bytes))) {
            return (Request) ois.readObject();
        } catch (ClassNotFoundException e) {
            throw new IOException("Ошибка десериализации: " + e.getMessage());
        }
    }

    /** Модуль отправки ответа. Протокол: 4 байта длина + данные. */
    private void sendResponse(SocketChannel channel, Response response) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try (ObjectOutputStream oos = new ObjectOutputStream(baos)) {
            oos.writeObject(response);
        }
        byte[] data = baos.toByteArray();
        ByteBuffer buf = ByteBuffer.allocate(4 + data.length);
        buf.putInt(data.length);
        buf.put(data);
        buf.flip();
        synchronized (channel) {
            while (buf.hasRemaining()) channel.write(buf);
        }
    }

    private int readFully(SocketChannel channel, ByteBuffer buf) throws IOException {
        int total = 0;
        while (buf.hasRemaining()) {
            int n = channel.read(buf);
            if (n == -1) return -1;
            if (n == 0) try { Thread.sleep(1); } catch (InterruptedException ignored) {}
            total += n;
        }
        return total;
    }

    private void closeChannel(SocketChannel channel) {
        try { channel.close(); } catch (IOException ignored) {}
    }

    /** Консоль сервера — команда exit. */
    private void runServerConsole() {
        Scanner scanner = new Scanner(System.in);
        while (running) {
            if (!scanner.hasNextLine()) break;
            String line = scanner.nextLine().trim().toLowerCase();
            if (line.equals("exit")) {
                logger.info("Завершение сервера по команде оператора.");
                running = false;
            } else {
                logger.warn("Доступные серверные команды: exit");
            }
        }
    }

    public static void main(String[] args) {
        DatabaseManager db = new DatabaseManager();
        LogManager logManager = new LogManager();

        try {
            db.connect();
            logManager.connect();
            logger.info("Обе БД подключены успешно (5433 для данных, 5432 для логов).");
        } catch (Exception e) {
            logger.error("Ошибка подключения к БД: {}", e.getMessage());
            System.err.println("Не удалось подключиться к БД: " + e.getMessage());
            System.exit(1);
        }

        CollectionManager collectionManager = new CollectionManager(db);
        try {
            collectionManager.loadFromDatabase();
        } catch (Exception e) {
            logger.error("Ошибка загрузки коллекции: {}", e.getMessage());
        }

        Server server = new Server(collectionManager, db, logManager);
        server.start();

        db.close();
        logManager.close();
    }
}