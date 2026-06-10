package db;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.sql.*;

/**
 * Менеджер логирования команд в первой БД (logs_db на порту 5432 в Docker).
 * 
 * Логирует все команды, которые выполняют пользователи.
 * Часть распределённой транзакции.
 * 
 * Порядок подключения: Docker контейнер postgres_logs на порту 5432
 */
public class LogManager {

    private static final Logger logger = LoggerFactory.getLogger(LogManager.class);
    
    private static final String URL = "jdbc:postgresql://localhost:5432/logs_db";
    private static final String USER = "postgres";
    private static final String PASSWORD = "postgres";


    private Connection connection;

    /**
     * Подключается к БД логов.
     * setAutoCommit(false) — ручное управление транзакциями!
     */
    public void connect() throws SQLException {
        connection = DriverManager.getConnection(URL, USER, PASSWORD);
        connection.setAutoCommit(false); 
        logger.info("Подключено к logs БД (PostgreSQL 5432).");
        createTableIfNotExists();
    }

    /**
     * Создаёт таблицу логов если её нет.
     */
    private void createTableIfNotExists() throws SQLException {
        String sql = """
            CREATE TABLE IF NOT EXISTS command_logs (
                id SERIAL PRIMARY KEY,
                user_login VARCHAR(100) NOT NULL,
                command_name VARCHAR(100) NOT NULL,
                timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                status VARCHAR(20) NOT NULL,
                message TEXT
            )
            """;
        try (Statement st = connection.createStatement()) {
            st.execute(sql);
            connection.commit();  
            logger.info("Таблица command_logs проверена/создана.");
        } catch (SQLException e) {
            connection.rollback();
            throw e;
        }
    }

    /**
     * Логирует команду БЕЗ коммита.
     * Коммит произойдёт позже, если всё успешно.
     */
    public void logCommand(String userLogin, String commandName, String status, String message) throws SQLException {
        String sql = "INSERT INTO command_logs (user_login, command_name, status, message) VALUES (?, ?, ?, ?)";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, userLogin);
            ps.setString(2, commandName);
            ps.setString(3, status); 
            ps.setString(4, message);
            ps.executeUpdate();
            logger.debug("Логирована команда: {} от {}", commandName, userLogin);
        }
    }

    /**
     * Коммитит лог в первой БД.
     * Вызывается ТОЛЬКО если вторая БД тоже успешно выполнила операцию.
     */
    public void commitLog() throws SQLException {
        connection.commit();
        logger.info("Логи закоммичены в первой БД (5432).");
    }

    /**
     * Откатывает лог при ошибке.
     * Откат происходит если произойдёт ошибка во второй БД.
     */
    public void rollbackLog() throws SQLException {
        try {
            connection.rollback();
            logger.warn("Откат логирования в первой БД (5432).");
        } catch (SQLException e) {
            logger.error("Ошибка при откате логирования: {}", e.getMessage());
        }
    }

    public void close() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
            }
        } catch (SQLException ignored) {}
    }
}