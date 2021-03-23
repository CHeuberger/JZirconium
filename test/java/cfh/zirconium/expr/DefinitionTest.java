package cfh.zirconium.expr;

import java.util.regex.Pattern;

import cfh.zirconium.Compiler.CompileException;
import cfh.zirconium.net.Pos;

public class DefinitionTest {

    /** Just for testing. */
    public static void main(String[] args) throws Exception {
        var errors = 0;
        for (var text : """
            a = 2
            a = 12 
            a = N
            a=K
            a = 1 2 3 + +
            a=NK1==
            a=1 2 +
            a=1 2 -
            a=1 2 *
            a=1 2 /
            a=1 2 =
            a  =  N  K  1  =  =  
            """.split("\n"))
        {
            errors += valid(text);
        }
        
        System.out.println();
        for (var text : """
            
            a
            a=
            a=1 +
            a=1 2
            a=1 2 3 +
            \f=N
            """.split("\n"))
        {
            errors += invalid(text);
        }
        
        System.out.println();
        var pattern = Pattern.compile("""
            (?x)
            ([^(]*)       # expression
            \\(\\s*       # (
               (\\d+)     # N
               \\s*,\\s*  # ,
               (\\d+)     # K
            \\s*\\)       # )
            \\s*:\\s*     # :
            (\\d+)        # expected
            \\s*
            """);
        // expr (n, k) = expected
        for (var test : """
            a = 1 (3, 4) : 1
            b = N (2, 2) : 2
            c = K (2, 3) : 3
            d = 2 3 + (0, 0) : 5
            e = 4 3 - (0, 0) : 1
            f = 3 5 * (0, 0) : 15
            g = 7 3 / (0, 0) : 2
            h = 8 0 / (0, 0) : 0
            i = 0 2 / (0, 0) : 0
            j = N 2 = (2, 3) : 1
            k = K 2 = (2, 3) : 0
            l = N K + 10 * (3, 4) : 70
            """.split("\n"))
        {
            var matcher = pattern.matcher(test);
            if (!matcher.matches()) {
                throw new IllegalArgumentException(test);
            }
            var expr = matcher.group(1);
            var n = Integer.parseInt(matcher.group(2));
            var k = Integer.parseInt(matcher.group(3));
            var expected = Integer.parseInt(matcher.group(4));
            errors += calculate(expr, n, k, expected);
        }
        
        if (errors > 0) {
            System.err.printf("%n=====  %d  ERRORS  =====%n", errors);
        } else {
            System.out.println("OK");
        }
    }

    private static int valid(String expr) {
        try {
            System.out.println(Definition.parse(new Pos(0,0), expr));
            return 0;
        } catch (Exception ex) {
            System.err.printf("Unexpected for \"%s\", %s: %s%n",
                expr, ex.getClass().getSimpleName(), ex.getMessage());
            return 1;
        }
    }
    
    private static int invalid(String expr) {
        try {
            var def = Definition.parse(new Pos(0, 0), expr);
            System.err.printf("Missing Exception for \"%s\" = %s%n", expr, def);
            return 1;
        } catch (CompileException ex) {
            System.out.println(ex.getMessage());
            return 0;
        }
    }
    
    private static int calculate(String expr, int n, int k, int expected) {
        try {
            var def = Definition.parse(new Pos(0, 0), expr);
            var result = def.calculate(n, k);
            if (result != expected) {
                System.err.printf("calculated %d, expected %d, for \"%s\" with n=%d, k=%d%n", 
                    result, expected, expr, n, k);
                return 1;
            } else {
                return 0;
            }
        } catch (CompileException ex) {
            System.err.printf("Unexpected for \"%s\", %s: %s%n",
                expr, ex.getClass().getSimpleName(), ex.getMessage());
            return 1;
        }        
    }
}
