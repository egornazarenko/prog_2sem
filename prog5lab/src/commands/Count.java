package commands;

import managers.CollectionManager;
import model.*;

/**
 * Команда {@code count_by_studio studio} — подсчёт элементов с заданной студией.
 */
public class Count implements Command {

    /** Менеджер коллекции. */
    private final CollectionManager collectionManager;

    /**
     * Создаёт команду count_by_studio.
     *
     * @param collectionManager менеджер коллекции
     */
    public Count(CollectionManager collectionManager) {
        this.collectionManager = collectionManager;
    }

    @Override
    public String getName() { return "count_by_studio"; }

    @Override
    public String getDescription() {
        return "count_by_studio studio : вывести количество элементов с заданной студией";
    }

    @Override
    public void execute(String[] args) {
        Studio studio = null;
        if (args.length > 0) {
            String studioName = String.join(" ", args).trim();
            if (!studioName.isEmpty()) {
                studio = new Studio(studioName);
            }
        }
        long count = collectionManager.countByStudio(studio);
        String studioInfo = (studio == null) ? "null" : ("'" + studio.getName() + "'");
        System.out.println("Количество элементов со студией " + studioInfo + ": " + count);
    }
}