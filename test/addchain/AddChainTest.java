package addchain;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Collections;

import org.junit.jupiter.api.Test;

import addchain.AddChainLine.ChainLineAssign;
import addchain.AddChainLine.ChainLineComment;
import addchain.AddChainLine.RhsAssign;
import addchain.AddChainLine.RhsMul;
import addchain.AddChainLine.RhsSquare;

public class AddChainTest {
    @Test
    public void testLineOne() {
        String line1 = "t10 = input        #    0 : 1";
        assertEquals(new ChainLineAssign("t10", new RhsAssign("input")), AddChainLine.newChainLine(line1));
    }

    @Test
    public void testLineTwo() {
        String line1 = "t0 = sqr(t10)      #    1 : 2";
        assertEquals(new ChainLineAssign("t0", new RhsSquare("t10")), AddChainLine.newChainLine(line1));
    }

    @Test
    public void testLineThree() {
        String line1 = "t1 = t0 * t10      #    2 : 3";
        assertEquals(new ChainLineAssign("t1", new RhsMul("t0", "t10")), AddChainLine.newChainLine(line1));
    }

    @Test
    public void testComment() {
        String line = "# This is a comment";
        assertEquals(new ChainLineComment(" This is a comment"), AddChainLine.newChainLine(line));
    }

    private static String toRustCode(String line, boolean verbose) {
        return AddChainLine.convertToRust(Collections.singletonList(line), verbose);
    }

    @Test
    public void testAssignToRust() {
        String line1 = "t1 = test";
        assertEquals("let t1 = test;\n", toRustCode(line1, true));
    }

    @Test
    public void testSquare() {
        String line1 = "t1 = sqr(t0)";
        assertEquals("let mut t1 = t0;\n"
                + "t1.square_assign();\n", toRustCode(line1, true));
    }

    @Test
    public void testSquareAssign() {
        String line1 = "t1 = sqr(t1)";
        assertEquals("t1.square_assign();\n", toRustCode(line1, true));
    }

    @Test
    public void testMul() {
        String line1 = "t1 = t0 * t2";
        assertEquals("let mut t1 = t0;\n"
                + "t1.mul_assign(&t2);\n", toRustCode(line1, true));
    }

    @Test
    public void testMulAssign() {
        String line1 = "t0 = t0 * t2";
        String line2 = "t0 = t2 * t0";
        assertEquals("t0.mul_assign(&t2);\n", toRustCode(line1, true));
        assertEquals("t0.mul_assign(&t2);\n", toRustCode(line2, true));
    }
}
