package cfh.zirconium.net;

import java.util.Collection;
import java.util.Objects;
import java.util.stream.Stream;

import cfh.zirconium.Environment;
import cfh.zirconium.Settings;

/** A station, single or bound. */
public abstract class Station {

    protected final Settings settings = Settings.instance();
    
    protected final Environment env;

    protected Station(Environment env) {
        this.env = Objects.requireNonNull(env);
    }

    /** Station stream with childs for bound station, or the station itself if single. */
    public abstract Stream<Single> stations();
    
    /** Is the given station adjacent to this station. */
    public abstract boolean isNeighbour(Single station);
    
    /** 
     * Number of drones on this station.<br/>
     * Single stations: drones of sibling childs are <b>not</B> included;<br/>
     * Bound stations: sum of all drones of child stations.
     */
    protected abstract int drones();
    
    /** All linked stations, including childs of bound stations. */
    protected abstract Collection<Single> linked();
    
    /** Resets the station, including childs of bound station. */
    public abstract void reset();

    /** Tick start, called on all stations before calling {@link #tick}. */
    public abstract void preTick();

    /** Tick process. */
    public abstract void tick();
    
    /** Tick end, called after all stations have processed {@link #tick}. */
    public abstract void posTick();
}
