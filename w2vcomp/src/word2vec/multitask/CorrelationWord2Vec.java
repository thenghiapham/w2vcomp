package word2vec.multitask;

import io.sentence.SentenceInputStream;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

import common.DataStructureUtils;
import common.correlation.MenCorrelation;
import common.exception.ValueException;
import neural.function.Correlation;
import word2vec.MultiThreadWord2Vec;

public abstract class CorrelationWord2Vec extends MultiThreadWord2Vec{
    Boolean isSomeThreadTraining = false;
    protected ArrayList<Correlation> trainedCorrelations = new ArrayList<Correlation>();
    protected ArrayList<double[][]> trainedCorVectors = new ArrayList<double[][]>();
    protected ArrayList<int[][]> trainedPairs = new ArrayList<int[][]>();
    public CorrelationWord2Vec(int projectionLayerSize, int windowSize,
            boolean hierarchicalSoftmax, int negativeSamples, double subSample) {
        super(projectionLayerSize, windowSize, hierarchicalSoftmax, negativeSamples,
                subSample);
        // TODO Auto-generated constructor stub
    }
    
    public CorrelationWord2Vec(int projectionLayerSize, int windowSize,
            boolean hierarchicalSoftmax, int negativeSamples, double subSample, String menFile) {
        super(projectionLayerSize, windowSize, hierarchicalSoftmax, negativeSamples,
                subSample, menFile);
        // TODO Auto-generated constructor stub
    }
    
    public void addTrainedCorrelation(String correlationFile) {
        if (vocab == null) {
            throw new ValueException("vocab cannot be null when adding correlation");
            // TODO: do a shit load of thing here
        }
        MenCorrelation men = new MenCorrelation(correlationFile);
        String[][] wordPairs = men.getWordPairs();
        double[] gold = men.getGolds();
        
        HashSet<String> wordSet = new HashSet<String>();
        int pairNum = 0;
        for (String[] pair: wordPairs) {
            if (vocab.getWordIndex(pair[0])!= -1 && vocab.getWordIndex(pair[1])!= -1) {
                wordSet.add(pair[0]);
                wordSet.add(pair[1]);
                pairNum++;
            }
        }
        ArrayList<String> wordList = new ArrayList<String>(wordSet);
        HashMap<String,Integer> word2Index = DataStructureUtils.listToMap(wordList);
        int[][] pairs = new int[pairNum][2];
        int index = 0;
        for (String[] pair: wordPairs) {
            if (vocab.getWordIndex(pair[0])!= -1 && vocab.getWordIndex(pair[1])!= -1) {
                pairs[index][0] = word2Index.get(pair[0]);
                pairs[index][1] = word2Index.get(pair[1]);
                index++;
            }
        }
        
        double[][] corVectors = new double[wordList.size()][];
        for (int i = 0; i < wordList.size(); i++) {
            corVectors[i] = weights0[vocab.getWordIndex(wordList.get(i))];
        }
        
        
        trainedCorrelations.add(new Correlation(gold));
        trainedPairs.add(pairs);
        trainedCorVectors.add(corVectors);
        
    }
    
    public void addTrainedCorrelation(String correlationFile, String name) {
        addTrainedCorrelation(correlationFile);
        trainedCorrelations.get(trainedCorrelations.size() -1).setName(name);
    }
    

    protected void trainModelThread(SentenceInputStream inputStream) {
        long oldWordCount = 0;
        try {
            while (true) {

                // read the whole sentence sentence,
                // the output would be the list of the word's indices in the
                // dictionary
                boolean hasNextSentence = inputStream.readNextSentence(vocab);
                if (!hasNextSentence) break;
                int[] sentence = inputStream.getCurrentSentence();
                // if end of file, finish
                if (sentence.length == 0) {
                    continue;
//                    if (!hasNextSentence)
//                        break;
                }

                // check word count
                // update alpha
                long newSentenceWordCount = inputStream.getWordCount() - oldWordCount;
                oldWordCount = inputStream.getWordCount();
                
                synchronized (this) {
                    wordCount = wordCount + newSentenceWordCount;
                    if (wordCount - lastWordCount >= 10000) {
                        lastWordCount = wordCount;
                        iteration++;
                        // update alpha
                        // what about thread safe???

                        alpha = starting_alpha
                                * (1 - (double) wordCount / (trainWords + 1));
                        if (alpha < starting_alpha * 0.0001) {
                            alpha = starting_alpha * 0.0001;
                        }
                        if (iteration % 10 == 0) {
                            System.out.println("Trained: " + wordCount + " words");
                            System.out.println("Training rate: " + alpha);
                        }
                        if (men != null && outputSpace != null && iteration %100 == 0) {
                            System.out.println("men: " + men.evaluateSpacePearson(outputSpace));
//                            System.out.println("men neg: " + men.evaluateSpacePearson(negSpace));
                            printStatistics();
                        }
                    }
                }

                trainSentence(sentence);
                
                
                int timeToTrainCorrelation = rand.nextInt(1000);
                if (timeToTrainCorrelation == 0) {
                    boolean willTrain = false;
                    synchronized (isSomeThreadTraining) {
                        if (!isSomeThreadTraining) {
                            willTrain = true;
                            isSomeThreadTraining = true;
                        }
                    }
                    if (willTrain) {
                        // TODO: train correlation here
                        trainCorrelation();
                        
                        synchronized (isSomeThreadTraining) {
                            isSomeThreadTraining = false;
                        }
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(1);
        }
    }

    private void trainCorrelation() {
        // TODO Auto-generated method stub
        if (trainedCorrelations.size() == 0) return;
        int corIndex = rand.nextInt(trainedCorrelations.size());
        Correlation correlation = trainedCorrelations.get(corIndex);
        double[][] vectors = trainedCorVectors.get(corIndex);
        int[][] pairs = trainedPairs.get(corIndex);
        int iteration = 100;
        // TODO: tune this
        double learningRate = alpha * 3;
        int vectorNum = vectors.length;
        int vectorSize = vectors[0].length;
        for (int iter = 0; iter < iteration; iter++) {
            double[][] delta = correlation.derivative(vectors, pairs);
            for (int i = 0; i < vectorNum; i++) {
                for (int j = 0; j < vectorSize; j++) {
                    vectors[i][j] += learningRate * delta[i][j];
                }
            }
        }
    }
}
