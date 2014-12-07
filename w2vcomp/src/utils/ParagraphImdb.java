package utils;

import java.io.File;
import java.io.IOException;

import space.RawSemanticSpace;
import word2vec.Paragraph2Vec;
import word2vec.SkipgramPara2Vec;
import common.SingleParsedPhraseVectorPrinter;
import demo.TestConstants;

public class ParagraphImdb {
    public static void main(String[] args) throws IOException {
        String vocabFile = TestConstants.S_VOCABULARY_FILE;
        String modelFile = TestConstants.S_MODEL_FILE;
        
        String datasetFile = TestConstants.S_IMDB_FILE;
        String goldFile = TestConstants.S_IMDB_LABEL_FILE;
        String datasetName = new File(datasetFile).getName();
        String outDir = TestConstants.S_IMDB_SVM_DIR;
        
        SingleParsedPhraseVectorPrinter imdb = new SingleParsedPhraseVectorPrinter(datasetFile, goldFile);
        String[] sentences = imdb.getSurfacePhrases();
        Paragraph2Vec p2v = new SkipgramPara2Vec(modelFile, vocabFile, 100, 5, true, 0, 0, 100);
        p2v.trainParagraphVector(sentences);
        double[][] sentenceVector = p2v.getParagraphVectors();
        RawSemanticSpace space = new RawSemanticSpace(sentences, sentenceVector);
        String outFilePath = outDir + "para" + datasetName;
        
        imdb.printPhraseVectors(outFilePath, space, true);
    }
}
