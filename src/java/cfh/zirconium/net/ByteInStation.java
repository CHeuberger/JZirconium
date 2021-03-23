package cfh.zirconium.net;

import cfh.zirconium.Environment;

public final class ByteInStation extends Single {

    public ByteInStation(int x, int y, Environment env) {
        super(x, y, env);
    }

    @Override
    public char type() {
        return '?';
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
