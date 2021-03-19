package cfh.zirconium.net;

import cfh.zirconium.gui.Main.Printer;

public final class DotStation extends Station {

    public DotStation(int x, int y, Printer printer) {
        super(x, y, printer);
    }

    // .  If this is occupied by any amount of drones, dispatch one drone to each linked station.
    @Override
    protected void tick0() {
        if (tokens() > 0) {
            send(1);
        }
    }
}
