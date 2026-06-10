package managers;

import db.DatabaseManager;
import model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.SQLException;
import java.time.LocalDate;
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;

/**
 * Менеджер коллекции.
 *
 * Все команды ЧТЕНИЯ работают с коллекцией в памяти (CopyOnWriteArrayList).
 * Все команды ЗАПИСИ сначала идут в БД, и только при успехе — обновляют память.
 *
 * CopyOnWriteArrayList обеспечивает потокобезопасное чтение без блокировок.
 * Запись синхронизирована через synchronized-методы.
 */
public class CollectionManager {

    private static final Logger logger = LoggerFactory.getLogger(CollectionManager.class);

    /** Потокобезопасная коллекция в памяти. */
    private final CopyOnWriteArrayList<MusicBand> collection = new CopyOnWriteArrayList<>();

    private final LocalDate initDate = LocalDate.now();
    private final DatabaseManager db;

    public CollectionManager(DatabaseManager db) {
        this.db = db;
    }

    /**
     * Загружает коллекцию из БД при старте сервера.
     */
    public void loadFromDatabase() throws SQLException {
        List<MusicBand> loaded = db.loadAll();
        collection.clear();
        collection.addAll(loaded);
        logger.info("Коллекция загружена из БД: {} элементов.", collection.size());
    }

    /**
     * Добавляет элемент: сначала в БД, потом в память.
     * ID назначается базой данных через sequence.
     *
     * @return добавленный объект с проставленным id
     */
    public synchronized MusicBand add(MusicBand band, String ownerLogin) throws SQLException {
        int id = db.insert(band, ownerLogin);
        band.setId(id);
        band.setCreationDate(LocalDate.now());
        collection.add(band);
        logger.debug("Добавлен элемент id={} владелец={}", id, ownerLogin);
        return band;
    }

    /**
     * Обновляет элемент: сначала в БД, потом в памяти.
     * Только владелец может обновить.
     */
    public synchronized boolean update(int id, MusicBand updated, String ownerLogin) throws SQLException {
        boolean dbUpdated = db.update(id, updated, ownerLogin);
        if (!dbUpdated) return false;

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
     * Удаляет по ID. Только владелец может удалить.
     */
    public synchronized boolean removeById(int id, String ownerLogin) throws SQLException {
        boolean dbDeleted = db.deleteById(id, ownerLogin);
        if (!dbDeleted) return false;
        collection.removeIf(b -> b.getId() == id);
        return true;
    }

    /**
     * Очищает коллекцию — удаляет только объекты текущего пользователя.
     */
    public synchronized int clear(String ownerLogin) throws SQLException {
        int deleted = db.deleteAllByOwner(ownerLogin);
        collection.removeIf(b -> true); // пересинхронизируем из БД
        collection.addAll(db.loadAll());
        return deleted;
    }

    /**
     * remove_greater — удаляет элементы пользователя, которые больше заданного.
     */
    public synchronized int removeGreater(MusicBand band, String ownerLogin) throws SQLException {
        List<Integer> toDelete = collection.stream()
                .filter(b -> b.compareTo(band) > 0)
                .map(MusicBand::getId)
                .collect(Collectors.toList());
        if (toDelete.isEmpty()) return 0;
        int deleted = db.deleteByIds(toDelete, ownerLogin);
        collection.removeIf(b -> toDelete.contains(b.getId()) &&
                db_isOwnerCached(b, ownerLogin));
        // пересинхронизация для точности
        resyncFromDb();
        return deleted;
    }

    /**
     * remove_lower — удаляет элементы пользователя, которые меньше заданного.
     */
    public synchronized int removeLower(MusicBand band, String ownerLogin) throws SQLException {
        List<Integer> toDelete = collection.stream()
                .filter(b -> b.compareTo(band) < 0)
                .map(MusicBand::getId)
                .collect(Collectors.toList());
        if (toDelete.isEmpty()) return 0;
        int deleted = db.deleteByIds(toDelete, ownerLogin);
        resyncFromDb();
        return deleted;
    }

    private boolean db_isOwnerCached(MusicBand b, String ownerLogin) {
        // Используется только как вспомогательный метод, реальная проверка в БД
        return true;
    }

    private void resyncFromDb() {
        try {
            List<MusicBand> fresh = db.loadAll();
            collection.clear();
            collection.addAll(fresh);
        } catch (SQLException e) {
            logger.error("Ошибка ресинхронизации с БД: {}", e.getMessage());
        }
    }


    public MusicBand getById(int id) {
        return collection.stream()
                .filter(b -> b.getId() == id)
                .findFirst().orElse(null);
    }

    public MusicBand getMin() {
        return collection.stream().min(MusicBand::compareTo).orElse(null);
    }

    public List<MusicBand> getSortedByName() {
        return collection.stream()
                .sorted(MusicBand::compareTo)
                .collect(Collectors.toList());
    }

    public List<MusicBand> getDescending() {
        return collection.stream()
                .sorted(Collections.reverseOrder())
                .collect(Collectors.toList());
    }

    public List<MusicBand> filterLessThanParticipants(int n) {
        return collection.stream()
                .filter(b -> b.getNumberOfParticipants() < n)
                .collect(Collectors.toList());
    }

    public long countByStudio(Studio studio) {
        return collection.stream()
                .filter(b -> studio == null ? b.getStudio() == null : studio.equals(b.getStudio()))
                .count();
    }

    public String getCollectionType() { return "CopyOnWriteArrayList"; }
    public LocalDate getInitDate()    { return initDate; }
    public int size()                 { return collection.size(); }
}
