package common.correlation;

import java.util.ArrayList;

import org.ejml.simple.SimpleMatrix;

import space.Neighbor;
import space.SemanticSpace;
import common.IOUtils;

public class WordAnalogyEvaluation {
    String[][] questions;
    int synIndex = 0;
    int questionNum;
    int synNum;
    int semNum;
    public WordAnalogyEvaluation(String dataset) {
        ArrayList<String> lines = IOUtils.readFile(dataset);
        int qCNum = 0;
        for (int i = 0; i < lines.size(); i++) {
            if (lines.get(i).startsWith(":"))
                qCNum++;
        }
        
        questionNum = lines.size() - qCNum;
        questions = new String[questionNum][];
        int qid = 0;
        for (int i = 0; i < lines.size(); i++) {
            if (lines.get(i).startsWith(":")) {
                qid ++;
                if (qid == 6) {
                    synIndex = (i + 1)- 6;
                }
            } else {
                questions[i - qid] = lines.get(i).toLowerCase().split("( |\t)");
            }
        }
        semNum = synIndex;
        synNum = questionNum / semNum;
    }
    public double[] evaluation(SemanticSpace space) {
        int numCorrect = 0;
        int syncorrect = 0;
        int semCorrect = 0;
        
        for (int i = 0; i < questions.length; i++) {
            if (i % 100 == 1) {
                System.out.print(" " + i);
            }
            String[] question = questions[i]; 
            if (correct(space, question)) {
                numCorrect++;
                if (i < synIndex) {
                    semCorrect++;
                } else {
                    syncorrect++;
                }
            }
        }
        System.out.println();
        double[] result = new double[3];
        result[0] = numCorrect / (double) questionNum;
        result[1] = (synNum == 0)?1:syncorrect / (double) synNum;
        result[0] = (semNum == 0)?1:semCorrect / (double) semNum;
        return result;
    }
    
    public boolean correct(SemanticSpace space, String[] question) {
        SimpleMatrix v1 = space.getVector(question[0]);
        SimpleMatrix v2 = space.getVector(question[1]);
        SimpleMatrix v3 = space.getVector(question[2]);
        
        if (v1 == null || v2 == null || v3 == null) {
            System.out.println("at least one word is not in the space");
            return false;
        }
        v1 = v1.scale(1 / v1.normF());
        v2 = v1.scale(1 / v2.normF());
        v3 = v1.scale(1 / v3.normF());
        SimpleMatrix v4 = v2.minus(v1).plus(v3);
        Neighbor[] neighbors = space.getNeighbors(v4, 4, new String[] { question[0], question[1],
                question[2] });
        if (neighbors[0].word.equals(question[3]))
            return true;
        return false;
    }
}
