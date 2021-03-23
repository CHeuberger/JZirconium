package cfh.zirconium.net;

import cfh.zirconium.Environment;

public final class HaltStation extends Single {

    public HaltStation(int x, int y, Environment env) {
        super(x, y, env);
    }

    @Override
    public char type() {
        return '!';
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
