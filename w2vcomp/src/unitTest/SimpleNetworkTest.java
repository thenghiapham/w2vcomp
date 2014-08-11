package unitTest;

import static org.junit.Assert.*;

import java.util.ArrayList;


import neural.CostFunction;
import neural.HierarchicalSoftmaxCost;
import neural.NegativeSamplingCost;
import neural.OutputLayer;
import neural.ProjectionLayer;
import neural.Sigmoid;

import org.ejml.simple.SimpleMatrix;
import org.junit.Before;
import org.junit.Test;

import common.SigmoidTable;
import common.ValueGradient;


public class SimpleNetworkTest {
    SigmoidTable sigmoidTable;
    @Before
    public void setUp() throws Exception {
        sigmoidTable = new SigmoidTable();
    }
    
    public ValueGradient computeGrad(SimpleMatrix input, SimpleMatrix softmaxWeight, SimpleMatrix output, CostFunction function) {
        ProjectionLayer pLayer = new ProjectionLayer(input);
        OutputLayer oLayer = new OutputLayer(softmaxWeight, new Sigmoid(), output, function);
        pLayer.addOutLayer(oLayer);
        oLayer.addInLayer(pLayer);
        pLayer.forward();
        oLayer.forward();
        oLayer.backward();
        pLayer.backward();
        double value = oLayer.getCost();
        
        SimpleMatrix gradient = pLayer.getGradient();
        ArrayList<SimpleMatrix> gradients = new ArrayList<SimpleMatrix>();
        gradients.add(gradient);
        return new ValueGradient(value, gradients);
    }
    
    public ArrayList<SimpleMatrix> computeNumericGrad(SimpleMatrix input, SimpleMatrix softmaxWeight, SimpleMatrix output, CostFunction function) {
        ArrayList<SimpleMatrix> numGradients = new ArrayList<>();
        SimpleMatrix theta[] = new SimpleMatrix[]{input, softmaxWeight};
        double e = 1e-4;
        for (int i = 0; i < 2; i++) {
            SimpleMatrix component = theta[i];
            int rowNum = component.numRows();
            int colNum = component.numCols();
            SimpleMatrix componentDelta = new SimpleMatrix(rowNum, colNum);
            SimpleMatrix componentGrad = new SimpleMatrix(rowNum, colNum);
            for (int x = 0; x < rowNum; x++) {
                for (int y = 0; y < colNum; y++) {
                    componentDelta.set(x, y, e);
                    theta[i] = component.plus(componentDelta);
                    double loss1 = computeGrad(theta[0], theta[1], output, function).value;
                    theta[i] = component.minus(componentDelta);
                    double loss2 = computeGrad(theta[0], theta[1], output, function).value;
                    componentGrad.set(x, y, (loss1 - loss2) / (2 * e));
                    componentDelta.set(x, y, 0);
                }
            }
            theta[i] = component;
            numGradients.add(componentGrad);
        }
        return numGradients;
    }
    
    

    @Test
    public void testSimpleSoftmax() {
        SimpleMatrix inputWord = new SimpleMatrix(2,1,true, new double[]{1,0});
        SimpleMatrix softmaxWeight = new SimpleMatrix(2,2,true, new double[]{1,0,0,1});
        SimpleMatrix softmaxValue = new SimpleMatrix(2,1,true, new double[]{1,0});
        ValueGradient valueGrad = computeGrad(inputWord, softmaxWeight, softmaxValue, new HierarchicalSoftmaxCost());
        ArrayList<SimpleMatrix> grads = valueGrad.gradients;
        ArrayList<SimpleMatrix> numGrads = computeNumericGrad(inputWord, softmaxWeight, softmaxValue, new HierarchicalSoftmaxCost());
        for (int i = 0; i < 1; i++) {
            System.out.println(grads.get(i));
            System.out.println(numGrads.get(i));
            assertArrayEquals(grads.get(i).getMatrix().getData(), numGrads.get(i).getMatrix().getData(), 1e-5);
        }
    }
    
    @Test
    public void testSimpleNegative() {
        SimpleMatrix inputWord = new SimpleMatrix(2,1,true, new double[]{1,0});
        SimpleMatrix softmaxWeight = new SimpleMatrix(2,2,true, new double[]{1,0,0,1});
        SimpleMatrix softmaxValue = new SimpleMatrix(2,1,true, new double[]{1,0});
        ValueGradient valueGrad = computeGrad(inputWord, softmaxWeight, softmaxValue, new NegativeSamplingCost());
        ArrayList<SimpleMatrix> grads = valueGrad.gradients;
        ArrayList<SimpleMatrix> numGrads = computeNumericGrad(inputWord, softmaxWeight, softmaxValue, new NegativeSamplingCost());
        for (int i = 0; i < 1; i++) {
            System.out.println(grads.get(i));
            System.out.println(numGrads.get(i));
            assertArrayEquals(grads.get(i).getMatrix().getData(), numGrads.get(i).getMatrix().getData(), 1e-5);
        }
        
    }

}
