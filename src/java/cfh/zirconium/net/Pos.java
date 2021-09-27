package cfh.zirconium.net;

/** Position on source code (column, row). */
public class Pos implements Comparable<Pos> {
    
    private final int x;
    private final int y;
    
    public Pos(int x, int y) {
        this.x = x;
        this.y = y;
    }
    
    public int x() { return x; }
    public int y() { return y; }
    
    @Override
    public String toString() {
        return String.format("(%d,%d)", y, x);
    }
    
    @Override
    public int hashCode() { return 37*x + 17*y; }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        Pos other = (Pos) obj;
        return other.x == this.x && other.y == this.y;
    }
    
    @Override
    public int compareTo(Pos other) {
        if (this.equals(other)) {
            return 0;
        } else {
            int comp = Integer.compare(this.y, other.y);
            return comp !=0 ? comp : Integer.compare(this.x, other.x);
        }
    }
}
