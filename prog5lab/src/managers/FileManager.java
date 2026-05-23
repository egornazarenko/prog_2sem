package managers;

import model.*;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Менеджер файлов. Отвечает за чтение и запись коллекции в CSV-файл.
 *
 * <p>Формат строки CSV:</p>
 * <pre>id,name,x,y,creationDate,numberOfParticipants,singlesCount,albumsCount,genre,studioName,rating</pre>
 * <p>Пустая строка в поле означает null для nullable-полей.</p>
 */
public class FileManager {

    /** Путь к файлу хранения коллекции. */
    private final String fileName;

    /**
     * Создаёт менеджер файлов.
     *
     * @param fileName путь к CSV-файлу
     */
    public FileManager(String fileName) {
        this.fileName = fileName;
    }

    /**
     * Загружает коллекцию из CSV-файла.
     *
     * @return список объектов {@link MusicBand}, прочитанных из файла
     */
    public List<MusicBand> loadCollection() {
        List<MusicBand> list = new ArrayList<>();
        File file = new File(fileName);

        if (!file.exists()) {
            System.out.println("[FileManager] Файл не найден: " + fileName + ". Коллекция пуста.");
            return list;
        }
        if (!file.canRead()) {
            System.err.println("[FileManager] Нет прав на чтение файла: " + fileName);
            return list;
        }

        try (BufferedInputStream bis = new BufferedInputStream(new FileInputStream(file))) {
            byte[] bytes = bis.readAllBytes();
            String content = new String(bytes, StandardCharsets.UTF_8);
            String[] lines = content.split("\n");

            for (int lineNum = 0; lineNum < lines.length; lineNum++) {
                String line = lines[lineNum].trim();
                if (line.isEmpty()) continue;

                try {
                    MusicBand band = parseLine(line);
                    if (band != null) {
                        list.add(band);
                    }
                } catch (Exception e) {
                    System.err.println("[FileManager] Ошибка разбора строки " + (lineNum + 1)
                                       + ": " + e.getMessage() + " — строка пропущена.");
                }
            }
        } catch (IOException e) {
            System.err.println("[FileManager] Ошибка чтения файла: " + e.getMessage());
        }

        System.out.println("[FileManager] Загружено " + list.size() + " элементов.");
        return list;
    }

    /**
     * Сохраняет коллекцию в CSV-файл.
     *
     * @param collection коллекция объектов {@link MusicBand} для сохранения
     */
    public void saveCollection(List<MusicBand> collection) {
        File file = new File(fileName);

        if (file.exists() && !file.canWrite()) {
            System.err.println("[FileManager] Нет прав на запись в файл: " + fileName);
            return;
        }

        StringBuilder sb = new StringBuilder();
        for (MusicBand band : collection) {
            sb.append(bandToCsv(band)).append("\n");
        }

        try (BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(file))) {
            bos.write(sb.toString().getBytes(StandardCharsets.UTF_8));
            bos.flush();
            System.out.println("[FileManager] Коллекция сохранена в файл: " + fileName);
        } catch (IOException e) {
            System.err.println("[FileManager] Ошибка записи в файл: " + e.getMessage());
        }
    }

    /**
     * Преобразует объект {@link MusicBand} в CSV-строку.
     *
     * @param band объект для преобразования
     * @return строка в CSV-формате
     */
    private String bandToCsv(MusicBand band) {
        return band.getId() + "," +
               escapeCsv(band.getName()) + "," +
               band.getCoordinates().getX() + "," +
               band.getCoordinates().getY() + "," +
               band.getCreationDate() + "," +
               band.getNumberOfParticipants() + "," +
               (band.getSinglesCount() != null ? band.getSinglesCount() : "") + "," +
               band.getAlbumsCount() + "," +
               band.getGenre() + "," +
               (band.getStudio() != null ? escapeCsv(band.getStudio().getName()) : "") + ","+
               band.getRating();
    }

    /**
     * Разбирает CSV-строку и создаёт объект {@link MusicBand}.
     *
     * @param line строка в CSV-формате
     * @return объект {@link MusicBand}
     * @throws IllegalArgumentException если строка имеет неверный формат
     */
    private MusicBand parseLine(String line) {
        String[] parts = splitCsv(line);
        if (parts.length < 11) {
            throw new IllegalArgumentException("Недостаточно полей: ожидается 10, получено " + parts.length);
        }

        int id = Integer.parseInt(parts[0].trim());
        if (id <= 0) throw new IllegalArgumentException("id должен быть > 0");

        String name = unescapeCsv(parts[1].trim());
        if (name.isEmpty()) throw new IllegalArgumentException("name не может быть пустым");

        int x = Integer.parseInt(parts[2].trim());
        if (x > Coordinates.MAX_X) throw new IllegalArgumentException("x превышает максимум " + Coordinates.MAX_X);

        String yStr = parts[3].trim();
        Integer y = yStr.isEmpty() ? null : Integer.parseInt(yStr);
        if (y == null) throw new IllegalArgumentException("y не может быть null");

        LocalDate creationDate = LocalDate.parse(parts[4].trim());

        int numberOfParticipants = Integer.parseInt(parts[5].trim());
        if (numberOfParticipants <= 0) throw new IllegalArgumentException("numberOfParticipants должен быть > 0");

        String singlesStr = parts[6].trim();
        Integer singlesCount = singlesStr.isEmpty() ? null : Integer.parseInt(singlesStr);
        if (singlesCount != null && singlesCount <= 0) throw new IllegalArgumentException("singlesCount должен быть > 0");

        int albumsCount = Integer.parseInt(parts[7].trim());
        if (albumsCount <= 0) throw new IllegalArgumentException("albumsCount должен быть > 0");

        MusicGenre genre = MusicGenre.valueOf(parts[8].trim().toUpperCase());

        String studioStr = parts[9].trim();
        Studio studio = studioStr.isEmpty() ? null : new Studio(unescapeCsv(studioStr));

        double rating = Double.parseDouble(parts[10].trim());
        if (rating <= 0) throw new IllegalArgumentException("rating должен быть > 0");

        Coordinates coordinates = new Coordinates(x, y);

        return new MusicBand(id, name, coordinates, creationDate, numberOfParticipants,
                             singlesCount, albumsCount, genre, studio, rating);
    }

    /**
     * Экранирует строку для CSV-формата (заменяет запятые).
     *
     * @param value исходная строка
     * @return экранированная строка
     */
    private String escapeCsv(String value) {
        if (value == null) return "";
        return value.replace(",", "\\,");
    }

    /**
     * Убирает экранирование строки из CSV-формата.
     *
     * @param value экранированная строка
     * @return исходная строка
     */
    private String unescapeCsv(String value) {
        if (value == null) return "";
        return value.replace("\\,", ",");
    }

    /**
     * Разбивает CSV-строку по запятым (учитывает экранирование).
     *
     * @param line CSV-строка
     * @return массив полей
     */
    private String[] splitCsv(String line) {
        List<String> result = new ArrayList<>();
        StringBuilder current = new StringBuilder();
        for (int i = 0; i < line.length(); i++) {
            char c = line.charAt(i);
            if (c == '\\' && i + 1 < line.length() && line.charAt(i + 1) == ',') {
                current.append(',');
                i++;
            } else if (c == ',') {
                result.add(current.toString());
                current = new StringBuilder();
            } else {
                current.append(c);
            }
        }
        result.add(current.toString());
        return result.toArray(new String[0]);
    }

    /**
     * Возвращает путь к файлу.
     *
     * @return путь к CSV-файлу
     */
    public String getFileName() {
        return fileName;
    }
}