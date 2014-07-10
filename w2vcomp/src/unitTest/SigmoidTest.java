package unitTest;

import static org.junit.Assert.*;

import java.util.Random;

import org.junit.Test;

import common.MathUtils;
import common.SigmoidTable;

public class SigmoidTest {

    @Test
    public void testPrecision() {
        Random rand = new Random();
        double maxX = 10;
        SigmoidTable sigmoidComputer = new SigmoidTable(1000000, maxX);
        double delta = (double) Math.exp(-12);
        for (int i = 0; i < 10000; i++) {
            double x = rand.nextFloat() * 2 * maxX - maxX;
            double realSigmoid = (double) MathUtils.sigmoid(x);
            double precomputedSigmoid = sigmoidComputer.getSigmoid(x);
            assertEquals(precomputedSigmoid, realSigmoid, delta);
        }
    }

}
