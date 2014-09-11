package demo;

import io.sentence.BasicTreeInputStream;
import io.sentence.TreeInputStream;
import io.word.TreeWordInputStream;
import io.word.WordInputStream;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

import common.IOUtils;
import common.LogUtils;

import vocab.Vocab;
import word2vec.Sentence2Vec;
import word2vec.SingleThreadedSentence2Vec;

import demo.TestConstants;

public class MassSentenceVectorLearning {
    public static void main(String[] args) throws IOException{
        String levelString = args[0];
        String allLevelString = args[1];
        String lexicalString = args[2];
        String constructionString = args[3];
        String outSuffix = args[0] + args[1].charAt(0) + args[2].charAt(0)  
                + args[3].charAt(0);
        
        LogUtils.setup(TestConstants.S_LOG_FILE + outSuffix);
        
        int hiddenLayerSize = 40;
        int windowSize = 5;
        boolean hierarchialSoftmax = true;
        int negativeSampling = 0;
        double subSampling = 0;
        
        int phraseLevel = new Integer(levelString);
        boolean allLevel = new Boolean(allLevelString);
        boolean lexical = new Boolean(lexicalString);
        boolean useConstruction = new Boolean(constructionString);
        
        String constructionFile = TestConstants.S_CONSTRUCTION_FILE;
        HashMap<String, String> constructionGroups = new HashMap<String, String>();
        
        if (useConstruction) {
            constructionGroups = IOUtils.readConstructionGroup(constructionFile);
        }
        // IOUtils.printConstructions(constructionGroups);
        Sentence2Vec sentence2vec = new SingleThreadedSentence2Vec(hiddenLayerSize, windowSize, 
                //hierarchialSoftmax, negativeSampling, subSampling, constructionGroups, phraseLevel, 
                hierarchialSoftmax, negativeSampling, subSampling, constructionGroups, phraseLevel,
                allLevel, lexical, TestConstants.S_MEN_FILE);
        String trainFile = TestConstants.S_TRAIN_FILE;
        String outputFile = TestConstants.S_VECTOR_FILE + outSuffix;
        String vocabFile = TestConstants.S_VOCABULARY_FILE + outSuffix;
        System.out.println("Starting training using file " + trainFile);
        boolean learnVocab = !(new File(vocabFile)).exists();
        Vocab vocab = new Vocab(5);
        if (!learnVocab)
            vocab.loadVocab(vocabFile);// ,minFrequency);
        else {
            WordInputStream wordInputStream = new TreeWordInputStream(new BasicTreeInputStream(trainFile));
            vocab.learnVocabFromTrainStream(wordInputStream);
            wordInputStream.close();
            // save vocabulary
            vocab.saveVocab(vocabFile);
        }

        sentence2vec.setVocab(vocab);

        sentence2vec.initNetwork();

        // single threaded instead of multithreading
        System.out.println("Start training");
        try {
            TreeInputStream treeInputStream = new BasicTreeInputStream(trainFile);;
            ArrayList<TreeInputStream> inputStreams = new ArrayList<TreeInputStream>();
            inputStreams.add(treeInputStream);
            sentence2vec.trainModel(inputStreams);
            sentence2vec.saveVector(outputFile, true);
        } catch (IOException e) {
            System.exit(1);
        }

    }
}
