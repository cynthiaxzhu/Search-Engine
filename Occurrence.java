package se;

/**
 * Occurrence encapsulates an occurence of a keyword in a document.
 * 
 * @author Data Structures Fall 2020
 * @author Cynthia Zhu
 */
public class Occurrence {
	
	/**
	 * Document in which keyword occurs.
	 */
	String document;
	
	/**
	 * Frequency of occurrence of keyword in document.
	 */
	int frequency;
	
	/**
	 * Initializes Occurence with document and frequency fields.
	 * 
	 * @param doc Document in which keyword occurs
	 * @param freq Frequency of occurrence of keyword in document
	 */
	public Occurrence(String doc, int freq) {
		this.document = doc;
		this.frequency = freq;
	}
	
}