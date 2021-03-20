package cfh.zirconium.net;

import cfh.zirconium.gui.Main.Printer;

/** 
 * {@code O} Station.
 * Dispatch N // K drones to each linked station, where N is the number of drones
 * occupying this station, K is the number of linked stations
 */
public final class SplitStation extends Single {

    public SplitStation(int x, int y, Printer printer) {
        super(x, y, printer);
    }

    @Override
    protected void tick0() {
        var k = linked().size();
        if (k > 0) {
            var n = total();
            var drones = n / k;
            if (drones > 0) {
                send(drones);
            }
        }
    }
}
