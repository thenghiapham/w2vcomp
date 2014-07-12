package common;

import java.util.ArrayList;

import org.apache.commons.math3.stat.correlation.PearsonsCorrelation;

public class MenCorrelation {
	String[][] wordPairs;
	double[] golds;
	PearsonsCorrelation pearson;
	public MenCorrelation(String dataset) {
	    pearson = new PearsonsCorrelation();
		readDataset(dataset);
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
			golds[i] = Integer.parseInt(elements[2]);
		}
	}
	
	public double pearsonCorrelation(double[] predicts) {
	    return pearson.correlation(golds, predicts);
	}
	
	
}
