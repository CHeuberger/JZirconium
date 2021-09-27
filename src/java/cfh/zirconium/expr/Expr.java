package cfh.zirconium.expr;

import static java.util.Objects.*;

/** {@code expr := value | expr sp* expr sp* operator} */
abstract class Expr {

    /** Executes this expression using given values for N and K. */
    abstract int calculate(int n, int k);

    //==============================================================================================
    
    /** {@code value := "N" | "K" | integer} */
    static abstract class Value extends Expr {
        //
    }

    //----------------------------------------------------------------------------------------------

    /** Number of drones. */
    static final class N extends Value {
        @Override
        int calculate(int n, int k) {
            return n;
        }
        @Override
        public String toString() {
            return "N";
        }
    }
    
    /** Number of links. */
    static final class K extends Value {
        @Override
        int calculate(int n, int k) {
            return k;
        }
        @Override
        public String toString() {
            return "K";
        }
    }
    
    /** Literal integer */
    static final class Literal extends Value {
        private final int value;
        public Literal(int value) {
            this.value = value;
        }
        @Override
        int calculate(int n, int k) {
            return value;
        }
        @Override
        public String toString() {
            return Integer.toString(value);
        }
    }
    
    //==============================================================================================
    
    /** {@code expr sp* expr sp* operator} */
    static final class Operation extends Expr {
        private static final String OPS = "+-*/=";
        private final Expr arg1;
        private final Expr arg2;
        private final char op;
        Operation(Expr arg1, Expr arg2, char op) {
            if (OPS.indexOf(op) == -1) {
                throw new IllegalArgumentException("invalid operation '" + op + "'");
            }
            this.arg1 = requireNonNull(arg1);
            this.arg2 = requireNonNull(arg2);
            this.op = op;
        }
        @Override
        int calculate(int n, int k) {
            int val1 = arg1.calculate(n, k);
            int val2 = arg2.calculate(n, k);
            switch (op) {
                case '+': return val1 + val2;
                case '-': return val1 - val2;
                case '*': return val1 * val2;
                case '/': return val2==0 ? 0 : val1 / val2;
                case '=': return val1==val2 ? 1 : 0;
                default: throw new IllegalArgumentException("invalid operation '" + op + "'");
            }
        }
        @Override
        public String toString() {
            return arg1 + " " + arg2 + " " + op;
//            return "(" + arg1 + op + arg2 + ")";
        }
    }
}
