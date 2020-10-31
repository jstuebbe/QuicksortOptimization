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
 * Implementation des BlockLomuto-Sortierverfahrens. Für weitere Details sei auf die begleitende Arbeit verwiesen (https://github.com/jstuebbe/QuicksortOptimization)
 * @author Jonas Stübbe im Zuge der BA
 *
 * @param <I>
 */
public class BlockLomutoSorter<I extends Comparable<I>> extends TexStringGenerator<I> {
	final int B = 2;
	@Override
	public void sort(ArrayList<I> input, List<SortingStep<I>> sortingSteps) {
		if(sortingSteps == null) {
			throw new IllegalArgumentException("sortingSteps müssen definiert sein!");
		}
		if(input == null || input.size() == 0) {
			throw new IllegalArgumentException("Kein/ungueltiges Feld.");
		}
		blockLomuto(input, 0, input.size() - 1, sortingSteps);
	}

	public void blockLomuto(ArrayList<I> input, int l, int r,List<SortingStep<I>> sortingSteps) {
		if(l < r) {
			choosePivot(input,l,r,sortingSteps);

			int cut = blockPartition(input, l, r,sortingSteps);
			blockLomuto(input,l,cut - 1,sortingSteps);
			blockLomuto(input, cut + 1, r,sortingSteps);
		}
	}

	public void choosePivot(ArrayList<I> input, int l, int r,List<SortingStep<I>> sortingSteps) {
	}

	public int blockPartition(ArrayList<I> input, int l, int r,List<SortingStep<I>> sortingSteps) {
		I pivot = input.get(r);
		Integer[] block = new Integer[B];
		int i = l;
		int j = l;
		int num = 0;


		saveSortingStep(sortingSteps, input, block, l, r, r, num, i, j, B, j);
		while(r - j > 0) {
			int t = Math.min(B, r - j);
			for(int c = 0; c < t; c++) {
				block[num] = c;
				num += (pivot.compareTo(input.get(j + c)) > 0) ? 1 : 0;
				saveSortingStep(sortingSteps, input, block, l, r, r, num, i, j+c, B, j);
			}
			for(int c = 0; c < num; c++) {
				Utils.swap(input,i,j + block[c]);
				i++;
				saveSortingStep(sortingSteps, input, block, l, r, r, num, i, j+t-1, B, j);

			}
			num = 0;
			j = j + t;
			saveSortingStep(sortingSteps, input, block, l, r, r, num, i, j, B, j);
		}
		Utils.swap(input,i,r);
		saveSortingStep(sortingSteps, input, block, l, r, r, num, i, j, B, j);
		return i;
	}

	@Override
	public String getTexString(ArrayList<SortingStep<I>> sortingSteps) {
		String[] input;
		String[] puffer;
		Integer num;
		Integer l;
		Integer r;
		Integer i;
		Integer j;
		Integer blocksizeOffsetL;
		Integer pivot;
		String texString = getTexIntro() + getLegend();
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
			pivot = sortingStep.getFromIndexMap("pivot");
			num = sortingStep.getFromIndexMap("num");
			blocksizeOffsetL = sortingStep.getFromIndexMap("blocksizeOffsetL");


			List<Integer> pointerIndex = new ArrayList<Integer>();
			pointerIndex.add(l);
			pointerIndex.add(r);
			pointerIndex.add(i);
			pointerIndex.add(j);
			List<String> pointerText = new ArrayList<String>();
			pointerText.add("l");
			pointerText.add("r");
			pointerText.add("i");
			pointerText.add("j");
			List<String> pointerDownarrow = new ArrayList<String>();
			pointerDownarrow.add("$\\downarrow$");
			pointerDownarrow.add("$\\downarrow$");
			pointerDownarrow.add("$\\downarrow$");
			pointerDownarrow.add("$\\downarrow$");

			ArrayList<Integer> seperators = new ArrayList<>();
			seperators.add(blocksizeOffsetL);
			seperators.add(blocksizeOffsetL + blocksize);
			texString += beginTable(size, seperators);

			texString += getPointers(pointerIndex,pointerText,size,true); // l,r
			texString += getPointers(pointerIndex,pointerDownarrow,size,true); // downarrow
			ArrayList<Integer> pivots = new ArrayList<Integer>();
			pivots.add(pivot);
			texString += getArray(input, pivots,blocksize,blocksizeOffsetL,null);


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
				offsetIndex.add(num);
				List<String> offsetText = new ArrayList<String>();
				offsetText.add("num");

				pointerDownarrow = new ArrayList<String>();
				pointerDownarrow.add("$\\downarrow$");

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
				.map(e -> e.toString()).collect(Collectors.joining(" | "))) : "nicht gefunden")) ;
		Label pufferLabel = new Label("pufer: " + (sortingStep.getFromIndexArrayMap("puffer") != null ? Arrays.stream(sortingStep.getFromIndexArrayMap("puffer"))
				.map(e -> e != null ? e.toString() : "").collect(Collectors.joining(" | ")) : "nicht gefunden"));


		Label lLabel = new Label("l: " + (sortingStep.getFromIndexMap("l") != null ? sortingStep.getFromIndexMap("l").toString() : "nicht gefunden"));
		Label rLabel = new Label("r: " + (sortingStep.getFromIndexMap("r") != null ? sortingStep.getFromIndexMap("r").toString() : "nicht gefunden"));
		Label pivotLabel = new Label("pivot: " + (sortingStep.getFromIndexMap("pivot") != null ? sortingStep.getFromIndexMap("pivot").toString() : "nicht gefunden"));

		Label pufferNumLabel = new Label("num: " + (sortingStep.getFromIndexMap("num") != null ? sortingStep.getFromIndexMap("num").toString() : "nicht gefunden"));

		c.add(inputLabel);
		c.add(pufferLabel);
		c.add(lLabel);
		c.add(rLabel);
		c.add(pivotLabel);
		c.add(pufferNumLabel);
		return c;
	}


	public void saveSortingStep(List<SortingStep<I>> sortingSteps, ArrayList<I> input, Integer[] block, Integer l,Integer r,
			Integer pivot,Integer num, Integer i, Integer j, Integer blocksize,Integer blocksizeOffsetL) {
		SortingStep<I> step = new SortingStep<I>();
		step.addToArrayListMap("input", new ArrayList<I>(input));
		step.addToIndexArrayMap("puffer",block.clone());
		step.addToIndexMap("l", l);
		step.addToIndexMap("r", r);
		step.addToIndexMap("i", i);
		step.addToIndexMap("j", j);
		step.addToIndexMap("pivot", r);
		step.addToIndexMap("num", num);
		step.addToIndexMap("blocksize", B);
		step.addToIndexMap("blocksizeOffsetL", blocksizeOffsetL);
		sortingSteps.add(step);
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
		texLegend += addText("Legende: Das aktuelle Pivot-\n" + 
				"Element ist grau hervorgehoben. Die klassifizierten Bereiche werden durch die Zeiger\n" + 
				"i und j begrenzt. Des Weiteren geben die Zeiger l und r die aktuellen Bereiche an,\n" + 
				"der aktuelle Pufferbereich ist gelb hervorgehoben und der Zeiger num gibt an, wie\n" + 
				"viele Elemente im Pufferbereich gefunden wurden, welche einen kleineren Wert als\n" + 
				"das Pivot-Element haben.", TextLineOptions.TEXT_NEW_LINE, TextFontOptions.TEXT);
		return texLegend;
	}
}
