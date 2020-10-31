package de.wwu.Quicksort;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Ein einzelner Sortierschritt aus dem Ablauf eines Sortierverfahrens.
 * <p>
 * Diese Klasse ist ein Snapshot der notwendigen Variablen eines laufenden Sortierverfahrens an einem Punkt.
 * @author Jonas Stübbe im Zuge der BA
 *
 * @param <I>
 */
public class SortingStep<I> {
	//speichert die Zeiger
	Map<String,Integer> indexMap;
	//peichert Integer Sequenzen, wie zum Beispiel Puffer
	Map<String,Integer[]> indexArrayMap;
	//speichert die Sequenzen des generischen Datentyps I
	Map<String, ArrayList<I>> arrayListMap;

	public SortingStep(){
		indexMap = new HashMap<String,Integer>();
		indexArrayMap = new HashMap<String,Integer[]>(); 
		arrayListMap = new HashMap<String, ArrayList<I>>();
	}

	public Integer addToIndexMap(String key, Integer value) {
		return indexMap.putIfAbsent(key, value);
	}
	public Integer[] addToIndexArrayMap(String key, Integer[] value) {
		return indexArrayMap.putIfAbsent(key, value);
	}
	public ArrayList<I> addToArrayListMap(String key, ArrayList<I> value){
		return arrayListMap.putIfAbsent(key, value);
	}
	public Integer getFromIndexMap(String key) {
		return indexMap.get(key);
	}
	public Integer[] getFromIndexArrayMap(String key) {
		return indexArrayMap.get(key);
	}
	public ArrayList<I> getFromArrayListMap(String key){
		return arrayListMap.get(key);
	}
}
