package utils;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;

import common.exception.ValueException;

import tree.Tree;

public class NotStatistics {
    public static HashMap<String, Integer> getNotConstructionStatistics(File parseFile) {
        HashMap<String, Integer> constructionMap = new HashMap<>();
        try {
            String fileName = parseFile.getName();
            System.out.println("Start :" + fileName);
            BufferedReader reader = new BufferedReader(new FileReader(parseFile));
            int lineNum = 0;
            String line = reader.readLine();
            while (line != null) {
                lineNum++;
                if (lineNum % 2000 == 0) {
                    System.out.println(fileName + ": " + lineNum);
                }
                if (!line.equals("")) {
                    try {
                        Tree tree = Tree.fromPennTree(line);
                        ArrayList<Tree> nodes = tree.allNodes();
                        for (Tree node: nodes) {
                            if (!node.isPreTerminal() && !node.isTerminal()) {
                                String construction = node.getConstruction();
                                if (!containsNot(node)) continue;
    //                            System.out.println(construction);
                                if (!constructionMap.containsKey(construction)) {
                                    constructionMap.put(construction, 1);
                                } else {
                                    constructionMap.put(construction, constructionMap.get(construction) + 1);
                                }
                            }
                        }
                    } catch (ValueException e) {
//                        e.printStackTrace();
                    }
                    line = reader.readLine();
                }
            }
            reader.close();
            return constructionMap;
        } catch (IOException e) {
            return constructionMap;
        }
    }
    
    public static boolean containsNot(Tree node) {
        if (!node.isPreTerminal() && !node.isTerminal() && node.getChildren().size() == 2) {
            for (Tree child: node.getChildren()) {
                if (child.isPreTerminal() && child.getSurfaceWords()[0].equals("not")) {
                    return true;
                }
            }
        } 
        return false;
        
    }
    
    // map1 is modified, map2 is not modified
    public static <K> void mergeHashMap(HashMap<K, Integer> map1, HashMap<K, Integer> map2) {
        Set<K> keys2 = map2.keySet();
        for (K key: keys2) {
            int num1 = map1.containsKey(key)?map1.get(key):0;
            int num2 = map2.containsKey(key)?map2.get(key):0;
            map1.put(key, num1 + num2);
        }
    }
    
    public static void printNotStatistics(HashMap<String, Integer> notData, HashMap<String, Integer> phraseData, String outputFile) {
        try {
            BufferedWriter writer = new BufferedWriter(new FileWriter(outputFile));
            for (String key: notData.keySet()) {
                int notNum = notData.get(key);
                int totalNum = phraseData.get(key);
                writer.write(key + "\t" + notNum + "\t" + totalNum + "\t" + (((double) notNum)/ totalNum) + "\n");
            }
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        
    }

    protected HashMap<String, Integer> statistics;
    protected ArrayList<File> files;
    protected int numThread;
    
    public NotStatistics(int numThread) {
        this.files = new ArrayList<>();
        this.numThread = numThread;
    }
    
    public HashMap<String, Integer> computeStatistics(File[] fileArray) {
        for (File file: fileArray) {
            files.add(file);
        }
        statistics = new HashMap<>();
        CountThread[] countThreads = new CountThread[numThread]; 
        for (int i = 0; i < numThread; i++) {
            countThreads[i] = new CountThread();
            countThreads[i].start();
        }
        for (int i = 0; i < numThread; i++) {
            try {
                countThreads[i].join();
            } catch (InterruptedException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
        return statistics;
    }
   
    protected class CountThread extends Thread {
        public void run() {
            while (true) {
                File file = null;
                synchronized (files) {
                    if (files.isEmpty()) {
                        return;
                    } else {
                        file = files.remove(files.size() - 1);
                    }
                }
                HashMap<String, Integer> tmpPhraseStatistics = NotStatistics.getNotConstructionStatistics(file);
                synchronized (statistics) {
                    mergeHashMap(statistics, tmpPhraseStatistics);
                }
            }
        }
    }
    
    public static void main(String[] args) {
        String dirPath = args[0];
        String outputFile = args[1];
        int numThread = new Integer(args[2]);
        File dir = new File(dirPath);
        File[] files = dir.listFiles();
        HashMap<String,Integer> notResult = new NotStatistics(numThread).computeStatistics(files);
        HashMap<String,Integer> phraseResult = new PhraseStatistics(numThread).computeStatistics(files);
        NotStatistics.printNotStatistics(notResult, phraseResult, outputFile);
        
    }
    
}
