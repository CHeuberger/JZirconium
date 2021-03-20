package cfh.zirconium.net;

import cfh.zirconium.gui.Main.Printer;

/** 
 * {@code 0} Station.
 * Do not dispatch any drones.
 */
public final class NopStation extends Single {

    public NopStation(int x, int y, Printer printer) {
        super(x, y, printer);
    }
    
    @Override
    public char type() { return '0'; }

    @Override
    protected void tick0() {
        // receive(drones());  // TODO undocumented
    }
}
