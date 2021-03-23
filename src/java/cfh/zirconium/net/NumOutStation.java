package cfh.zirconium.net;

import cfh.zirconium.Environment;

/** 
 * {@code `} Station.
 * If any drones occupy this, write the number of drones occupying this station 
 * in numeric form to the Output.
 */
public final class NumOutStation extends Single {

    public NumOutStation(int x, int y, Environment env) {
        super(x, y, env);
    }

    @Override
    public char type() {
        return '`';
    }

    @Override
    protected void tick0() {
        var total = total();
        if (total > 0) {
            env.output(Integer.toString(total) + settings.numberSeparator());
            env.print("%d", total);
        }
        
    }
}
