package demo;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import space.Neighbor;
import space.RawSemanticSpace;

public class WordSimilarity {
    public static void main(String[] args) {
        String vectorFile = args[0]; //"/home/thenghiapham/work/project/antonym/output/hs_skip_wiki9_300_anto_train_adj_verb.bin";//TestConstants.VECTOR_FILE;
//        String vectorFile = TestConstants.VECTOR_FILE.replace(".bin", "_anto.bin");
        RawSemanticSpace space = RawSemanticSpace.readSpace(vectorFile);
        space.exportSpace("/home/nghia/cphrase.txt", false);
        System.out.println("Enter a word or EXIT to exit");
        try {
            BufferedReader br = new BufferedReader(new InputStreamReader(
                    System.in));
            String input;
            while ((input = br.readLine()) != null) {
                if (input.equals("EXIT")) {
                    break;
                } else {
                    printNeighbors(space, input);
                }
            }

        } catch (IOException io) {
            io.printStackTrace();
        }
    }

    public static void printNeighbors(RawSemanticSpace space, String word) {
        if (!space.contains(word)) {
            System.out.println(word + " is not in the space");
            return;
        }
        Neighbor[] neighbors = space.getNeighbors(word, 20);
        System.out.println("Nearest neighbors of \"" + word + "\" are: ");
        for (int i = 0; i < neighbors.length; i++) {
            System.out.println(neighbors[i].word + " " + neighbors[i].sim);
        }
    }
}
