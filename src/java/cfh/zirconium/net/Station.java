package cfh.zirconium.net;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

public abstract sealed class Station permits NopStation, CreateStation, DotStation {

    private final Pos pos;
    private final String string;
    
    private final Set<Station> linked = new HashSet<>();
    
    public Station(int x, int y) {
        this(new Pos(x, y));
    }
    
    public Station(Pos pos) {
        this.pos = Objects.requireNonNull(pos);
        this.string = String.format("%s[%s]", getClass().getSimpleName(), pos);
    }
    
    public Pos pos() { return pos; }
    
    public int x() { return pos.x(); }
    
    public int y() { return pos.y(); }
    
    @Override
    public int hashCode() {
        return Objects.hash(pos);
    }
    
    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        var other = (Station) obj;
        return Objects.equals(other.pos, this.pos);
    }
    
    @Override
    public String toString() {
        return string;
    }

    public void link(Station station) {
        if (!linked.add(station)) {
            System.err.printf("%s already linked to %s%n", this, station);
        } else {
            System.out.printf("%s linked to %s%n", this, station);
        }
    }
}
