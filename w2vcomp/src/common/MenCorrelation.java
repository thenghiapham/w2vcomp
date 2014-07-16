package common;

import java.util.ArrayList;

import org.apache.commons.math3.stat.correlation.PearsonsCorrelation;
import org.apache.commons.math3.stat.correlation.SpearmansCorrelation;

import space.SemanticSpace;

public class MenCorrelation {
	String[][] wordPairs;
	double[] golds;
	PearsonsCorrelation pearson;
	SpearmansCorrelation spearman;
	public MenCorrelation(String dataset) {
	    pearson = new PearsonsCorrelation();
	    spearman = new SpearmansCorrelation();
		readDataset(dataset);
	}
	
	public MenCorrelation(String[][] wordPairs, double[] golds) {
	    pearson = new PearsonsCorrelation();
        spearman = new SpearmansCorrelation();
        this.wordPairs = wordPairs;
        this.golds = golds;
	}
	
	public void readDataset(String dataset) {
		ArrayList<String> data = IOUtils.readFile(dataset);
		golds = new double[data.size()];
		wordPairs = new String[data.size()][2];
		for (int i = 0; i < data.size(); i++) {
			String dataPiece = data.get(i);
			String elements[] = dataPiece.split(" ");
			wordPairs[i][0] = elements[0];
			wordPairs[i][1] = elements[1];
			golds[i] = Double.parseDouble(elements[2]);
		}
	}
	
	public double pearsonCorrelation(double[] predicts) {
	    return pearson.correlation(golds, predicts);
	}
	
	public double spearmanCorrelation(double[] predicts) {
        return spearman.correlation(golds, predicts);
    }
	
	public double evaluateSpacePearson(SemanticSpace space) {
	    double[] predicts = new double[golds.length];
	    for (int i = 0; i < golds.length; i++) {
	        predicts[i] = space.getSim(wordPairs[i][0], wordPairs[i][1]);
	    }
	    return pearson.correlation(golds, predicts);
	}
	
	public double evaluateSpaceSpearman(SemanticSpace space) {
        double[] predicts = new double[golds.length];
        for (int i = 0; i < golds.length; i++) {
            predicts[i] = space.getSim(wordPairs[i][0], wordPairs[i][1]);
        }
        return spearman.correlation(golds, predicts);
    }
	
	public double[] getGolds() {
	    return golds;
	}
}
