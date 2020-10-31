package de.wwu.Quicksort.latex;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
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
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

/**
 * Tests im Bereich der Kompilierung durch LaTeX
 * @author Jonas Stübbe im Zuge der BA
 *
 */
public class latexTest {
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

	/**
	 * Dieser Test ist standardmäßig augeschaltet! Falls Sie diesen Test benutzen wollen müssen Sie ihm TexReporter (Zeile 136,137)
	 * den Pfad ihrer pdflatex Installation manuell eingeben!
	 */
	@Disabled
	@Test
	public void testSortersLatex() {	

		stringSorters.forEach(sorter -> {
			ArrayList<SortingStep<String>> sortingSteps = new ArrayList<SortingStep<String>>();
//			ArrayList<String> randStringArrayListLimited = TestUtils.getStringArrayListLimitedCharacters(20,1);
			//Zur Zeit nur Strings der Länge 1 erlaubt.
			ArrayList<String> randStringArrayListLimited = new ArrayList<String>();
			ObservableList<String> items = FXCollections.observableArrayList();
			for(int i = 0; i < 20; i++) {
				items.add(""+TestUtils.getRndChar());
			}
			randStringArrayListLimited.addAll(items);
			
			sorter.sort(randStringArrayListLimited, sortingSteps);
			try {
				TexReporter reporter = new TexReporter("test");
				reporter.setDev();
				String texString = sorter.getTexString(sortingSteps);
				reporter.getReport(texString,"test","test");
			} catch (InvalidResourceFileException | InvalidPdflatexPathException e) {
				e.printStackTrace();
			}
		});

		integerSorters.forEach(sorter -> {
			ArrayList<SortingStep<Integer>> sortingSteps = new ArrayList<SortingStep<Integer>>();
			ArrayList<Integer> randIntegerArrayListLimited = TestUtils.getIntegerArrayListLimitedCharacters(40,3);
			sorter.sort(randIntegerArrayListLimited, sortingSteps);
			try {
				TexReporter reporter = new TexReporter("test");
				reporter.setDev();
				String texString = sorter.getTexString(sortingSteps);
				reporter.getReport(texString,"test","test");
			} catch (InvalidResourceFileException | InvalidPdflatexPathException e) {
				e.printStackTrace();
			}
		});
	}
	
	/**
	 * Dieser Test ist standardmäßig augeschaltet! Falls Sie diesen Test benutzen wollen müssen Sie ihm TexReporter (Zeile 136,137)
	 * den Pfad ihrer pdflatex Installation manuell eingeben!
	 */
	@Disabled
	@Test
	public void emptyTexString() {
		try {
			TexReporter reporter = new TexReporter("test");
			reporter.setDev();
			String texString = "";
			reporter.getReport(texString,"test","test");
		} catch (InvalidResourceFileException | InvalidPdflatexPathException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * TODO
	 * Es ist bisher leider nicht möglich falsche TeX-Strings abzufangen / zu testen.
	 * @throws Exception
	 */
	@Disabled
	@Test
	public void badTexString() throws Exception{
		
	}
	
	/**
	 * TODO
	 * Es ist bisher leider nicht möglich falsche TeX-Strings abzufangen / zu testen damit auch nicht das Template.
	 * Wir können nur Testen ob das Template richtig ist (s.o.)
	 * @throws Exception
	 */
	@Disabled
	@Test
	public void badTemplate() throws Exception{
		
	}
	
	
	/**
	 * TODO Test: Template wurde gelöscht / nicht gefunden.
	 * Es macht bisher leider keinen Sinn danach zu testen
	 * (es würde gehen, siehe InvalidResourceFileException),
	 * da wir keine Möglichkeit haben automatisch ein neues Template zu erstellen
	 * (auch möglich, kann zukünftige Erweiterung werden) 
	 * @throws Exception
	 */
	@Disabled
	@Test
	public void missingTemplate() throws Exception{
		
	}


}
