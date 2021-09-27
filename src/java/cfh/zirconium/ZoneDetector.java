package cfh.zirconium;

import static cfh.zirconium.Compiler.*;

import java.util.Deque;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Consumer;

import cfh.zirconium.Compiler.CompileException;
import cfh.zirconium.Compiler.Zone;
import cfh.zirconium.net.Pos;

/** Zone Detector for Zirconium programs. */
public class ZoneDetector {
    
    private final char[][] chars;
    
    ZoneDetector(char[][] chars) {
        this.chars = chars;
    }

    public Zone[][] detect() throws CompileException {
        Zone[][] zones = new Zone[chars.length][];
        for (int i = 0; i < zones.length; i++) {
            zones[i] = new Zone[chars[i].length];
        }
        
        for (int y = 0; y < zones.length; y++) {
            for (int x = 0; x < zones[y].length; x++) {
                if (zones[y][x] == null) {
                    if (isBorder(x, y)) {
                        zones[y][x] = Zone.NONE;
                    } else {
                        flood(x, y, zones);
                    }
                }
            }
        }
        
        return zones;
    }
    
    private void flood(int sx, int sy, Zone[][] zones) throws CompileException {
        Zone type = Zone.NONE;
        List<Pos> region = new LinkedList<>();
        Deque<Pos> open = new LinkedList<>();
        Consumer<Pos> push = p -> {
            if (!region.contains(p)) {
                open.remove(p);
                open.add(p);
            }
        };
        
        Pos start = new Pos(sx, sy);
        open.add(start);
        while (!open.isEmpty()) {
            Pos pos = open.remove();
            region.add(pos);
            int x = pos.x();
            int y = pos.y();
            assert zones[y][x] == null : pos + " " + zones[y][x];
            Zone newType = null;
            
            if (y > 0) {
                if (!isBorder(x, y-1) && zones[y-1][x] == null) {
                    push.accept(new Pos(x, y-1));
                } else {
                    // TODO ?
                }
            }
            if (x > 0) {
                if (!isBorder(x-1, y) && zones[y][x-1] == null) {
                    push.accept(new Pos(x-1, y));
                } else {
                    if (chars[y][x-1] == MP_L) {
                        newType = Zone.METROPOLIS;
                    } else if (chars[y][x-1] == EZ_L) {
                        newType = Zone.EXCLUSION;
                    }
                }
            }
            if (y+1 < zones.length) {
                if (!isBorder(x, y+1) && zones[y+1][x] == null) {
                    push.accept(new Pos(x, y+1));
                } else {
                    // TODO ?
                }
            }
            if (x+1 < zones[y].length) {
                if (!isBorder(x+1, y) && zones[y][x+1] == null) {
                    push.accept(new Pos(x+1, y));
                } else {
                    if (chars[y][x+1] == MP_R) {
                        newType = Zone.METROPOLIS;
                    } else if (chars[y][x+1] == EZ_R) {
                        newType = Zone.EXCLUSION;
                    }
                }
            }
            
            if (newType != null && newType != type) {
                if (type == Zone.NONE) {
                    type = newType;
                } else {
                    throw new CompileException(pos, "ambigous zone: " + type + ", " + newType);
                }
            }
        }
        
        for (Pos pos : region) {
            assert zones[pos.y()][pos.x()] == null : pos + " " + zones[pos.y()][pos.x()];
            zones[pos.y()][pos.x()] = type;
        }
    }

    private boolean isBorder(int x, int y) {
        return BORDER.indexOf(chars[y][x]) != -1;
    }
}
