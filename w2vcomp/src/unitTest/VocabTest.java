package unitTest;

import static org.junit.Assert.*;
import org.junit.Test;

import io.word.PushBackWordStream;
import io.word.WordInputStream;

import java.io.ByteArrayInputStream;

import vocab.Vocab;

public class VocabTest {

    // TODO: add more test
    @Test
    public void testLearnVocabFromStream() {
        String inputString = "It was the best of times , it was the worst of times ,\n"
                + "it was the age of wisdom , it was the age of foolishness ,\n"
                + "it was the epoch of belief , it was the epoch of incredulity ,\n"
                + "it was the season of Light , it was the season of Darkness ,\n"
                + "it was the spring of hope , it was the winter of despair ,\n"
                + "we had everything before us , we had nothing before us";
        WordInputStream inputStream = new PushBackWordStream(
                new ByteArrayInputStream(inputString.getBytes()), 100);
        Vocab vocab = new Vocab();
        vocab.learnVocabFromTrainStream(inputStream);
        assertEquals(vocab.getEntry("season").frequency, 2);
        assertEquals(vocab.getEntry("It").frequency, 1);
        assertEquals(vocab.getEntry("it").frequency, 9);
        assertTrue(vocab.getWordIndex("it") < vocab.getWordIndex("we"));
        assertTrue(vocab.getWordIndex("it") < vocab.getWordIndex("It"));
        System.out.println(vocab.getVocabSize());
        assertEquals(vocab.getVocabSize(), 29);
        assertEquals(vocab.getTrainWords(), 86);
    }
}
