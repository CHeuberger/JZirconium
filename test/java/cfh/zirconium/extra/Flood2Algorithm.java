package cfh.zirconium.extra;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Point;
import java.util.Deque;
import java.util.LinkedList;
import java.util.Queue;

public class Flood2Algorithm implements Algorithm {

    private enum Type {
        NEW(null), 
        FENCE(Color.GREEN), 
        FORT(Color.BLUE), 
        EXCLUSION(new Color(127, 255, 127)), 
        METROPOLIS(new Color(127, 127, 255));
        private final Color color;
        private Type(Color color) { this.color = color; }
        Color color() { return color; }
    }
    
    private final char[][] code;
    private Type[][] types;
    private final Queue<Point> open = new LinkedList<>(); // TODO delete
    private final Deque<Point> stack = new LinkedList<>();
    private final Queue<Point> zone = new LinkedList<>();
    private Type actual = Type.NEW;

    public Flood2Algorithm(char[][] code) {
        this.code = code;
    }

    @Override
    public boolean step() {
        if (types == null) {
            types = new Type[code.length][];
            for (var i = 0; i < code.length; i++) {
                types[i] = new Type[code[i].length];
                for (var j = 0; j < code[i].length; j++) {
                    var ch = code[i][j];
                    if (ZoneDetectionProbe.FENCE.indexOf(ch) != -1) {
                        types[i][j] = Type.FENCE;
                    } else if (ZoneDetectionProbe.FORT.indexOf(ch) != -1) {
                        types [i][j] = Type.FORT;
                    } else {
                        types[i][j] = Type.NEW;
                        open.add(new Point(j, i));
                    }
                }
            }
        }
        
        var point = stack.poll();
        if (point == null) {
            closeZone();
            
            point = open.poll();
            if (point == null) {
                closeZone();
                return false;
            }
        }
        
        zone.add(point);
        var x = point.x;
        var y = point.y;

        if (y > 0) {
            if (types[y-1][x] == Type.NEW) {
                push(x, y-1);
            } else {
                // TODO
            }
        }
        if (x > 0) {
            if (types[y][x-1] == Type.NEW) {
                push(x-1, y);
            } else {
                if (code[y][x-1] == '[') {
                    actual(Type.METROPOLIS);
                } else if (code[y][x-1] == '{') {
                    actual(Type.EXCLUSION);
                }
            }
        }
        if (y+1 < types[y].length) {
            if (types[y+1][x] == Type.NEW) {
                push(x, y+1);
            } else {
                // TODO
            }
        }
        if (x+1 < types[y].length) {
            if (types[y][x+1] == Type.NEW) {
                push(x+1, y);
            } else {
                if (code[y][x+1] == ']') {
                    actual(Type.METROPOLIS);
                } else if (code[y][x+1] == '}') {
                    actual(Type.EXCLUSION);
                }
            }
        }

        return true;
    }
    
    private void actual(Type type) {
        if (type == actual) {
            return;
        } else if (actual == Type.NEW) {
            actual = type;
        } else {
            throw new AssertionError(actual + " != " + type); 
        }
    }

    private void closeZone() {
//        assert actual != Type.NEW : actual;
        
        for (var point : zone) {
            types[point.y][point.x] = actual;
        }
        zone.clear();
        actual = Type.NEW;
    }
    
    private void push(int x, int y) {
        var point = new Point(x, y);
        if (open.remove(point)) {
            stack.remove(point);
            assert !zone.contains(point) : point;
            stack.push(point);
        }
    }
    
    @Override
    public void paint(Graphics2D gg, int size) {
        if (types != null) {
            for (var y = 0; y < types.length; y++) {
                for (var x = 0; x < types[y].length; x++) {
                    var color = types[y][x].color();
                    if (color != null) {
                        paintCell(gg, size, x, y, color);
                    }
                }
            }
        }
        
        gg.setColor(Color.BLACK);
        for (var point : open) {
            paintString(gg, size, point.x, point.y, "O");
        }
        for (var point : stack) {
            paintString(gg, size, point.x, point.y, "?");
        }
        for (var point : zone) {
            paintString(gg, size, point.x, point.y, "X");
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
