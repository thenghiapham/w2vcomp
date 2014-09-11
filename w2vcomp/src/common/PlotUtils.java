package common;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Random;

import de.erichseifert.gral.data.DataSeries;
import de.erichseifert.gral.data.DataTable;
import de.erichseifert.gral.data.EnumeratedData;
import de.erichseifert.gral.plots.XYPlot;
import de.erichseifert.gral.plots.lines.DefaultLineRenderer2D;
import de.erichseifert.gral.plots.lines.LineRenderer;

public class PlotUtils {
    private static final Random RANDOM = new Random();
    public static XYPlot createTimePlot(ArrayList<ArrayList<Double>> data, ArrayList<String> label) {
        DataSeries[] dataSeries = createDataTable(data, label);
        XYPlot plot = new XYPlot(dataSeries);
        for (DataSeries series: dataSeries) {
            LineRenderer lines = new DefaultLineRenderer2D();
            plot.setLineRenderer(series, lines);
          
            Color color = randomColor();
            plot.getPointRenderer(series).setColor(color);
            plot.getPointRenderer(series).setShape(null);
            plot.getLineRenderer(series).setColor(color);
        }
        plot.setLegendVisible(true);
        return plot;
    }
    
    public static DataSeries[] createDataTable(ArrayList<ArrayList<Double>> data, ArrayList<String> label) {
        int size = data.size();
        int seriesSize = -1;
        for (ArrayList<Double> series : data) {
            if (seriesSize == -1) seriesSize = series.size();
            if (seriesSize != series.size()) return null;
        }
        DataSeries[] result = new DataSeries[size];
        for (int i = 0; i < size; i++) {
            ArrayList<Double> series = data.get(i);
            @SuppressWarnings("unchecked")
            DataTable table = new DataTable(Double.class);
            for (int j = 0; j < seriesSize; j++) {
                table.add(series.get(j));
            }
            DataSeries dataSeries = new DataSeries(label.get(i), new EnumeratedData(table), 0,1);
            result[i] = dataSeries;
        }
        return result;
    }
    
    public static Color randomColor() {
        float r = RANDOM.nextFloat();
        float g = RANDOM.nextFloat();
        float b = RANDOM.nextFloat();
        return new Color(r, g, b);
    }
}
