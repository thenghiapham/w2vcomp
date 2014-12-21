package word2vec;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import neural.HierarchicalSoftmaxLearner;
import neural.InnerWeightedNetwork;
import neural.LearningStrategy;
import neural.NegativeSamplingLearner;
import neural.ProjectionMatrix;
import neural.WeightedCompositionMatrices;
import neural.function.ActivationFunction;
import neural.function.Sigmoid;

import org.ejml.simple.SimpleMatrix;

import space.ProjectionAdaptorSpace;
import space.WeightedCompositionSemanticSpace;
import tree.Tree;
import common.IOUtils;
import common.exception.UnimplementedException;

public class MTWeightedInnerWord2Vec extends MTWeightedSingleObjectSentence2Vec{
    protected LearningStrategy inLearningStrategy; 
    public MTWeightedInnerWord2Vec(int hiddenLayerSize, int windowSize,
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
            inLearningStrategy = HierarchicalSoftmaxLearner.zeroInitialize(vocab, hiddenLayerSize);
        } else {
            learningStrategy = NegativeSamplingLearner.zeroInitialize(vocab, negativeSamples, hiddenLayerSize);
            inLearningStrategy = NegativeSamplingLearner.zeroInitialize(vocab, negativeSamples, hiddenLayerSize);
        }
        compositionMatrices = WeightedCompositionMatrices.identityInitialize(constructionGroups, hiddenLayerSize);
        vocab.assignCode();
        
        this.totalLines = vocab.getEntry(0).frequency;
        
        space = new WeightedCompositionSemanticSpace(projectionMatrix, (WeightedCompositionMatrices) compositionMatrices, hiddenActivationFunction);
        singleWordSpace = new ProjectionAdaptorSpace(projectionMatrix);
    }
    
 // call after learning or loading vocabulary
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
        throw new UnimplementedException("Not available");
    }
    

    protected void trainSentence(Tree parseTree, String[] history, String[] sentence, String[] future) {
        // TODO Auto-generated method stub
        parseTree.updatePosition(history.length);
        parseTree.updateHeight();
        
        String[] historyPresentFuture = new String[history.length + sentence.length + future.length];
        System.arraycopy(history, 0, historyPresentFuture, 0, history.length);
        System.arraycopy(sentence, 0, historyPresentFuture, history.length, sentence.length);
        System.arraycopy(future, 0, historyPresentFuture, history.length + sentence.length, future.length);
        List<Tree> reversedNodes = parseTree.allNodes();
        // TODO: change this
//        Collections.reverse(reversedNodes);
        Collections.shuffle(reversedNodes);
        for (Tree subTree: reversedNodes) {
            int height = subTree.getHeight();
            if (height == 0) continue;
            if (!allLevel) {
                if (phraseHeight != -1 && height > phraseHeight)
                    continue;
                if (phraseHeight == -1 && height != parseTree.getHeight()) 
                    continue;
                if (!lexical && height == 1) 
                    continue;
            } else {
                if (!lexical && height == 1) 
                    continue;
                if (height > phraseHeight)
                    continue;
            }
            InnerWeightedNetwork network = InnerWeightedNetwork.createNetwork(subTree, parseTree, historyPresentFuture,
                    vocab, projectionMatrix, (WeightedCompositionMatrices) compositionMatrices, learningStrategy, inLearningStrategy, 
                    hiddenActivationFunction, new Sigmoid(), windowSize, subSample);
            // TODO: fix here
            if (network != null)
                network.learn(alpha);
        }
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
