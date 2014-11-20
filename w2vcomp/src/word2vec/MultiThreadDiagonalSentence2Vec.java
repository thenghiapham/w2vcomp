package word2vec;

import java.util.HashMap;

import neural.DiagonalCompositionMatrices;
import neural.DiagonalTreeNetwork;
import neural.NegativeSamplingLearner;
import neural.ProjectionMatrix;
import neural.RawHierarchicalSoftmaxLearner;
import neural.function.ActivationFunction;
import neural.function.Sigmoid;
import space.DiagonalCompositionSemanticSpace;
import space.ProjectionAdaptorSpace;
import tree.Tree;

public class MultiThreadDiagonalSentence2Vec extends MultiThreadSentence2Vec{

    public MultiThreadDiagonalSentence2Vec(int hiddenLayerSize, int windowSize,
            boolean hierarchicalSoftmax, int negativeSamples, double subSample,
            HashMap<String, String> constructionGroups, ActivationFunction hiddenActivationFunction, int phraseHeight,
            boolean allLevel, boolean lexical) {
        super(hiddenLayerSize, windowSize, hierarchicalSoftmax, negativeSamples,
                subSample, constructionGroups, hiddenActivationFunction, phraseHeight, allLevel, lexical);
        // TODO Auto-generated constructor stub
    }
    
    // call after learning or loading vocabulary
    public void initNetwork() {
        projectionMatrix = ProjectionMatrix.randomInitialize(vocab, hiddenLayerSize);
        if (hierarchicalSoftmax) {
            learningStrategy = RawHierarchicalSoftmaxLearner.zeroInitialize(vocab, hiddenLayerSize);
        } else {
            learningStrategy = NegativeSamplingLearner.zeroInitialize(vocab, negativeSamples, hiddenLayerSize);
        }
        compositionMatrices = DiagonalCompositionMatrices.identityInitialize(constructionGroups, hiddenLayerSize);
        vocab.assignCode();
        
        this.totalLines = vocab.getEntry(0).frequency;
        
        space = new DiagonalCompositionSemanticSpace(projectionMatrix, (DiagonalCompositionMatrices) compositionMatrices, hiddenActivationFunction);
        singleWordSpace = new ProjectionAdaptorSpace(projectionMatrix);
    }
    
    protected double computeCost(Tree parseTree) {
        DiagonalTreeNetwork network = DiagonalTreeNetwork.createNetwork(parseTree, projectionMatrix, (DiagonalCompositionMatrices) compositionMatrices, learningStrategy, hiddenActivationFunction, new Sigmoid(), windowSize, phraseHeight, allLevel, lexical);
        return network.computeCost();
    }

    protected void trainSentence(Tree parseTree) {
        // TODO Auto-generated method stub
        if (parseTree == null) return;
        DiagonalTreeNetwork network = DiagonalTreeNetwork.createNetwork(parseTree, projectionMatrix, (DiagonalCompositionMatrices) compositionMatrices, learningStrategy, hiddenActivationFunction, new Sigmoid(), windowSize, phraseHeight, allLevel, lexical);
        if (network != null)
            network.learn(alpha);
//        if (random.nextDouble() <= 0.0001) {
//            network.checkGradient();
//        }
    }

}
