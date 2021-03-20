package cfh.zirconium.net;

import cfh.zirconium.gui.Main.Printer;

/** 
 * {@code `} Station.
 * If any drones occupy this, write the number of drones occupying this station in numeric form to STDOUT.
 */
public final class NumOutStation extends Single {

    public NumOutStation(int x, int y, Printer printer) {
        super(x, y, printer);
    }

    @Override
    public char type() {
        return '`';
    }

    @Override
    protected void tick0() {
        var drones = total();
        if (drones > 0) {
            printer.print("%d", drones);  // TODO output
            System.out.print(drones);
        }
        
    }
}
