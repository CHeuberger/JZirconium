package cfh.zirconium.extra;

import java.awt.Color;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.Set;

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
    private final Set<Point> border = new HashSet<>();
    private final List<Point> errors = new ArrayList<>();
    private Type actual = Type.NEW;

    public Flood2Algorithm(char[][] code) {
        this.code = code;
    }

    @Override
    public boolean step() {
        if (types == null) {
            types = new Type[code.length][];
            for (int i = 0; i < code.length; i++) {
                types[i] = new Type[code[i].length];
                for (int j = 0; j < code[i].length; j++) {
                    char ch = code[i][j];
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
        
        Point point = stack.poll();
        if (point == null) {
            closeZone();
            
            point = open.poll();
            if (point == null) {
                closeZone();
                checkAllBorder();
                return false;
            }
        }
        
        zone.add(point);
        int x = point.x;
        int y = point.y;

        if (y > 0) {
            Type type = types[y-1][x];
            if (type == Type.NEW) {
                push(x, y-1);
            } else if (type == Type.FENCE || type == Type.FORT) {
                border.add(new Point(x, y-1));
            } else {
                // TODO ?
            }
        }
        if (x > 0) {
            Type type = types[y][x-1];
            if (type == Type.NEW) {
                push(x-1, y);
            } else if (type == Type.FENCE || type == Type.FORT) {
                border.add(new Point(x-1, y));
                switch (code[y][x-1]) {
                    case '[': actual(x-1, y, Type.METROPOLIS); break;
                    case '{': actual(x-1, y, Type.EXCLUSION); break;
                    default: break;
                }
            } else {
                // TODO ?
            }
        }
        if (y+1 < types[y].length) {
            Type type = types[y+1][x];
            if (type == Type.NEW) {
                push(x, y+1);
            } else if (type == Type.FENCE || type == Type.FORT) {
                border.add(new Point(x, y+1));
            } else {
                // TODO ?
            }
        }
        if (x+1 < types[y].length) {
            Type type = types[y][x+1];
            if (type == Type.NEW) {
                push(x+1, y);
            } else if (type == Type.FENCE || type == Type.FORT) {
                border.add(new Point(x+1, y));
                switch (code[y][x+1]) {
                    case ']': actual(x-1, y, Type.METROPOLIS); break;
                    case '}': actual(x-1, y, Type.EXCLUSION); break;
                    default: break;
                }
            } else {
                // TODO ?
            }
        }

        return true;
    }
    
    void actual(int x, int y, Type type) {
        if (type == actual) {
            return;
        } else if (actual == Type.NEW) {
            actual = type;
        } else {
            error(x, y, actual + " != " + type); 
        }
    }
    
    private void error(int x, int y, String message) {
        error(new Point(x, y), message);
    }
    
    private void error(Point point, String message) {
        errors.add(point);
        System.err.printf("(%d,%d) %s%n", point.x, point.y, message); 
    }

    private void closeZone() {
//        assert actual != Type.NEW : actual;
        
        for (Point point : zone) {
            types[point.y][point.x] = actual;
        }
        zone.clear();
        border.clear();
        actual = Type.NEW;
    }
    
    private void checkAllBorder() {
//        for (var y = 0; y < code.length; y++) {
//            var row = code[y];
//            for (var x = 0; x < row.length; x++) {
//                var ch = row[x];
//                switch (ch) {
//                    
//                }
//            }
//        }
    }
    
    private void push(int x, int y) {
        Point point = new Point(x, y);
        if (open.remove(point)) {
            stack.remove(point);
            assert !zone.contains(point) : point;
            stack.push(point);
        }
    }
    
    @Override
    public void paint(Graphics2D gg, int size) {
        if (types != null) {
            for (int y = 0; y < types.length; y++) {
                for (int x = 0; x < types[y].length; x++) {
                    Color color = types[y][x].color();
                    if (color != null) {
                        paintCell(gg, size, x, y, color);
                    }
                }
            }
        }
        
        gg.setColor(Color.BLACK);
        for (Point point : open) {
            paintString(gg, size, point.x, point.y, "O");
        }
        for (Point point : stack) {
            paintString(gg, size, point.x, point.y, "?");
        }
        for (Point point : zone) {
            paintString(gg, size, point.x, point.y, "X");
        }
        gg.setColor(Color.YELLOW);
        for (Point point : border) {
            paintString(gg, size, point.x, point.y, "> <");
        }
        for (Point point : errors) {
            paintCell(gg, size, point.x, point.y, Color.RED);
        }
    }

    private void paintCell(Graphics2D gg, int size, int x, int y, Color color) {
        gg.setColor(color);
        gg.fillRect(x*size+1, y*size+1, size-1, size-1);
    }
    
    private void paintString(Graphics2D gg, int size, int x, int y, String text) {
        FontMetrics fm = gg.getFontMetrics();
        int dx = (size - fm.stringWidth(text)) / 2;
        int dy = size - fm.getDescent() - (size-fm.getAscent()-fm.getDescent())/2;
        gg.drawString(text, x*size+dx, y*size+dy);
    }
}
