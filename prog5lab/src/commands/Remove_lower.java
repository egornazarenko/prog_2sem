package commands;

import managers.CollectionManager;
import managers.InputManager;
import managers.InputManager.MusicBandData;
import model.*;

import java.time.LocalDate;

/**
 * Команда {@code remove_lower} — удаление элементов, меньших заданного.
 */
public class Remove_lower implements Command {

    /** Менеджер коллекции. */
    private final CollectionManager collectionManager;

    /** Менеджер ввода. */
    private final InputManager inputManager;

    /**
     * Создаёт команду remove_lower.
     *
     * @param collectionManager менеджер коллекции
     * @param inputManager      менеджер ввода
     */
    public Remove_lower(CollectionManager collectionManager, InputManager inputManager) {
        this.collectionManager = collectionManager;
        this.inputManager = inputManager;
    }

    @Override
    public String getName() { return "remove_lower"; }

    @Override
    public String getDescription() {
        return "remove_lower {element} : удалить элементы, меньшие заданного";
    }

    @Override
    public void execute(String[] args) {
        try {
            System.out.println(" Удаление элементов, меньших заданного ");
            MusicBandData data = inputManager.readMusicBandData();
            MusicBand reference = new MusicBand(0, data.name, data.coordinates, LocalDate.now(),
                                                data.numberOfParticipants, data.singlesCount,
                                                data.albumsCount, data.genre, data.studio, data.rating);
            int sizeBefore = collectionManager.size();
            collectionManager.removeLower(reference);
            int removed = sizeBefore - collectionManager.size();
            System.out.println("Удалено элементов: " + removed);
        } catch (Exception e) {
            System.err.println("Ошибка: " + e.getMessage());
        }
    }
}
