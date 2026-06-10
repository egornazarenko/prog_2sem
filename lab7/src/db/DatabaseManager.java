package db;
 
import model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
 
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
 
/**
 * Менеджер базы данных.
 * Отвечает за подключение к PostgreSQL, создание таблиц,
 * операции с пользователями и объектами коллекции.
 *
 * Пароли хранятся с солью и перцем:
 * hash = SHA256(password + salt + PEPPER)
 */
public class DatabaseManager {
 
    private static final Logger logger = LoggerFactory.getLogger(DatabaseManager.class);
 
    private static final String URL = "jdbc:postgresql://localhost:5433/data_db";
    private static final String USER = "postgres";
    private static final String PASSWORD = "postgres";
 
    /** Перец — фиксированная секретная строка (хранится в коде, не в БД). */
    private static final String PEPPER = "security_pepper_2026_lab7_key";
 
    private Connection connection;
 
    /**
     * Подключается к БД и создаёт таблицы если их нет.
     */
    public void connect() throws SQLException {
        connection = DriverManager.getConnection(URL, USER, PASSWORD);
        connection.setAutoCommit(false);  
        logger.info("Подключено к data БД (PostgreSQL 5433).");
        createTablesIfNotExist();
    }

    /**
     * Коммитит данные во второй БД.
     */
    public void commitData() throws SQLException {
        connection.commit();
        logger.info("Данные закоммичены во второй БД.");
    }

    /**
     * Откатывает данные при ошибке.
     */
    public void rollbackData() throws SQLException {
        try {
            connection.rollback();
            logger.warn("Откат данных во второй БД.");
        } catch (SQLException e) {
            logger.error("Ошибка при откате данных: {}", e.getMessage());
        }
    }

    /**
     * Создаёт таблицы users и music_bands, sequence для id.
     */
    private void createTablesIfNotExist() throws SQLException {
        String createUsers = """
            CREATE TABLE IF NOT EXISTS users (
                id       SERIAL PRIMARY KEY,
                login    VARCHAR(100) UNIQUE NOT NULL,
                password VARCHAR(64)  NOT NULL,
                salt     VARCHAR(32)  NOT NULL
            )
            """;
 
        String createSequence = """
            CREATE SEQUENCE IF NOT EXISTS music_band_id_seq
                START WITH 1 INCREMENT BY 1
            """;
 
        String createBands = """
            CREATE TABLE IF NOT EXISTS music_bands (
                id                     INTEGER PRIMARY KEY DEFAULT nextval('music_band_id_seq'),
                name                   VARCHAR(200) NOT NULL,
                coord_x                INTEGER NOT NULL,
                coord_y                INTEGER NOT NULL,
                creation_date          DATE NOT NULL,
                number_of_participants INTEGER NOT NULL,
                singles_count          INTEGER,
                albums_count           INTEGER NOT NULL,
                genre                  VARCHAR(20) NOT NULL,
                studio_name            VARCHAR(200),
                rating                 DOUBLE PRECISION NOT NULL,
                owner_login            VARCHAR(100) NOT NULL REFERENCES users(login)
            )
            """;
 
        try (Statement st = connection.createStatement()) {
            st.execute(createUsers);
            st.execute(createSequence);
            st.execute(createBands);
            logger.info("Таблицы проверены/созданы.");
        }
    }

    /**
     * Генерирует случайную соль (16 байт в hex формате).
     */
    public static String generateSalt() {
        byte[] salt = new byte[16];
        new Random().nextBytes(salt);
        StringBuilder sb = new StringBuilder();
        for (byte b : salt) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }
 
    /**
     * Хеширует пароль с солью и перцем.
     * hash = SHA256(password + salt + PEPPER)
     */
    public static String hashPassword(String password, String salt) {
        String combined = password + salt + PEPPER;
        return sha256(combined);
    }
 
    /**
     * Хеширует строку алгоритмом SHA-256.
     */
    public static String sha256(String input) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hash = md.digest(input.getBytes(java.nio.charset.StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            for (byte b : hash) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 недоступен", e);
        }
    }
 
    /**
     * Регистрирует нового пользователя.
     * Генерирует соль, хеширует пароль с солью и перцем.
     *
     * @return true если успешно, false если логин уже занят
     */
    public boolean registerUser(String login, String password) throws SQLException {
        String salt = generateSalt();
        String hash = hashPassword(password, salt);
        
        String sql = "INSERT INTO users (login, password, salt) VALUES (?, ?, ?) ON CONFLICT DO NOTHING";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, login);
            ps.setString(2, hash);
            ps.setString(3, salt);
            int rows = ps.executeUpdate();
            return rows > 0;
        }
    }
 
    /**
     * Проверяет логин и пароль.
     * Загружает соль пользователя из БД, хеширует пароль и сравнивает.
     *
     * @return true если авторизация успешна
     */
    public boolean authenticate(String login, String password) throws SQLException {
        String sql = "SELECT salt FROM users WHERE login = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, login);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    return false; 
                }
                String salt = rs.getString("salt");
                String hash = hashPassword(password, salt);
                
                String sql2 = "SELECT 1 FROM users WHERE login = ? AND password = ?";
                try (PreparedStatement ps2 = connection.prepareStatement(sql2)) {
                    ps2.setString(1, login);
                    ps2.setString(2, hash);
                    try (ResultSet rs2 = ps2.executeQuery()) {
                        return rs2.next();
                    }
                }
            }
        }
    }
 
    /**
     * Загружает все объекты из БД.
     */
    public List<MusicBand> loadAll() throws SQLException {
        List<MusicBand> list = new ArrayList<>();
        String sql = "SELECT * FROM music_bands ORDER BY name";
        try (Statement st = connection.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                list.add(fromResultSet(rs));
            }
        }
        logger.info("Загружено {} элементов из БД.", list.size());
        return list;
    }
 
    /**
     * Добавляет новый объект в БД. ID назначается через sequence.
     *
     * @return сгенерированный ID
     */
    public int insert(MusicBand band, String ownerLogin) throws SQLException {
        String sql = """
            INSERT INTO music_bands
              (name, coord_x, coord_y, creation_date,
               number_of_participants, singles_count, albums_count,
               genre, studio_name, rating, owner_login)
            VALUES (?,?,?,?,?,?,?,?,?,?,?)
            """;
        try (PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, band.getName());
            ps.setInt(2, band.getCoordinates().getX());
            ps.setInt(3, band.getCoordinates().getY());
            ps.setDate(4, Date.valueOf(LocalDate.now()));
            ps.setInt(5, band.getNumberOfParticipants());
            if (band.getSinglesCount() != null) ps.setInt(6, band.getSinglesCount());
            else ps.setNull(6, Types.INTEGER);
            ps.setInt(7, band.getAlbumsCount());
            ps.setString(8, band.getGenre().name());
            if (band.getStudio() != null) ps.setString(9, band.getStudio().getName());
            else ps.setNull(9, Types.VARCHAR);
            ps.setDouble(10, band.getRating());
            ps.setString(11, ownerLogin);
            ps.executeUpdate();
 
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) return keys.getInt(1);
            }
        }
        throw new SQLException("Не удалось получить сгенерированный ID.");
    }
 
    /**
     * Обновляет объект в БД. Только владелец может обновить.
     *
     * @return true если строка обновлена
     */
    public boolean update(int id, MusicBand band, String ownerLogin) throws SQLException {
        String sql = """
            UPDATE music_bands SET
              name=?, coord_x=?, coord_y=?,
              number_of_participants=?, singles_count=?, albums_count=?,
              genre=?, studio_name=?, rating=?
            WHERE id=? AND owner_login=?
            """;
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, band.getName());
            ps.setInt(2, band.getCoordinates().getX());
            ps.setInt(3, band.getCoordinates().getY());
            ps.setInt(4, band.getNumberOfParticipants());
            if (band.getSinglesCount() != null) ps.setInt(5, band.getSinglesCount());
            else ps.setNull(5, Types.INTEGER);
            ps.setInt(6, band.getAlbumsCount());
            ps.setString(7, band.getGenre().name());
            if (band.getStudio() != null) ps.setString(8, band.getStudio().getName());
            else ps.setNull(8, Types.VARCHAR);
            ps.setDouble(9, band.getRating());
            ps.setInt(10, id);
            ps.setString(11, ownerLogin);
            return ps.executeUpdate() > 0;
        }
    }
 
    /**
     * Удаляет объект по ID. Только владелец может удалить.
     *
     * @return true если строка удалена
     */
    public boolean deleteById(int id, String ownerLogin) throws SQLException {
        String sql = "DELETE FROM music_bands WHERE id=? AND owner_login=?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.setString(2, ownerLogin);
            return ps.executeUpdate() > 0;
        }
    }
 
    /**
     * Удаляет все объекты пользователя.
     */
    public int deleteAllByOwner(String ownerLogin) throws SQLException {
        String sql = "DELETE FROM music_bands WHERE owner_login=?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, ownerLogin);
            return ps.executeUpdate();
        }
    }
 
    /**
     * Удаляет объекты пользователя по списку id.
     */
    public int deleteByIds(List<Integer> ids, String ownerLogin) throws SQLException {
        if (ids.isEmpty()) return 0;
        StringBuilder sb = new StringBuilder("DELETE FROM music_bands WHERE owner_login=? AND id IN (");
        for (int i = 0; i < ids.size(); i++) {
            sb.append(i == 0 ? "?" : ",?");
        }
        sb.append(")");
        try (PreparedStatement ps = connection.prepareStatement(sb.toString())) {
            ps.setString(1, ownerLogin);
            for (int i = 0; i < ids.size(); i++) ps.setInt(i + 2, ids.get(i));
            return ps.executeUpdate();
        }
    }
 
    /**
     * Проверяет, является ли пользователь владельцем объекта.
     */
    public boolean isOwner(int id, String login) throws SQLException {
        String sql = "SELECT 1 FROM music_bands WHERE id=? AND owner_login=?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.setString(2, login);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }
 
    private MusicBand fromResultSet(ResultSet rs) throws SQLException {
        int id = rs.getInt("id");
        String name = rs.getString("name");
        int x = rs.getInt("coord_x");
        int y = rs.getInt("coord_y");
        LocalDate date = rs.getDate("creation_date").toLocalDate();
        int participants = rs.getInt("number_of_participants");
        Integer singles = rs.getObject("singles_count", Integer.class);
        int albums = rs.getInt("albums_count");
        MusicGenre genre = MusicGenre.valueOf(rs.getString("genre"));
        String studioName = rs.getString("studio_name");
        Studio studio = studioName != null ? new Studio(studioName) : null;
        double rating = rs.getDouble("rating");
 
        return new MusicBand(id, name, new Coordinates(x, y), date,
                participants, singles, albums, genre, studio, rating);
    }
 
    public void close() {
        try {
            if (connection != null && !connection.isClosed()) connection.close();
        } catch (SQLException ignored) {}
    }
}
 