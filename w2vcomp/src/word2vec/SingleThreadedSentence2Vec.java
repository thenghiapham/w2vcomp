package word2vec;

import io.sentence.TreeInputStream;

import java.io.IOException;
import java.util.ArrayList;

import common.MenCorrelation;

import neural.IdentityFunction;
import neural.Sigmoid;
import neural.TreeNetwork;

import space.NewSemanticSpace;
import tree.Tree;

public class SingleThreadedSentence2Vec extends Sentence2Vec{
    MenCorrelation men;

    public SingleThreadedSentence2Vec(int hiddenLayerSize, int windowSize,
            boolean hierarchicalSoftmax, int negativeSamples, double subSample, int phraseHeight, boolean allLevel) {
        super(hiddenLayerSize, windowSize, hierarchicalSoftmax, negativeSamples,
                subSample, phraseHeight, allLevel);
        men = null;
    }
    
    public SingleThreadedSentence2Vec(int hiddenLayerSize, int windowSize,
            boolean hierarchicalSoftmax, int negativeSamples, double subSample, int phraseHeight, boolean allLevel, String menCorrelationFile) {
        super(hiddenLayerSize, windowSize, hierarchicalSoftmax, negativeSamples,
                subSample, phraseHeight, allLevel);
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
                    System.out.println("Trained: " + trainedLines + " lines");
                    System.out.println("Training rate: " + alpha);
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
            System.out.println("men: " + men.evaluateSpacePearson(new NewSemanticSpace(vocab, projectionMatrix.getMatrix(), false)));
        }
        System.out.println("norm comp: " + compositionMatrices.getCompositionMatrix("blah").normF());
        System.out.println("norm proj: " + projectionMatrix.getMatrix().normF());
        System.out.println("norm out: " + learningStrategy.getMatrix().normF());
    }

    protected void trainSentence(Tree parseTree) {
        // TODO Auto-generated method stub
        TreeNetwork network = TreeNetwork.createNetwork(parseTree, projectionMatrix, compositionMatrices, learningStrategy, new IdentityFunction(), new Sigmoid(), windowSize, phraseHeight, allLevel);
        network.learn(alpha);
    }
    
}
