package commands;

/**
 * Команда {@code exit} — завершение программы без сохранения.
 */
public class Exit implements Command {

    @Override
    public String getName() { return "exit"; }

    @Override
    public String getDescription() { return "завершить программу (без сохранения в файл)"; }

    @Override
    public void execute(String[] args) {
        System.out.println("Завершение программы. До свидания!");
        System.exit(0);
    }
}
