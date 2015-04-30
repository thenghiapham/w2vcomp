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
import word2vec.MMSkipgramMaxMarginWeighted;

import demo.TestConstants;

public class CrossSituationalLearning {
    public static void main(String[] args) throws ValueException, IOException {
        
        //MmSkipNGramWithMappingMaxMargin word2vec = new MmSkipNGramWithMappingMaxMargin(TestConstants.wordDimensions, 5, true, 0,TestConstants.negative_samples, (float) 1e-3);
        
        //MMSkipgramMaxMarginWeighted word2vec = new MMSkipgramMaxMarginWeighted(TestConstants.wordDimensions, 5, true, 0,TestConstants.negative_samples, 0, TestConstants.MEN_FILE);
        MMSkipgramMaxMargin word2vec = new MMSkipgramMaxMargin(TestConstants.wordDimensions, 5, true, 0,TestConstants.negative_samples, 0, TestConstants.MEN_FILE);
        
        
        String sourceFileTrain = TestConstants.SOURCE_FILE_TRAIN;
        String targetFileTrain = TestConstants.TARGET_FILE_TRAIN;
        
        //String sourceFileTest = TestConstants.SOURCE_FILE_TEST;
        //String targetFileTest = TestConstants.TARGET_FILE_TEST;
        
        
        String outputFile = TestConstants.VECTOR_FILE;
        String vocabFile_lang1 = TestConstants.VOCABULARY_FILE_lang1;
        String vocabFile_lang2 = TestConstants.VOCABULARY_FILE_lang2;
        
        String initFile = TestConstants.INITIALIZATION_FILE;
        String projInitFile = TestConstants.IMAGE_INITIALIZATION_FILE;
        String logGile = TestConstants.LOG_FILE;
        LogUtils.setup(logGile);
        
        
        System.out.println("Starting training using source file " + sourceFileTrain);
        
        System.out.println("Welcome!---->"+TestConstants.VECTOR_FILE);


        boolean learnVocab = true;
        Vocab vocab_lang1 = new Vocab(1); 
        if (!learnVocab)
            vocab_lang1.loadVocab(vocabFile_lang1);
        else {
            vocab_lang1.learnVocabFromTrainFile(sourceFileTrain);
            // save vocabulary
            vocab_lang1.saveVocab(vocabFile_lang1);
        }
        
        
        System.out.println("Starting training using target file " + targetFileTrain);
 
        learnVocab = !(new File(vocabFile_lang2)).exists();

        Vocab vocab_lang2 = new Vocab(1); 
        
        if (!learnVocab)
            vocab_lang2.loadVocab(vocabFile_lang2);
        else {
            vocab_lang2.learnVocabFromTrainFile(targetFileTrain);
            // save vocabulary
            vocab_lang2.saveVocab(vocabFile_lang2);
        }   

        word2vec.setVocabs(vocab_lang1,vocab_lang2);
       
        word2vec.initNetwork(initFile,projInitFile);
        
        word2vec.initImages(TestConstants.VISION_FILE,true);
        
       
        
        // Run training
        System.out.println("Start training");
        try {
            //Source Language
            SentenceInputStream sentenceInputStreamSource = new PlainSentenceInputStream(
                    new PushBackWordStream(sourceFileTrain, 100));
            ArrayList<SentenceInputStream> inputStreamsSource = new ArrayList<SentenceInputStream>();
            inputStreamsSource.add(sentenceInputStreamSource);
            
            //Target  Language
            SentenceInputStream sentenceInputStreamTarget = new PlainSentenceInputStream(
                    new PushBackWordStream(targetFileTrain, 100));
            ArrayList<SentenceInputStream> inputStreamsTarget = new ArrayList<SentenceInputStream>();
            inputStreamsTarget.add(sentenceInputStreamTarget);
            
            word2vec.trainModel(inputStreamsSource,inputStreamsTarget);
            word2vec.saveVector(outputFile, true);
           } catch (IOException e) {
            System.exit(1);
        }
        
        Vocab localVocab = new Vocab(1); 
      
        /*
        localVocab.learnVocabFromTrainFile(sourceFileTest);
        TestConstants.rate_multiplier_grad = 20;
        
        //Run inference
        try {
            //Source Language
            SentenceInputStream sentenceInputStreamSource = new PlainSentenceInputStream(
                    new PushBackWordStream(sourceFileTest, 100));
            ArrayList<SentenceInputStream> inputStreamsSource = new ArrayList<SentenceInputStream>();
            inputStreamsSource.add(sentenceInputStreamSource);
            
            //Target  Language
            SentenceInputStream sentenceInputStreamTarget = new PlainSentenceInputStream(
                    new PushBackWordStream(targetFileTest, 100));
            ArrayList<SentenceInputStream> inputStreamsTarget = new ArrayList<SentenceInputStream>();
            inputStreamsTarget.add(sentenceInputStreamTarget);
            
            word2vec.trainModel(inputStreamsSource,inputStreamsTarget,localVocab);
            word2vec.saveVector(outputFile, true);
           } catch (IOException e) {
            System.exit(1);
        }
        */
        double [] cors = word2vec.getCors();
        System.out.println("Printing spearman "+cors[0]);
        System.out.println("Printing pearson "+cors[1]);
     
        
       
    }
}