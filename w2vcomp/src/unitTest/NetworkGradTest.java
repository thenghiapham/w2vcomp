package unitTest;


import neural.CompositionMatrices;
import neural.HierarchicalSoftmaxLearner;
import neural.ProjectionMatrix;
import neural.TreeNetwork;
import neural.function.IdentityFunction;
import neural.function.Sigmoid;

import org.ejml.simple.SimpleMatrix;
import org.junit.Before;
import org.junit.Test;

import tree.Tree;
import vocab.Vocab;
import common.SigmoidTable;
import common.SimpleMatrixUtils;

public class NetworkGradTest {
    SigmoidTable sigmoidTable;
    @Before
    public void setUp() throws Exception {
        sigmoidTable = new SigmoidTable();
    }

    @Test
    public void testSimpleSoftmax() {

        double[] rawInputPhrase = new double[]{1,0,0,1,0.5, 0.5};
        double[] rawCompositionMatrix = new double[]{2, 0, 0, 2, 1, 0, 0, 1};
        double[] rawSoftmaxWeight = new double[]{1,0,0,1};
        SimpleMatrix wordMatrix = new SimpleMatrix(3,2,true,rawInputPhrase);
        SimpleMatrix compositionMatrix = new SimpleMatrix(2,4,false, rawCompositionMatrix);
        SimpleMatrix softmaxWeight = new SimpleMatrix(2,2,true,rawSoftmaxWeight);
        double subSample = 0;
//        SimpleMatrix softmaxValue = new SimpleMatrix(2,1, true, rawSoftmaxValue);
        Vocab vocab = new Vocab();
        vocab.loadVocab("/home/pham/vocab.txt");
        vocab.assignCode();
        ProjectionMatrix projectionBuilder = ProjectionMatrix.initializeFromMatrix(vocab, SimpleMatrixUtils.to2DArray(wordMatrix));
        CompositionMatrices hiddenBuilder = CompositionMatrices.createSimple(compositionMatrix);
        HierarchicalSoftmaxLearner outputBuilder = HierarchicalSoftmaxLearner.initializeFromMatrix(vocab, SimpleMatrixUtils.to2DArray(softmaxWeight));
//        String parseString = "(S (NP (JJ hot) (NN man) ) (VB eat))";
        String parseString = "(S (NP (NP (JJ hot) (NN man)) (NP (JJ hot) (NN man))) (VB man))";
//        String parseString = "(NP (NP (JJ hot) (NN man)) (VB hot))";
//        String parseString = "(S (NP (NP (JJ hot) (NN man)) (NP (JJ hot) (NN eat))) (VB eat))";
        Tree parseTree = Tree.fromPennTree(parseString);
        TreeNetwork network = TreeNetwork.createNetwork(parseTree, vocab, projectionBuilder, hiddenBuilder, outputBuilder, new IdentityFunction(), new Sigmoid(), 1, 3, false, false, subSample);
        //TreeNetwork network = TreeNetwork.createNetwork(parseTree, projectionBuilder, hiddenBuilder, outputBuilder, new IdentityFunction(), new Sigmoid(), 1, 3, true, false);
        network.checkGradient();
        
    }
    
    public static void main(String[] args) {
        NetworkGradTest test = new NetworkGradTest();
        test.testSimpleSoftmax();
    } 
}
