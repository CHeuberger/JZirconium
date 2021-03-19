package cfh.zirconium.net;

import cfh.zirconium.gui.Main.Printer;

public final class DupStation extends Station {

    public DupStation(int x, int y, Printer printer) {
        super(x, y, printer);
    }

    // o  Dispatch the number of drones occupying this station to each linked station.
    @Override
    protected void tick0() {
        var tokens = tokens();
        if (tokens > 0) {
            send(tokens);
        }
    }
}
