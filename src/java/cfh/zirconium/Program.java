package cfh.zirconium;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

import cfh.zirconium.gui.Main.Printer;
import cfh.zirconium.net.Node;
import cfh.zirconium.net.Station;

public class Program {

    private final String name;
    private final Set<Node> nodes;
    private final Printer printer;
    
    private boolean started = false;

    public Program(String name, Collection<Station> stations, Printer printer) {
        this.name = Objects.requireNonNull(name);
        this.nodes = Collections.unmodifiableSet(new HashSet<>(stations));
        this.printer = Objects.requireNonNull(printer);
    }
    
    public void step() {
        if (!started) {
            start();
        } else {
            printer.print("tick %s%n", name);
            nodes.forEach(Node::tick);
            printer.print("tack %s%n", name);
            nodes.forEach(Node::tack);
        }
    }

    private void start() {
        printer.print("start %s%n", name);
        reset();
        started = true;
    }
    
    private void reset() {
        printer.print("reset %s%n", name);
        nodes.forEach(Node::reset);
    }
}
