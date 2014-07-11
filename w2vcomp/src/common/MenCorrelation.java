package common;

import java.util.ArrayList;

public class MenCorrelation {
	String[][] wordPairs;
	double[] golds;
	PearsonCorrelation
	public MenCorrelation(String dataset) {
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
	
	public void pearsonCorrelation(double[] predicts) {
	    
	}
}
