package cfh.zirconium;

import java.awt.Font;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

/** Global settings. */
public class Settings {

    private static final String FONT_NAME = "DejaVu Sans Mono";
    private static final int FONT_SIZE = 14;
    
    
    private static final Settings instance = new Settings();
    
    /** Settings instance. */
    public static Settings instance() {
        return instance;
    }
    
    //==============================================================================================
    
    /** Creates the instance. */
    private Settings() {
        //
    }
    
    /** Charset used for input/output. */
    public Charset charset() { return StandardCharsets.ISO_8859_1; }
    
    // GUI
    /** Font for main pane components. */
    public Font mainFont() { return new Font(FONT_NAME, Font.PLAIN, FONT_SIZE); }
    /** Font for code pane. */
    public Font codeFont() { return new Font(FONT_NAME, Font.PLAIN, FONT_SIZE+4); }
    /** Separator printed after number output. */
    public String numberSeparator() { return " "; }
    
    // help
    /** Font for help pane. */
    public Font helpFont() { return new Font(FONT_NAME, Font.PLAIN, FONT_SIZE); }

}
