package evaluation;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;
import java.util.Set;
import java.util.TreeMap;
import java.util.List;


import org.ejml.simple.SimpleMatrix;

import common.HeatMapPanel;
import common.IOUtils;
import common.SimpleMatrixUtils;
import demo.TestConstants;

import space.Neighbor;
import space.SemanticSpace;
import word2vec.Images;

public class ZeroShotEvalCV {
    
    long seed ;
    
    boolean normalize;
    SemanticSpace vision;
    SemanticSpace language;
    
    Set<String> skipragramConcepts;
    Set<String> zeroshotConcepts;
    Set<String> allConcepts;
  
    SimpleMatrix trVision;
    SimpleMatrix trLanguage;
    SimpleMatrix tsVision;
    SimpleMatrix tsLanguage;
    
    SimpleMatrix pretrainedMapping;
    
    Set<String> searchConcepts;
    Set<String> trConcepts;
    Set<String> tsConcepts;
    
    //static double[] lambdas = {0,1,2,2.5,3,3.5,4,4.5,5,4.5,6,7,8,9,10,11,12,13,14,15,16,17,18,19,20};
    //static double[] lambdas = {17,18,19,20,21,22,23,24,25,26,27,28,30,40,50};
    static double[] lambdas = {10,0};
    
    enum trainingDirection {
        v2l, l2v;
    }
    
    
    public ZeroShotEvalCV(SemanticSpace language, SemanticSpace vision, boolean normalize, String matFile, boolean usePretrained ) throws FileNotFoundException{
        
        this.normalize = normalize;
    
   
        this.language = language;
        this.vision = vision;
    
        if (usePretrained){
            this.pretrainedMapping = new SimpleMatrix(IOUtils.readMatrix(new BufferedInputStream(new FileInputStream(matFile)), false));
            //HeatMapPanel.plotHeatMap(pretrainedMapping);
            //pretrainedMapping = SimpleMatrix.identity(300);
            
        }
        
        createTrainingTestData(language, vision);
       
        
    }
    
    private void createTrainingTestData(SemanticSpace language, SemanticSpace vision){
        
        //find common elements

        allConcepts = new HashSet<String>(vision.getWord2Index().keySet());
        allConcepts.retainAll(language.getWord2Index().keySet());

        System.out.println(allConcepts.size());
        //read in train and test set
        skipragramConcepts = new HashSet<String>(IOUtils.readFile(TestConstants.TRAIN_CONCEPTS));
        skipragramConcepts.retainAll(allConcepts);
        zeroshotConcepts = new HashSet<String>(allConcepts);
        //zeroshotConcepts.removeAll(skipragramConcepts);
        HashSet<String> LSVRC2012 =  new HashSet<String>(IOUtils.readFile("/home/angeliki/Desktop/LSVRC2012.txt"));
        
        int trainedWords = 0;
        for (String word:zeroshotConcepts){
            for (String LSVRC2012Class : LSVRC2012){ 
                if (LSVRC2012Class.contains(word)){
                    trainedWords+=1;
                    break;
                }
            }
        }
        
        System.out.println(trainedWords+"  out of "+zeroshotConcepts.size()+" are in the LSVRC2012");
        
        
                
    }
    
    public void startCV(int folds,trainingDirection dir){
        SimpleMatrix mapping, estimated;
        
        ArrayList<String> concepts = new ArrayList<String>(zeroshotConcepts);
        Collections.shuffle(concepts);
        System.out.println(concepts.get(0));

        System.out.println("Size of 0shot concepts "+zeroshotConcepts.size());
     
        
        int fold_size = concepts.size()/folds;
        for (double lambda: lambdas){
            Collections.shuffle(concepts);
            System.out.println(concepts.get(0));
            double[] ranks= new  double[] {0,0,0,0,0,0};
            for (int i=0;i<folds;i++){
                int start_index = fold_size * i;
                int end_index = start_index + fold_size;
                trConcepts = new HashSet<String>(concepts);
                tsConcepts = new HashSet<String>(concepts.subList(start_index, end_index));
                trConcepts.removeAll(tsConcepts);
                //for baseline
                trConcepts.addAll(skipragramConcepts);
                searchConcepts  = allConcepts;
    
                
                if (!normalize){
                    System.out.println(trConcepts.size());
                    trVision = new SimpleMatrix(vision.getSubSpace(trConcepts).getVectors());
                    tsVision = new SimpleMatrix(vision.getSubSpace(tsConcepts).getVectors());
                    trLanguage = new SimpleMatrix(language.getSubSpace(trConcepts).getVectors());
                    tsLanguage = new SimpleMatrix(language.getSubSpace(tsConcepts).getVectors());
                    }
                else {
                    trVision = new SimpleMatrix(vision.getSubSpace(trConcepts).rowNormalize().getVectors());
                    tsVision = new SimpleMatrix(vision.getSubSpace(tsConcepts).rowNormalize().getVectors());
                    trLanguage = new SimpleMatrix(language.getSubSpace(trConcepts).rowNormalize().getVectors());
                    tsLanguage = new SimpleMatrix(language.getSubSpace(tsConcepts).rowNormalize().getVectors());
                }
                
                if (this.pretrainedMapping!=null){
                    //System.out.println("TRAINING: USING PRETRAINED");
                    mapping = this.pretrainedMapping;
                }
                else{
                   // System.out.println("TRAINING: TRAINING WITH RIDGE");
                    mapping = trainMapping(dir,lambda);
                    //HeatMapPanel.plotHeatMap(mapping);
                    
                }
    
                estimated = applyMapping(mapping,dir);
                int j=0;
                double[] temp = new double[ranks.length];
                for(double r: evalRankAggr(estimated, dir )){
                    temp[j] = r;
                    ranks[j++] += r/(double)folds;
                }
                System.out.print(i+" ");
                printRanks(temp);
             }
            printRanks(ranks);
        }
    }
    
    
    
    
    private static SimpleMatrix solve(SimpleMatrix A, SimpleMatrix B,double lambda) {
        SimpleMatrix ATA = A.transpose().mult(A);
        int size = ATA.numCols();
        SimpleMatrix eye = SimpleMatrix.identity(size).scale(lambda);
        SimpleMatrix eyeTeye = eye.transpose().mult(eye);
        
        return (ATA.plus(eyeTeye)).pseudoInverse().mult(A.transpose()).mult(B);
        //return SimpleMatrix.identity(size);
        
    }
    
    private SimpleMatrix trainMapping(trainingDirection dir, double lambda){
        
        switch(dir){
        case v2l:
            return solve(trVision,trLanguage,lambda);
        case l2v:
            return solve(trLanguage,trVision, lambda);
        }
        return null;
    }
    
   
    
    private SimpleMatrix applyMapping(SimpleMatrix mappingF, trainingDirection dir){
        
        
        switch(dir){
        case v2l:
            return tsVision.mult(mappingF);
        case l2v:
            return tsLanguage.mult(mappingF);
        }
        
        return null;
    }
    
    private static Map<String, Double> sortByComparator(Map<String, ArrayList<Double>> unsortMap) {
        
        // Convert Map to List
        List<Map.Entry<String, ArrayList<Double>>> list = 
            new LinkedList<Map.Entry<String, ArrayList<Double>>>(unsortMap.entrySet());
 
        // Sort list with comparator, to compare the Map values
        Collections.sort(list, new Comparator<Map.Entry<String, ArrayList<Double>>>() {
            public int compare(Map.Entry<String, ArrayList<Double>> o1,
                                           Map.Entry<String, ArrayList<Double>> o2) {
                if (o1.getValue().get(0)==o2.getValue().get(0)){
                    //System.out.println(o1.getValue());
                    return (o1.getValue().get(1)).compareTo(o2.getValue().get(1));
                }
                return (o1.getValue().get(0)).compareTo(o2.getValue().get(0));
            }

            
        });
 
        // Convert sorted map back to a Map
        Map<String, Double> sortedMap = new LinkedHashMap<String, Double>();
        for (Iterator<Map.Entry<String, ArrayList<Double>>> it = list.iterator(); it.hasNext();) {
            Map.Entry<String, ArrayList<Double>> entry = it.next();
            sortedMap.put(entry.getKey(), entry.getValue().get(0));
        }
        return sortedMap;
    }
    
    public static void printMap(Map<String, Double> map) {
        for (Map.Entry<String, Double> entry : map.entrySet()) {
            System.out.println("[Key] : " + entry.getKey() 
                                      + " [Value] : " + entry.getValue());
        }
    }
    
    public static Map<String, Double> concertAverageToRank(Map<String, Double> map){
        Map<String, Double> newMap = new HashMap<String, Double>();
        int i=0;
        for (Map.Entry<String, Double> entry : map.entrySet()) {
            newMap.put( entry.getKey(), (double) i);
            i++;
        }
        return newMap;
    }
    private double[] evalRankAggr(SimpleMatrix vectors, trainingDirection dir ){
        
        double[] ranks= new  double[] {0,0,0,0,0,0 };
        
        SemanticSpace space = new SemanticSpace(new ArrayList<String>(tsConcepts),SimpleMatrixUtils.to2DArray(vectors));
        SemanticSpace evalSpace = null ;
        
        switch (dir){
        case l2v:
            evalSpace = vision.getSubSpace(searchConcepts);
            break;
        case v2l:
            evalSpace = language.getSubSpace(searchConcepts);
            break;
        }
        
        //System.out.println("size of eval space "+searchConcepts.size());
        HashMap<String, HashMap<String,Integer>> NNS_evalspace = new HashMap<String,HashMap<String,Integer>>();
        for (String w: evalSpace.getWord2Index().keySet()){
            NNS_evalspace.put(w, new HashMap<String,Integer>());
            Neighbor[] NNs = evalSpace.getNeighbors(w, space.getWord2Index().keySet().size(),  space);
            for (int i=0;i<NNs.length;i++){
                NNS_evalspace.get(w).put(NNs[i].word, i);
            }
        }
        
        HashMap<String, HashMap<String,ArrayList<Double>>> NNS_space = new HashMap<String,HashMap<String,ArrayList<Double>>>();

        HashMap<String, HashMap<String,Double>> NNS_space_sims =  new HashMap<String,HashMap<String,Double>>();
        for (String word: tsConcepts){
            NNS_space.put(word, new HashMap<String,ArrayList<Double>>());
            NNS_space_sims.put(word, new HashMap<String,Double>());
            Neighbor[] NNs = space.getNeighbors(word, evalSpace.getWord2Index().keySet().size(),  evalSpace);
            
            for (int i=0;i<NNs.length;i++){
                String n = NNs[i].word;
                
                ArrayList<Double> t = new ArrayList<Double>();
                t.add((double) i);
                //t.add((double) (NNS_evalspace.get(n).get(word)));
                t.add((double) ((NNS_evalspace.get(n).get(word)+i)/2));
                t.add(space.getSim(word, n, evalSpace));
                NNS_space.get(word).put(n, t);
                //NNS_space.get(word).put(n, (double) i);
                //NNS_space.get(word).put(n, (double) (NNS_evalspace.get(n).get(word)));

            }
            Map<String, Double> sortedMap = sortByComparator(NNS_space.get(word));
            Map<String, Double> toRanks = concertAverageToRank(sortedMap);
            //printMap(sortedMap);
            double rank = toRanks.get(word);
               
            if (rank==0){
                ranks[0]+=1; ranks[1]+=1; ranks[2]+=1; ranks[3]+=1; ranks[4]+=1;  ranks[5]+=1;
            }
            else if (rank==1){
                ranks[1]+=1; ranks[2]+=1; ranks[3]+=1; ranks[4]+=1;  ranks[5]+=1;
            }
            else if (rank<=10){
                ranks[2]+=1; ranks[3]+=1; ranks[4]+=1;  ranks[5]+=1;
            }
            else if (rank<=20){
                ranks[3]+=1; ranks[4]+=1;  ranks[5]+=1;
            }
            else if (rank<=50){
                ranks[4]+=1; ranks[5]+=1; 
            }
            else {
                ranks[5]+=1;
            }
        }
            
            
     
        //System.out.println(r/(double) tsConcepts.size());
        for (int i=0;i<ranks.length;i++){
            ranks[i] /= (double) tsConcepts.size();
        }
        
        return ranks;
        
    }
    
    
    
    
    public void printRanks(double [] ranks){
        System.out.print("P@1: "+ranks[0] * 100+" ");
        System.out.print("P@2: "+ranks[1] * 100+" ");
        System.out.print("P@10: "+ranks[2] * 100+" ");
        System.out.print("P@20: "+ranks[3] * 100+" ");
        System.out.print("P@50: "+ranks[4] * 100+" ");
        System.out.println();
    }

    /**
     * @param args
     * @throws FileNotFoundException 
     */
    public static void main(String[] args) throws FileNotFoundException {
        
        long seed = System.nanoTime();
        //long seed = TestConstants.SEED;
        System.out.println("Seed is "+seed);
        
        //SemanticSpace wordSpace = SemanticSpace.readSpace("/home/angeliki/Documents/mikolov_composition/out/multimodal/NAACL/cnn_again_zero/out_wiki_n5_m0.5_5_r11.0_r220.0l1.0E-4.bin");
        SemanticSpace wordSpace = SemanticSpace.readSpace("/home/angeliki/Documents/mikolov_composition/out/multimodal/NAACL/baseline/out_wiki_n-1_m0.5_-1_r11.0_r220.0l1.0E-4.bin");
        //SemanticSpace wordSpace = SemanticSpace.readSpace("/home/angeliki/Documents/mikolov_composition/out/multimodal/NAACL/cnn_again_mm_zero/out_wiki_n20_m0.5_20_r11.0_r21.0l1.0E-4.bin");
        //SemanticSpace wordSpace = SemanticSpace.importSpace("/home/angeliki/Downloads/skip3b.dm");
        String matFile = "/home/angeliki/Documents/mikolov_composition/out/multimodal/NAACL/cnn_again_zero/out_wiki_n5_m0.5_5_r11.0_r220.0l1.0E-4.mapping";
        Images im = new Images(TestConstants.VISION_FILE, true);
        SemanticSpace visionSpace = im.getVisionSpace();
        
        
        trainingDirection dir = trainingDirection.l2v;
        
        
        
        //training mapping
        ZeroShotEvalCV exp = new ZeroShotEvalCV(wordSpace,visionSpace,false, matFile, false);
        

        //evaluate mapping
        exp.startCV(1,dir);
        
        
        
     
        
     

    }

}
