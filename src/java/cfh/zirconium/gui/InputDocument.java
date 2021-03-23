package cfh.zirconium.gui;

import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.DocumentFilter;
import javax.swing.text.PlainDocument;

/** Document for input pane, do not allow insertion/deletion for already consumed chars. */
@SuppressWarnings("serial")
public class InputDocument extends PlainDocument {

    private int cursor = 0;
    
    InputDocument() {
        setDocumentFilter(new DocumentFilter() {
            @Override
            public synchronized void insertString(FilterBypass fb, int offset, String string, AttributeSet attr) throws BadLocationException {
                if (offset >= cursor) {
                    super.insertString(fb, offset, string, attr);
                }
            }
            @Override
            public void replace(FilterBypass fb, int offset, int length, String text, AttributeSet attrs) throws BadLocationException {
                if (offset >= cursor) {
                    super.replace(fb, offset, length, text, attrs);
                }
            }
            @Override
            public void remove(FilterBypass fb, int offset, int length) throws BadLocationException {
                if (offset >= cursor) {
                    super.remove(fb, offset, length);
                }
            }
        });
    }
    
    void reset() {
        cursor = 0;
    }

    synchronized boolean hasByte() {
        return cursor < getLength();
    }
    
    synchronized int nextByte() throws BadLocationException {
        if (!hasByte()) {
            throw new BadLocationException("no bytes available", cursor);
        }
        return getText(cursor++, 1).charAt(0);
    }
    
    synchronized boolean hasInteger() {
        var tmp = cursor;
        while (hasByte()) {
            int b;
            try {
                b = nextByte();
            } catch (BadLocationException ex) {
                ex.printStackTrace();
                throw new RuntimeException(ex);
            }
            if (Character.isDigit((char)b)) {
                cursor = tmp;
                return true;
            }
            if (b != ' ' && b != '\t') {
                break;
            }
        }
        cursor = tmp;
        return false;
    }
    
    synchronized int nextInteger() throws BadLocationException {
        while (hasByte()) {
            var b = nextByte();
            if (Character.isDigit((char)b)) {
                break;
            }
            if (b != ' ' && b != '\t') {
                throw new BadLocationException("no integer available", cursor-1);
            }
        }
        var offset = cursor-1;
        while (hasByte()) {
            if (!Character.isDigit((char)nextByte())) {
                cursor -= 1;
                break;
            }
        }
        return Integer.parseInt(getText(offset, cursor-offset));
    }
}
