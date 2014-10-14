package unitTest;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

import java.util.ArrayList;

import neural.TreeNetwork;

import org.ejml.simple.SimpleMatrix;
import org.junit.Before;
import org.junit.Test;

import word2vec.SkipGramPhrase2Vec;

import common.SigmoidTable;
import common.ValueGradient;

public class NetworkGradTest {
    SigmoidTable sigmoidTable;
    @Before
    public void setUp() throws Exception {
        sigmoidTable = new SigmoidTable();
    }

    @Test
    public void testSimpleSoftmax() {
//        TreeNetwork network = TreeNetwork.
        SkipGramPhrase2Vec w2v = new SkipGramPhrase2Vec(2, 2, true, 0, 1e-3);
        double[] rawInputPhrase = new double[]{1,0,0,1};
        double[] rawCompositionMatrix = new double[]{2, 0, 0, 2, 1, 0, 0, 1};
        double[] rawSoftmaxWeight = new double[]{1,0,0,1};
        double[] rawSoftmaxValue = new double[]{1,0};
        SimpleMatrix inputPhrase = new SimpleMatrix(4,1,true,rawInputPhrase);
        SimpleMatrix compositionMatrix = new SimpleMatrix(2,4,false, rawCompositionMatrix);
        SimpleMatrix softmaxWeight = new SimpleMatrix(2,2,true,rawSoftmaxWeight);
        SimpleMatrix softmaxValue = new SimpleMatrix(2,1, true, rawSoftmaxValue);
        ValueGradient valueGrad = w2v.computeGradientSoftmax(inputPhrase, compositionMatrix, softmaxWeight, softmaxValue);
        ArrayList<SimpleMatrix> grads = valueGrad.gradients;
        ArrayList<SimpleMatrix> numGrads = w2v.computeNumericGradientsSoftmax(inputPhrase, compositionMatrix, softmaxWeight, softmaxValue);
        for (int i = 0; i < 3; i++) {
            assertArrayEquals(grads.get(i).getMatrix().getData(), numGrads.get(i).getMatrix().getData(), 1e-5);
        }
        double value = Math.log(1 - sigmoidTable.getSigmoid(2)) + Math.log(sigmoidTable.getSigmoid(1)) - (w2v.getWeightDecay() * 5);
        System.out.println("expected value: " + value);
        System.out.println("real value: " + valueGrad.value);
        assertEquals(valueGrad.value, value,1e-8);
        
        SimpleMatrix d2 = new SimpleMatrix(2,1,true,new double[]{ - sigmoidTable.getSigmoid(2),1 - sigmoidTable.getSigmoid(1)});
        SimpleMatrix softmaxWeightGrad = d2.mult(new SimpleMatrix(1,2,true,new double[]{2,1}));

        System.out.println("real grad: " + softmaxWeightGrad);
        System.out.println("num grad: " + numGrads.get(2));
        assertArrayEquals(softmaxWeightGrad.getMatrix().getData(), grads.get(2).getMatrix().getData(), 1e-5);
        
    }
}
