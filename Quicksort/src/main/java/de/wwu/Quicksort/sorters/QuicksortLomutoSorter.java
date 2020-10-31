package de.wwu.Quicksort.sorters;

import java.util.ArrayList;
import java.util.List;

import de.wwu.Quicksort.SortingStep;
import de.wwu.Quicksort.utils.Utils;

/**
 * Implementation des Sortierverfahrens: LomutoSort. Für weitere Details sei auf die begleitende Arbeit verwiesen (https://github.com/jstuebbe/QuicksortOptimization)
 * @author Jonas Stübbe im Zuge der BA
 *
 * @param <I>
 */
public class QuicksortLomutoSorter<I extends Comparable<I>> extends QuicksortSorter<I> {


	@Override
	int partition(ArrayList<I> input, int l, int r,List<SortingStep<I>> sortingSteps) {		
		I pivotElement = input.get(r);
		int i = l;
		int j = l;
		for(; j < r; j++) {

			saveSortingStep(sortingSteps, input, i, j, r);

			if(input.get(j).compareTo(pivotElement) < 0) {
				Utils.swap(input,i,j);
				i++;
			}
		}
		Utils.swap(input, i , r);

		saveSortingStep(sortingSteps, input, i, j, i);

		return i;
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
		texLegend += addText("Legende: Das aktuelle Pivot-Element ist grau hervorgehoben und wird mit dem Zeiger pivot indiziert. "
				+ "Des Weiteren geben die Zeiger l und r die aktuellen Bereiche an.", TextLineOptions.TEXT_NEW_LINE, TextFontOptions.TEXT);
		return texLegend;
	}

}
