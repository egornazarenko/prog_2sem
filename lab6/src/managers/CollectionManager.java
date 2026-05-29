package managers;

import model.*;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Менеджер коллекции на сервере.
 * Все операции над коллекцией реализованы через Stream API с лямбда-выражениями.
 */
public class CollectionManager {

    private final ArrayList<MusicBand> collection = new ArrayList<>();
    private final LocalDate initDate;
    private final FileManager fileManager;
    private int nextId = 1;

    public CollectionManager(FileManager fileManager) {
        this.fileManager = fileManager;
        this.initDate = LocalDate.now();
    }

    public void loadCollection() {
        List<MusicBand> loaded = fileManager.loadCollection();
        collection.clear();
        loaded.stream()
              .filter(band -> getById(band.getId()) == null)
              .forEach(band -> {
                  collection.add(band);
                  if (band.getId() >= nextId) nextId = band.getId() + 1;
              });
    }

    public int generateId() {
        while (getById(nextId) != null) nextId++;
        return nextId++;
    }

    public void add(MusicBand band) {
        collection.add(band);
    }

    public boolean update(int id, MusicBand updated) {
        for (int i = 0; i < collection.size(); i++) {
            if (collection.get(i).getId() == id) {
                updated.setId(id);
                updated.setCreationDate(collection.get(i).getCreationDate());
                collection.set(i, updated);
                return true;
            }
        }
        return false;
    }

    public boolean removeById(int id) {
        return collection.removeIf(b -> b.getId() == id);
    }

    public void clear() {
        collection.clear();
    }

    public void save() {
        fileManager.saveCollection(collection);
    }

    public MusicBand getById(int id) {
        return collection.stream()
                .filter(b -> b.getId() == id)
                .findFirst()
                .orElse(null);
    }

    public MusicBand getMin() {
        return collection.stream()
                .min(MusicBand::compareTo)
                .orElse(null);
    }

    public void removeGreater(MusicBand band) {
        collection.removeIf(b -> b.compareTo(band) > 0);
    }

    public void removeLower(MusicBand band) {
        collection.removeIf(b -> b.compareTo(band) < 0);
    }

    public long countByStudio(Studio studio) {
        return collection.stream()
                .filter(b -> studio == null ? b.getStudio() == null : studio.equals(b.getStudio()))
                .count();
    }

    public List<MusicBand> filterLessThanNumberOfParticipants(int numberOfParticipants) {
        return collection.stream()
                .filter(b -> b.getNumberOfParticipants() < numberOfParticipants)
                .collect(Collectors.toList());
    }

    public List<MusicBand> getDescending() {
        return collection.stream()
                .sorted(Collections.reverseOrder())
                .collect(Collectors.toList());
    }

    /** Возвращает коллекцию, отсортированную по имени (для передачи клиенту). */
    public List<MusicBand> getSortedByName() {
        return collection.stream()
                .sorted(MusicBand::compareTo)
                .collect(Collectors.toList());
    }

    public String getCollectionType() { return collection.getClass().getSimpleName(); }
    public LocalDate getInitDate() { return initDate; }
    public int size() { return collection.size(); }

    public List<MusicBand> getCollection() {
        return Collections.unmodifiableList(collection);
    }
}
