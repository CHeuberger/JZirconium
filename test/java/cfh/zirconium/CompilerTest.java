package cfh.zirconium;

import cfh.zirconium.Compiler.CompileException;
import cfh.zirconium.Environment.*;

public class CompilerTest {
    
    public static void main(String[] args) {
        var test = new CompilerTest();
        test.validationTest();
    }

    private final Compiler compiler;
    
    private CompilerTest() {
        compiler = new Compiler(new Environment(printer, input, output, output));
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
            ==========
            @  (())
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
            """.split("\n *===+ *\n"))
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
            System.out.println(first(code));
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
