package demo;

import io.sentence.PlainSentenceInputStream;
import io.word.PushBackWordStream;
import io.sentence.SentenceInputStream;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;


import common.LogUtils;
import common.exception.ValueException;

import space.SemanticSpace;
import vocab.Vocab;
import word2vec.MMSkipgramMaxMargin;

import demo.TestConstants;

public class CrossSituationalLearning {
    public static void main(String[] args) throws ValueException, IOException {
        
        //MmSkipNGramWithMappingMaxMargin word2vec = new MmSkipNGramWithMappingMaxMargin(TestConstants.wordDimensions, 5, true, 0,TestConstants.negative_samples, (float) 1e-3);
        
        MMSkipgramMaxMargin word2vec = new MMSkipgramMaxMargin(TestConstants.wordDimensions, 5, true, 0,TestConstants.negative_samples, 0, TestConstants.MEN_FILE);
        
        
        //TODO: Fix that you have two different vobacularies
        String sourceFile = TestConstants.SOURCE_FILE;
        String targetFile = TestConstants.TARGET_FILE;
        
        String outputFile = TestConstants.VECTOR_FILE;
        String vocabFile = TestConstants.VOCABULARY_FILE;
        
        String initFile = TestConstants.INITIALIZATION_FILE;
        String projInitFile = TestConstants.IMAGE_INITIALIZATION_FILE;
        String logGile = TestConstants.LOG_FILE;
        LogUtils.setup(logGile);
        
        
        System.out.println("Starting training using file " + sourceFile);
        
        System.out.println("Welcome!---->"+TestConstants.VECTOR_FILE);


        boolean learnVocab = !(new File(vocabFile)).exists();
        
        Vocab vocab = new Vocab(1); 
        if (!learnVocab)
            vocab.loadVocab(vocabFile);
        else {
            vocab.learnVocabFromTrainFile(sourceFile);
            // save vocabulary
            vocab.saveVocab(vocabFile);
        }

        word2vec.setVocab(vocab);
        
        word2vec.initNetwork(initFile,projInitFile);
        
        word2vec.initImages(TestConstants.VISION_FILE,true);
        
       
      

        // single threaded instead of multithreading
        System.out.println("Start training");
        try {
            //Source Language
            SentenceInputStream sentenceInputStreamSource = new PlainSentenceInputStream(
                    new PushBackWordStream(sourceFile, 100));
            ArrayList<SentenceInputStream> inputStreamsSource = new ArrayList<SentenceInputStream>();
            inputStreamsSource.add(sentenceInputStreamSource);
            
            //Target  Language
            SentenceInputStream sentenceInputStreamTarget = new PlainSentenceInputStream(
                    new PushBackWordStream(targetFile, 100));
            ArrayList<SentenceInputStream> inputStreamsTarget = new ArrayList<SentenceInputStream>();
            inputStreamsTarget.add(sentenceInputStreamTarget);
            
            word2vec.trainModel(inputStreamsSource,inputStreamsTarget);
            word2vec.saveVector(outputFile, true);
           } catch (IOException e) {
            System.exit(1);
        }
        
       
        
        double [] cors = word2vec.getCors();
        System.out.println("Printing spearman "+cors[0]);
        System.out.println("Printing pearson "+cors[1]);
     
        
       
    }
}