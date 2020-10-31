package de.wwu.Quicksort;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import de.wwu.Quicksort.sorters.Sorter;
import javafx.scene.Node;

/**
 * Dient als Model in einer abgewandelten MVC-Struktur. Speichert die Sortierschritte, das dazugehoerige Sortierverfahren und die Eingabesequenz.
 * @author Jonas Stübbe im Zuge der BA
 *
 * @param <I>
 */
public class SortingState<I> {

	//Liste der Sortierschritte
	private ArrayList<SortingStep<I>> sortingSteps;
	//Sortierverfahren
	private Sorter<I> sorter;
	//Eingabesequenz
	private ArrayList<I> arrayList;
	
	private String dataType;

	public SortingState(ArrayList<SortingStep<I>> sortingSteps, Sorter<I> sorter) {
		this.sortingSteps = sortingSteps;
		this.sorter = sorter;
	}

	public ArrayList<SortingStep<I>> getSortingSteps(){
		return sortingSteps;
	}

	public Sorter<I> getSorter(){
		return sorter;
	}

	public ArrayList<I> getArrayList(){
		return arrayList;
	}

	public void sort() {
		if(arrayList != null) {
			sorter.sort(arrayList, sortingSteps);
		}
		else {
			//TODO Exception, neue Exception oder existierende?
		}
	}

	public Collection<Node> getViewElements(int index) {
		return sorter.getViewElements(sortingSteps.get(index));
	}

	public String getTexString() {
		return sorter.getTexString(sortingSteps);
	}

	public String getDataType() {
		return this.dataType;
	}

	/**
	 * Wandelt die, mit Kommata getrennten Elemente, eines Strings in eine Liste um.
	 * Dabei werden die Elemente von Strings in den richtigen Datentyp gecastet. 
	 * Vorraussetzung ist eine vorherige hinreichende Prüfung der Eingabe durch den Controller.
	 * @param arrayString Der Eingabesequenz String, bestehend aus, mit Kommata getrennten, Elementen 
	 * @param dataType Der Datentyp in den die Elemente im String gecastet werden.
	 */
	@SuppressWarnings("unchecked")
	public void setArray(String arrayString, String dataType) {
		Objects.requireNonNull(arrayString);
		Objects.requireNonNull(dataType);

		this.dataType = dataType;
		List<I> list = null;

		switch(dataType) {
		case "Integer":
			//in diesem Fall ist I = Integer und ein Integer Array 
			//gecastet zu einem Integer[] ist keine Veränderung.
			//Weiter ist durch die Methode checkInputForDatatype im Controller sichergestellt,
			//dass jedes Element e in einen Integer gecastet werden kann.
			//Daher können wir diese Warung vom Compiler unterdrücken.
			list = (List<I>) Arrays.stream(arrayString.split(","))
			.map(e -> Integer.parseInt(e))
			.collect(Collectors.toList());
			break;
		case "String":
			//in diesem Fall ist I = String und ein String Array 
			//gecastet zu String[] ist keine Veränderung.
			//Weiter ist durch die Methode checkInputForDatatype im Controller sichergestellt,
			//dass jedes Element e in einen String gecastet werden kann.
			//Daher können wir diese Warung vom Compiler unterdrücken.
			list = (List<I>) Arrays.stream(arrayString.split(","))
			.map(e -> e.toString())
			.collect(Collectors.toList()); 
			break;
		default:
			throw new IllegalArgumentException("Unbekannter Datentyp");
		}
		if(list != null) this.arrayList = new ArrayList<I>(list);

	}	
}
