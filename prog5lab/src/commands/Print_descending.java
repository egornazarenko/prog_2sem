package commands;

import managers.CollectionManager;
import model.*;

import java.util.List;

/**
 * Команда {@code print_descending} — вывод элементов в порядке убывания.
 */
public class Print_descending implements Command {

    /** Менеджер коллекции. */
    private final CollectionManager collectionManager;

    /**
     * Создаёт команду print_descending.
     *
     * @param collectionManager менеджер коллекции
     */
    public Print_descending(CollectionManager collectionManager) {
        this.collectionManager = collectionManager;
    }

    @Override
    public String getName() { return "print_descending"; }

    @Override
    public String getDescription() {
        return "print_descending : вывести элементы коллекции в порядке убывания";
    }

    @Override
    public void execute(String[] args) {
        List<MusicBand> desc = collectionManager.getDescending();
        if (desc.isEmpty()) {
            System.out.println("Коллекция пуста.");
            return;
        }
        System.out.println(" Элементы в порядке убывания");
        desc.forEach(System.out::println);
    }
}
