package cfh.zirconium.gui;

import static javax.swing.JOptionPane.*;

import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.util.function.Consumer;
import java.util.prefs.Preferences;
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

import cfh.zirconium.Compiler;
import cfh.zirconium.net.Program;

public class Main {

    private static final String VERSION = "0.0";
    private static final String TITLE = "JZirconium v" + VERSION;
    
    private static final Font MONOSPACED = new Font("DejaVu Sans Mono", Font.PLAIN, 16);
    
    public static void main(String... args) {
        SwingUtilities.invokeLater(Main::new);
    }
    
    private static final String PREF_FILE = "zirconium.file";

    private final Preferences PREFS = Preferences.userNodeForPackage(getClass());
    
    private final JFrame frame;
    private final JTextArea codePane;
    private final JTextArea logPane;
    
    private Program program = null;
    private boolean changed = false;
    
    private Main() {
        var open = new JMenuItem(newAction("Open", this::doOpen, "Open a new file"));
        var save = new JMenuItem(newAction("Save", this::doSave, "Save code to file"));
        var quit = new JMenuItem(newAction("Quit", this::doQuit, "Quits the program"));
        
        var fileMenu = new JMenu("File");
        fileMenu.add(open);
        fileMenu.add(save);
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
        var pane = new JEditorPane("text/html", """
            <HTML><BODY>
            <H1><CENTER>Zirconium</CENTER></H1>
            <H2>Stations</H2>
            <TABLE>
              <TR><TH><B>0</B><TD>Do not dispatch any drones.
              <TR><TH><B>@</B><TD>If this station is not occupied, dispatch one drone to each linked station.
              <TR><TH><B>.</B><TD>If this is occupied by any amount of drones, dispatch one drone to each linked station.
            </TABLE>
            """);
        pane.setBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4));
        pane.setEditable(false);
        pane.setFont(MONOSPACED);
        showMessageDialog(frame, newScrollPane(pane));
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
    
    private void doQuit(ActionEvent ev) {
        if (changed && showConfirmDialog(frame, "Code changed, quit anyway?", "Quit?", OK_CANCEL_OPTION) != OK_OPTION) {
            return;
        }
        frame.dispose();
    }
    
    private void doCompile(ActionEvent ev) {
        program = new Compiler(this::print).compile(codePane.getText());
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
