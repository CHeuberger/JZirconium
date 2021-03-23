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
}
