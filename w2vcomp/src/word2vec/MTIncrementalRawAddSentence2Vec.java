package word2vec;

import io.sentence.BasicTreeInputStream;
import io.sentence.TreeInputStream;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;

import common.correlation.MenCorrelation;
import common.correlation.ParsedPhraseCorrelation;
import common.correlation.TwoWordPhraseCorrelation;
import composition.WeightedAdditive;
import neural.IncrementalRawAddTreeNetwork;
import neural.function.ActivationFunction;
import space.RawSemanticSpace;
import space.SemanticSpace;
import tree.Tree;

public class MTIncrementalRawAddSentence2Vec extends Sentence2Vec{
    private static final Logger LOGGER = Logger.getLogger(MTIncrementalRawAddSentence2Vec.class.getName());
    Random random = new Random();
    ArrayList<MenCorrelation> wordCorrelations;
    ArrayList<TwoWordPhraseCorrelation> phraseCorrelations;
    ArrayList<ParsedPhraseCorrelation> sentenceCorrelations;
    ArrayList<Tree> testTrees;
    long lastLineCount;
    protected int incrementalStep;
    SemanticSpace space;
    
    double[][] weights0, weights1, negativeWeights1;
    UniGram unigram;
    Random rand;
    
    public MTIncrementalRawAddSentence2Vec(int hiddenLayerSize, int windowSize,
            boolean hierarchicalSoftmax, int negativeSamples, double subSample, 
            HashMap<String, String> constructionGroups, 
            ActivationFunction hiddenActivationFunction, int phraseHeight, 
            boolean allLevel, boolean lexical, int incrementalStep) {
        super(hiddenLayerSize, windowSize, hierarchicalSoftmax, negativeSamples,
                subSample, hiddenActivationFunction, constructionGroups, phraseHeight, 
                allLevel, lexical);
        this.incrementalStep = incrementalStep;
        wordCorrelations = new ArrayList<>();
        phraseCorrelations = new ArrayList<>();
        sentenceCorrelations = new ArrayList<>();
        testTrees = new ArrayList<>();
        rand = new Random();
    }
    
    protected void initBare() {
        int vocabSize = vocab.getVocabSize();
        if (negativeSamples > 0) {
            unigram = new UniGram(vocab);
        }
        weights0 = new double[vocabSize][hiddenLayerSize];
        if (hierarchicalSoftmax) {
            weights1 = new double[vocabSize - 1][hiddenLayerSize];
        }
        if (negativeSamples > 0) {
            negativeWeights1 = new double[vocabSize][hiddenLayerSize];
//            negativeWeights1 = weights0;
        }
        vocab.assignCode();
    }
    
    protected void randomInitProjectionMatrices() {
        for (int i = 0; i < vocab.getVocabSize(); i++) {
            for (int j = 0; j < hiddenLayerSize; j++) {
                weights0[i][j] = (double) (rand.nextFloat() - 0.5)
                        / hiddenLayerSize;
            }
        }
    }
    
    @Override
    public void initNetwork() {
        projectionMatrix = null;
        compositionMatrices = null;
        learningStrategy = null;
        
        initBare();
        randomInitProjectionMatrices();
        space = new RawSemanticSpace(vocab, weights0, false);
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
            String[] history = new String[0];
            String[] future = new String[0];
            String[] sentence = new String[0];
            int iteration = 0;
            Tree currentTree = null;
            Tree nextTree = null;
            while (true) {

                // read the whole sentence sentence,
                // the output would be the list of the word's indices in the
                // dictionary
                nextTree = inputStream.readTree();
                

                // check word count
                // update alpha
                tmpTrainedLines = inputStream.getReadLine();
                synchronized (this) {
                    trainedLines += tmpTrainedLines - oldTrainedLines; 
                }
                oldTrainedLines = tmpTrainedLines;
                
                synchronized (this) {
                    if (trainedLines - lastLineCount >= 1000) {
                        iteration++;
                        alpha = starting_alpha
                                * (1 - (double) trainedLines / (totalLines + 1));
                        if (alpha < starting_alpha * 0.0001) {
                            alpha = starting_alpha * 0.0001;
                        }
                        lastLineCount = trainedLines;
                        if (iteration % 2 == 0) {
                            printStatistics();
                        }
                    }
                }
                
                // TODO: do some smart thing about the document boundary
                if (nextTree == null || nextTree == BasicTreeInputStream.NEXT_DOC_TREE) {
                    future = new String[0];
                } else {
                    future = nextTree.getSurfaceWords();
                }
                
                if (currentTree != null && currentTree != BasicTreeInputStream.NEXT_DOC_TREE) {
                    trainSentence(currentTree, history, sentence, future);
                }
                
                if (nextTree == null) {
                    break;
                }
                // move on
                currentTree = nextTree;
                sentence = future;
                history = sentence;
                
                
            }
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(1);
        }
    }

    protected void printStatistics() {
        // TODO Auto-generated method stub
        System.out.println("alpha: " + alpha);
        for (MenCorrelation men : wordCorrelations) {
            double correlation = men.evaluateSpacePearson(space);
            LOGGER.log(Level.INFO, "men: " + correlation);
            System.out.println("men: " + correlation);
        }
        for (TwoWordPhraseCorrelation phraseCorrelation : phraseCorrelations) {
            double correlation = phraseCorrelation.evaluateSpacePearson(space, new WeightedAdditive());
            LOGGER.log(Level.INFO, phraseCorrelation.getName() + ": " + correlation);
            System.out.println(phraseCorrelation.getName() + ": " + correlation);
        }
        for (ParsedPhraseCorrelation sentenceCorrelation : sentenceCorrelations) {
            double correlation = sentenceCorrelation.evaluateSpacePearson(space, new WeightedAdditive());
            LOGGER.log(Level.INFO, sentenceCorrelation.getName() + ": " + correlation);
            System.out.println(sentenceCorrelation.getName() + ": " + correlation);
        }
    }
    
    
    protected void trainSentence(Tree parseTree, String[] history, String[] sentence, String[] future) {
        // TODO Auto-generated method stub
        parseTree.updatePosition(history.length);
        parseTree.updateHeight();
        
        String[] historyPresentFuture = new String[history.length + sentence.length + future.length];
        System.arraycopy(history, 0, historyPresentFuture, 0, history.length);
        System.arraycopy(sentence, 0, historyPresentFuture, history.length, sentence.length);
        System.arraycopy(future, 0, historyPresentFuture, history.length + sentence.length, future.length);
        ArrayList<Tree> reversedNodes = parseTree.allNodes();
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
            IncrementalRawAddTreeNetwork.createNetworkAndLearn(
                    subTree, parseTree, historyPresentFuture, vocab, unigram,
                    weights0, weights1, negativeWeights1, hierarchicalSoftmax,
                    negativeSamples, windowSize, 
                    subSample, incrementalStep, alpha);
        }
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

