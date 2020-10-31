package de.wwu.Quicksort.sorters;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;

import de.wwu.Quicksort.SortingStep;
import de.wwu.Quicksort.utils.Utils;
import javafx.scene.Node;
import javafx.scene.control.Label;

/**
 * Implementation des Sortierverfahrens: Quicksort. Für weitere Details sei auf die begleitende Arbeit verwiesen (https://github.com/jstuebbe/QuicksortOptimization)
 * @author Jonas Stübbe im Zuge der BA
 *
 * @param <I>
 */
public class QuicksortSorter<I extends Comparable<I>> extends TexStringGenerator<I> {

	@Override
	public void sort(ArrayList<I> input, List<SortingStep<I>> sortingSteps) {
		if(sortingSteps == null) {
			throw new IllegalArgumentException("sortingSteps müssen definiert sein!");
		}
		if(input == null || input.size() == 0) {
			throw new IllegalArgumentException("Kein/ungueltiges Feld.");
		}
		quicksort(input, 0, input.size() - 1, sortingSteps);
	}

	public void quicksort(ArrayList<I> input, int l, int r,List<SortingStep<I>> sortingSteps) {
		if(l < r) {
			choosePivot(input,l,r,sortingSteps);

			saveSortingStep(sortingSteps, input, l, r, r);

			int cut = partition(input, l, r,sortingSteps);
			quicksort(input,l,cut - 1,sortingSteps);
			quicksort(input, cut + 1, r,sortingSteps);
		}


	}

	public void choosePivot(ArrayList<I> input, int l, int r,List<SortingStep<I>> sortingSteps) {
	}


	int partition(ArrayList<I> input, int l, int r,List<SortingStep<I>> sortingSteps) {		
		I pivotElement = input.get(r);
		int i = l;
		int j = r-1;
		int pivotIndex = r ; 

		while ( i < j ) {	

			while(j > l	 && input.get(j).compareTo(pivotElement) >= 0) {
				j--;

				saveSortingStep(sortingSteps, input, i, j, pivotIndex);
			}


			while (i < r - 1 && input.get(i).compareTo(pivotElement) < 0) {
				i++;
				saveSortingStep(sortingSteps, input, i, j, pivotIndex);
			}

			if ( i < j ) {
				Utils.swap(input , i , j);
				j--;
				i++;
				saveSortingStep(sortingSteps, input, i, j, pivotIndex);
			}
		}

		if(input.get(i).compareTo(pivotElement) > 0) {
			Utils.swap(input , i , pivotIndex );
			saveSortingStep(sortingSteps, input, i, j, pivotIndex);
		}
		else if(i==j){
			Utils.swap(input , ++i , pivotIndex );
			saveSortingStep(sortingSteps, input, i, j, pivotIndex);
		}

		return i ;
	}

	public void saveSortingStep(List<SortingStep<I>> sortingSteps, ArrayList<I> input, Integer l, Integer r, Integer pivot) {
		SortingStep<I> step = new SortingStep<I>();

		if(input == null) {
			//Exception
		}
		step.addToArrayListMap("input", new ArrayList<I>(input));
		if(l != null) step.addToIndexMap("l", l);
		if(r != null) step.addToIndexMap("r", r);
		if(pivot != null) step.addToIndexMap("pivot", pivot);

		sortingSteps.add(step);
	}


	@Override
	public String getTexString(ArrayList<SortingStep<I>> sortingSteps) {
		String texString = getTexIntro() + getLegend();
		String[] input;
		Integer l;
		Integer r;
		Integer pivot;
		int size = sortingSteps.get(0).getFromArrayListMap("input").size();

		for(SortingStep<I> sortingStep : sortingSteps) {
			input = sortingStep.getFromArrayListMap("input").stream().map(s -> (s != null ? s.toString() : "-")).toArray(String[]::new);
			l = sortingStep.getFromIndexMap("l");
			r = sortingStep.getFromIndexMap("r");
			pivot = sortingStep.getFromIndexMap("pivot");

			List<Integer> pointerIndex = new ArrayList<Integer>();
			pointerIndex.add(l);
			pointerIndex.add(r);
			pointerIndex.add(pivot);
			List<String> pointerText = new ArrayList<String>();
			pointerText.add("l");
			pointerText.add("r");
			pointerText.add("pivot");
			List<String> pointerDownarrow = new ArrayList<String>();
			pointerDownarrow.add("$\\downarrow$");
			pointerDownarrow.add("$\\downarrow$");
			pointerDownarrow.add("$\\downarrow$");

			texString += beginTable(size, null,null,null);
			texString += getPointers(pointerIndex,pointerText,size,true); // l,r, pivot
			texString += getPointers(pointerIndex,pointerDownarrow,size,true); // downarrow
			ArrayList<Integer> pivots = new ArrayList<Integer>();
			pivots.add(pivot);
			texString += getArray(input, pivots,null);
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

		Label pivotLabel = new Label("pivot: " + (sortingStep.getFromIndexMap("pivot") != null ? sortingStep.getFromIndexMap("pivot").toString() : "nicht gefunden"));

		c.add(inputLabel);
		c.add(lLabel);
		c.add(rLabel);
		c.add(pivotLabel);
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
		texLegend += addText("Legende: Das aktuelle Pivot-Element ist grau hervorgehoben und wird mit dem Zeiger pivot indiziert."
				+ " Des Weiteren geben die Zeiger l und r die aktuellen Bereiche an.", TextLineOptions.TEXT_NEW_LINE, TextFontOptions.TEXT);
		return texLegend;
	}

}
