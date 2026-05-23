package commands;

import managers.CollectionManager;
import managers.InputManager;
import managers.InputManager.MusicBandData;
import model.*;

import java.time.LocalDate;

/**
 * Команда {@code add_if_min} — добавление элемента, если он меньше минимального в коллекции.
 */
public class Add_if_min implements Command {

    /** Менеджер коллекции. */
    private final CollectionManager collectionManager;

    /** Менеджер ввода. */
    private final InputManager inputManager;

    /**
     * Создаёт команду add_if_min.
     *
     * @param collectionManager менеджер коллекции
     * @param inputManager      менеджер ввода
     */
    public Add_if_min(CollectionManager collectionManager, InputManager inputManager) {
        this.collectionManager = collectionManager;
        this.inputManager = inputManager;
    }

    @Override
    public String getName() { return "add_if_min"; }

    @Override
    public String getDescription() {
        return "add_if_min {element} : добавить элемент, если он меньше наименьшего в коллекции";
    }

    @Override
    public void execute(String[] args) {
        try {
            System.out.println(" Добавление группы (если минимальная) ");
            MusicBandData data = inputManager.readMusicBandData();
            int id = collectionManager.generateId();
            MusicBand band = new MusicBand(id, data.name, data.coordinates, LocalDate.now(),
                                           data.numberOfParticipants, data.singlesCount,
                                           data.albumsCount, data.genre, data.studio, data.rating);

            MusicBand min = collectionManager.getMin();
            if (min == null || band.compareTo(min) < 0) {
                collectionManager.add(band);
                System.out.println("Группа '" + band.getName() + "' добавлена с ID=" + id + ".");
            } else {
                System.out.println("Элемент не добавлен: он не меньше минимального ('" + min.getName() + "').");
            }
        } catch (Exception e) {
            System.err.println("Ошибка при добавлении: " + e.getMessage());
        }
    }
}