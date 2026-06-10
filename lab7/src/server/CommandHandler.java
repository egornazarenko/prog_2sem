package server;

import common.Request;
import common.Response;
import db.DatabaseManager;
import db.LogManager;
import managers.CollectionManager;
import model.MusicBand;
import model.Studio;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Обработчик команд на сервере с поддержкой распределённой транзакции.
 *
 * Каждый запрос:
 * 1. Логируется в первой БД (logs_db на 5432)
 * 2. Выполняется во второй БД (data_db на 5433)
 * 3. При ошибке — откатываются ОБЕ БД
 * 4. При успехе — коммитятся ОБЕ БД
 */
public class CommandHandler {

    private static final Logger logger = LoggerFactory.getLogger(CommandHandler.class);

    private final CollectionManager collectionManager;
    private final DatabaseManager db;
    private final LogManager logManager;

    public CommandHandler(CollectionManager collectionManager, DatabaseManager db, LogManager logManager) {
        this.collectionManager = collectionManager;
        this.db = db;
        this.logManager = logManager;
    }

    /**
     * Главный метод обработки запроса с распределённой транзакцией.
     */
    public Response handle(Request request) {
        String cmd = request.getCommandName().toLowerCase().trim();

        if (cmd.equals("register")) return handleRegister(request);
        if (cmd.equals("login"))    return handleLogin(request);

        try {
            if (!db.authenticate(request.getLogin(), request.getPassword())) {
                try {
                    logManager.logCommand(request.getLogin(), cmd, "FAILED", "Ошибка авторизации");
                    logManager.commitLog();
                } catch (SQLException e) {
                    logManager.rollbackLog();
                }
                return new Response(Response.Status.ERROR, "Ошибка авторизации.");
            }

            try {
                logManager.logCommand(request.getLogin(), cmd, "PENDING", "Команда начата");

                Response response = executeCommand(cmd, request);

                if (response.getStatus() == Response.Status.ERROR) {
                    db.rollbackData();
                    logManager.rollbackLog();
                    logger.warn("Команда {} провалилась, откат обеих БД.", cmd);
                    return response;
                }

                db.commitData();
                logManager.logCommand(request.getLogin(), cmd, "SUCCESS", "Выполнено успешно");
                logManager.commitLog();

                logger.info("Команда {} выполнена успешно, обе БД закоммичены.", cmd);
                return response;

            } catch (SQLException e) {
                try {
                    db.rollbackData();
                    logManager.rollbackLog();
                } catch (SQLException ignored) {}

                logger.error("Ошибка БД при выполнении команды {}: {}", cmd, e.getMessage());
                return new Response(Response.Status.ERROR, "Ошибка БД: " + e.getMessage());
            }

        } catch (SQLException e) {
            logger.error("Ошибка БД: {}", e.getMessage());
            return new Response(Response.Status.ERROR, "Ошибка БД: " + e.getMessage());
        }
    }

    private Response executeCommand(String cmd, Request request) throws SQLException {
        switch (cmd) {
            case "help":            return handleHelp();
            case "info":            return handleInfo();
            case "show":            return handleShow();
            case "add":             return handleAdd(request);
            case "update":          return handleUpdate(request);
            case "remove_by_id":    return handleRemoveById(request);
            case "clear":           return handleClear(request);
            case "exit":            return new Response(Response.Status.EXIT, "До свидания!");
            case "add_if_min":      return handleAddIfMin(request);
            case "remove_greater":  return handleRemoveGreater(request);
            case "remove_lower":    return handleRemoveLower(request);
            case "count_by_studio": return handleCountByStudio(request);
            case "filter_less_than_number_of_participants": return handleFilter(request);
            case "print_descending": return handleDescending();
            default:
                return new Response(Response.Status.ERROR,
                        "Неизвестная команда: '" + cmd + "'. Введите 'help'.");
        }
    }


    private Response handleRegister(Request req) {
        if (req.getLogin() == null || req.getLogin().trim().isEmpty())
            return new Response(Response.Status.ERROR, "Логин не может быть пустым.");
        if (req.getPassword() == null || req.getPassword().trim().isEmpty())
            return new Response(Response.Status.ERROR, "Пароль не может быть пустым.");
        try {
            boolean ok = db.registerUser(req.getLogin().trim(), req.getPassword());
            if (ok) {
                logger.info("Зарегистрирован новый пользователь: {}", req.getLogin());
                return new Response(Response.Status.OK,
                        "Пользователь '" + req.getLogin() + "' успешно зарегистрирован.");
            } else {
                return new Response(Response.Status.ERROR,
                        "Пользователь с логином '" + req.getLogin() + "' уже существует.");
            }
        } catch (SQLException e) {
            return new Response(Response.Status.ERROR, "Ошибка регистрации: " + e.getMessage());
        }
    }

    private Response handleLogin(Request req) {
        try {
            if (db.authenticate(req.getLogin(), req.getPassword())) {
                logger.info("Авторизован пользователь: {}", req.getLogin());
                return new Response(Response.Status.OK,
                        "Добро пожаловать, " + req.getLogin() + "!");
            } else {
                return new Response(Response.Status.ERROR, "Неверный логин или пароль.");
            }
        } catch (SQLException e) {
            return new Response(Response.Status.ERROR, "Ошибка авторизации: " + e.getMessage());
        }
    }


    private Response handleHelp() {
        String help =
            "Доступные команды:\n" +
            "  help                                           — справка\n" +
            "  info                                           — информация о коллекции\n" +
            "  show                                           — показать все элементы\n" +
            "  add                                            — добавить элемент\n" +
            "  update <id>                                    — обновить свой элемент\n" +
            "  remove_by_id <id>                              — удалить свой элемент\n" +
            "  clear                                          — удалить все свои элементы\n" +
            "  exit                                           — выйти\n" +
            "  add_if_min                                     — добавить если меньше минимума\n" +
            "  remove_greater                                 — удалить свои элементы > заданного\n" +
            "  remove_lower                                   — удалить свои элементы < заданного\n" +
            "  count_by_studio <studio>                       — подсчёт по студии\n" +
            "  filter_less_than_number_of_participants <n>    — фильтр по числу участников\n" +
            "  print_descending                               — вывод в порядке убывания\n" +
            "\n  Примечание: модифицировать можно только свои объекты.";
        return new Response(Response.Status.OK, help);
    }

    private Response handleInfo() {
        String info = "Тип коллекции: " + collectionManager.getCollectionType() + "\n" +
                      "Дата инициализации: " + collectionManager.getInitDate() + "\n" +
                      "Количество элементов: " + collectionManager.size();
        return new Response(Response.Status.OK, info);
    }

    private Response handleShow() {
        List<MusicBand> sorted = collectionManager.getSortedByName();
        if (sorted.isEmpty()) {
            return new Response(Response.Status.OK, "Коллекция пуста.", sorted);
        }
        String msg = sorted.stream().map(MusicBand::toString).collect(Collectors.joining("\n"));
        return new Response(Response.Status.OK, msg, sorted);
    }

    private Response handleAdd(Request req) throws SQLException {
        MusicBand band = req.getMusicBand();
        if (band == null) return new Response(Response.Status.ERROR, "Объект группы не передан.");
        MusicBand added = collectionManager.add(band, req.getLogin());
        return new Response(Response.Status.OK,
                "Группа '" + added.getName() + "' добавлена с ID=" + added.getId() + ".");
    }

    private Response handleUpdate(Request req) throws SQLException {
        if (req.getArgument() == null) return new Response(Response.Status.ERROR, "Не указан ID.");
        int id;
        try { id = Integer.parseInt(req.getArgument().trim()); }
        catch (NumberFormatException e) {
            return new Response(Response.Status.ERROR, "Некорректный ID: " + req.getArgument());
        }
        if (collectionManager.getById(id) == null)
            return new Response(Response.Status.ERROR, "Элемент с ID=" + id + " не найден.");
        if (!db.isOwner(id, req.getLogin()))
            return new Response(Response.Status.ERROR,
                    "Нет прав: элемент ID=" + id + " вам не принадлежит.");
        MusicBand band = req.getMusicBand();
        if (band == null) return new Response(Response.Status.ERROR, "Объект группы не передан.");
        boolean ok = collectionManager.update(id, band, req.getLogin());
        return ok ? new Response(Response.Status.OK, "Элемент ID=" + id + " обновлён.")
                  : new Response(Response.Status.ERROR, "Не удалось обновить элемент ID=" + id + ".");
    }

    private Response handleRemoveById(Request req) throws SQLException {
        if (req.getArgument() == null) return new Response(Response.Status.ERROR, "Не указан ID.");
        int id;
        try { id = Integer.parseInt(req.getArgument().trim()); }
        catch (NumberFormatException e) {
            return new Response(Response.Status.ERROR, "Некорректный ID: " + req.getArgument());
        }
        if (collectionManager.getById(id) == null)
            return new Response(Response.Status.ERROR, "Элемент с ID=" + id + " не найден.");
        if (!db.isOwner(id, req.getLogin()))
            return new Response(Response.Status.ERROR,
                    "Нет прав: элемент ID=" + id + " вам не принадлежит.");
        boolean ok = collectionManager.removeById(id, req.getLogin());
        return ok ? new Response(Response.Status.OK, "Элемент ID=" + id + " удалён.")
                  : new Response(Response.Status.ERROR, "Не удалось удалить элемент ID=" + id + ".");
    }

    private Response handleClear(Request req) throws SQLException {
        int deleted = collectionManager.clear(req.getLogin());
        return new Response(Response.Status.OK,
                "Удалено ваших элементов: " + deleted + ".");
    }

    private Response handleAddIfMin(Request req) throws SQLException {
        MusicBand band = req.getMusicBand();
        if (band == null) return new Response(Response.Status.ERROR, "Объект группы не передан.");
        MusicBand min = collectionManager.getMin();
        if (min == null || band.compareTo(min) < 0) {
            MusicBand added = collectionManager.add(band, req.getLogin());
            return new Response(Response.Status.OK,
                    "Группа '" + added.getName() + "' добавлена с ID=" + added.getId() + ".");
        }
        return new Response(Response.Status.OK,
                "Элемент не добавлен: он не меньше минимального ('" + min.getName() + "').");
    }

    private Response handleRemoveGreater(Request req) throws SQLException {
        MusicBand band = req.getMusicBand();
        if (band == null) return new Response(Response.Status.ERROR, "Объект группы не передан.");
        int deleted = collectionManager.removeGreater(band, req.getLogin());
        return new Response(Response.Status.OK, "Удалено ваших элементов: " + deleted + ".");
    }

    private Response handleRemoveLower(Request req) throws SQLException {
        MusicBand band = req.getMusicBand();
        if (band == null) return new Response(Response.Status.ERROR, "Объект группы не передан.");
        int deleted = collectionManager.removeLower(band, req.getLogin());
        return new Response(Response.Status.OK, "Удалено ваших элементов: " + deleted + ".");
    }

    private Response handleCountByStudio(Request req) {
        String studioName = req.getArgument();
        Studio studio = (studioName == null || studioName.trim().isEmpty())
                ? null : new Studio(studioName.trim());
        long count = collectionManager.countByStudio(studio);
        String name = studio == null ? "null" : studio.getName();
        return new Response(Response.Status.OK,
                "Групп со студией '" + name + "': " + count);
    }

    private Response handleFilter(Request req) {
        if (req.getArgument() == null) return new Response(Response.Status.ERROR, "Не указано значение.");
        int n;
        try { n = Integer.parseInt(req.getArgument().trim()); }
        catch (NumberFormatException e) {
            return new Response(Response.Status.ERROR, "Некорректное значение: " + req.getArgument());
        }
        List<MusicBand> result = collectionManager.filterLessThanParticipants(n);
        if (result.isEmpty())
            return new Response(Response.Status.OK, "Групп с числом участников < " + n + " не найдено.", result);
        String msg = result.stream().map(MusicBand::toString).collect(Collectors.joining("\n"));
        return new Response(Response.Status.OK, msg, result);
    }

    private Response handleDescending() {
        List<MusicBand> result = collectionManager.getDescending();
        if (result.isEmpty())
            return new Response(Response.Status.OK, "Коллекция пуста.", result);
        String msg = result.stream().map(MusicBand::toString).collect(Collectors.joining("\n"));
        return new Response(Response.Status.OK, msg, result);
    }
}