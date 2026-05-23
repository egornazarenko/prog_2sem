package commands;

import managers.CollectionManager;

/**
 * Команда {@code save} — сохранение коллекции в файл.
 */
public class Save implements Command {

    /** Менеджер коллекции. */
    private final CollectionManager collectionManager;

    /**
     * Создаёт команду save.
     *
     * @param collectionManager менеджер коллекции
     */
    public Save(CollectionManager collectionManager) {
        this.collectionManager = collectionManager;
    }

    @Override
    public String getName() { return "save"; }

    @Override
    public String getDescription() { return "сохранить коллекцию в файл"; }

    @Override
    public void execute(String[] args) {
        collectionManager.save();
    }
}