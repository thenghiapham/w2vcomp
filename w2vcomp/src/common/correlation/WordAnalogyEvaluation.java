package common.correlation;

import java.util.ArrayList;

import space.NormalizedSemanticSpace;
import common.IOUtils;

public class WordAnalogyEvaluation {
    int threadNum = 25;
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
    public double[] evaluation(NormalizedSemanticSpace space) {
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
            boolean[] corrects = new boolean[section.size()];
            boolean[] seens = new boolean[section.size()];
            // create threads;
            int total = section.size();
            int div = total / threadNum;
            int mod = total % threadNum;
            EvaluateThread[] threads = new EvaluateThread[threadNum];
            for (int index = 0; index < threadNum; index++) {
                int beginIndex;
                int endIndex;
                if (index < mod) {
                    beginIndex = (div + 1) * index;
                    endIndex = (div + 1) * (index + 1);
                } else {
                    beginIndex = div * index + mod;
                    endIndex = div * (index + 1) + mod;
                }
                threads[index] = new EvaluateThread(space, section, corrects, seens, beginIndex, endIndex);
                // start threads;
                threads[index].start();
            }
            for (int index = 0; index < threadNum; index++) {
                try {
                    threads[index].join();
                } catch (InterruptedException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
            
            // join threads;
            for (int j = 0; j < section.size(); j++) {
                if (corrects[j]) sectionCorrect++;
                if (seens[j]) sectionSeen++;
            }
            System.out.println("acc: " + (sectionCorrect / (double) sectionSeen));
            if (i >= 5) {
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
        result[1] = (seenSyn == 0)?1:syncorrect / (double) seenSyn;
        result[2] = (seenSem == 0)?1:semCorrect / (double) seenSem;
        return result;
    }
    
    public boolean correct(NormalizedSemanticSpace space, String[] question) {
//        System.out.println("question: " + question[0] + " " +  question[1] + " " + question[2]);
        String answer = space.getAnalogy(question[0], question[1], question[2]);
//        System.out.println("answer: " + answer);
        if (answer.equals(question[3]))
            return true;
        return false;
    }
    
    protected class EvaluateThread extends Thread {
        ArrayList<String[]> section; 
        boolean[] corrects;
        boolean[] seens;
        int beginIndex;
        int endIndex;
        NormalizedSemanticSpace space;
        public EvaluateThread(NormalizedSemanticSpace space, ArrayList<String[]> section, 
                boolean[] corrects, boolean[] seens, int beginIndex, int endIndex) {
            this.beginIndex = beginIndex;
            this.endIndex = endIndex;
            this.section = section;
            this.corrects = corrects;
            this.seens = seens;
            this.space = space;
        }
        
        public void run() {
            for (int i = beginIndex; i < endIndex; i++) {
                String[] question = section.get(i);
                seens[i] = true;
                for (String word: question) {
                    if (space.getVector(word) == null) {
                        seens[i] = false;
                        break;
                    }
                }
                if (!seens[i]) continue;
                if (correct(space, question)) {
                    corrects[i] = true;
                }
            }
        }
    }
}
