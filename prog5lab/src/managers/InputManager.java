package managers;

import model.*;


import java.time.LocalDate;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.NoSuchElementException;
import java.util.Scanner;

/**
 * Менеджер ввода. Управляет чтением данных от пользователя или из скрипта.
 * Поддерживает стек сканеров для вложенного выполнения скриптов.
 */
public class InputManager {

    /** Стек сканеров (stdin + файловые сканеры для скриптов). */
    private final Deque<Scanner> scannerStack = new ArrayDeque<>();
    private boolean strictMode = false; 
    /**
     * Создаёт менеджер ввода с начальным сканером (обычно System.in).
     *
     * @param initialScanner начальный сканер
     */
    public InputManager(Scanner initialScanner) {
        scannerStack.push(initialScanner);
    }


    /**
     * Добавляет новый сканер в стек (для выполнения скрипта).
     *
     * @param scanner сканер файла скрипта
     */
    public void pushScanner(Scanner scanner) {
        scannerStack.push(scanner);
        strictMode = true; 
    }
    /**
     * Убирает верхний сканер из стека (завершение скрипта).
     * Нижний (stdin) сканер не удаляется.
     */
    public void popScanner() {
        if (scannerStack.size() > 1) {
            Scanner top = scannerStack.pop();
            top.close();
        }
        strictMode = scannerStack.size() > 1;  
    }

    /**
     * Проверяет, находится ли система в режиме исполнения скрипта.
     *
     * @return {@code true} если активен файловый сканер
     */
    public boolean isScriptMode() {
        return scannerStack.size() > 1;
    }

      /**
     * Проверяет, включён ли строгий режим (для скриптов).
     *
     * @return {@code true} если включён строгий режим
     */
    public boolean isStrictMode() {
        return strictMode;
    }

    /**
     * Проверяет, есть ли ещё строки для чтения.
     *
     * @return {@code true} если есть данные для чтения
     */
    public boolean hasNextLine() {
        return !scannerStack.isEmpty() && scannerStack.peek().hasNextLine();
    }

    /**
     * Читает следующую строку из текущего сканера.
     *
     * @return прочитанная строка или {@code null} при ошибке
     */
    public String readLine() {
        while (!scannerStack.isEmpty()) {
            Scanner current = scannerStack.peek();
            if (current.hasNextLine()) {
                String line = current.nextLine();
                if (isScriptMode()) {
                    System.out.println("> " + line);
                }
                return line;
            } else {
                popScanner();
            }
        }
        return null;
    }

    /**
     * Выводит приглашение к вводу (только не в режиме скрипта).
     *
     * @param prompt текст приглашения
     */
    public void prompt(String prompt) {
        if (!isScriptMode()) {
            System.out.print(prompt);
        }
    }

    /**
     * Интерактивно читает данные объекта {@link MusicBand} от пользователя.
     *
     * @return заполненный объект {@link MusicBand} без id и creationDate (генерируются автоматически)
     */
    public MusicBandData readMusicBandData() {
        String name = readNonEmptyString("Введите название группы: ");
        int x = readBoundedInt("Введите координату X (максимум " + Coordinates.MAX_X + "): ",
                               Integer.MIN_VALUE, Coordinates.MAX_X);
        int y = readInt("Введите координату Y: ");
        int numberOfParticipants = readPositiveInt("Введите количество участников (> 0): ");
        Integer singlesCount = readNullablePositiveInt("Введите количество синглов (> 0, или пустую строку для null): ");
        int albumsCount = readPositiveInt("Введите количество альбомов (> 0): ");
        MusicGenre genre = readEnum("Введите жанр", MusicGenre.class);
        Studio studio = readStudio();
        double rating = readPositiveDouble("Введите рейтинг группы (> 0): ");
        Coordinates coordinates = new Coordinates(x, y);
        return new MusicBandData(name, coordinates, numberOfParticipants, singlesCount, albumsCount, genre, studio, rating);
    }

    /**
     * Читает непустую строку.
     *
     * @param prompt приглашение к вводу
     * @return непустая строка
     */
    public String readNonEmptyString(String prompt) {
        while (true) {
            prompt(prompt);
            String line = readLine();
            if (line == null) {
                throw new NoSuchElementException("Конец потока ввода");
            }
            line = line.trim();
            if (!line.isEmpty()) return line;
            
            // В строгом режиме (скрипт) - кидаем исключение
            if (strictMode) {
                throw new IllegalArgumentException("Строка не может быть пустой");
            }
            System.err.println("Ошибка: строка не может быть пустой. Попробуйте снова.");
        }
    }

    /**
     * Читает целое число.
     *
     * @param prompt приглашение к вводу
     * @return введённое целое число
     */
    public int readInt(String prompt) {
        while (true) {
            prompt(prompt);
            String line = readLine();
            if (line == null) throw new NoSuchElementException("Конец потока ввода");
            try {
                double value = Double.parseDouble(line.trim().replace(',', '.'));
                if (value > Integer.MAX_VALUE) {
                    if (strictMode) {
                        throw new IllegalArgumentException("Число слишком большое. Максимум: " + Integer.MAX_VALUE);
                    }
                    System.err.println("Ошибка: число слишком большое. Максимальное значение: " + Integer.MAX_VALUE);
                    continue;
                }
                if (value < Integer.MIN_VALUE) {
                    if (strictMode) {
                        throw new IllegalArgumentException("Число слишком маленькое. Минимум: " + Integer.MIN_VALUE);
                    }
                    System.err.println("Ошибка: число слишком маленькое. Минимальное значение: " + Integer.MIN_VALUE);
                    continue;
                }
                if (value == (int) value) {
                    return (int) value;
                } else {
                    if (strictMode) {
                        throw new IllegalArgumentException("Ожидалось целое число, получено: " + line);
                    }
                    System.err.println("Ошибка: введите целое число. Дробные числа не принимаются.");
                }
            } catch (NumberFormatException e) {
                if (strictMode) {
                    throw new IllegalArgumentException("Ожидалось число, получено: " + line);
                }
                System.err.println("Ошибка: введите целое число.");
            }
        }
    }

    /**
     * Читает музыкальный жанр с поддержкой ввода чисел.
     *
     * @param prompt текст приглашения
     * @return выбранный жанр
     */
    public MusicGenre readMusicGenre(String prompt) {
        return readEnum(prompt, MusicGenre.class);
    }




    /**
     * Читает целое число в диапазоне.
     *
     * @param prompt приглашение
     * @param min    минимальное значение (включительно)
     * @param max    максимальное значение (включительно)
     * @return число в диапазоне [min, max]
     */
    public int readBoundedInt(String prompt, int min, int max) {
        while (true) {
            prompt(prompt);
            String line = readLine();
            if (line == null) throw new NoSuchElementException("Конец потока ввода");
            try {
                double value = Double.parseDouble(line.trim().replace(',', '.'));
                if (value == (int) value && value >= min && value <= max) {
                    return (int) value;
                }
                // В строгом режиме кидаем исключение
                if (strictMode) {
                    throw new IllegalArgumentException("Значение должно быть от " + min + " до " + max + ", получено: " + line);
                }
                if (value < min || value > max) {
                    System.err.println("Ошибка: значение должно быть от " + min + " до " + max + ".");
                } else {
                    System.err.println("Ошибка: введите целое число.");
                }
            } catch (NumberFormatException e) {
                if (strictMode) {
                    throw new IllegalArgumentException("Ожидалось число, получено: " + line);
                }
                System.err.println("Ошибка: введите целое число.");
            }
        }
    }

    /**
     * Читает положительное целое число (> 0).
     *
     * @param prompt приглашение
     * @return положительное целое число
     */
    public int readPositiveInt(String prompt) {
        while (true) {
            prompt(prompt);
            String line = readLine();
            if (line == null) throw new NoSuchElementException("Конец потока ввода");
            try {
                double value = Double.parseDouble(line.trim().replace(',', '.'));
                if (value == (int) value && value > 0) {
                    return (int) value;
                }
                if (strictMode) {
                    throw new IllegalArgumentException("Значение должно быть положительным целым числом, получено: " + line);
                }
                if (value <= 0) {
                    System.err.println("Ошибка: значение должно быть > 0.");
                } else {
                    System.err.println("Ошибка: введите целое число.");
                }
            } catch (NumberFormatException e) {
                if (strictMode) {
                    throw new IllegalArgumentException("Ожидалось число, получено: " + line);
                }
                System.err.println("Ошибка: введите целое число > 0");
            }
        }
    }

    /**
     * Читает nullable положительное целое число.
     * Пустая строка означает null.
     *
     * @param prompt приглашение
     * @return положительное целое число или {@code null}
     */

    public Integer readNullablePositiveInt(String prompt) {
        while (true) {
            prompt(prompt);
            String line = readLine();
            if (line == null) throw new NoSuchElementException("Конец потока ввода");
            line = line.trim();
            if (line.isEmpty()) return null;
            try {
                double value = Double.parseDouble(line.replace(',', '.'));
                if (value > 0 && value == (int) value) {
                    return (int) value;
                }
                if (strictMode) {
                    throw new IllegalArgumentException("Значение должно быть положительным целым числом, получено: " + line);
                }
                if (value <= 0) {
                    System.err.println("Ошибка: значение должно быть > 0.");
                } else {
                    System.err.println("Ошибка: введите целое число.");
                }
            } catch (NumberFormatException e) {
                if (strictMode) {
                    throw new IllegalArgumentException("Ожидалось число, получено: " + line);
                }
                System.err.println("Ошибка: введите целое число > 0 или оставьте пустым.");
            }
        }
    }

    public double readPositiveDouble(String prompt) {
        while (true) {
            prompt(prompt);
            String line = readLine();
            if (line == null) throw new NoSuchElementException("Конец потока ввода");
            try {
                double value = Double.parseDouble(line.trim().replace(',', '.'));
                if (value > 0 && value <= 10) {
                    return value;
                }
                if (strictMode) {
                    throw new IllegalArgumentException("Рейтинг должен быть в диапазоне (0, 10], получено: " + value);
                }
                System.err.println("Ошибка: рейтинг должен быть в диапазоне от 0 до 10.");
            } catch (NumberFormatException e) {
                if (strictMode) {
                    throw new IllegalArgumentException("Ожидалось число, получено: " + line);
                }
                System.err.println("Ошибка: введите вещественное число от 0 до 10.");
            }
        }
    }

    /**
     * Читает значение перечисления.
     *
     * @param prompt    текст приглашения
     * @param enumClass класс перечисления
     * @param <T>       тип перечисления
     * @return выбранная константа перечисления
     */
private <T extends Enum<T>> T readEnum(String prompt, Class<T> enumClass) {
    T[] constants = enumClass.getEnumConstants();
    System.out.println("Доступные значения для " + enumClass.getSimpleName() + ":");
    for (int i = 0; i < constants.length; i++) {
        System.out.println("  " + (i + 1) + " - " + constants[i].name());
    }
    while (true) {
        prompt(prompt + ": ");
        String line = readLine();
        if (line == null) throw new NoSuchElementException("Конец потока ввода");
        
        String trimmed = line.trim();
        try {
            int number = Integer.parseInt(trimmed);
            if (number >= 1 && number <= constants.length) {
                return constants[number - 1];
            }
            if (strictMode) {
                throw new IllegalArgumentException("Число должно быть от 1 до " + constants.length + ", получено: " + number);
            }
            System.err.println("Ошибка: введите число от 1 до " + constants.length + " или имя жанра.");
            continue;
        } catch (NumberFormatException e) {
            try {
                return Enum.valueOf(enumClass, trimmed.toUpperCase());
            } catch (IllegalArgumentException ex) {
                if (strictMode) {
                    throw new IllegalArgumentException("Неизвестное значение: " + trimmed);
                }
                System.err.println("Ошибка: неизвестное значение '" + trimmed + "'. Попробуйте снова.");
            }
        }
    }
}

    /**
     * Читает информацию о студии. Пустая строка — null.
     *
     * @return объект {@link Studio} или {@code null}
     */

    public Studio readStudio() {
        prompt("Введите название студии (или пустую строку для null): ");
        String line = readLine();
        if (line == null) throw new NoSuchElementException("Конец потока ввода");
        line = line.trim();
        if (line.isEmpty()) return null;
        return new Studio(line);
    }


    /**
     * Вспомогательный класс-контейнер для данных MusicBand без автогенерируемых полей.
     */
    public static class MusicBandData {
        /** Название группы. */
        public final String name;
        /** Координаты. */
        public final Coordinates coordinates;
        /** Количество участников. */
        public final int numberOfParticipants;
        /** Количество синглов. */
        public final Integer singlesCount;
        /** Количество альбомов. */
        public final int albumsCount;
        /** Жанр. */
        public final MusicGenre genre;
        /** Студия. */
        public final Studio studio;
        /**Рейтинг */
        public final double rating;

        /**
         * Создаёт контейнер данных.
         *
         * @param name                 название
         * @param coordinates          координаты
         * @param numberOfParticipants участники
         * @param singlesCount         синглы
         * @param albumsCount          альбомы
         * @param genre                жанр
         * @param studio               студия
         */
        public MusicBandData(String name, Coordinates coordinates, int numberOfParticipants,
                             Integer singlesCount, int albumsCount, MusicGenre genre, Studio studio, double rating) {
            this.name = name;
            this.coordinates = coordinates;
            this.numberOfParticipants = numberOfParticipants;
            this.singlesCount = singlesCount;
            this.albumsCount = albumsCount;
            this.genre = genre;
            this.studio = studio;
            this.rating = rating;
        }
    }
}