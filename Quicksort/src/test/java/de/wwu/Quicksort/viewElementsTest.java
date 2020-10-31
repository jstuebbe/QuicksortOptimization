package de.wwu.Quicksort;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import javafx.scene.Node;
import javafx.scene.control.Label;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Disabled;

import de.wwu.Quicksort.sorters.BlockLomuto2Sorter;
import de.wwu.Quicksort.sorters.BlockLomutoSorter;
import de.wwu.Quicksort.sorters.BlockQuicksortSorter;
import de.wwu.Quicksort.sorters.HeapsortSorter;
import de.wwu.Quicksort.sorters.IPS4oSorter;
import de.wwu.Quicksort.sorters.InsertionsortSorter;
import de.wwu.Quicksort.sorters.QuicksortLomutoSorter;
import de.wwu.Quicksort.sorters.QuicksortSorter;
import de.wwu.Quicksort.sorters.S3Sorter;
import de.wwu.Quicksort.sorters.SamplesortSorter;
import de.wwu.Quicksort.sorters.TexStringGenerator;
import de.wwu.Quicksort.utils.TestUtils;

/**
 * Dieser Test steht leider nicht zur Verfügung,
 * um JavaFX Komponenten zu erstellen ist ein weiteres Framework oder
 * eine MOCKING-View benötigt, dies ist im Umfang dieser Arbeit leider nicht möglich gewesen.
 * @author Jonas Stübbe im Zuge der BA
 *
 */
public class viewElementsTest {

	private List<TexStringGenerator<Integer>> integerSorters = new ArrayList<TexStringGenerator<Integer>>();
	private List<TexStringGenerator<String>> stringSorters = new ArrayList<TexStringGenerator<String>>();
	

	@BeforeEach
	public void classSetup() {
		//zu testende Sortierer:
		//InsertionsortSorter,HeapsortSorter, 
		//QuicksortSorter, QuicksortLomutoSorter, 
		//BlockQuicksortSorter, BlockLomutoSorter, BlockLomuto2Sorter,
		//Samplesort, S3Sorter, IPS4oSorter 

		integerSorters.add(new InsertionsortSorter<Integer>());
		integerSorters.add(new HeapsortSorter<Integer>());

		integerSorters.add(new QuicksortSorter<Integer>());
		integerSorters.add(new QuicksortLomutoSorter<Integer>());

		integerSorters.add(new BlockQuicksortSorter<Integer>());
		integerSorters.add(new BlockLomutoSorter<Integer>());
		integerSorters.add(new BlockLomuto2Sorter<Integer>());

		integerSorters.add(new SamplesortSorter<Integer>());
		integerSorters.add(new S3Sorter<Integer>());
		integerSorters.add(new IPS4oSorter<Integer>());

		stringSorters.add(new InsertionsortSorter<String>());
		stringSorters.add(new HeapsortSorter<String>());

		stringSorters.add(new QuicksortSorter<String>());
		stringSorters.add(new QuicksortLomutoSorter<String>());

		stringSorters.add(new BlockQuicksortSorter<String>());
		stringSorters.add(new BlockLomutoSorter<String>());
		stringSorters.add(new BlockLomuto2Sorter<String>());

		stringSorters.add(new SamplesortSorter<String>());
		stringSorters.add(new S3Sorter<String>());
		stringSorters.add(new IPS4oSorter<String>());
	}
	
	@Disabled
	@RepeatedTest(100)
	public void testSortersViewElements() {	
		integerSorters.forEach(sorter -> {
			List<SortingStep<Integer>> sortingSteps = new ArrayList<SortingStep<Integer>>();
			ArrayList<Integer> randIntegerArrayListLimited = TestUtils.getIntegerArrayListLimitedCharacters(40,3);
			sorter.sort(randIntegerArrayListLimited, sortingSteps);
			for(SortingStep<Integer> step : sortingSteps) {
				Collection<Node> nodeCollection = sorter.getViewElements(step);
				nodeCollection.forEach(node -> {
					assertEquals(Label.class, node.getClass());
				});
			}
		});
		
		stringSorters.forEach(sorter -> {
			List<SortingStep<String>> sortingSteps = new ArrayList<SortingStep<String>>();
			ArrayList<String> randStringArrayListLimited = TestUtils.getStringArrayListLimitedCharacters(40,3);
			sorter.sort(randStringArrayListLimited, sortingSteps);
			for(SortingStep<String> step : sortingSteps) {
				Collection<Node> nodeCollection = sorter.getViewElements(step);
				nodeCollection.forEach(node -> {
					assertEquals(Label.class, node.getClass());
				});
			}
		});
		
	}
	
}
