package model;

import java.time.LocalDate;

/**
 * Класс, представляющий музыкальную группу.
 * Реализует сортировку по умолчанию по имени группы (лексикографически).
 */
public class MusicBand implements Comparable<MusicBand> {

    /**
     * Уникальный идентификатор. Не может быть null.
     * Значение больше 0. Генерируется автоматически.
     */
    private Integer id;

    /** Название группы. Не может быть null и пустой строкой. */
    private String name;

    /** Координаты. Не может быть null. */
    private Coordinates coordinates;

    /** Дата создания записи. Не может быть null. Генерируется автоматически. */
    private LocalDate creationDate;

    /** Количество участников. Должно быть больше 0. */
    private int numberOfParticipants;

    /** Количество синглов. Может быть null. Если не null — больше 0. */
    private Integer singlesCount;

    /** Количество альбомов. Должно быть больше 0. */
    private int albumsCount;

    /** Музыкальный жанр. Не может быть null. */
    private MusicGenre genre;

    /** Студия звукозаписи. Может быть null. */
    private Studio studio;
    /** Рейтинг группы. Должен быть больше 0 */
    private double rating;

    /**
     * Создаёт объект музыкальной группы.
     * 
     * @param id                   уникальный идентификатор (авто)
     * @param name                 название группы
     * @param coordinates          координаты
     * @param creationDate         дата создания (авто)
     * @param numberOfParticipants количество участников
     * @param singlesCount         количество синглов (nullable)
     * @param albumsCount          количество альбомов
     * @param genre                жанр
     * @param studio               студия (nullable)
     * @param rating               рейтинг групппы
     */
    public MusicBand(Integer id, String name, Coordinates coordinates, LocalDate creationDate,
                     int numberOfParticipants, Integer singlesCount, int albumsCount,
                     MusicGenre genre, Studio studio, double rating) {
        this.id = id;
        this.name = name;
        this.coordinates = coordinates;
        this.creationDate = creationDate;
        this.numberOfParticipants = numberOfParticipants;
        this.singlesCount = singlesCount;
        this.albumsCount = albumsCount;
        this.genre = genre;
        this.studio = studio;
        this.rating = rating;
    }

    /** @return уникальный идентификатор */
    public Integer getId() { return id; }

    /** @param id новый идентификатор */
    public void setId(Integer id) { this.id = id; }

    /** @return название группы */
    public String getName() { return name; }

    /** @param name новое название */
    public void setName(String name) { this.name = name; }

    /** @return координаты */
    public Coordinates getCoordinates() { return coordinates; }

    /** @param coordinates новые координаты */
    public void setCoordinates(Coordinates coordinates) { this.coordinates = coordinates; }

    /** @return дата создания */
    public LocalDate getCreationDate() { return creationDate; }

    /** @param creationDate дата создания */
    public void setCreationDate(LocalDate creationDate) { this.creationDate = creationDate; }

    /** @return количество участников */
    public int getNumberOfParticipants() { return numberOfParticipants; }

    /** @param numberOfParticipants количество участников */
    public void setNumberOfParticipants(int numberOfParticipants) {
        this.numberOfParticipants = numberOfParticipants;
    }

    /** @return количество синглов */
    public Integer getSinglesCount() { return singlesCount; }

    /** @param singlesCount количество синглов */
    public void setSinglesCount(Integer singlesCount) { this.singlesCount = singlesCount; }

    /** @return количество альбомов */
    public int getAlbumsCount() { return albumsCount; }

    /** @param albumsCount количество альбомов */
    public void setAlbumsCount(int albumsCount) { this.albumsCount = albumsCount; }

    /** @return музыкальный жанр */
    public MusicGenre getGenre() { return genre; }

    /** @param genre музыкальный жанр */
    public void setGenre(MusicGenre genre) { this.genre = genre; }

    /** @return студия звукозаписи */
    public Studio getStudio() { return studio; }

    /** @param studio студия звукозаписи */
    public void setStudio(Studio studio) { this.studio = studio; }

    /** @return рейтинг группы */
    public double getRating() { return rating; }

    /** @param rating новый рейтинг */
    public void setRating(double rating) { this.rating = rating; }

    /**
     * Сортировка по умолчанию — по имени группы (лексикографически).
     *
     * @param other другой объект MusicBand
     * @return результат сравнения имён
     */
    @Override
    public int compareTo(MusicBand other) {
        return this.name.compareTo(other.name);
    }

    @Override
    public String toString() {
        return "MusicBand{" +
               "id=" + id +
               ", name='" + name + '\'' +
               ", coordinates=" + coordinates +
               ", creationDate=" + creationDate +
               ", numberOfParticipants=" + numberOfParticipants +
               ", singlesCount=" + singlesCount +
               ", albumsCount=" + albumsCount +
               ", genre=" + genre +
               ", studio=" + studio +
               ", rating=" + rating +
               '}';
    }
}