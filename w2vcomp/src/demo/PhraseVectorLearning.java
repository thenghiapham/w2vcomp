package demo;

import io.sentence.CcgInputStream;
import io.sentence.PlainSentenceInputStream;
import io.word.PushBackWordStream;
import io.word.WordInputStream;
import io.sentence.SentenceInputStream;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

import vocab.Vocab;
import word2vec.SkipGramPhrase2Vec;
//import word2vec.CBowWord2Vec;
import word2vec.SkipNGramWord2Vec;

import demo.TestConstants;

public class PhraseVectorLearning {
    public static void main(String[] args) throws IOException{
//        SkipGramPhrase2Vec word2vec = new SkipGramPhrase2Vec(150, 5, false, 10, (float) 1e-3);
        SkipNGramWord2Vec word2vec = new SkipNGramWord2Vec(200, 5, false, 10, (float) 1e-3);
        String trainFile = TestConstants.CCG_TRAIN_FILE;
        String outputFile = TestConstants.CCG_VECTOR_FILE;
        String vocabFile = TestConstants.CCG_VOCABULARY_FILE;
        String initFile = TestConstants.CCG_INITIALIZATION_FILE;
        System.out.println("Starting training using file " + trainFile);

        boolean learnVocab = !(new File(vocabFile)).exists();
        Vocab vocab = new Vocab(5);
        if (!learnVocab)
            vocab.loadVocab(vocabFile);// ,minFrequency);
        else {
            WordInputStream wordInputStream = new CcgInputStream(new BufferedReader(new FileReader(trainFile)));
            vocab.learnVocabFromTrainStream(wordInputStream);
            wordInputStream.close();
            // save vocabulary
            vocab.saveVocab(vocabFile);
        }

        word2vec.setVocab(vocab);

        word2vec.initNetwork(initFile);

        // single threaded instead of multithreading
        System.out.println("Start training");
        try {
            SentenceInputStream sentenceInputStream = new CcgInputStream(new BufferedReader(new FileReader(trainFile)));;
            ArrayList<SentenceInputStream> inputStreams = new ArrayList<SentenceInputStream>();
            inputStreams.add(sentenceInputStream);
            word2vec.trainModel(inputStreams);
            word2vec.saveVector(outputFile, true);
        } catch (IOException e) {
            System.exit(1);
        }

    }
}
