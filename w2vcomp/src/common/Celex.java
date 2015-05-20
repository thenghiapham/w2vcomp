package common;

import java.util.ArrayList;
import java.util.HashMap;

public class Celex {
    public static HashMap<String, ArrayList<String[]>> getDict(String inputFile) {
        HashMap<String, ArrayList<String[]>> result = new HashMap<String, ArrayList<String[]>>();
        ArrayList<String> lines = IOUtils.readFile(inputFile);
        for (String line: lines) {
            if (line.endsWith("NA")) {
                String[] elements = line.split("\\s");
                String affix = elements[0];
                String[] stemAndWord = new String[2];
                stemAndWord[0] = elements[1];
                stemAndWord[1] = elements[3];
                ArrayList<String[]> list = null;
                if (result.containsKey(affix)) {
                    list = result.get(affix);
                } else {
                    list = new ArrayList<String[]>();
                    result.put(affix, list);
                }
                list.add(stemAndWord);
            }
        }
        return result;
    }
    
    public static ArrayList<String[]> readTestData(String inputFile) {
        ArrayList<String> lines = IOUtils.readFile(inputFile);
        ArrayList<String[]> result = new ArrayList<String[]>();
        for (String line: lines) {
            if (line.endsWith("NA")) {
                String[] elements = line.split("\\s");
                String[] affixStemAndWord = new String[3];
                affixStemAndWord[0] = elements[0];
                affixStemAndWord[1] = elements[1];
                affixStemAndWord[2] = elements[3];
                result.add(affixStemAndWord);
            }
        }
        return result;
    }
}
