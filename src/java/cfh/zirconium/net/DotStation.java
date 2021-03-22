package cfh.zirconium.net;

import cfh.zirconium.Environment;

/** 
 * {@code .} Station.
 * If this is occupied by any amount of drones, dispatch one drone to each linked station.
 */
public final class DotStation extends Single {

    public DotStation(int x, int y, Environment env) {
        super(x, y, env);
    }
    
    @Override
    public char type() { return '.'; }

    @Override
    protected void tick0() {
        if (total() > 0) {
            send(1);
        }
    }
}
