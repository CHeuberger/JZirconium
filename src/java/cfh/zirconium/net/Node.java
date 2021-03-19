package cfh.zirconium.net;

import java.util.Collection;
import java.util.Objects;

import cfh.zirconium.gui.Main.Printer;

public sealed abstract class Node 
permits Station, Group {

    protected final Printer printer;

    protected Node(Printer printer) {
        this.printer = Objects.requireNonNull(printer);
    }
    
    protected abstract int ownTokens();
    
    protected abstract Collection<Station> linked();
    
    public abstract void reset();

    public abstract void tick();

    public abstract void tack();
}
