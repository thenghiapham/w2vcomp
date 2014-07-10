package word2vec;

import io.sentence.SentenceInputStream;
import io.word.Phrase;

import java.io.IOException;
import java.util.ArrayList;

/**
 * Still abstract class for learning words' vectors
 * Implement some common methods
 * @author thenghiapham
 *
 */
public abstract class SingleThreadWord2Vec extends AbstractWord2Vec {

    protected long oldWordCount;

    public SingleThreadWord2Vec(int projectionLayerSize, int windowSize,
            boolean hierarchicalSoftmax, int negativeSamples, double subSample) {
        super(projectionLayerSize, windowSize, hierarchicalSoftmax,
                negativeSamples, subSample);
    }

    @Override
    public void trainModel(ArrayList<SentenceInputStream> inputStreams) {
        // single-threaded instead of multi-threaded
        oldWordCount = 0;
        wordCount = 0;
        trainWords = vocab.getTrainWords();
        System.out.println("train words: " + trainWords);
        System.out.println("vocab size: " + vocab.getVocabSize());
        System.out.println("hidden size: " + projectionLayerSize);
        System.out.println("first word:" + vocab.getEntry(0).word);
        System.out.println("last word:"
                + vocab.getEntry(vocab.getVocabSize() - 1).word);
        for (SentenceInputStream inputStream : inputStreams) {
            trainModelThread(inputStream);
        }
        System.out.println("total word count: " + wordCount);
    }

    void trainModelThread(SentenceInputStream inputStream) {
        oldWordCount = wordCount;
        long lastWordCount = wordCount;
        try {
            while (true) {

                // read the whole sentence sentence,
                // the output would be the list of the word's indices in the
                // dictionary
                boolean hasNextSentence = inputStream.readNextSentence(vocab);

                int[] sentence = inputStream.getCurrentSentence();
                Phrase[] phrases = inputStream.getCurrentPhrases();
                // if end of file, finish
                if (sentence.length == 0) {
                    if (!hasNextSentence)
                        break;
                }

                // check word count
                // update alpha
                wordCount = oldWordCount + inputStream.getWordCount();
//                System.out.println(wordCount);
//                System.out.println(inputStream.getWordCount());
                if (wordCount - lastWordCount > 10000) {
                    // if (wordCount - lastWordCount > 50) {
                    System.out.println("Trained: " + wordCount + " words");
                    // update alpha
                    // what about thread safe???
                    alpha = starting_alpha
                            * (1 - (double) wordCount / (trainWords + 1));
                    if (alpha < starting_alpha * 0.0001) {
                        alpha = starting_alpha * 0.0001;
                    }
                    System.out.println("Training rate: " + alpha);
                    lastWordCount = wordCount;
                }
                trainSentence(sentence);
                trainPhrases(phrases, sentence);
            }
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(1);
        }
    }

    public void trainPhrases(Phrase[] phrases, int[] sentence) {
        for (Phrase phrase : phrases) {
            trainSinglePhrase(phrase, sentence);
        }
    }

    public abstract void trainSinglePhrase(Phrase phrase,
            int[] sentence);

    public abstract void trainSentence(int[] sentence);

}
