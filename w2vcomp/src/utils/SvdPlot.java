package utils;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Rectangle2D;
import java.io.*;
import java.util.Random;

import javax.swing.JFrame;

import de.erichseifert.gral.data.*;
import de.erichseifert.gral.data.comparators.Ascending;
import de.erichseifert.gral.plots.XYPlot;
import de.erichseifert.gral.plots.lines.DefaultLineRenderer2D;
import de.erichseifert.gral.plots.points.DefaultPointRenderer2D;
import de.erichseifert.gral.plots.points.PointRenderer;
import de.erichseifert.gral.ui.InteractivePanel;
import de.erichseifert.gral.util.Insets2D;

public class SvdPlot extends JFrame {

    /**
     * 
     */
    private static final long serialVersionUID = 1L;
    
    public DataSource createRandomSetNearLine(double alpha, double error, int seriesSize) {
        Random random = new Random();
        @SuppressWarnings("unchecked")
        DataTable table = new DataTable(Double.class, Double.class);
        for (int j = 0; j < seriesSize; j++) {
            double x = random.nextDouble() * 2 - 1;
            double y = (x * alpha) + (random.nextGaussian() * error);
            table.add(x, y);
        }
        return table;
    }
    
    public DataSource createLine(double alpha, double offset) {
        @SuppressWarnings("unchecked")
        DataTable table = new DataTable(Double.class, Double.class);
        int seriesSize = 100;
        for (int j = - seriesSize; j <= seriesSize; j++) {
            double x = j / (double) seriesSize;
            double y = x * alpha + offset;
            table.add(x, y);
        }
        return table;
    }
    
    public DataSource createVerticalLine(double offset) {
        @SuppressWarnings("unchecked")
        DataTable table = new DataTable(Double.class, Double.class);
        int seriesSize = 100;
        for (int j = - seriesSize; j <= seriesSize; j++) {
            double x = offset;
            double y = j / (double) seriesSize;
            table.add(x, y);
        }
        return table;
    }
    
    @SuppressWarnings("unchecked")
    public DataTable[] getExample(Double x, Double y) {
        Double x1 = (x + y) / 2;
        Double y1 = x1;
        
        Double x2 = x1;
        Double y2 = 0.0;
        
        Double x3 = 0.0;
        Double y3 = y1;
        
        DataTable approxLine = new DataTable(Double.class, Double.class);
        approxLine.add(x, y);
        approxLine.add(x1, y1);
        
        DataTable projLine1 = new DataTable(Double.class, Double.class);
        projLine1.add(x1, y1);
        projLine1.add(x2, y2);
        
        DataTable projLine2 = new DataTable(Double.class, Double.class);
        projLine2.add(x1, y1);
        projLine2.add(x3, y3);
        
        return new DataTable[]{approxLine, projLine1, projLine2};
    }

    @SuppressWarnings("unchecked")
    public SvdPlot() throws IOException {
        DataSource source = createRandomSetNearLine(1, 0.05, 100);
        DataTable randomPoints = new DataTable(source);
        randomPoints.sort(new Ascending(0));
        DataSource line = createLine(1, 0);
        
        Double x1 = -0.5;
        Double y1 = -0.6;
        
        DataTable[] exLines1 = getExample(x1, y1);
        DataTable approxLine1 = exLines1[0];
        DataTable projLine11 = exLines1[1];
        DataTable projLine12 = exLines1[2];
        
        Double x2 = 0.8;
        Double y2 = 0.8;
        
        DataTable[] exLines2 = getExample(x2, y2);
        DataTable approxLine2 = exLines2[0];
        DataTable projLine21 = exLines2[1];
        DataTable projLine22 = exLines2[2];
        
        Double x3 = 0.8;
        Double y3 = 0.0;
        
        DataTable[] exLines3 = getExample(x3, y3);
        DataTable approxLine3 = exLines3[0];
        DataTable projLine31 = exLines3[1];
        DataTable projLine32 = exLines3[2];
        
        DataTable examplePoint1 = new DataTable(Double.class, Double.class);
        examplePoint1.add(x1,y1);
        examplePoint1.add(x1,y1);
        
        DataTable examplePoint2 = new DataTable(Double.class, Double.class);
        examplePoint2.add(x2,y2);
        examplePoint2.add(x2,y2);
        
        DataTable examplePoint3 = new DataTable(Double.class, Double.class);
        examplePoint3.add(x3,y3);
        examplePoint3.add(x3,y3);
        
        
        XYPlot plot = new XYPlot(randomPoints, line, approxLine1, 
                projLine11, projLine12, approxLine2, 
                projLine21, projLine22, approxLine3, 
                projLine31, projLine32, examplePoint1, 
                examplePoint2, examplePoint3);
              

        plot.setPointRenderer(line, null);
        DefaultLineRenderer2D lineRenderer = new DefaultLineRenderer2D();
        lineRenderer.setColor(Color.BLUE);
        plot.setLineRenderer(line, lineRenderer);
        
        Color red = Color.RED;
        PointRenderer examplePointRdr1 = new DefaultPointRenderer2D();
        DefaultLineRenderer2D exampleLineRdr1 = new DefaultLineRenderer2D();
        exampleLineRdr1.setColor(red);
        examplePointRdr1.setColor(red);
        examplePointRdr1.setShape(new Rectangle2D.Double(-1, -1, 2.0, 2.0));
        examplePointRdr1.setValueVisible(true);
        
        plot.setPointRenderer(approxLine1, examplePointRdr1);
        plot.setPointRenderer(projLine11, examplePointRdr1);
        plot.setPointRenderer(projLine12, examplePointRdr1);
        
        plot.setLineRenderer(approxLine1, exampleLineRdr1);
        plot.setLineRenderer(projLine11, exampleLineRdr1);
        plot.setLineRenderer(projLine12, exampleLineRdr1);
        
        
        Color green = Color.GREEN;
        PointRenderer examplePointRdr2 = new DefaultPointRenderer2D();
        DefaultLineRenderer2D exampleLineRdr2 = new DefaultLineRenderer2D();
        exampleLineRdr2.setColor(green);
        examplePointRdr2.setColor(green);
        examplePointRdr2.setShape(new Rectangle2D.Double(-1, -1, 2.0, 2.0));
        examplePointRdr2.setValueVisible(true);
        
        plot.setPointRenderer(approxLine2, examplePointRdr2);
        plot.setPointRenderer(projLine21, examplePointRdr2);
        plot.setPointRenderer(projLine22, examplePointRdr2);
        
        plot.setLineRenderer(approxLine2, exampleLineRdr2);
        plot.setLineRenderer(projLine21, exampleLineRdr2);
        plot.setLineRenderer(projLine22, exampleLineRdr2);
        
        Color black = Color.BLACK;
        PointRenderer examplePointRdr3 = new DefaultPointRenderer2D();
        DefaultLineRenderer2D exampleLineRdr3 = new DefaultLineRenderer2D();
        exampleLineRdr3.setColor(black);
        examplePointRdr3.setColor(black);
        examplePointRdr3.setShape(new Rectangle2D.Double(-1, -1, 2.0, 2.0));
        examplePointRdr3.setValueVisible(true);
        
        plot.setPointRenderer(approxLine3, examplePointRdr3);
        plot.setPointRenderer(projLine31, examplePointRdr3);
        plot.setPointRenderer(projLine32, examplePointRdr3);
        
        plot.setLineRenderer(approxLine3, exampleLineRdr3);
        plot.setLineRenderer(projLine31, exampleLineRdr3);
        plot.setLineRenderer(projLine32, exampleLineRdr3);
        
        PointRenderer pointExampleRnd1 = new DefaultPointRenderer2D();
        pointExampleRnd1.setShape(new Ellipse2D.Double(-5, -5, 10, 10));
        pointExampleRnd1.setColor(red);
        plot.setPointRenderer(examplePoint1, pointExampleRnd1);
        
        PointRenderer pointExampleRnd2 = new DefaultPointRenderer2D();
        pointExampleRnd2.setShape(new Ellipse2D.Double(-5, -5, 10, 10));
        pointExampleRnd2.setColor(green);
        plot.setPointRenderer(examplePoint2, pointExampleRnd2);
        
        PointRenderer pointExampleRnd3 = new DefaultPointRenderer2D();
        pointExampleRnd3.setShape(new Ellipse2D.Double(-5, -5, 10, 10));
        pointExampleRnd3.setColor(black);
        plot.setPointRenderer(examplePoint3, pointExampleRnd3);

        plot.setInsets(new Insets2D.Double(20.0, 50.0, 40.0, 20.0));
        getContentPane().add(new InteractivePanel(plot), BorderLayout.CENTER);

        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setMinimumSize(getContentPane().getMinimumSize());
        setSize(600, 600);
    }



    public static void main(String[] args) throws IOException {
        SvdPlot df = new SvdPlot();
        df.setVisible(true);
    }
}