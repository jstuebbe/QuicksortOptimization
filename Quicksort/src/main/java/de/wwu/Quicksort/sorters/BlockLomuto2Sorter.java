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
import javafx.util.Pair;

/**
 * Implementation des Dual-Pivot BlockLomuto-Sortierverfahrens. Für weitere Details sei auf die begleitende Arbeit verwiesen (https://github.com/jstuebbe/QuicksortOptimization)
 * @author Jonas Stübbe im Zuge der BA
 *
 * @param <I>
 */
public class BlockLomuto2Sorter<I extends Comparable<I>> extends TexStringGenerator<I> {
	public final int B = 2;

	@Override
	public void sort(ArrayList<I> input, List<SortingStep<I>> sortingSteps) {
		if(sortingSteps == null) {
			throw new IllegalArgumentException("sortingSteps müssen definiert sein!");
		}
		if(input == null || input.size() == 0) {
			throw new IllegalArgumentException("Kein/ungueltiges Feld.");
		}
		blockLomuto2(input, 0, input.size() - 1, sortingSteps);
	}

	public void blockLomuto2(ArrayList<I> input, int l, int r,List<SortingStep<I>> sortingSteps) {
		if(l < r) {
			choosePivot(input,l,r,sortingSteps);

			Pair<Integer,Integer> cuts = blockPartition(input, l, r,sortingSteps);
			blockLomuto2(input,l,cuts.getKey() - 1,sortingSteps);
			blockLomuto2(input, cuts.getKey() + 1, cuts.getValue() - 1 ,sortingSteps);
			blockLomuto2(input, cuts.getValue() + 1, r,sortingSteps);
		}
	}
	public void choosePivot(ArrayList<I> input, int l, int r,List<SortingStep<I>> sortingSteps) {
	}

	public Pair<Integer,Integer> blockPartition(ArrayList<I> input, int l, int r,List<SortingStep<I>> sortingSteps) {
		if(input.get(l).compareTo(input.get(r)) > 0) {

			saveSortingStep(sortingSteps, input, l, r, l, r, B, null, null, null, null, null, null, null);
			Utils.swap(input, l, r);
		}
		I p = input.get(l);
		I q = input.get(r);
		Integer[] block = new Integer[B];
		int i = l+1,j = l+1,k = l+1;
		int numP = 0, numQ = 0;

		saveSortingStep(sortingSteps, input, l, r, l, r, B, i, j, k, numQ, j, numP, block);

		while(k < r) {
			int t = Math.min(B, r - k);
			for(int c = 0; c < t; c++) {
				block[numQ] = c;
				numQ += (q.compareTo(input.get(k+c)) >= 0) ? 1 : 0;
				saveSortingStep(sortingSteps, input, l, r, l, r, B, i, j, k+c, numQ, j, null, block);
			}
			for(int c = 0; c < numQ; c++) {
				Utils.swap(input, j+c, k+block[c]);
				saveSortingStep(sortingSteps, input, l, r, l, r, B, i, j, k+t, numQ, j, null, block);

			}
			k += t;
			for(int c = 0; c < numQ; c++) {
				block[numP] = c;
				numP += (p.compareTo(input.get(j+c)) > 0) ? 1 : 0;
				saveSortingStep(sortingSteps, input, l, r, l, r, B, i, j+c, k, null, j, numP, block);

			}
			for(int c = 0; c < numP; c++) {
				Utils.swap(input, i, j+block[c]);
				i++;
				saveSortingStep(sortingSteps, input, l, r, l, r, B, i, j+numQ, k, null, j, numP, block);

			}
			j += numQ;
			numP = 0;
			numQ = 0;
			saveSortingStep(sortingSteps, input, l, r, l, r, B, i, j, k, numQ, j, numP, block);
		}
		Utils.swap(input, i-1, l);
		saveSortingStep(sortingSteps, input, l, r, l, r, B, i, j, k, numQ, j, numP, block);
		Utils.swap(input, j, r);
		saveSortingStep(sortingSteps, input, l, r, l, r, B, i, j, k, numQ, j, numP, block);

		return new Pair<Integer,Integer>(i-1,j);
	}

	public void saveSortingStep(List<SortingStep<I>> sortingSteps, ArrayList<I> input, Integer l, Integer r,
			Integer pivot1, Integer pivot2, Integer blocksize,Integer i, Integer j, Integer k,
			Integer numQ, Integer blocksizeOffsetL, Integer numP,Integer[] block) {
		SortingStep<I> step = new SortingStep<I>();

		if(input == null) {
			//TODO Exception
		}
		step.addToArrayListMap("input", new ArrayList<I>(input));
		if(block != null) step.addToIndexArrayMap("puffer", block.clone());
		if(l != null) step.addToIndexMap("l", l);
		if(r != null) step.addToIndexMap("r", r);
		if(pivot1 != null) step.addToIndexMap("pivot1", l);
		if(pivot2 != null) step.addToIndexMap("pivot2", r);
		if(blocksize != null) step.addToIndexMap("blocksize", B);

		if(i != null) step.addToIndexMap("i", i);
		if(j != null) step.addToIndexMap("j", j);
		if(k != null) step.addToIndexMap("k", k);

		if(numQ != null) step.addToIndexMap("numQ", numQ);
		if(blocksizeOffsetL != null) step.addToIndexMap("blocksizeOffsetL", blocksizeOffsetL);
		if(numP != null) step.addToIndexMap("numP", numP);

		sortingSteps.add(step);
	}

	@Override
	public String getTexString(ArrayList<SortingStep<I>> sortingSteps) {
		String[] input;
		String[] puffer;
		Integer numP;
		Integer numQ;
		Integer l;
		Integer r;
		Integer i;
		Integer j;
		Integer k;
		Integer pivot1;
		Integer pivot2;
		String texString = getTexIntro() + getLegend();
		Integer blocksizeOffsetL;
		int size = sortingSteps.get(0).getFromArrayListMap("input").size();
		int blocksize = sortingSteps.get(0).getFromIndexMap("blocksize");
		for(SortingStep<I> sortingStep : sortingSteps) {
			input = sortingStep.getFromArrayListMap("input").stream().map(s -> (s != null ? s.toString() : "-")).toArray(String[]::new);

			puffer = sortingStep.getFromIndexArrayMap("puffer") != null ? Arrays.stream(sortingStep.getFromIndexArrayMap("puffer"))
					.map(s -> s!= null ? s.toString() : "-").toArray(String[]::new) : null;

			l = sortingStep.getFromIndexMap("l");
			r = sortingStep.getFromIndexMap("r");
			i = sortingStep.getFromIndexMap("i");
			j = sortingStep.getFromIndexMap("j");
			k = sortingStep.getFromIndexMap("k");
			pivot1 = sortingStep.getFromIndexMap("pivot1");
			pivot2 = sortingStep.getFromIndexMap("pivot2");
			numP = sortingStep.getFromIndexMap("numP");
			numQ = sortingStep.getFromIndexMap("numQ");
			blocksizeOffsetL = sortingStep.getFromIndexMap("blocksizeOffsetL");

			List<Integer> pointerIndex = new ArrayList<Integer>();
			List<String> pointerText = new ArrayList<String>();
			List<String> pointerDownarrow = new ArrayList<String>();

			if(l != null) {
				pointerIndex.add(l);
				pointerText.add("l");
				pointerDownarrow.add("$\\downarrow$");
			}
			if(r != null) {
				pointerIndex.add(r);
				pointerText.add("r");
				pointerDownarrow.add("$\\downarrow$");
			}
			if(i != null) {
				pointerIndex.add(i);
				pointerText.add("i");
				pointerDownarrow.add("$\\downarrow$");
			}
			if(j != null) {
				pointerIndex.add(j);
				pointerText.add("j");
				pointerDownarrow.add("$\\downarrow$");
			}	
			if(k != null) {
				pointerIndex.add(k);
				pointerText.add("k");
				pointerDownarrow.add("$\\downarrow$");
			}	

			ArrayList<Integer> seperators = null;
			if(r != null && blocksizeOffsetL != null && blocksizeOffsetL < r) {
				seperators = new ArrayList<>();
				seperators.add(blocksizeOffsetL);
				int B = blocksize;
				B = blocksizeOffsetL + blocksize >= r ? r - blocksizeOffsetL : blocksize;
				seperators.add(blocksizeOffsetL + B);
			}
			texString += beginTable(size, seperators);
			texString += getPointers(pointerIndex,pointerText,size,true); // l,r
			texString += getPointers(pointerIndex,pointerDownarrow,size,true); // downarrow
			ArrayList<Integer> pivots = new ArrayList<Integer>();
			pivots.add(pivot1);
			pivots.add(pivot2);
			if(r!= null && blocksizeOffsetL != null && blocksizeOffsetL < r) {
				texString += getArray(input, pivots,blocksize,blocksizeOffsetL,null);	
			}
			else {
				texString += getArray(input,pivots,null);
			}



			if(puffer != null){
				texString += seperateTable();

				List<Integer> pufferIndex = new ArrayList<Integer>();
				for(int z=0;z<blocksize;z++) {
					pufferIndex.add(z);
				}

				List<String> pufferText = new ArrayList<String>();
				for(int z=0;z<blocksize;z++) {
					pufferText.add(puffer[z]); //kann nicht null sein
				}


				List<Integer> offsetIndex = new ArrayList<Integer>();
				List<String> offsetText = new ArrayList<String>();
				pointerDownarrow = new ArrayList<String>();
				if(numP != null) {
					offsetIndex.add(numP);
					//					offsetText.add("num$_P$");
					offsetText.add("n$_P$");
					pointerDownarrow.add("$\\downarrow$");
				}
				if(numQ != null) {
					offsetIndex.add(numQ);
					//					offsetText.add("num$_Q$");
					offsetText.add("n$_Q$");
					pointerDownarrow.add("$\\downarrow$");
				}	

				texString += getPointers(offsetIndex,offsetText,size,true);
				texString += getPointers(offsetIndex,pointerDownarrow,size,true);
				texString += getCline(size,blocksize,true);
				texString += getPointers(pufferIndex,pufferText,size,false);
				texString += getCline(size,blocksize,true);
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
		Label pufferLabel = new Label("puffer: " + (sortingStep.getFromIndexArrayMap("puffer") != null ? Arrays.stream(sortingStep.getFromIndexArrayMap("puffer"))
				.map(e -> e != null ? e.toString() : "").collect(Collectors.joining(" | ")) : "nicht gefunden"));


		Label lLabel =
				new Label("l: " + (sortingStep.getFromIndexMap("l") != null ? sortingStep.getFromIndexMap("l").toString() : "nicht gefunden"));
		Label rLabel =
				new Label("r: " + (sortingStep.getFromIndexMap("r") != null ? sortingStep.getFromIndexMap("r").toString() : "nicht gefunden"));
		Label iLabel =
				new Label("i: " + (sortingStep.getFromIndexMap("i") != null ? sortingStep.getFromIndexMap("i").toString() : "nicht gefunden"));
		Label jLabel =
				new Label("j: " + (sortingStep.getFromIndexMap("j") != null ? sortingStep.getFromIndexMap("j").toString() : "nicht gefunden"));
		Label kLabel =
				new Label("k: " + (sortingStep.getFromIndexMap("k") != null ? sortingStep.getFromIndexMap("k").toString() : "nicht gefunden"));
		Label pivotLabel =
				new Label("pivot: " + (sortingStep.getFromIndexMap("pivot") != null ? sortingStep.getFromIndexMap("pivot").toString() : "nicht gefunden"));


		Label pufferNumPLabel =
				new Label("numP: " + (sortingStep.getFromIndexMap("numP") != null ? sortingStep.getFromIndexMap("numP").toString() : "nicht gefunden"));
		Label pufferNumQLabel =
				new Label("numQ: " + (sortingStep.getFromIndexMap("numQ") != null ? sortingStep.getFromIndexMap("numQ").toString() : "nicht gefunden"));

		c.add(inputLabel);
		c.add(pufferLabel);
		c.add(lLabel);
		c.add(rLabel);
		c.add(iLabel);
		c.add(jLabel);
		c.add(kLabel);
		c.add(pivotLabel);
		c.add(pufferNumPLabel);
		c.add(pufferNumQLabel);
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
		texLegend += addText("Legende: Die aktuellen\n" + 
				"Pivot-Elemente sind grau hervorgehoben. Die klassifizierten Bereiche werden\n" + 
				"durch die Zeiger i, j und k begrenzt. Des Weiteren geben die Zeiger l und r die\n" + 
				"aktuellen Bereiche an und der aktuelle Pufferbereich ist gelb hervorgehoben. Ferner\n" + 
				"gibt der Zeiger $n_Q$ an, wie viele Elemente im Pufferbereich gefunden wurden, welche\n" + 
				"einen kleineren Wert als das rechte (größere) Pivot-Element haben, während der\n" + 
				"Zeiger $n_P$ angibt, wie viele Elemente von den durch $n_Q$ angegebenen Elementen einen\n" + 
				"kleineren Wert als das linke (kleinere) Pivot-Element haben.", TextLineOptions.TEXT_NEW_LINE, TextFontOptions.TEXT);
		return texLegend;
	}

}
