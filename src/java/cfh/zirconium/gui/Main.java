package cfh.zirconium.gui;

import static java.nio.file.StandardCopyOption.*;
import static java.nio.file.StandardOpenOption.*;
import static javax.swing.JOptionPane.*;

import java.awt.BorderLayout;
import java.awt.Insets;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.function.Consumer;
import java.util.prefs.Preferences;
import java.util.stream.Collectors;

import javax.imageio.ImageIO;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.event.CaretEvent;
import javax.swing.event.CaretListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.text.BadLocationException;

import cfh.graph.Dot;
import cfh.zirconium.Compiler;
import cfh.zirconium.Program;
import cfh.zirconium.Compiler.CompileException;
import cfh.zirconium.Environment;
import cfh.zirconium.Environment.*;
import cfh.zirconium.Settings;
import cfh.zirconium.net.Pos;

/** Main for GUI. */
public class Main {

    public static final String VERSION = "0.03";
    private static final String TITLE = "JZirconium v" + VERSION;
    
    public static void main(String... args) {
        SwingUtilities.invokeLater(Main::new);
    }
    
    private static final String PREF_NAME = "zirconium.name";
    private static final String PREF_CODE = "zirconium.code";
    private static final String PREF_FILE = "zirconium.file";
    // TODO save window position, split location
    
    //----------------------------------------------------------------------------------------------
    
    private final Settings settings = Settings.instance();
    
    private final Preferences PREFS = Preferences.userNodeForPackage(getClass());
    
    private final JFrame frame;
    private final JTextArea codePane;
    private final JTextArea logPane;
    private final JTextArea inputPane;
    private final JTextArea outputPane;
    private final JTextField statusName;
    private final JTextField statusRow;
    private final JTextField statusCol;
    
    private final SingleModel singleTableModel;
    
    // TODO station list
    // TODO add/delete column
    // TODO show graph
    // TODO invalidate program on edit
    // TODO undo
    // TODO include files
    // TODO recognize no changes
    
    private final Action runAction;
    private final Action stepAction;
    private final Action graphAction;

    private final Environment env;
    
    private String name = null;
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
        
        
        var reset = newAction("Reset", this::doReset, "Resets program");
        runAction = newAction("Run", this::doRun, "Run the program");
        stepAction = newAction("Step", this::doStep, "Execute one step is program already started; otherwise it is started but stopped at first tick");
        var compile = newAction("Compile", this::doCompile, "Compile current code");
        graphAction = newAction("Graph", this::doGraph, "Show a DOT graph of compiled program");
        
        var runMenu = new JMenu("Run");
        runMenu.add(newMenuItem(reset));
        runMenu.add(newMenuItem(runAction));
        runMenu.add(newMenuItem(stepAction));
        runMenu.addSeparator();
        runMenu.add(newMenuItem(compile));
        runMenu.add(newMenuItem(graphAction));
        
        var help = newAction("Help", this::doHelp, "Show help");
        
        var helpMenu = new JMenu("Help");
        helpMenu.add(newMenuItem(help));
        
        var menubar = new JMenuBar();
        menubar.add(fileMenu);
        menubar.add(runMenu);
        menubar.add(helpMenu);
        menubar.add(Box.createHorizontalStrut(50));
        menubar.add(newMenuBarButton(stepAction));

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
                setProgram(null);
            }
            @Override
            public void insertUpdate(DocumentEvent e) {
                changed = true;
                setProgram(null);
            }
            @Override
            public void changedUpdate(DocumentEvent e) {
                changed = true;
                setProgram(null);
            }
        });
        
        singleTableModel = new SingleModel();
        var singleStationTable = new JTable(singleTableModel);
        singleStationTable.setAutoResizeMode(singleStationTable.AUTO_RESIZE_OFF);
        singleStationTable.setFont(settings.mainFont());
        singleStationTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        singleStationTable.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
            @Override
            public void valueChanged(ListSelectionEvent e) {
                if (!e.getValueIsAdjusting()) {
                    var row = singleStationTable.getSelectedRow();
                    if (row != -1) {
                        var station = singleTableModel.station(row);
                        mark(station.pos());
                    }
                }
            }
        });
        for (var i = 0; i < singleStationTable.getColumnCount(); i++) {
            singleStationTable.getColumnModel().getColumn(i).setPreferredWidth(singleTableModel.size(i));
        }
        
        inputPane = newTextArea();
        
        outputPane = newTextArea();
        outputPane.setEditable(false);
        
        var io = newSplitPane(true);
        io.setTopComponent(newScrollPane(inputPane));
        io.setBottomComponent(newScrollPane(outputPane));
        
        var detailPane = new JTabbedPane();
        detailPane.addTab("Stations", newScrollPane(singleStationTable));
        detailPane.addTab("IO", io);
        
        var centerSplit = newSplitPane(false);
        centerSplit.setLeftComponent(codePane);
        centerSplit.setRightComponent(detailPane);
        centerSplit.setDividerLocation(850);
        
        logPane = newTextArea();
        logPane.setEditable(false);
        
        var mainSplit = newSplitPane(true);
        mainSplit.setTopComponent(centerSplit);
        mainSplit.setBottomComponent(newScrollPane(logPane));
        mainSplit.setDividerLocation(600);
        
        statusName = newTextField(30, "Name");
        
        statusRow = newTextField(5, "Row");
        
        statusCol = newTextField(5, "Column");
        
        var statusLine = Box.createHorizontalBox();
        statusLine.setBorder(BorderFactory.createEmptyBorder(2, 4, 4, 4));
        statusLine.add(statusName);
        statusLine.add(Box.createHorizontalGlue());
        statusLine.add(Box.createHorizontalStrut(10));
        statusLine.add(statusRow);
        statusLine.add(statusCol);
        
        var input = new Input() {
            @Override
            public void reset() {
                
            }
        };
        Output output = new Output() {
            @Override
            public void reset() {
                outputPane.setText("");
            }
            @Override
            public void write(String str) {
                outputPane.append(str);
            }
        };
        env = new Environment(this::print, input, output);
            
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
        frame.setSize(1200, 900);
        frame.validate();
        frame.setLocationRelativeTo(null);

        codePane.setCaretPosition(0);
        setName(PREFS.get(PREF_NAME, "unnamed"));
        update();
        frame.setVisible(true);

        io.setDividerLocation(0.5);
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
            setProgram(new Compiler(env).compile(name, codePane.getText()));
        } catch (CompileException ex) {
            setProgram(null);
            if (ex.pos != null) {
                mark(ex.pos);
            }
            error(ex, "at %s", ex.pos);
        }
        singleTableModel.fireTableDataChanged();
    }
    
    /** Shows a DOT graph of compiled program. */
    private void doGraph(ActionEvent ev) {
        if (program == null) {
            Toolkit.getDefaultToolkit().beep();
            return;
        }
        var dir = Paths.get(".graph");
        var filename = name.replaceFirst("\\..*$", "");
        if (filename.isBlank()) {
            filename = "unnamed";
        }
        var dotPath = dir.resolve(filename + ".dot");
        var bakPath = dir.resolve(filename + ".bak");
        var pngPath = dir.resolve(filename + ".png");
        
        try {
            Files.createDirectories(dir);
            try (BufferedWriter writer = Files.newBufferedWriter(dotPath)) {
                program.graph(writer);
            }
            
            if (Files.exists(pngPath)) {
                Files.move(pngPath, bakPath, REPLACE_EXISTING);
            }
            try (var inp = Files.newInputStream(dotPath, READ);
                 var out = Files.newOutputStream(pngPath);) {
                Dot.dot("png", inp, out);
            }
        } catch (IOException | InterruptedException ex) {
            error(ex, "creating graph \"%s\"", filename);
            return;
        }
        
        try (var inp = Files.newInputStream(pngPath, READ)) {
            var img = new ImageIcon(ImageIO.read(inp));
            var msg = newScrollPane(new JLabel(img));
            showMessageDialog(frame, msg, filename, PLAIN_MESSAGE);
        } catch (IOException ex) {
            error(ex, "reading image \"%s\"", pngPath);
            return;
        }
    }
    
    /** Resets program. */
    private void doReset(ActionEvent ev) {
        if (program != null) {
            program.reset();
            singleTableModel.fireTableDataChanged();
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
            singleTableModel.fireTableDataChanged();
        }
    }
    
    /** Sets a new program and updates GUI (actions). */
    private void setProgram(Program program) {
        this.program = program;
        env.reset();
        singleTableModel.program(program);
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
        runAction.setEnabled(false);  // TODO
        stepAction.setEnabled(runable);
        graphAction.setEnabled(runable);
    }

    /** Mark given pos (select it). */
    private void mark(Pos pos) {
        try {
            var ls = codePane.getLineStartOffset(pos.y()-1);
            var le = codePane.getLineEndOffset(pos.y()-1);
            var index = ls + pos.x() - 1;
            if (index >= le) {
                index = le - 1;
            }
            codePane.setCaretPosition(index);
            if (index < le) {
                codePane.select(index, index+1);
            }
            codePane.requestFocusInWindow();
        } catch (BadLocationException ex1) {
            ex1.printStackTrace();
        }
    }

    /** Creates new Action. */
    private Action newAction(String title, Consumer<ActionEvent> runable, String tooltip) {
        var action = new AbstractAction(title) {
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
        pane.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));
        pane.setFont(settings.mainFont());
        return pane;
    }
    
    /** Creates a JTextField */
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
    
    /** Creates a JSplitPane. */
    private JSplitPane newSplitPane(boolean vertical) {
        var split = new JSplitPane();
        split.setOrientation(vertical ? split.VERTICAL_SPLIT : split.HORIZONTAL_SPLIT);
        split.setOneTouchExpandable(true);
        split.setResizeWeight(1);
        return split;
    }
    
    /** Creates a JButton for menu bar. */
    private JButton newMenuBarButton(Action action) {
        var button = new JButton(action);
//        button.setBorderPainted(false);
        button.setMargin(new Insets(2, 4, 2, 4));
        return button;
    }
    
    /** Shows and prints error message, including stack trace. */
    private void error(Throwable ex, String format, Object... args) {
        var msg = String.format(format, args);
        System.err.println(msg);
        ex.printStackTrace();
        print("%n%s %s", ex.getClass().getSimpleName(), msg);
        showMessageDialog(frame, new Object[] {ex.getClass().getSimpleName(), ex.getMessage(), msg}, ex.getClass().getSimpleName(), ERROR_MESSAGE);
    }
    
    /** prints message to log pane. */
    private void print(String format, Object... args) {
        var atEnd = logPane.getCaretPosition() == logPane.getText().length();
        logPane.append(String.format(format, args));
        if (atEnd) {
            logPane.setCaretPosition(logPane.getText().length());
        }
    }
}
