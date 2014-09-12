package common;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.Polygon;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.Arc2D;
import java.awt.geom.CubicCurve2D;
import java.awt.geom.Ellipse2D;
import java.awt.geom.QuadCurve2D;

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
        
        colorMap = new HeatMap();
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
                double value = data.get(i, j);
                Paint paint = colorMap.get(value);
                
            }
        }
        x1 = w/3;
        y1 = h;
        double ctrlx1 = w/4;
        double ctrly1 = h/2;
        double ctrlx2 = w/2;
        double ctrly2 = h*2/3;
        x2 = w;
        y2 = h*3/4;
        CubicCurve2D zone4 = new CubicCurve2D.Double(x1, y1, ctrlx1, ctrly1,
                                                     ctrlx2, ctrly2, x2, y2);
        g2.setPaint(new Color(180,150,100));
        g2.fill(zone4);
        xps = new int[] { (int)x1, (int)x2, w };
        yps = new int[] { (int)y1, (int)y2, h };
        p = new Polygon(xps, yps, 3);
        g2.fill(p);
    }
 
    public static void main(String[] args)
    {
        JFrame f = new JFrame();
        f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        f.getContentPane().add(new HeatMapPanel(null));
        f.setSize(400,400);
        f.setLocation(200,200);
        f.setVisible(true);
    }
}