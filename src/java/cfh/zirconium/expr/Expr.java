package cfh.zirconium.expr;

/** {@code expr := value | expr sp* expr sp* operator} */
sealed abstract class Expr {

    //==============================================================================================
    
    /** {@code value := "N" | "K" | integer} */
    static sealed abstract class Value extends Expr {
        //
    }

    //----------------------------------------------------------------------------------------------

    /** Number of drones. */
    static final class N extends Value {
        @Override
        public String toString() {
            return "N";
        }
    }
    
    /** Number of links. */
    static final class K extends Value {
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
        public String toString() {
            return Integer.toString(value);
        }
    }
    
    //==============================================================================================
    
    /** {@code expr sp* expr sp* operator} */
    static final class Operation extends Expr {
        private final Expr arg1;
        private final Expr arg2;
        private final char op;
        Operation(Expr arg1, Expr arg2, char op) {
            this.arg1 = arg1;
            this.arg2 = arg2;
            this.op = op;
        }
        @Override
        public String toString() {
            return arg1 + " " + arg2 + " " + op;
//            return "(" + arg1 + op + arg2 + ")";
        }
    }
}
