package model;

/**
 * Класс, представляющий студию звукозаписи.
 */
public class Studio {

    /** Название студии. Поле не может быть null. */
    private String name;

    /**
     * Создаёт студию с заданным названием.
     *
     * @param name название студии (не может быть null)
     */
    public Studio(String name) {
        this.name = name;
    }

    /**
     * Возвращает название студии.
     * @return название студии
     */
    public String getName() { return name; }

    /**
     * Устанавливает название студии.
     * @param name новое название
     */
    public void setName(String name) { this.name = name; }

    @Override
    public String toString() {
        return "Studio{name='" + name + "'}";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Studio)) return false;
        Studio other = (Studio) o;
        return name != null && name.equals(other.name);
    }

    @Override
    public int hashCode() {
        return name != null ? name.hashCode() : 0;
    }
}