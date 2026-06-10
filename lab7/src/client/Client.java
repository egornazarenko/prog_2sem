package client;

import java.util.Scanner;

import common.Request;
import common.Response;
import model.MusicBand;

/**
 * Клиентское приложение.
 *
 * При старте предлагает зарегистрироваться или войти.
 * После авторизации логин и пароль отправляются с каждым запросом.
 */
public class Client {

    private static final String HOST = "localhost";
    private static final int PORT = 45678;
    private static final int MAX_RECONNECT = 5;
    private static final int RECONNECT_DELAY_MS = 3000;

    private final InputManager inputManager;
    private ServerConnection connection;

    /** Логин и пароль текущего пользователя. */
    private String currentLogin;
    private String currentPassword;

    public Client(InputManager inputManager) {
        this.inputManager = inputManager;
        this.connection = new ServerConnection(HOST, PORT);
    }

    public void run() {
        System.out.println("Подключение к серверу " + HOST + ":" + PORT + "...");
        if (!connectWithRetry()) {
            System.err.println("Не удалось подключиться к серверу.");
            return;
        }
        System.out.println("Подключено.");

        while (true) {
            if (!authFlow()) {
                System.out.println("Завершение.");
                break;
            }

            System.out.println("Введите 'help' для списка команд.");

            while (true) {
                System.out.print("[" + currentLogin + "] >> ");
                String line = inputManager.readLine();
                if (line == null) {
                    System.out.println("\nКонец ввода. Завершение.");
                    connection.close();
                    return;
                }
                line = line.trim();
                if (line.isEmpty()) continue;

                String[] parts = line.split("\\s+", 2);
                String cmd = parts[0].toLowerCase();
                String arg = parts.length > 1 ? parts[1].trim() : null;

                if (cmd.equals("exit")) {
                    sendAndPrint(new Request("exit", currentLogin, currentPassword));
                    System.out.println("Выход выполнен. Вернись в меню авторизации...");
                    break; 
                }

                if (cmd.equals("save")) {
                    System.err.println("Команда 'save' недоступна клиенту. Данные сохраняются в БД автоматически.");
                    continue;
                }

                Request request = buildRequest(cmd, arg);
                if (request == null) continue;
                sendAndPrint(request);
            }
        }

        connection.close();
    }

    /**
     * Флоу авторизации: предлагает зарегистрироваться или войти.
     * @return true если авторизация прошла успешно
     */
    private boolean authFlow() {
        while (true) {
            System.out.println("\n1 — Войти");
            System.out.println("2 — Зарегистрироваться");
            System.out.println("0 — Выйти");
            System.out.print("Выбор: ");
            
            String choice = inputManager.readLine();
            if (choice == null) return false;
            
            choice = choice.trim();
            
            if (!choice.equals("0") && !choice.equals("1") && !choice.equals("2")) {
                System.err.println("Ошибка! Введите 0, 1 или 2");
                continue; 
            }
            
            if (choice.equals("0")) return false;
            
            String login    = inputManager.readNonEmptyString("Логин: ");
            String password = inputManager.readNonEmptyString("Пароль: ");

            String cmd = choice.equals("2") ? "register" : "login";
            Request req = Request.forAuth(cmd, login, password);
            Response resp = sendReceive(req);
            
            if (resp == null) {
                System.err.println("Нет ответа от сервера.");
                continue;
            }
            System.out.println(resp.getMessage());

            if (resp.getStatus() == Response.Status.OK) {
                currentLogin    = login;
                currentPassword = password;
                return true;
            }
        }
    }

    /**
     * Формирует Request с учётными данными пользователя.
     */
    private Request buildRequest(String cmd, String arg) {
        switch (cmd) {
            case "help":
            case "info":
            case "show":
            case "print_descending":
                return new Request(cmd, currentLogin, currentPassword);

            case "remove_by_id":
                if (arg == null) { System.err.println("Использование: remove_by_id <id>"); return null; }
                return new Request(cmd, arg, currentLogin, currentPassword);

            case "count_by_studio":
                return new Request(cmd, arg, currentLogin, currentPassword);

            case "filter_less_than_number_of_participants":
                if (arg == null) { System.err.println("Использование: filter_less_than_number_of_participants <n>"); return null; }
                return new Request(cmd, arg, currentLogin, currentPassword);

            case "update":
                if (arg == null) { System.err.println("Использование: update <id>"); return null; }
                try { Integer.parseInt(arg); } catch (NumberFormatException e) {
                    System.err.println("Некорректный ID: " + arg); return null;
                }
                System.out.println("Введите новые данные группы:");
                try {
                    MusicBand band = inputManager.readMusicBand();
                    return new Request(cmd, arg, band, currentLogin, currentPassword);
                } catch (Exception e) { System.err.println("Ошибка ввода: " + e.getMessage()); return null; }

            case "add":
            case "add_if_min":
            case "remove_greater":
            case "remove_lower":
                System.out.println("Введите данные группы:");
                try {
                    MusicBand band = inputManager.readMusicBand();
                    return new Request(cmd, null, band, currentLogin, currentPassword);
                } catch (Exception e) { System.err.println("Ошибка ввода: " + e.getMessage()); return null; }

            case "clear":
                return new Request(cmd, currentLogin, currentPassword);

            default:
                System.err.println("Неизвестная команда: '" + cmd + "'. Введите 'help'.");
                return null;
        }
    }

    private void sendAndPrint(Request request) {
        Response response = sendReceive(request);
        if (response == null) {
            System.err.println("[Client] Нет ответа от сервера.");
            return;
        }
        if (response.getStatus() == Response.Status.ERROR)
            System.err.println("[Ошибка] " + response.getMessage());
        else
            System.out.println(response.getMessage());
    }

    private Response sendReceive(Request request) {
        if (!connection.isConnected()) {
            System.out.println("[Client] Переподключение...");
            if (!connectWithRetry()) {
                System.err.println("[Client] Сервер недоступен.");
                return null;
            }
        }
        if (!connection.sendRequest(request)) {
            connection.close();
            if (!connectWithRetry()) return null;
            if (!connection.sendRequest(request)) return null;
        }
        return connection.receiveResponse();
    }

    private boolean connectWithRetry() {
        for (int i = 1; i <= MAX_RECONNECT; i++) {
            connection = new ServerConnection(HOST, PORT);
            if (connection.connect()) return true;
            if (i < MAX_RECONNECT) {
                System.out.println("[Client] Попытка " + i + "/" + MAX_RECONNECT +
                        ". Повтор через " + RECONNECT_DELAY_MS / 1000 + " сек...");
                try { Thread.sleep(RECONNECT_DELAY_MS); } catch (InterruptedException ignored) {}
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
