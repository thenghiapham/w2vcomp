package common.wordnet;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Random;

public class WordNetAdj {
	HashMap<String, Synset> id2Synset;
	HashMap<String, ArrayList<String>> word2SynsetIds;
	Random random;
	public WordNetAdj(String adjFile) throws IOException {
		id2Synset = WordNetReader.readSynsets(adjFile);
		word2SynsetIds = WordNetReader.getWord2SynsetIds(id2Synset);
		random = new Random();
	}
	
	public static String[] collection2Array(Collection<String> collection) {
		String[] result = new String[collection.size()];
		return collection.toArray(result);
	}
	
	public static void printStrings(String[] array) {
		StringBuffer sb = new StringBuffer();
		if (array == null || array.length == 0) {
			System.out.println();
			return;
		}
		sb.append(array[0]);
		for (int i = 1; i < array.length; i++) {
			String word = array[i];
			sb.append(", " + word );
		}
		System.out.println(sb.toString());
	}
	
	public boolean hasAdjSynset(String word) {
        return word2SynsetIds.containsKey(word);
    }
	
	public static ArrayList<String> getShuffledSynonyms(String[] synonyms, String[] simnyms) {
//	    int synLength = synonyms.length;
//	    int simLength = simnyms.length;
	    ArrayList<String> result = new ArrayList<String>(Arrays.asList(synonyms));
	    List<String> simList = Arrays.asList(simnyms);
        Collections.shuffle(simList);
        result.addAll(simList);
	    return result;
	}
	
	public String[][] getRandomSynoAntoSimNyms(String word) {
		ArrayList<String> synsetIds = word2SynsetIds.get(word);
		if (synsetIds == null) return null;
		String[][] result = new String[3][];
		HashSet<String> antonyms = new HashSet<String>();
		String id = synsetIds.get(random.nextInt(synsetIds.size()));
		Synset synset = id2Synset.get(id);
		if (synset.antonymSSId == null) {
			// TODO: if it's an s type?
			if (synset.synsetType.equals("s")) {
				for (String simId: synset.simSSId) {
					Synset simSynset = id2Synset.get(simId);
					if (simSynset.antonymSSId == null) continue;
					Synset antoSynset = id2Synset.get(simSynset.antonymSSId);
					for (String antonym: antoSynset.words) {
						antonyms.add(antonym);
					}
				}
			}
		}
		else {
			Synset antoSynset = id2Synset.get(synset.antonymSSId);
			for (String antonym: antoSynset.words) {
				antonyms.add(antonym);
			}
		}
		ArrayList<String> synonyms = new ArrayList<String>();
		for (String synonym: synset.words) {
			synonyms.add(synonym);
		}
		// TODO: check this
		synonyms.remove(word);
		// TODO: include type s
		ArrayList<String> simnyms = new ArrayList<String>(); 
		String[] simSSId = synset.simSSId;
		for (String simId: simSSId) {
			Synset simSynset = id2Synset.get(simId);
			for (String simnym: simSynset.words) {
				simnyms.add(simnym);
			}
		}
		simnyms.remove(word);
		result[2] =collection2Array(simnyms);
		result[1] = collection2Array(synonyms);
		result[0] = collection2Array(antonyms);
		return result;
	}
	
	public String[] getAllAntonyms(String word) {
		ArrayList<String> synsetIds = word2SynsetIds.get(word);
		if (synsetIds == null) return null;
		HashSet<String> antonyms = new HashSet<String>();
		for (String id: synsetIds) {
			Synset synset = id2Synset.get(id);
			if (synset.antonymSSId == null) {
				// TODO: if it's an s type?
				if (synset.synsetType.equals("s")) {
					for (String simId: synset.simSSId) {
						Synset simSynset = id2Synset.get(simId);
						if (simSynset.antonymSSId == null) continue;
						Synset antoSynset = id2Synset.get(simSynset.antonymSSId);
						for (String antonym: antoSynset.words) {
							antonyms.add(antonym);
						}
					}
				}
			}
			else {
				Synset antoSynset = id2Synset.get(synset.antonymSSId);
				for (String antonym: antoSynset.words) {
					antonyms.add(antonym);
				}
			}
		}
		return collection2Array(antonyms);
	}
	
	public String[] getAllSynonyms(String word) {
		ArrayList<String> synsetIds = word2SynsetIds.get(word);
		if (synsetIds == null) return null;
		HashSet<String> synonyms = new HashSet<String>();
		for (String id: synsetIds) {
			Synset synset = id2Synset.get(id);
			
			// TODO: include type s
//			if (synset.synsetType.equals("s")) continue;
			
			for (String synonym: synset.words) {
				synonyms.add(synonym);
			}
		}
		// TODO: check this
		synonyms.remove(word);
		return collection2Array(synonyms);
	}
	
	public String[] getAllSimilars(String word) {
		ArrayList<String> synsetIds = word2SynsetIds.get(word);
		if (synsetIds == null) return null;
		HashSet<String> simnyms = new HashSet<String>();
		for (String id: synsetIds) {
			Synset synset = id2Synset.get(id);
//			 TODO: include type s
//			if (synset.synsetType.equals("s")) continue;
						
			String[] simSSId = synset.simSSId;
			for (String simId: simSSId) {
				Synset simSynset = id2Synset.get(simId);
				for (String simnym: simSynset.words) {
					simnyms.add(simnym);
				}
			}
		}
		simnyms.remove(word);
		return collection2Array(simnyms);
	}
	
	public static void main(String[] args) throws IOException{
		String adjFile = args[0];
		WordNetAdj wordnetAdj = new WordNetAdj(adjFile);
		String word = "excellent";
		System.out.println("***   " + word + "   ***");
		
		String[][] antoSynoSimNyms = wordnetAdj.getRandomSynoAntoSimNyms(word);
		System.out.print("antonyms:");
		printStrings(antoSynoSimNyms[0]);
		System.out.print("synonyms:");
		printStrings(antoSynoSimNyms[1]);
		System.out.print("simnyms:");
		printStrings(antoSynoSimNyms[2]);
		
//		System.out.print("antonyms:");
//		printStrings(wordnetAdj.getAllAntonyms(word));
//		System.out.print("synonyms:");
//		printStrings(wordnetAdj.getAllSynonyms(word));
//		System.out.print("simnyms:");
//		printStrings(wordnetAdj.getAllSimilars(word));
	}
}
