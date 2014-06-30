package common;

import io.word.Phrase;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import dependency.RawPhraseEntry;

public class DataStructureUtils {
    public static <T> HashSet<T> arrayToSet(T[] inputArray) {
        HashSet<T> result = new HashSet<T>();
        if (inputArray != null) {
            for (int i = 0; i < inputArray.length; i++) {
                result.add(inputArray[i]);
            }
        }
        return result;
    }

    public static <T> HashMap<T, Integer> arrayToMap(T[] inputArray) {
        HashMap<T, Integer> result = new HashMap<T, Integer>();
        if (inputArray != null) {
            for (int i = 0; i < inputArray.length; i++) {
                result.put(inputArray[i], i);
            }
        }
        return result;
    }

    public static <T> ArrayList<T> arrayToList(T[] inputArray) {
        ArrayList<T> result = new ArrayList<T>();
        if (inputArray != null) {
            for (int i = 0; i < inputArray.length; i++) {
                result.add(inputArray[i]);
            }
        }
        return result;
    }

    public static float[][] arrayListTo2dArray(List<float[]> list) {
        float[][] array = new float[list.size()][list.get(0).length];
        list.toArray(array);
        return array;
    }

    public static String[] stringListToArray(List<String> list) {
        String[] array = new String[list.size()];
        list.toArray(array);
        return array;
    }

    public static Phrase[] phraseListToArray(List<Phrase> list) {
        Phrase[] array = new Phrase[list.size()];
        list.toArray(array);
        return array;
    }

    public static RawPhraseEntry[] rawPhraseListToArray(
            List<RawPhraseEntry> list) {
        RawPhraseEntry[] array = new RawPhraseEntry[list.size()];
        list.toArray(array);
        return array;
    }

    public static int[] intListToArray(List<Integer> list) {
        int[] array = new int[list.size()];
        int i = 0;
        for (Integer element : list) {
            array[i] = element;
            i++;
        }
        return array;
    }

    public static int searchSmallArray(int[] array, int key) {
        for (int i = 0; i < array.length; i++) {
            if (array[i] == key)
                return i;
        }
        return -1;
    }

}
