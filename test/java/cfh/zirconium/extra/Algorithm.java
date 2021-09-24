package cfh.zirconium.extra;

import java.awt.Graphics2D;

public interface Algorithm {

    boolean step();

    void paint(Graphics2D gg, int size);

}
