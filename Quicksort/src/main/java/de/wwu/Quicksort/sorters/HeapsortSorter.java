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
 * Implementation des Sortierverfahrens: Heapsort. Für weitere Details sei auf die begleitende Arbeit verwiesen (https://github.com/jstuebbe/QuicksortOptimization)
 * <p>
 * Der Methodenname deleteMin ist durch ADT PriorityQueue vorgegeben.
 * 
 * @author Code aus der Vorlesung Informatik 2 DATENSTRUKTUREN UND ALGORITHMEN im Sommersemester 2018 gehalten von Prof. Dr. Vahrenhold 
 * mit Erlaubnis für dieses Projekt abgeändert und bearbeitet von Jonas Stübbe
 *
 * @param <I>
 */
public class HeapsortSorter<I extends Comparable<I>> extends TexStringGenerator<I> {
	
	private int groesse;
	
	@Override
	public void sort(ArrayList<I> input, List<SortingStep<I>> sortingSteps) {
//		TODO Heapsort mit Grenzen l,r
		if(sortingSteps == null) {
			throw new IllegalArgumentException("sortingSteps müssen definiert sein!");
		}
		if(input == null || input.size() == 0) {
			throw new IllegalArgumentException("Kein/ungueltiges Feld.");
		}
		
		saveSortingStep(sortingSteps, input,null);
		
		MaxHeap(input);
		
		saveSortingStep(sortingSteps, input,groesse);
		
		while(groesse > 1) {
			deleteMin(input);
			
			saveSortingStep(sortingSteps, input,groesse);
		}
	}
	
	public void konstruiereHeap(ArrayList<I> input, int i) {
		int links = 2*i + 1;
		int rechts = 2*i + 2;
		//kleinstes müsste eigentlich in größtes umbenannt werden
		int kleinstes = i;
	
		//Ist das linke Kind noch gueltig und speichert ein Element, das kleiner is als input[i]?
		if((links < groesse) && (input.get(links).compareTo(input.get(i)) > 0)){
			kleinstes = links;
		}
		// Ist das rechte Kind noch gueltig und speichert ein Element, 
		// das kleiner als das kleinere der beiden Elemente data[i] und 
		// data[linkesKind(i)] ?
		if((rechts < groesse) && (input.get(rechts).compareTo(input.get(kleinstes)) > 0)) {
			kleinstes = rechts;
		}
		//Falls data[i] kleiner ist als eines der bei seinem Kindern gespeicherten Elemente,
		//vertausche mit dem kleineren der beiden Elemente und
		//fahre rekursiv fort.
		if(kleinstes != i) {
			Utils.swap(input,i,kleinstes); //data[i] :=: data[smallest]
			konstruiereHeap(input,kleinstes);
		}
	}
	
	public void deleteMin(ArrayList<I> input) {
		if(groesse > 0) {
			Utils.swap(input,0,groesse - 1);
			this.groesse--;
			konstruiereHeap(input,0);
		}
	}
	
	public void MaxHeap(ArrayList<I> input) {
			if(input.size() > 0) {
				groesse = input.size();
				//(Wieder-)Herstellen der Heap-Bedingung.
				for(int i = groesse/2 - 1; i >= 0; i--) {
					konstruiereHeap(input,i);
			}
		}
	}

	public void saveSortingStep(List<SortingStep<I>> sortingSteps, ArrayList<I> input, Integer groesse) {
		
		SortingStep<I> step = new SortingStep<I>();
		if(input == null) {
			//TODO Exception
		}
		step.addToArrayListMap("input", new ArrayList<I>(input));
		if(groesse != null) step.addToIndexMap("groesse", groesse);
		sortingSteps.add(step);
	}
	
	@Override
	public String getTexString(ArrayList<SortingStep<I>> sortingSteps) {
		String texString = getTexIntro() + getLegend();
		String[] input;
		Integer groesse;
		int size = sortingSteps.get(0).getFromArrayListMap("input").size();
		
		for(SortingStep<I> sortingStep : sortingSteps) {
			input = sortingStep.getFromArrayListMap("input").stream().map(s -> (s != null ? s.toString() : "-")).toArray(String[]::new);
			groesse = sortingStep.getFromIndexMap("groesse");
			
			List<Integer> pointerIndex = new ArrayList<Integer>();
			List<String> pointerText = new ArrayList<String>();
			List<String> pointerDownarrow = new ArrayList<String>();
			
			if(groesse != null) {
			pointerIndex.add(groesse);
			pointerText.add("groesse");
			pointerDownarrow.add("$\\downarrow$");
			}
			texString += beginTable(size,null,null,null);
			if(groesse != null) {
			if(groesse < size) { //TODO
				texString += getPointers(pointerIndex,pointerText,size,true); //groesse
				texString += getPointers(pointerIndex,pointerDownarrow,size,true); //downarrow
			}
			}
			texString += getArray(input,null,null);
			texString += endTable();
			texString += getTree(input);
			
		}
		
		return texString;
	}

	@Override
	public Collection<Node> getViewElements(SortingStep<I> sortingStep) {
		Collection<Node> c = new HashSet<Node>();
		Label arrayLabel = new Label("input: " + sortingStep.getFromArrayListMap("input") != null ? sortingStep.getFromArrayListMap("input").stream()
				.map(e -> e.toString()).collect(Collectors.joining(" | ")) : "nicht gefunden");
		Label groesseLabel = new Label("groesse: " + (sortingStep.getFromIndexMap("groesse") != null ? sortingStep.getFromIndexMap("groesse").toString() : "nicht gefunden"));
		
		c.add(arrayLabel);
		c.add(groesseLabel);
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
		texLegend += addText("Legende: Der Zeiger groesse gibt das logische Ende des Feldes an", TextLineOptions.TEXT_NEW_LINE, TextFontOptions.TEXT);
		return texLegend;
	}
	
}
