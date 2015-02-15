package neural;

import java.util.ArrayList;
import java.util.Random;

import org.ejml.simple.SimpleMatrix;

import common.MathUtils;
import neural.LearningStrategy;
import neural.ProjectionMatrix;
import neural.function.ActivationFunction;
import neural.function.ObjectiveFunction;
import neural.layer.Layer;
import neural.layer.OutputLayer;
import neural.layer.ProjectionLayer;
import tree.Tree;
import vocab.Vocab;

public class IncrementalAddTreeNetwork {
    String[] surfaceWords;
    int incrementalStep;
    ProjectionMatrix projectionBuilder;
    LearningStrategy outputBuilder;
    ProjectionLayer projectionLayer;
    ArrayList<OutputLayer> outputLayers;
    protected ArrayList<Integer> inputVectorIndices;
    protected ArrayList<int[]> outVectorIndices;
    int count;
    Tree parseTree;
    
    
    protected IncrementalAddTreeNetwork(Tree parseTree, int incrementalStep) {
        if (parseTree != null) {
            surfaceWords = parseTree.getSurfaceWords();
        }
        this.parseTree = parseTree;
        this.incrementalStep = incrementalStep; 
        // TODO Auto-generated constructor stub
        inputVectorIndices = new ArrayList<>();
        outVectorIndices = new ArrayList<>();
        outputLayers = new ArrayList<>();
    }
    
    // trees should already have updated height & width information
    // left position should be the length of history
    // concatenating concatenating 
    public static IncrementalAddTreeNetwork createNetwork(Tree parseTree, Tree rootTree, 
            String[] historyPresentFuture, Vocab vocab, 
            ProjectionMatrix projectionBuilder, LearningStrategy outputBuilder,
            ActivationFunction hiddenLayerActivation, ActivationFunction outputLayerActivation,
            int maxWindowSize, double subSample, int incrementalStep) {
        IncrementalAddTreeNetwork network = new IncrementalAddTreeNetwork(parseTree, incrementalStep);
        network.projectionBuilder = projectionBuilder;
        network.outputBuilder = outputBuilder;
        
        String[] surfaceWords = parseTree.getSurfaceWords();
        network.addOutputLayers(rootTree, historyPresentFuture, vocab, outputBuilder, outputLayerActivation, maxWindowSize, subSample);
        
        //TODO: remove
        int count = 0;
        SimpleMatrix sumMatrix = null;
        for (int i = 0; i < surfaceWords.length; i++) {
            SimpleMatrix matrix = projectionBuilder.getVector(surfaceWords[i]);
            if (matrix == null) continue;
            count ++;
            if (sumMatrix == null) 
                sumMatrix = matrix;
            else
                sumMatrix = sumMatrix.plus(matrix);
        }
        network.count = count;
        if (sumMatrix == null) return null;
        sumMatrix = sumMatrix.scale(1.0 / count);
        network.projectionLayer = new ProjectionLayer(sumMatrix);
        network.addOutputLayers(rootTree, historyPresentFuture, vocab, outputBuilder, outputLayerActivation, maxWindowSize, subSample);
        
        return network;
    }
    
    protected void addOutputLayers(Tree rootTree, String[] historyPresentFuture, Vocab vocab, LearningStrategy outputBuilder, 
            ActivationFunction outputLayerActivation, int maxWindowSize, double subSample) {

        // get the 
        String[] sentence = historyPresentFuture;
        Random random = new Random();
        
        Tree node = parseTree;
        int width = node.getRightmostPosition() - node.getLeftmostPosition() + 1;
        int height = parseTree.getHeight();
//        double significant = 1 / (double) width;
        double significant = 1;
        double inputCoefficient = 1 / (double) width;
        int windowSize = random.nextInt(maxWindowSize + (incrementalStep * height) -1) + 1;
//            int windowSize = maxWindowSize;
        
        // get the left and right position of the phrase
        // pick k words to the left and k words to the right to train the phrase 
        // (k = windowSize)
        for (int i = node.getLeftmostPosition() - windowSize; i <= node.getRightmostPosition() + windowSize; i++) {
            if ((i >= 0 && i < sentence.length && (i < node.getLeftmostPosition() || i > node.getRightmostPosition()))) {
                
                // adding the output layers to the hidden/projection layer 
                // corresponding to the phrase 
                
                
                int[] indices = outputBuilder.getOutputIndices(sentence[i]);
                if (indices == null) continue;
                
                // subSample (not check null)
                long frequency = vocab.getEntry(sentence[i]).frequency;
                long totalCount = vocab.getTrainWords();
                if (subSample >0 && !MathUtils.isSampled(frequency, totalCount, subSample)) continue;
                
                SimpleMatrix weightMatrix = outputBuilder.getOutputWeights(indices);
                SimpleMatrix goldMatrix = outputBuilder.getGoldOutput(sentence[i]);
                
                ObjectiveFunction costFunction = outputBuilder.getCostFunction();
                OutputLayer outputLayer = new OutputLayer(weightMatrix, outputLayerActivation, goldMatrix, costFunction, significant, inputCoefficient);
                outputLayer.addInLayer(projectionLayer);
                projectionLayer.addOutLayer(outputLayer);
                
                addOutputLayer(outputLayer, indices);
            }
            
        }
    }

    public void addOutputLayer(OutputLayer layer, int[] outVectorIndex) {
        outputLayers.add(layer);
        outVectorIndices.add(outVectorIndex);
    }

    public void learn(double alpha) {
        // TODO Auto-generated method stub
        for (Layer layer :outputLayers) {
            layer.forward();
        }
        for (Layer layer :outputLayers) {
            layer.backward();
        }
        projectionLayer.backward();
        
    }
    
    /**
     * Updating the global components
     * @param learningRate
     */
    public void update(double learningRate) {
        
        // updating the projection matrix
        SimpleMatrix projectGrad = projectionLayer.getGradient();
        for (int i = 0; i < inputVectorIndices.size(); i++) {
            int wordIndex = inputVectorIndices.get(i);
            if (projectGrad == null) {
                continue;
            }
            projectionBuilder.updateVector(wordIndex, 
                    projectGrad, learningRate / count);
        }
        
        // updating the hierarchical softmax or the negative sampling layer
        for (int i = 0; i < outputLayers.size(); i++) {
            outputBuilder.updateMatrix(outVectorIndices.get(i), 
                    outputLayers.get(i).getGradient(), learningRate);
        }
    }
}
