package com.civisanalytics.gephi;

import org.junit.Test;
import static org.junit.Assert.fail;

import static com.civisanalytics.gephi.CreateGephiForceDiagram.BoundedNumericArgument;

import net.sourceforge.argparse4j.ArgumentParsers;
import net.sourceforge.argparse4j.inf.ArgumentParser;
import net.sourceforge.argparse4j.inf.ArgumentParserException;

public class CreateGephiForceDiagramTest {
    ArgumentParser parser;

    public CreateGephiForceDiagramTest() {
        parser = ArgumentParsers.newArgumentParser("Foo");
        parser.addArgument("-db", "--double_bounded")
            .type(new BoundedNumericArgument<Double>(20.0, 100.0, true, true));
        parser.addArgument("-fb", "--float_bounded")
            .type(new BoundedNumericArgument<Float>(20.0f, 100.0f, false, false));
        parser.addArgument("-ib", "--int_bounded")
            .type(new BoundedNumericArgument<Integer>(20, 100, true, false));
        parser.addArgument("-dmax", "--double_max")
            .type(new BoundedNumericArgument<Double>(null, 100.0, true, true));
        parser.addArgument("-fmax", "--float_max")
            .type(new BoundedNumericArgument<Float>(null, 100.0f, false, false));
        parser.addArgument("-imax", "--int_max")
            .type(new BoundedNumericArgument<Integer>(null, 100, true, false));
        parser.addArgument("-dmin", "--double_min")
            .type(new BoundedNumericArgument<Double>(20.0, null, true, true));
        parser.addArgument("-fmin", "--float_min")
            .type(new BoundedNumericArgument<Float>(20.0f, null, false, false));
        parser.addArgument("-imin", "--int_min")
            .type(new BoundedNumericArgument<Integer>(20, null, true, false));
    }

    /**
     * Test validation of numeric arguments in specified interval
     */
    @Test
    public void testValidateColumnsInRange() throws ArgumentParserException {
        String[] args = new String[]{"-db", "50", "-fb", "60.0", "-ib", "81",
                                     "-dmax", "0.0", "-fmax", "10", "-imax", "30",
                                     "-dmin", "21.1", "-fmin", "1001.2", "-imin", "300"};
        parser.parseArgs(args);
    }

    /**
     * Test validation of numeric arguments at allowable edge of specified interval
     */
    @Test
    public void testValidateColumnsClosedEdge() throws ArgumentParserException {
        String[] args = new String[]{"-db", "100", "-ib", "20",
                                     "-dmax", "100.0",
                                     "-dmin", "20.0", "-imin", "20"};
        parser.parseArgs(args);
    }

    /**
     * Test identification of numeric arguments outside specified interval
     */
    @Test
    public void testValidateColumnsOutsideRange() throws ArgumentParserException {
        String[][] errorArgs = new String[][]{{"-db", "10"}, {"-fb", "1000"},
                                              {"-ib", "1"},
                                              {"-dmax", "1000"}, {"-fmax", "100.1"},
                                              {"-imax", "22222"},
                                              {"-dmin", "0"}, {"-fmin", "1.1"},
                                              {"-imin", "12"}};
        for (int i = 0; i < errorArgs.length; i++) {
            try {
                parser.parseArgs(errorArgs[i]);
                fail("No parsing exception for option " + errorArgs[i][0]
                     + " " + errorArgs[i][1]);
            } catch (ArgumentParserException e) {
                assert true;
            }
        }
    }

    /**
     * Test identification of numeric arguments at disallowed edge of specified interval
     */
    @Test
    public void testValidateColumnsOpenEdge() throws ArgumentParserException {
        String[][] errorArgs = new String[][]{{"-fb", "20"}, {"-fb", "100"}, {"-ib", "100"},
                                              {"-fmax", "100"}, {"-imax", "100"},
                                              {"-fmin", "20"}};
        for (int i = 0; i < errorArgs.length; i++) {
            try {
                parser.parseArgs(errorArgs[i]);
                fail("No parsing exception for option " + errorArgs[i][0]
                     + " " + errorArgs[i][1]);
            } catch (ArgumentParserException e) {
                assert true;
            }
        }
    }

    /**
     * Test identification of improperly formatted numeric arguments
     */
    @Test
    public void testBadNumberFormat() throws ArgumentParserException {
        String[][] errorArgs = new String[][]{{"-db", "50xx"}, {"-fb", "xx50"},
                                              {"-ib", "One"},
                                              {"-ib", "10.2"},
                                              {"-ib", "1.1e3"}};
        for (int i = 0; i < errorArgs.length; i++) {
            try {
                parser.parseArgs(errorArgs[i]);
                fail("No parsing exception for option " + errorArgs[i][0]
                     + " " + errorArgs[i][1]);
            } catch (ArgumentParserException e) {
                assert true;
            }
        }
    }
}
