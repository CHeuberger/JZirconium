package cfh.zirconium.net;

import cfh.zirconium.gui.Main.Printer;

/** 
 * {@code Q} Station.
 * If this station is occupied by N drones, dispatch N - 1 drones to linked stations.
 */
public final class QStation extends Single {

    public QStation(int x, int y, Printer printer) {
        super(x, y, printer);
    }
    
    @Override
    public char type() { return 'Q'; }

    @Override
    protected void tick0() {
        var drones = total() - 1;
        if (drones > 0) {
            send(drones);
        }
    }
}
