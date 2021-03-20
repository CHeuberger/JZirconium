package cfh.zirconium.net;

import cfh.zirconium.gui.Main.Printer;

/** 
 * {@code @} Station.
 * If this station is not occupied, dispatch one drone to each linked station.
 */
public final class CreateStation extends Single {

    public CreateStation(int x, int y, Printer printer) {
        super(x, y, printer);
    }
    
    @Override
    public char type() { return '@'; }
        
    @Override
    protected void tick0() {
        if (total() == 0) {
            send(1);
        }
    }
}
