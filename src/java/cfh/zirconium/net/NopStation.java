package cfh.zirconium.net;

import cfh.zirconium.gui.Main.Printer;

public final class NopStation extends Station {

    public NopStation(int x, int y, Printer printer) {
        super(x, y, printer);
    }

    // 0  Do not dispatch any drones.
    @Override
    protected void tick0() {
        // nothing
    }
}
