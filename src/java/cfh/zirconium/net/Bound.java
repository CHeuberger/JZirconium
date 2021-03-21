package cfh.zirconium.net;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Stream;

import cfh.zirconium.gui.Main.Printer;

/** Bound station. */
public final class Bound extends Station {

    private final int id;
    private final Set<Single> childs;
    
    /** Creates a bound station with given child stations. 
     * @param id TODO*/
    public Bound(int id, Printer printer, Single... childs) {
        super(printer);
        this.id = id;
        this.childs = new HashSet<>(Arrays.asList(childs));
        this.childs.forEach(s -> s.parent(this));
    }
    
    /** Child stations of this bound station. */
    public Collection<Single> childs() {
        return Collections.unmodifiableCollection(childs);
    }
    
    @Override
    public Stream<Single> stations() {
        return childs.stream();
    }
    
    @Override
    public boolean isNeighbour(Single station) {
        return childs.stream().anyMatch(s -> s.isNeighbour(station));
    }

    /** Adds a bounded station to this bound station. */
    public void addChild(Single station) {
        childs.add(station);
        station.parent(this);
    }
    
    @Override
    public void reset() {
        childs.forEach(Single::reset);
    }

    @Override
    public void preTick() {
        childs.forEach(Single::preTick);
    }

    @Override
    public void tick() {
        childs.forEach(Single::tick);
    }
    
    @Override
    public void posTick() {
        childs.forEach(Single::posTick);
    }

    @Override
    public int drones() {
        return childs.stream().mapToInt(Single::drones).sum();
    }

    @Override
    protected Collection<Single> linked() {
        return childs.stream().map(Single::linked).flatMap(Collection::stream).toList();
    }
    
    @Override
    public String toString() {
        return "{" + id + "}";
    }
}
