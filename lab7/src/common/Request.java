package common;

import model.MusicBand;
import java.io.Serializable;

/**
 * Объект запроса от клиента к серверу.
 * Содержит имя команды, аргумент, объект MusicBand и учётные данные пользователя.
 */
public class Request implements Serializable {
    private static final long serialVersionUID = 1L;

    private final String commandName;
    private final String argument;
    private final MusicBand musicBand;

    /** Логин пользователя — отправляется с каждым запросом. */
    private final String login;

    /** Пароль пользователя (plain-text, хэшируется на сервере). */
    private final String password;

    public Request(String commandName, String argument, MusicBand musicBand,
                   String login, String password) {
        this.commandName = commandName;
        this.argument = argument;
        this.musicBand = musicBand;
        this.login = login;
        this.password = password;
    }

    public Request(String commandName, String argument, String login, String password) {
        this(commandName, argument, null, login, password);
    }

    public Request(String commandName, String login, String password) {
        this(commandName, null, null, login, password);
    }

    /** Для команд register/login — без musicBand. */
    public static Request forAuth(String commandName, String login, String password) {
        return new Request(commandName, null, null, login, password);
    }

    public String getCommandName() { return commandName; }
    public String getArgument()    { return argument; }
    public MusicBand getMusicBand(){ return musicBand; }
    public String getLogin()       { return login; }
    public String getPassword()    { return password; }

    @Override
    public String toString() {
        return "Request{cmd='" + commandName + "', arg='" + argument + "', login='" + login + "'}";
    }
}
