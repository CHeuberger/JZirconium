package cfh.zirconium.net;

import cfh.zirconium.Environment;

/** 
 * {@code !} Station.
 * If any drones occupy this, halt the program.
 */
public final class HaltStation extends Single {

    public HaltStation(int x, int y, Environment env) {
        super(x, y, env);
    }

    @Override
    public String name() {
        return "!";
    }

    @Override
    protected void tick0() {
        // done after tick
    }
    
    @Override
    protected void posTick0() {
        if (total() > 0) {
            env.halt();
        }
    }
}
