package unitTest;

import static org.junit.Assert.*;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Random;

import org.ejml.simple.SimpleMatrix;
import org.junit.Before;
import org.junit.Test;

import common.IOUtils;
import common.SimpleMatrixUtils;

public class IOUtilsTest {

    @Before
    public void setUp() throws Exception {
    }

    @Test
    public void testSaveLoadMatrix() {
        try {
            File file = File.createTempFile("blah", "blah");
            String filePath = file.getAbsolutePath();
            Random rand = new Random();
            SimpleMatrix matrix = SimpleMatrix.random(4, 4, -100, 100, rand);
            System.out.println(matrix);
            double[][] originalMatrix = SimpleMatrixUtils.to2DArray(matrix);
            IOUtils.saveMatrix(filePath, originalMatrix, true);
            BufferedInputStream inputStream = new BufferedInputStream(new FileInputStream(filePath));
            double[][] readMatrix = IOUtils.readMatrix(inputStream, true);
            System.out.println(new SimpleMatrix(readMatrix));
            inputStream.close();
            for (int i = 0; i < 4; i++) {
                assertArrayEquals(originalMatrix[i], readMatrix[i], 1e-10);
            }
            
        } catch (IOException e) {
            e.printStackTrace();
            fail();
        }
    }

}
