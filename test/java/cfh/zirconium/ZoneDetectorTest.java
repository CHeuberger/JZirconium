package cfh.zirconium;

import java.util.Arrays;
import cfh.zirconium.Compiler.CompileException;
import cfh.zirconium.Compiler.Zone;

public class ZoneDetectorTest {

    public static void main(String[] args) {
        var test = new ZoneDetectorTest(args == null);
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
        for (var code : """
            A]{b
            =======
              ~
             {a}
              ~
            =======
                 ~
             ~  {a}
            {b}  ~
             ~
            =======
             ~
            {a}  ~
            {a} {b}
             ~   ~
            =======
             ~~ 
            {aa}
             ~~
            =======
                {~~~~~~~~~~~~~}
             ~  {aaaaaaaaaaaaa}
            {a} {a}~~~~~~~~~{a}
            {a} {a}         {a}
            {a} {a}  ~~~~~  {a}
            {a} {a} {aaaaa} {a}
            {a} {a} {a~~~a} {a}
            {a} {a} {a} {a} {a}
            {a} {a}  ~  {a} {a}
            {a} {a}     {a} {a}
            {a} {a}~~~~~{a} {a}
            {a} {aaaaaaaaa} {a}
            {a}  ~~~~~~~~~  {a}
            {a}             {a}
            {a~~~~~~~~~~~~~~~a}
            {aaaaaaaaaaaaaaaaa}
            {~~~~~~~~~~~~~~~~~}
            """.split("\n$|\n *===+ *\n"))
        {
            var parsed = parse(code);
            try {
                var zones = new ZoneDetector(parsed.chars).detect();
                errors += parsed.check(zones);
            } catch (CompileException ex) {
                ex.printStackTrace();
                errors += 1;
            }
        }
    }
    
    private void test() {
        // expect Exception
        for (var code : """
             ~~
            {aa]
             ~~
            =======
             ~~
            [aa}
             ~~
            """.split("\n$|\n *===+ *\n"))
        {
            errors += invalid(code);
        }
        
        // no Exception expected
        for (var code : """
               ~~
             {~xx}
            {xxx}
            {~~~}
            =======
             ~~
            {aa}
             {}
            ===============
               ~~~
              {bbb}
               ~~~
            ===============
             ~~   ~~   ~~
            {aa} {bb} {cc}
            {aa}  {b} {c}
             ~~    ~   ~
            ===============
             ~     ~
            {a}   {b}
            {aa} {bb}
             ~~   ~~
            ====================
             ~                  
            {a}
            {a}    ~
            {a}   {a}
            {a}   {a}
            {aa~ ~aa}
            {aaa~aaa}
            {aaaaaa}
            {aaaaaa}
             ~~~~~~
            ====================
             {~~}
            {xxxx}
             {xxxx~~~~~~}
            {xxxxxxxxxxxx}
            {xxxxx~~xxxxx}
             {~~~}  {~~~}
            """.split("\n$|\n *===+ *\n"))
      {
          errors += exclusionZone(code);
      }
    }
    
    private int invalid(String code) {
        var parsed = parse(code);
        
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
        var parsed = parse(code);
        
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
        var lines = code.split("\n");
        var rows = lines.length;
        var cols = Arrays.stream(lines).mapToInt(String::length).max().orElse(0);
        
        var chars = new char[rows][cols];
        var expected = new Zone[rows][cols];
        for (var y = 0; y < rows; y++) {
            var line = lines[y];
            Arrays.fill(chars[y], ' ');
            Arrays.fill(expected[y], Zone.NONE);
            for (var x = 0; x < line.length(); x++) {
                var ch = line.charAt(x);
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
    
    record Parsed(
        int rows,
        int cols,
        char[][] chars,
        Zone[][] expected) {
        
        int check(Zone[][] zones) {
            for (var y = 0; y < rows; y++) {
                for (var x = 0; x < cols; x++) {
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
