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
        final String HEADER = String.format(""
            + "                             J Z I R C O N I U M  v%-5s \n"
            + "                            ############################\n"
            + "\n",
            IDE.VERSION);
        
        final String INTRO = String.format(HEADER
            + "                                       INTRO\n"
            + "                                      =======\n"
            + "\n"
            + "Zirconium is an esoteric programming language made in September of 2020\n" 
            + "by RocketRace.\n"
            + "\n"
            + "A Zirconium program (or a map) is a network of stations in two-dimensional space, \n"
            + "linked together with tunnels, akin to a graph with nodes. At each tick of time, \n"
            + "stations will dispatch a number of drones to occupy linked stations. \n"
            + "\n"
            + "       0\n"
            + "    .--+--O\n"
            + "     \\ | /\n"
            + "      \\|/\n"
            + "       o----@\n"
            + "\n"
            + "Based on specification from 2021-09-23 https://esolangs.org/wiki/Zirconium\n"
            );
        final String STATIONS = String.format(HEADER
            + "                                      STATIONS\n"
            + "                                     ==========\n"
            + "At the beginning of the tick, all stations count the number of drones occupying them,\n" 
            + "and then consume all those drones. Afterwards, the stations execute the following:\n" 
            + "\n"                                 
            + "0  Do not dispatch any drones (just consume drones).\n"
            + "@  If this station is not occupied, dispatch one drone to each linked station.\n"
            + ".  If this is occupied by any amount of drones, dispatch one drone to each linked station.\n"
            + "o  Dispatch the number of drones occupying this station to each linked station.\n"
            + "Q  If this station is occupied by N drones, dispatch N - 1 drones to linked stations.\n"
            + "O  Dispatch N // K drones to each linked station, where N is the number of drones\n" 
            + "   occupying this station, K is the number of linked stations. (division by 0 returns 0)\n"
            + "\n"
            + "These stations can be placed anywhere.\n"
            );
        final String TUNNELS = String.format(HEADER
            + "                                      TUNNELS\n"
            + "                                     =========\n"
            + "                                \n"
            + "-   horizontal.\n"
            + "|   vertical.\n"
            + "/\\  diagonals.\n"
            + "+   both horizontal and vertical.\n"
            + "X   both diagonals.\n"
            + "*   any direction.\n"
            );
        final String APERTURES = String.format(HEADER
            + "                                     APERTURES\n"
            + "                                    ===========\n"
            + "                \n"
            + "^  north.\n"
            + ">  east.\n"
            + "v  south.\n"
            + "<  west.\n"
            + "#  all diagonals.\n"
            );
        final String EXCLUSION = String.format(HEADER
            + "                                   EXCLUSION ZONE \n"
            + "                                  ================\n"
            + "\n"
            + "An area of the program may be enclosed with fences to make it an exclusion zone:\n"
            + "\n"
            + "     {~~}\n"
            + "    {    }\n"
            + "     {    ~~~~~~}\n"
            + "    {            }\n"
            + "    {     ~~     }\n"
            + "     {~~~}  {~~~}\n"
            + "\n"
            + "The fences of an exclusion zone will behave as * tunnels. An exclusion zone \n"
            + "may contain special defect stations, which perform impure computation: \n"
            + "\n"
            + "?  If any drones occupy this, read one byte from Input and \n"
            + "   dispatch that many drones to linked stations.\n"
            + "   {{ do nothing on EOF }}\n"
            + "%%  If any drones occupy this, print the number of drones occupying this station \n"
            + "   as a byte modulo 256 to Output.\n"
            + "&  If any drones occupy this, write the number of drones occupying this \n"
            + "   as a byte modulo 256 to Error.\n"
            + "_  If any drones occupy this, read a numeric value from Input \n"
            + "   and dispatch that many drones to linked stations.\n"
            + "`  If any drones occupy this, write the number of drones occupying this station \n"
            + "   in numeric form to the Output.\n"
            + ";  Pause execution for a duration equal to the number of drones \n"
            + "   occupying this station in milliseconds.\n"
            + "!  If any drones occupy this, halt the program.\n"
            );
        final String METROPOLIS = String.format(HEADER
            + "                                     METROPOLIS \n"
            + "                                    ============\n"
            + "\n"
            + "An area of the program may be enclosed with forts to make it a metropolis:\n"

            + "     [==]\n"
            + "    [    ]\n"
            + "     [    ======]\n"
            + "    [            ]\n"
            + "    [     ==     ]\n"
            + "     [===]  [===]\n"

            + "Like the fences of an exclusion zone, the forts of a metropolis behave as * tunnels.\n"

            + "A metropolis may contain special synthetic stations.\n"
            );
        final String SYNTHETIC = String.format(HEADER
            + "                                  SYNTHETIC STATION \n"
            + "                                 ===================\n"
            + "\n"
            + "A synthetic station is a station whose behavior is defined by the user. Each tick, \n"
            + "a synthetic station dispatches some number of drones based on the number of \n"
            + "occupying drones and the number of linked stations using some arithmetic expression.\n"
            + "A synthetic station must be defined using a specific grammar. The definition \n"
            + "includes a target symbol, which is the synthetic station to be defined, and an \n"
            + "arithmetic expression in postfix notation. The expression is evaluated for the \n"
            + "station on each tick, and represents the number of drones dispatched. \n"
            + "The expression can be in terms of integer literals, as well as special \n"
            + "variables N and K, which represent the number of drones currently occupying \n"
            + "the station and the number of linked station.\n"

            + "For instance,\n"
            + "    Z = N 1 +\n"
            + "The Z station here is defined to dispatch N + 1 drones to each linked station.\n"

            + "Expressions may contain the following operators: +, -, *, /, =, corresponding \n"
            + "to addition, subtraction, multiplication, floor division (zero if divisor is 0) \n"
            + "and equality (1 if equal, 0 otherwise).\n"

            + "The following is a complete grammar for synthetic station definitions.\n"
            + "    definition := symbol sp* \"=\" sp* expr\n"
            + "    symbol := [^\\s]\n"
            + "    expr := value | expr sp* expr sp* operator\n"
            + "    value := \"N\" | \"K\" | integer\n"
            + "    integer := [\"0\"-\"9\"]+\n"
            + "    operator := \"+\" | \"-\" | \"*\" | \"/\" | \"=\" \n"
            + "    sp := \" \" | \"\\t\"\n"
            
            + "A synthetic station may be defined inside a lens. A lens is parsed at compile time:\n"
            + "    ((r = N K / 1 +))\n"
            );

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
        JTextArea area = new JTextArea(text);
        area.setBorder(BorderFactory.createEmptyBorder(8, 4, 8, 4));
        area.setEditable(false);
        area.setFont(settings.helpFont());
        return new JScrollPane(area);
    }
}
