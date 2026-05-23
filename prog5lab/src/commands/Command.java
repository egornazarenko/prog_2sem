package commands;

/**
 * Интерфейс команды приложения.
 */
public interface Command {

    /**
     * Возвращает имя команды (ключевое слово).
     *
     * @return имя команды
     */
    String getName();

    /**
     * Возвращает описание команды для справки.
     *
     * @return описание команды
     */
    String getDescription();

    /**
     * Выполняет команду.
     *
     * @param args аргументы, переданные после имени команды
     */
    void execute(String[] args);
}