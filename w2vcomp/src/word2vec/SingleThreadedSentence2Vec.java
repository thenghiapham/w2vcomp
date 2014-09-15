package word2vec;

import io.sentence.TreeInputStream;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

import common.correlation.MenCorrelation;


//import neural.IdentityFunction;
import neural.Sigmoid;
import neural.Tanh;
import neural.TreeNetwork;

import space.SMSemanticSpace;
import tree.Tree;

public class SingleThreadedSentence2Vec extends Sentence2Vec{
    private static final Logger LOGGER = Logger.getLogger(SingleThreadedSentence2Vec.class.getName());
    
    MenCorrelation men;

    public SingleThreadedSentence2Vec(int hiddenLayerSize, int windowSize,
            boolean hierarchicalSoftmax, int negativeSamples, double subSample, 
            HashMap<String, String> constructionGroups, int phraseHeight, boolean allLevel, boolean lexical) {
        super(hiddenLayerSize, windowSize, hierarchicalSoftmax, negativeSamples,
                subSample, constructionGroups, phraseHeight, allLevel, lexical);
        men = null;
    }
    
    public SingleThreadedSentence2Vec(int hiddenLayerSize, int windowSize,
            boolean hierarchicalSoftmax, int negativeSamples, double subSample, 
            HashMap<String, String> constructionGroups, int phraseHeight, 
            boolean allLevel, boolean lexical, String menCorrelationFile) {
        super(hiddenLayerSize, windowSize, hierarchicalSoftmax, negativeSamples,
                subSample, constructionGroups, phraseHeight, allLevel, lexical);
        men = new MenCorrelation(menCorrelationFile);
        
    }

    @Override
    public void trainModel(ArrayList<TreeInputStream> inputStreams) {
        // TODO Auto-generated method stub
        System.out.println("line num: " + totalLines);
        System.out.println("vocab size: " + vocab.getVocabSize());
        System.out.println("hidden size: " + hiddenLayerSize);
        System.out.println("first word:" + vocab.getEntry(0).word);
        System.out.println("last word:"
                + vocab.getEntry(vocab.getVocabSize() - 1).word);
        
        for (TreeInputStream inputStream : inputStreams) {
            trainModelThread(inputStream);
        }
        System.out.println("total read line num: " + this.trainedLines);
    }

    protected void trainModelThread(TreeInputStream inputStream) {
        // number of trained lines before this stream
        long oldTrainedLines = trainedLines;
        long tmpTrainedLines = trainedLines;
        try {
            int iteration = 0;
            while (true) {

                // read the whole sentence sentence,
                // the output would be the list of the word's indices in the
                // dictionary
                Tree parseTree = inputStream.readTree();
                if (parseTree == null) {
                    trainedLines = oldTrainedLines + inputStream.getReadLine();
                    break;
                } 

                // check word count
                // update alpha
                trainedLines = oldTrainedLines + inputStream.getReadLine();
                
                if (trainedLines - tmpTrainedLines >= 1000) {
                    iteration++;
                    // update alpha
                    // what about thread safe???
                    alpha = starting_alpha
                            * (1 - (double) trainedLines / (totalLines + 1));
                    if (alpha < starting_alpha * 0.0001) {
                        alpha = starting_alpha * 0.0001;
                    }
                    LOGGER.log(Level.INFO, "Trained: " + trainedLines + " lines");
                    LOGGER.log(Level.INFO, "Training rate: " + alpha);
                    tmpTrainedLines = trainedLines;
                    if (iteration % 4 == 0) {
                        printStatistics();
                    }
                }
                
                trainSentence(parseTree);
            }
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(1);
        }
    }

    protected void printStatistics() {
        // TODO Auto-generated method stub
        if (men != null) {
            LOGGER.log(Level.INFO, "men: " + men.evaluateSpacePearson(new SMSemanticSpace(vocab, projectionMatrix.getMatrix(), false)));
        }
        LOGGER.log(Level.INFO, "norm comp: " + compositionMatrices.getCompositionMatrix("blah").normF());
        LOGGER.log(Level.INFO, "norm proj: " + projectionMatrix.getMatrix().normF());
        LOGGER.log(Level.INFO, "norm out: " + learningStrategy.getMatrix().normF());
    }

    protected void trainSentence(Tree parseTree) {
        // TODO Auto-generated method stub
        TreeNetwork network = TreeNetwork.createNetwork(parseTree, projectionMatrix, compositionMatrices, learningStrategy, new Tanh(), new Sigmoid(), windowSize, phraseHeight, allLevel, lexical);
        network.learn(alpha);
    }
    
}
