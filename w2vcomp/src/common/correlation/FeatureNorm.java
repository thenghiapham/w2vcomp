package common.correlation;

import java.util.ArrayList;
import java.util.HashMap;

import org.apache.commons.math3.stat.correlation.PearsonsCorrelation;
import org.apache.commons.math3.stat.correlation.SpearmansCorrelation;
import org.ejml.simple.SimpleMatrix;

import common.IOUtils;
import common.SimpleMatrixUtils;
import space.SMSemanticSpace;
import space.SemanticSpace;

public class FeatureNorm {
    double[] gold;
    String[][] pairs;
    String[] verbArgs;
    HashMap<String, ArrayList<String>> verbArgsPrototype;
    public FeatureNorm(String dataset) {
        verbArgsPrototype = new HashMap<String, ArrayList<String>>();
        readDataset(dataset);
    }
    
    protected void readDataset(String datasetFile) {
        // prepare gold and pair
        ArrayList<String> lines = IOUtils.readFile(datasetFile);
        pairs = new String[lines.size()][2];
        gold = new double[lines.size()];
        for (int i = 0; i < lines.size(); i++) {
            String[] elements = lines.get(i).split("\t");
            pairs[i][0] = elements[0] + "_" + elements[2];
            pairs[i][1] = FeatureNorm.removePos(elements[1]);
            gold[i] = Double.parseDouble(elements[3]);
        }
        
        // prepare prototype
        lines = IOUtils.readFile(datasetFile + ".topnouns");
        for (String line: lines) {
            String[] elements = line.split("\t");
            String verbArg = elements[0];
            String noun = removePos(elements[1]);
            if (verbArgsPrototype.containsKey(verbArg)) {
                verbArgsPrototype.get(verbArg).add(noun);
            } else {
                ArrayList<String> nounList = new ArrayList<String>();
                nounList.add(noun);
                verbArgsPrototype.put(verbArg, nounList);
            }
        }
    }
    
    public double[] evaluate(SemanticSpace space) {
        double[] correlations = new double[2];
        ArrayList<String> keys = new ArrayList<String>();
        keys.addAll(verbArgsPrototype.keySet());
        SimpleMatrix newMatrix = new SimpleMatrix(keys.size(), space.getVectorSize());
        for (int i = 0; i < keys.size(); i++) {
            String key = keys.get(i);
            ArrayList<String> nounList = verbArgsPrototype.get(key);
            SimpleMatrix row = newMatrix.extractVector(true, i);
            for (int j = 0; j < nounList.size(); j++) {
                SimpleMatrix nounVector = space.getVector(nounList.get(j));
                row = row.plus(nounVector);
            }
            newMatrix.setRow(i, 0, row.getMatrix().data);
        }
        
        SMSemanticSpace newSpace = new SMSemanticSpace(keys, newMatrix);
        
        double[] sims = cosine(pairs, newSpace, space);
        correlations[0] = (new PearsonsCorrelation()).correlation(gold, sims);
        correlations[1] = (new SpearmansCorrelation()).correlation(gold, sims);
        return correlations;
    }
    
    public static String removePos(String wordPos) {
        return wordPos.substring(0, wordPos.length() - 2).toLowerCase();
    }
    
    public double[] cosine(String[][] pairs, SemanticSpace space1, SemanticSpace space2) {
        double[] sims = new double[pairs.length];
        for (int i = 0; i < pairs.length; i++) {
            String[] pair = pairs[i];
//            System.out.println(pair[0] + " " + pair[1]);
            SimpleMatrix v1 = space1.getVector(pair[0]);
            SimpleMatrix v2 = space2.getVector(pair[1]);
            if (v1 == null || v2 == null) {
                sims[i] = 0;
            } else {
                sims[i] = SimpleMatrixUtils.cosine(v1, v2);
            }
            
        }
        return sims;
    }
}
