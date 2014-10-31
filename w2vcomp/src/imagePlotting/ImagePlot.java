package imagePlotting;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

public class ImagePlot {
    
    
  //Convert to three separate channels
   

    /**
     * @param args
     */
    public static void main(String[] args) {
        BufferedImage img = null;
        try {
            img = ImageIO.read(new File("/home/angeliki?Desktop/me.jpg"));
        } catch (IOException e) {
        }
    }

    
    
   
}
