package cfh.zirconium;

import java.awt.Font;

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
    
    // GUI
    /** Font for main pane components. */
    public Font mainFont() { return new Font(FONT_NAME, Font.PLAIN, FONT_SIZE); }
    /** Font for code pane. */
    public Font codeFont() { return new Font(FONT_NAME, Font.PLAIN, FONT_SIZE+2); }
    
    // help
    /** Font for help pane. */
    public Font helpFont() { return new Font(FONT_NAME, Font.PLAIN, FONT_SIZE); }

}
