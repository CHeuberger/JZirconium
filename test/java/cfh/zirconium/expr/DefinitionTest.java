package cfh.zirconium.expr;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import cfh.zirconium.Compiler.CompileException;
import cfh.zirconium.net.Pos;

public class DefinitionTest {

    /** Just for testing. */
    public static void main(String[] args) {
        DefinitionTest test = new DefinitionTest(args == null);
        test.test();
    }
    
    private final boolean silent;
    
    private DefinitionTest(boolean silent) {
        this.silent = silent;
    }
    
    private void test() {    
        int errors = 0;
        for (String test : (""
            + "\n"
            + "a = 2\n"
            + "a = 12 \n"
            + "a = N\n"
            + "a=K\n"
            + "a = 1 2 3 + +\n"
            + "a=NK1==\n"
            + "a=1 2 +\n"
            + "a=1 2 -\n"
            + "a=1 2 *\n"
            + "a=1 2 /\n"
            + "a=1 2 =\n"
            + "a  =  N  K  1  =  =  \n"
            ).split("\n"))
        {
            errors += valid(test);
        }
        
        System.out.println();
        for (String test : (""
            + "a\n"
            + "a=\n"
            + "a=1 +\n"
            + "a=1 2\n"
            + "a=1 2 3 +\n"
            + "\f=N\n"
            ).split("\n"))
        {
            errors += invalid(test);
        }
        
        if (!silent) {
            System.out.println();
        }
        Pattern pattern = Pattern.compile(""
            + "(?x)\n"
            + "([^(]*)       # expression\n"
            + "\\(\\s*       # (\n"
            + "   (\\d+)     # N\n"
            + "   \\s*,\\s*  # ,\n"
            + "   (\\d+)     # K\n"
            + "\\s*\\)       # )\n"
            + "\\s*:\\s*     # :\n"
            + "(\\d+)        # expected\n"
            + "\\s*\n"
            );
        // expr (n, k) = expected
        for (String test : (""
            + "a = 1 (3, 4) : 1\n"
            + "b = N (2, 2) : 2\n"
            + "c = K (2, 3) : 3\n"
            + "d = 2 3 + (0, 0) : 5\n"
            + "e = 4 3 - (0, 0) : 1\n"
            + "f = 3 5 * (0, 0) : 15\n"
            + "g = 7 3 / (0, 0) : 2\n"
            + "h = 8 0 / (0, 0) : 0\n"
            + "i = 0 2 / (0, 0) : 0\n"
            + "j = N 2 = (2, 3) : 1\n"
            + "k = K 2 = (2, 3) : 0\n"
            + "l = N K + 10 * (3, 4) : 70\n"
            ).split("\n"))
        {
            Matcher matcher = pattern.matcher(test);
            if (!matcher.matches()) {
                throw new IllegalArgumentException(test);
            }
            String expr = matcher.group(1);
            int n = Integer.parseInt(matcher.group(2));
            int k = Integer.parseInt(matcher.group(3));
            int expected = Integer.parseInt(matcher.group(4));
            errors += calculate(expr, n, k, expected);
        }
        
        if (errors > 0) {
            System.err.printf("%n=====  %d  ERRORS  =====%n", errors);
        } else {
            System.out.println("OK");
        }
    }

    private int valid(String expr) {
        try {
            if (!silent) {
                System.out.println(Definition.parse(new Pos(0,0), expr));
            }
            return 0;
        } catch (Exception ex) {
            System.err.printf("Unexpected for \"%s\", %s: %s%n",
                expr, ex.getClass().getSimpleName(), ex.getMessage());
            return 1;
        }
    }
    
    private int invalid(String expr) {
        try {
            Definition def = Definition.parse(new Pos(0, 0), expr);
            System.err.printf("Missing Exception for \"%s\" = %s%n", expr, def);
            return 1;
        } catch (CompileException ex) {
            if (!silent) {
                System.out.println(ex.getMessage());
            }
            return 0;
        }
    }
    
    private int calculate(String expr, int n, int k, int expected) {
        try {
            Definition def = Definition.parse(new Pos(0, 0), expr);
            int result = def.calculate(n, k);
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
