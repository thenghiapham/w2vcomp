package word2vec;

import java.util.HashMap;

import neural.DiagonalCompositionMatrices;
import neural.DiagonalTreeNetwork;
import neural.NegativeSamplingLearner;
import neural.ProjectionMatrix;
import neural.RawHierarchicalSoftmaxLearner;
import neural.function.IdentityFunction;
import neural.function.Sigmoid;
import tree.Tree;

public class DiagonalSentence2Vec extends SingleThreadedSentence2Vec{

    public DiagonalSentence2Vec(int hiddenLayerSize, int windowSize,
            boolean hierarchicalSoftmax, int negativeSamples, double subSample,
            HashMap<String, String> constructionGroups, int phraseHeight,
            boolean allLevel, boolean lexical) {
        super(hiddenLayerSize, windowSize, hierarchicalSoftmax, negativeSamples,
                subSample, constructionGroups, phraseHeight, allLevel, lexical);
        // TODO Auto-generated constructor stub
    }
    
    public DiagonalSentence2Vec(int hiddenLayerSize, int windowSize,
            boolean hierarchicalSoftmax, int negativeSamples, double subSample, 
            HashMap<String, String> constructionGroups, int phraseHeight, 
            boolean allLevel, boolean lexical, String menCorrelationFile) {
        super(hiddenLayerSize, windowSize, hierarchicalSoftmax, negativeSamples,
                subSample, constructionGroups, phraseHeight, allLevel, lexical, menCorrelationFile);
    }
    
    // call after learning or loading vocabulary
    public void initNetwork() {
        projectionMatrix = ProjectionMatrix.randomInitialize(vocab, hiddenLayerSize);
        if (hierarchicalSoftmax) {
            learningStrategy = RawHierarchicalSoftmaxLearner.zeroInitialize(vocab, hiddenLayerSize);
        } else {
            learningStrategy = NegativeSamplingLearner.zeroInitialize(vocab, negativeSamples, hiddenLayerSize);
        }
        compositionMatrices = DiagonalCompositionMatrices.randomInitialize(constructionGroups, hiddenLayerSize);
        vocab.assignCode();
        
        this.totalLines = vocab.getEntry(0).frequency;
    }

    protected void trainSentence(Tree parseTree) {
        // TODO Auto-generated method stub
//        DiagonalTreeNetwork network = DiagonalTreeNetwork.createNetwork(parseTree, projectionMatrix, (DiagonalCompositionMatrices) compositionMatrices, learningStrategy, new Tanh(), new Sigmoid(), windowSize, phraseHeight, allLevel, lexical);
        DiagonalTreeNetwork network = DiagonalTreeNetwork.createNetwork(parseTree, projectionMatrix, (DiagonalCompositionMatrices) compositionMatrices, learningStrategy, new IdentityFunction(), new Sigmoid(), windowSize, phraseHeight, allLevel, lexical);
//        System.out.println(parseTree);
//        System.out.println(network.toString());
//        System.out.println(parseTree);
        network.learn(alpha);
//        if (random.nextDouble() <= 0.0001) {
//            network.checkGradient();
//        }
    }

}
