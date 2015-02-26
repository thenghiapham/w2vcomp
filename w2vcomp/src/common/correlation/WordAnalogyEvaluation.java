package common.correlation;

import java.util.ArrayList;

import org.ejml.simple.SimpleMatrix;

import space.Neighbor;
import space.SemanticSpace;
import common.IOUtils;

public class WordAnalogyEvaluation {
    ArrayList<ArrayList<String[]>> questionLists;
    ArrayList<String> labels;
    int questionNum = 0;
    int synNum = 0;
    int semNum = 0;
    public WordAnalogyEvaluation(String dataset) {
        ArrayList<String[]> currentQuestions = null; 
        
        questionLists = new ArrayList<ArrayList<String[]>>();
        ArrayList<String> lines = IOUtils.readFile(dataset);
        labels = new ArrayList<String>();
        
        for (int i = 0; i < lines.size(); i++) {
            if (lines.get(i).startsWith(":")) {
                currentQuestions = new ArrayList<String[]>();
                questionLists.add(currentQuestions);
                labels.add(lines.get(i));
                
            } else {
                questionNum++;
                if (i < 5) {
                    semNum++;
                }
//                System.out.println(lines.get(i));
                currentQuestions.add(lines.get(i).toLowerCase().split("( |\t)"));
            }
        }
        System.out.println("Question num: " + questionNum);
//        System.out.println("Question num: " + questionLists.get(0).size());
        synNum = questionNum - semNum;
    }
    public double[] evaluation(SemanticSpace space) {
        int seenSem = 0;
        int seenSyn = 0;
        int seenAll = 0;
        int numCorrect = 0;
        int syncorrect = 0;
        int semCorrect = 0;
        
        for (int i = 0; i < questionLists.size(); i++) {
            System.out.println(labels.get(i));
            ArrayList<String[]> section = questionLists.get(i);
            int sectionSeen = 0;
            int sectionCorrect = 0;
            for (int j = 0; j < section.size(); j++) {
                String[] question = section.get(j);
                
                boolean seen = true;
                for (String word: question) {
//                    System.out.print(word);
                    if (space.getVector(word) == null) {
                        seen = false;
                        break;
                    }
//                    System.out.println(" seen");
                }
                if (!seen) continue;
                sectionSeen++;
                if (correct(space, question)) {
                    sectionCorrect++;
                }
            }
            System.out.println("acc: " + (sectionCorrect / (double) sectionSeen));
            if (i < 5) {
                syncorrect += sectionCorrect;
                seenSyn += sectionSeen;
            } else {
                semCorrect += sectionCorrect;
                seenSem += sectionSeen;
            }
            numCorrect += sectionCorrect;
            seenAll += sectionSeen;
        }
        System.out.println();
        System.out.println("seen: " + seenAll);
        System.out.println("correct: " + numCorrect);
        double[] result = new double[3];
        result[0] = numCorrect / (double) seenAll;
        result[1] = (syncorrect == 0)?1:syncorrect / (double) seenSyn;
        result[2] = (seenSem == 0)?1:semCorrect / (double) seenSem;
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
