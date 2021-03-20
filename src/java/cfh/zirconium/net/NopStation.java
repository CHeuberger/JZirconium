package cfh.zirconium.net;

import cfh.zirconium.gui.Main.Printer;

/** 
 * {@code 0} Station.
 * Do not dispatch any drones.
 */
public final class NopStation extends Station {

    public NopStation(int x, int y, Printer printer) {
        super(x, y, printer);
    }

    @Override
    protected void tick0() {
        // nothing
    }
}
