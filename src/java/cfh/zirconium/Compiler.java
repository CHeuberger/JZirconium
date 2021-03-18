package cfh.zirconium;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

import cfh.zirconium.gui.Main.Printer;
import cfh.zirconium.net.CreateStation;
import cfh.zirconium.net.DotStation;
import cfh.zirconium.net.NopStation;
import cfh.zirconium.net.Program;
import cfh.zirconium.net.Station;

public class Compiler {

    private static final char NOP = '0';
    private static final char CREATE = '@';
    private static final char DOT = '.';
    
    private final Printer printer;
    
    public Compiler(Printer printer) {
        this.printer = Objects.requireNonNull(printer);
    }
    
    public Program compile(String code) {
        printer.print("%n");
        
        char[][] chars = parse(code);
        var rows = chars.length;
        var cols = rows==0 ? 0 : chars[0].length;
        
        var stations = scan(chars);
        return null;
    }
    
    private char[][] parse(String code) {
        var lines = code.split("\n");
        var rows = lines.length;
        printer.print("%d rows%n", rows);
        
        var cols = Arrays.stream(lines).mapToInt(String::length).max().orElse(0);
        printer.print("%d columns%n", cols);
        
        var chars = new char[rows][cols];
        for (var i = 0; i < rows; i++) {
            var line = lines[i];
            var row = chars[i];
            Arrays.fill(row, ' ');
            for (var j = 0; j < line.length(); j++) {
                row[j] = line.charAt(j);
            }
        }
        return chars;
    }
    
    private Set<Station> scan(char[][] chars) {
        var set = new HashSet<Station>();
        for (var i = 0; i < chars.length; i++) {
            var row = chars[i];
            for (var j = 0; j < row.length; j++) {
                var ch = row[j];
                switch (ch) {
                    case ' ': break;
                    case NOP: set.add(new NopStation(i, j)); break;
                    case CREATE: set.add(new CreateStation(i, j)); break;
                    case DOT: set.add(new DotStation(i, j)); break;
                    default: // TODO throw
                        printer.print("%d x %x : %s%n", i, j, ch);
                }
            }
        }
        printer.print("scaned %d stations%n", set.size());
        return set;
    }
}
