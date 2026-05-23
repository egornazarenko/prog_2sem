package commands;

import managers.CollectionManager;
import managers.InputManager;
import managers.InputManager.MusicBandData;
import model.*;

import java.time.LocalDate;

/**
 * Команда {@code add} — добавление нового элемента в коллекцию.
 */
public class Add implements Command {

    /** Менеджер коллекции. */
    private final CollectionManager collectionManager;

    /** Менеджер ввода. */
    private final InputManager inputManager;

    /**
     * Создаёт команду add.
     *
     * @param collectionManager менеджер коллекции
     * @param inputManager      менеджер ввода
     */
    public Add(CollectionManager collectionManager, InputManager inputManager) {
        this.collectionManager = collectionManager;
        this.inputManager = inputManager;
    }

    @Override
    public String getName() { return "add"; }

    @Override
    public String getDescription() { return "add {element} : добавить новый элемент в коллекцию"; }

    @Override
    public void execute(String[] args) {
        try {
            if (inputManager.isScriptMode()) {
                System.out.println("Чтение группы из скрипта...");
            } else {
                System.out.println("Добавление новой музыкальной группы");
            }
            
            MusicBandData data = inputManager.readMusicBandData();
            
            int id = collectionManager.generateId();
            MusicBand band = new MusicBand(id, data.name, data.coordinates, LocalDate.now(),
                                        data.numberOfParticipants, data.singlesCount,
                                        data.albumsCount, data.genre, data.studio, data.rating);
            collectionManager.add(band);
            System.out.println("Группа '" + band.getName() + "' успешно добавлена с ID=" + id + ".");
            
        } catch (IllegalArgumentException e) {
            System.err.println("Ошибка в скрипте: " + e.getMessage());
            // Не добавляем элемент, просто выводим ошибку
        } catch (Exception e) {
            System.err.println("Ошибка при добавлении элемента: " + e.getMessage());
        }
    }
}
