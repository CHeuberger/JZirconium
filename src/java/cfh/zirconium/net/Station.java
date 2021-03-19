package cfh.zirconium.net;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

import cfh.zirconium.gui.Main.Printer;

public abstract sealed class Station permits 
NopStation, CreateStation, DotStation, DupStation, QStation, SplitStation
{

    private final Pos pos;
    private final Printer printer;
    private final String string;
    
    private final Set<Station> linked = new HashSet<>();
    
    private int tokens = 0;
    private boolean ticking = false;
    private int delta = 0;
    
    public Station(int x, int y, Printer printer) {
        this(new Pos(x, y), printer);
    }
    
    public Station(Pos pos, Printer printer) {
        this.pos = Objects.requireNonNull(pos);
        this.printer = Objects.requireNonNull(printer);
        this.string = String.format("%s[%s]", getClass().getSimpleName(), pos);
    }
    
    public Pos pos() { return pos; }
    
    public int x() { return pos.x(); }
    
    public int y() { return pos.y(); }
    
    public void link(Station station) {
        if (!linked.add(station)) {
            System.err.printf("%s already linked to %s%n", this, station);
        } else {
            System.out.printf("%s linked to %s%n", this, station);
        }
    }

    final void reset() {
        reset0();
        tokens = 0;
        delta = 0;
    }
    
    final void tick() {
        assert !ticking : "re-tick " + this;
        ticking = true;
        tick0();
    }
    
    final void tack() {
        assert ticking : "not ticking " + this;
        tack0();
        ticking = false;
        tokens += delta;
        printer.print("%s: added: %d, total: %d%n", this, delta, tokens);
        delta = 0;
    }

    protected final int tokens() {
        return tokens;
    }
    
    protected final int linked() {
        return linked.size();
    }
    
    protected final void send(int tokens) {
        assert ticking : "not ticking " + this;
        if (tokens < 0) {
            throw new IllegalArgumentException(this + ": negative tokens: " + tokens);
        }
        linked.forEach(s -> s.receive(tokens));
    }
    
    final void receive(int tokens) {
        if (tokens < 0) {
            throw new IllegalArgumentException(this + ": negative tokens: " + tokens);
        }
        delta += tokens;
    }
    
    protected void reset0() { /**/ }
    protected abstract void tick0();
    protected void tack0() { /**/ }
    
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
}
