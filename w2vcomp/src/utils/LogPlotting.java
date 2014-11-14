package utils;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;

import javax.swing.JFrame;

import common.IOUtils;
import common.PlotUtils;

import de.erichseifert.gral.plots.XYPlot;
import de.erichseifert.gral.ui.InteractivePanel;
import demo.TestConstants;

public class LogPlotting extends JFrame {
    /**
     * 
     */
    private static final long serialVersionUID = 1L;
    public ArrayList<Double> smooth(ArrayList<Double> input, int numAverage) {
        ArrayList<Double> result= new ArrayList<Double>();
        result.add(input.get(0));
        int size = input.size();
        for (int i = 1; i < size; i++) {
            double sum = 0;
            int num = 0;
            for (int j = 1 - numAverage ; j < numAverage; j++) {
                if (j + i <= 0) continue;
                if (j + i >= size) break;
                sum += input.get(i + j);
                num++;
            }
            result.add(sum/num);
        }
        return result;
    }
    public LogPlotting(String logDirPath) {
        
        ArrayList<ArrayList<Double>> data = new ArrayList<>();
        ArrayList<String> labels = new ArrayList<>();
        File logDir = new File(logDirPath);
        String[] fileNames = logDir.list();
        Arrays.sort(fileNames);
        for (String fileName : fileNames) {
            
            System.out.println(fileName);
            if (!fileName.endsWith("log") || !fileName.contains("bnc")) continue;
            
            String filePath = logDirPath + "/" + fileName;
            ArrayList<Double> men = IOUtils.readLog(filePath, "men");
            ArrayList<Double> sick = IOUtils.readLog(filePath, "SICK");
            ArrayList<Double> smoothedMen = smooth(men, 20);
            ArrayList<Double> smoothedSick = smooth(sick, 20);
            data.add(smoothedMen);
            labels.add("men");
            data.add(smoothedSick);
            labels.add("sick");
        }
        XYPlot plot = PlotUtils.createTimePlot(data, labels);
        
        getContentPane().add(new InteractivePanel(plot));
        
        
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(900, 700);
    }
    
    public static void main(String args[]) {
        LogPlotting plotFrame = new LogPlotting(TestConstants.S_LOG_DIR);
        plotFrame.setVisible(true);
    }
    
}
