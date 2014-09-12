package common;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.RenderingHints;
import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.IOException;

import javax.swing.JFrame;
import javax.swing.JPanel;

import org.ejml.simple.SimpleMatrix;

import de.erichseifert.gral.plots.colors.HeatMap;

public class HeatMapPanel extends JPanel
{
    /**
     * 
     */
    
    protected int matWidth;
    protected int matHeight;
    protected HeatMap colorMap;
    protected SimpleMatrix data;
    
    private static final long serialVersionUID = 1L;

    public HeatMapPanel(SimpleMatrix data)
    {
        this.data = data;
        this.matHeight = data.numRows();
        this.matWidth = data.numCols();
        
        colorMap = new SimpleHeatMap();
        double max = SimpleMatrixUtils.elementMax(data);
        double min = SimpleMatrixUtils.elementMin(data);
        colorMap.setRange(min, max);
        
    }
 
    protected void paintComponent(Graphics g)
    {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D)g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                            RenderingHints.VALUE_ANTIALIAS_ON);
        int w = getWidth();
        int h = getHeight();
        if (w == 0 || h == 0) return;
        double cellWidth = ((double) w) / matWidth;
        double cellHeight = ((double) h) / matHeight;
        for (int i = 0; i < matWidth; i++) {
            for (int j = 0; j < matHeight; j++) {
                int x = (int) Math.round(i * cellWidth);
                int y = (int) Math.round(j * cellHeight);
                int deltaX = (int) Math.round((i + 1) * cellWidth);
                int deltaY = (int) Math.round((j + 1) * cellHeight);
                double value = data.get(j, i);
                Paint paint = colorMap.get(value);
                g2.setPaint(paint);
                g2.fillRect(x, y, deltaX - x, deltaY - y);
            }
        }
    }
 
    public static void main(String[] args) throws IOException
    {
        JFrame f = new JFrame();
        f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        String dataFile = "/home/thenghiapham/work/project/mikolov/output/phrase1.comp.csv";
        BufferedInputStream inputStream = new BufferedInputStream(new FileInputStream(dataFile));
        SimpleMatrix data = new SimpleMatrix(IOUtils.readMatrix(inputStream, false));
        inputStream.close();
        f.getContentPane().add(new HeatMapPanel(data));
        f.setSize(data.numCols() * 10, data.numRows() * 10);
        f.setLocation(200,200);
        f.setVisible(true);
    }
    
    private class SimpleHeatMap extends HeatMap {
        /**
         * 
         */
        private static final long serialVersionUID = 1L;

        public Paint get(double value) 
        {
            value = scale(value);
            int NUM_COLORS = 4;
            float[][] color = {{0,0,1}, {0,1,0}, {1,1,0}, {1,0,0} };
            // A static array of 4 colors:  (blue,   green,  yellow,  red) using {r,g,b} for each.
         
            int idx1;        // |-- Our desired color will be between these two indexes in "color".
            int idx2;        // |
            float fractBetween = 0;  // Fraction between "idx1" and "idx2" where our value is.
         
            if (value <= 0)      {  idx1 = idx2 = 0;            }    // accounts for an input <=0
            else if(value >= 1)  {  idx1 = idx2 = NUM_COLORS-1; }    // accounts for an input >=0
            else
            {
                value = value * (NUM_COLORS-1);        // Will multiply value by 3.
                idx1  = (int) Math.floor(value);                  // Our desired color will be after this index.
                idx2  = idx1+1;                        // ... and before this index (inclusive).
                fractBetween = (float) value - idx1;    // Distance between the two indexes (0-1).
            }
         
            float red   = (color[idx2][0] - color[idx1][0])*fractBetween + color[idx1][0];
            float green = (color[idx2][1] - color[idx1][1])*fractBetween + color[idx1][1];
            float blue  = (color[idx2][2] - color[idx1][2])*fractBetween + color[idx1][2];
            return new Color(red, green, blue);
        }
    }
}