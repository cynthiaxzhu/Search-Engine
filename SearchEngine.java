package se;

import java.io.*;
import java.util.*;

/**
 * SearchEngine indexes and searches for keywords using hash tables.
 * 
 * @author Data Structures Fall 2020
 * @author Cynthia Zhu
 */
public class SearchEngine {
	
	/**
	 * Hash map of all keywords.
	 */
	HashMap<String, ArrayList<Occurrence>> keywordsIndex;
	
	/**
	 * Hash set of all noise words.
	 */
	HashSet<String> noiseWords;
	
	/**
	 * Initializes SearchEngine with keyWordsIndex and noiseWords fields.
	 */
	public SearchEngine() {
		keywordsIndex = new HashMap<String, ArrayList<Occurrence>>(1000, 2.0f);
		noiseWords = new HashSet<String>(100, 2.0f);
	}
	
	/**
	 * Indexes all keywords in all documents.
	 * 
	 * @param noiseWordsFile Name of file that contains list of all noise words
	 * @param documentsFile Name of file that contains list of all document file names
	 * @throws FileNotFoundException If file is not found
	 */
	public void makeKeywordsIndex(String noiseWordsFile, String documentsFile) 
	throws FileNotFoundException {
		
		Scanner sc = new Scanner(new File(noiseWordsFile));
		while (sc.hasNext()) {
			String word = sc.next();
			noiseWords.add(word);
		}
		
		sc = new Scanner(new File(documentsFile));
		while (sc.hasNext()) {
			String documentFile = sc.next();
			HashMap<String, Occurrence> keywordsIndexPerDocument = makeKeywordsIndexPerDocument(documentFile);
			insertIntoKeywordsIndex(keywordsIndexPerDocument);
		}
		
		sc.close();
		
	}
	
	/**
	 * Indexes all keywords in one document.
	 * 
	 * @param documentFile Name of file that contains keywords
	 * @return Hash table of all keywords in given document
	 * @throws FileNotFoundException If file is not found
	 */
	public HashMap<String, Occurrence> makeKeywordsIndexPerDocument(String documentFile) 
	throws FileNotFoundException {
		HashMap<String, Occurrence> keywordsIndexPerDocument = new HashMap<String, Occurrence>(1000, 2.0f);
		Scanner sc = new Scanner(new File(documentFile));
		while (sc.hasNext()) {
			String word = sc.next();
			word = convertToKeyword(word);
			if (word != null) {
				if (!keywordsIndexPerDocument.containsKey(word)) {
					Occurrence occ = new Occurrence(documentFile, 0);
					keywordsIndexPerDocument.put(word, occ);
				}
				keywordsIndexPerDocument.get(word).frequency += 1;
			}
		}
		return keywordsIndexPerDocument;
	}
	
	/**
	 * Converts word to keyword.
	 * 
	 * @param word Word
	 * @return Keyword if word is keyword, null if word is not keyword
	 */
	public String convertToKeyword(String word) {
		
		//removes trailing punctuation
		char lastChar = word.charAt(word.length() - 1);
		while ((lastChar == '.') || (lastChar == '!') || (lastChar == '?') ||
			   (lastChar == ',') || (lastChar == ';') || (lastChar == ':')) {
			word = word.substring(0, word.length() - 1);
			if (word.equals("")) {
				return null;
			}
			lastChar = word.charAt(word.length() - 1);
		}
		
		//checks whether word contains only letters
		for (int i = 0; i < word.length(); i++) {
			char c = word.charAt(i);
			if (!(c >= 'a' && c <= 'z') && !(c >= 'A' && c <= 'Z')) {
				return null;
			}
		}
		
		//converts word to lower case
		word = word.toLowerCase();
		
		//checks whether word is noise word
		if (noiseWords.contains(word)) {
			return null;
		}
		
		return word;
		
	}
	
	/**
	 * Inserts all keywords in one document into hash table of all keywords in all documents.
	 * 
	 * @param kws Hash table of all keywords in one document
	 */
	public void insertIntoKeywordsIndex(HashMap<String,Occurrence> kws) {
		for (Map.Entry<String, Occurrence> entry : kws.entrySet()) {
			String keyword = entry.getKey();
			Occurrence occ = entry.getValue();
			if (!keywordsIndex.containsKey(keyword)) {
				ArrayList<Occurrence> occs = new ArrayList<Occurrence>();
				keywordsIndex.put(keyword, occs);
			}
			keywordsIndex.get(keyword).add(occ);
			insertLastOccurrence(keywordsIndex.get(keyword));
		}
	}
	
	/**
	 * Inserts last occurrence in list in correct position in list.
	 * 
	 * @param occs List of Occurrences
	 */
	public void insertLastOccurrence(ArrayList<Occurrence> occs) {
		int frequency = occs.get(occs.size() - 1).frequency;
		int left = 0;
		int right = occs.size() - 2;
		int index = -1;
		while (left <= right) {
			int middle = (left + right) / 2;
			if (occs.get(middle).frequency == frequency) {
				index = middle;
				break;
			}
			if (occs.get(middle).frequency < frequency) {
				right = middle - 1;
				if (left > right) {
					index = middle;
				}
			} else {
				left = middle + 1;
				if (left > right) {
					index = middle + 1;
				}
			}
		}
		if (index != occs.size() - 1) {
			Occurrence occ = occs.remove(occs.size() - 1);
			occs.add(index, occ);
		}
	}
	
	/**
	 * Searches for kw1 or kw2 in all documents.
	 * 
	 * @param kw1 First keyword
	 * @param kw2 Second keyword
	 * @return List of documents in which either kw1 or kw2 occurs in descending order of frequencies, null if no matches found
	 */
	public ArrayList<String> searchForKeywords(String kw1, String kw2) {
		ArrayList<Occurrence> occs1 = keywordsIndex.get(kw1);
		ArrayList<Occurrence> occs2 = keywordsIndex.get(kw2);
		ArrayList<String> files = new ArrayList<String>(5);
		if (occs1 != null && occs2 == null) {
			while (!occs1.isEmpty()) {
				files.add(occs1.get(0).document);
				occs1.remove(0);
			}
		} else if (occs1 == null && occs2 != null) {
			while (!occs2.isEmpty()) {
				files.add(occs2.get(0).document);
				occs2.remove(0);
			}
		} else if (occs1 != null && occs2 != null) {
			while (!occs1.isEmpty() && !occs2.isEmpty()) {
				Occurrence occ1 = occs1.get(0);
				Occurrence occ2 = occs2.get(0);
				if (occ1.frequency >= occ2.frequency) {
					files.add(occ1.document);
					occs1.remove(0);
					for (int i = 0; i < occs2.size(); i++) {
						if (occs2.get(i).document == occ1.document) {
							occs2.remove(i);
							break;
						}
					}
				} else {
					files.add(occ2.document);
					occs2.remove(0);
					for (int i = 0; i < occs1.size(); i++) {
						if (occs1.get(i).document == occ2.document) {
							occs1.remove(i);
							break;
						}
					}
				}
			}
			while (!occs1.isEmpty() && occs2.isEmpty()) {
				files.add(occs1.get(0).document);
				occs1.remove(0);
			}
			while (occs1.isEmpty() && !occs2.isEmpty()) {
				files.add(occs2.get(0).document);
				occs2.remove(0);
			}
		}
		return files;
	}
	
}