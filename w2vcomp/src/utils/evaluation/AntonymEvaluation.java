package utils.evaluation;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

import space.RawSemanticSpace;
import space.SemanticSpace;

public class AntonymEvaluation {
    public static void evaluate(String spaceFile, String datasetFile) throws IOException{
        ArrayList<String> words = readWord(datasetFile);
        ArrayList<ArrayList<String[]>> info = readAntoSynoSimnyms(datasetFile);
        System.out.println(info.size());
        System.out.println(words.size());
        double difference = 0;
        int count = 0;
        SemanticSpace space = RawSemanticSpace.readSpace(spaceFile);
        for (int index = 0; index < words.size(); index++) {
            String word = words.get(index);
            if (space.getVector(word) ==  null) continue;
            ArrayList<String[]> antoSynoSimnyms = info.get(index);
            for (String[] senseInfo: antoSynoSimnyms) {
                String[] antonyms = senseInfo[0].split(",");
                String[] synonyms = senseInfo[1].split(",");
                String[] simnyms = senseInfo[2].split(",");
                for (String antonym: antonyms) {
                    double antonymSim = space.getSim(word, antonym);
                    if (antonymSim == 0) continue;
                    for (String synonym: synonyms) {
                        double synonymSim = space.getSim(word, synonym);
                        if (synonymSim == 0) continue;
                        count++;
                        difference += (synonymSim - antonymSim);
                    }
                    for (String simnym: simnyms) {
                        double simnymSim = space.getSim(word, simnym);
                        if (simnymSim == 0) continue;
                        count++;
                        difference += (simnymSim - antonymSim);
                    }
                }
            }
        }
        System.out.println("difference = " + (difference / count));
    }
    
    public static ArrayList<ArrayList<String[]>> readAntoSynoSimnyms(String adjFile) throws IOException{
        ArrayList<String> words = new ArrayList<String>();
        ArrayList<ArrayList<String[]>> result = new ArrayList<ArrayList<String[]>>();
        BufferedReader reader = new BufferedReader(new FileReader(adjFile));
        String line = reader.readLine();
        ArrayList<String[]> antoSynoSimnyms = null;
        while (line != null) {
            if (line.equals("___")) {
                // close and add
                if (antoSynoSimnyms != null) {
                    result.add(antoSynoSimnyms);
                }
                antoSynoSimnyms = new ArrayList<String[]>();
                // read new line
                words.add(reader.readLine());
            } else if (line.equals("+++")) {
                // read 3 lines and add
                String[] senseAntoSynoSims = new String[3];
                senseAntoSynoSims[0]=reader.readLine();
                senseAntoSynoSims[1]=reader.readLine();
                senseAntoSynoSims[2]=reader.readLine();
                antoSynoSimnyms.add(senseAntoSynoSims);
            }
            line = reader.readLine();
        }
        result.add(antoSynoSimnyms);
        reader.close();
        return result;
    }
    
    public static ArrayList<String> readWord(String adjFile) throws IOException{
        ArrayList<String> words = new ArrayList<String>();
        
        BufferedReader reader = new BufferedReader(new FileReader(adjFile));
        String line = reader.readLine();
        
        while (line != null) {
            if (line.equals("___")) {
                words.add(reader.readLine());
            } 
            line = reader.readLine();
        }
        reader.close();
        return words;
    }
    
    public static void main(String[] args) throws IOException {
        String evalDataFile = args[0];
        String spaceFile = args[1];
        evaluate(spaceFile, evalDataFile);
        evaluate(spaceFile.replaceAll(".bin","_anto.bin"), evalDataFile);
        evaluate(spaceFile.replaceAll(".bin","_anto_train.bin"), evalDataFile);
    }
}
