package moses;

/**
 * Represents a logistics hub (a "Node" in our supply chain graph).
 */
public class Location {
    private String id;
    private String name;

    public Location(String id, String name) {
        this.id = id;
        this.name = name;
    }

    public String getId() { return id; }
    public String getName() { return name; }

    @Override
    public String toString() {
        return name + " (" + id + ")";
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Location location = (Location) obj;
        return id.equals(location.id);
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }
}