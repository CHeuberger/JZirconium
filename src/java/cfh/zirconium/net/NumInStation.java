package cfh.zirconium.net;

import cfh.zirconium.Environment;

/** 
 * {@code _} Station.
 * If any drones occupy this, read a numeric value from Input 
 * and dispatch that many drones to linked stations.
 */
public final class NumInStation extends Single {

    public NumInStation(int x, int y, Environment env) {
        super(x, y, env);
    }

    @Override
    public String name() {
        return "_";
    }

    @Override
    protected void tick0() {
        if (total() > 0) {
            int drones = env.input().readInteger();
            if (drones > 0) {
                send(drones);
            }
        }
    }
}
