package commands;

import managers.CollectionManager;
import model.*;

import java.util.List;

/**
 * Команда {@code show} — вывод всех элементов коллекции.
 */
public class Show implements Command {

    /** Менеджер коллекции. */
    private final CollectionManager collectionManager;

    /**
     * Создаёт команду show.
     *
     * @param collectionManager менеджер коллекции
     */
    public Show(CollectionManager collectionManager) {
        this.collectionManager = collectionManager;
    }

    @Override
    public String getName() { return "show"; }

    @Override
    public String getDescription() { return "вывести все элементы коллекции в строковом представлении"; }

    @Override
    public void execute(String[] args) {
        List<MusicBand> sorted = collectionManager.getSorted();
        if (sorted.isEmpty()) {
            System.out.println("Коллекция пуста.");
            return;
        }
        System.out.println("=== Элементы коллекции (" + sorted.size() + " шт.) ===");
        sorted.forEach(System.out::println);
    }
}