package cfh.zirconium;

import static java.util.Objects.*;

/** Environment for program execution. */
public class Environment {

    private final Printer printer;
    private final Input input;
    private final Output output;
    private final Output error;
    
    private boolean halted = false;
    
    public Environment(Printer printer, Input input, Output output, Output error) {
        this.printer = requireNonNull(printer);
        this.input = requireNonNull(input);
        this.output = requireNonNull(output);
        this.error = requireNonNull(error);
    }
    
    public Printer printer() { return printer; }
    public Input input() { return input; }
    public Output output() { return output; }
    public Output error() { return error; }
    public boolean halted() { return halted; }
    
    public void reset() {
        halted = false;
        input.reset();
        output.reset();
        error.reset();
    }
    
    public void start() {
        print("start%n");
        halted = false;
    }
    
    public void halt() {
        print("halt%n");
        halted = true;
    }
    
    public void print(String format, Object... args) {
        printer.print(format, args);
    }
    
    public void output(String text) {
        output.write(text);
    }
    
    //==============================================================================================
    
    public interface Printer {
        /** Print a formatted message to the log pane (see {@link String#format}). */
        public void print(String format, Object... args);
    }
    
    public interface Input {
        
        /** Resets the input. */
        public void reset();
        
        /** Read one byte. */
        public int readByte();
        
        /** Read integer. */
        public int readInteger();
    }
    
    public interface Output {
        
        /** Resets the output. */
        public void reset();

        /** Write one byte. */
        public void write(int b);
        
        /** Write text to output. */
        public void write(String text);
    }
}
