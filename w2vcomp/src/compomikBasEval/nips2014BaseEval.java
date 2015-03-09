package compomikBasEval;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import org.apache.commons.math3.stat.correlation.PearsonsCorrelation;
import org.apache.commons.math3.stat.correlation.SpearmansCorrelation;
import org.ejml.simple.SimpleMatrix;

import space.SemanticSpace;

import common.IOUtils;
import common.MathUtils;
import common.SimpleMatrixUtils;

public class nips2014BaseEval {
    
    ArrayList<String[]> phrasePairs ;
    Set<String> unique_pairs;
    double[] golds ;
    SemanticSpace space;
    datasetType type;
    ArrayList<ArrayList<ArrayList<String>>> sentencePairs ;

    
    enum datasetType {
        twopairs, onepair, sentence, svo;
    }
    
    
    public nips2014BaseEval(SemanticSpace space,String dataset, datasetType choice){
        this.space = space;
        unique_pairs = new HashSet<String>();
        switch(choice){
        case twopairs:
            phrasePairs = new ArrayList<String[]>();

            readDatasetPairs(dataset);
            break;
        case onepair:
            phrasePairs = new ArrayList<String[]>();

            readDatasetOnePair(dataset);
            break;
        case sentence:
            sentencePairs = new ArrayList<ArrayList<ArrayList<String>>>();
            readDatasetSick(dataset);
            break;
        case svo:
            sentencePairs = new ArrayList<ArrayList<ArrayList<String>>>();
            readDatasetGreffen(dataset);
            break;
        }
        
       
    }

    private void readDatasetPairs(String dataset) {
        ArrayList<String> data = IOUtils.readFile(dataset);
        golds = new double[data.size()];
       
        for (int i = 0; i < data.size(); i++) {
            String[] item = new String[4];
            String dataPiece = data.get(i);
            String elements[] = dataPiece.split(" ");
            item[0] = elements[0];
            item[1] = elements[1];
            item[2] = elements[2];
            item[3] = elements[3];
            phrasePairs.add(i, item);
            golds[i] = Double.parseDouble(elements[4]);
            
        }
    }
    
    private void readDatasetOnePair(String dataset) {
        ArrayList<String> data = IOUtils.readFile(dataset);
        golds = new double[data.size()];
        for (int i = 0; i < data.size(); i++) {
            String[] item = new String[3];
            String dataPiece = data.get(i);
            String elements[] = dataPiece.split(" ");
            item[0] = elements[0];
            item[1] = elements[1];
            item[2] = elements[2];
            phrasePairs.add(i, item);
            golds[i] = Double.parseDouble(elements[3]);
            
        }
    }
    
    
    
    private void readDatasetSick(String dataset) {
        ArrayList<String> data = IOUtils.readFile(dataset);
        golds = new double[data.size()];
       
        for (int i = 0; i < data.size(); i++) {
            String dataPiece = data.get(i);
            String elements[] = dataPiece.split("\t");
            ArrayList<ArrayList<String>> temp = new ArrayList<ArrayList<String>>();
            for (int j=1;j<3;j++){
                ArrayList<String> sentence = new ArrayList<String>();
                for (String word:elements[j].replace(",", "").replace(".", "").split(" ")){
                    sentence.add(word);
                }
                temp.add(j-1,sentence);
            }
            
            sentencePairs.add(i, temp);
            golds[i] = Double.parseDouble(elements[3]);
            
        }
    }
    
    private void readDatasetGreffen(String dataset) {
        ArrayList<String> data = IOUtils.readFile(dataset);
        golds = new double[data.size()];
       
        for (int i = 0; i < data.size(); i++) {
            String dataPiece = data.get(i);
            String elements[] = dataPiece.split(" ");
            
            ArrayList<ArrayList<String>> temp = new ArrayList<ArrayList<String>>();
            ArrayList<String> sentence= new ArrayList<String>();
            sentence.add(elements[2]);
            sentence.add(elements[3]);
            sentence.add(elements[4]);
            sentence.add(elements[6]);
            sentence.add(elements[7]);
            temp.add(0,sentence);
            
            ArrayList<String> sentence1= new ArrayList<String>(sentence);
            sentence1.remove(2);
            sentence1.add(2,elements[5]);
            temp.add(1,sentence1);
            
            sentencePairs.add(i, temp);
            golds[i] = Double.parseDouble(elements[8]);
            
        }
    }
    
    public double[] simsPairs(datasetType type){
        double[] s = new double[phrasePairs.size()];
        SimpleMatrix mat1, mat2, mat3, mat4;
        int j=0;
        
        for (int i=0;i<phrasePairs.size();i++){
            switch(type){
            case twopairs:
                mat1 = new SimpleMatrix(1,space.getVectorSize(),false,space.getVector(phrasePairs.get(i)[0]));
                mat2 = new SimpleMatrix(1,space.getVectorSize(),false,space.getVector(phrasePairs.get(i)[1]));
                mat3 = new SimpleMatrix(1,space.getVectorSize(),false,space.getVector(phrasePairs.get(i)[2]));
                mat4 = new SimpleMatrix(1,space.getVectorSize(),false,space.getVector(phrasePairs.get(i)[3]));
                
                s[j] = MathUtils.cosine(SimpleMatrixUtils.to2DArray(mat1.plus(mat2))[0], SimpleMatrixUtils.to2DArray(mat3.plus(mat4))[0]);
                j++;
                break;
            case onepair:
                mat1 = new SimpleMatrix(1,space.getVectorSize(),false,space.getVector(phrasePairs.get(i)[0]));
                mat2 = new SimpleMatrix(1,space.getVectorSize(),false,space.getVector(phrasePairs.get(i)[1]));
                mat3 = new SimpleMatrix(1,space.getVectorSize(),false,space.getVector(phrasePairs.get(i)[2]));


                s[j] = MathUtils.cosine(SimpleMatrixUtils.to2DArray(mat1.plus(mat2))[0], SimpleMatrixUtils.to2DArray(mat2.plus(mat3))[0]);
                j++;
                break;
               
            }         
        }
        return s;
    }
    
    
    public double[] simsSick(){
        double[] s = new double[sentencePairs.size()];
        SimpleMatrix mat1, mat2;
        int j=0;
        
        for (int i=0;i<sentencePairs.size();i++){

                mat1 = SimpleMatrixUtils.sumRows(new SimpleMatrix(space.getSubSpace(sentencePairs.get(i).get(0)).getVectors()));
                mat2 = SimpleMatrixUtils.sumRows(new SimpleMatrix(space.getSubSpace(sentencePairs.get(i).get(1)).getVectors()));

                s[j] = MathUtils.cosine(SimpleMatrixUtils.to2DArray(mat1)[0], SimpleMatrixUtils.to2DArray(mat2)[0]);
                j++;
           
         }         
        return s;
    }
    
    public static void main(String[] args) {
        
        PearsonsCorrelation pearson = new PearsonsCorrelation();
        SpearmansCorrelation spearman = new SpearmansCorrelation();
        
        SemanticSpace wordSpace = SemanticSpace.readSpace("/home/angeliki/Documents/mikolov_composition/out/bnc/out_bnc_100.bin");
        String ANDataset = "/home/angeliki/Documents/mikolov_composition/misc/compo_misc/an_lemma.txt";
        
        nips2014BaseEval eval1 = new nips2014BaseEval(wordSpace, ANDataset,datasetType.twopairs);
        double[] ranks1 = eval1.simsPairs(datasetType.twopairs);
        for (int i=0;i<ranks1.length;i++){
           // System.out.println(ranks1[i]+" "+eval1.golds[i]);
        }
        System.out.println("AN Dataset: " + pearson.correlation(ranks1, eval1.golds)); 
        
        String NNDataset = "/home/angeliki/Documents/mikolov_composition/misc/compo_misc/nn_lemma.txt";
        eval1 = new nips2014BaseEval(wordSpace, NNDataset,datasetType.twopairs);
        ranks1= eval1.simsPairs(datasetType.twopairs);
        System.out.println("NN Dataset: " + pearson.correlation(ranks1, eval1.golds)); 
        
        String SVDataset = "/home/angeliki/Documents/mikolov_composition/misc/compo_misc/sv_lemma.txt";
        eval1 = new nips2014BaseEval(wordSpace, SVDataset,datasetType.onepair);
        ranks1= eval1.simsPairs(datasetType.onepair);
        System.out.println("SV Dataset: " + pearson.correlation(ranks1, eval1.golds)); 
        
        String VODataset = "/home/angeliki/Documents/mikolov_composition/misc/compo_misc/vo_lemma.txt";
        eval1 = new nips2014BaseEval(wordSpace, VODataset,datasetType.twopairs);
        ranks1= eval1.simsPairs(datasetType.twopairs);
        System.out.println("VO Dataset: " + pearson.correlation(ranks1, eval1.golds)); 
        
        String SickDataset = "/home/angeliki/Documents/mikolov_composition/misc/compo_misc/SICK_train_trial.txt";
        eval1 = new nips2014BaseEval(wordSpace, SickDataset,datasetType.sentence);
        ranks1= eval1.simsSick();
        System.out.println("Sick: " + pearson.correlation(ranks1, eval1.golds)); 
        
        String Greffen = "/home/angeliki/Documents/mikolov_composition/misc/compo_misc/GS2012data.txt";
        eval1 = new nips2014BaseEval(wordSpace, Greffen,datasetType.svo);
        ranks1= eval1.simsSick();
        System.out.println("Greffen: " + pearson.correlation(ranks1, eval1.golds));


    }

}
