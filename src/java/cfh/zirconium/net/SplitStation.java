package cfh.zirconium.net;

import cfh.zirconium.Environment;

/** 
 * {@code O} Station.
 * Dispatch N // K drones to each linked station, where N is the number of drones
 * occupying this station, K is the number of linked stations
 */
public final class SplitStation extends Single {

    public SplitStation(int x, int y, Environment env) {
        super(x, y, env);
    }
    
    @Override
    public String name() { 
        return "O"; 
    }

    @Override
    protected void tick0() {
        var k = linked().size();
        if (k > 0) {
            var n = total();
            var drones = n / k;
            if (drones > 0) {
                send(drones);
            }
        }
    }
}
