package word2vec;

import io.sentence.SentenceInputStream;
import io.sentence.SubSamplingSentenceInputStream;

import java.io.IOException;
import java.util.ArrayList;

import space.SemanticSpace;

import common.MenCorrelation;

/**
 * Still abstract class for learning words' vectors
 * Implement some common methods
 * @author thenghiapham
 *
 */
public abstract class MultiThreadWord2Vec extends AbstractWord2Vec {

    protected MenCorrelation men;
    protected SemanticSpace outputSpace;
    long lastWordCount = 0;
    int iteration = 0;
    

    public MultiThreadWord2Vec(int projectionLayerSize, int windowSize,
            boolean hierarchicalSoftmax, int negativeSamples, 
            int negativeSamplesImages, double subSample) {
        super(projectionLayerSize, windowSize, hierarchicalSoftmax,
                negativeSamples, negativeSamplesImages, subSample);
    }
    
    public MultiThreadWord2Vec(int projectionLayerSize, int windowSize,
            boolean hierarchicalSoftmax, int negativeSamples, 
            int negativeSamplesImages, double subSample, 
            String menFile) {
        this(projectionLayerSize, windowSize, hierarchicalSoftmax,
                negativeSamples, negativeSamplesImages, subSample);
        men = new MenCorrelation(menFile);
    }

    @Override
    public void trainModel(ArrayList<SentenceInputStream> inputStreams) {
        // single-threaded instead of multi-threaded
        wordCount = 0;
        lastWordCount = 0;
        trainWords = vocab.getTrainWords();
        System.out.println("train words: " + trainWords);
        System.out.println("vocab size: " + vocab.getVocabSize());
        System.out.println("hidden size: " + projectionLayerSize);
        System.out.println("first word:" + vocab.getEntry(0).word);
        System.out.println("last word:"
                + vocab.getEntry(vocab.getVocabSize() - 1).word);
        
        if (men != null) {
            outputSpace = new SemanticSpace(vocab, weights0, false);
        }
        
        TrainingThread[] trainingThreads = new TrainingThread[inputStreams.size()];
        for (int i = 0; i < inputStreams.size(); i++) {
            SentenceInputStream inputStream = inputStreams.get(i);
            if (subSample > 0) {
                inputStream = new SubSamplingSentenceInputStream(inputStream, subSample);
            }
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
        
        if (men != null) {
            System.out.println("men: " + men.evaluateSpacePearson(outputSpace));
        }
        
        System.out.println("total word count: " + wordCount);
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
                    if (wordCount - lastWordCount > 10000) {
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
            }
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(1);
        }
    }

    
    public void printStatistics() {
    }

    
    public abstract void trainSentence(int[] sentence);
    
    protected class TrainingThread extends Thread {
        SentenceInputStream inputStream;
        
        public TrainingThread(SentenceInputStream inputStream) {
            this.inputStream = inputStream;
        }
        
        public void run() {
            trainModelThread(inputStream);
        }
    }
    
}