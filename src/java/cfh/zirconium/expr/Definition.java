package cfh.zirconium.expr;

import java.nio.BufferUnderflowException;
import java.nio.CharBuffer;
import java.util.LinkedList;

import cfh.zirconium.Compiler.CompileException;
import cfh.zirconium.expr.Expr.*;
import cfh.zirconium.net.Pos;

/** Definition for synthetic stations. */
public class Definition {

    /*
     * definition := symbol sp* "=" sp* expr
     * symbol := [^\s]
     * expr := value | expr sp* expr sp* operator
     * value := "N" | "K" | integer
     * integer := ["0"-"9"]+
     * operator := "+" | "-" | "*" | "/" | "=" 
     * sp := " " | "\t"
     */
    private static final String SP = " \t";
    private static final String OP = "+-*/=";
    
    /** Parse given text for a Definition. */
    public static Definition parse(Pos pos, String text) throws CompileException {
        if (text.isBlank()) {
            throw new CompileException(pos, "empty lens");
        }
        var buf = CharBuffer.wrap(text).asReadOnlyBuffer();
        try {
            skipSpaces(buf);
            var symbol = buf.get();
            if (Character.isWhitespace(symbol)) {
                throw new CompileException(new Pos(pos.x()+buf.position()-1, pos.y()), "invalid symbol");
            }
            skipSpaces(buf);
            if (buf.get() != '=') {
                throw new CompileException(new Pos(pos.x()+buf.position()-1, pos.y()), "expcted \"=\" in defintion");
            }
            skipSpaces(buf);

            var stack = new LinkedList<Expr>();
            while (buf.hasRemaining()) {
                var ch = buf.get();
                if (ch == 'N') stack.push(new N());
                else if (ch == 'K') stack.push(new K());
                else if (Character.isDigit(ch)) {
                    var str = Character.toString(ch);
                    while (buf.hasRemaining()) {
                        buf.mark();
                        ch = buf.get();
                        if (Character.isDigit(ch)) {
                            str += ch;
                        } else {
                            buf.reset();
                            break;
                        }
                    }
                    stack.push(new Literal(Integer.parseInt(str)));
                } else if (OP.indexOf(ch) != -1) {
                    if (stack.size() < 2) {
                        throw new CompileException(new Pos(pos.x()+buf.position()-1, pos.y()), "not enough arguments");
                    }
                    var arg2 = stack.pop();
                    var arg1 = stack.pop();
                    stack.push(new Operation(arg1, arg2, ch));
                } else {
                    throw new CompileException(new Pos(pos.x()+buf.position()-1, pos.y()), "unrecognized character: " + ch);
                }
                skipSpaces(buf);
            }
            
            if (stack.isEmpty()) {
                throw new CompileException(new Pos(pos.x()+buf.position()-1, pos.y()), "no expression");
            }
            var expr = stack.pop();
            if (!stack.isEmpty()) {
                throw new CompileException(new Pos(pos.x()+buf.position()-1, pos.y()), "too many arguments/missing operator");
            }
            
            return new Definition(pos, text, symbol, expr);
        } catch (BufferUnderflowException ex) {
            throw new CompileException(new Pos(pos.x()+buf.position()-1, pos.y()), "incomplete definition");
        }
        
        // TODO
    }
    
    /** Skip spaces leaving the buffer at the next non-empty char. */
    private static void skipSpaces(CharBuffer buf) {
        while (buf.hasRemaining()) {
            buf.mark();
            var ch = buf.get();
            if (SP.indexOf(ch) == -1) {
                buf.reset();
                break;
            }
        }
    }

    //----------------------------------------------------------------------------------------------
    
    private final Pos pos;
    private final String text;
    
    private final char symbol;
    private final Expr expr;
    
    /** Constructor. */
    private Definition(Pos pos, String text, char symbol, Expr expr) {
        this.pos = pos;
        this.text = text;
        this.symbol = symbol;
        this.expr = expr;
    }
    
    @Override
    public int hashCode() {
        return Character.hashCode(symbol);
    }
    
    @Override
    public boolean equals(Object other) {
        if (other == this) return true;
        if (other == null || other.getClass() != this.getClass()) return false;
        return ((Definition)other).symbol == this.symbol;
    }
    
    @Override
    public String toString() {
        return symbol + ":" + expr;
    }
}
