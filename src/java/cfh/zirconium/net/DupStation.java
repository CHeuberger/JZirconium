package cfh.zirconium.net;

import cfh.zirconium.gui.Main.Printer;

/** 
 * {@code o} Station.
 * Dispatch the number of drones occupying this station to each linked station.
 */
public final class DupStation extends Single {

    public DupStation(int x, int y, Printer printer) {
        super(x, y, printer);
    }
    
    @Override
    public char type() { return 'o'; }

    @Override
    protected void tick0() {
        var drones = total();
        if (drones > 0) {
            send(drones);
        }
    }
}
