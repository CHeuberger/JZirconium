package cfh.zirconium;

import java.awt.Font;

/** Global settings. */
public class Settings {

    private static final String FONT_NAME = "DejaVu Sans Mono";
    private static final int FONT_SIZE = 16;
    
    
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
    /** Font for code pane. */
    public Font codeFont() { return new Font(FONT_NAME, Font.PLAIN, FONT_SIZE); }
    
    // help
    /** Font for help pane. */
    public Font helpFont() { return new Font(FONT_NAME, Font.PLAIN, FONT_SIZE); }
}
