package cfh.zirconium.net;

/** Position on source code (column, row). */
public record Pos(int x, int y) {
    
    @Override
    public String toString() {
        return String.format("(%d,%d)", y, x);
    }
}
