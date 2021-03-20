package cfh.zirconium.net;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import cfh.zirconium.gui.Main.Printer;

/** Bound station. */
public final class Bound extends Node {

    private final Set<Station> childs;
    
    /** Creates a bound station with given child stations. */
    public Bound(Printer printer, Station... childs) {
        super(printer);
        this.childs = new HashSet<>(Arrays.asList(childs));
        this.childs.forEach(s -> s.parent(this));
    }
    
    /** Child stations of this bound station. */
    public Collection<Station> childs() {
        return Collections.unmodifiableCollection(childs);
    }
    
    @Override
    public boolean isNeighbour(Station station) {
        return childs.stream().anyMatch(s -> s.isNeighbour(station));
    }

    /** Adds a bounded station to this bound station. */
    public void addChild(Station station) {
        childs.add(station);
        station.parent(this);
    }
    
    @Override
    public void reset() {
        childs.forEach(Station::reset);
    }

    @Override
    public void preTick() {
        childs.forEach(Station::preTick);
    }

    @Override
    public void tick() {
        childs.forEach(Station::tick);
    }
    
    @Override
    public void posTick() {
        childs.forEach(Station::posTick);
    }

    @Override
    protected int drones() {
        return childs.stream().mapToInt(Station::drones).sum();
    }

    @Override
    protected Collection<Station> linked() {
        return childs.stream().map(Station::linked).flatMap(Collection::stream).toList();
    }
}
