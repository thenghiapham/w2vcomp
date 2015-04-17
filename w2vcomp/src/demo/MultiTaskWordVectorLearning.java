package demo;

import io.sentence.PlainSentenceInputStream;
import io.word.CombinedWordInputStream;
import io.word.PushBackWordStream;
import io.word.WordInputStream;
import io.sentence.SentenceInputStream;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import java.util.HashSet;

import common.IOUtils;
import common.exception.ValueException;
import common.wordnet.WordNetAdj;
import common.wordnet.WordNetNoun;
import vocab.Vocab;
import word2vec.MultiThreadAlterSkipGram;
import word2vec.MultiThreadSkipGram;
import word2vec.MultiThreadWord2Vec;
import word2vec.multitask.AntonymWord2Vec;

public class MultiTaskWordVectorLearning {
    public static void main(String[] args) throws IOException{
        
        boolean softmax = false;
        int negativeSamples = 10;
        double subSampling = 1e-3;
        MultiThreadWord2Vec word2vec = null;
        String configFile = args[0];
        int size = Integer.parseInt(args[1]);
        int type = Integer.parseInt(args[2]);
        boolean adj = Boolean.parseBoolean(args[3]);
        boolean noun = Boolean.parseBoolean(args[4]);
        boolean verb = Boolean.parseBoolean(args[5]);
        
        String forbiddenWordFile = null;
        if (args.length == 7) {
            forbiddenWordFile = args[6];
        }
        W2vProperties properties = new W2vProperties(configFile);
        String trainDirPath = properties.getProperty("TrainDir");
        String outputFile = properties.getProperty("WordVectorFile");
        String vocabFile = properties.getProperty("VocabFile");
        String menFile = properties.getProperty("MenFile");
        outputFile = outputFile.replaceAll(".bin", "_" + size + ".bin");
        switch (type) {
        case 0:
            word2vec = new MultiThreadSkipGram(size, 5, softmax, negativeSamples, subSampling, menFile);
            break;
        case 1:
            word2vec = new MultiThreadAlterSkipGram(size, 5, softmax, negativeSamples, subSampling, menFile);
            outputFile = outputFile.replaceAll(".bin", "_old.bin");
            break;
        case 2: 
            word2vec = new AntonymWord2Vec(size, 5, softmax, negativeSamples, 5, subSampling, menFile);
            if (!(noun || adj || verb)) {
                throw new ValueException("should train with at least one wordnet");
            } else {
                
                AntonymWord2Vec antoWord2Vec = (AntonymWord2Vec) word2vec;
                outputFile = outputFile.replaceAll(".bin", "_anto.bin");
                HashSet<String> forbiddenSet = new HashSet<String>();
                if (forbiddenWordFile != null) {
                    ArrayList<String> forbiddenWords = IOUtils.readFile(forbiddenWordFile);
                    forbiddenSet = new HashSet<String>(forbiddenWords);
                    outputFile = outputFile.replaceAll(".bin", "_train.bin");
                }
                antoWord2Vec.setForbiddenWords(forbiddenSet);
                
                if (noun) {
                    WordNetNoun wordNetNoun = new WordNetNoun(properties.getProperty("WordNetNoun"));
                    antoWord2Vec.setWordNetNoun(wordNetNoun);
                    outputFile = outputFile.replaceAll(".bin", "_noun.bin");
                }
                if (adj) {
                    WordNetAdj wordNetAdj = new WordNetAdj(properties.getProperty("WordNetAdj"));
                    antoWord2Vec.setWordNetAdj(wordNetAdj);
                    outputFile = outputFile.replaceAll(".bin", "_adj.bin");
                }
                if (verb) {
                    
                }
                
            }
            
            break;
        }
        
        File trainDir = new File(trainDirPath);
        File[] trainFiles = trainDir.listFiles();
        System.out.println("Starting training using dir " + trainDirPath);

        boolean learnVocab = !(new File(vocabFile)).exists();
        Vocab vocab = new Vocab(Integer.parseInt(properties.getProperty("MinFrequency")));
        if (!learnVocab)
            vocab.loadVocab(vocabFile);// ,minFrequency);
        else {
            ArrayList<WordInputStream> wordStreamList = new ArrayList<>();
            for (File trainFile: trainFiles) {
                WordInputStream wordStream = new PushBackWordStream(trainFile.getAbsolutePath(), 200);
                wordStreamList.add(wordStream);
            }
          
            CombinedWordInputStream wordStream = new CombinedWordInputStream(wordStreamList);
            vocab.learnVocabFromTrainStream(wordStream);
            // save vocabulary
            vocab.saveVocab(vocabFile);
        }

        word2vec.setVocab(vocab);
        word2vec.initNetwork();

        System.out.println("Start training");
        try {
            ArrayList<SentenceInputStream> inputStreams = new ArrayList<SentenceInputStream>();
            for (File trainFile: trainFiles) {
                SentenceInputStream sentenceInputStream = new PlainSentenceInputStream(
                    new PushBackWordStream(trainFile.getAbsolutePath(), 200));
                inputStreams.add(sentenceInputStream);
            }
            word2vec.trainModel(inputStreams);
            word2vec.saveVector(outputFile, true);
        } catch (IOException e) {
            System.exit(1);
        }

    }
}
