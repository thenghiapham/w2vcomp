package word2vec;

import io.sentence.TreeInputStream;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;

import common.correlation.MenCorrelation;
import common.correlation.ParsedPhraseCorrelation;
import common.correlation.TwoWordPhraseCorrelation;

import neural.TreeNetwork;
import neural.function.ActivationFunction;
import neural.function.Sigmoid;

import space.CompositionSemanticSpace;
import space.CompositionalSemanticSpace;
import space.ProjectionAdaptorSpace;
import space.SemanticSpace;
import tree.Tree;

public class MultiThreadSentence2Vec extends Sentence2Vec{
    private static final Logger LOGGER = Logger.getLogger(MultiThreadSentence2Vec.class.getName());
    Random random = new Random();
    ActivationFunction hiddenActivationFunction;
    ArrayList<MenCorrelation> wordCorrelations;
    ArrayList<TwoWordPhraseCorrelation> phraseCorrelations;
    ArrayList<ParsedPhraseCorrelation> sentenceCorrelations;
    CompositionalSemanticSpace space;
    SemanticSpace singleWordSpace;
    ArrayList<Tree> testTrees;
    long lastLineCount;

    public MultiThreadSentence2Vec(int hiddenLayerSize, int windowSize,
            boolean hierarchicalSoftmax, int negativeSamples, double subSample, 
            HashMap<String, String> constructionGroups, ActivationFunction hiddenActivationFunction, int phraseHeight, boolean allLevel, boolean lexical) {
        super(hiddenLayerSize, windowSize, hierarchicalSoftmax, negativeSamples,
                subSample, constructionGroups, phraseHeight, 
                allLevel, lexical);
        this.hiddenActivationFunction = hiddenActivationFunction;
        wordCorrelations = new ArrayList<>();
        phraseCorrelations = new ArrayList<>();
        sentenceCorrelations = new ArrayList<>();
        testTrees = new ArrayList<>();
    }
    
    @Override
    public void initNetwork() {
        super.initNetwork();
        space = new CompositionSemanticSpace(projectionMatrix, compositionMatrices, hiddenActivationFunction);
        singleWordSpace = new ProjectionAdaptorSpace(projectionMatrix);
    }
    
    @Override
    public void trainModel(ArrayList<TreeInputStream> inputStreams) {
        // TODO Auto-generated method stub
        lastLineCount = 0;
        System.out.println("line num: " + totalLines);
        System.out.println("vocab size: " + vocab.getVocabSize());
        System.out.println("hidden size: " + hiddenLayerSize);
        System.out.println("first word:" + vocab.getEntry(0).word);
        System.out.println("last word:"
                + vocab.getEntry(vocab.getVocabSize() - 1).word);
        System.out.println("size: " + inputStreams.size());
        
        TrainingThread[] trainingThreads = new TrainingThread[inputStreams.size()];
        for (int i = 0; i < inputStreams.size(); i++) {
            System.out.println("i: " + i);
            TreeInputStream inputStream = inputStreams.get(i);
            TrainingThread thread = new TrainingThread(inputStream);
            trainingThreads[i] = thread;
            thread.start();
            
        }
        try {
            for (TrainingThread thread: trainingThreads) {
                thread.join();
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        
        System.out.println("total read line num: " + this.trainedLines);
    }
    
    

    protected void trainModelThread(TreeInputStream inputStream) {
        // number of trained lines before this stream
        printStatistics();
        long oldTrainedLines = trainedLines;
        long tmpTrainedLines = trainedLines;
        try {
            int iteration = 0;
            while (true) {

                // read the whole sentence sentence,
                // the output would be the list of the word's indices in the
                // dictionary
                Tree parseTree = inputStream.readTree();
                

                // check word count
                // update alpha
                tmpTrainedLines = inputStream.getReadLine();
                synchronized (this) {
                    trainedLines += tmpTrainedLines - oldTrainedLines; 
                }
                oldTrainedLines = tmpTrainedLines;
                
                
                if (parseTree == null) {
                    break;
                } 
                
                synchronized (this) {
                    if (trainedLines - lastLineCount >= 1000) {
                        iteration++;
                        alpha = starting_alpha
                                * (1 - (double) trainedLines / (totalLines + 1));
                        if (alpha < starting_alpha * 0.0001) {
                            alpha = starting_alpha * 0.0001;
                        }
                        lastLineCount = trainedLines;
                        if (iteration % 20 == 0) {
                            printStatistics();
                        }
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
        System.out.println("alpha: " + alpha);
//        System.out.println(projectionMatrix.getMatrix().normF());
//        System.out.println(compositionMatrices.getCompositionMatrix("NN").normF());
//        System.out.println(compositionMatrices.getCompositionMatrix("NP JJ NN").normF());
//        System.out.println(compositionMatrices.getCompositionMatrix("NP NN NN").normF());
//        System.out.println(learningStrategy.getMatrix().normF());
        for (MenCorrelation men : wordCorrelations) {
            double correlation = men.evaluateSpacePearson(singleWordSpace);
            LOGGER.log(Level.INFO, "men: " + correlation);
            System.out.println("men: " + correlation);
        }
        for (TwoWordPhraseCorrelation phraseCorrelation : phraseCorrelations) {
            double correlation = phraseCorrelation.evaluateSpacePearson(space);
            LOGGER.log(Level.INFO, phraseCorrelation.getName() + ": " + correlation);
            System.out.println(phraseCorrelation.getName() + ": " + correlation);
        }
        for (ParsedPhraseCorrelation sentenceCorrelation : sentenceCorrelations) {
            double correlation = sentenceCorrelation.evaluateSpacePearson(space);
            LOGGER.log(Level.INFO, sentenceCorrelation.getName() + ": " + correlation);
            System.out.println(sentenceCorrelation.getName() + ": " + correlation);
        }
    }
    
    protected double computeCost(ArrayList<Tree> parseTrees) {
        double cost = 0;
        for (Tree parseTree: parseTrees) {
            cost += computeCost(parseTree);
        }
        return cost / (parseTrees.size() + 1);
    }
    
    protected double computeCost(Tree parseTree) {
        TreeNetwork network = TreeNetwork.createNetwork(parseTree, projectionMatrix, compositionMatrices, learningStrategy, hiddenActivationFunction, new Sigmoid(), windowSize, phraseHeight, allLevel, lexical);
        return network.computeCost();
    }
    
    protected void trainSentence(Tree parseTree) {
        // TODO Auto-generated method stub
        TreeNetwork network = TreeNetwork.createNetwork(parseTree, projectionMatrix, compositionMatrices, learningStrategy, hiddenActivationFunction, new Sigmoid(), windowSize, phraseHeight, allLevel, lexical);
        network.learn(alpha);
//        if (random.nextDouble() <= 0.0001) {
//            network.checkGradient();
//        }
    }
    
    public void addMenCorrelation(MenCorrelation men) {
        wordCorrelations.add(men);
    }
    
    public void addPhraseCorrelation(TwoWordPhraseCorrelation phrase) {
        phraseCorrelations.add(phrase);
    }
    
    public void addSentenceCorrelation(ParsedPhraseCorrelation sentence) {
        sentenceCorrelations.add(sentence);
    }
    
    public void setTestTrees(ArrayList<Tree> testTrees) {
        this.testTrees = testTrees;
    }
    
    protected class TrainingThread extends Thread {
        TreeInputStream inputStream;
        
        public TrainingThread(TreeInputStream inputStream) {
            this.inputStream = inputStream;
        }
        
        public void run() {
            trainModelThread(inputStream);
        }
    }
}

