package unitTest;

import static org.junit.Assert.*;

import java.util.Random;
import org.junit.Test;

import common.SigmoidTable;

public class SigmoidTest {

    @Test
    public void testPrecision() {
        Random rand = new Random();
        float maxX = 10;
        SigmoidTable sigmoidComputer = new SigmoidTable(1000000, maxX);
        float delta = (float) Math.exp(-12);
        for (int i = 0; i < 10000; i++) {
            float x = rand.nextFloat() * 2 * maxX - maxX;
            float realSigmoid = (float) SigmoidTable.sigmoid(x);
            float precomputedSigmoid = sigmoidComputer.getSigmoid(x);
            assertEquals(precomputedSigmoid, realSigmoid, delta);
        }
    }

}
