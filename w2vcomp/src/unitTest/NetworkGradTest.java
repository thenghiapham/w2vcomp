package unitTest;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

import java.util.ArrayList;

import neural.CompositionMatrices;
import neural.HierarchicalSoftmaxLearner;
import neural.ProjectionMatrix;
import neural.TreeNetwork;
import neural.function.IdentityFunction;
import neural.function.Sigmoid;
import neural.function.Tanh;

import org.ejml.simple.SimpleMatrix;
import org.junit.Before;
import org.junit.Test;

import tree.Tree;
import vocab.Vocab;
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

        SkipGramPhrase2Vec w2v = new SkipGramPhrase2Vec(2, 2, true, 0, 1e-3);
        double[] rawInputPhrase = new double[]{1,0,0,1,0.5, 0.5};
        double[] rawCompositionMatrix = new double[]{2, 0, 0, 2, 1, 0, 0, 1};
        double[] rawSoftmaxWeight = new double[]{1,0,0,1};
        SimpleMatrix wordMatrix = new SimpleMatrix(3,2,true,rawInputPhrase);
        SimpleMatrix compositionMatrix = new SimpleMatrix(2,4,false, rawCompositionMatrix);
        SimpleMatrix softmaxWeight = new SimpleMatrix(2,2,true,rawSoftmaxWeight);
//        SimpleMatrix softmaxValue = new SimpleMatrix(2,1, true, rawSoftmaxValue);
        Vocab vocab = new Vocab();
        vocab.loadVocab("/home/pham/vocab.txt");
        vocab.assignCode();
        ProjectionMatrix projectionBuilder = ProjectionMatrix.initializeFromMatrix(vocab, wordMatrix);
        CompositionMatrices hiddenBuilder = CompositionMatrices.createSimple(compositionMatrix);
        HierarchicalSoftmaxLearner outputBuilder = HierarchicalSoftmaxLearner.initializeFromMatrix(vocab, softmaxWeight);
//        String parseString = "(S (NP (JJ hot) (NN man) ) (VB eat))";
        String parseString = "(S (NP (NP (JJ hot) (NN man)) (NP (JJ hot) (NN man))) (VB man))";
//        String parseString = "(NP (NP (JJ hot) (NN man)) (VB hot))";
//        String parseString = "(S (NP (NP (JJ hot) (NN man)) (NP (JJ hot) (NN eat))) (VB eat))";
        Tree parseTree = Tree.fromPennTree(parseString);
        TreeNetwork network = TreeNetwork.createNetwork(parseTree, projectionBuilder, hiddenBuilder, outputBuilder, new IdentityFunction(), new Sigmoid(), 1, 3, false, false);
        //TreeNetwork network = TreeNetwork.createNetwork(parseTree, projectionBuilder, hiddenBuilder, outputBuilder, new IdentityFunction(), new Sigmoid(), 1, 3, true, false);
        network.checkGradient();
        
    }
    
    public static void main(String[] args) {
        NetworkGradTest test = new NetworkGradTest();
        test.testSimpleSoftmax();
    } 
}
