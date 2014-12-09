package word2vec;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.HashMap;

import org.ejml.simple.SimpleMatrix;

import common.IOUtils;

import neural.NegativeSamplingLearner;
import neural.ProjectionMatrix;
import neural.HierarchicalSoftmaxLearner;
import neural.WeightedCompositionMatrices;
import neural.WeightedTreeNetwork;
import neural.function.ActivationFunction;
import neural.function.Sigmoid;
import space.WeightedCompositionSemanticSpace;
import space.ProjectionAdaptorSpace;
import tree.Tree;

public class MultiThreadWeightedSentence2Vec extends MultiThreadSentence2Vec{

    public MultiThreadWeightedSentence2Vec(int hiddenLayerSize, int windowSize,
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
            learningStrategy = HierarchicalSoftmaxLearner.zeroInitialize(vocab, hiddenLayerSize);
        } else {
            learningStrategy = NegativeSamplingLearner.zeroInitialize(vocab, negativeSamples, hiddenLayerSize);
        }
        compositionMatrices = WeightedCompositionMatrices.identityInitialize(constructionGroups, hiddenLayerSize);
        vocab.assignCode();
        
        this.totalLines = vocab.getEntry(0).frequency;
        
        space = new WeightedCompositionSemanticSpace(projectionMatrix, (WeightedCompositionMatrices) compositionMatrices, hiddenActivationFunction);
        singleWordSpace = new ProjectionAdaptorSpace(projectionMatrix);
    }
    
    public void initNetwork(String wordModelFile) {
        try {
            BufferedInputStream inputStream = new BufferedInputStream(new FileInputStream(wordModelFile));
            double[][] rawMatrix = IOUtils.readMatrix(inputStream, true);
            projectionMatrix = ProjectionMatrix.initializeFromMatrix(vocab, rawMatrix);
            rawMatrix = IOUtils.readMatrix(inputStream, true);
            if (hierarchicalSoftmax) {
                learningStrategy = HierarchicalSoftmaxLearner.initializeFromMatrix(vocab, rawMatrix);
            } else {
                learningStrategy = NegativeSamplingLearner.initializeFromMatrix(vocab, negativeSamples, rawMatrix);
            }
            compositionMatrices = WeightedCompositionMatrices.identityInitialize(constructionGroups, hiddenLayerSize);
            vocab.assignCode();
            
            this.totalLines = vocab.getEntry(0).frequency;
            inputStream.close();
            space = new WeightedCompositionSemanticSpace(projectionMatrix, (WeightedCompositionMatrices) compositionMatrices, hiddenActivationFunction);
            singleWordSpace = new ProjectionAdaptorSpace(projectionMatrix);
        } catch (IOException e) {
            e.printStackTrace();
        }
        
    }
    
    protected double computeCost(Tree parseTree) {
        WeightedTreeNetwork network = WeightedTreeNetwork.createNetwork(parseTree, projectionMatrix, (WeightedCompositionMatrices) compositionMatrices, learningStrategy, hiddenActivationFunction, new Sigmoid(), windowSize, phraseHeight, allLevel, lexical);
        return network.computeCost();
    }

    protected void trainSentence(Tree parseTree) {
        // TODO Auto-generated method stub
        if (parseTree == null) return;
        WeightedTreeNetwork network = WeightedTreeNetwork.createNetwork(parseTree, projectionMatrix, (WeightedCompositionMatrices) compositionMatrices, learningStrategy, hiddenActivationFunction, new Sigmoid(), windowSize, phraseHeight, allLevel, lexical);
        if (network != null)
            network.learn(alpha);
//        if (random.nextDouble() <= 0.0001) {
//            network.checkGradient();
//        }
    }
    
    @Override
    protected void printStatistics() {
        super.printStatistics();
        SimpleMatrix anMatrix = compositionMatrices.getCompositionMatrix("@NP JJ NN");
        System.out.println("an mat:" + anMatrix.get(0) + " " + anMatrix.get(1));
        SimpleMatrix nnMatrix = compositionMatrices.getCompositionMatrix("@NP NN NN");
        System.out.println("nn mat:" + nnMatrix.get(0) + " " + nnMatrix.get(1));
    }

}
