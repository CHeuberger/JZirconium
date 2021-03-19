package cfh.zirconium.net;

import java.util.Collections;
import java.util.Map;
import java.util.Objects;

import cfh.zirconium.gui.Main.Printer;

public class Program {

    private final String name;
    private final Map<Pos, Station> stations;
    private final Printer printer;
    
    private boolean started = false;

    public Program(String name, Map<Pos, Station> stations, Printer printer) {
        this.name = Objects.requireNonNull(name);
        this.stations = Collections.unmodifiableMap(stations);
        this.printer = Objects.requireNonNull(printer);
    }
    
    public void step() {
        if (!started) {
            start();
        } else {
            printer.print("tick %s%n", name);
            stations.values().forEach(Station::tick);
            printer.print("tack %s%n", name);
            stations.values().forEach(Station::tack);
        }
    }

    private void start() {
        printer.print("start %s%n", name);
        reset();
        started = true;
    }
    
    private void reset() {
        printer.print("reset %s%n", name);
        stations.values().forEach(Station::reset);
    }
}
