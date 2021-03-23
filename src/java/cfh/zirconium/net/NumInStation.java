package cfh.zirconium.net;

import cfh.zirconium.Environment;

public final class NumInStation extends Single {

    public NumInStation(int x, int y, Environment env) {
        super(x, y, env);
    }

    @Override
    public char type() {
        return '_';
    }

    @Override
    protected void tick0() {
        
    }
}
