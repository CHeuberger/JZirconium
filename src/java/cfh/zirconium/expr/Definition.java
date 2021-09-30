package cfh.zirconium.expr;

import static cfh.zirconium.Compiler.*;

import java.nio.BufferUnderflowException;
import java.nio.CharBuffer;
import java.util.LinkedList;

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
        assert !text.isBlank() : "empty expression";
        var buf = CharBuffer.wrap(text).asReadOnlyBuffer();
        try {
            skipSpaces(buf);
            var symbol = buf.get();
            if (Character.isWhitespace(symbol)) {
                throw new CompileException(new Pos(pos.x()+buf.position()-1, pos.y()), "invalid symbol: whitespace");
            }
            if (NOT_STATION.indexOf(symbol) != -1) {
                throw new CompileException(new Pos(pos.x()+buf.position()-1, pos.y()), "invalid symbol: '" + symbol + "'");
            }
            skipSpaces(buf);
            if (buf.get() != '=') {
                throw new CompileException(new Pos(pos.x()+buf.position()-1, pos.y()), "expcted '=' in defintion");
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
            
            return new Definition(symbol, expr);
        } catch (BufferUnderflowException ex) {
            throw new CompileException(new Pos(pos.x()+buf.position()-1, pos.y()), "incomplete definition");
        }
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
    
    public final char symbol;
    public final Expr expr;
    
    /** Constructor. */
    private Definition(char symbol, Expr expr) {
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

    /** Execute the expression. */
    public int calculate(int n, int k) {
        return expr.calculate(n, k);
    }
}
