package cfh.zirconium.gui;

import static java.util.stream.Collectors.*;
import static javax.swing.JOptionPane.*;
import static cfh.zirconium.Compiler.*;

import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.util.Collections;
import java.util.Map;
import java.util.function.Consumer;
import java.util.prefs.Preferences;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JEditorPane;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.text.BadLocationException;

import cfh.zirconium.Compiler;
import cfh.zirconium.Compiler.CompileException;
import cfh.zirconium.net.Program;

public class Main {

    private static final String VERSION = "0.0";
    private static final String TITLE = "JZirconium v" + VERSION;
    
    private static final Font MONOSPACED = new Font("DejaVu Sans Mono", Font.PLAIN, 14);
    
    public static void main(String... args) {
        SwingUtilities.invokeLater(Main::new);
    }
    
    private static final String PREF_FILE = "zirconium.file";

    private final Preferences PREFS = Preferences.userNodeForPackage(getClass());
    
    private static final Map<Character, String> HTML_ESC = Collections.unmodifiableMap(
        Pattern.compile(" ").splitAsStream("<&lt; >&gt;").collect(toMap(s -> s.charAt(0), s -> s.substring(1))));
    
    private final JFrame frame;
    private final JTextArea codePane;
    private final JTextArea logPane;
    
    private Program program = null;
    private boolean changed = false;
    
    private Main() {
        var open = new JMenuItem(newAction("Open", this::doOpen, "Open a new file"));
        var save = new JMenuItem(newAction("Save", this::doSave, "Save code to file"));
        var clearLog = new JMenuItem(newAction("Clear", this::doClearLog, "Clear log"));
        var quit = new JMenuItem(newAction("Quit", this::doQuit, "Quits the program"));
        
        var fileMenu = new JMenu("File");
        fileMenu.add(open);
        fileMenu.add(save);
        fileMenu.addSeparator();
        fileMenu.add(clearLog);
        fileMenu.addSeparator();
        fileMenu.add(quit);
        
        var compile = new JMenuItem(newAction("Compile", this::doCompile, "Compile current code"));
        
        var runMenu = new JMenu("Run");
        runMenu.add(compile);
        
        var help = new JMenuItem(newAction("Help", this::doHelp, "Show help"));
        
        var helpMenu = new JMenu("Help");
        helpMenu.add(help);
        
        var menubar = new JMenuBar();
        menubar.add(fileMenu);
        menubar.add(runMenu);
        menubar.add(helpMenu);

        codePane = newTextArea();
        codePane.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void removeUpdate(DocumentEvent e) {
                changed = true;
            }
            @Override
            public void insertUpdate(DocumentEvent e) {
                changed = true;
            }
            @Override
            public void changedUpdate(DocumentEvent e) {
                changed = true;
            }
        });
        
        logPane = newTextArea();
        logPane.setEditable(false);
        
        var mainSplit = new JSplitPane();
        mainSplit.setOrientation(mainSplit.VERTICAL_SPLIT);
        mainSplit.setTopComponent(newScrollPane(codePane));
        mainSplit.setBottomComponent(newScrollPane(logPane));
        mainSplit.setDividerLocation(500);
        
        frame = new JFrame();
        frame.setDefaultCloseOperation(frame.DO_NOTHING_ON_CLOSE);
        frame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                doQuit(null);
            }
        });
        frame.setFont(new Font("monospaced", Font.PLAIN, 14));
        frame.setJMenuBar(menubar);
        frame.setTitle(TITLE);
        frame.add(mainSplit);
        frame.setSize(1000, 900);
        frame.validate();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }
    
    private void doHelp(ActionEvent ev) {
        
//        var pane = new JEditorPane("text/html", String.format(
//            """
//            <HTML><BODY>
//            <H1><CENTER>Zirconium</CENTER></H1>
//            <H2>Stations</H2>
//            <TABLE>
//            <TR><TH><TT>%s</TT><TD>If this station is not occupied, dispatch one drone to each linked station.
//            <TR><TH><TT>%s</TT><TD>If this is occupied by any amount of drones, dispatch one drone to each linked station.
//            <TR><TH><TT>%s</TT><TD>Dispatch N // K drones to each linked station, where N is the number of drones occupying this station, K is the number of linked stations
//            <TR><TH><TT>%s</TT><TD>If this station is occupied by N drones, dispatch N - 1 drones to linked stations.
//            <TR><TH><TT>%s</TT><TD>Dispatch the number of drones occupying this station to each linked station.
//            <TR><TH><TT>%s</TT><TD>Do not dispatch any drones.
//            </TABLE>
//            <H2>Tunnels</H2>
//            <TABLE>
//            <TR><TH><TT>%s%s%s%s</TT><TD>horizontal, vertical or diagonals.
//            <TR><TH><TT>%s</TT><TD>both horizontal and vertical.combination: horizontal and vertical, diagonals, any.
//            <TR><TH><TT>%s</TT><TD>both diagonals.
//            <TR><TH><TT>%s</TT><TD>any direction.
//            </TABLE>
//            <H2>Apertures</H2>
//            <TABLE>
//            <TR><TH><TT>%s%s%s%s</TT><TD>north, east, south, west.
//            <TR><TH><TT>%s</TT><TD>diagonals.
//            </TABLE>
//            """,
//            escape(CREATE), 
//            escape(DOT),
//            escape('O'),
//            escape('Q'), 
//            escape('o'), 
//            escape(NOP), 
//            
//            escape(HORZ), escape(VERT), escape(DIAG_U), escape(DIAG_D),
//            escape(CROSS_HV), escape(CROSS_DD), escape(CROSS_ALL),
//            escape(APERT_N), escape(APERT_E), escape(APERT_S), escape(APERT_W),
//            escape(APERT_DIAG)
//            ));
        
        var pane = new JTextArea("""
            
                        Z  I  R  C  O  N  I  U  M
                       =========================== 
                        
            STATIONS
            --------
            0  Do not dispatch any drones.
            @  If this station is not occupied, dispatch one drone to each linked station.
            .  If this is occupied by any amount of drones, dispatch one drone to each linked station.
            o  Dispatch the number of drones occupying this station to each linked station.
            Q  If this station is occupied by N drones, dispatch N - 1 drones to linked stations.
            O  Dispatch N // K drones to each linked station, where N is the number of drones 
               occupying this station, K is the number of linked stations
            
            TUNNELS
            -------
            -|/\\  horizontal, vertical or diagonals.
              +   both horizontal and vertical.combination: horizontal and vertical, diagonals, any.
              X   both diagonals.
              *   any direction.
              
            APERTURES
            ---------
            ^>v<  north, east, south, west.
              #   diagonals.
            """);
        pane.setBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4));
        pane.setEditable(false);
        pane.setFont(MONOSPACED);
        showMessageDialog(frame, newScrollPane(pane));
    }
    
    private static String escape(char ch) {
        return HTML_ESC.getOrDefault(ch, Character.toString(ch));
    }
    
    private void doOpen(ActionEvent ev) {
        if (changed && showConfirmDialog(frame, "Code changed, overwrite?", "Confirm Open", OK_CANCEL_OPTION) != OK_OPTION) {
            return;
        }
        var file = new File(PREFS.get(PREF_FILE, "."));
        var chooser = new JFileChooser();
        chooser.setAcceptAllFileFilterUsed(true);
        chooser.setFileFilter(new FileNameExtensionFilter("Zirconium", "zc", "zch"));
        chooser.setFileSelectionMode(chooser.FILES_ONLY);
        chooser.setMultiSelectionEnabled(false);
        chooser.setSelectedFile(file);
        if (chooser.showOpenDialog(frame) != chooser.APPROVE_OPTION) {
            return;
        }
        file = chooser.getSelectedFile();
        PREFS.put(PREF_FILE, file.getAbsolutePath());
        try {
            var code = Files.lines(file.toPath()).collect(Collectors.joining("\n"));
            codePane.setText(code);
        } catch (IOException ex) {
            error(ex, "opening \"%s\"", file);
            return;
        }
        changed = false;
        program = null;
        frame.setTitle(TITLE + " - " + file.getName());
        print("%nLoaded %s%n", file.getAbsolutePath());
    }
    
    private void doSave(ActionEvent ev) {
        var file = new File(PREFS.get(PREF_FILE, "."));
        var chooser = new JFileChooser();
        chooser.setAcceptAllFileFilterUsed(true);
        chooser.setFileFilter(new FileNameExtensionFilter("Zirconium", "zc", "zch"));
        chooser.setFileSelectionMode(chooser.FILES_ONLY);
        chooser.setMultiSelectionEnabled(false);
        chooser.setSelectedFile(file);
        if (chooser.showSaveDialog(frame) != chooser.APPROVE_OPTION) {
            return;
        }
        file = chooser.getSelectedFile();
        if (file.getName().indexOf('.') == -1) {
            file = new File(file.getParentFile(), file.getName() + ".zc");
        }
        PREFS.put(PREF_FILE, file.getAbsolutePath());
        if (file.exists()) {
            if (showConfirmDialog(frame, "File already exists, overwrite?", "Confirm Save", OK_CANCEL_OPTION) != OK_OPTION) {
                return;
            }
            var name = file.getName();
            var index = name.lastIndexOf('.');
            if (index != -1) {
                name = name.substring(0, index);
            }
            var bak = new File(file.getParentFile(), name + ".bak");
            if (bak.exists()) {
                bak.delete();
            }
            file.renameTo(bak);
        }
        try {
            Files.writeString(file.toPath(), codePane.getText(), StandardOpenOption.CREATE_NEW);
        } catch (IOException ex) {
            error(ex, "saving \"%s\"", file);
            return;
        }
        changed = false;
        frame.setTitle(TITLE + " - " + file.getName());
        print("%nSaved  %s%n", file.getAbsoluteFile());
    }
    
    private void doClearLog(ActionEvent ev) {
        logPane.setText("");
    }
    
    private void doQuit(ActionEvent ev) {
        if (changed && showConfirmDialog(frame, "Code changed, quit anyway?", "Quit?", OK_CANCEL_OPTION) != OK_OPTION) {
            return;
        }
        frame.dispose();
    }
    
    private void doCompile(ActionEvent ev) {
        try {
            program = new Compiler(this::print).compile(codePane.getText());
        } catch (CompileException ex) {
            if (ex.pos != null) {
                try {
                    var ls = codePane.getLineStartOffset(ex.pos.y()-1);
                    var le = codePane.getLineEndOffset(ex.pos.y()-1);
                    var index = ls + ex.pos.x() - 1;
                    if (index >= le) {
                        index = le - 1;
                    }
                    codePane.setCaretPosition(index);
                    if (index < le-1) {
                        codePane.select(index, index+1);
                    }
                } catch (BadLocationException ex1) {
                    ex1.printStackTrace();
                }
            }
            error(ex, "compiling at %s", ex.pos);
        }
    }
    
    private Action newAction(String name, Consumer<ActionEvent> runable, String tooltip) {
        @SuppressWarnings("serial")
        var action = new AbstractAction(name) {
            {
                putValue(SHORT_DESCRIPTION, tooltip);
            }
            @Override
            public void actionPerformed(ActionEvent ev) {
                runable.accept(ev);
            }
        };
        return action;
    }
    
    private JTextArea newTextArea() {
        var pane = new JTextArea();
        pane.setBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4));
        pane.setFont(MONOSPACED);
        return pane;
    }
    
    private JScrollPane newScrollPane(JComponent view) {
        return new JScrollPane(view);
    }
    
    private void error(Throwable ex, String format, Object... args) {
        var msg = String.format(format, args);
        System.err.print(msg);
        ex.printStackTrace();
        print("%n%s%n%s", ex, msg);
        showMessageDialog(frame, new Object[] {ex.toString(), msg}, ex.getClass().getSimpleName(), ERROR_MESSAGE);
    }
    
    private void print(String format, Object... args) {
        var atEnd = logPane.getCaretPosition() == logPane.getText().length();
        logPane.append(String.format(format, args));
        if (atEnd) {
            logPane.setCaretPosition(logPane.getText().length());
        }
    }
    
    //====================================================================================================
    
    public interface Printer {
        public void print(String format, Object... args);
    }
}
