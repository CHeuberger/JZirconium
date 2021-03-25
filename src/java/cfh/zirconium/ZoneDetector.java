package cfh.zirconium;

import static java.util.Objects.*;

import cfh.zirconium.Compiler.CompileException;
import cfh.zirconium.net.Pos;

public class ZoneDetector {

    private final String name;
    private final char left;
    private final char horiz;
    private final char right;
    private final String sides;
    private final char[][] chars;
    
    private final int[][] zones;
    
    ZoneDetector(String name, char left, char horiz, char right, char[][] chars) {
        this.name = requireNonNull(name);
        this.left = left;
        this.horiz = horiz;
        this.right = right;
        this.sides = "" + left + horiz + right;
        this.chars = requireNonNull(chars);
        
        zones = new int[chars.length][chars[0].length];
    }
    
    int[][] detect() throws CompileException {
        var next = 1;
        for (var y = 1; y < chars.length-1; y++) {
            var row = chars[y];
            var outside = true;
            for (var x = 1; x < row.length-1; x++) {
                var ch = row[x];
                if (outside) {
                    if (ch == left) {
                        if (!side(x+1, y)) {
                            outside = false;
                        }
                    } else if (ch == horiz) {
                        if (   (side(x-1, y-1) || side(x, y-1) || side(x+1, y-1))
                            && (side(x-1, y+1) || side(x, y+1) || side(x+1, y+1)) ) {
                            outside = false;
                        }
                    } else if (ch == right) {
                        if (!side(x-1, y)) {
                            throw new CompileException(new Pos(x, y), "invalid " + name);
                        }
                    }
                } else {
                    // inside
                    if (ch == left) {
                        throw new CompileException(new Pos(x, y), "nested " + name);
                    } else if (ch == horiz) {
                        if (   (side(x-1, y-1) || side(x, y-1) || side(x+1, y-1))
                            && (side(x-1, y+1) || side(x, y+1) || side(x+1, y+1)) ) {
                            outside = true;
                        }
                    } else if (ch == right) {
                        outside = true;
                    } else {
                        if (zones[y][x] == 0) {
                            walk(x, y, next++);
                        }
                    }
                }
            }
        }
        return zones;
    }
    
    private void walk(int x, int y, int id) throws CompileException {
        var row = chars[y];
        int ex;
        for (ex = x; ex < row.length; ex++) {
            if (side(ex, y))
                break;
            if (!side(ex, y-1)) {
                var above = zones[y-1][ex];
                if (above == 0) {
                    throw new CompileException(new Pos(ex, y-1), "unclosed " + name);
                }
                if (above != id) {
                    change(above, id);
                }
            }
            var z = zones[y][ex];
            if (z != 0 && z != id) {
                change(z, id);
            } else {
                zones[y][ex] = id;
            }
        }
        for (var tx = x; tx < ex; tx++) {
            var outside = false;
            for (var ty = y+1; ty < chars.length; ty++) {
                if (side(tx, ty)) {
                    outside = true;
                    break;
                }
            }
            if (!outside) {
                throw new CompileException(new Pos(tx, y), "unclosed " + name);
            }
        }
        // TODO walk down ?
    }

    int[][] detect0() throws CompileException {
        var next = 1;
        for (var y = 1; y < chars.length-1; y++) {
            var row = chars[y];
            var id = 0;
            for (var x = 1; x < row.length-1; x++) {
                if (id == 0) {
                    if (side(x, y) && !side(x+1, y)) {
                        id = zones[y-1][x+1];
                        if (id == 0) {
                            if (side(x+1, y-1) && zones[y-1][x] == 0) {
                                id = next++;
                            }
                        }
                    }
                } else {
                    if (side(x, y)) {
                        id = 0;
                    } else {
                        var prev = zones[y-1][x];
                        if (prev != 0 && prev != id) {
                            change(prev, id);
                        }
                        zones[y][x] = id;
                    }
                }
            }
            if (id != 0) {
                change(id,  0);
            }
        }
        return zones;
    }
    
    private boolean side(int x, int y) {
        return sides.indexOf(chars[y][x]) != -1;
    }
    
    private void change(int from, int to) {
        for (var row : zones) {
            for (var x = 0; x < row.length; x++) {
                if (row[x] == from) {
                    row[x] = to;
                }
            }
        }
    }
}
