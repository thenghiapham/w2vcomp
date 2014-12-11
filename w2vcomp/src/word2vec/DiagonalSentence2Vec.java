package word2vec;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.HashMap;

import common.IOUtils;

import neural.DiagonalCompositionMatrices;
import neural.DiagonalTreeNetwork;
import neural.NegativeSamplingLearner;
import neural.ProjectionMatrix;
import neural.HierarchicalSoftmaxLearner;
import neural.function.ActivationFunction;
import neural.function.Sigmoid;
import space.DiagonalCompositionSemanticSpace;
import space.ProjectionAdaptorSpace;
import tree.Tree;

public class DiagonalSentence2Vec extends SingleThreadedSentence2Vec{

    public DiagonalSentence2Vec(int hiddenLayerSize, int windowSize,
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
        compositionMatrices = DiagonalCompositionMatrices.identityInitialize(constructionGroups, hiddenLayerSize);
        vocab.assignCode();
        
        this.totalLines = vocab.getEntry(0).frequency;
        
        space = new DiagonalCompositionSemanticSpace(projectionMatrix, (DiagonalCompositionMatrices) compositionMatrices, hiddenActivationFunction);
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
                learningStrategy = NegativeSamplingLearner.zeroInitialize(vocab, negativeSamples, hiddenLayerSize);
            }
            compositionMatrices = DiagonalCompositionMatrices.identityInitialize(constructionGroups, hiddenLayerSize);
            vocab.assignCode();
            
            this.totalLines = vocab.getEntry(0).frequency;
            inputStream.close();
            space = new DiagonalCompositionSemanticSpace(projectionMatrix, (DiagonalCompositionMatrices) compositionMatrices, hiddenActivationFunction);
            singleWordSpace = new ProjectionAdaptorSpace(projectionMatrix);
        } catch (IOException e) {
            e.printStackTrace();
        }
        
    }
    
    protected double computeCost(Tree parseTree) {
        DiagonalTreeNetwork network = DiagonalTreeNetwork.createNetwork(parseTree, projectionMatrix, (DiagonalCompositionMatrices) compositionMatrices, learningStrategy, hiddenActivationFunction, new Sigmoid(), windowSize, phraseHeight, allLevel, lexical);
        return network.computeCost();
    }

    protected void trainSentence(Tree parseTree) {
        // TODO Auto-generated method stub
        DiagonalTreeNetwork network = DiagonalTreeNetwork.createNetwork(parseTree, projectionMatrix, (DiagonalCompositionMatrices) compositionMatrices, learningStrategy, hiddenActivationFunction, new Sigmoid(), windowSize, phraseHeight, allLevel, lexical);
        network.learn(alpha);
//        if (random.nextDouble() <= 0.0001) {
//            network.checkGradient();
//        }
    }

}
