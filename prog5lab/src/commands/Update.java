package commands;

import managers.CollectionManager;
import managers.InputManager;
import managers.InputManager.MusicBandData;
import model.*;

import java.time.LocalDate;

/**
 * Команда {@code update id} — обновление элемента коллекции по ID.
 */
public class Update implements Command {

    /** Менеджер коллекции. */
    private final CollectionManager collectionManager;

    /** Менеджер ввода. */
    private final InputManager inputManager;

    /**
     * Создаёт команду update.
     *
     * @param collectionManager менеджер коллекции
     * @param inputManager      менеджер ввода
     */
    public Update(CollectionManager collectionManager, InputManager inputManager) {
        this.collectionManager = collectionManager;
        this.inputManager = inputManager;
    }

    @Override
    public String getName() { return "update"; }

    @Override
    public String getDescription() {
        return "update id : обновить элемент коллекции с заданным id (выборочное обновление полей)";
    }

    @Override
    public void execute(String[] args) {
        if (args.length < 1) {
            System.err.println("Использование: update <id>");
            return;
        }
        
        int id;
        try {
            id = Integer.parseInt(args[0].trim());
        } catch (NumberFormatException e) {
            System.err.println("Ошибка: id должен быть целым числом.");
            return;
        }
        
        MusicBand existingBand = collectionManager.getById(id);
        if (existingBand == null) {
            System.err.println("Ошибка: элемент с ID=" + id + " не найден.");
            return;
        }
        
        try {
            System.out.println("=== Обновление группы с ID=" + id + " ===");
            System.out.println("Текущие значения:");
            printBandInfo(existingBand);
            System.out.println("\n=== Выберите поля для обновления ===");
            
            String name = existingBand.getName();
            Coordinates coordinates = existingBand.getCoordinates();
            int numberOfParticipants = existingBand.getNumberOfParticipants();
            Integer singlesCount = existingBand.getSinglesCount();
            int albumsCount = existingBand.getAlbumsCount();
            MusicGenre genre = existingBand.getGenre();
            Studio studio = existingBand.getStudio();
            double rating = existingBand.getRating();
            
            boolean updated = false;
            
            if (askYesNo("Обновить название группы? (y/n): ")) {
                name = inputManager.readNonEmptyString("Введите новое название: ");
                updated = true;
            }
            
            if (askYesNo("Обновить координаты? (y/n): ")) {
                int x = inputManager.readBoundedInt("Введите координату X (максимум " + Coordinates.MAX_X + "): ",
                                                    Integer.MIN_VALUE, Coordinates.MAX_X);
                int y = inputManager.readInt("Введите координату Y: ");
                coordinates = new Coordinates(x, y);
                updated = true;
            }
            
            if (askYesNo("Обновить количество участников? (y/n): ")) {
                numberOfParticipants = inputManager.readPositiveInt("Введите количество участников (> 0): ");
                updated = true;
            }
            
            if (askYesNo("Обновить количество синглов? (y/n): ")) {
                singlesCount = inputManager.readNullablePositiveInt("Введите количество синглов (> 0, или пустую строку для null): ");
                updated = true;
            }
            
            if (askYesNo("Обновить количество альбомов? (y/n): ")) {
                albumsCount = inputManager.readPositiveInt("Введите количество альбомов (> 0): ");
                updated = true;
            }
            
            if (askYesNo("Обновить жанр? (y/n): ")) {
                genre = inputManager.readMusicGenre("Введите жанр");
                updated = true;
            }
            
            if (askYesNo("Обновить студию? (y/n): ")) {
                studio = inputManager.readStudio();
                updated = true;
            }
            
            if (askYesNo("Обновить рейтинг? (y/n): ")) {
                rating = inputManager.readPositiveDouble("Введите рейтинг группы (от 0 до 10): ");
                updated = true;
            }
            
            if (!updated) {
                System.out.println("Ни одно поле не было обновлено.");
                return;
            }
            
            MusicBand updatedBand = new MusicBand(id, name, coordinates, existingBand.getCreationDate(),
                                                  numberOfParticipants, singlesCount, albumsCount, 
                                                  genre, studio, rating);
            collectionManager.update(id, updatedBand);
            
            System.out.println("\n=== Группа с ID=" + id + " успешно обновлена ===");
            System.out.println("Новые значения:");
            printBandInfo(updatedBand);
            
        } catch (Exception e) {
            System.err.println("Ошибка при обновлении: " + e.getMessage());
        }
    }
    
    /**
     * Задаёт вопрос с ответом да/нет.
     *
     * @param prompt текст приглашения
     * @return true если пользователь ответил да, false если нет
     */
    private boolean askYesNo(String prompt) {
        while (true) {
            System.out.print(prompt);
            String line = inputManager.readLine();
            if (line == null) return false;
            String answer = line.trim().toLowerCase();
            if (answer.equals("y") || answer.equals("yes") || answer.equals("д") || answer.equals("да")) {
                return true;
            }
            if (answer.equals("n") || answer.equals("no") || answer.equals("н") || answer.equals("нет")) {
                return false;
            }
            System.out.println("Пожалуйста, ответьте y/n (да/нет)");
        }
    }
    
    /**
     * Выводит информацию о группе.
     *
     * @param band группа
     */
    private void printBandInfo(MusicBand band) {
        System.out.println("  ID: " + band.getId());
        System.out.println("  Название: " + band.getName());
        System.out.println("  Координаты: " + band.getCoordinates());
        System.out.println("  Дата создания: " + band.getCreationDate());
        System.out.println("  Количество участников: " + band.getNumberOfParticipants());
        System.out.println("  Количество синглов: " + (band.getSinglesCount() != null ? band.getSinglesCount() : "null"));
        System.out.println("  Количество альбомов: " + band.getAlbumsCount());
        System.out.println("  Жанр: " + band.getGenre());
        System.out.println("  Студия: " + (band.getStudio() != null ? band.getStudio().getName() : "null"));
        System.out.println("  Рейтинг: " + band.getRating());
    }
}