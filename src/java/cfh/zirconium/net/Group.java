package cfh.zirconium.net;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import cfh.zirconium.gui.Main.Printer;

public final class Group extends Node {

    private final Set<Station> child;
    
    protected Group(Printer printer) {
        super(printer);
        child = new HashSet<>();
    }

    @Override
    public void reset() {
        child.forEach(Station::reset);
    }

    @Override
    public void tick() {
        child.forEach(Station::tick);
    }

    @Override
    public void tack() {
        child.forEach(Station::tack);
    }

    @Override
    protected int ownTokens() {
        return child.stream().mapToInt(Station::ownTokens).sum();
    }

    @Override
    protected Collection<Station> linked() {
        return child.stream().map(Station::linked).flatMap(Collection::stream).toList();
    }
}
