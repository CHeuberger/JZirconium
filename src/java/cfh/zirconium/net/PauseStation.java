package cfh.zirconium.net;

import cfh.zirconium.Environment;

/** 
 * {@code ;} Station.
 * Pause execution for a duration equal to the number of drones 
 * occupying this station in milliseconds.
 */
public final class PauseStation extends Single {

    public PauseStation(int x, int y, Environment env) {
        super(x, y, env);
    }

    @Override
    public String name() {
        return ";";
    }

    @Override
    protected void tick0() {
        // done after tick
    }
    
    @Override
    protected void posTick0() {
        int total = total();
        if (total > 0) {
            try {
                Thread.sleep(total);
            } catch (InterruptedException ex) {
                ex.printStackTrace();
                env.print("interrupted during pause at %s", this);
                env.halt();
            }
        }
    }
}
