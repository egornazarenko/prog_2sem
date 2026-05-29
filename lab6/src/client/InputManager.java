package client;

import model.*;

import java.util.NoSuchElementException;
import java.util.Scanner;

/**
 * Менеджер ввода на клиенте.
 * Валидирует вводимые данные и формирует объекты MusicBand.
 */
public class InputManager {

    private final Scanner scanner;

    public InputManager(Scanner scanner) {
        this.scanner = scanner;
    }

    /**
     * Интерактивно читает данные MusicBand от пользователя.
     * Возвращает объект без id и creationDate (они назначаются сервером).
     */
    public MusicBand readMusicBand() {
        String name = readNonEmptyString("Введите название группы: ");
        int x = readBoundedInt("Введите координату X (максимум " + Coordinates.MAX_X + "): ",
                               Integer.MIN_VALUE, Coordinates.MAX_X);
        int y = readInt("Введите координату Y: ");
        int numberOfParticipants = readPositiveInt("Введите количество участников (> 0): ");
        Integer singlesCount = readNullablePositiveInt("Введите количество синглов (или пустую строку для пропуска): ");
        int albumsCount = readPositiveInt("Введите количество альбомов (> 0): ");
        MusicGenre genre = readEnum("Введите жанр", MusicGenre.class);
        Studio studio = readStudio();
        double rating = readPositiveDouble("Введите рейтинг группы (0 < rating <= 10): ");

        return new MusicBand(null, name, new Coordinates(x, y), null,
                numberOfParticipants, singlesCount, albumsCount, genre, studio, rating);
    }

    public String readNonEmptyString(String prompt) {
        while (true) {
            System.out.print(prompt);
            String line = scanner.nextLine();
            if (line == null) throw new NoSuchElementException("Конец ввода");
            line = line.trim();
            if (!line.isEmpty()) return line;
            System.err.println("Строка не может быть пустой. Попробуйте снова.");
        }
    }

    public int readInt(String prompt) {
        while (true) {
            System.out.print(prompt);
            String line = scanner.nextLine().trim();
            try {
                return Integer.parseInt(line);
            } catch (NumberFormatException e) {
                System.err.println("Ошибка: введите целое число.");
            }
        }
    }

    public int readBoundedInt(String prompt, int min, int max) {
        while (true) {
            System.out.print(prompt);
            String line = scanner.nextLine().trim();
            try {
                int value = Integer.parseInt(line);
                if (value >= min && value <= max) return value;
                System.err.println("Значение должно быть от " + min + " до " + max + ".");
            } catch (NumberFormatException e) {
                System.err.println("Ошибка: введите целое число.");
            }
        }
    }

    public int readPositiveInt(String prompt) {
        while (true) {
            System.out.print(prompt);
            String line = scanner.nextLine().trim();
            try {
                int value = Integer.parseInt(line);
                if (value > 0) return value;
                System.err.println("Значение должно быть > 0.");
            } catch (NumberFormatException e) {
                System.err.println("Ошибка: введите целое число > 0.");
            }
        }
    }

    public Integer readNullablePositiveInt(String prompt) {
        while (true) {
            System.out.print(prompt);
            String line = scanner.nextLine().trim();
            if (line.isEmpty()) return null;
            try {
                int value = Integer.parseInt(line);
                if (value > 0) return value;
                System.err.println("Значение должно быть > 0.");
            } catch (NumberFormatException e) {
                System.err.println("Ошибка: введите целое число > 0 или оставьте пустым.");
            }
        }
    }

    public double readPositiveDouble(String prompt) {
        while (true) {
            System.out.print(prompt);
            String line = scanner.nextLine().trim().replace(',', '.');
            try {
                double value = Double.parseDouble(line);
                if (value > 0 && value <= 10) return value;
                System.err.println("Рейтинг должен быть в диапазоне (0, 10].");
            } catch (NumberFormatException e) {
                System.err.println("Ошибка: введите число от 0 до 10.");
            }
        }
    }

    public <T extends Enum<T>> T readEnum(String prompt, Class<T> enumClass) {
        T[] constants = enumClass.getEnumConstants();
        System.out.println("Доступные значения для " + enumClass.getSimpleName() + ":");
        for (int i = 0; i < constants.length; i++) {
            System.out.println("  " + (i + 1) + " - " + constants[i].name());
        }
        while (true) {
            System.out.print(prompt + ": ");
            String line = scanner.nextLine().trim();
            try {
                int num = Integer.parseInt(line);
                if (num >= 1 && num <= constants.length) return constants[num - 1];
                System.err.println("Введите число от 1 до " + constants.length + ".");
            } catch (NumberFormatException e) {
                try {
                    return Enum.valueOf(enumClass, line.toUpperCase());
                } catch (IllegalArgumentException ex) {
                    System.err.println("Неизвестное значение '" + line + "'. Попробуйте снова.");
                }
            }
        }
    }

    public Studio readStudio() {
        System.out.print("Введите название студии (или пустую строку для пропуска): ");
        String line = scanner.nextLine().trim();
        return line.isEmpty() ? null : new Studio(line);
    }

    public boolean hasNextLine() {
        return scanner.hasNextLine();
    }

    public String readLine() {
        return scanner.hasNextLine() ? scanner.nextLine() : null;
    }
}
