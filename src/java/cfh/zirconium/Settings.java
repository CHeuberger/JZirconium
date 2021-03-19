package cfh.zirconium;

import java.awt.Font;

public class Settings {

    private static final String FONT_NAME = "DejaVu Sans Mono";
    private static final int FONT_SIZE = 16;
    
    
    private static final Settings instance = new Settings();
    
    public static Settings instance() {
        return instance;
    }
    
    //==============================================================================================
    
    private Settings() {
        //
    }
    
    // GUI
    public Font codeFont() { return new Font(FONT_NAME, Font.PLAIN, FONT_SIZE); }
    
    // help
    public Font helpFont() { return new Font(FONT_NAME, Font.PLAIN, FONT_SIZE); }
}
