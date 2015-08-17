package demo;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;

import org.ejml.simple.SimpleMatrix;

import common.IOUtils;
import common.SimpleMatrixUtils;

public class FromBinToTxt {

    /**
     * @param args
     * @throws FileNotFoundException 
     */
    public static void main(String[] args) throws FileNotFoundException {
        String mapFile = "/home/aggeliki/sas/visLang/mmskipgram/out/fast-mapping/hs/cnn_svd/out_3b_z_n5_m0.5_5_r11.0_r220.0l1.0E-4.mapping";

        
        SimpleMatrix mapping = new SimpleMatrix(IOUtils.readMatrix(new BufferedInputStream(new FileInputStream(mapFile)), false));
        
        IOUtils.saveMatrix(mapFile+".txt", SimpleMatrixUtils.to2DArray(mapping), false);
        
}
}
