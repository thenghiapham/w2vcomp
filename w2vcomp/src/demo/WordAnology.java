package demo;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import org.ejml.simple.SimpleMatrix;

import space.Neighbor;
import space.RealVector;
import space.RawSemanticSpace;

import demo.TestConstants;

public class WordAnology {
    public static void main(String[] args) {
        //String vectorFile = TestConstants.GZIP_VECTOR_FILE;
//        String vectorFile = TestConstants.VECTOR_FILE;
        String vectorFile = TestConstants.CCG_VECTOR_FILE;
        RawSemanticSpace space = RawSemanticSpace.readSpace(vectorFile);
        System.out.println("Enter 3 words or EXIT to exit");
        try {
            BufferedReader br = new BufferedReader(new InputStreamReader(
                    System.in));
            String input;
            while ((input = br.readLine()) != null) {
                if (input.equals("EXIT")) {
                    break;
                } else {
                    String[] elements = input.split(" ");
                    if (elements.length != 3) {
                        System.out.println("need 3 words");
                    } else {
                        getWordAnology(space, elements[0], elements[1],
                                elements[2]);
                    }
                }
            }

        } catch (IOException io) {
            io.printStackTrace();
        }
    }

    public static String predictWordAnology(RawSemanticSpace space, String word1,
            String word2, String word3) {
        if (!(space.contains(word1) && space.contains(word2) && space
                .contains(word3))) {
            System.out.println("at least one word is not in the space");
            return "";
        }
        double[] v2 = RealVector.norm(space.getVector(word2).getMatrix().getData());
        double[] v1 = RealVector.norm(space.getVector(word1).getMatrix().getData());
        double[] v3 = RealVector.norm(space.getVector(word3).getMatrix().getData());
        SimpleMatrix v4 = new SimpleMatrix(1, v1.length, true, 
                RealVector.add(RealVector.subtract(v2, v1), v3));

        Neighbor[] neighbors;
        neighbors = space.getNeighbors(v4, 20, new String[] { word1, word2,
                word3 });
        for (int i = 0; i < neighbors.length; i++) {
            String neighbor = neighbors[i].word;
            if (!(word1.equals(neighbor) || word2.equals(neighbor) || word3
                    .equals(neighbor)))
                return neighbor;
        }
        return "";
    }

    public static void getWordAnology(RawSemanticSpace space, String word1,
            String word2, String word3) {
        if (!(space.contains(word1) && space.contains(word2) && space
                .contains(word3))) {
            System.out.println("at least one word is not in the space");
            return;
        }
        double[] v2 = RealVector.norm(space.getVector(word2).getMatrix().getData());
        double[] v1 = RealVector.norm(space.getVector(word1).getMatrix().getData());
        double[] v3 = RealVector.norm(space.getVector(word3).getMatrix().getData());
        SimpleMatrix v4 = new SimpleMatrix(1, v1.length, true, 
                RealVector.add(RealVector.subtract(v2, v1), v3));

        Neighbor[] neighbors;
        neighbors = space.getNeighbors(v4, 20, new String[] { word1, word2,
                word3 });
        System.out.println("\"" + word1 + "\" to \"" + word2 + "\" is like \""
                + word3 + "\" to: ");
        for (int i = 0; i < neighbors.length; i++) {
            System.out.println(neighbors[i].word + " " + neighbors[i].sim);
        }
    }
}
