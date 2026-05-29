package model;

import java.io.Serializable;
import java.time.LocalDate;

/**
 * Класс, представляющий музыкальную группу.
 * Реализует сортировку по умолчанию по имени группы (лексикографически).
 */
public class MusicBand implements Comparable<MusicBand>, Serializable {
    private static final long serialVersionUID = 1L;

    private Integer id;
    private String name;
    private Coordinates coordinates;
    private LocalDate creationDate;
    private int numberOfParticipants;
    private Integer singlesCount;
    private int albumsCount;
    private MusicGenre genre;
    private Studio studio;
    private double rating;

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

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public Coordinates getCoordinates() { return coordinates; }
    public void setCoordinates(Coordinates coordinates) { this.coordinates = coordinates; }
    public LocalDate getCreationDate() { return creationDate; }
    public void setCreationDate(LocalDate creationDate) { this.creationDate = creationDate; }
    public int getNumberOfParticipants() { return numberOfParticipants; }
    public void setNumberOfParticipants(int numberOfParticipants) { this.numberOfParticipants = numberOfParticipants; }
    public Integer getSinglesCount() { return singlesCount; }
    public void setSinglesCount(Integer singlesCount) { this.singlesCount = singlesCount; }
    public int getAlbumsCount() { return albumsCount; }
    public void setAlbumsCount(int albumsCount) { this.albumsCount = albumsCount; }
    public MusicGenre getGenre() { return genre; }
    public void setGenre(MusicGenre genre) { this.genre = genre; }
    public Studio getStudio() { return studio; }
    public void setStudio(Studio studio) { this.studio = studio; }
    public double getRating() { return rating; }
    public void setRating(double rating) { this.rating = rating; }

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
