package cfh.zirconium.net;

import static java.lang.Math.*;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

import cfh.zirconium.gui.Main.Printer;

// TODO rename to Single
/** Single station. */
public abstract sealed class Station extends Node
permits NopStation, CreateStation, DotStation, DupStation, QStation, SplitStation {

    private final Pos pos;
    
    private Bound parent;
    private final Set<Station> linked = new HashSet<>();
    
    private int drones = 0;
    
    private boolean ticking = false;
    private int previous = 0;
    
    /** Creates a new station. */
    public Station(int x, int y, Printer printer) {
        this(new Pos(x, y), printer);
    }
    
    /** Creates a new station. */
    public Station(Pos pos, Printer printer) {
        super(printer);
        this.pos = Objects.requireNonNull(pos);
    }
    
    /** Links this station to given station (destination). */
    public void linkTo(Station station) {
        if (!linked.add(station)) {
            System.err.printf("%s already linked to %s%n", this, station);
        } else {
            System.out.printf("%s linked to %s%n", this, station);
        }
    }
    
    /** Sets the bound station this station is in. */
    public void parent(Bound bound) {
        parent = Objects.requireNonNull(bound);
    }
    
    /** Position of this station. */
    public Pos pos() { return pos; }
    
    /** Column of this station. */
    public int x() { return pos.x(); }
    
    /** Row fo this station. */
    public int y() { return pos.y(); }

    @Override
    protected final int drones() {
        return ticking ? previous : drones;
    }
    
    @Override
    public boolean isNeighbour(Station station) {
        return station != this 
            && abs(station.pos.x()-this.pos.x()) <= 1 
            && abs(station.pos.y()-this.pos.y()) <= 1;
    }
    
    /** Total number of drones, including drones of bounded siblings, see {@link #drones}. */
    protected final int total() {
        return (parent==null ? this : parent).drones();
    }

    @Override
    protected Collection<Station> linked() {
        // TODO dest parent.childs
        return Collections.unmodifiableCollection(linked);
    }
    
    /** Sends a number of drones to aeach linked station. */
    protected final void send(int number) {
        assert ticking : "not ticking " + this;
        if (number < 0) {
            throw new IllegalArgumentException(this + ": negative drones: " + number);
        }
        (parent==null ? this : parent).linked().forEach(s -> s.receive(number));
    }
    
    /** Receive number of drones. */
    final void receive(int number) {
        if (number < 0) {
            throw new IllegalArgumentException(this + ": negative drones: " + number);
        }
        this.drones += number;
    }

    @Override
    public final void reset() {
        reset0();
        drones = 0;
        previous = 0;
    }

    @Override
    public final void preTick() {
        assert !ticking : "re-tick " + this;
        ticking = true;
        previous = drones;
        drones = 0;
        preTick0();
    }

    @Override
    public final void tick() {
        assert ticking : "not ticking " + this;
        tick0();
    }
    
    @Override
    public void posTick() {
        assert ticking : "not ticking " + this;
        posTick0();
        ticking = false;
        printer.print("%s %d => %d%n", this, previous, drones);
    }

    /** {@link #reset} to be overriden by subclass. */
    protected void reset0() { /**/ }
    /** {@link #preTick} to be overriden by subclass. */
    protected void preTick0() { /**/ }
    /** {@link #tick} to be overriden by subclass. */
    protected abstract void tick0();
    /** {@link #posTick} to be overriden by subclass. */
    protected void posTick0() { /**/ }
    
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
        return String.format("%s[%s]", getClass().getSimpleName(), pos);
    }
}
