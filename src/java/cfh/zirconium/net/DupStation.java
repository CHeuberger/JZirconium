package cfh.zirconium.net;

import cfh.zirconium.Environment;

/** 
 * {@code o} Station.
 * Dispatch the number of drones occupying this station to each linked station.
 */
public final class DupStation extends Single {

    public DupStation(int x, int y, Environment env) {
        super(x, y, env);
    }
    
    @Override
    public String name() { 
        return "o"; 
    }

    @Override
    protected void tick0() {
        var drones = total();
        if (drones > 0) {
            send(drones);
        }
    }
}
