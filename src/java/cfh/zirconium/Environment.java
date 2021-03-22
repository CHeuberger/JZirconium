package cfh.zirconium;

/** Environment for program execution. */
public record Environment(Printer printer, Input input, Output output) {
    
    public void reset() {
        input.reset();
        output.reset();
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
    }
    
    public interface Output {
        
        /** Resets the output. */
        public void reset();

        /** Write text to output. */
        public void write(String text);
    }
}
