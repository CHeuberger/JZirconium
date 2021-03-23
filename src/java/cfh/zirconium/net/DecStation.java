package cfh.zirconium.net;

import cfh.zirconium.Environment;

/** 
 * {@code Q} Station.
 * If this station is occupied by N drones, dispatch N - 1 drones to linked stations.
 */
public final class DecStation extends Single {

    public DecStation(int x, int y, Environment env) {
        super(x, y, env);
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
