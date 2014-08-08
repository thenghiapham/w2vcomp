package composition;

import org.ejml.simple.SimpleMatrix;

import common.SimpleMatrixUtils;

import space.NewSemanticSpace;
import space.SemanticSpace;

public abstract class BasicComposition {
    public abstract SimpleMatrix compose(SimpleMatrix uMatrix, SimpleMatrix vMatrix);
    
    public double[] compose(double[] u, double[] v) {
        // TODO: implement later
        return null;
    }
    
    public double[][] compose(double[][] uMatrix, double[][] vMatrix) {
        SimpleMatrix u = new SimpleMatrix(uMatrix);
        SimpleMatrix v = new SimpleMatrix(vMatrix);
        SimpleMatrix composedMatrix = compose(u, v);
        return SimpleMatrixUtils.to2DArray(composedMatrix);
    }
    
    public SemanticSpace composeSpace(SemanticSpace inputSpace, String[][] wordTriples) {
        int phraseNum = wordTriples.length;
        int vectorSize = inputSpace.getVectorSize();
        String[] phrases = new String[phraseNum];
        double[][] uMatrix = new double[phraseNum][vectorSize];
        double[][] vMatrix = new double[phraseNum][vectorSize];
        for (int i = 0; i < phraseNum; i++) {
            System.arraycopy(inputSpace.getVector(wordTriples[i][0]), 0, uMatrix[i], 0, vectorSize);
            System.arraycopy(inputSpace.getVector(wordTriples[i][1]), 0, vMatrix[i], 0, vectorSize);
            phrases[i] = wordTriples[i][2];
        }
        return new SemanticSpace(phrases, compose(uMatrix, vMatrix));
    }
    
    public SemanticSpace composeSpace(NewSemanticSpace inputSpace, String[][] wordTriples) {
        int phraseNum = wordTriples.length;
        int vectorSize = inputSpace.getVectorSize();
        String[] phrases = new String[phraseNum];
        double[][] uMatrix = new double[phraseNum][vectorSize];
        double[][] vMatrix = new double[phraseNum][vectorSize];
        for (int i = 0; i < phraseNum; i++) {
            System.arraycopy(inputSpace.getVector(wordTriples[i][0]).getMatrix().getData(), 0, uMatrix[i], 0, vectorSize);
            System.arraycopy(inputSpace.getVector(wordTriples[i][1]).getMatrix().getData(), 0, vMatrix[i], 0, vectorSize);
            phrases[i] = wordTriples[i][2];
        }
        return new SemanticSpace(phrases, compose(uMatrix, vMatrix));
    }
    
}
