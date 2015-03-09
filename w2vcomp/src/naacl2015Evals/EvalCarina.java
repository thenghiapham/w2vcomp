package naacl2015Evals;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import space.SemanticSpace;

import common.IOUtils;
import common.MenCorrelation;

import demo.TestConstants;

public class EvalCarina {

    /**
     * @param args
     */
    public static void main(String[] args) {
        MenCorrelation semSim =  new MenCorrelation(TestConstants.Carina_FILE,2);
        MenCorrelation visSim =  new MenCorrelation(TestConstants.Carina_FILE,3);
        
        SemanticSpace wordSpace = SemanticSpace.readSpace("/home/angeliki/Documents/mikolov_composition/out/multimodal/NAACL/cnn_again_mm/out_wiki_n10_m0.5_10_r11.0_r21.0l1.0E-6.bin");
        wordSpace.getSubSpace(semSim.getWords());
        String dataset = "/home/angeliki/Documents/mikolov_composition/misc/mcrae_similarity_semantic-sae_bimodal";
        ArrayList<String> data = IOUtils.readFile(dataset);          
        int field=2;
        
        HashMap<String, HashMap<String,Double>> scores = new  HashMap<String, HashMap<String,Double>>();
        
        for (int i = 0; i < data.size(); i++) {
            String dataPiece = data.get(i);
            String[] elements = dataPiece.split("[ \t]+");
            if (!scores.containsKey(elements[0])){
                scores.put(elements[0], new HashMap<String,Double>());
            }
            //scores.get(elements[0]).put(elements[1], Double.parseDouble(elements[field]));
            scores.get(elements[0]).put(elements[1], wordSpace.getSim(elements[0], elements[1]));
        }
        double a = visSim.evaluateSpaceSpearman(scores);
        System.out.println(a);
        }
    
    }


