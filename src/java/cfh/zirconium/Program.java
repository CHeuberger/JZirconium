package cfh.zirconium;

import java.io.Writer;
import java.util.Collection;
import java.util.Collections;
import java.util.Formatter;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

import cfh.zirconium.gui.Main.Printer;
import cfh.zirconium.net.Bound;
import cfh.zirconium.net.Single;
import cfh.zirconium.net.Station;

/** A program (map). */
public class Program {

    private final Settings settings = Settings.instance();
    
    private final String name;
    // TODO sourece?
    private final Set<Station> stations;
    private final Printer printer;
    
    private boolean started = false;

    /** Creates a program with given stations. */
    public Program(String name, Collection<Station> stations, Printer printer) {
        this.name = Objects.requireNonNull(name);
        this.stations = Collections.unmodifiableSet(new HashSet<>(stations));
        this.printer = Objects.requireNonNull(printer);
    }
    
    /** All stations. */
    public Collection<Station> stations() {
        return Collections.unmodifiableCollection(stations);
    }

    /** Creates DOT graph. */
    public void graph(Writer wr) {
        @SuppressWarnings("resource")
        var out = new Formatter(wr);
        out.format("digraph \"%s\" {%n", name);
        out.format("  fontname = \"%s\";%n", settings.codeFont().getName());
        out.format("  node [fontname = \"%s\"]%n", settings.codeFont().getName());
        out.format("  edge [fontname = \"%s\"]%n", settings.codeFont().getName());
        out.format("  concentrate = true;%n");
        out.format("  splines = true;%n");
        
        for (var station : stations) {
            if (station instanceof Single s) {
                out.format("  \"%s\" [label=\"%1$s\\n%d\"];%n", s, s.drones());
            } else if (station instanceof Bound b) {
                out.format("  subgraph \"cluster_%s\" {%n", b);
                out.format("    label = \"%s:%d\";%n", b, b.drones());
                b.stations().forEach(s -> out.format("    \"%s\" [label=\"%1$s\\n%d\"];%n", s, s.drones()));
                out.format("  }%n");
            }
        }
        out.format("%n");
        for (var station : stations) {
            station.stations().forEach(src -> {
                src.linked().forEach(dst -> out.format("  \"%s\" -> \"%s\";%n", src, dst));
            });
        }
        
        out.format("}");
    }
    
    /** Reset. */
    public void reset() {
        printer.print("reset %s%n", name);
        stations.forEach(Station::reset);
        started = false;
    }
    
    /** Executes a single step, starting if not already done. */
    public void step() {
        if (!started) {
            start();
        }
        printer.print("step %s%n", name);
        stations.forEach(Station::preTick);
        stations.forEach(Station::tick);
        stations.forEach(Station::posTick);
    }

    /** Starts the program, basically only resets all stations. */
    private void start() {
        printer.print("start %s%n", name);
        stations.forEach(Station::reset);
        started = true;
    }
}
