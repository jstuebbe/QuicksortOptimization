package de.wwu.Quicksort.sorters;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import de.wwu.Quicksort.SortingStep;
import de.wwu.Quicksort.utils.Classifier;
import de.wwu.Quicksort.utils.Utils;
import javafx.scene.Node;
import javafx.scene.control.Label;

/**
 * Implementation des Sortierverfahrens: Super Scalar Samplesort. Für weitere Details sei auf die begleitende Arbeit verwiesen (https://github.com/jstuebbe/QuicksortOptimization)
 * @author Jonas Stübbe im Zuge der BA
 *
 * @param <I>
 */
public class S3Sorter<I extends Comparable<I>> extends TexStringGenerator<I> {	
	final int BASECASESIZE = 4; //1024
	final int MAXBUCKETS = 4;
	final int SAMPLESIZE = MAXBUCKETS - 1;
	final int PUFFERSIZE = 3;

	//Kann in einer Erweiterung hinzugefügt werden.
	//final int OVERSAMPLING_FACTOR = 1;

	private Classifier<I> classifier;

	@Override
	public void sort(ArrayList<I> input, List<SortingStep<I>> sortingSteps) {
		if(sortingSteps == null) {
			throw new IllegalArgumentException("sortingSteps müssen definiert sein!");
		}
		if(input == null || input.size() == 0) {
			throw new IllegalArgumentException("Kein/ungueltiges Feld.");
		}
		s3Sort(input,0,input.size()-1,sortingSteps);
	}

	public void s3Sort(ArrayList<I> input, int l, int r,List<SortingStep<I>> sortingSteps) {

		int n = r - l + 1;
		if(n / SAMPLESIZE < BASECASESIZE) {
			Utils.smallSort(input,l,r); //insertionsort
			saveSortingStep(sortingSteps, input, null, null, null, l, r, null, null, null, null);
			return;
		}

		ArrayList<I> out = new ArrayList<I>((Collections.nCopies(input.size(), null)));

		int[] bktout = new int[n];

		Map<String,Integer> constantsMap = new HashMap<String,Integer>();
		constantsMap.put("BASECASESIZE", BASECASESIZE);
		constantsMap.put("SAMPLESIZE", SAMPLESIZE);
		constantsMap.put("MAXBUCKETS", MAXBUCKETS);

		//Kann in einer Erweiterung hinzugefügt werden.
		//constantsMap.put("OVERSAMPLING_FACTOR", OVERSAMPLING_FACTOR);

		saveSortingStep(sortingSteps, input, null, null, out, l, r, null, bktout, null, constantsMap);

		s3SortInt(input,l,r,out,bktout,false,sortingSteps);

		for(int i = 0; i < input.size(); i++) {
			input.set(i, out.get(i));
		}

		out = null;
		bktout = null;

		saveSortingStep(sortingSteps, input, null, null, out, l, r, null, bktout, null, constantsMap);
	}

	public void s3SortInt(ArrayList<I> input,int l, int r,ArrayList<I> out,int[] bktout,boolean beginIsHome,List<SortingStep<I>> sortingSteps) {
		//beginIsHome guckt ob wir gerade input als hilfspeicher benutzen oder nicht.
		saveSortingStep(sortingSteps, input, null, null, out, l, r, beginIsHome, bktout, null, null);

		//------------------------------------------------------------------------
		//Wähle Sample und Sortiere es
		classifier = new Classifier<I>(SAMPLESIZE,MAXBUCKETS);
		classifier.build(input, l, r);
		ArrayList<I> samples = classifier.getSamples();

		Utils.smallSort(samples,0,SAMPLESIZE - 1); //insertionsort

		//Prüfe ob alle Samples gleich sind.
		if(samples.get(0).compareTo(samples.get(SAMPLESIZE - 1)) == 0) {
			//Alle Samples gleich.
			samples = null;
			Utils.smallSort(input,l,r);
			//referenziert out oder input auf das ursprüngliche input welches sortiert werden soll? 
			if(!beginIsHome) {
				for(int z = 0; z < r - l + 1; z++) {
					out.set(l + z, input.get(l + z));
				}
			}
			return;
		}
		//------------------------------------------------------------------------

		//------------------------------------------------------------------------
		//Klassifiziere Elemente
		int[] bktsize = new int[MAXBUCKETS];

		ArrayList<I> splitters = classifier.getSplitters();

		classify(input,l,r,samples,bktout,bktsize, splitters);

		saveSortingStep(sortingSteps, input, samples, splitters, out, l, r, beginIsHome, bktout, bktsize, null);

		distribute(input,l,r,out,bktout,bktsize);

		saveSortingStep(sortingSteps, input, samples, splitters, out, l, r, beginIsHome, bktout, bktsize, null);

		int offset = 0;
		for(int i = 0 ; i < MAXBUCKETS; i++) {

			saveSortingStep(sortingSteps, input, samples, splitters, out, l, r, beginIsHome, bktout, bktsize, null);

			//bktsize ist eine aufaddierende Folge
			int size = bktsize[i] - offset;
			if (size == 0) continue; // leerer Bucket
			if (size <= BASECASESIZE 
					//	        		|| (n / size) < 2
					) {
				// Either it's a small bucket, or very large (more than half of all
				// elements). In either case, we fall back to std::sort.  The reason
				// we're falling back to std::sort in the second case is that the
				// partitioning into buckets is obviously not working (likely
				// because a single value made up the majority of the items in the
				// previous recursion level, but it's also surrounded by lots of
				// other infrequent elements, passing the "all-samples-equal" test.
				Utils.smallSort(out, l+offset,l + bktsize[i] - 1);

				saveSortingStep(sortingSteps, input, samples, splitters, out, l, r, beginIsHome, bktout, bktsize, null);


				if (beginIsHome) {
					saveSortingStep(sortingSteps, input, samples, splitters, out, l, r, beginIsHome, bktout, bktsize, null);
					// uneven recursion level, we have to move the result
					for(int z = 0; z < size; z++) {

						saveSortingStep(sortingSteps, input, samples, splitters, out, l, r, beginIsHome, bktout, bktsize, null);

						input.set(l + offset + z, out.get(l + offset + z));
					}
				}
			} else {
				//Bucket ist noch zu groß, führe Samplesort Rekursiv aus
				s3SortInt(out, 
						l + offset, 
						l + bktsize[i] - 1, //-1?
						input,
						bktout, 
						!beginIsHome,
						sortingSteps);
			}
			offset += size;
		}
	}

	public void classify(ArrayList<I> input, int l, int r, ArrayList<I> samples,int[] bktout, int[] bktsize, ArrayList<I> splitters) {
		for(int i = l; i < r + 1; i++) {
			int bucket = classifier.classify(input.get(i));
			bktout[i] = bucket;
			bktsize[bucket]++;
		}
	}

	public void distribute(ArrayList<I> input, int l, int r, ArrayList<I> out,int[] bktout, int[] bktsize) {
		int sum = 0;
		for(int i = 0; i < MAXBUCKETS; i++) {
			int curr_size = bktsize[i];
			bktsize[i] = sum;
			sum += curr_size;
		}
		for(int i = 0; i < r - l + 1; i++) {
			out.set(l + bktsize[bktout[i + l]]++, input.get(l + i));
		}
	}

	public void saveSortingStep(List<SortingStep<I>> sortingSteps, ArrayList<I> input, ArrayList<I> samples,
			ArrayList<I> splitters,ArrayList<I> out,Integer l, Integer r, Boolean beginIsHome,int[] bktout,int[] bktsize, Map<String,Integer> constantsMap ) {
		SortingStep<I> step = new SortingStep<I>();

		if(input == null) {
			//TODO Exception
		}
		step.addToArrayListMap("input", new ArrayList<I>(input));

		if(constantsMap != null) {
			if(constantsMap.get("BASECASESIZE") != null) step.addToIndexMap("BASECASESIZE", constantsMap.get("BASECASESIZE"));
			if(constantsMap.get("SAMPLESIZE") != null) step.addToIndexMap("SAMPLESIZE",  constantsMap.get("SAMPLESIZE"));
			if( constantsMap.get("MAXBUCKETS") != null) step.addToIndexMap("MAXBUCKETS",  constantsMap.get("MAXBUCKETS"));
			if(constantsMap.get("OVERSAMPLING_FACTOR") != null) step.addToIndexMap("OVERSAMPLING_FACTOR",  constantsMap.get("OVERSAMPLING_FACTOR"));
		}

		if(out != null) step.addToArrayListMap("out", new ArrayList<I>(out));
		if(samples != null) step.addToArrayListMap("samples", new ArrayList<I>(samples));
		if(splitters != null) step.addToArrayListMap("splitters", new ArrayList<I>(splitters));

		if(l != null) step.addToIndexMap("l", l);
		if(r != null) step.addToIndexMap("r", r);
		if(beginIsHome != null) step.addToIndexMap("beginIsHome", (beginIsHome) ? 1 : 0);

		if(bktout != null) step.addToIndexArrayMap("bktout", Arrays.stream( bktout ).boxed().toArray( Integer[]::new ));
		if(bktsize != null) step.addToIndexArrayMap("bktsize", Arrays.stream( bktsize ).boxed().toArray( Integer[]::new ));

		sortingSteps.add(step);
	}


	@Override
	public String getTexString(ArrayList<SortingStep<I>> sortingSteps) {
		String texString = getTexIntro() + getLegend();

		String[] input;
		String[] out;

		//Kann in einer Erweiterung hinzugefügt werden.
		//String[] samples;
		String[] splitters;
		String[] bktout;

		//Kann in einer Erweiterung hinzugefügt werden.
		//String[] bktsize;

		Integer l;
		Integer r;

		//Kann in einer Erweiterung hinzugefügt werden.
		//Integer bktoutL;

		Boolean beginIsHome;
		int size = sortingSteps.get(0).getFromArrayListMap("input").size();
		final Integer BASECASESIZE = sortingSteps.get(0).getFromIndexMap("BASECASESIZE");
		final Integer SAMPLESIZE = sortingSteps.get(0).getFromIndexMap("SAMPLESIZE");
		final Integer MAXBUCKETS = sortingSteps.get(0).getFromIndexMap("MAXBUCKETS");

		//Kann in einer Erweiterung hinzugefügt werden.
		//final Integer OVERSAMPLING_FACTOR = sortingSteps.get(0).getFromIndexMap("OVERSAMPLING_FACTOR");

		texString += addText("CONSTANTS: ", TextLineOptions.TEXT_NEW_LINE,TextFontOptions.BOLD);
		texString += addText(" BASECASESIZE: " + (BASECASESIZE != null ? BASECASESIZE : "-"),TextLineOptions.TEXT,TextFontOptions.TEXT);
		texString += addText(" SAMPLESIZE: " + (SAMPLESIZE != null ? SAMPLESIZE : "-"),TextLineOptions.TEXT,TextFontOptions.TEXT);
		texString += addText(" MAXBUCKETS: " + (MAXBUCKETS != null ? MAXBUCKETS : "-"), TextLineOptions.TEXT_NEW_LINE,TextFontOptions.TEXT);

		//Kann in einer Erweiterung hinzugefügt werden.
		//texString += addText("OVERSAMPLING_FACTOR: " + OVERSAMPLING_FACTOR, textOptions.TEXT_NEWLINE);

		for(SortingStep<I> sortingStep : sortingSteps) {
			input = sortingStep.getFromArrayListMap("input").stream()
					.map(s -> (s != null ? s.toString() : "-")).toArray(String[]::new);
			out = sortingStep.getFromArrayListMap("out") != null ? sortingStep.getFromArrayListMap("out").stream()
					.map(s -> (s != null ? s.toString() : "-")).toArray(String[]::new) : null;

			//Kann in einer Erweiterung hinzugefügt werden.
			//samples = (sortingStep.getFromArrayListMap("samples") != null ? sortingStep.getFromArrayListMap("samples").stream()
			//.map(s -> (s != null ? s.toString() : "-")).toArray(String[]::new) : null);
			splitters = (sortingStep.getFromArrayListMap("splitters") != null ? sortingStep.getFromArrayListMap("splitters").stream()
					.map(s -> (s != null ? s.toString() : "-")).toArray(String[]::new) : null);

			bktout = (sortingStep.getFromIndexArrayMap("bktout") != null ? Arrays.stream(sortingStep.getFromIndexArrayMap("bktout"))
					.map(s -> (s != null ? s.toString() : "-")).toArray(String[]::new) : null);


			//Kann in einer Erweiterung hinzugefügt werden.
			//bktsize = (sortingStep.getFromIndexArrayMap("bktsize") != null ? Arrays.stream(sortingStep.getFromIndexArrayMap("bktsize"))
			//.map(s -> (s != null ? s.toString() : "-")).toArray(String[]::new) : null);

			l = sortingStep.getFromIndexMap("l");
			r = sortingStep.getFromIndexMap("r");

			//Kann in einer Erweiterung hinzugefügt werden.
			//bktoutL = sortingStep.getFromIndexMap("bktoutL");

			beginIsHome = (sortingStep.getFromIndexMap("beginIsHome") != null ? (sortingStep.getFromIndexMap("beginIsHome") == 1) ? true : false : null);

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

			if(beginIsHome != null) {
				texString += addText("beginIsHome: " + beginIsHome, TextLineOptions.TEXT_NEW_LINE,TextFontOptions.TEXT);
			}

			texString += beginTable(size, null,null,null);
			texString += getPointers(pointerIndex,pointerText,size,true); // l,r, pivot
			texString += getPointers(pointerIndex,pointerDownarrow,size,true); // downarrow

			texString += getArray(input, pivots,null);
			if(out != null) {
				texString += seperateTable();
				if(out != null) texString += getArray(out, null,null);
			}
			if(bktout != null) {
				texString += seperateTable();
				if(bktout != null) texString += getArray(bktout, null,null);
			}

			texString += endTable();
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
		texLegend += addText("Legende: Das aktuelle Sample ist grau\n" + 
				"hervorgehoben mit dem Splitter-Feld dargestellt als Binärbaum."
				+ "Neben der Eingabesequenz wird das Feld out, mit welchem die Eingabe alterniert, sowie das Oracle-Feld\n" + 
				"dargestellt. Des Weiteren geben die Zeiger l und r die aktuellen Bereiche an.", TextLineOptions.TEXT_NEW_LINE, TextFontOptions.TEXT);
		return texLegend;
	}
}


