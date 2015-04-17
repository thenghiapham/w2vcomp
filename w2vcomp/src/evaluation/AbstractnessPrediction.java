package evaluation;

import java.io.BufferedInputStream;
import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import org.apache.commons.math3.stat.correlation.PearsonsCorrelation;
import org.apache.commons.math3.stat.correlation.SpearmansCorrelation;
import org.ejml.simple.SimpleMatrix;

import common.IOUtils;
import common.MenCorrelation;
import common.SimpleMatrixUtils;

import demo.TestConstants;
import space.Neighbor;
import space.SemanticSpace;
import word2vec.Images;

public class AbstractnessPrediction {
    String[] words;
    ArrayList<ArrayList<String>> pairs;
    double[] golds;
    PearsonsCorrelation pearson;
    SpearmansCorrelation spearman;
    
    public AbstractnessPrediction(String dataset, boolean composition, int field){
        pearson = new PearsonsCorrelation();
        spearman = new SpearmansCorrelation();
        field = field==-1? 1:field;
        if (!composition)
            readDataset(dataset, field);
        else
            readDatasetPairs(dataset);
    }
    
    public String[] getWords(){
        return words;
    }
    private void readDataset(String dataset, int field){
        ArrayList<String>  data =  IOUtils.readFile(dataset);
        golds = new double[data.size()];
        words = new String[data.size()];
        for (int i = 0; i < data.size(); i++) {
            String dataPiece = data.get(i);
            String elements[] = dataPiece.split("[ \t]+");
            words[i] = elements[0];
            golds[i] = Double.parseDouble(elements[field]);
        }
    }
    
   
    private double computeEntropy(double[] vec){
        double en = 0.0;
        double sum = 0.0;
        double [] k = new double[vec.length];
        double min = 0.0;
        SimpleMatrix u = new SimpleMatrix(1,vec.length,false,vec);
        double normF = u.normF();
             
        for (int i=0;i<vec.length;i++){
            vec[i]/=normF;
        }
        for (int i=0;i<vec.length;i++){
            k[i] = vec[i]+0.05;
            sum += k[i];
            if (vec[i]<=min){
                min = vec[i];
            }
            
        }
        
        
        for (int i=0;i<vec.length;i++){
            if (k[i] <= 0.0) continue;
            en -=  (k[i]/sum) * Math.log(k[i]/sum);
        }
        if (Double.isNaN(en)){
            System.out.println("IsNan");
            en = 0;
        }
       return en;
        
    }
    
  
    public double measureAbstractness(SemanticSpace space){
        ArrayList<Double> predicts = new ArrayList<Double>();
        ArrayList<Double> g  = new ArrayList<Double>();
        
        Set<String> rows = space.getWord2Index().keySet();
        int k=0;
        for (int i = 0; i < golds.length; i++) {
            
            if (!rows.contains(words[i])){
               continue;
            }
            else{
                predicts.add(k, computeEntropy(space.getVector(words[i])));
                g.add(k,golds[i]);
                k+=1;
            }
        }
        System.out.println("The are elements "+g.size()+" "+predicts.size());
        System.out.println("The are rows"+rows.size());
        
        double[] p1 = new double[g.size()]; 
        double[] p2 = new double[g.size()]; 
        for (int i=0 ;i<g.size();i++){
            if (Double.isNaN(predicts.get(i))){
                System.out.println(predicts.get(i));
                predicts.add(i,0.0);
            }
            p1[i] = g.get(i);
            p2[i] = predicts.get(i);
            System.out.println(words[i]+"\t"+p1[i]+"\t"+p2[i]);

        }
        return spearman.correlation(p1, p2);
    }
        
    public double measureAbstractness(SemanticSpace space, SimpleMatrix mapping, Set<String> tokeep){
        ArrayList<Double> predicts = new ArrayList<Double>();
        ArrayList<Double> g  = new ArrayList<Double>();

        
        Set<String> rows = space.getWord2Index().keySet();
        for (int i = 0; i < golds.length; i++) {
            if (!rows.contains(words[i]) || tokeep.contains(words[i])){
                continue;
            }
            else{
                //System.out.print(words[i]);
                predicts.add(computeEntropy(SimpleMatrixUtils.to2DArray((new SimpleMatrix(1,space.getVectorSize(),true,space.getVector(words[i])).mult(mapping)))[0]));
                g.add(golds[i]);
            }
        }
        System.out.println("The are "+g.size());
        
        double[] p1 = new double[g.size()]; 
        double[] p2 = new double[g.size()]; 
        for (int i=0 ;i<g.size();i++){
            p1[i] = g.get(i);
            p2[i] = predicts.get(i);
        }
        return spearman.correlation(p1, p2);
        }
    
    public double measureAbstractness(SemanticSpace space, SimpleMatrix mapping){
        ArrayList<Double> predicts = new ArrayList<Double>();
        ArrayList<Double> g  = new ArrayList<Double>();
        ArrayList<String> w = new ArrayList<String>();
        Set<String> rows = space.getWord2Index().keySet();
        for (int i = 0; i < golds.length; i++) {
            if (!rows.contains(words[i])){
                    continue;
            }
            else{
                predicts.add(computeEntropy(SimpleMatrixUtils.to2DArray((new SimpleMatrix(1,space.getVectorSize(),true,space.getVector(words[i])).mult(mapping)))[0]));
                g.add(golds[i]);
                w.add(words[i]);
            }
        }
        
        double[] p1 = new double[g.size()]; 
        double[] p2 = new double[g.size()]; 
        for (int i=0 ;i<g.size();i++){
            p1[i] = g.get(i);
            p2[i] = predicts.get(i);
            System.out.println(w.get(i)+"\t"+ p1[i]+"\t"+p2[i]);
        }
        
        System.out.println("The are "+g.size()+" "+spearman.correlation(p1, p2));
        return spearman.correlation(p1, p2);
        
        }
  
    private void readDatasetPairs(String dataset){
        ArrayList<String>  data =  IOUtils.readFile(dataset);
        golds = new double[data.size()];
        pairs = new ArrayList<ArrayList<String>>();
        for (int i = 0; i < data.size(); i++) {
            String dataPiece = data.get(i);
            String elements[] = dataPiece.split("[ \t]+");
            ArrayList<String> t = new ArrayList<String>();
            t.add(elements[0]);
            t.add(elements[1]);
            pairs.add(t);
            golds[i] = Integer.parseInt(elements[2]);
        }
    }
    
    public void printDecision(SemanticSpace space, SimpleMatrix mapping){
        
        ArrayList<Double> predicts = new ArrayList<Double>();
        ArrayList<Double> g  = new ArrayList<Double>();
        ArrayList<String> w = new ArrayList<String>();
        Set<String> rows = space.getWord2Index().keySet();
        for (int i = 0; i < golds.length; i++) {
            if (!rows.contains(pairs.get(i).get(0)) || !rows.contains(pairs.get(i).get(1))){
                    continue;
            }
            else{
                SimpleMatrix add = (new SimpleMatrix(1,space.getVectorSize(),true,space.getVector(pairs.get(i).get(0))).mult(mapping)).plus((new SimpleMatrix(1,space.getVectorSize(),true,space.getVector(pairs.get(i).get(1))).mult(mapping)));
                //SimpleMatrix add =(new SimpleMatrix(1,space.getVectorSize(),true,space.getVector(pairs.get(i).get(0)))).plus((new SimpleMatrix(1,space.getVectorSize(),true,space.getVector(pairs.get(i).get(1))))).mult(mapping);
                //SimpleMatrix add = (new SimpleMatrix(1,space.getVectorSize(),true,space.getVector(pairs.get(i).get(0)))).plus((new SimpleMatrix(1,space.getVectorSize(),true,space.getVector(pairs.get(i).get(1)))));
                double en = computeEntropy(SimpleMatrixUtils.to2DArray(add)[0]);
                predicts.add(en);
                g.add(golds[i]);
                w.add(pairs.get(i).get(0)+" "+pairs.get(i).get(1));
                System.out.println((golds[i]+1)%2+" "+en);
                
            }
        }
        
    }
    public double frequencyBaseline(String vocabulary){
        ArrayList<String>  data =  IOUtils.readFile(vocabulary);
        HashMap<String, Integer>  voc = new HashMap<String, Integer>();
        
        ArrayList<Integer> predicts = new ArrayList<Integer>();
        ArrayList<Double> g  = new ArrayList<Double>();
        
        for (int i = 0; i < data.size(); i++) {
            String dataPiece = data.get(i);
            String elements[] = dataPiece.split("[ \t]+");
            voc.put(elements[0], Integer.valueOf(elements[1]));      
        }
        
        int k=0;
        for (int i = 0; i < golds.length; i++) {
            if (!voc.containsKey(words[i])){
               continue;
            }
            else{
                predicts.add(k, voc.get(words[i]));
                g.add(k,golds[i]);
                k+=1;
            }
        }
        System.out.println("There are "+g.size());
        double[] p1 = new double[g.size()]; 
        double[] p2 = new double[g.size()]; 
        for (int i=0 ;i<g.size();i++){
            p1[i] = g.get(i);
            p2[i] = predicts.get(i);
        }
        return spearman.correlation(p1, p2);
        
    }
    
   void printNeighbours(SemanticSpace space, SemanticSpace space2) throws IOException{
       String outFile = "/home/angeliki/Documents/mikolov_composition/out/multimodal/NAACL/abstract_experiment/nearest_neighbours_top5.txt";
       
       BufferedWriter f = new BufferedWriter(new FileWriter(outFile));
       
       String text = "";
       for (int i=0;i<this.words.length;i++){
           double score = this.golds[i];
           //info for word
           String word = this.words[i];
           
           boolean isFirst = true;
           
           text += word+" "+ Double.toString(score)+" ";
           
           for (Neighbor s: space.getNeighbors(word,5,space2)){
               if (isFirst){
                   if (s.word.equals(word)){
                       continue;
                   }
                   text += s.word+" "+ Double.toString(s.sim)+" ";
                   //isFirst= false;
                   //break;
               }
               else{
                   text += s.word+" ";
               }
           }
           text +="\n";
       }
       f.write(text);
       f.close();
       
   }
    
    
    
    
    /**
     * @param args
     * @throws IOException 
     */
    public static void main(String[] args) throws IOException {
        String absFile = "/home/angeliki/Documents/mikolov_composition/misc/abstractness.txt";
       // SemanticSpace space = SemanticSpace.readSpace("/home/angeliki/Documents/mikolov_composition/out/multimodal/NAACL/baseline/out_wiki_n-1_m0.5_-1_r11.0_r220.0l1.0E-4.bin");
        //SemanticSpace space = SemanticSpace.readSpace("/home/angeliki/Documents/mikolov_composition/out/multimodal/NAACL/cnn/out_wiki_n5_m0.5_5_r11.0_r220.0l1.0E-4.bin");
        SemanticSpace space = SemanticSpace.readSpace("/home/angeliki/Documents/mikolov_composition/out/multimodal/NAACL/cnn_again_mm/out_wiki_n10_m0.5_10_r11.0_r21.0l1.0E-6.bin");
        String mapFile = "/home/angeliki/Documents/mikolov_composition/out/multimodal/NAACL/cnn/out_wiki_n5_m0.5_5_r11.0_r220.0l1.0E-4.mapping";
        SimpleMatrix mapping = new SimpleMatrix(IOUtils.readMatrix(new BufferedInputStream(new FileInputStream(mapFile)), false));
        double a;
        
        AbstractnessPrediction eval = new AbstractnessPrediction(absFile, false,1);
        //a = eval.measureAbstractness(space, mapping);
        a = eval.measureAbstractness(space);
        //a = eval.measureAbstractness(space, mapping,vision.getWord2Index().keySet());
        
        System.out.println("Spearman is "+a);
        
        Images im = new Images(TestConstants.VISION_FILE, true);
        eval.printNeighbours(space,im.getVisionSpace());
        
        //a = eval.measureAbstractness(vision);
        //System.out.println("Spearman is "+a);
        
        //a = eval.frequencyBaseline("/home/angeliki/sas/visLang/mmskipgram/ini/out_wiki.voc");
        //System.out.println("Spearman is "+a);
        
        
        //absFile = "/home/angeliki/Documents/mikolov_composition/misc/met-lit.txt";
        //eval = new AbstractnessPrediction(absFile, true);
        //eval.printDecision(space, mapping);
    }

}
