package cfh.zirconium.net;

import cfh.zirconium.gui.Main.Printer;

public final class QStation extends Station {

    public QStation(int x, int y, Printer printer) {
        super(x, y, printer);
    }

    // Q  If this station is occupied by N drones, dispatch N - 1 drones to linked stations.
    @Override
    protected void tick0() {
        var tokens = tokens() - 1;
        if (tokens > 0) {
            send(tokens);
        }
    }
}
