package cfh.zirconium.net;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

public abstract class Station {

    private final int row;
    private final int col;
    private final String string;
    
    private final Set<Station> linked = new HashSet<>();
    
    public Station(int row, int col) {
        this.row = row;
        this.col = col;
        this.string = String.format("%s[%d,%d]", getClass().getSimpleName(), row, col);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(row, col);
    }
    
    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        var other = (Station) obj;
        return other.row == this.row && other.col == this.col;
    }
    
    @Override
    public String toString() {
        return string;
    }
}
