package composition;

import org.ejml.simple.SimpleMatrix;

import common.SimpleMatrixUtils;

import space.SMSemanticSpace;
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
            System.arraycopy(inputSpace.getVector(wordTriples[i][0]).getMatrix().getData(), 0, uMatrix[i], 0, vectorSize);
            System.arraycopy(inputSpace.getVector(wordTriples[i][1]).getMatrix().getData(), 0, vMatrix[i], 0, vectorSize);
            phrases[i] = wordTriples[i][2];
        }
        return new SMSemanticSpace(phrases, compose(uMatrix, vMatrix));
    }
    
    public SemanticSpace composeSpace(SemanticSpace inputSpace, String[] phrases) {
        int phraseNum = phrases.length;
        int vectorSize = inputSpace.getVectorSize();
        SimpleMatrix composedMatrix = new SimpleMatrix(phraseNum, vectorSize);
        for (int i = 0; i < phraseNum; i++) {
            String[] words = phrases[i].split(" ");
            SimpleMatrix composedVector = inputSpace.getVector(words[0]);
            for (int j = 1; j < words.length; j++) {
                SimpleMatrix newVector = inputSpace.getVector(words[j]);
                composedVector = compose(composedVector, newVector);
            }
            composedMatrix.setRow(i, 0, composedVector.getMatrix().data);
        }
        return new SMSemanticSpace(phrases, composedMatrix);
    }
    
}
