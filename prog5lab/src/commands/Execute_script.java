package commands;

import managers.CommandManager;
import managers.InputManager;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.HashSet;
import java.util.Scanner;
import java.util.Set;

/**
 * Команда {@code execute_script file_name} — исполнение скрипта из файла.
 * Предотвращает рекурсивный вызов одного и того же скрипта.
 */
public class Execute_script implements Command {

    /** Менеджер команд для исполнения команд скрипта. */
    private final CommandManager commandManager;

    /** Менеджер ввода для подмены сканера. */
    private final InputManager inputManager;

    /**
     * Множество абсолютных путей к скриптам, исполняющимся в данный момент.
     * Используется для предотвращения рекурсии.
     */
    private static final Set<String> executingScripts = new HashSet<>();

    /**
     * Создаёт команду execute_script.
     *
     * @param commandManager менеджер команд
     * @param inputManager   менеджер ввода
     */
    public Execute_script(CommandManager commandManager, InputManager inputManager) {
        this.commandManager = commandManager;
        this.inputManager = inputManager;
    }

    @Override
    public String getName() { return "execute_script"; }

    @Override
    public String getDescription() {
        return "execute_script file_name : считать и исполнить скрипт из указанного файла";
    }

    @Override
    public void execute(String[] args) {
        if (args.length < 1) {
            System.err.println("Использование: execute_script <имя_файла>");
            return;
        }

        File file = new File(args[0].trim());

        if (!file.exists()) {
            System.err.println("Ошибка: файл скрипта не найден: " + file.getPath());
            return;
        }
        if (!file.canRead()) {
            System.err.println("Ошибка: нет прав на чтение файла: " + file.getPath());
            return;
        }

        String absolutePath = file.getAbsolutePath();
        if (executingScripts.contains(absolutePath)) {
            System.err.println("Ошибка: обнаружена рекурсия в скрипте: " + absolutePath);
            return;
        }

        executingScripts.add(absolutePath);
        System.out.println("Выполнение скрипта: " + absolutePath);

        try (Scanner fileScanner = new Scanner(new FileInputStream(file))) {
            inputManager.pushScanner(fileScanner);
            commandManager.runScriptMode();
        } catch (IOException e) {
            System.err.println("Ошибка при чтении скрипта: " + e.getMessage());
        } finally {
            executingScripts.remove(absolutePath);
            if (inputManager.isScriptMode()) {
                inputManager.popScanner();
            }
        }

        System.out.println("Скрипт завершён: " + absolutePath);
    }
}
