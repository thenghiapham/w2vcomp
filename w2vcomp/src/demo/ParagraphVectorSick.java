package demo;

import space.RawSemanticSpace;
import word2vec.CBowPara2Vec;
import word2vec.Paragraph2Vec;
import word2vec.SkipgramPara2Vec;
import common.correlation.ParsedPhraseCorrelation;

public class ParagraphVectorSick {
    public static void main(String[] args) {
        String vocabFile = TestConstants.S_VOCABULARY_FILE;
        String modelFile = TestConstants.S_MODEL_FILE;
        String sickFile  = TestConstants.S_SICK_FILE;
        ParsedPhraseCorrelation sick = new ParsedPhraseCorrelation(sickFile);
        String[] sentences = sick.getSurfacePhrase();
//        Paragraph2Vec p2v = new CBowPara2Vec(modelFile, vocabFile, 100, 5, true, 0, 0, 20);
        Paragraph2Vec p2v = new SkipgramPara2Vec(modelFile, vocabFile, 100, 5, true, 0, 0, 100);
        p2v.trainParagraphVector(sentences);
        double[][] sentenceVector = p2v.getParagraphVectors();
        RawSemanticSpace space = new RawSemanticSpace(sentences, sentenceVector);
        System.out.println(sick.evaluatePhraseSpacePearson(space));
        
    }
}
