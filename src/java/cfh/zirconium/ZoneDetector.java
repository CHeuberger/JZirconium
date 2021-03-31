package cfh.zirconium;

import static java.util.Objects.*;

import java.util.LinkedList;

import cfh.zirconium.Compiler.CompileException;
import cfh.zirconium.Compiler.Dir;
import cfh.zirconium.net.Pos;

// TODO settings
// TODO javadoc
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
            boolean inWall = false;
            for (var x = 1; x < row.length-1; x++) {
                if (inWall) {
                    if (zones[y][x] == 0 && !wall(x, y)) {
                        inWall = false;
                        for (var tx = x+1; tx < row.length; tx++) {
                            if (wall(tx, y)) {
                                walk(x, y, next++);
                                x = tx+1;
                                break;
                            }
                        }
                    }
                } else {
                    if (wall(x, y)) {
                        inWall = true;
                    }
                }
            }
        }
        for (var y = 1; y < chars.length-1; y++) {
            var row = chars[y];
            for (var x = 1; x < row.length-1; x++) {
                var ch = chars[y][x];
                if (ch == left) {
                    if (zones[y][x-1]!=0 || (zones[y][x+1] == 0 && !wall(x+1, y))) {
                        throw new CompileException(new Pos(x, y), "invalid " + name + ": " + ch + " or incomplete zone");
                    }
                } else if (ch == right) {
                    if ((zones[y][x-1] == 0 && !wall(x-1, y)) || zones[y][x+1]!=0) {
                        throw new CompileException(new Pos(x, y), "invalid " + name + ": " + ch + " or incomplete zone");
                    }
                } else if (ch == horiz) {
                    if ((zones[y-1][x] != 0) == (zones[y+1][x] != 0)) {
                        throw new CompileException(new Pos(x, y), "invalid " + name + ": " + ch + " or incomplete zone");
                    }
                }
            }
        }
        return zones;
    }
    
    private void walk(int sx, int sy, int id) {
        var rows = chars.length;
        var cols = chars[0].length;
        var stack = new LinkedList<Pos>();
        stack.push(new Pos(sx, sy));
        while (!stack.isEmpty()) {
            var pos = stack.poll();
            var x = pos.x();
            var y = pos.y();
            zones[y][x] = id;
            for (var ty = y-1; ty <= y+1; ty++) {
                for (var tx = x-1; tx <= x+1; tx++) {
                    if ((tx == x) != (ty == y)) {
                        if (0 <= tx && tx < cols && 0 <= ty && ty < rows) {
                            var z = zones[ty][tx];
                            if (z == 0) {
                                if (!wall(tx, ty)) {
                                    stack.push(new Pos(tx, ty));
                                }
                            } else if (z != id) {
                                change(z, id);
                            }
                        } else {
                            change(id, 0);
                            return;
                        }
                    }
                }
            }
        }
    }
    
    int[][] detect1() throws CompileException {
        var next = 1;
        for (var y = 1; y < chars.length-1; y++) {
            var row = chars[y];
            for (var x = 1; x < row.length; x++) {
                if (wall(x, y)) {
                    var count = 0;
                    for (var dir : Dir.values()) {
                        if (wall(x+dir.dx, y+dir.dy)) {
                            count += 1;
                        }
                    }
                    if (count > 2) {
                        throw new CompileException(new Pos(x, y), "too many neighbours");
                    }
                }
            }
            var outside = true;
            for (var x = 1; x < row.length-1; x++) {
                var ch = row[x];
                if (outside) {
                    if (ch == left) {
                        if (!wall(x+1, y)) {
                            outside = false;
                        }
                    } else if (ch == horiz) {
                        if (   (wall(x-1, y-1) || wall(x, y-1) || wall(x+1, y-1))
                            && (wall(x-1, y+1) || wall(x, y+1) || wall(x+1, y+1)) ) {
                            outside = false;
                        }
                    } else if (ch == right) {
                        if (!wall(x-1, y)) {
                            throw new CompileException(new Pos(x, y), "invalid " + name);
                        }
                    }
                } else {
                    // inside
                    if (ch == left) {
                        throw new CompileException(new Pos(x, y), "nested " + name);
                    } else if (ch == horiz) {
                        if (   (wall(x-1, y-1) || wall(x, y-1) || wall(x+1, y-1))
                            && (wall(x-1, y+1) || wall(x, y+1) || wall(x+1, y+1)) ) {
                            outside = true;
                        }
                    } else if (ch == right) {
                        outside = true;
                    } else {
                        if (zones[y][x] == 0) {
                            walk1(x, y, next++);
                        }
                    }
                }
            }
        }
        return zones;
    }
    
    private void walk1(int x, int y, int id) throws CompileException {
        var row = chars[y];
        int ex;
        for (ex = x; ex < row.length; ex++) {
            if (wall(ex, y))
                break;
            if (!wall(ex, y-1)) {
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
                if (wall(tx, ty)) {
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

    int[][] detect0() {
        var next = 1;
        for (var y = 1; y < chars.length-1; y++) {
            var row = chars[y];
            var id = 0;
            for (var x = 1; x < row.length-1; x++) {
                if (id == 0) {
                    if (wall(x, y) && !wall(x+1, y)) {
                        id = zones[y-1][x+1];
                        if (id == 0) {
                            if (wall(x+1, y-1) && zones[y-1][x] == 0) {
                                id = next++;
                            }
                        }
                    }
                } else {
                    if (wall(x, y)) {
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
    
    private boolean wall(int x, int y) {
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
