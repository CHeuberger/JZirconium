package cfh.zirconium;

import java.util.Arrays;

import cfh.zirconium.Compiler.CompileException;
import cfh.zirconium.Environment.*;

public class CompilerTest {
    
    public static void main(String[] args) {
        var test = new CompilerTest();
        test.validationTest();
    }

    private final Compiler compiler;
    
    private CompilerTest() {
        compiler = new Compiler(new Environment(printer, input, output));
    }
    
    private void validationTest() {
        var errors = 0;
        for (var code : """
            0 @ .o Q O   
            ==========
            0-0 0>0<0
            0->0<-0
            ==========
            0 0 0 0 0
            | v ^ | ^
            0 0 0 v |
                  0 0
            ==========
              ()
            ==========
              (TEST)
            ==========
            ((A=1))
            
            """.split("(?m)^ *===+ *\n"))
        {
            errors += valid(code);
        }
        
        System.out.println();
        for (var code : """
            Y   
            ==========
            O (
            ==========
            0 (TEST
            ==========
            @  (())
            """.split("\n *===+ *\n"))
        {
            errors += invalid(code);
        }
        
        System.out.println();
        for (var code : """
              {aaa}  
            =========
               ~~~
              {bbb}
               ~~~
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
            zones = compiler.zones("fence", '{', '~', '}', chars);
        } catch (CompileException ex) {
            System.err.printf("Unexpected %s: %s for %n%s%n",
                ex.getClass().getSimpleName(), ex.getMessage(), code);
            return 1;
        }
        Arrays.stream(zones).map(Arrays::toString).forEach(System.out::println);
        System.out.println();
        for (var y = 0; y < rows; y++) {
            for (var x = 0; x < cols; x++) {
                if (expected[y][x] == ' ' && zones[y][x] != 0) {
                    System.err.printf("should not be a zone at [%d,%d]: %d%n", y, x, zones[y][x]);
                    return 1;
                }
                if (expected[y][x] != ' ' && zones[y][x] == 0) {
                    System.err.printf("should be a zone at [%d,%d]: %d%n", y, x, zones[y][x]);
                    return 1;
                }
            }
        }
        return 0;
    }
    
    private int valid(String code) {
        try {
            compiler.compile(first(code), code);
            System.out.println(first(code));
            return 0;
        } catch (Exception ex) {
            System.err.printf("Unexpected %s: %s for %n%s%n",
                ex.getClass().getSimpleName(), ex.getMessage(), code);
            return 1;
        }
    }
    
    private int invalid(String code) {
        try {
            compiler.compile(first(code), code);
            System.err.printf("Missing Exception for %n%s%n", code);
            return 1;
        } catch (CompileException ex) {
            System.out.println(first(code));
            return 0;
        }
    }
    
    private static String first(String code) {
        var i = code.indexOf('\n');
        return i==-1 ? code : code.substring(0, i);
    }
    
    //----------------------------------------------------------------------------------------------
    
    private static final Printer printer = new Printer() {
        @Override
        public void print(String format, Object... args) {
            //
        }
    };
    private static final Input input = new Input() {
        @Override
        public void reset() {
            //
        }
        @Override
        public int readByte() {
            // TODO Auto-generated method stub
            return 0;
        };
        @Override
        public int readInteger() {
            // TODO Auto-generated method stub
            return 0;
        };
    };
    private static final Output output = new Output() {
        @Override
        public void reset() {
            //
        };
        @Override
        public void write(String text) {
            //
        }
        @Override
        public void write(int b) {
            // TODO Auto-generated method stub
            
        }
    };
}
