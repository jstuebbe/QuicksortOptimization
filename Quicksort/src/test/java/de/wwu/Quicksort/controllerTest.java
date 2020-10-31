package de.wwu.Quicksort;

import static org.junit.Assert.fail;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Test;

import de.wwu.Quicksort.Controller.SaveOption;
import de.wwu.Quicksort.utils.TestUtils;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.util.Pair;

/**
 * Tests rund um die Methoden des Controllers (SORT und SAVE)
 * @author Jonas Stübbe im Zuge der BA
 *
 */
public class controllerTest {
	
	private List<String> algorithms = new ArrayList<String>();

	private static boolean setUpIsDone = false;

	@BeforeEach
	public void setUp() {
		if (setUpIsDone) {
			return;
		}
		ObservableList<String> algos = FXCollections.observableArrayList();
		algos.addAll(
				"Insertionsort",
				"Heapsort",
				"Quicksort",
				"Lomuto Quicksort",
				"BlockQuicksort",
				"BlockLomuto",
				"Dual-Pivot BlockLomuto",
				"Samplesort",
				"Super Scalar Samplesort",
				"In-Place Parallel Super Scalar Samplesort"
				);
		algorithms.addAll(algos);
		setUpIsDone = true;
	}

//	@Disabled
	@RepeatedTest(100)
	public void testControllerSorting() {	

		int randomPosNumber = 0;
		while(randomPosNumber <= 0) {
			Random rand = new Random();
			randomPosNumber = rand.nextInt(100);
		}
		int size = randomPosNumber;

		Pair<String,ArrayList<Integer>> integerInputStringPair = TestUtils.getIntegerArrayString(size,null);

		algorithms.forEach(e -> {
			Controller controller = Controller.getInstance();
			controller.sort(integerInputStringPair.getKey(), e, "Integer");
			TestUtils.sort(integerInputStringPair.getValue());
			assertEquals(integerInputStringPair.getValue(),controller.getState().getArrayList());
			
			assertThrows(IllegalArgumentException.class,() -> controller.sort(integerInputStringPair.getKey(), e, "TestError"));
			assertThrows(IllegalArgumentException.class,() -> controller.sort(integerInputStringPair.getKey(), "TestError", "Integer"));
		});

		Pair<String,ArrayList<String>> stringinputStringPair = TestUtils.getStringArrayString(size,null);

		algorithms.forEach(e -> {
			Controller controller = Controller.getInstance();
			controller.sort(stringinputStringPair.getKey(), e, "String");
			TestUtils.sort(stringinputStringPair.getValue());
			controller.getState().getArrayList();
			assertEquals(stringinputStringPair.getValue(),controller.getState().getArrayList());
			
			assertThrows(IllegalArgumentException.class,() -> controller.sort(stringinputStringPair.getKey(), e, "TestError"));
			assertThrows(IllegalArgumentException.class,() -> controller.sort(stringinputStringPair.getKey(), "TestError", "String"));
		});

	}
	
//	@Disabled
	@Test
	public void testControllerSortingErrors() {	

		algorithms.forEach(e -> {
			Controller controller = Controller.getInstance();
			String error1 = "";
			assertThrows(NullPointerException.class,() -> controller.sort(null, e, "Integer"));
			assertThrows(NullPointerException.class,() -> controller.sort(error1, null, "Integer"));
			assertThrows(NullPointerException.class,() -> controller.sort(null, e, null));
			
			boolean failure = false;
			try {
			//leer
			controller.sort(error1, e, "Integer");
			failure = true;
			}
			catch(AssertionError exception) {
			}
			if(failure) fail("assert error");
			
			failure = false;
			try {
			//datentyp
			String error2 = "a,b,c";
			controller.sort(error2, e, "Integer");
			failure = true;
			}
			catch(AssertionError exception) {
			}
			if(failure) fail("assert error");
			
			failure = false;
			try {
			//leere, durch Kommata getrennte, Stellen
			String error3 = "1,,3";
			controller.sort(error3, e, "Integer");
			failure = true;
			}
			catch(AssertionError exception) {
			}
			if(failure) fail("assert error");
			
			failure = false;
			try {
//			//länge
			String error4 = "1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,"
					+ "1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,"
					+ "1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,"
					+ "1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,"
					+ "1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,"
					+ "1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,";
			controller.sort(error4, e, "Integer");
			failure = true;
			}
			catch(AssertionError exception) {
			}
			if(failure) fail("assert error");
			
		});

	}
	


	/**
	 * Voraussetzung: testControllerSorting erzeugt keine Fehler.
	 * Dieser Test ist standardmäßig augeschaltet! Falls Sie diesen Test benutzen wollen müssen Sie ihm TexReporter (Zeile 136,137)
	 * den Pfad ihrer pdflatex Installation manuell eingeben!
	 */
	@Disabled
	@Test
	public void testControllerSaving() {	

		int randomPosNumber = 0;
		while(randomPosNumber <= 0) {
			Random rand = new Random();
			randomPosNumber = rand.nextInt(20);
		}
		int size = randomPosNumber;


		algorithms.forEach(e -> {

			Controller controller = Controller.getInstance();
			Pair<String,ArrayList<Integer>> integerInputStringPair = TestUtils.getIntegerArrayString(size,30);
			controller.sort(integerInputStringPair.getKey(), e, "Integer");
			controller.save(true, false, false, false, SaveOption.PDF,false);
			

			assertThrows(IllegalArgumentException.class,() -> controller.save(false, false, false, false, SaveOption.PDF,false));
			assertThrows(IllegalArgumentException.class,() -> controller.save(true, true, false, false, SaveOption.PDF,false));
			assertThrows(IllegalArgumentException.class,() -> controller.save(true, false, true, false, SaveOption.PDF,false));
			assertThrows(IllegalArgumentException.class,() -> controller.save(true, false, false, true, SaveOption.PDF,false));

			controller.save(true, false, false, false, SaveOption.LATEX,false);

			assertThrows(IllegalArgumentException.class,() -> controller.save(false, false, false, false, SaveOption.LATEX,false));
			assertThrows(IllegalArgumentException.class,() -> controller.save(true, true, false, false, SaveOption.LATEX,false));
			assertThrows(IllegalArgumentException.class,() -> controller.save(true, false, true, false, SaveOption.LATEX,false));
			assertThrows(IllegalArgumentException.class,() -> controller.save(true, false, false, true, SaveOption.LATEX,false));

			controller.save(true, false, false, false, SaveOption.TEMPLATE,false);

			assertThrows(IllegalArgumentException.class,() -> controller.save(false, false, false, false, SaveOption.TEMPLATE,false));
			assertThrows(IllegalArgumentException.class,() -> controller.save(true, true, false, false, SaveOption.TEMPLATE,false));
			assertThrows(IllegalArgumentException.class,() -> controller.save(true, false, true, false, SaveOption.TEMPLATE,false));
			assertThrows(IllegalArgumentException.class,() -> controller.save(true, false, false, true, SaveOption.TEMPLATE,false));
		});

		Pair<String,ArrayList<String>> stringinputStringPair = TestUtils.getStringArrayString(size,2);

		algorithms.forEach(e -> {

			Controller controller = Controller.getInstance();
			controller.sort(stringinputStringPair.getKey(), e, "String");
			controller.save(true, false, false, false, SaveOption.PDF,false);	

			assertThrows(IllegalArgumentException.class,() -> controller.save(false, false, false, false, SaveOption.PDF,false));
			assertThrows(IllegalArgumentException.class,() -> controller.save(true, true, false, false, SaveOption.PDF,false));
			assertThrows(IllegalArgumentException.class,() -> controller.save(true, false, true, false, SaveOption.PDF,false));
			assertThrows(IllegalArgumentException.class,() -> controller.save(true, false, false, true, SaveOption.PDF,false));

			controller.save(true, false, false, false, SaveOption.LATEX,false);

			assertThrows(IllegalArgumentException.class,() -> controller.save(false, false, false, false, SaveOption.LATEX,false));
			assertThrows(IllegalArgumentException.class,() -> controller.save(true, true, false, false, SaveOption.LATEX,false));
			assertThrows(IllegalArgumentException.class,() -> controller.save(true, false, true, false, SaveOption.LATEX,false));
			assertThrows(IllegalArgumentException.class,() -> controller.save(true, false, false, true, SaveOption.LATEX,false));

			controller.save(true, false, false, false, SaveOption.TEMPLATE,false);

			assertThrows(IllegalArgumentException.class,() -> controller.save(false, false, false, false, SaveOption.TEMPLATE,false));
			assertThrows(IllegalArgumentException.class,() -> controller.save(true, true, false, false, SaveOption.TEMPLATE,false));
			assertThrows(IllegalArgumentException.class,() -> controller.save(true, false, true, false, SaveOption.TEMPLATE,false));
			assertThrows(IllegalArgumentException.class,() -> controller.save(true, false, false, true, SaveOption.TEMPLATE,false));
		});

	}
	
}
