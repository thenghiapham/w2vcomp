package unitTest;

import static org.junit.Assert.*;

import org.junit.Test;

import vocab.HuffmanTree;

public class HuffmanTest {

    @Test
    public void testSimpleHuffman() {
        long[] inCounts = new long[] { 100, 37, 35, 14, 11, 9, 7, 5, 3 };
        HuffmanTree huffmanTree = new HuffmanTree(inCounts);
        assertEquals("0", huffmanTree.getCode(0)); // code of 100
        assertEquals("111", huffmanTree.getCode(1)); // code of 37
        assertEquals("110", huffmanTree.getCode(2)); // code of 35
        assertEquals("1010", huffmanTree.getCode(3)); // code of 14
        assertEquals("1001", huffmanTree.getCode(4)); // code of 11
        assertEquals("1000", huffmanTree.getCode(5)); // code of 9
        assertEquals("10110", huffmanTree.getCode(6)); // code of 7
        assertEquals("101111", huffmanTree.getCode(7)); // code of 5
        assertEquals("101110", huffmanTree.getCode(8)); // code of 3
    }

}
