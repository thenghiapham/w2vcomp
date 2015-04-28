package common.correlation;

import java.util.ArrayList;
import java.util.HashSet;

//import org.apache.commons.math3.stat.inference.

import space.SemanticSpace;

import common.IOUtils;

public class AntonymSynonymROC {
    String[][] pairs;
    double[] golds;
    public AntonymSynonymROC(String antonymFile, String synonymFile) {
        // TODO: unknow words?
        String[][] antonymPairs = getPairs(antonymFile);
        String[][] synonymPairs = getPairs(synonymFile);
        pairs = new String[antonymPairs.length + synonymPairs.length][];
        System.arraycopy(antonymPairs, 0, pairs, 0, antonymPairs.length);
        System.arraycopy(synonymPairs, 0, pairs, antonymPairs.length, synonymPairs.length);
        golds = new double[pairs.length];
        for (int i = 0; i < antonymPairs.length; i++) {
            golds[i] = 0;
        }
        for (int i = 0; i < synonymPairs.length; i++) {
            golds[i + antonymPairs.length] = 1;
        }
        
        
    }
    
    public double areaUnderCurve(SemanticSpace space) {
        double[] predicted = new double[golds.length];
        for (int i = 0; i < pairs.length; i++) {
            predicted[i] = space.getSim(pairs[i][0], pairs[i][1]);
        }
        return AreaUnderCurve.computeAUC(golds, predicted);
    }
    
    protected String[][] getPairs(String inputFile) {
        ArrayList<String> xnymInfo = IOUtils.readFile(inputFile);
        HashSet<String> filteredInfo = new HashSet<>();
        boolean verb = inputFile.contains("verb");
        
        for (String pair: xnymInfo) {
            String[] elements = pair.split("\\s");
            if (!elements[1].equals("0")) {
                filteredInfo.add(pair);
            }
        }
        String[][] result = new String[filteredInfo.size()][];
        int index = 0;
        for (String filteredPair: filteredInfo) {
            filteredPair = filteredPair.replaceAll("_", "-");
            String[] elements = filteredPair.split("\\s");
            if (verb) {
                elements[0] = elements[0].replaceFirst("to-", "");
                elements[1] = elements[1].replaceFirst("to-", "");
            }
            
            
            result[index] = elements;
            index++;
        }
        if (verb) {
//            System.out.println(result[0][0] + " " + result[0][1]);
        }
        return result;
    }
    
    
}
