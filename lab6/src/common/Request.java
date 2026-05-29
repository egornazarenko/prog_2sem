package common;

import model.MusicBand;
import java.io.Serializable;

/**
 * Объект запроса от клиента к серверу.
 * Содержит имя команды, строковый аргумент и (опционально) объект MusicBand.
 */
public class Request implements Serializable {
    private static final long serialVersionUID = 1L;

    /** Имя команды. */
    private final String commandName;

    /** Строковый аргумент (например, id). */
    private final String argument;

    /** Объект музыкальной группы (для команд add, update и т.д.). */
    private final MusicBand musicBand;

    public Request(String commandName, String argument, MusicBand musicBand) {
        this.commandName = commandName;
        this.argument = argument;
        this.musicBand = musicBand;
    }

    public Request(String commandName, String argument) {
        this(commandName, argument, null);
    }

    public Request(String commandName) {
        this(commandName, null, null);
    }

    public String getCommandName() { return commandName; }
    public String getArgument() { return argument; }
    public MusicBand getMusicBand() { return musicBand; }

    @Override
    public String toString() {
        return "Request{command='" + commandName + "', arg='" + argument + "'}";
    }
}
