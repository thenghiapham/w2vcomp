package evaluation;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.*;

import javax.imageio.ImageIO;
import javax.swing.JFrame;
import javax.swing.JPanel;


import common.IOUtils;
import common.Stats;
import common.WeightedObject;


/**
 * Represents a similarity matrix between words and objects.
 * @author bkiev_000
 *
 */
public class CompareAssociations {

   // // For the Childes corpus, this maps words onto objects for the gold standard.
    //public static int[] GOLD_OVERALL = {0,1,2,2,2,3,3,4,4,5,5,5,5,6,6,7,8,9,10,10,10,11,11,12,13,13,13,14,15,15,16,13,4,10,6,6,4};  

    // Map words onto object.
   // public int[] goldStandard = {0,1,2,2,2,3,3,4,4,5,5,5,5,6,6,7,8,9,10,10,10,11,11,12,13,13,13,14,15,15,16,13,4,10,6,6,4}; 
    public HashMap<String, String> goldStandard;
    
    
    // Objects.
    public String[] xAxis;
    public ArrayList<String> xAxis_2;
    
    // Words.
    public String[] yAxis;
    public ArrayList<String> yAxis_2;
    
    // Probability that words are related to objects.
    public double[][] values;
            
    public void fScore(BufferedWriter w, String tag) throws IOException {
        
        WeightedObject<Boolean>[] corrects = new WeightedObject[yAxis.length];
                
        for(int i=0;i<yAxis.length;i++) {
            double max = values[i][0];
            int maxIndex = 0;
            for(int j=0;j<xAxis.length;j++) {
                if(values[i][j] > max) {
                    max = values[i][j];
                    maxIndex = j;
                }
            }
            
            if(maxIndex == xAxis_2.indexOf(goldStandard.get(yAxis[i]))) {
                corrects[i] = new WeightedObject<Boolean>(true, max);
            } else {
                corrects[i] = new WeightedObject<Boolean>(false, max);              
            }
            
        }
        Arrays.sort(corrects, new Comparator<WeightedObject>() {
            public int compare(WeightedObject arg0, WeightedObject arg1) {
                return -arg0.compareTo(arg1);
            }
        });
        
        
        /*
        for(WeightedObject<Boolean> correct : corrects) {
            if(correct.object) {
                System.out.print(1);
            } else {
                System.out.print(0);
            }
        }
        System.out.println();
        */
        double f_all = 0;
        int correct = 0;
        for(int i=0;i<corrects.length;i++) {
            if(corrects[i].object) {
                correct++;
            }
            double precision = correct / (double)(i + 1);
            double recall = correct / (double)corrects.length;              
            double f = 2 * (precision * recall) / (precision + recall);
            String textOut = yAxis[i] + "," + precision + "," + recall + "," + f + "\n";
            f_all+=f;
            if(w != null) {
                w.write(textOut);
            } else {
                System.out.print(textOut);
            }
        }
        System.out.println("F overall is "+(f_all/corrects.length));
    }
    

    /**
     * Given a set of models, find the maximum weighting to optimize number correct.
     * @param models
     * @return
     */
    /*
    public static double[] optimizeHybrid(final CompareAssociations[] models) {
        
        MaximizationFunction maxFunc = new MaximizationFunction() {
            public double function(double[] param) {                
                CompareAssociations hybrid = generateHybrid(models, param);  
                double score = hybrid.score();
                return score;
            }
        };
       
        double[] optimizations = new double[models.length];
        for(int i=0;i<optimizations.length;i++) {
            optimizations[i] = 1;
        }
        
        Maximization m = new Maximization();
        m.nelderMead(maxFunc, optimizations, 1000);
        optimizations = m.getParamValues();
                
        return optimizations;
    }*/
    
    /**
     * Calculate number correct.
     * This finds the maximum value in each column and compares to the gold standard.
     * @return
     */
    public double score() {             
        double score = 0;
        for(int i=0;i<yAxis.length;i++) {
            double max = values[i][0];
            int maxIndex = 0;
            for(int j=0;j<xAxis.length;j++) {
                if(values[i][j] > max) {
                    max = values[i][j];
                    maxIndex = j;
                }
            }
            
            if(maxIndex == xAxis_2.indexOf(goldStandard.get(yAxis[i])))  {
                System.out.println("Correct for "+yAxis_2.get(i));
                score++;
            }
        }
        
        return score;
    }   

    /**
     * Create a hybrid using weighted 
     * @param models
     * @param weights
     * @return
     */
    public static CompareAssociations generateHybrid(CompareAssociations[] models, double[] weights) {
        CompareAssociations newModel = new CompareAssociations(models[0].xAxis,models[0].yAxis,new double[models[0].values.length][models[0].values[0].length]);
        
        /*
        for(int x=0;x<newModel.values.length;x++) {
            for(int y=0;y<newModel.values[x].length;y++) {
                newModel.values[x][y] = 1;
            }
        }
        
        for(int i=0;i<models.length;i++) {
            for(int x=0;x<newModel.values.length;x++) {
                for(int y=0;y<newModel.values[x].length;y++) {
                    //System.out.println(x + " " + y + " " + models[i].values[x][y] + " " + weights[i] + " " + Math.pow(models[i].values[x][y], weights[i]));
                    newModel.values[x][y] *= Math.pow(Math.max(0, models[i].values[x][y]), weights[i]);
                }   
            }
        }
        */
        
        for(int x=0;x<newModel.values.length;x++) {
            for(int y=0;y<newModel.values[x].length;y++) {
                newModel.values[x][y] = 0;
            }
        }
        
        for(int i=0;i<models.length;i++) {
            for(int x=0;x<newModel.values.length;x++) {
                for(int y=0;y<newModel.values[x].length;y++) {
                    //System.out.println(x + " " + y + " " + models[i].values[x][y] + " " + weights[i] + " " + Math.pow(models[i].values[x][y], weights[i]));
                    newModel.values[x][y] += Math.max(0, models[i].values[x][y]) * weights[i];
                }   
            }
        }
        
        return newModel;
    }
    
    /**
     * Save data out to a file in csv format.
     * @param file
     */
    public void saveData(File file) {
        try {
            BufferedWriter w = new BufferedWriter(new FileWriter(file));
            for(int i=0;i<xAxis.length;i++) {
                w.write("," + xAxis[i]);
            }
            w.write("\n");
            
            for(int i=0;i<yAxis.length;i++) {
                w.write(yAxis[i]);
                for(int j=0;j<xAxis.length;j++) {
                    w.write("," + values[i][j]);
                }
                w.write("\n");
            }
            w.close();
            
        } catch(IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Save confusion plot as image.
     * @param file
     */
    public void saveGraph(File file) {
        BufferedImage image = new BufferedImage(800,800,BufferedImage.TYPE_INT_RGB);
        Graphics g = image.getGraphics();
        Stats.showPredictionMatrix(g, new Rectangle(80,80,image.getWidth()-90,image.getHeight()-120), yAxis, xAxis, values, goldStandard);
        try {
            ImageIO.write(image, "png", file);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    /**
     * Create a copy of the association.
     */
    public CompareAssociations clone() {

        String[] xAxis = new String[this.xAxis.length];
        String[] yAxis = new String[this.yAxis.length];
        double[][] values = new double[this.values.length][this.values[0].length];

        for(int i=0;i<xAxis.length;i++) {
            xAxis[i] = this.xAxis[i];
        }
        
        for(int i=0;i<yAxis.length;i++) {
            yAxis[i] = this.yAxis[i];
        }
        
        for(int i=0;i<values.length;i++) {
            for(int j=0;j<values[i].length;j++) {
                values[i][j] = this.values[i][j];
            }
        }
        
        return new CompareAssociations(xAxis,yAxis,values);
    }
    
    /**
     * Show confusion plot in new frame.
     * @param title
     */
    public void showGraph(String title) {
        
        JFrame frame = new JFrame(title);
        frame.setSize(400, 400);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.add(new JPanel() {
            private static final long serialVersionUID = 1L;
            public void paintComponent(Graphics g) {
                super.paintComponent(g);
                Stats.showPredictionMatrix(g, new Rectangle(50,50,getWidth()-60,getHeight()-80), yAxis, xAxis, values, goldStandard);
            }
        });
        frame.setVisible(true);
    }
    
    /**
     * Show the top Y value for each X balue.
     */
    public void showBest() {
        for(int i=0;i<yAxis.length;i++) {
            double max = Integer.MIN_VALUE;
            String maxStr = xAxis[0];
            for(int j=0;j<xAxis.length;j++) {
                if(values[i][j] > max) {
                    max = values[i][j];
                    maxStr = xAxis[j];
                }
            }
            System.out.println(yAxis[i] + " " + maxStr);
        }
    }
    
    /**
     * Create a binary filter over the values.
     * To do this, assign one Y value to each X value by taking the maximum.
     */
    public void filterTo1() {
        for(int i=0;i<yAxis.length;i++) {
            
            // Find max.
            double max = values[i][0];
            int maxIndex = 0;
            for(int j=0;j<xAxis.length;j++) {
                if(values[i][j] > max) {
                    // New max.
                    max = values[i][j];
                    maxIndex = j;
                }
                
                // Make all 0.
                values[i][j] = 0;
            }
            
            // Make max 1.
            values[i][maxIndex] = 1;
        }
    }
    
    /**
     * Get the most probably object for the given word.
     * @param word
     * @return
     */
    public int getBestObject(int word) {
            
        // Find max.
        double max = values[word][0];
        int maxIndex = 0;
        for(int j=0;j<xAxis.length;j++) {
            if(values[word][j] > max) {
                // New max.
                max = values[word][j];
                maxIndex = j;
            }
        }

        return maxIndex;
    }
    
    /**
     * Applies a power boost to each value. This exaggerates higher values for power > 1 and deemphasizes for power < 1.
     * @param power
     */
    public void boost(double power) {
        for(int i=0;i<values.length;i++) {
            for(int j=0;j<values[i].length;j++) {
                values[i][j] = Math.pow(values[i][j], power);
            }
        }
    }
    
    /**
     * Invert the similarity of this comparison.
     * Adds 100 to attempt to keep everything positive.
     * Could be done better (ex find max)
     */
    public void invert() {
        for(int i=0;i<values.length;i++) {
            for(int j=0;j<values[i].length;j++) {
                values[i][j] = 100-values[i][j];
            }
        }
    }
    
    /**
     * Set this comparison equal to the average of this and another comparison.
     * @param other
     */
    public void average(CompareAssociations other) {
        for(int i=0;i<values.length;i++) {
            for(int j=0;j<values[i].length;j++) {
                values[i][j] = (values[i][j] + other.values[i][j]) / 2;
            }
        }
    }

    /**
     * Initialize with all required parameters.
     * @param xAxis
     * @param yAxis
     * @param values
     */
    public CompareAssociations(String[] yAxis, String[] xAxis, double[][] values) {
        this.xAxis = xAxis;
        this.xAxis_2 = new ArrayList<String>();
        for (String word : xAxis){
            this.xAxis_2.add(word);
        }
        this.yAxis = yAxis;
        this.yAxis_2 = new ArrayList<String>();
        for (String word : yAxis){
            this.yAxis_2.add(word);
        }
        this.values = values;
        
    }
    
    public CompareAssociations(String[] yAxis, String[] xAxis, double[][] values, HashMap<String, String> gold){
        this.goldStandard = gold;
        
        this.xAxis = xAxis;
        this.xAxis_2 = new ArrayList<String>();
        for (String word : xAxis){
            this.xAxis_2.add(word);
        }
        this.yAxis = yAxis;
        this.yAxis_2 = new ArrayList<String>();
        for (String word : yAxis){
            this.yAxis_2.add(word);
        }
        this.values = values;
        
    }
    
    
    


    public void readGoldStandard(String gFile){
        this.goldStandard = new HashMap<String, String>();
        
        ArrayList<String> lines = IOUtils.readFile(gFile);
        for (String line:lines){
            String[] els = line.split("[\t| ]+");
            this.goldStandard.put(els[0], els[1]);
        }
       
    }
    /**
     * Initialize from a formatted file.
     * @param f
     */
    public CompareAssociations(File f) {
        try {
            BufferedReader r = new BufferedReader(new FileReader(f));
            String line = r.readLine();

            // Read first line which should contain xAxis names.
            String[] parts = line.split(",");
            xAxis = new String[parts.length-1];
            for(int i=0;i<xAxis.length;i++) {
                xAxis[i] = parts[i+1];
            }

            // Store each yAxis line because we don't know how many there are.
            Vector<String> yAxis = new Vector<String>();
            Vector<double[]> values = new Vector<double[]>();
            while((line = r.readLine()) != null) {
                parts = line.split(",");
                yAxis.add(parts[0]);
                
                // Read a line of data and store it.
                double[] lineData = new double[parts.length-1];
                for(int i=0;i<xAxis.length;i++) {
                    lineData[i] = Double.parseDouble(parts[i+1]);
                }
                values.add(lineData);
            }
            
            // Convert vectors to arrays.
            this.yAxis = yAxis.toArray(new String[0]);
            this.values = new double[values.size()][];
            for(int i=0;i<values.size();i++) {
                this.values[i] = values.get(i);
            }
            
            r.close();
        } catch(IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Transpose matrix. This swaps the x and y axis.
     */
    public void transpose() {
        String[] tmp = xAxis;
        xAxis = yAxis;
        yAxis = tmp;
        
        double[][] tmp2 = new double[values[0].length][values.length];
        for(int x=0;x<values.length;x++) {
            for(int y=0;y<values[x].length;y++) {
                tmp2[y][x] = values[x][y];
            }   
        }
        values = tmp2;
    }
    
    /**
     * Print results to screen.
     */
    public void show() {
        
        // Show x axis.
        for(String s : xAxis) {
            System.out.print("," + s);
        }
        System.out.println();
        
        // Show y axis and values.
        for(int i=0;i<values.length;i++) {
            System.out.print(yAxis[i]);
            for(int j=0;j<values[i].length;j++) {
                System.out.print("," + values[i][j]);
            }
            System.out.println();
        }
    }
    
    /**
     * Sort the x axis based on another CompareAssociations x axis.
     * @param other
     */
    public void orderAs(CompareAssociations other) {
        
        // Can only compare to a CompareAssociations that has the same dimensionality.
        if(other.xAxis.length != xAxis.length || other.yAxis.length != yAxis.length) {
            
            // Show mismatches.
            System.out.println("Non-Matching size. [" + other.xAxis.length + " " + xAxis.length  + "," + other.yAxis.length + " " + yAxis.length + "]");
            System.out.println("X");
            showMismatch(other.xAxis, xAxis);           
            System.out.println("Y");
            showMismatch(other.yAxis, yAxis);   
            return;
        }
        
        // Create string by index list for x axis.
        Hashtable<String,Integer> index = new Hashtable<String,Integer>();
        for(int i=0;i<other.xAxis.length;i++) {
            index.put(other.xAxis[i], i);
        }
        
        // Sort the x axis of this list according to the x axis of the other list.
        String[] newXAxis = new String[xAxis.length];       
        double[][] newValues = new double[values.length][values[0].length];
        for(int i=0;i<xAxis.length;i++) {
            newXAxis[index.get(xAxis[i])] = xAxis[i];
        }
        
        // Perform the sort on the columns.
        for(int i=0;i<values.length;i++) {
            for(int j=0;j<values[i].length;j++) {
                newValues[i][index.get(xAxis[j])] = values[i][j];   
            }
        }       
        
        xAxis = newXAxis;
        values = newValues;     
    }
    
    /**
     * Prints to display items in list 1 that are not in list 2 and items that are in list 2 that are not in list 1.
     * @param a
     * @param b
     */
    public void showMismatch(String[] a, String[] b) {
        HashSet<String> c = new HashSet<String>();
        for(String s : a) {
            c.add(s);
        }
        for(String s : b) {
            if(!c.remove(s)) {
                System.out.println("1 not containing " + s);
            }
        }
        for(String s : c) {
            System.out.println("2 not containing " + s);
        }
    }
    
    /**
     * Normalize the rows of the matrix to 1.
     */
    public void normalize() {
        for(int i=0;i<values.length;i++) {
            double sum = 0;
            for(int j=0;j<values[i].length;j++) {
                sum += values[i][j];
            }
            for(int j=0;j<values[i].length;j++) {
                values[i][j] = values[i][j] / sum;
            }
        }
    }
    
    public void normalize2() {
        for(int i=0;i<values.length;i++) {
            double min = values[i][0];
            double max = values[i][0];
            for(int j=0;j<values[i].length;j++) {
                min = Math.min(min, values[i][j]);
                max = Math.max(max, values[i][j]);
            }
            for(int j=0;j<values[i].length;j++) {
                values[i][j] = (values[i][j] - min) / (max - min);
            }
        }
    }
    
    /**
     * Perform scalling on the similarity matrix.
     * @param val
     */
    public void multiply(double val) {
        for(int i=0;i<values.length;i++) {
            for(int j=0;j<values[i].length;j++) {
                values[i][j] *= val;
            }
        }
    }
    
}