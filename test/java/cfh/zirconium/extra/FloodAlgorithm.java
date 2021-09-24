package cfh.zirconium.extra;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Point;
import java.util.Deque;
import java.util.LinkedList;

public class FloodAlgorithm implements Algorithm {

    private final char[][] code;
    
    private final int[][] zones;
    private int zone = 0;
    private Point pivot = new Point(0, 0);
    private Point last = null;
    private final Deque<Point> stack = new LinkedList<>();
    
    
    public FloodAlgorithm(char[][] code) {
        this.code = code;
        zones = new int[code.length][code[0].length];
    }

    @Override
    public boolean step() {
        int x;
        int y;
        if (stack.isEmpty()) {
            last = null;
            while (code[pivot.y][pivot.x] != ' ' || zones[pivot.y][pivot.x] != 0) {
                if (++pivot.x >= code[pivot.y].length) {
                    pivot.x = 0;
                    if (++pivot.y >= code.length) {
                        return false;
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
        if (last != null) {
            gg.setColor(Color.BLUE);
            paintString(gg, size, last.x, last.y, "[ ]");
        }
    }
    
    private void paintString(Graphics2D gg, int size, int x, int y, String text) {
        var fm = gg.getFontMetrics();
        var dx = (size - fm.stringWidth(text)) / 2;
        var dy = size - fm.getDescent() - (size-fm.getAscent()-fm.getDescent())/2;
        gg.drawString(text, x*size+dx, y*size+dy);
    }
}
