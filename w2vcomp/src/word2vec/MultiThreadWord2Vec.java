package word2vec;

import io.sentence.SentenceInputStream;
import io.sentence.SubSamplingSentenceInputStream;

import java.io.IOException;
import java.util.ArrayList;

import space.RawSemanticSpace;

import common.correlation.MenCorrelation;

/**
 * Still abstract class for learning words' vectors
 * Implement some common methods
 * @author thenghiapham
 *
 */
public abstract class MultiThreadWord2Vec extends AbstractWord2Vec {
    
    protected MenCorrelation men;
    protected RawSemanticSpace outputSpace;
    protected RawSemanticSpace negSpace;
    protected long lastWordCount = 0;
    protected int iteration = 0;
    protected int epochNum = 1;
    

    public MultiThreadWord2Vec(int projectionLayerSize, int windowSize,
            boolean hierarchicalSoftmax, int negativeSamples, double subSample) {
        super(projectionLayerSize, windowSize, hierarchicalSoftmax,
                negativeSamples, subSample);
    }
    
    public MultiThreadWord2Vec(int projectionLayerSize, int windowSize,
            boolean hierarchicalSoftmax, int negativeSamples, double subSample, String menFile) {
        this(projectionLayerSize, windowSize, hierarchicalSoftmax,
                negativeSamples, subSample);
        men = new MenCorrelation(menFile);
    }

    @Override
    public void trainModel(ArrayList<SentenceInputStream> inputStreams) {
        // single-threaded instead of multi-threaded
        wordCount = 0;
        lastWordCount = 0;
        trainWords = vocab.getTrainWords() * epochNum;
        System.out.println("train words: " + trainWords);
        System.out.println("vocab size: " + vocab.getVocabSize());
        System.out.println("hidden size: " + projectionLayerSize);
        System.out.println("first word:" + vocab.getEntry(0).word);
        System.out.println("last word:"
                + vocab.getEntry(vocab.getVocabSize() - 1).word);
        
        if (men != null) {
            outputSpace = new RawSemanticSpace(vocab, weights0, false);
            if (negativeSamples > 0) {
                negSpace = new RawSemanticSpace(vocab, negativeWeights1, false);
            }
        }
        
        
        TrainingThread[] threads = new TrainingThread[inputStreams.size()];
        for (int epoch = 0; epoch < epochNum; epoch++) {
            System.out.println("epoch: " + epoch);
            for (int i = 0; i < inputStreams.size(); i++) {
                SentenceInputStream inputStream = inputStreams.get(i);
                if (subSample > 0) {
                    inputStream = new SubSamplingSentenceInputStream(inputStream, subSample);
                }
                threads[i] = new TrainingThread(inputStream);
                threads[i].start();
            }
            try {
                for (TrainingThread thread: threads) {
                        thread.join();
                }
            } catch (InterruptedException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
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
