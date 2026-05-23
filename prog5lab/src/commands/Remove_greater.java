package commands;

import managers.CollectionManager;
import managers.InputManager;
import managers.InputManager.MusicBandData;
import model.*;

import java.time.LocalDate;

/**
 * Команда {@code remove_greater} — удаление элементов, превышающих заданный.
 */
public class Remove_greater implements Command {

    /** Менеджер коллекции. */
    private final CollectionManager collectionManager;

    /** Менеджер ввода. */
    private final InputManager inputManager;

    /**
     * Создаёт команду remove_greater.
     *
     * @param collectionManager менеджер коллекции
     * @param inputManager      менеджер ввода
     */
    public Remove_greater(CollectionManager collectionManager, InputManager inputManager) {
        this.collectionManager = collectionManager;
        this.inputManager = inputManager;
    }

    @Override
    public String getName() { return "remove_greater"; }

    @Override
    public String getDescription() {
        return "remove_greater {element} : удалить элементы, превышающие заданный";
    }

    @Override
    public void execute(String[] args) {
        try {
            System.out.println(" Удаление элементов, превышающих заданный");
            MusicBandData data = inputManager.readMusicBandData();
            MusicBand reference = new MusicBand(0, data.name, data.coordinates, LocalDate.now(),
                                                data.numberOfParticipants, data.singlesCount,
                                                data.albumsCount, data.genre, data.studio, data.rating);
            int sizeBefore = collectionManager.size();
            collectionManager.removeGreater(reference);
            int removed = sizeBefore - collectionManager.size();
            System.out.println("Удалено элементов: " + removed);
        } catch (Exception e) {
            System.err.println("Ошибка: " + e.getMessage());
        }
    }
}
