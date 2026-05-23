import managers.CollectionManager;
import managers.CommandManager;
import managers.FileManager;
import managers.InputManager;

import java.util.Scanner;


public class Main {

    /**
     * Точка входа в приложение.
     *
     * @param args аргументы командной строки (не используются)
     */
    public static void main(String[] args) {
        String fileName = System.getenv("MUSIC_BAND_FILE");
        if (fileName == null || fileName.trim().isEmpty()) {
            System.err.println("Ошибка: переменная окружения MUSIC_BAND_FILE не установлена.");
            System.err.println("Установите её перед запуском программы.");
            System.exit(1);
        }

        FileManager fileManager = new FileManager(fileName.trim());
        CollectionManager collectionManager = new CollectionManager(fileManager);
        collectionManager.loadCollection();

        InputManager inputManager = new InputManager(new Scanner(System.in));
        CommandManager commandManager = new CommandManager(collectionManager, fileManager, inputManager);

        commandManager.runInteractiveMode();
    }
}