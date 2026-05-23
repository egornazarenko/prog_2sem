package commands;

import managers.CollectionManager;

/**
 * Команда {@code clear} — очистка коллекции.
 */
public class Clear implements Command {

    /** Менеджер коллекции. */
    private final CollectionManager collectionManager;

    /**
     * Создаёт команду clear.
     *
     * @param collectionManager менеджер коллекции
     */
    public Clear(CollectionManager collectionManager) {
        this.collectionManager = collectionManager;
    }

    @Override
    public String getName() { return "clear"; }

    @Override
    public String getDescription() { return "очистить коллекцию"; }

    @Override
    public void execute(String[] args) {
        collectionManager.clear();
        System.out.println("Коллекция очищена.");
    }
}
