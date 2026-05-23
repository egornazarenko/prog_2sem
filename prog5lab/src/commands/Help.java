package commands;

import managers.CommandManager;

/**
 * Команда {@code help} — вывод справки по всем доступным командам.
 */
public class Help implements Command {

    /** Менеджер команд, предоставляющий список команд. */
    private final CommandManager commandManager;

    /**
     * Создаёт команду help.
     *
     * @param commandManager менеджер команд
     */
    public Help(CommandManager commandManager) {
        this.commandManager = commandManager;
    }

    @Override
    public String getName() { return "help"; }

    @Override
    public String getDescription() { return "вывести справку по доступным командам"; }

    @Override
    public void execute(String[] args) {
        System.out.println(" Доступные команды ");
        for (Command cmd : commandManager.getCommands()) {
            System.out.printf("  %-50s %s%n", cmd.getName(), cmd.getDescription());
        }
    }
}
