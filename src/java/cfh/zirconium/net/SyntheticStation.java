package cfh.zirconium.net;

import static java.util.Objects.*;

import cfh.zirconium.Environment;
import cfh.zirconium.expr.Definition;

/** 
 * A Synthetic Station is a station whose behavior is defined by the user.
 * Each tick, a synthetic station dispatches some number of drones based on the number of 
 * occupying drones and the number of linked stations using some arithmetic expression.
 */
public final class SyntheticStation extends Single {

    private final Definition definition;
    
    public SyntheticStation(int x, int y, Environment env, Definition definition) {
        super(x, y, env);
        this.definition = requireNonNull(definition); 
    }

    @Override
    public String name() {
        return "[" + definition.symbol + "]";
    }

    @Override
    protected void tick0() {
        int k = countLinked();
        int n = total();
        int drones = definition.calculate(n, k);
        if (drones > 0) {
            send(drones);
        }
    }
}
