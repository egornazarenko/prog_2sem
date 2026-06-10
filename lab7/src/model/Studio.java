package model;

import java.io.Serializable;

/**
 * Студия звукозаписи. Serializable для передачи по сети.
 */
public class Studio implements Serializable {
    private static final long serialVersionUID = 1L;

    private String name;

    public Studio(String name) {
        this.name = name;
    }

    public String getName() { return name; }
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
