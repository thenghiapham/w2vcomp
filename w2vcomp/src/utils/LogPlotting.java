package utils;

import java.io.File;
import java.util.ArrayList;

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
        for (String fileName : fileNames) {
            String filePath = logDirPath + "/" + fileName;
            String suffix = fileName.split(".")[fileName.split(".").length - 1];
            ArrayList<Double> men = IOUtils.readLog(filePath, "men");
            data.add(men);
            labels.add(suffix);
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
