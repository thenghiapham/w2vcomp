package utils;

import java.awt.BorderLayout;

import javax.swing.JFrame;

import de.erichseifert.gral.data.DataSeries;
import de.erichseifert.gral.data.DataTable;
import de.erichseifert.gral.plots.BoxPlot;
import de.erichseifert.gral.plots.XYPlot;
import de.erichseifert.gral.ui.InteractivePanel;

public class BarPlotting extends JFrame {
    public BarPlotting() {
        // Create data
        DataTable data = new DataTable(Double.class, Double.class);

        final int POINT_COUNT = 10;
        java.util.Random rand = new java.util.Random();
        for (int i = 0; i < POINT_COUNT; i++) {
            double x = i;
            double y1 = rand.nextGaussian();
            data.add(x, y1);
        }

        // Create series
        DataSeries series1 = new DataSeries("Series 1", data, 0, 1);
        BoxPlot plot = new BoxPlot(BoxPlot.createBoxData(series1));
//        XYPlot plot = new XYPlot(series1);
        

        plot.getAxis("y").setMin(-2);
        plot.getAxis("y").setMax(2);
        // Display on screen
        getContentPane().add(new InteractivePanel(plot), BorderLayout.CENTER);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setMinimumSize(getContentPane().getMinimumSize());
        setSize(504, 327);
    }

    public static void main(String[] args) {
        BarPlotting df = new BarPlotting();
        df.setVisible(true);
    }
}