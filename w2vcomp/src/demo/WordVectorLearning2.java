package demo;

import io.sentence.BasicTreeInputStream;
import io.sentence.ParseSentenceInputStream;
import io.sentence.SentenceInputStream;
import io.word.CombinedWordInputStream;
import io.word.TreeWordInputStream;
import io.word.WordInputStream;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import common.LogUtils;
import vocab.Vocab;
import word2vec.MultiThreadCBow;
import word2vec.MultiThreadSkipGram;

import word2vec.MultiThreadWord2Vec;

/*
 * Learning normal skipgram and Cbow but the input is the parsed corpus
 * Need to preprocess the input into plain text
 */
public class WordVectorLearning2 {
    public static void main(String[] args) throws IOException{
        String configFile = args[0];
        int hiddenSize = Integer.parseInt(args[1]);
        int windowSize = Integer.parseInt(args[2]);
        boolean cbow = Boolean.parseBoolean(args[3]);
        
        W2vProperties properties = new W2vProperties(configFile);
        boolean hierarchicalSoftmax = Boolean.parseBoolean(properties.getProperty("HierarchialSoftmax"));
        int negativeSamples = Integer.parseInt(properties.getProperty("NegativeSampling"));
        double subSampling = Double.parseDouble(properties.getProperty("SubSampling"));
        
        MultiThreadWord2Vec word2vec;
        if (cbow)
            word2vec = new MultiThreadCBow(hiddenSize, windowSize, hierarchicalSoftmax, negativeSamples, subSampling, properties.getProperty("MenFile"));
        else
            word2vec = new MultiThreadSkipGram(hiddenSize, windowSize, hierarchicalSoftmax, negativeSamples, subSampling, properties.getProperty("MenFile"));
        String suffix = "";
        String trainDirPath = properties.getProperty("STrainDir");
        String outputFile = properties.getProperty("WordVectorFile").replace(".bin", suffix + ".bin").replaceAll("size", "" + hiddenSize).replace("ws", "" + windowSize);
        String vocabFile = properties.getProperty("VocabFile");
        String modelFile = properties.getProperty("WordModelFile").replace(".mdl", suffix + ".mdl").replaceAll("size", "" + hiddenSize).replace("ws", "" + windowSize);
        System.out.println("Starting training using files in " + trainDirPath);

        boolean learnVocab = !(new File(vocabFile)).exists();
        File trainDir = new File(trainDirPath);
        File[] trainFiles = trainDir.listFiles();
        String logFile = properties.getProperty("WordLogFile").replaceAll("size", "" + hiddenSize).replace("ws", "" + windowSize);
        if (cbow) {
            outputFile = outputFile.replaceAll("skip", "cbow");
            modelFile = modelFile.replaceAll("skip", "cbow");
            logFile = logFile.replaceAll("skip", "cbow");
        }
        LogUtils.setup(logFile);
        
        Vocab vocab = new Vocab(new Integer(properties.getProperty("MinFrequency")));
        
        if (!learnVocab)
            vocab.loadVocab(vocabFile);// ,minFrequency);
        else {
            ArrayList<WordInputStream> wordStreamList = new ArrayList<>();
            for (File trainFile: trainFiles) {
                TreeWordInputStream wordStream = new TreeWordInputStream(new BasicTreeInputStream(trainFile));
                wordStreamList.add(wordStream);
            }
          
            CombinedWordInputStream wordStream = new CombinedWordInputStream(wordStreamList);
            vocab.learnVocabFromTrainStream(wordStream);
            vocab.saveVocab(vocabFile);
        }

        word2vec.setVocab(vocab);

        word2vec.initNetwork();

        // single threaded instead of multithreading
        System.out.println("Start training");
        try {
            ArrayList<SentenceInputStream> inputStreams = new ArrayList<SentenceInputStream>();
            for (File trainFile: trainFiles) {
                SentenceInputStream sentenceInputStream = new ParseSentenceInputStream(new BasicTreeInputStream(trainFile));
                inputStreams.add(sentenceInputStream);
            }
            word2vec.trainModel(inputStreams);
            word2vec.saveVector(outputFile, true);
            word2vec.saveNegVectors(outputFile.replace("bin", "neg.bin"), true);
            word2vec.saveNetwork(modelFile, true);
        } catch (IOException e) {
            System.exit(1);
        }


    }
}
