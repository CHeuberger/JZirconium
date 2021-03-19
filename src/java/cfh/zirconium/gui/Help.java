package cfh.zirconium.gui;

import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;

import cfh.zirconium.Settings;

public class Help {
    
    private final Settings settings = Settings.instance();
    
    private final JTabbedPane pane;
    
    public Help() {
        final var HEADER = String.format("""
                                    J Z I R C O N I U M  v%-5s                         
                                   ############################
            
            """,
            Main.VERSION);
        
        final var INTRO = String.format(HEADER + """
                                              INTRO
                                             =======
                                             
            WIP
            """);
        final var STATIONS = String.format(HEADER + """
                                             STATIONS
                                            ==========
                                             
            0  Do not dispatch any drones.
            @  If this station is not occupied, dispatch one drone to each linked station.
            .  If this is occupied by any amount of drones, dispatch one drone to each linked station.
            o  Dispatch the number of drones occupying this station to each linked station.
            Q  If this station is occupied by N drones, dispatch N - 1 drones to linked stations.
            O  Dispatch N // K drones to each linked station, where N is the number of drones 
               occupying this station, K is the number of linked stations
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
        
        pane = new JTabbedPane();
        pane.addTab("INTRO", newArea(INTRO));
        pane.addTab("STATIONS", newArea(STATIONS));
        pane.addTab("TUNNELS", newArea(TUNNELS));
        pane.addTab("APERTURES", newArea(APERTURES));
    }
    
    public JComponent pane() {
        return pane;
    }
    
    private JComponent newArea(String text) {
        var area = new JTextArea(text);
        area.setBorder(BorderFactory.createEmptyBorder(8, 4, 8, 4));
        area.setEditable(false);
        area.setFont(settings.helpFont());
        return new JScrollPane(area);
    }
}
