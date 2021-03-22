package cfh.zirconium.expr;

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
        if (errors > 0) {
            System.err.printf("%n=====  %d  ERRORS  =====%n", errors);
        }
    }

    private static int valid(String text) {
        try {
            System.out.println(Definition.parse(new Pos(0,0), text));
            return 0;
        } catch (Exception ex) {
            System.err.printf("Unexpected for \"%s\", %s: %s%n",
                text, ex.getClass().getSimpleName(), ex.getMessage());
            return 1;
        }
    }
    
    private static int invalid(String text) {
        try {
            var def = Definition.parse(new Pos(0, 0), text);
            System.err.printf("Missing Exception for \"%s\" = %s%n", text, def);
            return 1;
        } catch (CompileException ex) {
            System.out.println(ex.getMessage());
            return 0;
        }
    }
}
