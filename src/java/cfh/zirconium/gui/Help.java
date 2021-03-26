package cfh.zirconium.gui;

import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;

import cfh.zirconium.Settings;

/** Help pane. */
public class Help {
    
    private final Settings settings = Settings.instance();
    
    private final JTabbedPane pane;
    
    //* Creates a new help pane. */
    public Help() {
        final var HEADER = String.format("""
                                         J Z I R C O N I U M  v%-5s                         
                                        ############################
            
            """,
            IDE.VERSION);
        
        final var INTRO = String.format(HEADER + """
                                                   INTRO
                                                  =======                                 
                                             
            Zirconium is an esoteric programming language made in September of 2020 
            by RocketRace.
            
            A Zirconium program (or a map) is a network of stations in two-dimensional space,
            linked together with tunnels, akin to a graph with nodes. At each tick of time, 
            stations will dispatch a number of drones to occupy linked stations. 
                
                   0
                .--+--O
                 \\ | /
                  \\|/
                   o----@
            """);
        final var STATIONS = String.format(HEADER + """
                                                  STATIONS
                                                 ==========
            At the beginning of the tick, all stations count the number of drones occupying them, 
            and then consume all those drones. Afterwards, the stations execute the following: 
                                             
            0  Do not dispatch any drones (just consume drones).
            @  If this station is not occupied, dispatch one drone to each linked station.
            .  If this is occupied by any amount of drones, dispatch one drone to each linked station.
            o  Dispatch the number of drones occupying this station to each linked station.
            Q  If this station is occupied by N drones, dispatch N - 1 drones to linked stations.
            O  Dispatch N // K drones to each linked station, where N is the number of drones 
               occupying this station, K is the number of linked stations (division by 0 returns 0)
            """);
        final var TUNNELS = String.format(HEADER + """
                                                  TUNNELS
                                                 =========
                                            
            -   horizontal.
            |   vertical.
            /\\  diagonals.
            +   both horizontal and vertical.
            X   both diagonals.
            *   any direction.
            """);
        final var APERTURES = String.format(HEADER + """
                                                 APERTURES
                                                ===========
                            
            ^  north.
            >  east.
            v  south.
            <  west.
            #  all diagonals.
            """);
        final var EXCLUSION = String.format(HEADER + """
                                               EXCLUSION ZONE 
                                              ================
            
            An area of the program may be enclosed with fences to make it an exclusion zone:
            
                 {~~}
                {    }
                 {    ~~~~~~}
                {            }
                {     ~~     }
                 {~~~}  {~~~}
            
            The fences of an exclusion zone will behave as * tunnels. An exclusion zone 
            may contain special defect stations, which perform impure computation: 
            
            ?  If any drones occupy this, read one byte from Input and 
               dispatch that many drones to linked stations.
               {{ do nothing on EOF }}
            %%  If any drones occupy this, print the number of drones occupying this station 
               as a byte modulo 256 to Output.
            &  If any drones occupy this, write the number of drones occupying this 
               as a byte modulo 256 to Error.
            _  If any drones occupy this, read a numeric value from Input 
               and dispatch that many drones to linked stations.
            `  If any drones occupy this, write the number of drones occupying this station 
               in numeric form to the Output.
            ;  Pause execution for a duration equal to the number of drones 
               occupying this station in milliseconds.
            !  If any drones occupy this, halt the program.
            """);
        final var METROPOLIS = String.format(HEADER + """
                                                 METROPOLIS 
                                                ============
            
            An area of the program may be enclosed with forts to make it a metropolis:

                 [==]
                [    ]
                 [    ======]
                [            ]
                [     ==     ]
                 [===]  [===]

            Like the fences of an exclusion zone, the forts of a metropolis behave as * tunnels.

            A metropolis may contain special synthetic stations.
            """);
        final var SYNTHETIC = String.format(HEADER + """
                                              SYNTHETIC STATION 
                                             ===================
            
            A synthetic station is a station whose behavior is defined by the user. Each tick, 
            a synthetic station dispatches some number of drones based on the number of 
            occupying drones and the number of linked stations using some arithmetic expression.

            A synthetic station must be defined using a specific grammar. The definition 
            includes a target symbol, which is the synthetic station to be defined, and an 
            arithmetic expression in postfix notation. The expression is evaluated for the 
            station on each tick, and represents the number of drones dispatched. 
            The expression can be in terms of integer literals, as well as special 
            variables N and K, which represent the number of drones currently occupying 
            the station and the number of linked station.

            For instance,
                Z = N 1 +
            The Z station here is defined to dispatch N + 1 drones to each linked station.

            Expressions may contain the following operators: +, -, *, /, =, corresponding 
            to addition, subtraction, multiplication, floor division (zero if divisor is 0) 
            and equality (1 if equal, 0 otherwise).

            The following is a complete grammar for synthetic station definitions.
                definition := symbol sp* "=" sp* expr
                symbol := [^\s]
                expr := value | expr sp* expr sp* operator
                value := "N" | "K" | integer
                integer := ["0"-"9"]+
                operator := "+" | "-" | "*" | "/" | "=" 
                sp := " " | "\t"

            A synthetic station may be defined inside a lens. A lens is parsed at compile time:
                ((r = N K / 1 +))
            """);

        pane = new JTabbedPane();
        pane.addTab("INTRO", newArea(INTRO));
        pane.addTab("STATIONS", newArea(STATIONS));
        pane.addTab("TUNNELS", newArea(TUNNELS));
        pane.addTab("APERTURES", newArea(APERTURES));
        pane.addTab("EXCLUSION", newArea(EXCLUSION));
        pane.addTab("METROPOLIS", newArea(METROPOLIS));
        pane.addTab("SYNTHETIC", newArea(SYNTHETIC));
    }
    
    
    /** Help pane. */
    public JComponent pane() {
        return pane;
    }
    
    /** Creates JTextArea. */
    private JComponent newArea(String text) {
        var area = new JTextArea(text);
        area.setBorder(BorderFactory.createEmptyBorder(8, 4, 8, 4));
        area.setEditable(false);
        area.setFont(settings.helpFont());
        return new JScrollPane(area);
    }
}
