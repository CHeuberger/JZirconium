package cfh.zirconium;

import cfh.zirconium.Compiler.CompileException;
import cfh.zirconium.Environment.*;

public class CompilerTest {
    
    public static void main(String[] args) {
        CompilerTest test = new CompilerTest(args == null);
        test.validationTest();
    }

    private final boolean silent;
    private final Compiler compiler;
    
    private CompilerTest(boolean silent) {
        this.silent = silent;
        compiler = new Compiler(new Environment(printer, input, output, output));
    }
    
    private void validationTest() {
        int errors = 0;
        for (String code : (""
            + "0 @ .o Q O   \n"
            + "==========\n"
            + "0-0 0>0<0\n"
            + "0->0<-0\n"
            + "==========\n"
            + "0 0 0 0 0\n"
            + "| v ^ | ^\n"
            + "0 0 0 v |\n"
            + "      0 0\n"
            + "==========\n"
            + "  ()\n"
            + "==========\n"
            + "  (TEST)\n"
            + "==========\n"
            + "((A=1))\n"
            + "==========\n"
            + "@ (( ))\n"
            )
            .split("(?m)^ *===+ *\n"))
        {
            errors += valid(code);
        }
        
        if (!silent) {
            System.out.println();
        }

        for (String code : (""
            + "Y   \n"
            + "==========\n"
            + "O (\n"
            + "==========\n"
            + "0 (TEST\n"
            )
            .split("\n *===+ *\n"))
        {
            errors += invalid(code);
        }
        
        if (errors > 0) {
            System.err.printf("%n=====  %d  ERRORS  =====%n", errors);
        } else {
            System.out.println("\nOK");
        }
    }
    
    private int valid(String code) {
        try {
            compiler.compile(first(code), code, "");
            if (!silent) {
                System.out.println(first(code));
            }
            return 0;
        } catch (Exception ex) {
            ex.printStackTrace();
            System.err.printf("Unexpected %s: %s for %n%s%n",
                ex.getClass().getSimpleName(), ex.getMessage(), code);
            return 1;
        }
    }
    
    private int invalid(String code) {
        try {
            compiler.compile(first(code), code, "");
            System.err.printf("Missing Exception for %n%s%n", code);
            return 1;
        } catch (CompileException ex) {
            if (!silent) {
                System.out.println(first(code));
            }
            return 0;
        }
    }
    
    private static String first(String code) {
        int i = code.indexOf('\n');
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
            return 0;
        }
        @Override
        public int readInteger() {
            return 0;
        }
    };
    private static final Output output = new Output() {
        @Override
        public void reset() {
            //
        }
        @Override
        public void write(String text) {
            //
        }
        @Override
        public void write(int b) {
            //
        }
    };
}
