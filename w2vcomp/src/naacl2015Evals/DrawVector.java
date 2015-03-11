package naacl2015Evals;

import java.awt.Color;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import javax.swing.JFrame;

import org.ejml.simple.SimpleMatrix;

import space.SemanticSpace;
import utils.LogPlotting;

import common.IOUtils;
import common.PlotUtils;
import common.SimpleMatrixUtils;

import de.erichseifert.gral.plots.XYPlot;
import de.erichseifert.gral.ui.InteractivePanel;
import demo.TestConstants;

public class DrawVector extends JFrame {

    public DrawVector() throws FileNotFoundException {
    ArrayList<ArrayList<Double>> data = new ArrayList<>();
    ArrayList<String> labels = new ArrayList<>();
    
    //SemanticSpace space = SemanticSpace.readSpace("/home/angeliki/Documents/mikolov_composition/out/multimodal/NAACL/cnn/out_wiki_n5_m0.5_5_r11.0_r220.0l1.0E-4.bin")
    SemanticSpace space = SemanticSpace.readSpace("/home/angeliki/Documents/mikolov_composition/out/multimodal/NAACL/cnn_again_mm/out_wiki_n10_m0.5_10_r11.0_r21.0l1.0E-6.bin");
    //String mapFile = "/home/angeliki/Documents/mikolov_composition/out/multimodal/NAACL/cnn/out_wiki_n5_m0.5_5_r11.0_r220.0l1.0E-4.mapping";
    //SimpleMatrix map = new SimpleMatrix(IOUtils.readMatrix(new BufferedInputStream(new FileInputStream(mapFile)), false));
    
   
    Set<String> toMap = new HashSet<String>();
    //String[] asbtract = {"moral", "hope","thought"};
    String[] asbtract = {"hope"};
    Set<String> a = new HashSet<String>();
    for (String word:asbtract){
        a.add(word);
    }
    toMap.addAll(a);
    
    Set<String> c = new HashSet<String>();
    //String[] concrete = {"meat","girl","polic"};
    String[] concrete = {"meat"};
    for (String word:concrete){
        c.add(word);
    }
    toMap.addAll(c) ;
    //SemanticSpace wordSpaceMapped = new SemanticSpace(space.getSubSpace(toMap).getWords(), SimpleMatrixUtils.to2DArray((new SimpleMatrix(space.getSubSpace(toMap).getVectors())).mult(map)));
    SemanticSpace wordSpaceMapped = space;
    ArrayList<Color> colors = new ArrayList<Color>();
    for (String word: toMap) {
        
        ArrayList<Double> vec = new ArrayList<Double>();
        double[] vec_simple = wordSpaceMapped.getVector(word);
        SimpleMatrix u = new SimpleMatrix(1,vec_simple.length,false,vec_simple);
        double normF = u.normF();
        for (double val: wordSpaceMapped.getVector(word)){
            vec.add(val/normF);
        }
        data.add(vec);
        
        labels.add(word);
        if (a.contains(word)){
            colors.add(new Color((float) 0.5,(float) 0.0,(float) 0.0));
        }
        else{
            colors.add(new Color((float) 0.0,(float) 0.0,(float) 0.5));
        }
    }
    
    XYPlot plot = PlotUtils.createTimePlot(data, labels,colors);
    
    getContentPane().add(new InteractivePanel(plot));
    
    
    setDefaultCloseOperation(EXIT_ON_CLOSE);
    setSize(900, 700);
}

public static void main(String args[]) throws FileNotFoundException {
    DrawVector plotFrame = new DrawVector();
    plotFrame.setVisible(true);
}

}
