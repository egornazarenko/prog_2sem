package server;

import common.Request;
import common.Response;
import managers.CollectionManager;
import managers.FileManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.util.Iterator;
import java.util.Scanner;
import java.util.Set;

public class Server {

    private static final Logger logger = LoggerFactory.getLogger(Server.class);

    private static final int PORT = 12345;

    private final CollectionManager collectionManager;
    private final CommandHandler commandHandler;
    private volatile boolean running = true;

    public Server(CollectionManager collectionManager) {
        this.collectionManager = collectionManager;
        this.commandHandler = new CommandHandler(collectionManager);
    }

    public void start() {
        Thread consoleThread = new Thread(this::runServerConsole, "server-console");
        consoleThread.setDaemon(true);
        consoleThread.start();

        try (ServerSocketChannel serverChannel = ServerSocketChannel.open();
             Selector selector = Selector.open()) {

            serverChannel.configureBlocking(false);
            serverChannel.bind(new InetSocketAddress(PORT));
            serverChannel.register(selector, SelectionKey.OP_ACCEPT);

            logger.info("Сервер запущен на порту {}", PORT);
            logger.info("Серверные команды: save — сохранить коллекцию, exit — завершить сервер");

            while (running) {
                int ready = selector.select(500);
                if (ready == 0) continue;

                Set<SelectionKey> keys = selector.selectedKeys();
                Iterator<SelectionKey> iter = keys.iterator();

                while (iter.hasNext()) {
                    SelectionKey key = iter.next();
                    iter.remove();

                    try {
                        if (key.isAcceptable()) {
                            acceptConnection(serverChannel, selector);
                        } else if (key.isReadable()) {
                            handleClient(key);
                        }
                    } catch (Exception e) {
                        logger.error("Ошибка обработки ключа: {}", e.getMessage());
                        closeKey(key);
                    }
                }
            }

        } catch (IOException e) {
            logger.error("Критическая ошибка сервера: {}", e.getMessage());
        } finally {
            collectionManager.save();
            logger.info("Коллекция сохранена. Сервер остановлен.");
        }
    }

    private void acceptConnection(ServerSocketChannel serverChannel, Selector selector) throws IOException {
        SocketChannel clientChannel = serverChannel.accept();
        if (clientChannel == null) return;
        clientChannel.configureBlocking(false);
        clientChannel.register(selector, SelectionKey.OP_READ);
        logger.info("Новое подключение: {}", clientChannel.getRemoteAddress());
    }

    private void handleClient(SelectionKey key) throws IOException {
        SocketChannel channel = (SocketChannel) key.channel();

        Request request = readRequest(channel);
        if (request == null) {
            logger.info("Клиент отключился: {}", channel.getRemoteAddress());
            closeKey(key);
            return;
        }

        logger.info("Получен запрос: команда='{}', аргумент='{}'",
                request.getCommandName(), request.getArgument());

        Response response = commandHandler.handle(request);

        sendResponse(channel, response);
        logger.info("Отправлен ответ: статус={}", response.getStatus());

        if (response.getStatus() == Response.Status.EXIT) {
            logger.info("Клиент завершил сеанс: {}", channel.getRemoteAddress());
            closeKey(key);
        }
    }

    private Request readRequest(SocketChannel channel) throws IOException {
        ByteBuffer lenBuf = ByteBuffer.allocate(4);
        int bytesRead = readFully(channel, lenBuf);
        if (bytesRead == -1) return null;

        lenBuf.flip();
        int dataLength = lenBuf.getInt();
        if (dataLength <= 0 || dataLength > 10 * 1024 * 1024) {
            throw new IOException("Некорректная длина данных: " + dataLength);
        }

        logger.debug("Чтение запроса: {} байт", dataLength);

        ByteBuffer dataBuf = ByteBuffer.allocate(dataLength);
        bytesRead = readFully(channel, dataBuf);
        if (bytesRead == -1) return null;

        dataBuf.flip();
        byte[] bytes = new byte[dataBuf.remaining()];
        dataBuf.get(bytes);

        try (ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(bytes))) {
            return (Request) ois.readObject();
        } catch (ClassNotFoundException e) {
            throw new IOException("Ошибка десериализации запроса: " + e.getMessage());
        }
    }

    private void sendResponse(SocketChannel channel, Response response) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try (ObjectOutputStream oos = new ObjectOutputStream(baos)) {
            oos.writeObject(response);
            oos.flush();
        }
        byte[] data = baos.toByteArray();

        logger.debug("Отправка ответа: {} байт", data.length);

        ByteBuffer buf = ByteBuffer.allocate(4 + data.length);
        buf.putInt(data.length);
        buf.put(data);
        buf.flip();

        while (buf.hasRemaining()) {
            channel.write(buf);
        }
    }

    private int readFully(SocketChannel channel, ByteBuffer buf) throws IOException {
        int total = 0;
        while (buf.hasRemaining()) {
            int n = channel.read(buf);
            if (n == -1) return -1;
            if (n == 0) {
                try { Thread.sleep(1); } catch (InterruptedException ignored) {}
            }
            total += n;
        }
        return total;
    }

    private void closeKey(SelectionKey key) {
        try {
            key.cancel();
            key.channel().close();
        } catch (IOException ignored) {}
    }

    private void runServerConsole() {
        Scanner scanner = new Scanner(System.in);
        while (running) {
            if (!scanner.hasNextLine()) break;
            String line = scanner.nextLine().trim().toLowerCase();
            switch (line) {
                case "save":
                    collectionManager.save();
                    logger.info("Коллекция сохранена по команде оператора.");
                    break;
                case "exit":
                    logger.info("Завершение работы сервера по команде оператора.");
                    running = false;
                    break;
                default:
                    logger.warn("Неизвестная серверная команда: '{}'. Доступны: save, exit", line);
            }
        }
    }

    public static void main(String[] args) {
        String fileName = System.getenv("MUSIC_BAND_FILE");
        if (fileName == null || fileName.trim().isEmpty()) {
            logger.error("Переменная окружения MUSIC_BAND_FILE не установлена.");
            System.exit(1);
        }

        logger.info("Инициализация сервера. Файл коллекции: {}", fileName.trim());

        FileManager fileManager = new FileManager(fileName.trim());
        CollectionManager collectionManager = new CollectionManager(fileManager);
        collectionManager.loadCollection();

        Server server = new Server(collectionManager);
        server.start();
    }
}