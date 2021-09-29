package cfh.zirconium;

import java.io.Writer;
import java.util.Collection;
import java.util.Collections;
import java.util.Formatter;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

import cfh.zirconium.net.Bound;
import cfh.zirconium.net.Single;
import cfh.zirconium.net.Station;

/** A program (map). */
public class Program {

    private static final String NODE = "%s\"%s\" [label=\"%2$s\\n%s\"];%n";
    
    private final Settings settings = Settings.instance();
    
    private final String name;
    // TODO sourece?
    private final Set<Station> stations;
    private final Environment env;
    
    private boolean started = false;
    // TODO step counter
    
    /** Creates a program with given stations. */
    public Program(String name, Collection<Station> stations, Environment env) {
        this.name = Objects.requireNonNull(name);
        this.stations = Collections.unmodifiableSet(new HashSet<>(stations));
        this.env = Objects.requireNonNull(env);
    }
    
    public String name() {
        return name;
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
                out.format(NODE, "  ", s, num(s.drones()));
            } else if (station instanceof Bound b) {
                out.format("  subgraph \"cluster_%s\" {%n", b);
                out.format("    label = \"%s %s\";%n", b, num(b.drones()));
                b.stations().forEach(s -> out.format(NODE, "    ", s, num(s.drones())));
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
    
    private String num(int num) {
        return num==0 ? "" : Integer.toString(num);
    }
    
    /** Reset. */
    public void reset() {
        stations.forEach(Station::reset);
        env.reset();
        started = false;
    }
    
    /** 
     * Executes a single step, starting if not already done.
     * @return {@code true} if the number of drones of no station was changeed 
     */
    public boolean step() {
        if (!started) {
            start();
        }
        if (!env.halted()) {
            stations.forEach(Station::preTick);
            stations.forEach(Station::tick);
            stations.forEach(Station::posTick);
            return stations.stream().flatMap(Station::stations).mapToInt(Single::delta).allMatch(i -> i == 0);
        }
        return true;
    }

    /** Starts the program, basically only resets all stations. */
    private void start() {
        env.start();
        stations.forEach(Station::reset);
        started = true;
    }
}
