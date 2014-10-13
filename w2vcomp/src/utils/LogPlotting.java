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

    public LogPlotting(String logDirPath) {
        
        ArrayList<ArrayList<Double>> data = new ArrayList<>();
        ArrayList<String> labels = new ArrayList<>();
        File logDir = new File(logDirPath);
        String[] fileNames = logDir.list();
        Arrays.sort(fileNames);
        for (String fileName : fileNames) {
            
            
            if (fileName.endsWith("lck") || !fileName.endsWith("log") || !fileName.startsWith("men-en-wiki9")) continue;
            System.out.println(fileName);
            String filePath = logDirPath + "/" + fileName;
            String suffix = fileName.split("log")[fileName.split("log").length - 1];
            ArrayList<Double> men = IOUtils.readLog(filePath, "correlation");
            data.add(men);
            
            labels.add(suffix);
        }
        XYPlot plot = PlotUtils.createTimePlot(data, labels);
        
        getContentPane().add(new InteractivePanel(plot));
        
        
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(900, 700);
    }
    
    public static void main(String args[]) {
        LogPlotting plotFrame = new LogPlotting(TestConstants.LOG_DIR);
        plotFrame.setVisible(true);
    }
    
}