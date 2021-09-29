package cfh.zirconium.net;

import java.util.function.Predicate;

import cfh.zirconium.Compiler;
import cfh.zirconium.Environment;
import cfh.zirconium.Program;
import cfh.zirconium.Environment.Input;
import cfh.zirconium.Environment.Output;
import cfh.zirconium.Environment.Printer;

public class StationTest {

    public static void main(String[] args) {
        var test = new StationTest();
        test.pureStations();
        test.tunnel();
        test.tunnelCrossing();
        test.aperture();
        test.boundStation();
        test.syntax();
        test.exclusionZone();
        if (test.errors == 0) {
            System.out.println("\nOK");
        } else {
            System.err.printf("%nerrors: %d%n", test.errors);
        }
    }

    private int errors;
    private final Printer printer;
    private final Input input;
    private final Output output = new TestOutput();
    private final Output error = new TestOutput();
    private final Compiler compiler;
    
    private StationTest() {
        errors = 0;
        printer = new Printer() {
            @Override
            public void print(String format, Object... args) {
                //
            }
        };
        input = new Input() {
            @Override
            public void reset() {
                //
            }
            @Override
            public int readByte() {
                return 0;
            }
            @Override
            public int readInteger() {
                return 0;
            }
        };
        compiler = new Compiler(new Environment(printer, input, output, error));
    }

    private void pureStations() {
        // . If this is occupied by any amount of drones, dispatch one drone to each linked station.
        try {
            var program = compiler.compile("test.pure.dot", "0<.>0", "");
            var nop1 = (NopStation) get(0, 0, program);
            var dot = (DotStation) get(2, 0, program);
            var nop2 = (NopStation) get(4, 0, program);
            
            program.reset();
            assertEquals(0, dot.drones(), program.name() + ": . - after reset"); 
            assertEquals(0, nop1.drones(), program.name() + ": nop1 - after reset"); 
            assertEquals(0, nop2.drones(), program.name() + ": nop2 - after reset"); 
            program.step();
            assertEquals(0, dot.drones(), program.name() + ": dot- after first step"); 
            assertEquals(0, nop1.drones(), program.name() + ": nop1 - after first step"); 
            assertEquals(0, nop2.drones(), program.name() + ": nop2 - after first step"); 
            dot.receive(5);
            program.step();
            assertEquals(0, dot.drones(), program.name() + ": dot- after second step"); 
            assertEquals(1, nop1.drones(), program.name() + ": nop1 - after second step"); 
            assertEquals(1, nop2.drones(), program.name() + ": nop2 - after second step"); 
            program.step();
            assertEquals(0, dot.drones(), program.name() + ": dot- after third step"); 
            assertEquals(0, nop1.drones(), program.name() + ": nop1 - after third step"); 
            assertEquals(0, nop2.drones(), program.name() + ": nop2 - after third step"); 
        } catch (Exception ex) {
            errors += 1;
            ex.printStackTrace();
        }
        
        // o Dispatch the number of drones occupying this station to each linked station.
        try {
            var program = compiler.compile("test.pure.o", "0<o>0", "");
            var nop1 = (NopStation) get(0, 0, program);
            var dup = (DupStation) get(2, 0, program);
            var nop2 = (NopStation) get(4, 0, program);
            
            program.reset();
            assertEquals(0, dup.drones(), program.name() + ": o - after reset"); 
            assertEquals(0, nop1.drones(), program.name() + ": nop1 - after reset"); 
            assertEquals(0, nop2.drones(), program.name() + ": nop2 - after reset"); 
            program.step();
            assertEquals(0, dup.drones(), program.name() + ": o - after first step"); 
            assertEquals(0, nop1.drones(), program.name() + ": nop1 - after first step"); 
            assertEquals(0, nop2.drones(), program.name() + ": nop2 - after first step"); 
            dup.receive(3);
            program.step();
            assertEquals(0, dup.drones(), program.name() + ": o - after second step"); 
            assertEquals(3, nop1.drones(), program.name() + ": nop1 - after second step"); 
            assertEquals(3, nop2.drones(), program.name() + ": nop2 - after second step"); 
            program.step();
            assertEquals(0, dup.drones(), program.name() + ": o - after third step"); 
            assertEquals(0, nop1.drones(), program.name() + ": nop1 - after third step"); 
            assertEquals(0, nop2.drones(), program.name() + ": nop2 - after third step"); 
        } catch (Exception ex) {
            errors += 1;
            ex.printStackTrace();
        }
        
        // 0 Do not dispatch any drones.
        try {
            var program = compiler.compile("test.pure.0", "0<0>0", "");
            var nop1 = (NopStation) get(0, 0, program);
            var nop = (NopStation) get(2, 0, program);
            var nop2 = (NopStation) get(4, 0, program);
            
            program.reset();
            assertEquals(0, nop.drones(), program.name() + ": 0 - after reset"); 
            assertEquals(0, nop1.drones(), program.name() + ": nop1 - after reset"); 
            assertEquals(0, nop2.drones(), program.name() + ": nop2 - after reset"); 
            program.step();
            assertEquals(0, nop.drones(), program.name() + ": 0 - after first step"); 
            assertEquals(0, nop1.drones(), program.name() + ": nop1 - after first step"); 
            assertEquals(0, nop2.drones(), program.name() + ": nop2 - after first step"); 
            nop.receive(3);
            program.step();
            assertEquals(0, nop.drones(), program.name() + ": 0 - after second step"); 
            assertEquals(0, nop1.drones(), program.name() + ": nop1 - after second step"); 
            assertEquals(0, nop2.drones(), program.name() + ": nop2 - after second step"); 
        } catch (Exception ex) {
            errors += 1;
            ex.printStackTrace();
        }
        
        // O Dispatch N // K drones to each linked station, where N is the number of drones occupying 
        //   this station, K is the number of linked stations, and // is the floor division operation.
        try {
            var program = compiler.compile("test.pure.O", "0<O>0", "");
            var nop1 = (NopStation) get(0, 0, program);
            var split = (SplitStation) get(2, 0, program);
            var nop2 = (NopStation) get(4, 0, program);
            
            program.reset();
            assertEquals(0, split.drones(), program.name() + ": O - after reset"); 
            assertEquals(0, nop1.drones(), program.name() + ": nop1 - after reset"); 
            assertEquals(0, nop2.drones(), program.name() + ": nop2 - after reset"); 
            program.step();
            assertEquals(0, split.drones(), program.name() + ": O - after first step"); 
            assertEquals(0, nop1.drones(), program.name() + ": nop1 - after first step"); 
            assertEquals(0, nop2.drones(), program.name() + ": nop2 - after first step"); 
            split.receive(5);
            program.step();
            assertEquals(0, split.drones(), program.name() + ": O - after second step"); 
            assertEquals(2, nop1.drones(), program.name() + ": nop1 - after second step"); 
            assertEquals(2, nop2.drones(), program.name() + ": nop2 - after second step"); 
            program.step();
            assertEquals(0, split.drones(), program.name() + ": O - after third step"); 
            assertEquals(0, nop1.drones(), program.name() + ": nop1 - after third step"); 
            assertEquals(0, nop2.drones(), program.name() + ": nop2 - after third step"); 
        } catch (Exception ex) {
            errors += 1;
            ex.printStackTrace();
        }
        
        // Q If this station is occupied by N drones, dispatch N - 1 drones to linked stations.
        try {
            var program = compiler.compile("test.pure.Q", "0<Q>0", "");
            var nop1 = (NopStation) get(0, 0, program);
            var dec = (DecStation) get(2, 0, program);
            var nop2 = (NopStation) get(4, 0, program);
            
            program.reset();
            assertEquals(0, dec.drones(), program.name() + ": Q - after reset"); 
            assertEquals(0, nop1.drones(), program.name() + ": nop1 - after reset"); 
            assertEquals(0, nop2.drones(), program.name() + ": nop2 - after reset"); 
            program.step();
            assertEquals(0, dec.drones(), program.name() + ": Q - after first step"); 
            assertEquals(0, nop1.drones(), program.name() + ": nop1 - after first step"); 
            assertEquals(0, nop2.drones(), program.name() + ": nop2 - after first step"); 
            dec.receive(5);
            program.step();
            assertEquals(0, dec.drones(), program.name() + ": Q - after second step"); 
            assertEquals(4, nop1.drones(), program.name() + ": nop1 - after second step"); 
            assertEquals(4, nop2.drones(), program.name() + ": nop2 - after second step"); 
            program.step();
            assertEquals(0, dec.drones(), program.name() + ": Q - after third step"); 
            assertEquals(0, nop1.drones(), program.name() + ": nop1 - after third step"); 
            assertEquals(0, nop2.drones(), program.name() + ": nop2 - after third step"); 
        } catch (Exception ex) {
            errors += 1;
            ex.printStackTrace();
        }
        
        // @  If this station is not occupied, dispatch one drone to each linked station.
        try {
            var program = compiler.compile("test.pure.@", "0<@>0", "");
            var nop1 = (NopStation) get(0, 0, program);
            var create = (CreateStation) get(2, 0, program);
            var nop2 = (NopStation) get(4, 0, program);
            
            program.reset();
            assertEquals(0, create.drones(), program.name() + ": @ - after reset"); 
            assertEquals(0, nop1.drones(), program.name() + ": nop1 - after reset"); 
            assertEquals(0, nop2.drones(), program.name() + ": nop2 - after reset"); 
            program.step();
            assertEquals(0, create.drones(), program.name() + ": @ - after first step"); 
            assertEquals(1, nop1.drones(), program.name() + ": nop1 - after first step"); 
            assertEquals(1, nop2.drones(), program.name() + ": nop2 - after first step"); 
            create.receive(1);
            program.step();
            assertEquals(0, create.drones(), program.name() + ": @ - after second step"); 
            assertEquals(0, nop1.drones(), program.name() + ": nop1 - after second step"); 
            assertEquals(0, nop2.drones(), program.name() + ": nop2 - after second step"); 
            create.receive(3);
            program.step();
            assertEquals(0, create.drones(), program.name() + ": @ - after third step"); 
            assertEquals(0, nop1.drones(), program.name() + ": nop1 - after third step"); 
            assertEquals(0, nop2.drones(), program.name() + ": nop2 - after third step"); 
        } catch (Exception ex) {
            errors += 1;
            ex.printStackTrace();
        }
    }

    private void tunnel() {
        // A tunnel may be horizontal, vertical or diagonal, using -, |, /, or \ symbols
        try {
            var code = "@---0";
            var program = compiler.compile("test.tunnel.horizontal", code, "");
            var create = (CreateStation) get(0, 0, program);
            var nop = (NopStation) get(4, 0, program);
            
            program.reset();
            assertEquals(0, create.drones(), program.name() + ": @ - after reset"); 
            assertEquals(0, nop.drones(), program.name() + ": nop - after reset"); 
            program.step();
            assertEquals(0, create.drones(), program.name() + ": @ - after first step"); 
            assertEquals(1, nop.drones(), program.name() + ": nop - after first step"); 
            program.step();
            assertEquals(0, create.drones(), program.name() + ": @ - after second step"); 
            assertEquals(1, nop.drones(), program.name() + ": nop - after second step"); 
            create.receive(1);
            program.step();
            assertEquals(0, create.drones(), program.name() + ": @ - after third step"); 
            assertEquals(0, nop.drones(), program.name() + ": nop - after third step"); 
        } catch (Exception ex) {
            errors += 1;
            ex.printStackTrace();
        }

        try {
            var code = """
                @
                |
                |
                0
                """;
            var program = compiler.compile("test.tunnel.vertical", code, "");
            var create = (CreateStation) get(0, 0, program);
            var nop = (NopStation) get(0, 3, program);
            
            program.reset();
            assertEquals(0, create.drones(), program.name() + ": @ - after reset"); 
            assertEquals(0, nop.drones(), program.name() + ": nop - after reset"); 
            program.step();
            assertEquals(0, create.drones(), program.name() + ": @ - after first step"); 
            assertEquals(1, nop.drones(), program.name() + ": nop - after first step"); 
            program.step();
            assertEquals(0, create.drones(), program.name() + ": @ - after second step"); 
            assertEquals(1, nop.drones(), program.name() + ": nop - after second step"); 
            create.receive(1);
            program.step();
            assertEquals(0, create.drones(), program.name() + ": @ - after third step"); 
            assertEquals(0, nop.drones(), program.name() + ": nop - after third step"); 
        } catch (Exception ex) {
            errors += 1;
            ex.printStackTrace();
        }
        
        try {
            var code = """
                   @
                  / \\
                 /   \\
                0     0
                """;
            var program = compiler.compile("test.tunnel.diagonal", code, "");
            var create = (CreateStation) get(3, 0, program);
            var nop1 = (NopStation) get(0, 3, program);
            var nop2 = (NopStation) get(6, 3, program);
            
            program.reset();
            assertEquals(0, create.drones(), program.name() + ": @ - after reset"); 
            assertEquals(0, nop1.drones(), program.name() + ": nop1 - after reset"); 
            assertEquals(0, nop2.drones(), program.name() + ": nop2 - after reset"); 
            program.step();
            assertEquals(0, create.drones(), program.name() + ": @ - after first step"); 
            assertEquals(1, nop1.drones(), program.name() + ": nop1 - after first step"); 
            assertEquals(1, nop2.drones(), program.name() + ": nop2 - after first step"); 
            program.step();
            assertEquals(0, create.drones(), program.name() + ": @ - after second step"); 
            assertEquals(1, nop1.drones(), program.name() + ": nop1 - after second step"); 
            assertEquals(1, nop2.drones(), program.name() + ": nop2 - after second step"); 
            create.receive(1);
            program.step();
            assertEquals(0, create.drones(), program.name() + ": @ - after third step"); 
            assertEquals(0, nop1.drones(), program.name() + ": nop1 - after third step"); 
            assertEquals(0, nop2.drones(), program.name() + ": nop2 - after third step"); 
        } catch (Exception ex) {
            errors += 1;
            ex.printStackTrace();
        }
    }
    
    private void tunnelCrossing() {
        // A + behaves as both a horizontal and a vertical tunnel, ...
        try {
            var code = """
                   @
                   |
                @--+--0
                   |
                   0
                """;
            var program = compiler.compile("test.tunnel.+", code, "");
            var create1 = (CreateStation) get(3, 0, program);
            var create2 = (CreateStation) get(0, 2, program);
            var nop1 = (NopStation) get(3, 4, program);
            var nop2 = (NopStation) get(6, 2, program);
            
            program.reset();
            assertEquals(0, create1.drones(), program.name() + ": @1 - after reset"); 
            assertEquals(0, create2.drones(), program.name() + ": @2 - after reset"); 
            assertEquals(0, nop1.drones(), program.name() + ": nop1 - after reset"); 
            assertEquals(0, nop2.drones(), program.name() + ": nop2 - after reset"); 
            program.step();
            assertEquals(0, create1.drones(), program.name() + ": @1 - after first step"); 
            assertEquals(0, create2.drones(), program.name() + ": @2 - after reset"); 
            assertEquals(1, nop1.drones(), program.name() + ": nop1 - after first step"); 
            assertEquals(1, nop2.drones(), program.name() + ": nop2 - after first step"); 
            program.step();
            assertEquals(0, create1.drones(), program.name() + ": @1 - after second step"); 
            assertEquals(0, create2.drones(), program.name() + ": @2 - after reset"); 
            assertEquals(1, nop1.drones(), program.name() + ": nop1 - after second step"); 
            assertEquals(1, nop2.drones(), program.name() + ": nop2 - after second step"); 
            create1.receive(1);
            program.step();
            assertEquals(0, create1.drones(), program.name() + ": @1 - after third step"); 
            assertEquals(0, create2.drones(), program.name() + ": @2 - after reset"); 
            assertEquals(0, nop1.drones(), program.name() + ": nop1 - after third step"); 
            assertEquals(1, nop2.drones(), program.name() + ": nop2 - after third step"); 
            create2.receive(1);
            program.step();
            assertEquals(0, create1.drones(), program.name() + ": @1 - after third step"); 
            assertEquals(0, create2.drones(), program.name() + ": @2 - after reset"); 
            assertEquals(1, nop1.drones(), program.name() + ": nop1 - after third step"); 
            assertEquals(0, nop2.drones(), program.name() + ": nop2 - after third step"); 
        } catch (Exception ex) {
            errors += 1;
            ex.printStackTrace();
        }
        // ... a X behaves as both diagonal tunnels, ...
        try {
            var code = """
                @   0
                 \\ /
                  X
                 / \\
                @   0
                """;
            var program = compiler.compile("test.tunnel.X", code, "");
            var create1 = (CreateStation) get(0, 0, program);
            var create2 = (CreateStation) get(0, 4, program);
            var nop1 = (NopStation) get(4, 4, program);
            var nop2 = (NopStation) get(4, 0, program);
            
            program.reset();
            assertEquals(0, create1.drones(), program.name() + ": @1 - after reset"); 
            assertEquals(0, create2.drones(), program.name() + ": @2 - after reset"); 
            assertEquals(0, nop1.drones(), program.name() + ": nop1 - after reset"); 
            assertEquals(0, nop2.drones(), program.name() + ": nop2 - after reset"); 
            program.step();
            assertEquals(0, create1.drones(), program.name() + ": @1 - after first step"); 
            assertEquals(0, create2.drones(), program.name() + ": @2 - after first step"); 
            assertEquals(1, nop1.drones(), program.name() + ": nop1 - after first step"); 
            assertEquals(1, nop2.drones(), program.name() + ": nop2 - after first step"); 
            program.step();
            assertEquals(0, create1.drones(), program.name() + ": @1 - after second step"); 
            assertEquals(0, create2.drones(), program.name() + ": @2 - after second step"); 
            assertEquals(1, nop1.drones(), program.name() + ": nop1 - after second step"); 
            assertEquals(1, nop2.drones(), program.name() + ": nop2 - after second step"); 
            create1.receive(1);
            program.step();
            assertEquals(0, create1.drones(), program.name() + ": @1 - after third step"); 
            assertEquals(0, create2.drones(), program.name() + ": @2 - after third step"); 
            assertEquals(0, nop1.drones(), program.name() + ": nop1 - after third step"); 
            assertEquals(1, nop2.drones(), program.name() + ": nop2 - after third step"); 
            create2.receive(1);
            program.step();
            assertEquals(0, create1.drones(), program.name() + ": @1 - after fourth step"); 
            assertEquals(0, create2.drones(), program.name() + ": @2 - after fourth step"); 
            assertEquals(1, nop1.drones(), program.name() + ": nop1 - after fourth step"); 
            assertEquals(0, nop2.drones(), program.name() + ": nop2 - after fourth step"); 
        } catch (Exception ex) {
            errors += 1;
            ex.printStackTrace();
        }
        // ... and * behaves as any tunnel.
        try {
            var code = """
                @ 0 @
                 \\|/
                0-*-@
                 /|\\
                0 @ 0
                """;
            var program = compiler.compile("test.tunnel.*", code, "");
            var create1 = (CreateStation) get(0, 0, program);
            var create2 = (CreateStation) get(4, 0, program);
            var create3 = (CreateStation) get(4, 2, program);
            var create4 = (CreateStation) get(2, 4, program);
            var nop1 = (NopStation) get(4, 4, program);
            var nop2 = (NopStation) get(0, 4, program);
            var nop3 = (NopStation) get(0, 2, program);
            var nop4 = (NopStation) get(2, 0, program);
            
            program.reset();
            assertEquals(0, create1.drones(), program.name() + ": @1 - after reset"); 
            assertEquals(0, create2.drones(), program.name() + ": @2 - after reset"); 
            assertEquals(0, create3.drones(), program.name() + ": @3 - after reset"); 
            assertEquals(0, create4.drones(), program.name() + ": @4 - after reset"); 
            assertEquals(0, nop1.drones(), program.name() + ": nop1 - after reset"); 
            assertEquals(0, nop2.drones(), program.name() + ": nop2 - after reset"); 
            assertEquals(0, nop3.drones(), program.name() + ": nop3 - after reset"); 
            assertEquals(0, nop4.drones(), program.name() + ": nop4 - after reset"); 
            program.step();
            assertEquals(0, create1.drones(), program.name() + ": @1 - after first step"); 
            assertEquals(0, create2.drones(), program.name() + ": @2 - after first step"); 
            assertEquals(0, create3.drones(), program.name() + ": @3 - after first step"); 
            assertEquals(0, create4.drones(), program.name() + ": @4 - after first step"); 
            assertEquals(1, nop1.drones(), program.name() + ": nop1 - after first step"); 
            assertEquals(1, nop2.drones(), program.name() + ": nop2 - after first step"); 
            assertEquals(1, nop3.drones(), program.name() + ": nop3 - after first step"); 
            assertEquals(1, nop4.drones(), program.name() + ": nop4 - after first step"); 
            program.step();
            assertEquals(0, create1.drones(), program.name() + ": @1 - after second step"); 
            assertEquals(0, create2.drones(), program.name() + ": @2 - after second step"); 
            assertEquals(0, create3.drones(), program.name() + ": @3 - after second step"); 
            assertEquals(0, create4.drones(), program.name() + ": @4 - after second step"); 
            assertEquals(1, nop1.drones(), program.name() + ": nop1 - after second step"); 
            assertEquals(1, nop2.drones(), program.name() + ": nop2 - after second step"); 
            assertEquals(1, nop3.drones(), program.name() + ": nop3 - after second step"); 
            assertEquals(1, nop4.drones(), program.name() + ": nop4 - after second step"); 
            create1.receive(1);
            program.step();
            assertEquals(0, create1.drones(), program.name() + ": @1 - after third step"); 
            assertEquals(0, create2.drones(), program.name() + ": @2 - after thrid step"); 
            assertEquals(0, create3.drones(), program.name() + ": @3 - after third step"); 
            assertEquals(0, create4.drones(), program.name() + ": @4 - after third step"); 
            assertEquals(0, nop1.drones(), program.name() + ": nop1 - after third step"); 
            assertEquals(1, nop2.drones(), program.name() + ": nop2 - after third step"); 
            assertEquals(1, nop3.drones(), program.name() + ": nop3 - after third step"); 
            assertEquals(1, nop4.drones(), program.name() + ": nop4 - after third step"); 
            create2.receive(1);
            program.step();
            assertEquals(0, create1.drones(), program.name() + ": @1 - after fourth step"); 
            assertEquals(0, create2.drones(), program.name() + ": @2 - after fourth step"); 
            assertEquals(0, create3.drones(), program.name() + ": @3 - after fourth step"); 
            assertEquals(0, create4.drones(), program.name() + ": @4 - after fourth step"); 
            assertEquals(1, nop1.drones(), program.name() + ": nop1 - after fourth step"); 
            assertEquals(0, nop2.drones(), program.name() + ": nop2 - after fourth step"); 
            assertEquals(1, nop3.drones(), program.name() + ": nop3 - after fourth step"); 
            assertEquals(1, nop4.drones(), program.name() + ": nop4 - after fourth step"); 
            create3.receive(1);
            program.step();
            assertEquals(0, create1.drones(), program.name() + ": @1 - after 5th step"); 
            assertEquals(0, create2.drones(), program.name() + ": @2 - after 5th step"); 
            assertEquals(0, create3.drones(), program.name() + ": @3 - after 5th step"); 
            assertEquals(0, create4.drones(), program.name() + ": @4 - after 5th step"); 
            assertEquals(1, nop1.drones(), program.name() + ": nop1 - after 5th step"); 
            assertEquals(1, nop2.drones(), program.name() + ": nop2 - after 5th step"); 
            assertEquals(0, nop3.drones(), program.name() + ": nop3 - after 5th step"); 
            assertEquals(1, nop4.drones(), program.name() + ": nop4 - after 5th step"); 
            create4.receive(1);
            program.step();
            assertEquals(0, create1.drones(), program.name() + ": @1 - after 6th step"); 
            assertEquals(0, create2.drones(), program.name() + ": @2 - after 6th step"); 
            assertEquals(0, create3.drones(), program.name() + ": @3 - after 6th step"); 
            assertEquals(0, create4.drones(), program.name() + ": @4 - after 6th step"); 
            assertEquals(1, nop1.drones(), program.name() + ": nop1 - after 6th step"); 
            assertEquals(1, nop2.drones(), program.name() + ": nop2 - after 6th step"); 
            assertEquals(1, nop3.drones(), program.name() + ": nop3 - after 6th step"); 
            assertEquals(0, nop4.drones(), program.name() + ": nop4 - after 6th step"); 
        } catch (Exception ex) {
            errors += 1;
            ex.printStackTrace();
        }
    }
    
    private void aperture() {
        // Horizontal and vertical apertures are marked with >, ...
        try {
            var code = "0-->@-->0";
            var program = compiler.compile("test.aperture.>", code, "");
            var nop1 = (NopStation) get(0, 0, program);
            var create1 = (CreateStation) get(4, 0, program);
            var nop2 = (NopStation) get(8, 0, program);
            
            program.reset();
            assertEquals(0, nop1.drones(), program.name() + ": nop1 - after reset"); 
            assertEquals(0, create1.drones(), program.name() + ": @1 - after reset"); 
            assertEquals(0, nop2.drones(), program.name() + ": nop2 - after reset"); 
            program.step();
            assertEquals(0, nop1.drones(), program.name() + ": nop1 - after first step"); 
            assertEquals(0, create1.drones(), program.name() + ": @1 - after first step"); 
            assertEquals(1, nop2.drones(), program.name() + ": nop2 - after first step"); 
            program.step();
            assertEquals(0, nop1.drones(), program.name() + ": nop1 - after second step"); 
            assertEquals(0, create1.drones(), program.name() + ": @1 - after second step"); 
            assertEquals(1, nop2.drones(), program.name() + ": nop2 - after second step"); 
        } catch (Exception ex) {
            errors += 1;
            ex.printStackTrace();
        }
        // ... ^, ...
        try {
            var code = """
                0
                ^
                |
                @
                ^
                |
                0
                """;
            var program = compiler.compile("test.aperture.^", code, "");
            var nop1 = (NopStation) get(0, 0, program);
            var create1 = (CreateStation) get(0, 3, program);
            var nop2 = (NopStation) get(0, 6, program);
            
            program.reset();
            assertEquals(0, nop1.drones(), program.name() + ": nop1 - after reset"); 
            assertEquals(0, create1.drones(), program.name() + ": @1 - after reset"); 
            assertEquals(0, nop2.drones(), program.name() + ": nop2 - after reset"); 
            program.step();
            assertEquals(1, nop1.drones(), program.name() + ": nop1 - after first step"); 
            assertEquals(0, create1.drones(), program.name() + ": @1 - after first step"); 
            assertEquals(0, nop2.drones(), program.name() + ": nop2 - after first step"); 
            program.step();
            assertEquals(1, nop1.drones(), program.name() + ": nop1 - after second step"); 
            assertEquals(0, create1.drones(), program.name() + ": @1 - after second step"); 
            assertEquals(0, nop2.drones(), program.name() + ": nop2 - after second step"); 
        } catch (Exception ex) {
            errors += 1;
            ex.printStackTrace();
        }
        // ... <, ...
        try {
            var code = "0<--@<--0";
            var program = compiler.compile("test.aperture.<", code, "");
            var nop1 = (NopStation) get(0, 0, program);
            var create1 = (CreateStation) get(4, 0, program);
            var nop2 = (NopStation) get(8, 0, program);
            
            program.reset();
            assertEquals(0, nop1.drones(), program.name() + ": nop1 - after reset"); 
            assertEquals(0, create1.drones(), program.name() + ": @1 - after reset"); 
            assertEquals(0, nop2.drones(), program.name() + ": nop2 - after reset"); 
            program.step();
            assertEquals(1, nop1.drones(), program.name() + ": nop1 - after first step"); 
            assertEquals(0, create1.drones(), program.name() + ": @1 - after first step"); 
            assertEquals(0, nop2.drones(), program.name() + ": nop2 - after first step"); 
            program.step();
            assertEquals(1, nop1.drones(), program.name() + ": nop1 - after second step"); 
            assertEquals(0, create1.drones(), program.name() + ": @1 - after second step"); 
            assertEquals(0, nop2.drones(), program.name() + ": nop2 - after second step"); 
        } catch (Exception ex) {
            errors += 1;
            ex.printStackTrace();
        }
        // ... or v
        try {
            var code = """
                0
                |
                v
                @
                |
                v
                0
                """;
            var program = compiler.compile("test.aperture.v", code, "");
            var nop1 = (NopStation) get(0, 0, program);
            var create1 = (CreateStation) get(0, 3, program);
            var nop2 = (NopStation) get(0, 6, program);
            
            program.reset();
            assertEquals(0, nop1.drones(), program.name() + ": nop1 - after reset"); 
            assertEquals(0, create1.drones(), program.name() + ": @1 - after reset"); 
            assertEquals(0, nop2.drones(), program.name() + ": nop2 - after reset"); 
            program.step();
            assertEquals(0, nop1.drones(), program.name() + ": nop1 - after first step"); 
            assertEquals(0, create1.drones(), program.name() + ": @1 - after first step"); 
            assertEquals(1, nop2.drones(), program.name() + ": nop2 - after first step"); 
            program.step();
            assertEquals(0, nop1.drones(), program.name() + ": nop1 - after second step"); 
            assertEquals(0, create1.drones(), program.name() + ": @1 - after second step"); 
            assertEquals(1, nop2.drones(), program.name() + ": nop2 - after second step"); 
        } catch (Exception ex) {
            errors += 1;
            ex.printStackTrace();
        }
        // Diagonal apertures are all marked with #
        try {
            var code = """
                   @
                  / #
                 #   \\
                0     0
                """;
            var program = compiler.compile("test.aperture.v", code, "");
            var nop1 = (NopStation) get(0, 3, program);
            var create1 = (CreateStation) get(3, 0, program);
            var nop2 = (NopStation) get(6, 3, program);
            
            program.reset();
            assertEquals(0, nop1.drones(), program.name() + ": nop1 - after reset"); 
            assertEquals(0, create1.drones(), program.name() + ": @1 - after reset"); 
            assertEquals(0, nop2.drones(), program.name() + ": nop2 - after reset"); 
            program.step();
            assertEquals(1, nop1.drones(), program.name() + ": nop1 - after first step"); 
            assertEquals(0, create1.drones(), program.name() + ": @1 - after first step"); 
            assertEquals(0, nop2.drones(), program.name() + ": nop2 - after first step"); 
            program.step();
            assertEquals(1, nop1.drones(), program.name() + ": nop1 - after second step"); 
            assertEquals(0, create1.drones(), program.name() + ": @1 - after second step"); 
            assertEquals(0, nop2.drones(), program.name() + ": nop2 - after second step"); 
        } catch (Exception ex) {
            errors += 1;
            ex.printStackTrace();
        }
    }
    
    private void boundStation() {
        // A bound station executes the behavior of all its child stations each tick, ...
        try {
            var code = "@@@->0";
            var program = compiler.compile("test.bound.all1", code, "");
            var nop1 = (NopStation) get(5, 0, program);
            
            program.reset();
            assertEquals(0, nop1.drones(), program.name() + ": nop1 - after reset"); 
            program.step();
            assertEquals(3, nop1.drones(), program.name() + ": nop1 - after step"); 
        } catch (Exception ex) {
            errors += 1;
            ex.printStackTrace();
        }

        try {
            var code = "@@@->o@->0";
            var program = compiler.compile("test.bound.all2", code, "");
            var nop1 = (NopStation) get(9, 0, program);
            
            program.reset();
            assertEquals(0, nop1.drones(), program.name() + ": nop1 - after reset"); 
            program.step();
            assertEquals(1, nop1.drones(), program.name() + ": nop1 - after first step"); 
            program.step();
            assertEquals(3, nop1.drones(), program.name() + ": nop1 - after second step"); 
        } catch (Exception ex) {
            errors += 1;
            ex.printStackTrace();
        }
        // ...and is linked to each station its child stations are linked to
        try {
            var code = "0<-0@@@0->0";
            var program = compiler.compile("test.bound.link", code, "");
            var nop1 = (NopStation) get(0, 0, program);
            var nop2 = (NopStation) get(10, 0, program);
            
            program.reset();
            assertEquals(0, nop1.drones(), program.name() + ": nop1 - after reset"); 
            assertEquals(0, nop2.drones(), program.name() + ": nop2 - after reset"); 
            program.step();
            assertEquals(3, nop1.drones(), program.name() + ": nop1 - after step"); 
            assertEquals(3, nop2.drones(), program.name() + ": nop2 - after step"); 
        } catch (Exception ex) {
            errors += 1;
            ex.printStackTrace();
        }
        // A bound station may be linked to itself in certain configurations.
        try {
            var code = """
                0<-@>0
                    0
                """;
            var program = compiler.compile("test.bound.recursive", code, "");
            var nop1 = (NopStation) get(0, 0, program);
            
            program.reset();
            assertEquals(0, nop1.drones(), program.name() + ": nop1 - after reset"); 
            program.step();
            assertEquals(1, nop1.drones(), program.name() + ": nop1 - after first step"); 
            program.step();
            assertEquals(0, nop1.drones(), program.name() + ": nop1 - after second step"); 
            program.step();
            assertEquals(1, nop1.drones(), program.name() + ": nop1 - after second step"); 
        } catch (Exception ex) {
            errors += 1;
            ex.printStackTrace();
        }
    }
    
    private void syntax() {
        // Anything contained between inside parentheses () is a bubble and is ignored.
        try {
            var code = "( 0 )";
            var program = compiler.compile("test.syntax.bubble", code, "");
            
            assertEquals(0, program.stations().size(), "no stations");
        } catch (Exception ex) {
            errors += 1;
            ex.printStackTrace();
        }
        // Anything contained inside double parentheses (()) is a lens, used for synthetic station definitions.
        try {
            var code = """
                0<-[-A]
                   [==]
                ((A=7))
                """;
            var program = compiler.compile("test.syntax.lens", code, "");
            var nop1 = (NopStation) get(0, 0, program);
            
            program.reset();
            assertEquals(0, nop1.drones(), program.name() + ": nop1 - after reset"); 
            program.step();
            assertEquals(7, nop1.drones(), program.name() + ": nop1 - after step"); 
        } catch (Exception ex) {
            errors += 1;
            ex.printStackTrace();
        }
    }
    
    private void exclusionZone() {
        // TODO
    }
    
    private Single get(int x, int y, Program program) {
        return get(Single.class, x, y, program);
    }
    
    private <T extends Single> T get(Class<T> type, int x, int y, Program program) {
        Predicate<Single> posFilter = s -> s.pos().x() == x && s.pos().y() == y;
        return 
            program.stations()
            .stream()
            .filter(type::isInstance)
            .map(type::cast)
            .filter(posFilter)
            .findFirst()
            .get();
    }
    
    private <T extends Station> T get(Class<T> type, Program program) {
        return 
            program.stations()
            .stream()
            .filter(type::isInstance)
            .map(type::cast)
            .findFirst()
            .get();
    }
    
    private void assertEquals(int expected, int actual, String message) throws Exception {
        if (actual != expected) {
            throw new Exception(String.format("expected: %d, actual: %d, %s", expected, actual, message));
        }
    }
    
    ////////////////////////////////////////////////////////////////////////////////////////////////////
    
    private static class TestOutput implements Output {
        @Override
        public void reset() {
            //
        }
        @Override
        public void write(String text) {
            //
        }
        @Override
        public void write(int b) {
            //
        }
    }
}
