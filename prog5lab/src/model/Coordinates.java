package model;
/**
 * Класс, представляющий координаты местоположения музыкальной группы.
 */
public class Coordinates {

    /** Максимально допустимое значение координаты X. */
    public static final int MAX_X = 290;

    /** Координата X. Максимальное значение поля: 290. */
    private int x;

    /** Координата Y. Поле не может быть null. */
    private Integer y;

    /**
     * Создаёт объект координат.
     *
     * @param x координата X (не более {@value #MAX_X})
     * @param y координата Y (не может быть null)
     */
    public Coordinates(int x, Integer y) {
        this.x = x;
        this.y = y;
    }

    /**
     * Возвращает координату X.
     * @return координата X
     */
    public int getX() { return x; }

    /**
     * Возвращает координату Y.
     * @return координата Y
     */
    public Integer getY() { return y; }

    /**
     * Устанавливает координату X.
     * @param x новое значение (не более {@value #MAX_X})
     */
    public void setX(int x) { this.x = x; }

    /**
     * Устанавливает координату Y.
     * @param y новое значение
     */
    public void setY(Integer y) { this.y = y; }

    @Override
    public String toString() {
        return "Coordinates{x=" + x + ", y=" + y + "}";
    }
}