package demo;

import io.sentence.CcgInputStream;
import io.word.WordInputStream;
import io.sentence.SentenceInputStream;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

import vocab.Vocab;
import word2vec.AbstractWord2Vec;
import word2vec.NeuralLanguageModel;
import word2vec.SkipGramPhrase2Vec;
//import word2vec.SkipNGramWord2Vec;
//import word2vec.CBowWord2Vec;

import demo.TestConstants;

public class PhraseVectorLearning {
    public static void main(String[] args) throws IOException{
        int hiddenLayerSize = 100;
        int windowSize = 5;
        boolean hierarchialSoftmax = true;
        int negativeSampling = 0;
        double subSampling = 0;
        // TODO: checking subSampling with phrase
//        AbstractWord2Vec word2vec = new NeuralLanguageModel(hiddenLayerSize, windowSize, hierarchialSoftmax, negativeSampling, subSampling, TestConstants.CCG_MEN_FILE, TestConstants.CCG_AN_FILE);
        AbstractWord2Vec word2vec = new SkipGramPhrase2Vec(hiddenLayerSize, windowSize, hierarchialSoftmax, negativeSampling, subSampling, TestConstants.S_MEN_FILE, TestConstants.S_AN_FILE);
//        AbstractWord2Vec word2vec = new CBowWord2Vec(hiddenLayerSize, windowSize, hierarchialSoftmax, negativeSampling, subSampling, TestConstants.CCG_MEN_FILE);
//        AbstractWord2Vec word2vec = new SkipNGramWord2Vec(hiddenLayerSize, windowSize, hierarchialSoftmax, negativeSampling, subSampling, TestConstants.CCG_MEN_FILE);
        String trainFile = TestConstants.TRAIN_FILE;
        String outputFile = TestConstants.VECTOR_FILE;
        String vocabFile = TestConstants.VOCABULARY_FILE;
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

//        word2vec.initNetwork(initFile);
        word2vec.initNetwork();

        // single threaded instead of multithreading
        System.out.println("Start training");
        try {
            SentenceInputStream sentenceInputStream = new CcgInputStream(new BufferedReader(new FileReader(trainFile)));;
            ArrayList<SentenceInputStream> inputStreams = new ArrayList<SentenceInputStream>();
            inputStreams.add(sentenceInputStream);
            word2vec.trainModel(inputStreams);
            word2vec.saveVector(outputFile, true);
            if (word2vec instanceof SkipGramPhrase2Vec || word2vec instanceof NeuralLanguageModel) {
                if (word2vec instanceof SkipGramPhrase2Vec)
                    ((SkipGramPhrase2Vec) word2vec).saveMatrix(TestConstants.MATRIX_FILE, false);
                else
                    ((NeuralLanguageModel) word2vec).saveMatrix(TestConstants.MATRIX_FILE, false);
            }
//            word2vec.saveNetwork(initFile, true);
        } catch (IOException e) {
            System.exit(1);
        }

    }
}
