package unitTest;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

import common.correlation.MenCorrelation;

public class MenTest {
    MenCorrelation men;
    
    @Before
    public void setUp() throws Exception {
        men = new MenCorrelation("/home/thenghiapham/work/project/mikolov/men/subMen.txt");
    }

    @Test
    public void testOne() {
        assertEquals(men.pearsonCorrelation(men.getGolds()), 1.0, 1e-10);
    }

}
