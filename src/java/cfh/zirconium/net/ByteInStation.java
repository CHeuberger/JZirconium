package cfh.zirconium.net;

import cfh.zirconium.Environment;

/** 
 * {@code ?} Station.
 * If any drones occupy this, read one byte from Input and 
 * dispatch that many drones to linked stations.
 */
public final class ByteInStation extends Single {

    public ByteInStation(int x, int y, Environment env) {
        super(x, y, env);
    }

    @Override
    public String name() {
        return "?";
    }

    @Override
    protected void tick0() {
        if (total() > 0) {
            int drones = env.input().read();
            if (drones > 0) {
                send(drones);
            }
        }
    }
}
