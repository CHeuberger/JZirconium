package cfh.zirconium.extra;

import java.awt.AlphaComposite;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.util.Arrays;
import java.util.Deque;
import java.util.LinkedList;
import java.util.concurrent.ExecutionException;
import java.util.prefs.Preferences;
import java.util.stream.Collectors;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JMenuBar;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;

public class ZoneDetectionProbe extends JPanel {

    private static final int GAP = 10;
    private static final int SIZE = 25;
    private static final int COUNT = 20;
    private static final int FONT = 20;
    
    private static final int UNDO_SIZE = 20;
    
    public static final String EMPTY = " ";
    public static final String PURE = ".o0OQ@";
    public static final String TUNNEL = "-/|\\+X*";
    public static final String APERTURE = ">^<v#";
    public static final String COMMENT = "()";
    public static final String FENCE = "{~}";
    public static final String FORT = "[=]";
    public static final String VALID_ZONE = FENCE + FORT;
    public static final String VALID_CODE = EMPTY + PURE + TUNNEL + APERTURE + COMMENT + FENCE + FORT;
    
    public static void main(String[] args) {
        SwingUtilities.invokeLater(ZoneDetectionProbe::new);
    }
    
    private final Preferences preferences = Preferences.userNodeForPackage(getClass());
    private static final String PREF_CODE = "test.code";
    
    private final JButton clearButton;
    private final JButton flood1Button;
    private final JButton flood2Button;
    private final JButton stepButton;
    private final JButton slowButton;
    private final JButton runButton;
    private final JButton helpButton;
    
    private Point mark = null;
    private Point markEnd = null;
    private char[][] code = new char[COUNT][COUNT];
    private Algorithm algorithm = null;
    private volatile boolean running = false;
    
    private final Deque<char[][]> undo = new LinkedList<>();
    
    
    private ZoneDetectionProbe() {
        clear();
        var text = preferences.get(PREF_CODE, null);
        if (text != null) {
            codeFromString(0, 0, text);
        }
        
        addKeyListener(new KeyAdapter() {
            @Override
            public void keyTyped(KeyEvent ev) {
                doKeyTyped(ev);
            }
        });
        addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent ev) {
                doMousePressed(ev);
            }
        });
        addMouseMotionListener(new MouseAdapter() {
            @Override
            public void mouseDragged(MouseEvent ev) {
                doMouseDragged(ev);
            }
        });
        setFont(new Font("Dialog", Font.BOLD, FONT));
        setPreferredSize(new Dimension(COUNT*SIZE+1, COUNT*SIZE+1));
        setKeystrokeAction("up", KeyStroke.getKeyStroke("pressed UP"), this::doUp);
        setKeystrokeAction("s-up", KeyStroke.getKeyStroke("shift pressed UP"), this::doUp);
        setKeystrokeAction("down", KeyStroke.getKeyStroke("pressed DOWN"), this::doDown);
        setKeystrokeAction("s-down", KeyStroke.getKeyStroke("shift pressed DOWN"), this::doDown);
        setKeystrokeAction("left", KeyStroke.getKeyStroke("pressed LEFT"), this::doLeft);
        setKeystrokeAction("s-left", KeyStroke.getKeyStroke("shift pressed LEFT"), this::doLeft);
        setKeystrokeAction("right", KeyStroke.getKeyStroke("pressed RIGHT"), this::doRight);
        setKeystrokeAction("s-right", KeyStroke.getKeyStroke("shift pressed RIGHT"), this::doRight);
        setKeystrokeAction("copy", KeyStroke.getKeyStroke("control pressed C"), this::doCopy);
        setKeystrokeAction("paste", KeyStroke.getKeyStroke("control pressed V"), this::doPaste);
        setKeystrokeAction("shiftPaste", KeyStroke.getKeyStroke("control shift pressed V"), this::doPaste);
        setKeystrokeAction("undo", KeyStroke.getKeyStroke("control pressed Z"), this::doUndo);
        setKeystrokeAction("delete", KeyStroke.getKeyStroke("pressed DELETE"), this::doDel);
        setKeystrokeAction("backspace", KeyStroke.getKeyStroke("pressed BACK_SPACE"), this::doBack);
        
        clearButton = newButton("Clear", this::doClear, "Clear all fields");
        flood1Button = newButton("FLOOD-1", this::doFlood1, "Start FLOOD-1 algorithm");
        flood2Button = newButton("FLOOD-2", this::doFlood2, "Start FLOOD-2 algorithm");
        stepButton = newButton("Step", this::doStep, "Do one step on started algorithm");
        slowButton = newButton("Slow", this::doSlow, "Slow run the started algorithm up to the end");
        runButton = newButton("Run", this::doRun, "Run the started algorithm up to the end");
        helpButton =newButton("Help", this::doHelp, "Show help");
        runStatus(false);
        
        var menubar = new JMenuBar();
        menubar.add(Box.createHorizontalStrut(5));
        menubar.add(clearButton);
        menubar.add(Box.createHorizontalGlue());
        menubar.add(flood1Button);
        menubar.add(flood2Button);
        menubar.add(Box.createHorizontalGlue());
        menubar.add(stepButton);
        menubar.add(slowButton);
        menubar.add(runButton);
        menubar.add(Box.createHorizontalGlue());
        menubar.add(helpButton);
        menubar.add(Box.createHorizontalStrut(5));
        
        var center = new JPanel();
        center.add(this);
        center.setBorder(BorderFactory.createEmptyBorder(GAP, GAP, GAP, GAP));
        
        var frame = new JFrame();
        frame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosed(WindowEvent ev) {
                preferences.put(PREF_CODE, codeToString());
            }
        });
        frame.setDefaultCloseOperation(frame.DISPOSE_ON_CLOSE);
        frame.setJMenuBar(menubar);
        frame.add(center, BorderLayout.CENTER);
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }
    
    private void markCell(Graphics2D gg, Color color, int x, int y) {
        gg.setColor(color);
        var xx = x * SIZE;
        var yy = y * SIZE;
        for (var i = 1; i <= 2; i++) {
            gg.drawRect(xx+i, yy+i, SIZE-i-i, SIZE-i-i);
        }
    }

    private void doClear(ActionEvent ev) {
        if (!Arrays.stream(code).map(String::new).allMatch(String::isBlank)) {
            pushCode();
        }
        clear();
        preferences.put(PREF_CODE, codeToString());
        algorithm = null;
        runStatus(false);
        repaint();
    }
    
    private void doFlood1(ActionEvent ev) {
        algorithm = new Flood1Algorithm(code);
        runStatus(true);
        repaint();
    }
    
    private void doFlood2(ActionEvent ev) {
        algorithm = new Flood2Algorithm(code);
        runStatus(true);
        repaint();
    }
    
    private void doStep(ActionEvent ev) {
        if (algorithm != null) {
            if (running) {
                running = false;
            } else if (algorithm.step()) {
                repaint();
            } else {
                repaint();
                runStatus(false);
            }
        } else {
            runStatus(false);
        }
    }
    
    private void doSlow(ActionEvent ev) {
        var alg = algorithm;
        if (alg != null) {
            var worker = new SwingWorker<Void, Void>() {
                @Override
                protected Void doInBackground() throws Exception {
                    while (running && alg == algorithm) {
                        if (!alg.step()) {
                            repaint();
                            break;
                        }
                        repaint();
                        Thread.sleep(20);
                    }
                    return null;
                }
                @Override
                protected void done() {
                    try {
                        get();
                    } catch (InterruptedException | ExecutionException ex) {
                        ex.printStackTrace();
                    }
                    runStatus(!running);
                }
            };
            runStatus(false);
            stepButton.setEnabled(true);
            running = true;
            worker.execute();
        }
    }
    
    private void doRun(ActionEvent ev) {
        var alg = algorithm;
        runStatus(false);
        if (alg != null) {
            while (alg == algorithm) {
                if (!alg.step()) {
                    repaint();
                    break;
                }
                repaint();
            }
        }
    }
    
    private void doHelp(ActionEvent ev) {
        var text = """
              H E L P
            
            left-click:  select cell
            left-drag:  select cell region
            shift-left-click:  extend cell region
            
            DEL:  delete selected region
            BACKSPACE:  move selection left and delete, or delete selected region
            ctrl-C:  copy selected region or whole code if no region selected
            ctrl-V:  clears all and paste code from clipboard
            shift-ctrl-V:  paste clipboard starting at selected cell
            ctrl-Z:  undo
            
            arrows:  move selected cell
            shift arrows:  move end of selected region
            [=]{~}:  enter type character into selected cell
            space:  clear selected cell
            """;
        var area = new JTextArea(text);
        area.setBorder(BorderFactory.createEmptyBorder(8, 4, 8, 4));
        area.setEditable(false);
        JOptionPane.showMessageDialog(this, new JScrollPane(area));
    }
    
    private void doUp(ActionEvent ev) {
        if (mark != null) {
            if (isShift(ev)) {
                if (markEnd == null) {
                    if (mark.y > 0) {
                        selectEnd(mark.x, mark.y-1);
                    }
                } else {
                    if (markEnd.y > 0) {
                        selectEnd(markEnd.x, markEnd.y-1);
                    }
                }
            } else {
                if (mark.y > 0) {
                    select(mark.x, mark.y-1);
                }
            }
        }
    }
    
    private void doDown(ActionEvent ev) {
        if (mark != null) {
            if (isShift(ev)) {
                if (markEnd == null) {
                    if (mark.y+1 < COUNT) {
                        selectEnd(mark.x, mark.y+1);
                    }
                } else {
                    if (markEnd.y+1 < COUNT) {
                        selectEnd(markEnd.x, markEnd.y+1);
                    }
                }
            } else {
                if (mark.y+1 < COUNT) {
                    select(mark.x, mark.y+1);
                }
            }
        }
    }
    
    private void doLeft(ActionEvent ev) {
        if (mark != null) {
            if (isShift(ev)) {
                if (markEnd == null) {
                    if (mark.x > 0) {
                        selectEnd(mark.x-1, mark.y);
                    }
                } else {
                    if (markEnd.x > 0) {
                        selectEnd(markEnd.x-1, markEnd.y);
                    }
                }
            } else {
                if (mark.x > 0) {
                    select(mark.x-1, mark.y);
                }
            }
        }
    }
    
    private void doRight(ActionEvent ev) {
        if (mark != null) {
            if (isShift(ev)) {
                if (markEnd == null) {
                    if (mark.x+1 < COUNT) {
                        selectEnd(mark.x+1, mark.y);
                    }
                } else {
                    if (markEnd.x+1 < COUNT) {
                        selectEnd(markEnd.x+1, markEnd.y);
                    }
                }
            } else {
                if (mark.x+1 < COUNT) {
                    select(mark.x+1, mark.y);
                }
            }
        }
    }
    
    private void doCopy(ActionEvent ev) {
        String source;
        if (mark != null && markEnd != null) {
            var x0 = Math.min(mark.x, markEnd.x);
            var x1 = Math.max(mark.x, markEnd.x) + 1;
            var y0 = Math.min(mark.y, markEnd.y);
            var y1 = Math.max(mark.y, markEnd.y) + 1;
            source = Arrays.stream(code, y0, y1).map(a -> new String(a, x0, x1-x0)).collect(Collectors.joining("\n"));
        } else {
            String text = codeToString();
            source = text;
        }
        var content = new StringSelection(source);
        var clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
        clipboard.setContents(content, content);
    }
    
    private void doPaste(ActionEvent ev) {
        var clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
        if (clipboard.isDataFlavorAvailable(DataFlavor.stringFlavor)) {
            try {
                var text = (String) clipboard.getData(DataFlavor.stringFlavor);
                if (text != null) {
                    pushCode();
                    int x;
                    int y;
                    if (isShift(ev)) {
                        if (mark == null) {
                            return;
                        }
                        x = mark.x;
                        y = mark.y;
                    } else {
                        clear();
                        x = 0;
                        y = 0;
                    }
                    codeFromString(x, y, text);
                    preferences.put(PREF_CODE, text);
                    algorithm = null;
                    repaint();
                }
            } catch (UnsupportedFlavorException | IOException ex) {
                ex.printStackTrace();
            }
        }
    }
    
    private String codeToString() {
        return Arrays.stream(code).map(String::new).collect(Collectors.joining("\n"));
    }

    private void codeFromString(int x, int y, String text) {
        var source = text.split("\n");
        for (var i = 0; y+i < COUNT && i < source.length; i++) {
            for (var j = 0; x+j < COUNT && j < source[i].length(); j++) {
                var ch = source[i].charAt(j);
                if (ch == ' ' || VALID_ZONE.indexOf(ch) != -1) {
                    code[y+i][x+j] = ch;
                }
            }
        }
    }
    
    private void doUndo(ActionEvent ev) {
        var clone = undo.poll();
        if (clone != null) {
            for (var i = 0 ; i < COUNT && i < clone.length; i++) {
                System.arraycopy(clone[i], 0, code[i], 0, COUNT);
            }
            preferences.put(PREF_CODE, codeToString());
        } else {
            beep();
        }
        algorithm = null;
        repaint();
    }
    
    private void doDel(ActionEvent ev) {
        if (mark != null) {
            pushCode();
            if (markEnd != null) {
                var x0 = Math.min(mark.x, markEnd.x);
                var x1 = Math.max(mark.x, markEnd.x);
                var y0 = Math.min(mark.y, markEnd.y);
                var y1 = Math.max(mark.y, markEnd.y);
                for (var x = x0; x <= x1; x++) {
                    for (var y = y0; y <= y1; y++) {
                        code[y][x] = ' ';
                    }
                }
            } else {
                code[mark.y][mark.x] = ' ';
            }
            repaint();
        }
    }

    private void doBack(ActionEvent ev) {
        if (mark != null) {
            pushCode();
            if (markEnd != null) {
                var x0 = Math.min(mark.x, markEnd.x);
                var x1 = Math.max(mark.x, markEnd.x);
                var y0 = Math.min(mark.y, markEnd.y);
                var y1 = Math.max(mark.y, markEnd.y);
                for (var x = x0; x <= x1; x++) {
                    for (var y = y0; y <= y1; y++) {
                        code[y][x] = ' ';
                    }
                }
                
            } else {
                if (--mark.x < 0) {
                    if (--mark.y < 0) {
                        mark.y = code.length - 1;
                    }
                    mark.x = code[mark.y].length - 1;
                }
                code[mark.y][mark.x] = ' ';
            }
            repaint();
        }
    }
    
    private void doMousePressed(MouseEvent ev) {
        if (SwingUtilities.isLeftMouseButton(ev)) {
            requestFocus();
            var x = ev.getX() / SIZE;
            var y = ev.getY() / SIZE;
            if (noModifier(ev)) {
                markEnd = null;
                select(x, y);
            } else if (isShiftOnly(ev)) {
                selectEnd(x, y);
            }
        }
    }
    
    private void doMouseDragged(MouseEvent ev) {
        if (SwingUtilities.isLeftMouseButton(ev)) {
            requestFocus();
            var x = ev.getX() / SIZE;
            var y = ev.getY() / SIZE;
            selectEnd(x, y);
        }
    }
    
    private void doKeyTyped(KeyEvent ev) {
        var ch = ev.getKeyChar();
        if (ch == ' ' || VALID_ZONE.indexOf(ch) != -1) {
            insertCode(ch);
        }
    }
    
    private void select(int x, int y) {
        if (x >= 0 && x < COUNT && y >= 0 && y < COUNT) {
            mark = new Point(x, y);
        } else {
            mark = null;
        }
        repaint();
    }
    
    private void selectEnd(int x, int y) {
        if (x >= 0 && x < COUNT && y >= 0 && y < COUNT) {
            markEnd = new Point(x, y);
        } else {
            markEnd = null;
        }
        repaint();
    }
    
    private void insertCode(char ch) {
        if (mark != null) {
            var x = mark.x;
            var y = mark.y;
            if (ch != code[y][x]) {
                pushCode();
                code[y][x] = ch;
                preferences.put(PREF_CODE, codeToString());
            }
            if ("[]{}".indexOf(ch) != -1) {
                if (++y >= COUNT) {
                    y = 0;
                    if (++x >= COUNT) {
                        x = 0;
                    }
                }
            } else {
                if (++x >= COUNT) {
                    x = 0;
                    if (++y >= COUNT) {
                        y = 0;
                    }
                }
            }
            select(x, y);
            repaint();
        }
    }

    private void clear() {
        for (var i = 0; i < COUNT; i++) {
            Arrays.fill(code[i], ' ');
        }
    }
    
    private void pushCode() {
        if (undo.isEmpty() || !Arrays.deepEquals(undo.peek(), code)) {
            undo.push(Arrays.stream(code).map(char[]::clone).toArray(char[][]::new));
            while (undo.size() > UNDO_SIZE) {
                undo.pollLast();
            }
        }
    }
    
    private void runStatus(boolean enabled) {
        stepButton.setEnabled(enabled);
        slowButton.setEnabled(enabled);
        runButton.setEnabled(enabled);
    }
    
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        
        var gg = (Graphics2D) g.create();
        try {
            paintGtrid(gg);
            paintMark(gg);
            paintCode(gg);
            paintAlgorithm(gg);
        } finally {
            gg.dispose();
        }
    }

    private void paintGtrid(Graphics2D gg) {
        gg.setColor(Color.LIGHT_GRAY);
        var total = COUNT * SIZE;
        
        for (var y = 0; y <= total; y += SIZE) {
            gg.drawLine(0, y, total, y);
        }
        for (var x = 0 ; x <= total ; x += SIZE) {
            gg.drawLine(x, 0, x, total);
        }
    }
    
    private void paintMark(Graphics2D gg) {
        if (markEnd != null) {
            var x0 = Math.min(mark.x, markEnd.x);
            var x1 = Math.max(mark.x, markEnd.x);
            var y0 = Math.min(mark.y, markEnd.y);
            var y1 = Math.max(mark.y, markEnd.y);
            for (var x = x0; x <= x1; x++) {
                for (var y = y0; y <= y1; y++) {
                    markCell(gg, Color.CYAN.darker(), x, y);
                }
            }
        }
        if (mark != null) {
            markCell(gg, Color.BLUE, mark.x, mark.y);
        }
    }
    
    private void paintAlgorithm(Graphics2D gg) {
        if (algorithm != null) {
            var g2 = (Graphics2D) gg.create();
            try {
                g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_ATOP, 0.5F));
                algorithm.paint(g2, SIZE);
            } finally {
                g2.dispose();
            }
        }
    }
    
    private void paintCode(Graphics2D gg) {
        var fm = gg.getFontMetrics();
        var dx = 0;
        var dy = SIZE - fm.getDescent() - (SIZE-fm.getAscent()-fm.getDescent())/2;
        gg.setColor(Color.BLACK);
        for (var y = 0; y < COUNT; y++) {
            for (var x = 0; x < COUNT; x++) {
                var ch = code[y][x];
                if (ch != ' ') {
                    dx = (SIZE - fm.charWidth(ch)) / 2;
                    gg.drawString(Character.toString(ch), x*SIZE+dx, y*SIZE+dy);
                }
            }
        }
    }
    
    private JButton newButton(String name, ActionListener listener, String tooltip) {
        var button = new JButton(name);
        button.addActionListener(listener);
        button.setToolTipText(tooltip);
        button.setFocusable(false);
        return button;
    }
    
    private Action newAction(String name, ActionListener listener) {
        return new AbstractAction(name) {
            @Override
            public void actionPerformed(ActionEvent ev) {
                listener.actionPerformed(ev);
            }
        };
    }
    
    private void setKeystrokeAction(String name, KeyStroke key, ActionListener listener) {
        var actions = getActionMap();
        var inputs = getInputMap();

        actions.put(name, newAction(name, listener));
        inputs.put(key, name);
    }
    
    private boolean noModifier(MouseEvent ev) {
        return (ev.getModifiersEx() & (ev.ALT_DOWN_MASK | ev.CTRL_DOWN_MASK | ev.SHIFT_DOWN_MASK)) == 0;
    }

    private boolean isShiftOnly(MouseEvent ev) {
        return (ev.getModifiersEx() & (ev.ALT_DOWN_MASK | ev.CTRL_DOWN_MASK | ev.SHIFT_DOWN_MASK)) == ev.SHIFT_DOWN_MASK;
    }
    
    private boolean isShift(ActionEvent ev) {
        return (ev.getModifiers() & ev.SHIFT_MASK) != 0;
    }
    
    private void beep() {
        Toolkit.getDefaultToolkit().beep();
    }
}
