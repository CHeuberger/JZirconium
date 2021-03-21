package cfh.zirconium.expr;

import cfh.zirconium.Compiler.CompileException;
import cfh.zirconium.net.Pos;

public class DefinitionTest {

    /** Just for testing. */
    public static void main(String[] args) throws Exception {
        valid("a = 2");
        valid("a = 12 ");
        valid(" a = N");
        valid("a=K");
        valid("a = 1 2 3 + +");
        valid("a=NK1==");
        valid("a=1 2 +");
        valid("a=1 2 -");
        valid("a=1 2 *");
        valid("a=1 2 /");
        valid("a=1 2 =");
        valid("  a  =  N  K  1  =  =  ");
        invalid(" ");
        invalid("a");
        invalid("a=");
        invalid("a=1 +");
        invalid("a=1 2");
        invalid("a=1 2 3 +");
        invalid("\f=N");
    }

    private static void valid(String text) {
        try {
            System.out.println(Definition.parse(new Pos(0,0), text));
        } catch (Exception ex) {
            System.err.println("Unexpected for \"" + text + "\" \" + ex.getClass().getSimpleName() + \": " + ex.getMessage());
        }
    }
    
    private static void invalid(String text) {
        try {
            System.out.println(Definition.parse(new Pos(0, 0), text));
            System.err.println("Missing Exception for \"" + text + "\"" );
        } catch (CompileException ex) {
            System.out.println(ex.getMessage());
        }
    }
}
