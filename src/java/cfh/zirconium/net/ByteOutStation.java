package cfh.zirconium.net;

import cfh.zirconium.Environment;

/** 
 * {@code %} Station.
 * If any drones occupy this, print the number of drones occupying this station 
 * as a byte modulo 256 to Output.
 */
public final class ByteOutStation extends Single {

    public ByteOutStation(int x, int y, Environment env) {
        super(x, y, env);
    }

    @Override
    public char type() {
        return '%' ;
    }

    @Override
    protected void tick0() {
        var drones = total();
        if (drones > 0) {
            int value = drones % 256;
            env.output().write(value);
            env.print("%c", (char)value);
        }
    }

}
