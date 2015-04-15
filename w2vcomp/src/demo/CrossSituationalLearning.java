package demo;

import io.sentence.PlainSentenceInputStream;
import io.word.PushBackWordStream;
import io.sentence.SentenceInputStream;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;


import common.LogUtils;
import common.exception.ValueException;

import vocab.Vocab;
import word2vec.MMSkipNgramWord2Vec;
import word2vec.MMSkipgramMaxMargin;
import word2vec.MmSkipNGramWithMappingMaxMargin;
//import word2vec.CBowWord2Vec;
import word2vec.SkipNGramWord2Vec;
import word2vec.extra.MmSkipNGramWithMappingCosine;

import demo.TestConstants;

public class CrossSituationalLearning {
    public static void main(String[] args) throws ValueException, IOException {
        
        //MmSkipNGramWithMappingMaxMargin word2vec = new MmSkipNGramWithMappingMaxMargin(TestConstants.wordDimensions, 5, true, 0,TestConstants.negative_samples, (float) 1e-3);
        
        MMSkipgramMaxMargin word2vec = new MMSkipgramMaxMargin(TestConstants.wordDimensions, 5, true, 0,TestConstants.negative_samples, (float) 1e-3);
        
        
        //TODO: Assume that we extend vocabulary with new items
        //TODO: Assume that for every word we have its extended context
        String trainFile = TestConstants.TRAIN_FILE;
        String outputFile = TestConstants.VECTOR_FILE;
        String vocabFile = TestConstants.VOCABULARY_FILE;
        String initFile = TestConstants.INITIALIZATION_FILE;
        String projInitFile = TestConstants.IMAGE_INITIALIZATION_FILE;
        String logGile = TestConstants.LOG_FILE;
        LogUtils.setup(logGile);
        
        System.out.println("Starting training using file " + trainFile);
        
        System.out.println("Welcome!---->"+TestConstants.VECTOR_FILE);


        boolean learnVocab = !(new File(vocabFile)).exists();
        
        Vocab vocab = new Vocab(5); 
        if (!learnVocab)
            vocab.loadVocab(vocabFile);
        else {
            vocab.learnVocabFromTrainFile(trainFile);
            // save vocabulary
            vocab.saveVocab(vocabFile);
        }

        word2vec.setVocab(vocab);
        
        word2vec.initNetwork(initFile,projInitFile);
        
        word2vec.initImages(TestConstants.VISION_FILE,false);
        
       
      

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
        
        double [] cors = word2vec.getCors();
        System.out.println("Printing pearson "+cors[0]);
        System.out.println("Printing spearman "+cors[1]);
     
        
       
    }
}