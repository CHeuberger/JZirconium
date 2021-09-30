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
    public static final String DEFECT_STATIONS = "" + BYTE_IN + BYTE_OUT + BYTE_ERR + NUM_IN + NUM_OUT + PAUSE + HALT;

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
    
    // Combinations
    public static final String BORDER = FENCES + FORTS;
    public static final String NOT_STATION = " \t" + TUNNELS + APERTURES + FENCES + FORTS;

    /** Zone Types. */
    enum Zone {
        NONE, EXCLUSION, METROPOLIS;
    }

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
    
    /** Running environment, basic standard and error I/O. */
    private final Environment environment;
    
    /** Creates a compiler. */
    public Compiler(Environment env) {
        this.environment = Objects.requireNonNull(env);
    }
    
    // TODO compile as much as possible, accumulate errors
    /** Compiles the given code and creates a program with givne name. */
    public Program compile(String name, String code, String header) throws CompileException {
        environment.print("%n");
        
        char[][] chars = parse(code);
        
        Map<Character, Definition> definitions = parseHeaderFile(header);
        
        definitions.putAll(bubblesLenses(chars));
        
        Zone[][] zones = new ZoneDetector(chars).detect();
        
        Map<Pos, Single> singles = scanStations(chars, zones, definitions);
        
        List<Station> stations = bound(chars, singles);
        
        link(chars, singles);
        // TODO check unconnected tunnels
        
        return new Program(name, stations, environment);
    }
    
    /** 
     * Creates a character matrix for given code. 
     * One extra empty row/column is added to each side of boundary.
     */
    private char[][] parse(String code) {
        String[] lines = code.split("\n", -1);
        int rows = lines.length;
        environment.print("%d rows%n", rows);
        
        int cols = Arrays.stream(lines).mapToInt(String::length).max().orElse(0);
        environment.print("%d columns%n", cols);
        
        char[][] chars = new char[rows][cols];
        for (int y = 0; y < rows; y++) {
            String line = lines[y];
            char[] row = chars[y];
            Arrays.fill(row, EMPTY);
            for (int x = 0; x < line.length(); x++) {
                row[x] = line.charAt(x);
            }
        }
        return chars;
    }
    
    /** Extract definitions from header file. */
    private Map<Character, Definition> parseHeaderFile(String header) throws CompileException {
        Map<Character, Definition> definitions = new HashMap<>();
        int count = 0;
        for (String line : header.split("\n")) {
            if (!line.trim().isEmpty()) {
                Pos pos = new Pos(0, count);
                Definition def;
                try {
                    def = Definition.parse(pos, line);
                } catch (CompileException ex) {
                    throw (CompileException) new CompileException(ex.pos, true, ex.getMessage()).initCause(ex);
                }
                if (definitions.containsKey(def.symbol)) {
                    throw new CompileException(pos, true, "duplicated definition");
                }
                definitions.put(def.symbol, def);
            }
            count += 1;
        }
        return definitions;
    }
    
    /** Removes bubles {@code (...)} and extract definitions from lenses {@code ((...))}. */
    private Map<Character, Definition> bubblesLenses(char[][] chars) throws CompileException {
        Map<Character, Definition> definitions = new HashMap<>();
        for (int y = 0; y < chars.length; y++) {
            char[] row = chars[y];
            for (int x = 0; x < row.length; x++) {
                if (row[x] == '(') {
                    row[x] = ' ';
                    x += 1;
                    // lens
                    if (x < row.length && row[x] == '(') {
                        row[x] = ' ';
                        x += 1;
                        int  start = x;
                        String expr = "";
                        while (x < row.length) {
                            if (row[x] == ')') {
                                row[x] = ' ';
                                break;
                            }
                            expr += row[x];
                            row[x] = ' ';
                            x += 1;
                        }
                        x += 1;
                        if (x >= row.length || row[x] != ')') {
                            throw new CompileException(new Pos(x-1, y), "lens not correctly terminated");
                        }
                        row[x] = ' ';
                        Pos pos = new Pos(start, y);
                        if (!expr.trim().isEmpty()) {
                            Definition def = Definition.parse(pos, expr);
                            if (definitions.containsKey(def.symbol)) {
                                throw new CompileException(pos, "duplicated definition");
                            }
                            definitions.put(def.symbol, def);
                        }
                    } else {
                        while (x < row.length) {
                            if (row[x] == ')') {
                                row[x] = ' ';
                                break;
                            }
                            row[x] = ' ';
                            x += 1;
                        }
                        if (x >= row.length) {
                            throw new CompileException(new Pos(x, y), "bubble not correctly terminated");
                        }
                    }
                }
            }
        }
        return definitions;
    }
    
    /** Scans the character matrix for stations. */
    private Map<Pos, Single> scanStations(char[][] chars, Zone[][] zones, Map<Character, Definition> definitions) throws CompileException {
        Map<Pos, Single> map = new HashMap<>();
        for (int y = 0; y < chars.length; y++) {
            char[] row = chars[y];
            for (int x = 0; x < row.length; x++) {
                char ch = row[x];
                if (NOT_STATION.indexOf(ch) == -1) {
                    Single station;
                    switch (zones[y][x]) {
                        case NONE: {
                            station = pureStation(ch, x, y);
                            if (station == null) {
                                if (DEFECT_STATIONS.indexOf(ch) != -1) {
                                    throw new CompileException(new Pos(x, y), "'" + ch + "' station only valid in Exclusion Zone");
                                } else {
                                    throw new CompileException(new Pos(x, y), "unrecognized station  '" + ch + "'");
                                }
                            }
                            break;
                        }
                        case EXCLUSION: {
                            switch (ch) {
                                case BYTE_IN: station = new ByteInStation(x, y, environment); break;
                                case BYTE_OUT: station = new ByteOutStation(x, y, environment); break;
                                case BYTE_ERR: station = new ByteErrStation(x, y, environment); break;
                                case NUM_IN: station = new NumInStation(x, y, environment); break;
                                case NUM_OUT: station = new NumOutStation(x, y, environment); break;
                                case PAUSE: station = new PauseStation(x, y, environment); break;
                                case HALT: station = new HaltStation(x, y, environment); break;
                                case NOP:
                                case CREATE:
                                case DOT:
                                case DUP:
                                case DEC:
                                case SPLIT: 
                                    station = pureStation(ch, x, y);
                                    break;
                                default:
                                    throw new CompileException(new Pos(x, y), "unrecognized station  '" + ch + "'");
                            }
                            break;
                        }
                        case METROPOLIS: {
                            Definition def = definitions.get(ch);
                            if (def != null) {
                                station =  new SyntheticStation(x, y, environment, def);
                            } else {
                                station = pureStation(ch, x, y);
                                if (station == null) {
                                    throw new CompileException(new Pos(x, y), "unrecognized station  '" + ch + "'");
                                }
                            }
                            break;
                        }
                        default: 
                            throw new CompileException(new Pos(x, y), "unhandled Zone: " + zones[y][x]);
                    }
                    map.put(station.pos(), station);
                }
            }
        }
        environment.print("scaned %d stations%n", map.size());
        return map;
    }
    
    /** Create pure station for given char. */
    private Single pureStation(char ch, int x, int y) {
        switch (ch) {
            case NOP: return new NopStation(x, y, environment);
            case CREATE: return new CreateStation(x, y, environment);
            case DOT: return new DotStation(x, y, environment);
            case DUP: return new DupStation(x, y, environment);
            case DEC: return new DecStation(x, y, environment);
            case SPLIT: return new SplitStation(x, y, environment);
            default: return null;
        }
    }
    
    /**
     * Creates bound station from adjacent single stations.
     * Returns a list including all bound stations and all stations that are not bounded.
     */
    private List<Station> bound(char[][] chars, Map<Pos, Single> singles) throws CompileException {
        List<Station> stations = new ArrayList<>();
        int boundID = 0;
        int count = 0;
        for (Single station : singles.values()) {
            Bound bound = null;
            for (Station n : new ArrayList<>(stations)) {
                if (n.isNeighbour(station)) {
                    if (bound == null) {
                        if (n instanceof Single) {
                            Single s = (Single) n;
                            stations.remove(s);
                            String id = "" + (char)('A' + boundID / 10) + (boundID % 10);
                            boundID += 1;
                            bound = new Bound(id, environment, s, station);
                            stations.add(bound);
                            count += 1;
                        } else if (n instanceof Bound) {
                            Bound b = (Bound) n;
                            bound = b;
                            bound.addChild(station);
                        } else {
                            throw new CompileException(station.pos(), "unhandled neighbour " + n.getClass().getSimpleName());
                        }
                    } else {
                        if (n instanceof Single) {
                            Single s = (Single) n;
                            stations.remove(s);
                            bound.addChild(s);
                        } else if (n instanceof Bound) {
                            Bound b = (Bound) n;
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
        environment.print("%d bound stations%n", count);
        return stations;
    }
    
    /** Links stations. */
    private void link(char[][] chars, Map<Pos, Single> singles) throws CompileException {
        int count = 0;
        for (Single station : singles.values()) {
            for (Dir dir : Dir.values()) {
                int x = station.x() + dir.dx;
                int y = station.y() + dir.dy;
                if (!valid(x, y, chars)) {
                    continue;
                }
                char ch = chars[y][x];
                if (dir.isTunnel(ch) || dir.isOut(ch) && !dir.isIn(ch)) {
                    boolean direct = false;
                    while (dir.isTunnel(ch)) {
                        if (dir.isDirect(ch) ) {
                            direct = true;
                        }
                        x += dir.dx;
                        y += dir.dy;
                        ch = valid(x, y, chars) ? chars[y][x] : EMPTY;
                    }
                    if (dir.isOut(ch)) {
                        direct = true;
                        x += dir.dx;
                        y += dir.dy;
                        if (valid(x, y, chars)) {
                            ch = chars[y][x];
                            if (BORDER.indexOf(ch) != -1) {
                                x += dir.dx;
                                y += dir.dy;
                            }
                        }
                    }
                    Pos pos = new Pos(x, y);
                    Single dest = valid(x, y, chars) ? singles.get(pos) : null;
                    if (dest != null) {
                        station.linkTo(dest);
                        count += 1;
                    } else if (direct) {    // TODO need direct?
                        throw new CompileException(pos, String.format(
                            "tunnel not ending at a stationm %s of %s", dir, station));
                    }
                }
            }
        }
        environment.print("%d links created%n", count);
    }
    
    /** Return if given coordinates are valis. */
    private boolean valid(int x, int y, char[][] chars) {
        return 0 <= y && y < chars.length && 0 <= x && x < chars[y].length;
    }
    
    //==============================================================================================
    
    /** Exception throw by {@link Compiler}. */
    @SuppressWarnings("serial")
    public static class CompileException extends Exception {
        /** Position of error, can be {@code null}. */
        public final Pos pos;
        /** Was it in header file. */
        public final boolean header;
        /** Creates new exception without position. */
        public CompileException(String message) {
            this(null, false, message);
        }
        /** Creates new exception. */
        public CompileException(Pos pos, String message) {
            this(pos, false, message);
        }
        /** Creates new exception. */
        public CompileException(Pos pos, boolean header, String message) {
            super(message);
            this.pos = pos;
            this.header = header;
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
