package demo;

import io.sentence.PlainSentenceInputStream;
import io.word.PushBackWordStream;
import io.sentence.SentenceInputStream;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import vocab.Vocab;
//import word2vec.CBowWord2Vec;
import word2vec.SkipNGramWord2Vec;

import demo.TestConstants;

public class MultiModalWordVectorLearning {
    public static void main(String[] args) {
        SkipNGramWord2Vec word2vec = new SkipNGramWord2Vec(200,5, true, 0, (float) 1e-3, TestConstants.CCG_MEN_FILE);
        //MMSkipNGramWord2Vec word2vec = new SkipNGramWord2Vec(200, 5, true, 0, (float) 1e-3);
        
        //TODO: Assume that we extend vocabulary with new items
        //TODO: Assume that for every word we have its extended context
        String trainFile = TestConstants.TRAIN_FILE;
        String outputFile = TestConstants.VECTOR_FILE;
        String vocabFile = TestConstants.VOCABULARY_FILE;
        String initFile = TestConstants.INITIALIZATION_FILE;
        
        System.out.println("Starting training using file " + trainFile);

        boolean learnVocab = !(new File(vocabFile)).exists();
        Vocab vocab = new Vocab(5);
        if (!learnVocab)
            vocab.loadVocab(vocabFile);// ,minFrequency);
        else {
            vocab.learnVocabFromTrainFile(trainFile);
            // save vocabulary
            vocab.saveVocab(vocabFile);
        }

        word2vec.setVocab(vocab);

        word2vec.initNetwork(initFile,initFile);

        // single threaded instead of multithreading
        System.out.println("Start training");
        try {
            SentenceInputStream sentenceInputStream = new PlainSentenceInputStream(
                    new PushBackWordStream(trainFile, 100));
            ArrayList<SentenceInputStream> inputStreams = new ArrayList<SentenceInputStream>();
            inputStreams.add(sentenceInputStream);
            word2vec.trainModel(inputStreams);
            word2vec.saveVector(outputFile, true);
        } catch (IOException e) {
            System.exit(1);
        }

    }
}
