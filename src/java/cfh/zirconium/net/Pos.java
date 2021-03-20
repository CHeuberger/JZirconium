package cfh.zirconium.net;

/** Position on source code (column, row). */
public record Pos(int x, int y) implements Comparable<Pos> {
    
    @Override
    public String toString() {
        return String.format("(%d,%d)", y, x);
    }

    @Override
    public int compareTo(Pos other) {
        if (this.equals(other)) {
            return 0;
        } else {
            var comp = Integer.compare(this.y, other.y);
            return comp !=0 ? comp : Integer.compare(this.x, other.x);
        }
    }
}
