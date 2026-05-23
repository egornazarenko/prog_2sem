package commands;

import managers.CollectionManager;
import model.*;

import java.util.List;

/**
 * Команда {@code filter_less_than_number_of_participants numberOfParticipants} —
 * вывод элементов с numberOfParticipants меньше заданного.
 */
public class Filter_less_than_numb_of_participations implements Command {

    /** Менеджер коллекции. */
    private final CollectionManager collectionManager;

    /**
     * Создаёт команду filter_less_than_number_of_participants.
     *
     * @param collectionManager менеджер коллекции
     */
    public Filter_less_than_numb_of_participations(CollectionManager collectionManager) {
        this.collectionManager = collectionManager;
    }

    @Override
    public String getName() { return "filter_less_than_number_of_participants"; }

    @Override
    public String getDescription() {
        return "filter_less_than_number_of_participants n : вывести элементы с числом участников < n";
    }

    @Override
    public void execute(String[] args) {
        if (args.length < 1) {
            System.err.println("Использование: filter_less_than_number_of_participants <число>");
            return;
        }
        try {
            int n = Integer.parseInt(args[0].trim());
            List<MusicBand> result = collectionManager.filterLessThanNumberOfParticipants(n);
            if (result.isEmpty()) {
                System.out.println("Нет элементов с numberOfParticipants < " + n + ".");
            } else {
                System.out.println("Элементы с numberOfParticipants < " + n + ":");
                result.forEach(System.out::println);
            }
        } catch (NumberFormatException e) {
            System.err.println("Ошибка: numberOfParticipants должен быть целым числом.");
        }
    }
}
