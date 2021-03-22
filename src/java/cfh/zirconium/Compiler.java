package cfh.zirconium;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import cfh.zirconium.expr.Definition;
import cfh.zirconium.gui.Main.Printer;
import cfh.zirconium.net.*;

/** Compiler for Zirconium programs. */
public class Compiler {

    public static final char EMPTY = ' ';
    
    // Stations
    public static final char NOP = '0';
    public static final char CREATE = '@';
    public static final char DOT = '.';
    public static final char DUP = 'o';
    public static final char Q = 'Q';
    public static final char SPLIT = 'O';
    
    // Tunnels
    public static final char HORZ = '-';
    public static final char VERT = '|';
    public static final char DIAG_U = '/';
    public static final char DIAG_D = '\\';
    public static final char CROSS_HV = '+';
    public static final char CROSS_DD = 'X';
    public static final char CROSS_ALL = '*';
    
    // Apertures
    public static final char APERT_N = '^';
    public static final char APERT_E = '>';
    public static final char APERT_S = 'v';
    public static final char APERT_W = '<';
    public static final char APERT_DIAG = '#';
    
    // TODO exclusion zones, defect stations
    
    // TODO metropolis, Synthetic stations
    
    // TODO add fences as *
    // TODO add forts as *
    /** Directions. */
    private enum Dir {
        N ( 0, -1, APERT_S, APERT_N, VERT, CROSS_HV, CROSS_ALL),
        NE(+1, -1, APERT_DIAG, APERT_DIAG, DIAG_U, CROSS_DD, CROSS_ALL),
        E (+1,  0, APERT_W, APERT_E, HORZ, CROSS_HV, CROSS_ALL),
        SE(+1, +1, APERT_DIAG, APERT_DIAG, DIAG_D, CROSS_DD, CROSS_ALL),
        S ( 0, +1, APERT_N, APERT_S, VERT, CROSS_HV, CROSS_ALL),
        SW(-1, +1, APERT_DIAG, APERT_DIAG, DIAG_U, CROSS_DD, CROSS_ALL),
        W (-1,  0, APERT_E, APERT_W, HORZ, CROSS_HV, CROSS_ALL),
        NW(-1, -1, APERT_DIAG, APERT_DIAG, DIAG_D, CROSS_DD, CROSS_ALL);
        
        /** X step for this direction. */
        final int dx;
        /** Y step for this direction. */
        final int dy;
        /** Aperture comming from this direction. */
        final char apertureIn;
        /** Aperture at end of this direction. */
        final char apertureOut;
        /** Valid tunnels for this direction, excluded apertures. */
        final String tunnels;
        /** Creates direciton instance. */
        private Dir(int dx, int dy, char apertureIn, char apertureOut, char... tunnels) {
            this.dx = dx;
            this.dy = dy;
            this.apertureIn = apertureIn;
            this.apertureOut = apertureOut;
            this.tunnels = new String(tunnels);
        }
        /** Is given character an incomming aperture for this direction. */
        boolean isIn(char ch) { return ch == apertureIn; }
        /** Is given character an outgoing aperture for this direction. */
        boolean isOut(char ch) { return ch == apertureOut; }
        /** Is given character a valid tunnel for this direction, excluded apertures. */
        boolean isTunnel(char ch) { return tunnels.indexOf(ch) != -1; }
    }
    
    //----------------------------------------------------------------------------------------------
    
    private final Printer printer;
    
    /** Creates a compiler. */
    public Compiler(Printer printer) {
        this.printer = Objects.requireNonNull(printer);
    }
    
    /** Compiles the given code and creates a program with givne name. */
    public Program compile(String name, String code) throws CompileException {
        printer.print("%n");
        
        char[][] chars = parse(code);
        
        var definitions = bubblesLenses(chars);
        
        var singles = scanStations(chars);
        
        var stations = bound(chars, singles);
        
        link(chars, singles);
        // TODO check unconnected tunnels
        
        // exclusion zones
        
        // metropolis
        
        return new Program(name, stations, printer);
    }
    
    /** 
     * Creates a character matrix for given code. 
     * One extra empty row/column is added to each side of boundary.
     */
    private char[][] parse(String code) {
        var lines = code.split("\n");
        var rows = lines.length;
        printer.print("%d rows%n", rows);
        
        var cols = Arrays.stream(lines).mapToInt(String::length).max().orElse(0);
        printer.print("%d columns%n", cols);
        
        var chars = new char[rows+2][cols+2];
        Arrays.fill(chars[0], EMPTY);
        Arrays.fill(chars[rows+1], EMPTY);
        for (var i = 0; i < rows; i++) {
            var line = lines[i];
            var row = chars[i+1];
            Arrays.fill(row, ' ');
            for (var j = 0; j < line.length(); j++) {
                row[j+1] = line.charAt(j);
            }
        }
        return chars;
    }
    
    /** Removes bubles {@code (...)} and extract definitions from lenses {@code ((...))}. */
    private Collection<Definition> bubblesLenses(char[][] chars) throws CompileException {
        var definitions = new HashSet<Definition>();
        for (var y = 0; y < chars.length; y++) {
            var row = chars[y];
            for (var x = 0; x < row.length; x++) {
                if (row[x] == '(') {
                    row[x] = ' ';
                    x += 1;
                    // lens
                    if (row[x] == '(') {
                        row[x] = ' ';
                        x += 1;
                        var  start = x;
                        var expr = "";
                        while (x < row.length) {
                            if (row[x] == ')') {
                                row[x] = ' ';
                                break;
                            }
                            expr += row[x];
                            row[x] = ' ';
                            x += 1;
                        }
                        if (x == row.length || row[x+1] != ')') {
                            throw new CompileException(new Pos(x, y), "lens not correctly terminated");
                        }
                        x += 1;
                        row[x] = ' ';
                        var pos = new Pos(start, y);
                        var def = Definition.parse(pos, expr);
                        if (definitions.contains(def)) {
                            throw new CompileException(pos, "duplicated definition");
                        }
                        definitions.add(def);
                    } else {
                        while (x < row.length) {
                            if (row[x] == ')') {
                                row[x] = ' ';
                                break;
                            }
                            row[x] = ' ';
                            x += 1;
                        }
                        if (x == row.length) {
                            throw new CompileException(new Pos(x, y), "bubble not correctly terminated");
                        }
                    }
                }
            }
        }
        return definitions;
    }
    
    /** Scans the character matrix for stations. */
    private Map<Pos, Single> scanStations(char[][] chars) throws CompileException {
        var map = new HashMap<Pos, Single>();
        for (var y = 1; y < chars.length; y++) {
            var row = chars[y];
            for (var x = 1; x < row.length; x++) {
                var ch = row[x];
                var station = switch (ch) {
                    case ' ' -> null;
                    case NOP -> new NopStation(x, y, printer);
                    case CREATE -> new CreateStation(x, y, printer);
                    case DOT -> new DotStation(x, y, printer);
                    case DUP -> new DupStation(x, y, printer);
                    case Q -> new QStation(x, y, printer);
                    case SPLIT -> new SplitStation(x, y, printer);
                    case HORZ, VERT, DIAG_U, DIAG_D, CROSS_HV, CROSS_DD, CROSS_ALL, 
                         APERT_N, APERT_E, APERT_S, APERT_W, APERT_DIAG -> null;
                    default -> throw new CompileException(new Pos(x, y), "unrecognized symbol '" + ch + "'");
                    
                    // {TEST} must be inside exclusion zone
                    case '`' -> new NumOutStation(x, y, printer);
                };
                if (station != null) {
                    map.put(station.pos(), station);
                }
            }
        }
        printer.print("scaned %d stations%n", map.size());
        return map;
    }
    
    /**
     * Creates bound station from adjacent single stations.
     * Returns a list including all bound stations and all stations that are not bounded.
     */
    private List<Station> bound(char[][] chars, Map<Pos, Single> singles) throws CompileException {
        var stations = new ArrayList<Station>();
        var boundID = 0;
        var count = 0;
        for (var station : singles.values()) {
            Bound bound = null;
            for (var n : new ArrayList<>(stations)) {
                if (n.isNeighbour(station)) {
                    if (bound == null) {
                        if (n instanceof Single s) {
                            stations.remove(s);
                            bound = new Bound(boundID++, printer, s, station);
                            stations.add(bound);
                            count += 1;
                        } else if (n instanceof Bound b) {
                            bound = b;
                            bound.addChild(station);
                        } else {
                            throw new CompileException(station.pos(), "unhandled neighbour " + n.getClass().getSimpleName());
                        }
                    } else {
                        if (n instanceof Single s) {
                            stations.remove(s);
                            bound.addChild(s);
                        } else if (n instanceof Bound b) {
                            stations.remove(b);
                            b.childs().forEach(bound::addChild);
                            bound = b;
                            count -= 1;
                        } else {
                            throw new CompileException(station.pos(), "unhandled neighbour " + n.getClass().getSimpleName());
                        }
                    }
                }
            }
            if (bound == null) {
                stations.add(station);
            }
        }
        printer.print("%d bound stations%n", count);
        return stations;
    }
    
    /** Links stations. */
    private void link(char[][] chars, Map<Pos, Single> singles) throws CompileException {
        // TODO # needs at least one additional connection
        var count = 0;
        for (var station : singles.values()) {
            for (var dir : Dir.values()) {
                var link = dir.ordinal() < 4;
                var x = station.x() + dir.dx;
                var y = station.y() + dir.dy;
                var ch = chars[y][x];
                if (dir.isIn(ch)) {
                    boolean diag = ch == APERT_DIAG;
                    do {
                        x += dir.dx;
                        y += dir.dy;
                        ch = chars[y][x];
                        if (diag && dir.isTunnel(ch)) {
                            diag = false;
                        }
                    } while (dir.isTunnel(ch));
//                    if (diag) {
//                        continue;
//                    }
                    if (!diag) {
                        var pos = new Pos(x, y);
                        var start = singles.get(pos);
                        if (start == null) {
                            throw new CompileException(pos, String.format(
                                "tunnel not starting at a station, %s %s", dir, station));
                        }
                        if (link) {
                            start.linkTo(station);
                            count += 1;
                        }
                    }
                } 
                if (dir.isOut(ch)) {
                    var pos = new Pos(x+dir.dx, y+dir.dy);
                    var dest = singles.get(pos);
                    if (dest == null) {
                        throw new CompileException(pos, String.format(
                            "tunnel not ending at a stationm %s %s", dir, station));
                    }
                    if (link) {
                        station.linkTo(dest);
                        count += 1;
                    }
                }
                if (dir.isTunnel(ch)) {
                    do {
                        x += dir.dx;
                        y += dir.dy;
                        ch = chars[y][x];
                    } while (dir.isTunnel(ch));
                    var directed = dir.isOut(ch);
                    if (directed) {
                        x += dir.dx;
                        y += dir.dy;
                        ch = chars[y][x];
                    }
                    var pos = new Pos(x, y);
                    var dest = singles.get(pos);
                    if (dest == null) {
                        throw new CompileException(pos, String.format(
                            "tunnel not ending at a station, %s %s", dir, station));
                    }
                    if (link) {
                        station.linkTo(dest);
                        count += 1;
                        if (!directed) {
                            dest.linkTo(station);
                            count += 1;
                        }
                    }
                }
            }
        }
        printer.print("%d links created%n", count);
    }
    
    //==============================================================================================
    
    /** Exception throw by {@link Compiler}. */
    @SuppressWarnings("serial")
    public static class CompileException extends Exception {
        /** Position of error, can be {@code null}. */
        public final Pos pos;
        /** Creates new exception without position. */
        public CompileException(String message) {
            this(null, message);
        }
        /** Creates new exception. */
        public CompileException(Pos pos, String message) {
            super(message);
            this.pos = pos;
        }
        @Override
        public String getMessage() {
            return (pos==null?"":pos+" ") + super.getMessage();
        }
        @Override
        public String toString() {
            return (pos==null?"":pos) + super.toString();
        }
    }
}
