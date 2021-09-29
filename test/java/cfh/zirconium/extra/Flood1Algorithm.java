package cfh.zirconium.extra;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Point;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class Flood1Algorithm implements Algorithm {

    private enum Type {
        NONE(null), 
        FENCE(Color.GREEN), 
        FORT(Color.BLUE), 
        EXCLUSION(new Color(127, 255, 127)), 
        METROPOLIS(new Color(127, 127, 255));
        private final Color color;
        private Type(Color color) { this.color = color; }
        Color color() { return color; }
    }

    private final char[][] code;
    
    private final int[][] zones;
    private final Type[][] types;
    private final Map<Integer, Type> map = new HashMap<>();
    private int zone = 0;
    private Point pivot = new Point(0, 0);
    private Point last = null;
    private final Deque<Point> stack = new LinkedList<>();
    private final List<Point> errors = new ArrayList<>();
    private boolean flooding = true;
    
    
    public Flood1Algorithm(char[][] code) {
        this.code = code;
        zones = new int[code.length][code[0].length];
        types = new Type[code.length][code[0].length];
    }

    @Override
    public boolean step() {
        if (flooding) {
            return flood();
        } else {
            return check();
        }
    }
    
    private boolean flood() {
        int x;
        int y;
        if (stack.isEmpty()) {
            last = null;
            while (code[pivot.y][pivot.x] != ' ' || zones[pivot.y][pivot.x] != 0) {
                if (++pivot.x >= code[pivot.y].length) {
                    pivot.x = 0;
                    if (++pivot.y >= code.length) {
                        pivot.y = 0;
                        flooding = false;
                        return true;
                    }
                }
            }
            x = pivot.x;
            y = pivot.y;
            last = (Point) pivot.clone();
            zone += 1;
        } else {
            var point = stack.pop();
            x = point.x;
            y = point.y;
            last = point;
        }
        
        assert zones[y][x] == 0 : x + "," + y + " " + zones[y][x];
        if (code[y][x] == ' ') {
            zones[y][x] = zone;
            if (x+1 < zones[y].length) {
                if (zones[y][x+1] == 0) {
                    push(x+1, y);
                } else {
                    assert zones[y][x+1] == zone : (x+1) + "," + y + " " + zones[y][x+1];
                }
            }
            if (x > 0) {
                if (zones[y][x-1] == 0) {
                    push(x-1, y);
                } else {
                    assert zones[y][x-1] == zone : (x-1) + "," + y + " " + zones[y][x-1];
                }
            }
            if (y+1 < zones.length) {
                if (zones[y+1][x] == 0) {
                    push(x, y+1);
                } else {
                    assert zones[y+1][x] == zone : x + "," + (y+1) + " " + zones[y+1][x];
                }
            }
            if (y > 0) {
                if (zones[y-1][x] == 0) {
                    push(x, y-1);
                } else {
                    assert zones[y-1][x] == zone : x + "," + (y-1) + " " + zones[y-1][x+1];
                }
            }
        }
 
        return true;
    }
    
    private void push(int x, int y) {
        var p = new Point(x, y);
        stack.remove(p);
        stack.push(p);
    }
    
    private boolean check() {
        int x = pivot.x;
        int y = pivot.y;
        if (zones[y][x] == 0) {
            var ch = code[y][x];
            switch (ch) {
                case '[': {
                    types[y][x] = Type.FORT;
                    if (x+1 < code[y].length) {
                        var z = zones[y][x+1];
                        if (z != 0) {
                            var prev = map.putIfAbsent(z, Type.METROPOLIS);
                            if (prev != null && prev != Type.METROPOLIS) {
                                error(x, y, "ambigous zone at " + ch);
                            }
                        }
                    }
                }
                break;
                case '=': {
                    types[y][x] = Type.FORT;
                    // TODO
                }
                break;
                case ']': {
                    types[y][x] = Type.FORT;
                    if (x > 0) {
                        var z = zones[y][x-1];
                        if (z != 0) {
                            var prev = map.putIfAbsent(z, Type.METROPOLIS);
                            if (prev != null && prev != Type.METROPOLIS) {
                                error(x, y, "ambigous zone at " + ch);
                            }
                        }
                    }
                }
                break;
                case '{': {
                    types[y][x] = Type.FENCE;
                    if (x+1 < code[y].length) {
                        var z = zones[y][x+1];
                        if (z != 0) {
                            var prev = map.putIfAbsent(z, Type.EXCLUSION);
                            if (prev != null && prev != Type.EXCLUSION) {
                                error(x, y, "ambigous zone at " + ch);
                            }
                        }
                    }
                }
                break;
                case '~': {
                    types[y][x] = Type.FENCE;
                    // TODO
                }
                break;
                case '}': {
                    types[y][x] = Type.FENCE;
                    if (x > 0) {
                        var z = zones[y][x-1];
                        if (z != 0) {
                            var prev = map.putIfAbsent(z, Type.EXCLUSION);
                            if (prev != null && prev != Type.EXCLUSION) {
                                error(x, y, "ambigous zone at " + ch);
                            }
                        }
                    }
                }
                break;
            }
            last = (Point) pivot.clone();
        }
        do {
            if (++pivot.x >= code[pivot.y].length) {
                pivot.x = 0;
                if (++pivot.y >= code.length) {
                    for (var yy = 0; yy < types.length; yy++) {
                        for (var xx = 0; xx < types[yy].length ; xx++) {
                            var z = zones[yy][xx];
                            if (z != 0) {
                                var type = map.getOrDefault(z, Type.NONE);
                                types[yy][xx] = type;
                            }
                        }
                    }
                    pivot.y = 0;
                    return false;
                }
            }
        } while (zones[pivot.y][pivot.x] != 0);
        return true;
    }
    
    private void error(int x, int y, String message) {
        error(new Point(x, y), message);
    }
    
    private void error(Point point, String message) {
        errors.add(point);
        System.err.printf("(%d,%d) %s%n", point.x, point.y, message); 
    }
 
    @Override
    public void paint(Graphics2D gg, int size) {
        gg.setColor(Color.BLACK);
        for (var y = 0; y < zones.length; y++) {
            for (var x = 0; x < zones[y].length; x++) {
                var z = zones[y][x];
                if (z != 0) {
                    paintString(gg, size, x, y, Integer.toString(z));
                }
            }
        }
        gg.setColor(Color.GREEN);
        for (var point : stack) {
            paintString(gg, size, point.x, point.y, "O");
        }
        for (var y = 0; y < types.length; y++) {
            for (var x = 0; x < types[y].length; x++) {
                if (types[y][x] != null) {
                    var color = types[y][x].color();
                    if (color != null) {
                        paintCell(gg, size, x, y, color);
                    }
                }
            }
        }
        if (last != null) {
            gg.setColor(Color.BLUE);
            paintString(gg, size, last.x, last.y, "[ ]");
        }
        for (var point : errors) {
            paintCell(gg, size, point.x, point.y, Color.RED);
        }
    }

    private void paintCell(Graphics2D gg, int size, int x, int y, Color color) {
        gg.setColor(color);
        gg.fillRect(x*size+1, y*size+1, size-1, size-1);
    }

    private void paintString(Graphics2D gg, int size, int x, int y, String text) {
        var fm = gg.getFontMetrics();
        var dx = (size - fm.stringWidth(text)) / 2;
        var dy = size - fm.getDescent() - (size-fm.getAscent()-fm.getDescent())/2;
        gg.drawString(text, x*size+dx, y*size+dy);
    }
}
