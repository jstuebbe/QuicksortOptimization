package de.wuu.Quicksort.Sorting;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Test;

import de.wwu.Quicksort.SortingStep;
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
 * Tests im Bereich der Sortierverfahren
 * @author Jonas Stübbe im Zuge der BA
 *
 */
public class SorterTest {

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

//	@Disabled
	@RepeatedTest(100)
	public void testSortersSorting() {	

		int randomPosNumber = 0;
		while(randomPosNumber <= 0) {
			Random rand = new Random();
			randomPosNumber = rand.nextInt(100);
		}
		int size = randomPosNumber;

		integerSorters.forEach(sorter -> {
			ArrayList<Integer> randIntegerArrayList = TestUtils.getIntegerArrayList(size);
			ArrayList<Integer> sortedIntegerArrayList = new ArrayList<Integer>(randIntegerArrayList);
			TestUtils.sort(sortedIntegerArrayList);
			sorter.sort(randIntegerArrayList, new ArrayList<SortingStep<Integer>>());
			assertEquals(sortedIntegerArrayList, randIntegerArrayList);
		});

		stringSorters.forEach(sorter -> {
			ArrayList<String> randStringArrayList = TestUtils.getStringArrayList(size);
			ArrayList<String> sortedStringArrayList = new ArrayList<String>(randStringArrayList);
			TestUtils.sort(sortedStringArrayList);
			sorter.sort(randStringArrayList, new ArrayList<SortingStep<String>>());
			assertEquals(sortedStringArrayList, randStringArrayList);
		});

	}

//	@Disabled
	@Test
	public void testSortersWrongInput() {	

		List<SortingStep<Integer>> sortingSteps = new ArrayList<SortingStep<Integer>>();

		integerSorters.forEach(sorter -> {
			ArrayList<Integer> emptyArrayList = TestUtils.getIntegerArrayList(0);	
			assertThrows(IllegalArgumentException.class, () -> {
				sorter.sort(emptyArrayList, sortingSteps);
			});
			assertThrows(IllegalArgumentException.class, () -> {
				sorter.sort(null, sortingSteps);
			});
			ArrayList<Integer> randArrayList = TestUtils.getIntegerArrayList(1);
			assertThrows(IllegalArgumentException.class, () -> {
				sorter.sort(randArrayList, null);
			});
			assertThrows(IllegalArgumentException.class, () -> {
				sorter.sort(null, null);
			});
		});
	}
}
