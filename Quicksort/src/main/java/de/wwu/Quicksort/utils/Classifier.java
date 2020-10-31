package de.wwu.Quicksort.utils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Random;

import javafx.util.Pair;

/**
 * Klasse zur Klassifizierung einer Eingabesequenz anhand eines erstellten Sample und Splitter-Feldes.
 * Wird für die Klassifizierung in S3Sorter und IPS4oSorter benutzt,
 * zukünftige Erweiterungen sehen außerdem die Benutzung in SamplesortSorter vor.
 * @author Jonas Stübbe im Zuge der BA
 *
 * @param <I>
 */
public class Classifier<I extends Comparable<I>> {
	final int SAMPLESIZE;
	final int MAXBUCKETS;
	private ArrayList<I> samples;
	private ArrayList<I> splitters;


	public Classifier(final int SAMPLESIZE, final int MAXBUCKETS) {
		this.SAMPLESIZE = SAMPLESIZE;
		this.MAXBUCKETS = MAXBUCKETS;
	}

	public ArrayList<I> getSamples(){
		return samples;
	}

	public ArrayList<I> getSplitters(){
		return splitters;
	}

	public int classify(I key) {
		int i = 1;
		while (i <= SAMPLESIZE) {
			i = step(i, key);
		}
		return (i - MAXBUCKETS);
	}

	public Pair<Integer,Boolean> build(ArrayList<I> input,int l,int r){
		sample(input,l,r);
		Utils.smallSort(samples,0,SAMPLESIZE - 1);
		splitters = new ArrayList<I>((Collections.nCopies(SAMPLESIZE, null)));
		buildRecursive(samples,0, SAMPLESIZE - 1, 1,splitters);

		return new Pair<Integer,Boolean>(MAXBUCKETS,false);
	}

	// Draw a random sample without replacement using the Fisher-Yates Shuffle.
	// This reorders the input somewhat but the sorting does that anyway.

	//Die letzten sampleSize Stellen der Sequenz input werden zur Stichprobe.
	//HIER: die ersten Stellen
	public void sample(ArrayList<I> input, int l, int r) {	
		//Größe SAMPLESIZE
		samples = new ArrayList<I>(Collections.nCopies(SAMPLESIZE, null));

		int startMax = r - l + 1;
		int max = r - l + 1;
		Random rand = new Random();

		ArrayList<Integer> usedIndices = new ArrayList<>();
		for(int i = 0; i < SAMPLESIZE; i++) {
			int index;
			do {
				index =  (rand.nextInt() & Integer.MAX_VALUE) % max; // zero out the sign bit
			}while(usedIndices.contains(index));
			usedIndices.add(index);
			max--;
			Utils.swap(input,l+index, l+ startMax - max - 1);
			samples.set(i, input.get(l + startMax - max - 1));
		}
	}

	public void buildRecursive(ArrayList<I> samples,int lo, int hi, int pos, ArrayList<I> splitters) {
		int midIndex = lo + (int)(hi - lo)/2;
		splitters.set(pos - 1, samples.get(midIndex));

		if (2 * pos < SAMPLESIZE) {
			buildRecursive(samples,lo, midIndex, 2*pos,splitters); //meine pos ist 0 am Anfang
			buildRecursive(samples,midIndex + 1, hi , 2*pos + 1,splitters);
		}
	}

	public int step(int i, I key) {
		return 2*i + ((key.compareTo(splitters.get(i - 1)) > 0) ? 1 : 0);
	}

}
