package common.correlation;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

import space.RawSemanticSpace;

public class Toefl {
    String[][] dataset;
    public Toefl(String datasetFile) throws IOException{
        dataset = new String[80][6];
        BufferedReader reader = new BufferedReader(new FileReader(datasetFile));
        for (int i = 0; i < 80; i++) {
            String line = reader.readLine();
            String[] elements = line.split("( |\t)+");
            if (i % 4 == 0) {
                dataset[i / 4][0] = elements[1];
            }
            if (elements[3].equals("TRUE")) {
                dataset[i / 4][5] = elements[2];
            }
            dataset[i / 4][(i % 4) + 1] = elements[3];
        }
        reader.close();     
    }
    
    public double evaluation(RawSemanticSpace space) {
        int wrongNum = 0;
        for (int i = 0; i < 80; i++) {
            String[] tuple = dataset[i];
            for (int j = 0; j < 6; j++) {
                System.out.print(tuple[i] + " ");
            }
            System.out.println();
            for (int j = 1; j < 5; j++) {
                System.out.print(space.getSim(tuple[0], tuple[j]));
            }
            System.out.println();
            
            double maxSim = space.getSim(tuple[0], tuple[5]);
            for (int j = 1; j < 5; j++) {
                double sim = space.getSim(tuple[0], tuple[j]);
                if (sim > maxSim) {
                    System.out.println("wrong");
                    wrongNum++;
                    break;
                }
                System.out.println("correct");
            }
        }
        return (80 - wrongNum) / 80.0;
    }
    
}
