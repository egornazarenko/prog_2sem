package commands;

import managers.CollectionManager;

/**
 * Команда {@code info} — вывод информации о коллекции.
 */
public class Info implements Command {

    /** Менеджер коллекции. */
    private final CollectionManager collectionManager;

    /**
     * Создаёт команду info.
     *
     * @param collectionManager менеджер коллекции
     */
    public Info(CollectionManager collectionManager) {
        this.collectionManager = collectionManager;
    }

    @Override
    public String getName() { return "info"; }

    @Override
    public String getDescription() { return "вывести информацию о коллекции"; }

    @Override
    public void execute(String[] args) {
        System.out.println("Информация о коллекции");
        System.out.println("  Тип коллекции  : " + collectionManager.getCollectionType());
        System.out.println("  Дата инициализации: " + collectionManager.getInitDate());
        System.out.println("  Количество элементов: " + collectionManager.size());
    }
}
