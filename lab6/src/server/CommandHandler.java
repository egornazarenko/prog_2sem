package server;

import common.Request;
import common.Response;
import managers.CollectionManager;
import model.MusicBand;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Модуль обработки команд на сервере.
 * Принимает Request, выполняет команду, возвращает Response.
 */
public class CommandHandler {

    private final CollectionManager collectionManager;

    public CommandHandler(CollectionManager collectionManager) {
        this.collectionManager = collectionManager;
    }

    /**
     * Обрабатывает запрос и возвращает ответ.
     *
     * @param request запрос от клиента
     * @return ответ для клиента
     */
    public Response handle(Request request) {
        try {
            String cmd = request.getCommandName().toLowerCase().trim();
            switch (cmd) {
                case "help":        return handleHelp();
                case "info":        return handleInfo();
                case "show":        return handleShow();
                case "add":         return handleAdd(request);
                case "update":      return handleUpdate(request);
                case "remove_by_id":return handleRemoveById(request);
                case "clear":       return handleClear();
                case "exit":        return new Response(Response.Status.EXIT, "Клиент отключился.");
                case "add_if_min":  return handleAddIfMin(request);
                case "remove_greater": return handleRemoveGreater(request);
                case "remove_lower":   return handleRemoveLower(request);
                case "count_by_studio": return handleCountByStudio(request);
                case "filter_less_than_number_of_participants": return handleFilter(request);
                case "print_descending": return handleDescending();
                default:
                    return new Response(Response.Status.ERROR,
                            "Неизвестная команда: '" + cmd + "'. Введите 'help' для справки.");
            }
        } catch (Exception e) {
            return new Response(Response.Status.ERROR, "Ошибка выполнения команды: " + e.getMessage());
        }
    }

    private Response handleHelp() {
        String help =
            "Доступные команды:\n" +
            "  help                                           — справка\n" +
            "  info                                           — информация о коллекции\n" +
            "  show                                           — вывести все элементы\n" +
            "  add                                            — добавить элемент\n" +
            "  update <id>                                    — обновить элемент по id\n" +
            "  remove_by_id <id>                              — удалить элемент по id\n" +
            "  clear                                          — очистить коллекцию\n" +
            "  exit                                           — завершить работу клиента\n" +
            "  add_if_min                                     — добавить, если меньше минимума\n" +
            "  remove_greater                                 — удалить элементы больше заданного\n" +
            "  remove_lower                                   — удалить элементы меньше заданного\n" +
            "  count_by_studio <studio>                       — подсчитать по студии\n" +
            "  filter_less_than_number_of_participants <n>    — фильтр по числу участников\n" +
            "  print_descending                               — вывести в порядке убывания";
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
        String msg = sorted.stream()
                .map(MusicBand::toString)
                .collect(Collectors.joining("\n"));
        return new Response(Response.Status.OK, msg, sorted);
    }

    private Response handleAdd(Request request) {
        MusicBand band = request.getMusicBand();
        if (band == null) return new Response(Response.Status.ERROR, "Объект группы не передан.");
        int id = collectionManager.generateId();
        band.setId(id);
        band.setCreationDate(LocalDate.now());
        collectionManager.add(band);
        return new Response(Response.Status.OK,
                "Группа '" + band.getName() + "' добавлена с ID=" + id + ".");
    }

    private Response handleUpdate(Request request) {
        if (request.getArgument() == null) return new Response(Response.Status.ERROR, "Не указан ID.");
        int id;
        try {
            id = Integer.parseInt(request.getArgument().trim());
        } catch (NumberFormatException e) {
            return new Response(Response.Status.ERROR, "Некорректный ID: " + request.getArgument());
        }
        if (collectionManager.getById(id) == null) {
            return new Response(Response.Status.ERROR, "Элемент с ID=" + id + " не найден.");
        }
        MusicBand band = request.getMusicBand();
        if (band == null) return new Response(Response.Status.ERROR, "Объект группы не передан.");
        collectionManager.update(id, band);
        return new Response(Response.Status.OK, "Элемент с ID=" + id + " обновлён.");
    }

    private Response handleRemoveById(Request request) {
        if (request.getArgument() == null) return new Response(Response.Status.ERROR, "Не указан ID.");
        int id;
        try {
            id = Integer.parseInt(request.getArgument().trim());
        } catch (NumberFormatException e) {
            return new Response(Response.Status.ERROR, "Некорректный ID: " + request.getArgument());
        }
        boolean removed = collectionManager.removeById(id);
        return removed
            ? new Response(Response.Status.OK, "Элемент с ID=" + id + " удалён.")
            : new Response(Response.Status.ERROR, "Элемент с ID=" + id + " не найден.");
    }

    private Response handleClear() {
        collectionManager.clear();
        return new Response(Response.Status.OK, "Коллекция очищена.");
    }

    private Response handleAddIfMin(Request request) {
        MusicBand band = request.getMusicBand();
        if (band == null) return new Response(Response.Status.ERROR, "Объект группы не передан.");
        int id = collectionManager.generateId();
        band.setId(id);
        band.setCreationDate(LocalDate.now());
        MusicBand min = collectionManager.getMin();
        if (min == null || band.compareTo(min) < 0) {
            collectionManager.add(band);
            return new Response(Response.Status.OK,
                    "Группа '" + band.getName() + "' добавлена с ID=" + id + ".");
        }
        return new Response(Response.Status.OK,
                "Элемент не добавлен: он не меньше минимального ('" + min.getName() + "').");
    }

    private Response handleRemoveGreater(Request request) {
        MusicBand band = request.getMusicBand();
        if (band == null) return new Response(Response.Status.ERROR, "Объект группы не передан.");
        int before = collectionManager.size();
        collectionManager.removeGreater(band);
        int removed = before - collectionManager.size();
        return new Response(Response.Status.OK, "Удалено элементов: " + removed + ".");
    }

    private Response handleRemoveLower(Request request) {
        MusicBand band = request.getMusicBand();
        if (band == null) return new Response(Response.Status.ERROR, "Объект группы не передан.");
        int before = collectionManager.size();
        collectionManager.removeLower(band);
        int removed = before - collectionManager.size();
        return new Response(Response.Status.OK, "Удалено элементов: " + removed + ".");
    }

    private Response handleCountByStudio(Request request) {
        String studioName = request.getArgument();
        model.Studio studio = (studioName == null || studioName.trim().isEmpty())
                ? null : new model.Studio(studioName.trim());
        long count = collectionManager.countByStudio(studio);
        String name = studio == null ? "null" : studio.getName();
        return new Response(Response.Status.OK,
                "Количество групп со студией '" + name + "': " + count);
    }

    private Response handleFilter(Request request) {
        if (request.getArgument() == null) return new Response(Response.Status.ERROR, "Не указано значение.");
        int n;
        try {
            n = Integer.parseInt(request.getArgument().trim());
        } catch (NumberFormatException e) {
            return new Response(Response.Status.ERROR, "Некорректное значение: " + request.getArgument());
        }
        List<MusicBand> result = collectionManager.filterLessThanNumberOfParticipants(n);
        if (result.isEmpty()) {
            return new Response(Response.Status.OK, "Групп с числом участников < " + n + " не найдено.", result);
        }
        String msg = result.stream().map(MusicBand::toString).collect(Collectors.joining("\n"));
        return new Response(Response.Status.OK, msg, result);
    }

    private Response handleDescending() {
        List<MusicBand> result = collectionManager.getDescending();
        if (result.isEmpty()) {
            return new Response(Response.Status.OK, "Коллекция пуста.", result);
        }
        String msg = result.stream().map(MusicBand::toString).collect(Collectors.joining("\n"));
        return new Response(Response.Status.OK, msg, result);
    }
}
