package client;

import common.Request;
import common.Response;
import model.MusicBand;

import java.util.Scanner;

/**
 * Клиентское приложение.
 *
 * Обязанности:
 *   - Чтение команд из консоли
 *   - Валидация вводимых данных
 *   - Сериализация команды и аргументов в объект Request
 *   - Отправка Request на сервер
 *   - Получение и вывод Response
 *   - Корректная обработка временной недоступности сервера
 *
 * Команды и аргументы передаются как объекты классов (Request/MusicBand),
 * не как "простые строки".
 */
public class Client {

    private static final String HOST = "localhost";
    private static final int PORT = 12345;
    private static final int RECONNECT_DELAY_MS = 3000;
    private static final int MAX_RECONNECT_ATTEMPTS = 5;

    private final InputManager inputManager;
    private ServerConnection connection;

    public Client(InputManager inputManager) {
        this.inputManager = inputManager;
        this.connection = new ServerConnection(HOST, PORT);
    }

    /**
     * Основной цикл клиента: читает команды, формирует Request, отправляет, выводит Response.
     */
    public void run() {
        System.out.println("Клиент запущен. Подключение к серверу " + HOST + ":" + PORT + "...");

        if (!connectWithRetry()) {
            System.err.println("Не удалось подключиться к серверу. Завершение.");
            return;
        }

        System.out.println("Подключено к серверу. Введите 'help' для списка команд.");

        while (true) {
            System.out.print(">> ");
            String line = inputManager.readLine();

            if (line == null) {
                System.out.println("\nКонец ввода. Завершение клиента.");
                break;
            }

            line = line.trim();
            if (line.isEmpty()) continue;

            String[] parts = line.split("\\s+", 2);
            String cmdName = parts[0].toLowerCase();
            String arg = parts.length > 1 ? parts[1].trim() : null;
            if (cmdName.equals("exit")) {
                sendAndPrint(new Request("exit"));
                System.out.println("Завершение работы клиента.");
                break;
            }

            if (cmdName.equals("save")) {
                System.err.println("Команда 'save' недоступна на клиенте. " +
                                   "Для сохранения используйте консоль сервера.");
                continue;
            }

            Request request = buildRequest(cmdName, arg);
            if (request == null) continue;
            sendAndPrint(request);
        }

        connection.close();
    }

    /**
     * Формирует объект Request для команды.
     * Для команд, требующих MusicBand, вызывает интерактивный ввод.
     * Возвращает null если пользователь допустил ошибку или отменил ввод.
     */
    private Request buildRequest(String cmdName, String arg) {
        switch (cmdName) {
            case "help":
            case "info":
            case "show":
            case "clear":
            case "print_descending":
                return new Request(cmdName);

            case "remove_by_id":
                if (arg == null || arg.isEmpty()) {
                    System.err.println("Использование: remove_by_id <id>");
                    return null;
                }
                return new Request(cmdName, arg);

            case "count_by_studio":
                return new Request(cmdName, arg);

            case "filter_less_than_number_of_participants":
                if (arg == null || arg.isEmpty()) {
                    System.err.println("Использование: filter_less_than_number_of_participants <число>");
                    return null;
                }
                return new Request(cmdName, arg);

            case "update":
                if (arg == null || arg.isEmpty()) {
                    System.err.println("Использование: update <id>");
                    return null;
                }
                try {
                    Integer.parseInt(arg);
                } catch (NumberFormatException e) {
                    System.err.println("Некорректный ID: " + arg);
                    return null;
                }
                System.out.println("Введите новые данные группы:");
                try {
                    MusicBand band = inputManager.readMusicBand();
                    return new Request(cmdName, arg, band);
                } catch (Exception e) {
                    System.err.println("Ошибка ввода: " + e.getMessage());
                    return null;
                }

            case "add":
            case "add_if_min":
            case "remove_greater":
            case "remove_lower":
                System.out.println("Введите данные группы:");
                try {
                    MusicBand band = inputManager.readMusicBand();
                    return new Request(cmdName, null, band);
                } catch (Exception e) {
                    System.err.println("Ошибка ввода: " + e.getMessage());
                    return null;
                }

            default:
                System.err.println("Неизвестная команда: '" + cmdName + "'. Введите 'help'.");
                return null;
        }
    }

    /**
     * Отправляет запрос и выводит ответ.
     * При потере связи пытается переподключиться.
     */
    private void sendAndPrint(Request request) {
        if (!connection.isConnected()) {
            System.out.println("[Client] Соединение потеряно. Попытка переподключения...");
            if (!connectWithRetry()) {
                System.err.println("[Client] Сервер недоступен. Команда не выполнена.");
                return;
            }
        }

        boolean sent = connection.sendRequest(request);
        if (!sent) {
            System.err.println("[Client] Ошибка отправки. Попытка переподключения...");
            connection.close();
            if (!connectWithRetry()) {
                System.err.println("[Client] Сервер недоступен. Команда не выполнена.");
                return;
            }
            if (!connection.sendRequest(request)) {
                System.err.println("[Client] Не удалось отправить команду.");
                return;
            }
        }

        Response response = connection.receiveResponse();
        if (response == null) {
            System.err.println("[Client] Не получен ответ от сервера.");
            connection.close();
            return;
        }

        if (response.getStatus() == Response.Status.ERROR) {
            System.err.println("[Ошибка] " + response.getMessage());
        } else {
            System.out.println(response.getMessage());
        }
    }

    /**
     * Пытается подключиться к серверу несколько раз.
     * Корректно обрабатывает временную недоступность сервера.
     */
    private boolean connectWithRetry() {
        for (int attempt = 1; attempt <= MAX_RECONNECT_ATTEMPTS; attempt++) {
            connection = new ServerConnection(HOST, PORT);
            if (connection.connect()) {
                System.out.println("[Client] Подключено к серверу.");
                return true;
            }
            if (attempt < MAX_RECONNECT_ATTEMPTS) {
                System.out.println("[Client] Попытка " + attempt + "/" + MAX_RECONNECT_ATTEMPTS +
                        " не удалась. Повтор через " + (RECONNECT_DELAY_MS / 1000) + " сек...");
                try {
                    Thread.sleep(RECONNECT_DELAY_MS);
                } catch (InterruptedException ignored) {
                    Thread.currentThread().interrupt();
                    return false;
                }
            }
        }
        return false;
    }

    public static void main(String[] args) {
        InputManager inputManager = new InputManager(new Scanner(System.in));
        Client client = new Client(inputManager);
        client.run();
    }
}
