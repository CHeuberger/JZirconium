package cfh.zirconium;

import java.util.Arrays;
import cfh.zirconium.Compiler.CompileException;

public class ZoneDetectorTest {

    public static void main(String[] args) {
        var test = new ZoneDetectorTest();
        test.test();
        
        if (test.errors > 0) {
            System.err.printf("%n=====  %d  ERRORS  =====%n", test.errors);
        } else {
            System.out.println("\nOK");
        }
    }

    private int errors = 0;
    
    private void test0() {
        System.out.println(exclusionZone("""
            
            /
            """));
    }
    
    private void test() {
        for (var code : """
             ~
            {a}
             ~
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
      
      if (errors > 0) {
          System.err.printf("%n=====  %d  ERRORS  =====%n", errors);
      } else {
          System.out.println("\nOK");
      }
    }
    
    private int exclusionZone(String code) {
        var lines = code.split("\n");
        var rows = lines.length;
        var cols = Arrays.stream(lines).mapToInt(String::length).max().orElse(0);
        
        var chars = new char[rows+2][cols+2];
        var expected = new char[rows+2][cols+2];
        Arrays.fill(chars[0], ' ');
        Arrays.fill(expected[0], ' ');
        Arrays.fill(chars[rows+1], ' ');
        Arrays.fill(expected[rows+1], ' ');
        for (var y = 0; y < rows; y++) {
            var line = lines[y];
            var row = chars[y+1];
            Arrays.fill(row, ' ');
            Arrays.fill(expected[y+1], ' ');
            for (var x = 0; x < line.length(); x++) {
                var ch = line.charAt(x);
                if ('a' <= ch && ch <= 'z') {
                    expected[y+1][x+1] = ch;
                } else {
                    row[x+1] = ch;
                }
            }
        }
        
        int[][] zones;
        try {
            zones = new ZoneDetector("fence", '{', '~', '}', chars).detect();
        } catch (CompileException ex) {
            System.err.printf("Unexpected %s: %s for %n%s%n",
                ex.getClass().getSimpleName(), ex.getMessage(), code);
            return 1;
        }
        System.out.println();
        var map = new int['z'-'a'+1];
        for (var y = 0; y < rows+2; y++) {
            for (var x = 0; x < cols+2; x++) {
                var expect = expected[y][x];
                if (expect == ' ') {
                    if (zones[y][x] != 0) {
                        System.err.printf("should not be a zone at [%d,%d]: %d%n", x, y, zones[y][x]);
                        return 1;
                    }
                } else {
                    if (zones[y][x] == 0) {
                        System.err.printf("should be a zone at [%d,%d]: %d%n", x, y, zones[y][x]);
                        return 1;
                    }
                    var i = expect - 'a';
                    if (map[i] == 0) {
                        map[i] = zones[y][x];
                    } else {
                        if (zones[y][x] != map[i]) {
                            System.err.printf("zone missmatch at [%d,%d] %d != %d(%c)%n", x, y, zones[y][x], map[i], expect);
                        }
                    }
                }
            }
        }
        return 0;
    }
}
