package de.wwu.Quicksort.sorters;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.stream.Collectors;

import de.wwu.Quicksort.SortingStep;
import de.wwu.Quicksort.utils.Utils;
import javafx.scene.Node;
import javafx.scene.control.Label;

/**
 * Implementation des Sortierverfahrens: Samplesort. Für weitere Details sei auf die begleitende Arbeit verwiesen (https://github.com/jstuebbe/QuicksortOptimization)
 * @author Jonas Stübbe im Zuge der BA
 *
 * @param <I>
 */
public class SamplesortSorter<I extends Comparable<I>> extends TexStringGenerator<I> {	
	final int BASECASESIZE = 4; //1024
	final int MAXBUCKETS = 4;
	final int SAMPLESIZE = MAXBUCKETS - 1;
	final int PUFFERSIZE = 3;

	//Kann in einer Erweiterung hinzugefügt werden.
	//final int OVERSAMPLING_FACTOR = 1;

	@Override
	public void sort(ArrayList<I> input, List<SortingStep<I>> sortingSteps) {
		if(sortingSteps == null) {
			throw new IllegalArgumentException("sortingSteps müssen definiert sein!");
		}
		if(input == null || input.size() == 0) {
			throw new IllegalArgumentException("Kein/ungueltiges Feld.");
		}
		samplesort(input,0,input.size()-1,sortingSteps);
	}

	public void samplesort(ArrayList<I> input,int l,int r, List<SortingStep<I>> sortingSteps) {
		int n = r - l + 1;
		if((n / SAMPLESIZE) < BASECASESIZE) {
			Utils.smallSort(input,l,r); //Hier Insertionsort
			saveSortingStep(sortingSteps, input, null, null, null, null, null,null, l, r);
			return;
		}

		Map<String,Integer> constantsMap = new HashMap<String,Integer>();
		constantsMap.put("BASECASESIZE", BASECASESIZE);
		constantsMap.put("SAMPLESIZE", SAMPLESIZE);
		constantsMap.put("MAXBUCKETS", MAXBUCKETS);

		//Kann in einer Erweiterung hinzugefügt werden.
		//constantsMap.put("OVERSAMPLING_FACTOR", OVERSAMPLING_FACTOR);

		saveSortingStep(sortingSteps, input, null, null, null, constantsMap, null,null, l, r);

		samplesortInt(input,l,r,sortingSteps);
	}

	public void samplesortInt(ArrayList<I> input, int l, int r, List<SortingStep<I>> sortingSteps) {
		ArrayList<I> samples = sample(input,l,r);
		Utils.smallSort(samples,0,SAMPLESIZE - 1); //insertionsort

		saveSortingStep(sortingSteps, input, samples, null, null, null, null, null,l, r);

		//Check ob alle Samples gleich sind.
		if(samples.get(0).compareTo(samples.get(SAMPLESIZE - 1)) == 0) {
			samples = null;
			Utils.smallSort(input,l,r);
			return;
		}

		//Größe: MAXBUCKETS ArrayLists a ... Größe
		ArrayList<ArrayList<I>> buckets = new ArrayList<ArrayList<I>>();
		for(int i = 0; i < MAXBUCKETS; i++) {
			buckets.add(new ArrayList<I>());
		}

		//Größe: SAMPLESIZE
		ArrayList<I> splitters = new ArrayList<I>((Collections.nCopies(SAMPLESIZE, null)));
		//baue einen Binärbaum aus samples -> splitters
		buildRecursive(samples,0, SAMPLESIZE - 1, 1,splitters);

		findAndInsert(input, l, r, samples, splitters, buckets, sortingSteps);

		int[] bktsize = new int[MAXBUCKETS];

		for(int counter = l,i = 0 ; i < MAXBUCKETS; i++) {
			for(int j = 0; j < buckets.get(i).size(); j++, counter++) {
				input.set(counter, buckets.get(i).get(j));
				bktsize[i]++;
			}

			saveSortingStep(sortingSteps, input, samples, splitters, buckets, null, bktsize, i,l, r);
		}

		int begin = l;
		for(int i = 0 ; i < MAXBUCKETS; i++) {
			if(bktsize[i] < 2) {

			}
			else if(bktsize[i] < BASECASESIZE) {
				Utils.smallSort(input, begin, begin + bktsize[i] - 1);
				saveSortingStep(sortingSteps, input, samples, splitters, buckets, null, bktsize, null, l, r);

			}
			else {
				samplesortInt( input, begin, begin + bktsize[i] - 1,sortingSteps);

			}
			begin = begin + bktsize[i];
		}

	}

	public void findAndInsert(ArrayList<I> input, int l, int r, ArrayList<I> samples,ArrayList<I> splitters,
			ArrayList<ArrayList<I>> buckets, List<SortingStep<I>> sortingSteps){
		saveSortingStep(sortingSteps, input, samples, splitters, buckets, null, null, null,l, r);

		for(int i = l; i < r + 1; i++) {
			//bucket zwischen 0 und MAXBUCKETS
			int bucket = findBucket(splitters,input.get(i));
			buckets.get(bucket).add(input.get(i));
		}

		saveSortingStep(sortingSteps, input, samples, splitters, buckets, null, null,null, l, r);
	}

	public int findBucket(ArrayList<I> splitters, I key) {
		int i = 1;
		while (i <= SAMPLESIZE) {
			i = step(splitters,i, key);
		}
		return (i - MAXBUCKETS);
	}

	public int step(ArrayList<I> splitters, int i, I key) {
		return 2*i + ((key.compareTo(splitters.get(i - 1)) > 0) ? 1 : 0);
	}

	public void buildRecursive(ArrayList<I> samples,int lo, int hi, int pos, ArrayList<I> splitters) {
		int midIndex = lo + (int)(hi - lo)/2;
		splitters.set(pos - 1,samples.get(midIndex));

		if (2 * pos < SAMPLESIZE) {
			buildRecursive(samples,lo, midIndex, 2*pos,splitters); //meine pos ist 0 am Anfang
			buildRecursive(samples,midIndex + 1, hi , 2*pos + 1,splitters);
		}
	}

	// Draw a random sample without replacement using the Fisher-Yates Shuffle.
	// This reorders the input somewhat but the sorting does that anyway.

	//Die letzten sampleSize Stellen der Sequenz input werden zur Stichprobe.
	public ArrayList<I> sample(ArrayList<I> input, int l, int r) {	
		//Größe = SAMPLESIZE
		ArrayList<I> samples = new ArrayList<I>((Collections.nCopies(SAMPLESIZE, null)));

		int max = r - l + 1;
		Random rand = new Random();

		for(int i = 0; i < SAMPLESIZE; i++) {
			if(max == 0) {
			}
			int index =  (rand.nextInt() & Integer.MAX_VALUE) % max--; // zero out the sign bit
			Utils.swap(input,l+index, l+max);
			samples.set(i, input.get(l + max));
		}
		return samples;
	}

	public void saveSortingStep(List<SortingStep<I>> sortingSteps, ArrayList<I> input, ArrayList<I> samples,
			ArrayList<I> splitters,ArrayList<ArrayList<I>> buckets, Map<String,Integer> constantsMap,int[] bktsize,Integer currentBucket,Integer l, Integer r) {
		SortingStep<I> step = new SortingStep<I>();
		step.addToArrayListMap("input", new ArrayList<I>(input));


		if(constantsMap != null) {
			if(constantsMap.get("BASECASESIZE") != null) step.addToIndexMap("BASECASESIZE", constantsMap.get("BASECASESIZE"));
			if(constantsMap.get("SAMPLESIZE") != null) step.addToIndexMap("SAMPLESIZE",  constantsMap.get("SAMPLESIZE"));
			if( constantsMap.get("MAXBUCKETS") != null) step.addToIndexMap("MAXBUCKETS",  constantsMap.get("MAXBUCKETS"));
			if(constantsMap.get("OVERSAMPLING_FACTOR") != null) step.addToIndexMap("OVERSAMPLING_FACTOR",  constantsMap.get("OVERSAMPLING_FACTOR"));
		}

		if(l != null) step.addToIndexMap("l", l);
		if(r != null) step.addToIndexMap("r", r);
		if(currentBucket != null) step.addToIndexMap("currentBucket", currentBucket);

		if(samples != null) step.addToArrayListMap("samples", new ArrayList<I>(samples));

		if(splitters != null) step.addToArrayListMap("splitters", new ArrayList<I>(splitters));

		if(buckets != null) {
			for(int z = 0; z < MAXBUCKETS; z++) {
				step.addToArrayListMap("bucket" + z, new ArrayList<I>(buckets.get(z)));
			}
		}

		if(bktsize != null) step.addToIndexArrayMap("bktsize", Arrays.stream( bktsize ).boxed().toArray( Integer[]::new ));
		sortingSteps.add(step);
	}

	@Override
	public String getTexString(ArrayList<SortingStep<I>> sortingSteps) {
		String texString = getTexIntro() + getLegend();

		String[] input;

		//Kann in einer Erweiterung hinzugefügt werden.
		//String[] samples;	
		String[] splitters;
		Integer[] bktsize;

		ArrayList<String[]> buckets;

		Integer l;
		Integer r;
		Integer currentBucket;

		//Könnte man auch aus der Klasse ziehen, ich möchte gerne die Möglichkeit offen halten, diese zu Parametriesieren.
		final Integer BASECASESIZE = sortingSteps.get(0).getFromIndexMap("BASECASESIZE");
		final Integer SAMPLESIZE = sortingSteps.get(0).getFromIndexMap("SAMPLESIZE");
		final Integer MAXBUCKETS = sortingSteps.get(0).getFromIndexMap("MAXBUCKETS");
		int size = sortingSteps.get(0).getFromArrayListMap("input").size();

		texString += addText("CONSTANTS: ", TextLineOptions.TEXT_NEW_LINE,TextFontOptions.BOLD);
		texString += addText(" BASECASESIZE: " + (BASECASESIZE != null ? BASECASESIZE : "-"),TextLineOptions.TEXT,TextFontOptions.TEXT);
		texString += addText(" SAMPLESIZE: " + (SAMPLESIZE != null ? SAMPLESIZE : "-"),TextLineOptions.TEXT,TextFontOptions.TEXT);
		texString += addText(" MAXBUCKETS: " + (MAXBUCKETS != null ? MAXBUCKETS : "-"), TextLineOptions.TEXT_NEW_LINE,TextFontOptions.TEXT);
		//		texString += addText("OVERSAMPLING_FACTOR: " + OVERSAMPLING_FACTOR, textOptions.TEXT_NEWLINE);

		for(SortingStep<I> sortingStep : sortingSteps) {
			input = sortingStep.getFromArrayListMap("input").stream().map(s -> (s != null ? s.toString() : "-")).toArray(String[]::new);
			//Kann in einer Erweiterung hinzugefügt werden.
			//samples = (sortingStep.getFromArrayListMap("samples") != null ? sortingStep.getFromArrayListMap("samples").stream()
			//.map(s -> (s != null ? s.toString() : "-")).toArray(String[]::new) : null);
			splitters = (sortingStep.getFromArrayListMap("splitters") != null ? sortingStep.getFromArrayListMap("splitters").stream()
					.map(s -> (s != null ? s.toString() : "-")).toArray(String[]::new) : null);
			bktsize = sortingStep.getFromIndexArrayMap("bktsize");
			buckets = null;
			if(sortingStep.getFromArrayListMap("bucket1") != null) {
				buckets = new ArrayList<String[]>();
				for(int z = 0; z < MAXBUCKETS; z++) {
					String[] bucket = (sortingStep.getFromArrayListMap("bucket"+z) != null ? sortingStep.getFromArrayListMap("bucket"+z).stream()
							.map(s -> (s != null ? s.toString() : "-")).toArray(String[]::new) : null);
					buckets.add(bucket);
				}
			}
			l = sortingStep.getFromIndexMap("l");
			r = sortingStep.getFromIndexMap("r");
			currentBucket = sortingStep.getFromIndexMap("currentBucket");

			List<Integer> pointerIndex = new ArrayList<Integer>();
			pointerIndex.add(l);
			pointerIndex.add(r);
			List<String> pointerText = new ArrayList<String>();
			pointerText.add("l");
			pointerText.add("r");
			List<String> pointerDownarrow = new ArrayList<String>();
			pointerDownarrow.add("$\\downarrow$");
			pointerDownarrow.add("$\\downarrow$");

			ArrayList<Integer> pivots = new ArrayList<Integer>();
			if(splitters != null) {
				texString += getTree(splitters);
				for(int i = 0; i < splitters.length; i ++) {
					pivots.add(r- i);
				}
			}

			texString += beginTable(size, null,null,null);
			texString += getPointers(pointerIndex,pointerText,size,true); // l,r, pivot
			texString += getPointers(pointerIndex,pointerDownarrow,size,true); // downarrow

			if(currentBucket != null && buckets != null) {
				int current = 0;
				for(int i = 0; i < currentBucket; i++) {
					current += bktsize[i];
				}

				texString += getArray(input,null,buckets.get(currentBucket).length,current + l,null);
			}
			else {
				texString += getArray(input, pivots,null);	
			}


			texString += endTable();
			texString += addVSpace(0.1);

			if(buckets != null) {
				for(int i = 0; i < MAXBUCKETS;i++) {
					String[] bucket = buckets.get(i);
					if(bucket != null) {
						texString += addText("Bucket " + i + ": ", TextLineOptions.TEXT, TextFontOptions.TEXT);
						if(bucket.length > 0) {
							texString += beginTable(bucket.length, null,null,null);
							if(currentBucket != null && currentBucket == i) {
								texString += getArray(bucket,null,buckets.get(currentBucket).length,0,null);
							}
							else {
								texString += getArray(bucket, null,null);
							}
							texString += endTable();
						}
						else {
							texString += addText("leer", TextLineOptions.TEXT_NEW_LINE,TextFontOptions.TEXT);
						}
						texString += addVSpace(0.1);
					}
				}
			}
		}
		return texString;
	}

	@Override
	public Collection<Node> getViewElements(SortingStep<I> sortingStep) {
		Collection<Node> c = new HashSet<Node>();
		Label inputLabel = new Label("input: " + (sortingStep.getFromArrayListMap("input") != null ? (sortingStep.getFromArrayListMap("input").stream()
				.map(e -> e.toString()).collect(Collectors.joining(" | "))) : "nicht gefunden"));

		Label lLabel = new Label("l: " + (sortingStep.getFromIndexMap("l") != null ? sortingStep.getFromIndexMap("l").toString() : "nicht gefunden"));
		Label rLabel = new Label("r: " + (sortingStep.getFromIndexMap("r") != null ? sortingStep.getFromIndexMap("r").toString() : "nicht gefunden"));

		c.add(inputLabel);
		c.add(lLabel);
		c.add(rLabel);
		return c;
	}

//	@Override
//	public String getTexIntro() {
//		String texIntro = "";
//		texIntro += addText("Todo: Individualisiertes Intro", TextLineOptions.TEXT_NEW_LINE, TextFontOptions.TEXT);
//		return texIntro;
//	}

	@Override
	public String getLegend() {
		String texLegend = "";
		texLegend += addText("Legende: Das aktuelle Sample ist\n" + 
				"grau hervorgehoben mit dem Splitter-Feld dargestellt als Binärbaum. Der Schreibund\n" + 
				"Lesevorgang in bzw. aus einem Bucket ist gelb hervorgehoben. Des Weiteren\n" + 
				"geben die Zeiger l und r die aktuellen Bereiche an.", TextLineOptions.TEXT_NEW_LINE, TextFontOptions.TEXT);
		return texLegend;
	}
}


