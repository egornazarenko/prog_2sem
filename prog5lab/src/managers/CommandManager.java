package managers;

import commands.*;

import java.util.*;

/**
 * Менеджер команд. Регистрирует, хранит и выполняет команды приложения.
 * Управляет интерактивным режимом и режимом исполнения скриптов.
 */
public class CommandManager {

    /** Карта зарегистрированных команд: имя → команда. */
    private final Map<String, Command> commandMap = new LinkedHashMap<>();

    /** Менеджер коллекции. */
    private final CollectionManager collectionManager;

    /** Менеджер файлов. */
    private final FileManager fileManager;

    /** Менеджер ввода. */
    private final InputManager inputManager;

    /**
     * Создаёт менеджер команд и регистрирует все доступные команды.
     *
     * @param collectionManager менеджер коллекции
     * @param fileManager       менеджер файлов
     * @param inputManager      менеджер ввода
     */
    public CommandManager(CollectionManager collectionManager,FileManager fileManager, InputManager inputManager) {
        this.collectionManager = collectionManager;
        this.fileManager = fileManager;
        this.inputManager = inputManager;
        registerCommands();
    }

    /**
     * Регистрирует все команды приложения.
     */
    private void registerCommands() {
        register(new Help(this));
        register(new Info(collectionManager));
        register(new Show(collectionManager));
        register(new Add(collectionManager, inputManager));
        register(new Update(collectionManager, inputManager));
        register(new Remove(collectionManager));
        register(new Clear(collectionManager));
        register(new Save(collectionManager));
        register(new Execute_script(this, inputManager));
        register(new Exit());
        register(new Add_if_min(collectionManager, inputManager));
        register(new Remove_greater(collectionManager, inputManager));
        register(new Remove_lower(collectionManager, inputManager));
        register(new Count(collectionManager));
        register(new Filter_less_than_numb_of_participations(collectionManager));
        register(new Print_descending(collectionManager));
    }

    /**
     * Регистрирует команду.
     *
     * @param command команда для регистрации
     */
    private void register(Command command) {
        commandMap.put(command.getName(), command);
    }

    /**
     * Возвращает коллекцию всех зарегистрированных команд.
     *
     * @return коллекция команд
     */
    public Collection<Command> getCommands() {
        return commandMap.values();
    }

    /**
     * Выполняет команду по введённой строке.
     *
     * @param line строка ввода (имя команды + аргументы)
     * @return {@code true} если команда выполнена, {@code false} если команда не найдена
     */
    public boolean execute(String line) {
        if (line == null || line.trim().isEmpty()) return false;

        String[] parts = line.trim().split("\\s+", 2);
        String commandName = parts[0].toLowerCase();
        String[] args = parts.length > 1 ? parts[1].trim().split("\\s+") : new String[0];

        Command command = commandMap.get(commandName);
        if (command == null) {
            System.err.println("Неизвестная команда: '" + commandName
                               + "'. Введите 'help' для справки.");
            return false;
        }

        command.execute(args);
        return true;
    }

    /**
     * Запускает интерактивный режим работы приложения.
     * Читает команды из стандартного ввода до команды exit.
     */
    public void runInteractiveMode() {
        System.out.println("Приложение запущено. Введите 'help' для списка команд.");
        while (true) {
            System.out.print(">> ");
            String line = inputManager.readLine();
            if (line == null) {
                System.out.println("\nКонец потока ввода. Завершение программы.");
                break;
            }
            execute(line);
        }
    }

    /**
     * Запускает режим исполнения скрипта.
     * Читает и выполняет команды до конца файла или команды exit.
     */
    public void runScriptMode() {
        while (inputManager.hasNextLine()) {
            String line = inputManager.readLine();
            if (line != null) {
                execute(line);
            }
        }
    }
}
