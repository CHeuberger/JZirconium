package cfh.zirconium;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import cfh.zirconium.expr.Definition;
import cfh.zirconium.net.*;

/** Compiler for Zirconium programs. */
public class Compiler {

    public static final char EMPTY = ' ';
    
    // Stations
    public static final char NOP = '0';
    public static final char CREATE = '@';
    public static final char DOT = '.';
    public static final char DUP = 'o';
    public static final char DEC = 'Q';
    public static final char SPLIT = 'O';
    
    // Tunnels
    public static final char HORZ = '-';
    public static final char VERT = '|';
    public static final char DIAG_U = '/';
    public static final char DIAG_D = '\\';
    public static final char CROSS_HV = '+';
    public static final char CROSS_DD = 'X';
    public static final char CROSS_ALL = '*';
    public static final String TUNNELS = "" + HORZ + VERT + DIAG_U + DIAG_D + CROSS_HV + CROSS_DD + CROSS_ALL;
    
    // Apertures
    public static final char APERT_N = '^';
    public static final char APERT_E = '>';
    public static final char APERT_S = 'v';
    public static final char APERT_W = '<';
    public static final char APERT_DIAG = '#';
    public static final String APERTURES = "" + APERT_N + APERT_E + APERT_S + APERT_W + APERT_DIAG;
 
    // Defect Stations
    public static final char BYTE_IN = '?';
    public static final char BYTE_OUT = '%';
    public static final char BYTE_ERR = '&';
    public static final char NUM_IN  = '_';
    public static final char NUM_OUT = '`';
    public static final char PAUSE= ';';
    public static final char HALT = '!';
    
    // Exclusion Zones
    public static final char EZ_L = '{';
    public static final char EZ_H = '~';
    public static final char EZ_R = '}';
    public static final String FENCES = "" + EZ_L + EZ_H + EZ_R;
    
    // Metropolis
    public static final char MP_L = '[';
    public static final char MP_H = '=';
    public static final char MP_R = ']';
    public static final String FORTS = "" + MP_L + MP_H + MP_R;
    
    public static final String NOT_STATION = " \t" + TUNNELS + APERTURES + FENCES + FORTS;

    /** Directions. */
    enum Dir {
        N ( 0, -1, APERT_S, APERT_N, VERT, CROSS_HV),
        NE(+1, -1, APERT_DIAG, APERT_DIAG, DIAG_U, CROSS_DD),
        E (+1,  0, APERT_W, APERT_E, HORZ, CROSS_HV),
        SE(+1, +1, APERT_DIAG, APERT_DIAG, DIAG_D, CROSS_DD),
        S ( 0, +1, APERT_N, APERT_S, VERT, CROSS_HV),
        SW(-1, +1, APERT_DIAG, APERT_DIAG, DIAG_U, CROSS_DD),
        W (-1,  0, APERT_E, APERT_W, HORZ, CROSS_HV),
        NW(-1, -1, APERT_DIAG, APERT_DIAG, DIAG_D, CROSS_DD);
        
        /** X step for this direction. */
        final int dx;
        /** Y step for this direction. */
        final int dy;
        /** Aperture comming from this direction. */
        final char apertureIn;
        /** Aperture at end of this direction. */
        final char apertureOut;
        /** Direct tunnel. */
        final char direct;
        /** Valid tunnels for this direction, excluded apertures. */
        final String tunnels;
        /** Creates direciton instance. */
        private Dir(int dx, int dy, char apertureIn, char apertureOut, char direct, char... tunnels) {
            this.dx = dx;
            this.dy = dy;
            this.apertureIn = apertureIn;
            this.apertureOut = apertureOut;
            this.direct = direct;
            this.tunnels = direct + new String(tunnels) + CROSS_ALL + FENCES + FORTS;
        }
        /** Is given character an incomming aperture for this direction. */
        boolean isIn(char ch) { return ch == apertureIn; }
        /** Is given character an outgoing aperture for this direction. */
        boolean isOut(char ch) { return ch == apertureOut; }
        /** Is direct tunnel. */
        boolean isDirect(char ch) { return ch == direct; }
        /** Is given character a valid tunnel for this direction, excluded apertures. */
        boolean isTunnel(char ch) { return tunnels.indexOf(ch) != -1; }
    }
    
    //----------------------------------------------------------------------------------------------
    
    private final Environment env;
    
    /** Creates a compiler. */
    public Compiler(Environment env) {
        this.env = Objects.requireNonNull(env);
    }
    
    // TODO compile as much as possible, accumulate errors
    /** Compiles the given code and creates a program with givne name. */
    public Program compile(String name, String code) throws CompileException {
        env.print("%n");
        
        char[][] chars = parse(code);
        
        Map<Character, Definition> definitions = bubblesLenses(chars);
        
        // TODO boolean[][]
        int[][] exclusion = new ZoneDetector("fence", EZ_L, EZ_H, EZ_R, chars).detect();
        int[][] metropolis = new ZoneDetector("fort", MP_L, MP_H, MP_R, chars).detect();
        
        Map<Pos, Single> singles = scanStations(chars, exclusion, metropolis, definitions);
        
        List<Station> stations = bound(chars, singles);
        
        link(chars, singles);
        // TODO check unconnected tunnels
        
        return new Program(name, stations, env);
    }
    
    /** 
     * Creates a character matrix for given code. 
     * One extra empty row/column is added to each side of boundary.
     */
    private char[][] parse(String code) {
        var lines = code.split("\n");
        var rows = lines.length;
        env.print("%d rows%n", rows);
        
        var cols = Arrays.stream(lines).mapToInt(String::length).max().orElse(0);
        env.print("%d columns%n", cols);
        
        var chars = new char[rows+2][cols+2];
        Arrays.fill(chars[0], EMPTY);
        Arrays.fill(chars[rows+1], EMPTY);
        for (var y = 0; y < rows; y++) {
            var line = lines[y];
            var row = chars[y+1];
            Arrays.fill(row, EMPTY);
            for (var x = 0; x < line.length(); x++) {
                row[x+1] = line.charAt(x);
            }
        }
        return chars;
    }
    
    /** Removes bubles {@code (...)} and extract definitions from lenses {@code ((...))}. */
    private Map<Character, Definition> bubblesLenses(char[][] chars) throws CompileException {
        var definitions = new HashMap<Character, Definition>();
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
                        if (definitions.containsKey(def.symbol)) {
                            throw new CompileException(pos, "duplicated definition");
                        }
                        definitions.put(def.symbol, def);
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
    
    /** Find exclusion zones. */
    int[][] zones(String name, char left, char horiz, char right, char[][] chars) throws CompileException {
        return new ZoneDetector(name, left, horiz, right, chars).detect();
    }
    
    /** Scans the character matrix for stations. */
    private Map<Pos, Single> scanStations(char[][] chars, int[][] exclusion, int[][] metropolis, Map<Character, Definition> definitions) throws CompileException {
        var map = new HashMap<Pos, Single>();
        for (var y = 1; y < chars.length; y++) {
            var row = chars[y];
            for (var x = 1; x < row.length; x++) {
                var ch = row[x];
                if (NOT_STATION.indexOf(ch) == -1) {
                    Single station;
                    var excl = exclusion[y][x] != 0;
                    var metro = metropolis[y][x] != 0;
                    if (!excl && !metro) {
                        station = switch (ch) {
                            case NOP -> new NopStation(x, y, env);
                            case CREATE -> new CreateStation(x, y, env);
                            case DOT -> new DotStation(x, y, env);
                            case DUP -> new DupStation(x, y, env);
                            case DEC -> new DecStation(x, y, env);
                            case SPLIT -> new SplitStation(x, y, env);
                            case BYTE_IN, BYTE_OUT, BYTE_ERR, NUM_IN, NUM_OUT, PAUSE, HALT 
                                 -> throw new CompileException(new Pos(x, y), "'" + ch + "' station only valid in Exclusion Zone");
                            default -> throw new CompileException(new Pos(x, y), "unrecognized station  '" + ch + "'");
                        };
                    } else if (excl && !metro) {
                        station = switch (ch) {
                            case BYTE_IN -> new ByteInStation(x, y, env);
                            case BYTE_OUT -> new ByteOutStation(x, y, env);
                            case BYTE_ERR -> new ByteErrStation(x, y, env);
                            case NUM_IN -> new NumInStation(x, y, env);
                            case NUM_OUT -> new NumOutStation(x, y, env);
                            case PAUSE -> new PauseStation(x, y, env);
                            case HALT -> new HaltStation(x, y, env);
                            case NOP, CREATE, DOT, DUP, DEC, SPLIT
                                 -> throw new CompileException(new Pos(x, y), "'" + ch + "' station not valid in Exclusion Zone");
                            default -> throw new CompileException(new Pos(x, y), "unrecognized station  '" + ch + "'");
                        };
                    } else if (!excl && metro) {
                        var def = definitions.get(ch);
                        if (def == null) {
                            throw new CompileException(new Pos(x, y), "unrecognized station  '" + ch + "'");
                        } else {
                            station =  new SyntheticStation(x, y, env, def);
                        }
                    } else {
                        throw new CompileException(new Pos(x, y), "mixed zones");
                    }
                    map.put(station.pos(), station);
                }
            }
        }
        env.print("scaned %d stations%n", map.size());
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
                            var id = "" + (char)('A' + boundID / 10) + (boundID % 10);
                            boundID += 1;
                            bound = new Bound(id, env, s, station);
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
        env.print("%d bound stations%n", count);
        return stations;
    }
    
    /** Links stations. */
    private void link(char[][] chars, Map<Pos, Single> singles) throws CompileException {
        var count = 0;
        for (var station : singles.values()) {
            for (var dir : Dir.values()) {
                var x = station.x() + dir.dx;
                var y = station.y() + dir.dy;
                var ch = chars[y][x];
                if (dir.isTunnel(ch) || dir.isOut(ch) && !dir.isIn(ch)) {
                    var direct = false;
                    while (dir.isTunnel(ch)) {
                        if (dir.isDirect(ch) ) {
                            direct = true;
                        }
                        x += dir.dx;
                        y += dir.dy;
                        ch = chars[y][x];
                    }
                    if (dir.isOut(ch)) {
                        direct = true;
                        x += dir.dx;
                        y += dir.dy;
                        ch = chars[y][x];
                        if ((FENCES+FORTS).indexOf(ch) != -1) {
                            x += dir.dx;
                            y += dir.dy;
                        }
                    }
                    var pos = new Pos(x, y);
                    var dest = singles.get(pos);
                    if (dest != null) {
                        station.linkTo(dest);
                        count += 1;
                    } else if (direct) {
                        throw new CompileException(pos, String.format(
                            "tunnel not ending at a stationm %s %s", dir, station));
                    }
                }
            }
        }
        env.print("%d links created%n", count);
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
