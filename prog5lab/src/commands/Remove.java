package commands;

import managers.CollectionManager;

/**
 * Команда {@code remove_by_id id} — удаление элемента по ID.
 */
public class Remove implements Command {

    /** Менеджер коллекции. */
    private final CollectionManager collectionManager;

    /**
     * Создаёт команду remove_by_id.
     *
     * @param collectionManager менеджер коллекции
     */
    public Remove(CollectionManager collectionManager) {
        this.collectionManager = collectionManager;
    }

    @Override
    public String getName() { return "remove_by_id"; }

    @Override
    public String getDescription() {
        return "remove_by_id id : удалить элемент из коллекции по его id";
    }

    @Override
    public void execute(String[] args) {
        if (args.length < 1) {
            System.err.println("Использование: remove_by_id <id>");
            return;
        }
        try {
            int id = Integer.parseInt(args[0].trim());
            if (collectionManager.removeById(id)) {
                System.out.println("Элемент с ID=" + id + " успешно удалён.");
            } else {
                System.err.println("Элемент с ID=" + id + " не найден.");
            }
        } catch (NumberFormatException e) {
            System.err.println("Ошибка: id должен быть целым числом.");
        }
    }
}
