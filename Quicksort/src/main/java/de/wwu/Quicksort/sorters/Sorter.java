package de.wwu.Quicksort.sorters;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import de.wwu.Quicksort.SortingStep;
import javafx.scene.Node;

/**
 * Das Generisches Interface Sorter sorgt dafür, dass neue Sortierverfahren die nötigen
 * Methoden implementieren müssen, um mit dem Programm kompatibel zu sein.
 * @author Jonas Stübbe im Zuge der BA
 *
 * @param <I>
 */
public interface Sorter<I>{

	public void sort(ArrayList<I> input, List<SortingStep<I>> sortingSteps);

	public String getTexString(ArrayList<SortingStep<I>> sortingSteps);

	public Collection<Node> getViewElements(SortingStep<I> sortingStep);

	public String getTexIntro();
	
	public String getLegend();

}
