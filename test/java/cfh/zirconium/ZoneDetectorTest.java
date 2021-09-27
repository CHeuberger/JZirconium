package cfh.zirconium;

import java.util.Arrays;
import java.util.stream.Collectors;

import cfh.zirconium.Compiler.CompileException;
import cfh.zirconium.Compiler.Zone;

public class ZoneDetectorTest {

    public static void main(String[] args) {
        ZoneDetectorTest test = new ZoneDetectorTest(args == null);
        test.testW();
        test.test();
        
        if (test.errors > 0) {
            System.err.printf("%n=====  ERRORS: %d  =====%n", test.errors);
        } else {
            System.out.println("\nOK");
        }
    }

    private final boolean silent;
    private int errors = 0;

    private ZoneDetectorTest(boolean silent) {
        this.silent = silent;
    }
    
    private void testW() {
        for (String code : (""
            + "A]{b\n"
            + "=======\n" 
            + "  ~ \n"
            + " {a}\n"
            + "  ~ \n"
            + "=======\n"
            + "     ~ \n"
            + " ~  {a}\n"
            + "{b}  ~ \n"
            + " ~     \n"
            + "=======\n"
            + " ~     \n"
            + "{a}  ~ \n"
            + "{a} {b}\n"
            + " ~   ~ \n"
            + "=======\n"
            + " ~~ \n"
            + "{aa}\n"
            + " ~~ \n"
            + "=======\n"
            + "    {~~~~~~~~~~~~~}\n"
            + " ~  {aaaaaaaaaaaaa}\n"
            + "{a} {a}~~~~~~~~~{a}\n"
            + "{a} {a}         {a}\n"
            + "{a} {a}  ~~~~~  {a}\n"
            + "{a} {a} {aaaaa} {a}\n"
            + "{a} {a} {a~~~a} {a}\n"
            + "{a} {a} {a} {a} {a}\n"
            + "{a} {a}  ~  {a} {a}\n"
            + "{a} {a}     {a} {a}\n"
            + "{a} {a}~~~~~{a} {a}\n"
            + "{a} {aaaaaaaaa} {a}\n"
            + "{a}  ~~~~~~~~~  {a}\n"
            + "{a}             {a}\n"
            + "{a~~~~~~~~~~~~~~~a}\n"
            + "{aaaaaaaaaaaaaaaaa}\n"
            + "{~~~~~~~~~~~~~~~~~}\n"
            ).split("\n$|\n *===+ *\n"))
        {
            Parsed parsed = parse(code);
            try {
                Zone[][] zones = new ZoneDetector(parsed.chars).detect();
                errors += parsed.check(zones);
            } catch (CompileException ex) {
                ex.printStackTrace();
                errors += 1;
            }
        }
    }
    
    private void test() {
        // expect Exception
        for (String code : (""
            + " ~~\n"
            + "{aa]\n"
            + " ~~\n"
            + "=======\n"
            + " ~~\n"
            + "[aa}\n"
            + " ~~\n"
            ).split("\n$|\n *===+ *\n"))
        {
            errors += invalid(code);
        }
        
        // no Exception expected
        for (String code : (""
            + "   ~~\n"
            + " {~xx}\n"
            + "{xxx}\n"
            + "{~~~}\n"
            + "=======\n"
            + " ~~\n"
            + "{aa}\n"
            + " {}\n"
            + "===============\n"
            + "   ~~~\n"
            + "  {bbb}\n"
            + "   ~~~\n"
            + "===============\n"
            + " ~~   ~~   ~~\n"
            + "{aa} {bb} {cc}\n"
            + "{aa}  {b} {c}\n"
            + " ~~    ~   ~\n"
            + "===============\n"
            + " ~     ~\n"
            + "{a}   {b}\n"
            + "{aa} {bb}\n"
            + " ~~   ~~\n"
            + "====================\n"
            + " ~                  \n"
            + "{a}\n"
            + "{a}    ~\n"
            + "{a}   {a}\n"
            + "{a}   {a}\n"
            + "{aa~ ~aa}\n"
            + "{aaa~aaa}\n"
            + "{aaaaaa}\n"
            + "{aaaaaa}\n"
            + " ~~~~~~\n"
            + "====================\n"
            + " {~~}\n"
            + "{xxxx}\n"
            + " {xxxx~~~~~~}\n"
            + "{xxxxxxxxxxxx}\n"
            + "{xxxxx~~xxxxx}\n"
            + " {~~~}  {~~~}\n"
            ).split("\n$|\n *===+ *\n"))
      {
          errors += exclusionZone(code);
      }
    }
    
    private int invalid(String code) {
        Parsed parsed = parse(code);
        
        try {
            new ZoneDetector(parsed.chars).detect();
            System.err.printf("Missing exception for %n\"%s\"%n", code);
            return 1;
        } catch (CompileException expected) {
            if (!silent) {
                System.out.println(expected);
            }
            return 0;
        }
    }
    
    private int exclusionZone(String code) {
        Parsed parsed = parse(code);
        
        Zone[][] zones;
        try {
            zones = new ZoneDetector(parsed.chars).detect();
        } catch (CompileException ex) {
            System.err.printf("Unexpected %s: %s for %n%s%n",
                ex.getClass().getSimpleName(), ex.getMessage(), code);
            return 1;
        }
        return parsed.check(zones);
    }
    
    private Parsed parse(String code) {
        String[] lines = code.split("\n");
        int rows = lines.length;
        int cols = Arrays.stream(lines).mapToInt(String::length).max().orElse(0);
        
        char[][] chars = new char[rows][cols];
        Zone[][] expected = new Zone[rows][cols];
        for (int y = 0; y < rows; y++) {
            String line = lines[y];
            Arrays.fill(chars[y], ' ');
            Arrays.fill(expected[y], Zone.NONE);
            for (int x = 0; x < line.length(); x++) {
                char ch = line.charAt(x);
                if ('a' <= ch && ch <= 'z') {
                    expected[y][x] = Zone.EXCLUSION;
                } else if ('A' <= ch && ch <= 'Z') {
                    expected[y][x] = Zone.METROPOLIS;
                } else {
                    expected[y][x] = Zone.NONE;
                    chars[y][x] = ch;
                }
            }
        }
        return new Parsed(rows, cols, chars, expected);
    }
    
    //----------------------------------------------------------------------------------------------
    
    class Parsed {
        private final int rows;
        private final int cols;
        private final char[][] chars;
        private final Zone[][] expected;
        
        Parsed(int rows, int cols, char[][] chars, Zone[][] expected) {
            this.rows = rows;
            this.cols = cols;
            this.chars = chars;
            this.expected = expected;
        }
        
        int check(Zone[][] zones) {
            for (int y = 0; y < rows; y++) {
                for (int x = 0; x < cols; x++) {
                    if (zones[y][x] != expected[y][x]) {
                            System.err.printf("expected %s at [%d,%d]: %s%n", expected[y][x], x, y, zones[y][x]);
                            return 1;
                    }
                }
            }
            return 0;
        }
    }
}
