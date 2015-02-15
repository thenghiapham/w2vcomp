package neural;

import java.util.ArrayList;
import java.util.Random;

import common.MathUtils;
import common.SigmoidTable;
import neural.layer.OutputLayer;
import tree.Tree;
import vocab.Vocab;
import vocab.VocabEntry;
import word2vec.UniGram;

public class IncrementalRawAddTreeNetwork {
    public static SigmoidTable sigmoidTable = new SigmoidTable();
    private Random rand = new Random();
    String[] surfaceWords;
    int incrementalStep;
    ArrayList<OutputLayer> outputLayers;
    double[][] weights0, weights1, negativeWeights1;
    UniGram unigram;
    protected ArrayList<Integer> inputVectorIndices;
    
    int count;
    
    int hiddenLayerSize;
    Tree parseTree;
    double[] projectLayer;
    
    protected IncrementalRawAddTreeNetwork(Tree parseTree, int incrementalStep) {
        if (parseTree != null) {
            surfaceWords = parseTree.getSurfaceWords();
        }
        this.parseTree = parseTree;
        this.incrementalStep = incrementalStep; 
        // TODO Auto-generated constructor stub
        inputVectorIndices = new ArrayList<>();
        outputLayers = new ArrayList<>();
    }
    
    // trees should already have updated height & width information
    // left position should be the length of history
    // concatenating concatenating 
    public static void createNetworkAndLearn(Tree parseTree, Tree rootTree, 
            String[] historyPresentFuture, Vocab vocab, UniGram unigram, 
            double[][] weights0, double[][] weights1, double[][] negativeWeights1,
            boolean hierarchicalSoftmax, int negativeSamples,
            int maxWindowSize, double subSample, int incrementalStep, double alpha) {
        IncrementalRawAddTreeNetwork network = new IncrementalRawAddTreeNetwork(parseTree, incrementalStep);
        network.weights0 = weights0;
        network.weights1 = weights1;
        network.negativeWeights1 = negativeWeights1;
        network.unigram = unigram;
        
        String[] surfaceWords = parseTree.getSurfaceWords();
        
        //TODO: remove
        int count = 0;
        int hiddenLayerSize = weights0[0].length;
        double[] projectLayer = new double[hiddenLayerSize];
        for (int i = 0; i < surfaceWords.length; i++) {
            String word = surfaceWords[i];
            int wordIndex = vocab.getWordIndex(word);
            if (wordIndex == -1) continue;
            network.inputVectorIndices.add(wordIndex);
            count ++;
            for (int j = 0; j < hiddenLayerSize; j++) {
                projectLayer[j] += weights0[wordIndex][j];
            }
        }
        network.count = count;
        if (count == 0) return;
        for (int j = 0; j < hiddenLayerSize; j++) {
            projectLayer[j] /= count;
        }
        
        network.projectLayer = projectLayer;
        network.addOutputLayers(rootTree, historyPresentFuture, vocab, hierarchicalSoftmax, negativeSamples, maxWindowSize, subSample, alpha);
    }
    
    protected void addOutputLayers(Tree rootTree, String[] historyPresentFuture, Vocab vocab, boolean hierarchicalSoftmax, int negativeSamples, 
            int maxWindowSize, double subSample, double alpha) {

        // get the 
        String[] sentence = historyPresentFuture;
        Random random = new Random();
        
        Tree node = parseTree;
        int height = parseTree.getHeight();
        int windowSize = random.nextInt(maxWindowSize + (incrementalStep * height) -1) + 1;
        
        // get the left and right position of the phrase
        // pick k words to the left and k words to the right to train the phrase 
        // (k = windowSize)
        double[] a1error = new double[hiddenLayerSize];
        
        for (int i = node.getLeftmostPosition() - windowSize; i <= node.getRightmostPosition() + windowSize; i++) {
            if ((i >= 0 && i < sentence.length && (i < node.getLeftmostPosition() || i > node.getRightmostPosition()))) {
                // adding the output layers to the hidden/projection layer 
                // corresponding to the phrase 
                // HIERARCHICAL SOFTMAX
                String contextWord = sentence[i];
                
                int contextWordIndex = vocab.getWordIndex(contextWord);
                if (contextWordIndex == -1) return;
                
                VocabEntry contextEntry = vocab.getEntry(contextWordIndex);
                // subSample (not check null)
                long frequency = contextEntry.frequency;
                long totalCount = vocab.getTrainWords();
                if (subSample >0 && !MathUtils.isSampled(frequency, totalCount, subSample)) continue;
                
                
                if (hierarchicalSoftmax) {
                    for (int bit = 0; bit < contextEntry.code.length(); bit++) {
                        double z2 = 0;
                        int iParentIndex = contextEntry.ancestors[bit];
                        // Propagate hidden -> output
                        for (int j = 0; j < hiddenLayerSize; j++) {
                            z2 += projectLayer[j]
                                    * weights1[iParentIndex][j];
                        }

                        double a2 = sigmoidTable.getSigmoid(z2);
                        if (a2 == 0 || a2 == 1)
                            continue;
                        // 'g' is the gradient multiplied by the learning
                        // rate
                        // not alpha yet
                        // TODO: put alpha here?
                        double gradient = (double) ((1 - (contextEntry.code
                                .charAt(bit) - 48) - a2) * alpha);
                        // Propagate errors output -> hidden
                        for (int j = 0; j < hiddenLayerSize; j++) {
                            a1error[j] += gradient
                                    * weights1[iParentIndex][j];
                        }
                        // Learn weights hidden -> output
                        for (int j = 0; j < hiddenLayerSize; j++) {
                            weights1[iParentIndex][j] += gradient
                                    * projectLayer[j];
                        }
                    }
                }

                // NEGATIVE SAMPLING
                if (negativeSamples > 0) {
                    for (int l = 0; l < negativeSamples + 1; l++) {
                        int target;
                        int label;

                        if (l == 0) {
                            target = contextWordIndex;
                            label = 1;
                        } else {
                            target = unigram.randomWordIndex();
                            if (target == 0) {
                                target = rand
                                        .nextInt(vocab.getVocabSize() - 1) + 1;
                            }
                            if (target == contextWordIndex)
                                continue;
                            label = 0;
                        }
                        double z2 = 0;
                        double gradient;
                        for (int j = 0; j < hiddenLayerSize; j++) {
                            z2 += projectLayer[j]
                                    * negativeWeights1[target][j];
                        }
                        double a2 = sigmoidTable.getSigmoid(z2);
                        gradient = (double) ((label - a2) * alpha);
                        for (int j = 0; j < hiddenLayerSize; j++) {
                            a1error[j] += gradient
                                    * negativeWeights1[target][j];
                        }
                        for (int j = 0; j < hiddenLayerSize; j++) {
                            negativeWeights1[target][j] += gradient
                                    * projectLayer[j];
                        }
                        for (int j = 0; j < hiddenLayerSize; j++) {
                            System.out.print(negativeWeights1[target][j]);
                            System.out.println();
                        }
                    }
                }
                
                
            }
        }
        //  Learn weights input -> hidden
        for (int j = 0; j < hiddenLayerSize; j++) {
            a1error[j] /= count;
        }
        for (int i = 0; i < inputVectorIndices.size(); i++) {
            int wordIndex = inputVectorIndices.get(i);
            for (int j = 0; j < hiddenLayerSize; j++) {
                weights0[wordIndex][j] += a1error[j];
            }
            for (int j = 0; j < hiddenLayerSize; j++) {
                System.out.println("weight 0");
                System.out.print(weights0[wordIndex][j]);
                System.out.println();
            }
            if (rand.nextInt(1000) < 2) System.exit(1);
        }
        
    }
}
