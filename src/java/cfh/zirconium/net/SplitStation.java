package cfh.zirconium.net;

import cfh.zirconium.gui.Main.Printer;

public final class SplitStation extends Station {

    public SplitStation(int x, int y, Printer printer) {
        super(x, y, printer);
    }

    // O  Dispatch N // K drones to each linked station, where N is the number of drones 
    //    occupying this station, K is the number of linked stations
    @Override
    protected void tick0() {
        var n = tokens();
        var k = linked().size();
        var tokens = n / k;
        if (tokens > 0) {
            send(tokens);
        }
    }
}
