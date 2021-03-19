package cfh.zirconium.net;

import cfh.zirconium.gui.Main.Printer;

public final class CreateStation extends Station {

    public CreateStation(int x, int y, Printer printer) {
        super(x, y, printer);
    }
    
    // @  If this station is not occupied, dispatch one drone to each linked station.
    @Override
    protected void tick0() {
        if (tokens() == 0) {
            send(1);
        }
    }
}
