package cfh.zirconium.extra;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Point;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import javax.swing.JTextField;

public class Flood3Algorithm implements Algorithm {

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
    
    private enum Step {
        START,
        FIND,
        BORDER,
        FLOOD,
        FILL,
        END;
    }

    private final char[][] code;
    private final JTextField status;
    private Step step;
    
    private final Type[][] types;
    private final Deque<Point> stack = new LinkedList<>();
    private final Set<Point> border = new HashSet<>();
    private boolean[][] wall;
    private final List<Point> errors = new ArrayList<>();
    private Point pivot = null;
    private Iterator<Point> flood = null;
    private Type type;
    
    
    public Flood3Algorithm(char[][] code, JTextField status) {
        this.code = code;
        this.status = status;
        this.step = Step.START;
        types = new Type[code.length][code[0].length];
        status("");
    }

    @Override
    public boolean step() {
        switch (step) {
            case START: start(); return true;
            case FIND: find(); return true;
            case BORDER: border(); return true;
            case FLOOD: flood(); return true;
            case FILL: fill(); return true;
            case END: return false;
            default: throw new IllegalArgumentException("not implemented: " + step);
        }
    }
    
    private void start() {
        if (pivot == null) {
            pivot = new Point(-1, 0);
        }
        do {
            if (++pivot.x >= code[pivot.y].length) {
                pivot.x = 0;
                if (++pivot.y >= code.length) {
                    step = Step.END;
                    return;
                }
            }
        } while (types[pivot.y][pivot.x] != null || !isBorder(pivot.x, pivot.y));
        status("adding: (%d,%d)", pivot.x, pivot.y);
        border.clear();
        stack.clear();
        stack.push(new Point(pivot.x, pivot.y));
        step = Step.FIND;
    }
    
    private void find() {
        if (stack.isEmpty()) {
            status("stack empty");
            step = Step.BORDER;
            return;
        }
        
        var point = stack.pop();
        var x = point.x;
        var y = point.y;
        assert !border.contains(point) : point;
        
        border.add(point);
        var adding = "";
        for (var yy = y-1; yy <= y+1; yy++) {
            for (var xx = x-1; xx <= x+1; xx++) {
                if (   0 <= yy && yy < code.length 
                    && 0 <= xx && xx < code[yy].length 
                    && (xx != x || yy !=y)
                    && isBorder(xx, yy)) {
                    var p = new Point(xx, yy);
                    if (!border.contains(p) && !stack.contains(p)) {
                        stack.add(p);
                        adding += String.format(adding=="" ? ", adding: (%d,%d)" : ", (%d,%d)", p.x, p.y);
                    }
                }
            }
        }
        status("border: (%d,%d)" + adding, x, y);
    }
    
    private void border() {
        wall = new boolean[code.length][code[0].length];
        Type type = null;
        var msg = "";
        for (var point : border) {
            var t = switch (code[point.y][point.x]) {
                case '[', '=', ']' -> Type.FORT;
                case '{', '~', '}' -> Type.FENCE;
                default -> {
                    var err = "unknown border: '" + code[point.y][point.x] + "'";
                    msg += ", %s at (%d,%d)".formatted(err, point.x, point.y);
                    error(point, err);
                    yield Type.NONE;
                }
            };
            if (type == null) {
                type = t;
            } else if (t != type) {
                var err = "ambigous border: '" + code[point.y][point.x] + "'";
                msg += ", %s at (%d,%d)".formatted(err, point.x, point.y);
                error(point, err);
            }
            types[point.y][point.x] = type; 
            wall[point.y][point.x] = true;
        }
        flood = null;
        status("walls%s", msg);
        step = Step.FLOOD;
    }
    
    private void flood() {
        if (flood == null) {
            flood = border.iterator();
        }
        Point p;
        do {
            if (!flood.hasNext()) {
                border.clear();
                status("flooded");
                step = Step.START;
                return;
            }
            p = flood.next();
        } while (code[p.y][p.x] == '~' || code[p.y][p.x] == '=');
        stack.clear();
        if (p.x > 0 && (code[p.y][p.x] == ']' || code[p.y][p.x] == '}')) {
            type = code[p.y][p.x] == ']' ? Type.METROPOLIS : Type.EXCLUSION;
            stack.push(new Point(p.x-1, p.y));
            status("right %s at (%d,%d), adding: (%d,%d)", type, p.x, p.y, p.x-1, p.y);
            step = Step.FILL;
            return;
        }
        if (p.x+1 < code[p.y].length && (code[p.y][p.x] == '[' || code[p.y][p.x] == '{')) {
            type = code[p.y][p.x] == '[' ? Type.METROPOLIS : Type.EXCLUSION;
            stack.push(new Point(p.x+1, p.y));
            status("left %s at (%d,%d), adding: (%d,%d)", type, p.x, p.y, p.x+1, p.y);
            step = Step.FILL;
            return;
        }
        status("flood (%d,%d)", p.x, p.y);
    }
    
    private void fill() {
        if (stack.isEmpty()) {
            status("filled");
            step = Step.FLOOD;
            return;
        }
        var msg = "";
        var p = stack.pop();
        var x = p.x;
        var y = p.y;
        assert !wall[y][x] : p;

        if (types[y][x] != null && types[y][x] != type) {
            msg += ", ambigous zone %s/%s".formatted(types[y][x], type);
            error(p, "ambigous zone");
        } else {
            types[y][x] = type;
        }
        var prefix = ", adding: ";
        if (x+1 < types[y].length && types[y][x+1] == null) {
            msg += "%s(%d,%d)".formatted(prefix, x+1, y);
            prefix = ", ";
            stack.push(new Point(x+1, y));
        }
        if (y+1 < types.length && types[y+1][x] == null) {
            msg += "%s(%d,%d)".formatted(prefix, x, y+1);
            prefix = ", ";
            stack.push(new Point(x, y+1));
        }
        if (x > 0 && types[y][x-1] == null) {
            msg += "%s(%d,%d)".formatted(prefix, x-1, y);
            prefix = ", ";
            stack.push(new Point(x-1, y));
        }
        if (y > 0 && types[y-1][x] == null) {
            msg += "%s(%d,%d)".formatted(prefix, x, y-1);
            prefix = ", ";
            stack.push(new Point(x, y-1));
        }
        status("point (%d,%d)%s", x, y, msg);
    }
    
    @SuppressWarnings("unused")
    private void error(int x, int y, String message) {
        error(new Point(x, y), message);
    }
    
    private boolean isBorder(int x, int y) {
        return "[=]{~}".indexOf(code[y][x]) != -1;
    }
    
    private void error(Point point, String message) {
        errors.add(point);
        System.err.printf("(%d,%d) %s%n", point.x, point.y, message); 
    }
 
    @Override
    public void paint(Graphics2D gg, int size) {
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
        gg.setColor(Color.GREEN);
        for (var point : stack) {
            paintString(gg, size, point.x, point.y, "O");
        }
        for (var point : border) {
            paintCell(gg, size, point.x, point.y, Color.YELLOW);
        }
        gg.setColor(Color.BLUE);
        if (pivot != null) {
            paintString(gg, size, pivot.x, pivot.y, "< >");
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
    
    private void status(String format, Object... args) {
        var text = "%5.5s ".formatted(step) + format.formatted(args);
        status.setText(text);
    }
}
