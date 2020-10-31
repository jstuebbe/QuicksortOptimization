package de.wwu.Quicksort.sorters;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;

import de.wwu.Quicksort.SortingStep;
import de.wwu.Quicksort.utils.Utils;
import javafx.scene.Node;
import javafx.scene.control.Label;

/**
 * Implementation des BlockQuicksort-Sortierverfahrens. Für weitere Details sei auf die begleitende Arbeit verwiesen (https://github.com/jstuebbe/QuicksortOptimization)
 * @author Jonas Stübbe im Zuge der BA
 *
 * @param <I>
 */
public class BlockQuicksortSorter<I extends Comparable<I>> extends TexStringGenerator<I>{

	@Override
	public void sort(ArrayList<I> input, List<SortingStep<I>> sortingSteps) {
		if(sortingSteps == null) {
			throw new IllegalArgumentException("sortingSteps müssen definiert sein!");
		}
		if(input == null || input.size() == 0) {
			throw new IllegalArgumentException("Kein/ungueltiges Feld.");
		}
		blockQuicksort(input, 0, input.size() - 1, sortingSteps);
	}

	public void blockQuicksort(ArrayList<I> input, int l, int r,List<SortingStep<I>> sortingSteps) {
		if(l < r) {
			int blocksize = 2;
			//choosePivot(input,l,r,sortingSteps);		

			int cut = hoare_block_partition_simple(input,l, r,blocksize,r, sortingSteps);
			blockQuicksort(input,l,cut - 1,sortingSteps);
			blockQuicksort(input, cut + 1, r,sortingSteps);
		}
	}

	/**
	 * Code von Edelkamp Weiß BlockQuicksort Paper
	 */
	public int hoare_block_partition_simple(ArrayList<I> input, int start, int end, int Blocksize,
			int pivotPosition,List<SortingStep<I>> sortingSteps) {
		//für sortingSteps
		int blocksizeOffsetL = 0, blocksizeOffsetR = 0;


		Integer[] pufferL = new Integer[Blocksize];
		Integer[] pufferR = new Integer[Blocksize];

		I pivot = input.get(pivotPosition);

		int l = start;
		int r = end;
		r--;

		int num_left = 0; //anzahl der elemente im linken puffer
		int num_right = 0; //anzahl der elemente im rechten puffer
		int start_left = 0; //pointer im linken puffer
		int start_right = 0; //pointer im rechten puffer
		int num; //minimale Anzahl der Elemente in beiden Puffern



		saveSortingStep(sortingSteps, input, l, r, r+1,
				pufferL, pufferR, start_left, start_right,Blocksize,blocksizeOffsetL,blocksizeOffsetR);
		//solange nicht partitionierten Elemente größer als 2 mal die Blocksize sind
		while(r - l + 1 > 2 * Blocksize) {
			saveSortingStep(sortingSteps, input, l, r, r+1,
					pufferL, pufferR, start_left, start_right,Blocksize,blocksizeOffsetL,blocksizeOffsetR);
			//vergleiche und füge in die puffer ein
			if(num_left == 0) {
				//puffer ist leer, nachfüllen
				start_left = 0;
				for (int j = 0; j < Blocksize; j++) {
					pufferL[num_left] = j;
					num_left += (input.get(l + j).compareTo(pivot) > 0) ? 1 : 0;
					saveSortingStep(sortingSteps,input, l+j, r, r+1,
							pufferL, pufferR, start_left, start_right,null,blocksizeOffsetL,blocksizeOffsetR);
				}
			}

			if(num_right == 0) {
				start_right = 0;
				for (int j = 0; j < Blocksize; j++) {
					pufferR[num_right] = j;
					num_right += (pivot.compareTo(input.get(r - j)) > 0) ? 1 : 0;	
					saveSortingStep(sortingSteps,input, l+Blocksize-1, r-j, r-j+1,
							pufferL, pufferR, start_left, start_right,null,blocksizeOffsetL,blocksizeOffsetR);
				}
			}

			saveSortingStep(sortingSteps,input, l, r, r+1,pufferL, pufferR, start_left, start_right, null, blocksizeOffsetL, blocksizeOffsetR);


			//bewege Elemente
			num = Math.min(num_left, num_right);
			for (int j = 0; j < num; j++) {
				Utils.swap(input, l + pufferL[start_left + j], r - pufferR[start_right + j]);
				saveSortingStep(sortingSteps, input, l+Blocksize-1, r-Blocksize+1,r-Blocksize+2,
						pufferL, pufferR, start_left, start_right,null,blocksizeOffsetL,blocksizeOffsetR);
			}




			num_left -= num;
			num_right -= num;
			start_left += num;
			start_right += num;
			l += (num_left == 0) ? Blocksize : 0;
			r -= (num_right == 0) ? Blocksize : 0;

			blocksizeOffsetL += (num_left == 0) ? Blocksize : 0;
			blocksizeOffsetL += (num_right == 0) ? Blocksize : 0;

			saveSortingStep(sortingSteps, input, l, r, r+1,pufferL, pufferR, start_left, start_right, null, blocksizeOffsetL, blocksizeOffsetR);
		}



		//Compare and store in buffers final iteration
		int shiftR = 0, shiftL = 0;
		if (num_right == 0 && num_left == 0) {	//for small arrays or in the unlikely case that both buffers are empty
			shiftL = ((r - l) + 1) / 2;
			shiftR = (r - l) + 1 - shiftL;
			assert(shiftL >= 0); assert(shiftL <= Blocksize);
			assert(shiftR >= 0); assert(shiftR <= Blocksize);
			start_left = 0; start_right = 0;
			for (int j = 0; j < shiftL; j++) {
				pufferL[num_left] = j;
				num_left += (input.get(l+j).compareTo(pivot) > 0) ? 1 : 0;
				pufferR[num_right] = j;
				num_right += (pivot.compareTo(input.get(r - j)) > 0) ? 1 : 0;
			}
			if (shiftL < shiftR)
			{
				assert(shiftL + 1 == shiftR);
				pufferR[num_right] = shiftR - 1;
				num_right += (pivot.compareTo(input.get(r - shiftR + 1)) > 0) ? 1 : 0;
			}
		}
		else if (num_right != 0) {
			shiftL = (r - l) - Blocksize + 1;
			shiftR = Blocksize;
			assert(shiftL >= 0); assert(shiftL <= Blocksize); assert(num_left == 0);
			start_left = 0;
			for (int j = 0; j < shiftL; j++) {
				pufferL[num_left] = j;
				num_left += (input.get(l + j).compareTo(pivot) > 0) ? 1 : 0;
			}
		}
		else {
			shiftL = Blocksize;
			shiftR = (r - l) - Blocksize + 1;
			assert(shiftR >= 0); assert(shiftR <= Blocksize); assert(num_right == 0);
			start_right = 0;
			for (int j = 0; j < shiftR; j++) {
				pufferR[num_right] = j;
				num_right += (pivot.compareTo(input.get(r-j)) > 0) ? 1 : 0;
			}
		}

		//rearrange final iteration
		num = Math.min(num_left, num_right);
		for (int j = 0; j < num; j++)
			Utils.swap(input, l + pufferL[start_left + j], r - pufferR[start_right + j]);

		num_left -= num;
		num_right -= num;
		start_left += num;
		start_right += num;
		l += (num_left == 0) ? shiftL : 0;
		r -= (num_right == 0) ? shiftR : 0;			
		//end final iteration


		//rearrange elements remaining in buffer
		if (num_left != 0)
		{

			assert(num_right == 0);
			int lowerI = start_left + num_left - 1;
			int upper = r - l;
			//search first element to be swapped
			while (lowerI >= start_left && pufferL[lowerI] == upper) {
				upper--; lowerI--;
			}
			while (lowerI >= start_left) Utils.swap(input, l+upper--, l + pufferL[lowerI--]);

			Utils.swap(input, pivotPosition, l+upper+1); // fetch the pivot 
			return l + upper + 1;
		}
		else if (num_right != 0) {
			assert(num_left == 0);
			int lowerI = start_right + num_right - 1;
			int upper = r - l;
			//search first element to be swapped
			while (lowerI >= start_right && pufferR[lowerI] == upper) {
				upper--; lowerI--;
			}

			while (lowerI >= start_right)
				Utils.swap(input, r - upper--, r - pufferR[lowerI--]);

			Utils.swap(input, pivotPosition, r-upper); // fetch the pivot 
			return r - upper;
		}
		else { //no remaining elements
			assert(r + 1 == l);
			Utils.swap(input, pivotPosition, l);// fetch the pivot 
			return l;
		}

	}

	public void saveSortingStep(List<SortingStep<I>> sortingSteps, ArrayList<I> input, Integer l, Integer r, Integer pivot,Integer[] pufferL,
			Integer[] pufferR, Integer start_left, Integer start_right, Integer blocksize, int blocksizeOffsetL, int blocksizeOffsetR) {
		SortingStep<I> step = new SortingStep<I>();
		step.addToArrayListMap("input", new ArrayList<I>(input));
		step.addToIndexMap("l", l);
		step.addToIndexMap("r", r);
		if(r != null) step.addToIndexMap("pivot", pivot);
		step.addToIndexArrayMap("pufferL", pufferL.clone());
		step.addToIndexArrayMap("pufferR", pufferR.clone());
		step.addToIndexMap("start_left", start_left);
		step.addToIndexMap("start_right", start_right);
		step.addToIndexMap("blocksize", blocksize);
		step.addToIndexMap("blocksizeOffsetL", blocksizeOffsetL);
		step.addToIndexMap("blocksizeOffsetR", blocksizeOffsetR);
		sortingSteps.add(step);
	}

	@Override
	public String getTexString(ArrayList<SortingStep<I>> sortingSteps) {
		String[] input;
		String[] pufferL;
		String[] pufferR;
		Integer start_left;
		Integer start_right;
		Integer l;
		Integer r;
		Integer blocksizeOffsetL;
		Integer blocksizeOffsetR;
		Integer pivot;
		String texString = getTexIntro() + getLegend();
		int size = sortingSteps.get(0).getFromArrayListMap("input").size();
		int blocksize = sortingSteps.get(0).getFromIndexMap("blocksize");
		for(SortingStep<I> sortingStep : sortingSteps) {
			input = sortingStep.getFromArrayListMap("input").stream().map(s -> s.toString()).toArray(String[]::new);

			pufferL = sortingStep.getFromIndexArrayMap("pufferL") != null ? Arrays.stream(sortingStep.getFromIndexArrayMap("pufferL"))
					.map(s -> s!= null ? s.toString() : "-").toArray(String[]::new) : null;
			pufferR = sortingStep.getFromIndexArrayMap("pufferR") != null ? Arrays.stream(sortingStep.getFromIndexArrayMap("pufferR"))
					.map(s -> s!= null ? s.toString() : "-").toArray(String[]::new) : null;
			l = sortingStep.getFromIndexMap("l");
			r = sortingStep.getFromIndexMap("r");
			pivot = sortingStep.getFromIndexMap("pivot");
			start_right = sortingStep.getFromIndexMap("start_right");
			start_left = sortingStep.getFromIndexMap("start_left");
			blocksizeOffsetL = sortingStep.getFromIndexMap("blocksizeOffsetL");
			blocksizeOffsetR = sortingStep.getFromIndexMap("blocksizeOffsetR");


			List<Integer> pointerIndex = new ArrayList<Integer>();
			pointerIndex.add(l);
			pointerIndex.add(r);
			List<String> pointerText = new ArrayList<String>();
			pointerText.add("l");
			pointerText.add("r");
			List<String> pointerDownarrow = new ArrayList<String>();
			pointerDownarrow.add("$\\downarrow$");
			pointerDownarrow.add("$\\downarrow$");

			texString += beginTable(size, blocksize,blocksizeOffsetL,blocksizeOffsetR);
			texString += getPointers(pointerIndex,pointerText,size,true); // l,r
			texString += getPointers(pointerIndex,pointerDownarrow,size,true); // downarrow
			texString += getArray(input, pivot, blocksize, blocksizeOffsetL,blocksizeOffsetR,null);

			if(pufferL != null && pufferR != null){
				texString += seperateTable();

				List<Integer> pufferIndex = new ArrayList<Integer>();
				for(int i=0;i<blocksize;i++) {
					pufferIndex.add(i);
				}
				for(int i=size - blocksize;i<size;i++) {
					pufferIndex.add(i);
				}
				List<String> pufferText = new ArrayList<String>();
				for(int i=0;i<blocksize;i++) {
					pufferText.add(pufferL[i]); //kann nicht null sein
				}
				for(int i=0;i<blocksize;i++) {
					pufferText.add(pufferR[i]); //kann nicht null sein
				}

				List<Integer> offsetIndex = new ArrayList<Integer>();
				//TODO
				offsetIndex.add(start_left < 0 ? 0 : start_left);
				offsetIndex.add((size - blocksize + start_right) >= size ? size-1 : (size - blocksize + start_right));
				List<String> offsetText = new ArrayList<String>();
				offsetText.add("i$_L$");
				offsetText.add("i$_R$");

				texString += getPointers(offsetIndex,offsetText,size,true);
				texString += getPointers(offsetIndex,pointerDownarrow,size,true);
				texString += getCline(size,blocksize,false);
				texString += getPointers(pufferIndex,pufferText,size,false);
				texString += getCline(size,blocksize,false);
				texString += endPufferTable();

			}
			else {
				texString += endTable();
			}

		}
		return texString;
	}

	@Override
	public Collection<Node> getViewElements(SortingStep<I> sortingStep) {
		Collection<Node> c = new HashSet<Node>();
		Label inputLabel = new Label("input: " + (sortingStep.getFromArrayListMap("input") != null ? (sortingStep.getFromArrayListMap("input").stream()
				.map(e -> e.toString()).collect(Collectors.joining(" | "))) : "nicht gefunden"));
		Label pufferLLabel = new Label("pufferL: " + (sortingStep.getFromIndexArrayMap("pufferL") != null ? Arrays.stream(sortingStep.getFromIndexArrayMap("pufferL"))
				.map(e -> e != null ? e.toString() : "").collect(Collectors.joining(" | ")) : "nicht gefunden"));
		Label pufferRLabel = new Label("pufferR: " + (sortingStep.getFromIndexArrayMap("pufferR") != null ? Arrays.stream(sortingStep.getFromIndexArrayMap("pufferR"))
				.map(e -> e !=null ? e.toString() : "").collect(Collectors.joining(" | ")) : "nicht gefunden"));

		Label lLabel = new Label("l: " + (sortingStep.getFromIndexMap("l") != null ? sortingStep.getFromIndexMap("l").toString() : "nicht gefunden"));
		Label rLabel = new Label("r: " + (sortingStep.getFromIndexMap("r") != null ? sortingStep.getFromIndexMap("r").toString() : "nicht gefunden"));
		Label pivotLabel = new Label("pivot: " + (sortingStep.getFromIndexMap("pivot") != null ? sortingStep.getFromIndexMap("pivot").toString() : "nicht gefunden"));

		Label startRightLabel =
				new Label("startRight: " + (sortingStep.getFromIndexMap("start_left") != null ? sortingStep.getFromIndexMap("start_left").toString() : "nicht gefunden"));
		Label startLeftLabel =
				new Label("startLeft: " + (sortingStep.getFromIndexMap("start_right") != null ? sortingStep.getFromIndexMap("start_right").toString() : "nicht gefunden"));

		c.add(inputLabel);
		c.add(pufferLLabel);
		c.add(pufferRLabel);
		c.add(lLabel);
		c.add(rLabel);
		c.add(pivotLabel);
		c.add(startRightLabel);
		c.add(startLeftLabel);

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
		texLegend += addText("Legende: Das aktuelle Pivot-Element ist grau hervorgehoben.Weiter geben die Zeiger l und r die aktuellen Bereiche an, "
				+ "ferner ist der aktuelle Pufferbereich in gelb und mit Trennlinien hervorgehoben. "
				+ "Die Zeiger $i_L$ und $i_R$ zeigen auf den ersten Index im linken bzw. rechten Puffer, "
				+ "welcher ein noch zu tauschendes Element referenziert.", TextLineOptions.TEXT_NEW_LINE, TextFontOptions.TEXT);
		return texLegend;
	}
}