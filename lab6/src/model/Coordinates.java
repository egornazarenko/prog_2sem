package model;

import java.io.Serializable;

/**
 * Класс координат. Serializable для передачи по сети.
 */
public class Coordinates implements Serializable {
    private static final long serialVersionUID = 1L;

    public static final int MAX_X = 290;

    private int x;
    private Integer y;

    public Coordinates(int x, Integer y) {
        this.x = x;
        this.y = y;
    }

    public int getX() { return x; }
    public Integer getY() { return y; }
    public void setX(int x) { this.x = x; }
    public void setY(Integer y) { this.y = y; }

    @Override
    public String toString() {
        return "Coordinates{x=" + x + ", y=" + y + "}";
    }
}
