package utils;

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

    public LogPlotting(String logFile) {
        
        ArrayList<ArrayList<Double>> data = new ArrayList<>();
        ArrayList<String> labels = new ArrayList<>();
        
        ArrayList<Double> men = IOUtils.readLog(TestConstants.S_LOG_FILE, "men");
        data.add(men);
        labels.add("men");
        
        XYPlot plot = PlotUtils.createTimePlot(data, labels);
        
        getContentPane().add(new InteractivePanel(plot));
        
        
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(900, 700);
    }
    
    public static void main(String args[]) {
        LogPlotting plotFrame = new LogPlotting("");
        plotFrame.setVisible(true);
    }
    
}
