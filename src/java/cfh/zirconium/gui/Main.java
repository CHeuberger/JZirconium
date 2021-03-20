package cfh.zirconium.gui;

import static javax.swing.JOptionPane.*;

import java.awt.BorderLayout;
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
import javax.swing.Box;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.event.CaretEvent;
import javax.swing.event.CaretListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.text.BadLocationException;

import cfh.zirconium.Compiler;
import cfh.zirconium.Program;
import cfh.zirconium.Compiler.CompileException;
import cfh.zirconium.Settings;

/** Main for GUI. */
public class Main {

    public static final String VERSION = "0.01";
    private static final String TITLE = "JZirconium v" + VERSION;
    
    public static void main(String... args) {
        SwingUtilities.invokeLater(Main::new);
    }
    
    private static final String PREF_NAME = "zirconium.name";
    private static final String PREF_CODE = "zirconium.code";
    private static final String PREF_FILE = "zirconium.file";
    
    //----------------------------------------------------------------------------------------------
    
    private final Settings settings = Settings.instance();
    
    private final Preferences PREFS = Preferences.userNodeForPackage(getClass());
    
    private final JFrame frame;
    private final JTextArea codePane;
    private final JTextArea logPane;
    private final JTextField statusName;
    private final JTextField statusRow;
    private final JTextField statusCol;
    
    // TODO station list
    // TODO add/delete column
    // TODO show graph
    // TODO invalidate program on edit
    // TODO undo
    // TODO include files
    
    private final Action runAction;
    private final Action stepAction;
    
    private String name = "";
    private Program program = null;
    private boolean changed = false;
    
    /** Builds and shows GUI. */
    private Main() {
        var open = newAction("Open", this::doOpen, "Open a new file");
        var save = newAction("Save", this::doSave, "Save code to file");
        var clearLog = newAction("Clear", this::doClearLog, "Clear log");
        var quit =newAction("Quit", this::doQuit, "Quits the program");
        
        var fileMenu = new JMenu("File");
        fileMenu.add(newMenuItem(open));
        fileMenu.add(newMenuItem(save));
        fileMenu.addSeparator();
        fileMenu.add(newMenuItem(clearLog));
        fileMenu.addSeparator();
        fileMenu.add(newMenuItem(quit));
        
        
        var compile =newAction("Compile", this::doCompile, "Compile current code");
        runAction = newAction("Run", this::doRun, "Run the program");
        stepAction = newAction("Step", this::doStep, "Execute one step is program already started; otherwise it is started but stopped at first tick");
        
        var runMenu = new JMenu("Run");
        runMenu.add( newMenuItem(compile));
        runMenu.addSeparator();
        runMenu.add(newMenuItem(runAction));
        runMenu.add(newMenuItem(stepAction));
        
        var help = newAction("Help", this::doHelp, "Show help");
        
        var helpMenu = new JMenu("Help");
        helpMenu.add(newMenuItem(help));
        
        var menubar = new JMenuBar();
        menubar.add(fileMenu);
        menubar.add(runMenu);
        menubar.add(helpMenu);

        codePane = newTextArea();
        codePane.setFont(settings.codeFont());
        codePane.setText(PREFS.get(PREF_CODE, ""));
        codePane.addCaretListener(new CaretListener() {
            @Override
            public void caretUpdate(CaretEvent e) {
                try {
                    var dot = e.getDot();
                    var line = codePane.getLineOfOffset(dot);
                    var col = dot - codePane.getLineStartOffset(line);
                    statusCol.setText(Integer.toString(col+1));
                    statusRow.setText(Integer.toString(line+1));
                } catch (BadLocationException ex) {
                    ex.printStackTrace();
                }
                
            }
        });
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
        
        statusName = newTextField(30, "Name");
        statusName.setText(PREFS.get(PREF_NAME, ""));
        
        statusRow = newTextField(5, "Row");
        
        statusCol = newTextField(5, "Column");
        
        var statusLine = Box.createHorizontalBox();
        statusLine.setBorder(BorderFactory.createEmptyBorder(2, 4, 4, 4));
        statusLine.add(statusName);
        statusLine.add(Box.createHorizontalGlue());
        statusLine.add(Box.createHorizontalStrut(10));
        statusLine.add(statusRow);
        statusLine.add(statusCol);
        
        var mainSplit = new JSplitPane();
        mainSplit.setOrientation(mainSplit.VERTICAL_SPLIT);
        mainSplit.setTopComponent(newScrollPane(codePane));
        mainSplit.setBottomComponent(newScrollPane(logPane));
        mainSplit.setDividerLocation(500);
        
        frame = new JFrame();
        frame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosed(WindowEvent e) {
                PREFS.put(PREF_CODE, codePane.getText());
            }
        });
        frame.setDefaultCloseOperation(frame.DO_NOTHING_ON_CLOSE);
        frame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                doQuit(null);
            }
        });
        frame.setJMenuBar(menubar);
        frame.setTitle(TITLE);
        frame.setLayout(new BorderLayout());
        frame.add(mainSplit, BorderLayout.CENTER);
        frame.add(statusLine, BorderLayout.PAGE_END);
        frame.setSize(1000, 900);
        frame.validate();
        frame.setLocationRelativeTo(null);
        
        codePane.setCaretPosition(0);
        update();
        frame.setVisible(true);
    }
    
    /** Shows help dialog. */
    private void doHelp(ActionEvent ev) {
        showMessageDialog(frame, newScrollPane(new Help().pane()), "Help", OK_OPTION);
    }
    
    /** Read program from file. */
    private void doOpen(ActionEvent ev) {
        if (changed && showConfirmDialog(frame, "Code changed, overwrite?", "Confirm Open", OK_CANCEL_OPTION) != OK_OPTION) {
            return;
        }
        var file = new File(PREFS.get(PREF_FILE, "."));
        var chooser = new JFileChooser();
        chooser.setAcceptAllFileFilterUsed(true);
        chooser.setFileFilter(new FileNameExtensionFilter("Zirconium Source", "zc", "zch"));
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
        setName(file.getName());
        setProgram(null);
        print("%nLoaded %s%n", file.getAbsolutePath());
        frame.repaint();
    }
    
    /** Save program to file. */
    private void doSave(ActionEvent ev) {
        var file = new File(PREFS.get(PREF_FILE, "."));
        var chooser = new JFileChooser();
        chooser.setAcceptAllFileFilterUsed(true);
        chooser.setFileFilter(new FileNameExtensionFilter("Zirconium Source", "zc", "zch"));
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
        setName(file.getName());
        print("%nSaved  %s%n", file.getAbsoluteFile());
        frame.repaint();
    }
    
    /** Clears log test. */
    private void doClearLog(ActionEvent ev) {
        logPane.setText("");
    }
    
    /** Quits the application. */
    private void doQuit(ActionEvent ev) {
        if (changed && showConfirmDialog(frame, "Code changed, quit anyway?", "Quit?", OK_CANCEL_OPTION) != OK_OPTION) {
            return;
        }
        frame.dispose();
    }
    
    /** Compiles the program. */
    private void doCompile(ActionEvent ev) {
        // thread
        try {
            setProgram(new Compiler(this::print).compile(name, codePane.getText()));
        } catch (CompileException ex) {
            setProgram(null);
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
    
    /** Executes the program. */
    private void doRun(ActionEvent ev) {
        // TODO run, SwingWorker?
    }
    
    /** Step the program. */
    private void doStep(ActionEvent ev) {
        if (program != null) {
            program.step();
        }
    }
    
    /** Sets a new program and updates GUI (actions). */
    private void setProgram(Program program) {
        this.program = program;
        update();
    }
    
    /** Sets the name and update GUI. */
    private void setName(String name) {
        this.name = name;
        frame.setTitle(TITLE + " - " + name);
        statusName.setText(name);
        PREFS.put(PREF_NAME, name);
    }
    
    /** Updates GUI (actions). */
    private void update() {
        boolean runable = program != null;
        runAction.setEnabled(runable);
        stepAction.setEnabled(runable);
    }
    
    /** Creates new Action. */
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
    
    /** Creates a JMenuItem. */
    private JMenuItem newMenuItem(Action action) {
        return new JMenuItem(action);
    }
    
    /** Creates a JTextArea. */
    private JTextArea newTextArea() {
        var pane = new JTextArea();
        pane.setBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4));
        pane.setFont(settings.mainFont());
        return pane;
    }
    
    /** Create a JTextField */
    private JTextField newTextField(int columns, String tooltip) {
        var field = new JTextField(columns);
        field.setEditable(false);
        field.setFont(settings.mainFont());
        field.setMaximumSize(field.getPreferredSize());
        field.setHorizontalAlignment(field.CENTER);
        field.setToolTipText(tooltip);
        return field;
    }
    
    /** Creates a JScrollPane. */
    private JScrollPane newScrollPane(JComponent view) {
        return new JScrollPane(view);
    }
    
    /** Shows and prints error message, including stack trace. */
    private void error(Throwable ex, String format, Object... args) {
        var msg = String.format(format, args);
        System.err.print(msg);
        ex.printStackTrace();
        print("%n%s%n%s", ex, msg);
        showMessageDialog(frame, new Object[] {ex.toString(), msg}, ex.getClass().getSimpleName(), ERROR_MESSAGE);
    }
    
    /** prints message to log pane. */
    private void print(String format, Object... args) {
        var atEnd = logPane.getCaretPosition() == logPane.getText().length();
        logPane.append(String.format(format, args));
        if (atEnd) {
            logPane.setCaretPosition(logPane.getText().length());
        }
    }
    
    //====================================================================================================
    
    /** Used to register Main to accetp messages for the log pane. */
    public interface Printer {
        /** Print a formatted message to the log pane (see String.format). */
        public void print(String format, Object... args);
    }
}
