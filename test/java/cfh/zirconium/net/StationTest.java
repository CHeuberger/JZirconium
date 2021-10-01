package cfh.zirconium.net;

import java.util.Collection;
import java.util.LinkedList;
import java.util.Objects;
import java.util.Queue;
import java.util.function.Predicate;
import java.util.stream.Stream;

import cfh.zirconium.Compiler;
import cfh.zirconium.Compiler.CompileException;
import cfh.zirconium.Environment;
import cfh.zirconium.Program;
import cfh.zirconium.Environment.Input;
import cfh.zirconium.Environment.Output;
import cfh.zirconium.Environment.Printer;

public class StationTest {
    
    private static final boolean strictZone = false;

    public static void main(String[] args) {
        var test = new StationTest();
        test.pureStations();
        test.tunnel();
        test.tunnelCrossing();
        test.aperture();
        test.boundStation();
        test.syntax();
        test.exclusionZone();
        test.defectStation();
        test.metropolis();
        test.syntheticStation();
        test.zoneInference();
        if (test.errors == 0) {
            System.out.println("\nOK");
        } else {
            System.err.printf("%nerrors: %d%n", test.errors);
        }
    }

    private int errors;
    private final PrinterMock printer;
    private final InputMock input;
    private final OutputMock output;
    private final OutputMock error;
    private final Environment environment;
    private final Compiler compiler;
    
    private StationTest() {
        errors = 0;
        printer = new PrinterMock();
        input = new InputMock();
        output = new OutputMock();
        error = new OutputMock();
        environment = new Environment(printer, input, output, error);
        compiler = new Compiler(environment);
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
            program.step();  // init
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
            program.step();  // init
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
            program.step();  // init
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
            program.step();  // init
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
            program.step();  // init
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
            program.step();  // init
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
            program.step();  // init
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
            program.step();  // init
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
            program.step();  // init
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
            program.step();  // init
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
            program.step();  // init
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
            program.step();  // init
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
            program.step();  // init
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
            program.step();  // init
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
            program.step();  // init
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
            program.step();  // init
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
            program.step();  // init
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
            program.step();  // init
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
            program.step();  // init
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
            program.step();  // init
            assertEquals(3, nop1.drones(), program.name() + ": nop1 - after step");
            assertEquals(3, nop2.drones(), program.name() + ": nop2 - after step");
        } catch (Exception ex) {
            errors += 1;
            ex.printStackTrace();
        }
        try {
            var code = "0<-[-0©0-]->0";
            var program = compiler.compile("test.bound.link.synthetic", code, "©=K2*");
            var nop1 = (NopStation) get(0, 0, program);
            var nop2 = (NopStation) get(12, 0, program);
            
            program.reset();
            assertEquals(0, nop1.drones(), program.name() + ": nop1 - after reset");
            assertEquals(0, nop2.drones(), program.name() + ": nop2 - after reset");
            program.step();  // init
            assertEquals(4, nop1.drones(), program.name() + ": nop1 - after step");
            assertEquals(4, nop2.drones(), program.name() + ": nop2 - after step");
        } catch (Exception ex) {
            errors += 1;
            ex.printStackTrace();
        }
        try {
            var code = """
                   o
                   v
                0<0O0>0
                   v
                   0
                """;
            var program = compiler.compile("test.bound.link.O", code, "");
            var dup1 = (DupStation) get(3, 0, program);
            var split1 = (SplitStation) get(3, 2, program);
            var nop1 = (NopStation) get(0, 2, program);
            var nop2 = (NopStation) get(6, 2, program);
            var nop3 = (NopStation) get(3, 4, program);
            
            program.reset();
            assertEquals(0, dup1.drones(), program.name() + ": dup1 - after reset");
            assertEquals(0, split1.drones(), program.name() + ": split1 - after reset");
            assertEquals(0, nop1.drones(), program.name() + ": nop1 - after reset");
            assertEquals(0, nop2.drones(), program.name() + ": nop2 - after reset");
            assertEquals(0, nop3.drones(), program.name() + ": nop3 - after reset");
            program.step();  // init
            assertEquals(0, dup1.drones(), program.name() + ": dup1 - after first step");
            assertEquals(0, split1.drones(), program.name() + ": split1 - after first step");
            assertEquals(0, nop1.drones(), program.name() + ": nop1 - after first step");
            assertEquals(0, nop2.drones(), program.name() + ": nop2 - after first step");
            assertEquals(0, nop3.drones(), program.name() + ": nop3 - after first step");
            dup1.receive(15);
            program.step();
            assertEquals(0, dup1.drones(), program.name() + ": dup1 - after second step");
            assertEquals(15, split1.drones(), program.name() + ": split1 - after second step");
            assertEquals(0, nop1.drones(), program.name() + ": nop1 - after second step");
            assertEquals(0, nop2.drones(), program.name() + ": nop2 - after second step");
            assertEquals(0, nop3.drones(), program.name() + ": nop3 - after second step");
            program.step();
            assertEquals(0, dup1.drones(), program.name() + ": dup1 - after third step");
            assertEquals(0, split1.drones(), program.name() + ": split1 - after third step");
            assertEquals(5, nop1.drones(), program.name() + ": nop1 - after third step");
            assertEquals(5, nop2.drones(), program.name() + ": nop2 - after third step");
            assertEquals(5, nop3.drones(), program.name() + ": nop3 - after third step");
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
            program.step();  // init
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
                0<-[-©]
                   [==]
                ((©=7))
                """;
            var program = compiler.compile("test.syntax.lens", code, "");
            var nop1 = (NopStation) get(0, 0, program);
            
            program.reset();
            assertEquals(0, nop1.drones(), program.name() + ": nop1 - after reset");
            program.step();  // init
            assertEquals(7, nop1.drones(), program.name() + ": nop1 - after step");
        } catch (Exception ex) {
            errors += 1;
            ex.printStackTrace();
        }
    }
    
    private void exclusionZone() {
        if (strictZone) {
            // A position must be fully enclosed by fences for it to be considered "inside" the exclusion zone.
            try {
                var code = """
                    {~~~}
                    { ? }
                    {===}
                    """;
                var program = compiler.compile("test.exclusion.enclosed", code, "");
                
                errors += 1;
                System.err.printf("%s: expected Exception - zone not fully enclosed by fences%n", program.name());
                Thread.dumpStack();
            } catch (Exception ex) {
                // expected
            }
            // The fences of an exclusion zone will behave as * tunnels.
            try {
                var code = """
                       {~~}
                    @->{0 }
                       {==}
                    """;
                var program = compiler.compile("test.exclusion.arrow-fence", code, "");
                
                errors += 1;
                System.err.printf("%s: expected Exception - arrow before fence%n", program.name());
                Thread.dumpStack();
            } catch (Exception ex) {
                // expected
            }
        }
        
        // The fences of an exclusion zone will behave as * tunnels.
        try {
            var code = """
                  {~~~}
                @-{-0 }
                  {~~~}
                """;
            var program = compiler.compile("test.exclusion.*.1", code, "");
            var create1 = (CreateStation) get(0, 1, program);
            var nop1 = (NopStation) get(4, 1, program);
            
            program.reset();
            assertEquals(0, create1.drones(), program.name() + ": create1 - after reset");
            assertEquals(0, nop1.drones(), program.name() + ": nop1 - after reset");
            program.step();  // init
            assertEquals(1, nop1.drones(), program.name() + ": nop1 - after step");
        } catch (Exception ex) {
            errors += 1;
            ex.printStackTrace();
        }
        try {
            var code = """
                  {~~~}
                  {0  }
                  {~~~}
                 /
                @
                """;
            var program = compiler.compile("test.exclusion.*.2", code, "");
            var create1 = (CreateStation) get(0, 4, program);
            var nop1 = (NopStation) get(3, 1, program);
            
            program.reset();
            assertEquals(0, create1.drones(), program.name() + ": create1 - after reset");
            assertEquals(0, nop1.drones(), program.name() + ": nop1 - after reset");
            program.step();  // init
            assertEquals(1, nop1.drones(), program.name() + ": nop1 - after step");
        } catch (Exception ex) {
            errors += 1;
            ex.printStackTrace();
        }
        try {
            var code = """
                {~~~}
                { 0 }
                {~~~}
                  |
                  @
                """;
            var program = compiler.compile("test.exclusion.*.3", code, "");
            var create1 = (CreateStation) get(2, 4, program);
            var nop1 = (NopStation) get(2, 1, program);
            
            program.reset();
            assertEquals(0, create1.drones(), program.name() + ": create1 - after reset");
            assertEquals(0, nop1.drones(), program.name() + ": nop1 - after reset");
            program.step();  // init
            assertEquals(1, nop1.drones(), program.name() + ": nop1 - after step");
        } catch (Exception ex) {
            errors += 1;
            ex.printStackTrace();
        }
        // An exclusion zone in a metropolis is still an exclusion zone.
        try {
            var code = """
                [=====]
                [ {~} ]
                [ {!} ]
                [ {~} ]
                [     ]
                [=====]
                """;
            var program = compiler.compile("test.exclusion.inside", code, "");
            get(HaltStation.class, 3, 2, program);
        } catch (Exception ex) {
            errors += 1;
            ex.printStackTrace();
        }
    }
    
    private void defectStation() {
        // ? If any drones occupy this, read one byte from STDIN and dispatch that many drones to linked stations. 
        try {
            var code = """
                {~~~}
                {?>0}
                {~~~}
                """;
            var program = compiler.compile("test.defect.?", code, "");
            var in1 = (ByteInStation) get(1, 1, program);
            var nop1 = (NopStation) get(3, 1, program);
            
            program.reset();
            assertEquals(0, nop1.drones(), program.name() + ": nop1 - after reset");
            in1.receive(1);
            program.step();  // init
            assertEquals(0, nop1.drones(), program.name() + ": nop1 - after first step");
            input.expectByte(3);
            in1.receive(1);
            program.step();
            assertEquals(3, nop1.drones(), program.name() + ": nop1 - after second step");
            input.expectByte(4);
            in1.receive(1);
            program.step();
            assertEquals(4, nop1.drones(), program.name() + ": nop1 - after third step");
            in1.receive(1);
            program.step();
            assertEquals(0, nop1.drones(), program.name() + ": nop1 - after fourth step");
        } catch (Exception ex) {
            errors += 1;
            ex.printStackTrace();
        }
        // ! If any drones occupy this, halt the program.  
        try {
            var code = """
                {~~~}
                { ! }
                {~~~}
                """;
            var program = compiler.compile("test.defect.!", code, "");
            var halt1 = (HaltStation) get(2, 1, program);
            
            program.reset();
            assertEquals(0, halt1.drones(), program.name() + ": halt1 - after reset");
            program.step();  // init
            assertEquals(0, halt1.drones(), program.name() + ": halt1 - after first step");
            assertEquals(false, environment.halted(), "halted");
            halt1.receive(1);
            program.step();
            assertEquals(true, environment.halted(), "halted");
        } catch (Exception ex) {
            errors += 1;
            ex.printStackTrace();
        }
        // % If any drones occupy this, print the number of drones occupying this station 
        //   as a byte modulo 256 to STDOUT
        try {
            var code = """
                {~~~}
                { % }
                {~~~}
                """;
            var program = compiler.compile("test.defect.%", code, "");
            var out1 = (ByteOutStation) get(2, 1, program);
            
            program.reset();
            assertEquals(0, out1.drones(), program.name() + ": out1 - after reset");
            assertEquals(true, output.isEmpty(), program.name() + ": output empty after reset");
            program.step();  // init
            assertEquals(0, out1.drones(), program.name() + ": out1 - after first step");
            assertEquals(true, output.isEmpty(), program.name() + ": output empty after first step");
            out1.receive(123);
            program.step();
            assertEquals(0, out1.drones(), program.name() + ": out1 - after second step");
            assertEquals(123, output.nextInt(), program.name() + ": output after second step");
            out1.receive(256 + 45);
            program.step();
            assertEquals(0, out1.drones(), program.name() + ": out1 - after third step");
            assertEquals(45, output.nextInt(), program.name() + ": output after third step");
            program.step();
            assertEquals(0, out1.drones(), program.name() + ": out1 - after fourth step");
            assertEquals(true, output.isEmpty(), program.name() + ": output empty after fourth step");
        } catch (Exception ex) {
            errors += 1;
            ex.printStackTrace();
        }
        // & If any drones occupy this, write the number of drones occupying this 
        //   as a byte modulo 256 to STDERR.
        try {
            var code = """
                {~~~}
                { & }
                {~~~}
                """;
            var program = compiler.compile("test.defect.&", code, "");
            var err1 = (ByteErrStation) get(2, 1, program);
            
            program.reset();
            assertEquals(0, err1.drones(), program.name() + ": err1 - after reset");
            assertEquals(true, error.isEmpty(), program.name() + ": error empty after reset");
            program.step();  // init
            assertEquals(0, err1.drones(), program.name() + ": err1 - after first step");
            assertEquals(true, error.isEmpty(), program.name() + ": error empty after first step");
            err1.receive(45);
            program.step();
            assertEquals(0, err1.drones(), program.name() + ": err1 - after second step");
            assertEquals(45, error.nextInt(), program.name() + ": error after second step");
            err1.receive(256 + 67);
            program.step();
            assertEquals(0, err1.drones(), program.name() + ": err1 - after third step");
            assertEquals(67, error.nextInt(), program.name() + ": error after third step");
            program.step();
            assertEquals(0, err1.drones(), program.name() + ": err1 - after fourth step");
            assertEquals(true, error.isEmpty(), program.name() + ": error empty after fourth step");
        } catch (Exception ex) {
            errors += 1;
            ex.printStackTrace();
        }
        // ` If any drones occupy this, write the number of drones occupying this station 
        //   in numeric form to STDOUT. 
        try {
            var code = """
                {~~~}
                { ` }
                {~~~}
                """;
            var program = compiler.compile("test.defect.`", code, "");
            var out1 = (NumOutStation) get(2, 1, program);
            
            program.reset();
            assertEquals(0, out1.drones(), program.name() + ": out1 - after reset");
            assertEquals(true, output.isEmpty(), program.name() + ": output empty after reset");
            program.step();  // init
            assertEquals(0, out1.drones(), program.name() + ": out1 - after first step");
            assertEquals(true, output.isEmpty(), program.name() + ": output empty after first step");
            out1.receive(123);
            program.step();
            assertEquals(0, out1.drones(), program.name() + ": out1 - after second step");
            assertEquals("123 ", output.nextString(), program.name() + ": output after second step");
            out1.receive(456);
            program.step();
            assertEquals(0, out1.drones(), program.name() + ": out1 - after third step");
            assertEquals("456 ", output.nextString(), program.name() + ": output after third step");
            program.step();
            assertEquals(0, out1.drones(), program.name() + ": out1 - after fourth step");
            assertEquals(true, output.isEmpty(), program.name() + ": output empty after fourth step");
        } catch (Exception ex) {
            errors += 1;
            ex.printStackTrace();
        }
        // _ If any drones occupy this, read a numeric value from STDIN and dispatch that many drones to linked stations. 
        try {
            var code = """
                {~~~}
                {_>0}
                {~~~}
                """;
            var program = compiler.compile("test.defect._", code, "");
            var in1 = (NumInStation) get(1, 1, program);
            var nop1 = (NopStation) get(3, 1, program);
            
            program.reset();
            assertEquals(0, nop1.drones(), program.name() + ": nop1 - after reset");
//            in1.receive(1);
            program.step();  // init
            assertEquals(0, nop1.drones(), program.name() + ": nop1 - after first step");
            input.expectInt(123);
            in1.receive(1);
            program.step();
            assertEquals(123, nop1.drones(), program.name() + ": nop1 - after second step");
            input.expectInt(456);
            in1.receive(1);
            program.step();
            assertEquals(456, nop1.drones(), program.name() + ": nop1 - after third step");
            in1.receive(1);
            program.step();
            assertEquals(0, nop1.drones(), program.name() + ": nop1 - after fourth step");
        } catch (Exception ex) {
            errors += 1;
            ex.printStackTrace();
        }
        // ; Pause execution for a duration equal to the number of drones occupying this station in milliseconds. 
        try {
            var code = """
                {~~~}
                { ; }
                {~~~}
                """;
            var program = compiler.compile("test.defect.;", code, "");
            var pause1 = (PauseStation) get(2, 1, program);
            
            program.reset();
            program.step();
            program.step();
            var start = System.currentTimeMillis();
            program.step();
            var delta = System.currentTimeMillis() - start;
            pause1.receive(500);
            start = System.currentTimeMillis();
            program.step();
            var delay = System.currentTimeMillis() - start - delta;
            assertEquals(true, 400 < delay && delay < 600, "delay: " + delay);
        } catch (Exception ex) {
            errors += 1;
            ex.printStackTrace();
        }
    }
    
    private void metropolis() {
        // Similar semantics apply for metropoleis that apply to exclusion zones. 

        if (strictZone) {
            // A position must be fully enclosed by fences for it to be considered "inside" the exclusion zone.
            try {
                var code = """
                    [~~~]
                    [ © ]
                    [===]
                    """;
                var program = compiler.compile("test.metropolis.enclosed", code, "©=1");
                
                errors += 1;
                System.err.printf("%s: expected Exception - zone not fully enclosed by forts%n", program.name());
                Thread.dumpStack();
            } catch (Exception ex) {
                // expected
            }
            // The fences of an exclusion zone will behave as * tunnels.
            try {
                var code = """
                       [==]
                    @->[0 ]
                       [==]
                    """;
                var program = compiler.compile("test.metropolis.arrow-fence", code, "");
                
                errors += 1;
                System.err.printf("%s: expected Exception - arrow before fort%n", program.name());
                Thread.dumpStack();
            } catch (Exception ex) {
                // expected
            }
        }
        
        // The fences of an exclusion zone will behave as * tunnels.
        try {
            var code = """
                  [===]
                @-[-0 ]
                  [===]
                """;
            var program = compiler.compile("test.metropolis.*.1", code, "");
            var create1 = (CreateStation) get(0, 1, program);
            var nop1 = (NopStation) get(4, 1, program);
            
            program.reset();
            assertEquals(0, create1.drones(), program.name() + ": create1 - after reset");
            assertEquals(0, nop1.drones(), program.name() + ": nop1 - after reset");
            program.step();
            assertEquals(1, nop1.drones(), program.name() + ": nop1 - after step");
        } catch (Exception ex) {
            errors += 1;
            ex.printStackTrace();
        }
        try {
            var code = """
                  [===]
                  [0  ]
                  [===]
                 /
                @
                """;
            var program = compiler.compile("test.metropolis.*.2", code, "");
            var create1 = (CreateStation) get(0, 4, program);
            var nop1 = (NopStation) get(3, 1, program);
            
            program.reset();
            assertEquals(0, create1.drones(), program.name() + ": create1 - after reset");
            assertEquals(0, nop1.drones(), program.name() + ": nop1 - after reset");
            program.step();
            assertEquals(1, nop1.drones(), program.name() + ": nop1 - after step");
        } catch (Exception ex) {
            errors += 1;
            ex.printStackTrace();
        }
        try {
            var code = """
                [===]
                [ 0 ]
                [===]
                  |
                  @
                """;
            var program = compiler.compile("test.metropolis.*.3", code, "");
            var create1 = (CreateStation) get(2, 4, program);
            var nop1 = (NopStation) get(2, 1, program);
            
            program.reset();
            assertEquals(0, create1.drones(), program.name() + ": create1 - after reset");
            assertEquals(0, nop1.drones(), program.name() + ": nop1 - after reset");
            program.step();
            assertEquals(1, nop1.drones(), program.name() + ": nop1 - after step");
        } catch (Exception ex) {
            errors += 1;
            ex.printStackTrace();
        }
        // An exclusion zone in a metropolis is still an exclusion zone.
        try {
            var code = """
                {~~~~~}
                { [=] }
                { [©] }
                { [=] }
                {     }
                {~~~~~}
                """;
            var program = compiler.compile("test.metropolis.inside", code, "©=0");
            get(SyntheticStation.class, 3, 2, program);
        } catch (Exception ex) {
            errors += 1;
            ex.printStackTrace();
        }
    }
    
    private void syntheticStation() {
        // The expression is evaluated for the station on each tick, and represents the number of drones dispatched
        try {
            var code = """
                [=====]
                [ ©>0 ]
                [=====]
                ((©=5))
                """;
            var program = compiler.compile("test.synthetic.send", code, "");
            var syn1 = (SyntheticStation) get(2, 1, program);
            var nop1 = (NopStation) get(4, 1, program);
            
            program.reset();
            assertEquals(0, syn1.drones(), program.name() + ": syn1 - after reset");
            assertEquals(0, nop1.drones(), program.name() + ": nop1 - after reset");
            program.step();
            assertEquals(0, syn1.drones(), program.name() + ": syn1 - after first step");
            assertEquals(5, nop1.drones(), program.name() + ": nop1 - after first step");
            program.step();
            assertEquals(0, syn1.drones(), program.name() + ": syn1 - after second step");
            assertEquals(5, nop1.drones(), program.name() + ": nop1 - after second step");
        } catch (Exception ex) {
            errors += 1;
            ex.printStackTrace();
        }
        // Negative drones are clamped to 0.
        try {
            var code = """
                [=====]
                [ ©>0 ]
                [=====]
                ((©=0 6-))
                """;
            var program = compiler.compile("test.synthetic.negative", code, "");
            var syn1 = (SyntheticStation) get(2, 1, program);
            var nop1 = (NopStation) get(4, 1, program);
            
            program.reset();
            assertEquals(0, syn1.drones(), program.name() + ": syn1 - after reset");
            assertEquals(0, nop1.drones(), program.name() + ": nop1 - after reset");
            program.step();
            assertEquals(0, syn1.drones(), program.name() + ": syn1 - after first step");
            assertEquals(0, nop1.drones(), program.name() + ": nop1 - after first step");
        } catch (Exception ex) {
            errors += 1;
            ex.printStackTrace();
        }
        // The expression can be in terms of integer literals, as well as special variables N and K, 
        // which represent the number of drones currently occupying the station and the number of linked station.
        try {
            var code = """
                [=====]
                [0-©-0]
                [=====]
                ((©=N 1 +))
                """;
            var program = compiler.compile("test.synthetic.N", code, "");
            var nop1 = (NopStation) get(1, 1, program);
            var syn1 = (SyntheticStation) get(3, 1, program);
            var nop2 = (NopStation) get(5, 1, program);
            
            program.reset();
            assertEquals(0, syn1.drones(), program.name() + ": syn1 - after reset");
            assertEquals(0, nop1.drones(), program.name() + ": nop1 - after reset");
            assertEquals(0, nop2.drones(), program.name() + ": nop2 - after reset");
            program.step();
            syn1.receive(6);
            program.step();
            assertEquals(0, syn1.drones(), program.name() + ": syn1 - after first step");
            assertEquals(7, nop1.drones(), program.name() + ": nop1 - after first step");
            assertEquals(7, nop2.drones(), program.name() + ": nop2 - after first step");
            program.step();
            assertEquals(0, syn1.drones(), program.name() + ": syn1 - after second step");
            assertEquals(1, nop1.drones(), program.name() + ": nop1 - after second step");
            assertEquals(1, nop2.drones(), program.name() + ": nop2 - after second step");
        } catch (Exception ex) {
            errors += 1;
            ex.printStackTrace();
        }
        try {
            var code = """
                [=====]
                [0-©-0]
                [  |  ]
                [  0  ]
                [=====]
                ((©=K 2 +))
                """;
            var program = compiler.compile("test.synthetic.K", code, "");
            var nop1 = (NopStation) get(1, 1, program);
            var syn1 = (SyntheticStation) get(3, 1, program);
            var nop2 = (NopStation) get(5, 1, program);
            var nop3 = (NopStation) get(3, 3, program);
            
            program.reset();
            assertEquals(0, syn1.drones(), program.name() + ": syn1 - after reset");
            assertEquals(0, nop1.drones(), program.name() + ": nop1 - after reset");
            assertEquals(0, nop2.drones(), program.name() + ": nop2 - after reset");
            assertEquals(0, nop3.drones(), program.name() + ": nop3 - after reset");
            program.step();
            syn1.receive(6);
            program.step();
            assertEquals(0, syn1.drones(), program.name() + ": syn1 - after first step");
            assertEquals(5, nop1.drones(), program.name() + ": nop1 - after first step");
            assertEquals(5, nop2.drones(), program.name() + ": nop2 - after first step");
            assertEquals(5, nop3.drones(), program.name() + ": nop3 - after first step");
            program.step();
            assertEquals(0, syn1.drones(), program.name() + ": syn1 - after second step");
            assertEquals(5, nop1.drones(), program.name() + ": nop1 - after second step");
            assertEquals(5, nop2.drones(), program.name() + ": nop2 - after second step");
            assertEquals(5, nop3.drones(), program.name() + ": nop3 - after second step");
        } catch (Exception ex) {
            errors += 1;
            ex.printStackTrace();
        }
        // Symbols may be any non-whitespace character, except for symbols already defined in the Zirconium grammar
        // The only exception to this rule are station symbols. ...
        var template = """
            [===]
            [ © ]
            [===]
            ((©=1))
            """;
        for (var ch : "-|/\\+X*>^<v#(){~}[=]".toCharArray()) {
            var code = template.replace('©', ch);
            try {
                var program = compiler.compile("test.synthetic." + ch, code, "");
                get(SyntheticStation.class, 2, 1, program);

                errors += 1;
                System.err.printf("%s: expected Exception - invalid symbol%n", program.name());
                Thread.dumpStack();
            } catch (CompileException ex) {
                // expected
            } catch (Exception ex) {
                errors += 1;
                ex.printStackTrace();
            }
        }
        // ... These may be overridden, but the new definition will only apply to synthetic stations inside metropoleis.
        try {
            var code = """
                [===] @
                [@-0] |
                [===] 0
                ((@=3))
                """;
            var program = compiler.compile("test.synthetic.override", code, "");
            var nop1 = (NopStation) get(3, 1, program);
            var nop2 = (NopStation) get(6, 2, program);
            
            program.reset();
            assertEquals(0, nop1.drones(), program.name() + ": nop1 - after reset");
            assertEquals(0, nop2.drones(), program.name() + ": nop2 - after reset");
            program.step();
            assertEquals(3, nop1.drones(), program.name() + ": nop1 - after first step");
            assertEquals(1, nop2.drones(), program.name() + ": nop2 - after first step");
            program.step();
            assertEquals(3, nop1.drones(), program.name() + ": nop1 - after second step");
            assertEquals(1, nop2.drones(), program.name() + ": nop2 - after second step");
        } catch (Exception ex) {
            errors += 1;
            ex.printStackTrace();
        }
        // Implementations are encouraged to support unicode codepoints
        // A synthetic station may be defined inside a lens. 
        try {
            var code = """
                [=====]
                [ ✈-0 ]
                [=====]
                ((✈=8))
                """;
            var program = compiler.compile("test.synthetic.utf", code, "");
            var syn1 = (SyntheticStation) get(2, 1, program);
            var nop1 = (NopStation) get(4, 1, program);
            
            program.reset();
            assertEquals(0, syn1.drones(), program.name() + ": syn1 - after reset");
            assertEquals(0, nop1.drones(), program.name() + ": nop1 - after reset");
            program.step();
            assertEquals(0, syn1.drones(), program.name() + ": syn1 - after first step");
            assertEquals(8, nop1.drones(), program.name() + ": nop1 - after first step");
            program.step();
            assertEquals(0, syn1.drones(), program.name() + ": syn1 - after second step");
            assertEquals(8, nop1.drones(), program.name() + ": nop1 - after second step");
        } catch (Exception ex) {
            errors += 1;
            ex.printStackTrace();
        }
        // A synthetic station may also be defined in a special header file
        try {
            var code = """
                [=====]
                [ ©-0 ]
                [=====]
                """;
            var program = compiler.compile("test.synthetic.header", code, "© = 9");
            var syn1 = (SyntheticStation) get(2, 1, program);
            var nop1 = (NopStation) get(4, 1, program);
            
            program.reset();
            assertEquals(0, syn1.drones(), program.name() + ": syn1 - after reset");
            assertEquals(0, nop1.drones(), program.name() + ": nop1 - after reset");
            program.step();
            assertEquals(0, syn1.drones(), program.name() + ": syn1 - after first step");
            assertEquals(9, nop1.drones(), program.name() + ": nop1 - after first step");
            program.step();
            assertEquals(0, syn1.drones(), program.name() + ": syn1 - after second step");
            assertEquals(9, nop1.drones(), program.name() + ": nop1 - after second step");
        } catch (Exception ex) {
            errors += 1;
            ex.printStackTrace();
        }
    }
    
    private void zoneInference() {
        // Fences or forts may be omitted in some situations. Specifically, whenever a fence or a fort stops at one of the borders of the program
        try {
            var code = " {%";
            var program = compiler.compile("test.inference.right", code, "");
            get(ByteOutStation.class, 2, 0, program);
        } catch (Exception ex) {
            errors += 1;
            ex.printStackTrace();
        }
        try {
            var code = "? } ";
            var program = compiler.compile("test.inference.left", code, "");
            get(ByteInStation.class, 0, 0, program);
        } catch (Exception ex) {
            errors += 1;
            ex.printStackTrace();
        }
        try {
            var code = """
                © ] 
                ==] 
                ((©=1))
                """;
            var program = compiler.compile("test.inference.left-top", code, "");
            get(SyntheticStation.class, 0, 0, program);
        } catch (Exception ex) {
            errors += 1;
            ex.printStackTrace();
        }
        try {
            var code = """
                 ~~~
                { `
                """;
            var program = compiler.compile("test.inference.right-bottom", code, "");
            get(NumOutStation.class, 2, 1, program);
        } catch (Exception ex) {
            errors += 1;
            ex.printStackTrace();
        }
        try {
            var code = """
                {&}
                 ~
                """;
            var program = compiler.compile("test.inference.top", code, "");
            get(ByteErrStation.class, 1, 0, program);
        } catch (Exception ex) {
            errors += 1;
            ex.printStackTrace();
        }
    }
    

    
    private Single get(int x, int y, Program program) {
        return get(Single.class, x, y, program);
    }
    
    private <T extends Single> T get(Class<T> type, int x, int y, Program program) {
        Predicate<Single> posFilter = s -> s.pos().x() == x && s.pos().y() == y;
        return 
            Stream.concat(
                program.stations().stream()
                ,
                program.stations().stream()
                .filter(Bound.class::isInstance)
                .map(Bound.class::cast)
                .flatMap(Bound::stations)
            )
            .filter(type::isInstance)
            .map(type::cast)
            .filter(posFilter)
            .findFirst()
            .get();
    }
    
    private void assertEquals(String expected, String actual, String message) throws Exception {
        if (!Objects.equals(actual, expected)) {
            throw new Exception(String.format("expected: \"%s\", actual: \"%s\", %s", expected, actual, message));
        }
    }
    
    private void assertEquals(int expected, int actual, String message) throws Exception {
        if (actual != expected) {
            throw new Exception(String.format("expected: %d, actual: %d, %s", expected, actual, message));
        }
    }
    
    private void assertEquals(boolean expected, boolean actual, String message) throws Exception {
        if (actual != expected) {
            throw new Exception(String.format("expected: %s, actual: %s, %s", expected, actual, message));
        }
    }
    
    ////////////////////////////////////////////////////////////////////////////////////////////////////
    
    private static class PrinterMock implements Printer {
        @Override
        public void print(String format, Object... args) {
            //
        }
    }
    
    private static class InputMock implements Input {
        private final Queue<Number> values = new LinkedList<>();
        private boolean lenient = true;
        void expectByte(int value) { values.add((byte)value); }
        void expectInt(int value) { values.add(value); }
        @Override
        public void reset() {
            values.clear();
        }
        @Override
        public int readByte() {
            return lenient&&values.isEmpty() ? -1 : (Byte) values.remove();
        }
        @Override
        public int readInteger() {
            return lenient&&values.isEmpty() ? -1 : (Integer) values.remove();
        }
    }

    private static class OutputMock implements Output {
        private final Queue<Object> values = new LinkedList<>();
        boolean isEmpty() { return values.isEmpty(); }
        String nextString() { return (String) values.remove(); }
        int nextInt() { return (Integer) values.remove(); }
        @Override
        public void reset() {
            values.clear();
        }
        @Override
        public void write(String text) {
            values.add(text);
        }
        @Override
        public void write(int b) {
            values.add(b);
        }
    }
}
