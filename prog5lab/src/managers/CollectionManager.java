package managers;

import model.*;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Менеджер коллекции. Управляет коллекцией объектов {@link MusicBand}.
 * Для хранения используется {@link java.util.ArrayList}.
 */
public class CollectionManager {

    /** Коллекция музыкальных групп. */
    private final ArrayList<MusicBand> collection = new ArrayList<>();

    /** Дата инициализации коллекции. */
    private final LocalDate initDate;

    /** Менеджер файлов для загрузки/сохранения. */
    private final FileManager fileManager;

    /** Счётчик для генерации уникальных ID. */
    private int nextId = 1;

    /**
     * Создаёт менеджер коллекции.
     *
     * @param fileManager менеджер файлов
     */
    public CollectionManager(FileManager fileManager) {
        this.fileManager = fileManager;
        this.initDate = LocalDate.now();
    }

    /**
     * Загружает коллекцию из файла через {@link FileManager}.
     * Обновляет счётчик ID на основе максимального существующего.
     */
    public void loadCollection() {
        List<MusicBand> loaded = fileManager.loadCollection();
        collection.clear();

        for (MusicBand band : loaded) {
            // Проверка уникальности ID
            if (getById(band.getId()) != null) {
                System.err.println("[CollectionManager] Дублирующийся ID=" + band.getId()
                                   + " у '" + band.getName() + "' — элемент пропущен.");
                continue;
            }
            collection.add(band);
            if (band.getId() >= nextId) {
                nextId = band.getId() + 1;
            }
        }
    }

    /**
     * Генерирует новый уникальный ID.
     *
     * @return новый уникальный ID
     */
    public int generateId() {
        while (getById(nextId) != null) {
            nextId++;
        }
        return nextId++;
    }

    /**
     * Добавляет новый элемент в коллекцию.
     *
     * @param band объект {@link MusicBand} для добавления
     */
    public void add(MusicBand band) {
        collection.add(band);
    }

    /**
     * Обновляет элемент с заданным ID.
     *
     * @param id      идентификатор обновляемого элемента
     * @param updated новые данные
     * @return {@code true} если элемент найден и обновлён, иначе {@code false}
     */
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

    /**
     * Удаляет элемент коллекции по его ID.
     *
     * @param id идентификатор удаляемого элемента
     * @return {@code true} если элемент найден и удалён, иначе {@code false}
     */
    public boolean removeById(int id) {
        return collection.removeIf(b -> b.getId() == id);
    }

    /**
     * Очищает коллекцию.
     */
    public void clear() {
        collection.clear();
    }

    /**
     * Сохраняет коллекцию в файл.
     */
    public void save() {
        fileManager.saveCollection(collection);
    }

    /**
     * Возвращает элемент коллекции по ID.
     *
     * @param id идентификатор
     * @return элемент или {@code null} если не найден
     */
    public MusicBand getById(int id) {
        return collection.stream()
                .filter(b -> b.getId() == id)
                .findFirst()
                .orElse(null);
    }

    /**
     * Возвращает минимальный элемент коллекции по естественному порядку.
     *
     * @return минимальный элемент или {@code null} если коллекция пуста
     */
    public MusicBand getMin() {
        return collection.isEmpty() ? null : Collections.min(collection);
    }

    /**
     * Удаляет из коллекции все элементы, превышающие заданный.
     *
     * @param band опорный элемент
     */
    public void removeGreater(MusicBand band) {
        collection.removeIf(b -> b.compareTo(band) > 0);
    }

    /**
     * Удаляет из коллекции все элементы, меньшие заданного.
     *
     * @param band опорный элемент
     */
    public void removeLower(MusicBand band) {
        collection.removeIf(b -> b.compareTo(band) < 0);
    }

    /**
     * Подсчитывает количество элементов с заданной студией.
     *
     * @param studio студия для поиска (может быть null)
     * @return количество совпадений
     */
    public long countByStudio(Studio studio) {
        return collection.stream()
                .filter(b -> {
                    if (studio == null) return b.getStudio() == null;
                    return studio.equals(b.getStudio());
                })
                .count();
    }

    /**
     * Возвращает элементы, у которых numberOfParticipants меньше заданного.
     *
     * @param numberOfParticipants пороговое значение
     * @return список элементов
     */
    public List<MusicBand> filterLessThanNumberOfParticipants(int numberOfParticipants) {
        return collection.stream()
                .filter(b -> b.getNumberOfParticipants() < numberOfParticipants)
                .collect(Collectors.toList());
    }

    /**
     * Возвращает элементы коллекции в порядке убывания (по естественному порядку).
     *
     * @return список в порядке убывания
     */
    public List<MusicBand> getDescending() {
        return collection.stream()
                .sorted(Collections.reverseOrder())
                .collect(Collectors.toList());
    }

    /**
     * Возвращает отсортированную по умолчанию коллекцию.
     *
     * @return новый отсортированный список
     */
    public List<MusicBand> getSorted() {
        List<MusicBand> sorted = new ArrayList<>(collection);
        Collections.sort(sorted);
        return sorted;
    }

    /**
     * Возвращает тип коллекции.
     *
     * @return имя класса коллекции
     */
    public String getCollectionType() {
        return collection.getClass().getSimpleName();
    }

    /**
     * Возвращает дату инициализации коллекции.
     *
     * @return дата инициализации
     */
    public LocalDate getInitDate() {
        return initDate;
    }

    /**
     * Возвращает количество элементов в коллекции.
     *
     * @return размер коллекции
     */
    public int size() {
        return collection.size();
    }

    /**
     * Возвращает немодифицируемое представление коллекции.
     *
     * @return список элементов
     */
    public List<MusicBand> getCollection() {
        return Collections.unmodifiableList(collection);
    }
}