package cfh.zirconium.net;

import cfh.zirconium.Environment;

/** 
 * {@code 0} Station.
 * Do not dispatch any drones.
 */
public final class NopStation extends Single {

    public NopStation(int x, int y, Environment env) {
        super(x, y, env);
    }
    
    @Override
    public char type() { return '0'; }

    @Override
    protected void tick0() {
        // do nothing
    }
}
