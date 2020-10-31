package de.wwu.Quicksort.sorters;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;

import de.wwu.Quicksort.SortingStep;
import javafx.scene.Node;
import javafx.scene.control.Label;

/**
 * Implementation des Sortierverfahrens: Insertionsort. Für weitere Details sei auf die begleitende Arbeit verwiesen (https://github.com/jstuebbe/QuicksortOptimization)
 * @author Jonas Stübbe im Zuge der BA
 *
 * @param <I>
 */
public class InsertionsortSorter<I extends Comparable<I>> extends TexStringGenerator<I>{

	@Override
	public void sort(ArrayList<I> input, List<SortingStep<I>> sortingSteps) {
		if(sortingSteps == null) {
			throw new IllegalArgumentException("sortingSteps müssen definiert sein!");
		}
		if(input == null || input.size() == 0) {
			throw new IllegalArgumentException("Kein/ungueltiges Feld.");
		}
		saveSortingStep(sortingSteps, input, 0, 0);

		for(int i=0;i<input.size();i++) {
			I temp = input.get(i);
			int j = i;
			while(j > 0 && input.get(j-1).compareTo(temp) > 0) { //input[j-1] > temp
				input.set(j, input.get(j-1));
				j = j-1;

				saveSortingStep(sortingSteps, input, i, j);
			}
			input.set(j, temp);

			saveSortingStep(sortingSteps, input, i, j);
		}	
	}

	public void saveSortingStep(List<SortingStep<I>> sortingSteps, ArrayList<I> input, Integer i, Integer j) {
		SortingStep<I> step = new SortingStep<I>();
		if(input == null) {
			//Exception
		}
		step.addToArrayListMap("input", new ArrayList<I>(input));
		if(i != null) step.addToIndexMap("i", i);
		if(j != null) step.addToIndexMap("j", j);

		sortingSteps.add(step);
	}

	@Override
	public String getTexString(ArrayList<SortingStep<I>> sortingSteps) {
		String texString = getTexIntro() + getLegend();
		String[] input;
		Integer i;
		Integer j;
		Integer pivot;
		int size = sortingSteps.get(0).getFromArrayListMap("input").size();

		for(SortingStep<I> sortingStep : sortingSteps) {
			input = sortingStep.getFromArrayListMap("input").stream().map(s -> (s != null ? s.toString() : "-")).toArray(String[]::new);
			i = sortingStep.getFromIndexMap("i");
			j = sortingStep.getFromIndexMap("j");
			pivot = sortingStep.getFromIndexMap("pivot");


			List<Integer> pointerIndex = new ArrayList<Integer>();
			pointerIndex.add(i);
			pointerIndex.add(j);
			List<String> pointerText = new ArrayList<String>();
			pointerText.add("i");
			pointerText.add("j");
			List<String> pointerDownarrow = new ArrayList<String>();
			pointerDownarrow.add("$\\downarrow$");
			pointerDownarrow.add("$\\downarrow$");

			texString += beginTable(size, null,null,null);
			texString += getPointers(pointerIndex,pointerText,size,true); // i,j
			texString += getPointers(pointerIndex,pointerDownarrow,size,true); // downarrow
			ArrayList<Integer> pivots = null;
			if(pivot != null) {
				pivots = new ArrayList<Integer>();
				pivots.add(pivot);
			}
			texString += getArray(input,pivots,null);
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
		texLegend += addText("Legende: Die Zeiger i und j geben die aktuellen Bereiche an", TextLineOptions.TEXT_NEW_LINE, TextFontOptions.TEXT);
		return texLegend;
	}

}
