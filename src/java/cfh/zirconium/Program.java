package cfh.zirconium;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

import cfh.zirconium.gui.Main.Printer;
import cfh.zirconium.net.Node;

/** A program (map). */
public class Program {

    private final String name;
    // TODO sourece?
    private final Set<Node> nodes;
    private final Printer printer;
    
    private boolean started = false;

    /** Creates a program with given stations. */
    public Program(String name, Collection<Node> stations, Printer printer) {
        this.name = Objects.requireNonNull(name);
        this.nodes = Collections.unmodifiableSet(new HashSet<>(stations));
        this.printer = Objects.requireNonNull(printer);
    }
    
    /** Executes a single step, starting if not already done. */
    public void step() {
        if (!started) {
            start();
        }
        printer.print("step %s%n", name);
        nodes.forEach(Node::preTick);
        nodes.forEach(Node::tick);
        nodes.forEach(Node::posTick);
    }

    /** Starts the program, basically only resets all stations. */
    private void start() {
        printer.print("start %s%n", name);
        nodes.forEach(Node::reset);
        started = true;
    }
}
