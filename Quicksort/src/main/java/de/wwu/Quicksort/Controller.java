package de.wwu.Quicksort;

import java.awt.Desktop;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Objects;
import java.util.stream.Collectors;

import de.wwu.Quicksort.latex.InvalidPdflatexPathException;
import de.wwu.Quicksort.latex.InvalidResourceFileException;
import de.wwu.Quicksort.latex.TexReporter;
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
import de.wwu.Quicksort.utils.StyledDialogs;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.TitledPane;

/**
 * Singleton Klasse, dient als Controller in einer abgewandelten MVC-Struktur.
 * Ermöglicht es, dass die vom Benutzer ausgeführten Interaktionen in der View auch etwas bewirken
 * und die View so mit dem restlichen Programm interagieren kann.  
 * @author Jonas Stübbe im Zuge der BA
 */
public class Controller {
	//siehe Singleton
	private static Controller instance;
	//Model in (abgewandeltem) MVC
	private SortingState<?> state;

	//siehe Singleton
	private Controller() {
	}

	//siehe Singleton
	public static Controller getInstance() {
		if(instance == null) {
			instance = new Controller();
		}
		return instance;
	}

	/**
	 * Die verschiedenen zur Verfügung stehenden Speicheroptionen einer fertiggestellten Sortiersequenz.
	 * PDF: Stelle die Sortierschritte in einem PDF-Dokument dar. 
	 * BEAMER: Stelle die Sortierschritte in einem PDF-Dokument dar (andere Formatierung; noch nicht Implementiert).
	 * LATEX: Gib die Sortierschritte als rohen LaTeX-Code aus (ohne Template).
	 * TEMPLATE: Speichert nichts, öffnet das LaTeX-Template mit dem der LaTeX-Code in ein PDF-Dokument kompiliert wird. 
	 *
	 */
	public enum SaveOption 
	{
		PDF,BEAMER,LATEX,TEMPLATE
	}

	/**
	 * Erstellt den in der View gewählten Sortieralgorithmus
	 * und startet mit den entsprechenden Parametern den Sortierprozess, folgende Validierungsmethoden werden vorher vorausgesetzt:
	 * checkInputForEmptyString, checkInputForDatatype,checkInputForGaps,checkInputForLength.
	 * Die Daten des Sortierprozesses werden im State gespeichert.
	 * @param arrayInput die Eingabesequenz welche es zu sortieren gilt in Form eines durch Kommata getrennten Strings 
	 * @param selectedAlgo das ausgewählte Sortierverfahren
	 * @param selectedDatatype der ausgewählte Datentyp
	 * @throws IllegalArgumentException Exception falls ein unbekanntes Verfahren oder ein unbekannter Datentyp übergeben wird
	 */
	public void sort(String arrayInput,String selectedAlgo,String selectedDatatype) throws IllegalArgumentException {
		Objects.requireNonNull(arrayInput);
		Objects.requireNonNull(selectedAlgo);
		Objects.requireNonNull(selectedDatatype);
		 assert !checkInputForEmptyString(arrayInput);
		 assert !checkInputForDatatype(arrayInput, selectedDatatype);
		 assert !checkInputForGaps(arrayInput);
		 assert !checkInputForLength(arrayInput);
		 
		switch(selectedDatatype) {
		case "Integer":
			switch(selectedAlgo) {
			case "Insertionsort":
				state = new SortingState<Integer>(new ArrayList<SortingStep<Integer>>(), new InsertionsortSorter<Integer>());
				break;
			case "Heapsort":
				state = new SortingState<Integer>(new ArrayList<SortingStep<Integer>>(), new HeapsortSorter<Integer>());
				break;
			case "Quicksort":
				state = new SortingState<Integer>(new ArrayList<SortingStep<Integer>>(), new QuicksortSorter<Integer>());
				break;
			case "Lomuto Quicksort":
				state = new SortingState<Integer>(new ArrayList<SortingStep<Integer>>(), new QuicksortLomutoSorter<Integer>());
				break;
			case "BlockQuicksort":
				state = new SortingState<Integer>(new ArrayList<SortingStep<Integer>>(), new BlockQuicksortSorter<Integer>());
				break;
			case "BlockLomuto":
				state = new SortingState<Integer>(new ArrayList<SortingStep<Integer>>(), new BlockLomutoSorter<Integer>());
				break;
			case "Dual-Pivot BlockLomuto":
				state = new SortingState<Integer>(new ArrayList<SortingStep<Integer>>(), new BlockLomuto2Sorter<Integer>());
				break;
			case "Samplesort":
				state = new SortingState<Integer>(new ArrayList<SortingStep<Integer>>(), new SamplesortSorter<Integer>());
				break;
			case "Super Scalar Samplesort":
				state = new SortingState<Integer>(new ArrayList<SortingStep<Integer>>(), new S3Sorter<Integer>());
				break;
			case "In-Place Parallel Super Scalar Samplesort":
				state = new SortingState<Integer>(new ArrayList<SortingStep<Integer>>(), new IPS4oSorter<Integer>());
				break;
			default:
				throw new IllegalArgumentException("Unbekannter Algorithmus ausgewählt.");
			}

			break;

		case "String":
			switch(selectedAlgo) {
			case "Insertionsort":
				state = new SortingState<String>(new ArrayList<SortingStep<String>>(), new InsertionsortSorter<String>());
				break;
			case "Heapsort":
				state = new SortingState<Integer>(new ArrayList<SortingStep<Integer>>(), new HeapsortSorter<Integer>());
				break;
			case "Quicksort":
				state = new SortingState<String>(new ArrayList<SortingStep<String>>(), new QuicksortSorter<String>());
				break;
			case "Lomuto Quicksort":
				state = new SortingState<String>(new ArrayList<SortingStep<String>>(), new QuicksortLomutoSorter<String>());
				break;
			case "BlockQuicksort":
				state = new SortingState<String>(new ArrayList<SortingStep<String>>(), new BlockQuicksortSorter<String>());
				break;
			case "BlockLomuto":
				state = new SortingState<String>(new ArrayList<SortingStep<String>>(), new BlockLomutoSorter<String>());
				break;
			case "Dual-Pivot BlockLomuto":
				state = new SortingState<String>(new ArrayList<SortingStep<String>>(), new BlockLomuto2Sorter<String>());
				break;
			case "Samplesort":
				state = new SortingState<String>(new ArrayList<SortingStep<String>>(), new SamplesortSorter<String>());
				break;
			case "Super Scalar Samplesort":
				state = new SortingState<String>(new ArrayList<SortingStep<String>>(), new S3Sorter<String>());
				break;
			case "In-Place Parallel Super Scalar Samplesort":
				state = new SortingState<String>(new ArrayList<SortingStep<String>>(), new IPS4oSorter<String>());
				break;
			default:
				throw new IllegalArgumentException("Unbekannter Algorithmus ausgewählt.");
			}
			break;
		default:
			throw new IllegalArgumentException("Unbekannter Datentyp ausgewählt.");
		}
		state.setArray(arrayInput, selectedDatatype);
		try {
			state.sort();
		}
		//Kann vorkommen, falls die Eingabeseqeunz zu groß ist.
		catch(OutOfMemoryError e) {
			Alert alert = StyledDialogs.getAlert(AlertType.INFORMATION, "Sortieren",
					"Speichergrenze erreicht", "Der Sortierprozess hat die Speichergrenzen erreicht! "
					+ "Für die beste Nutzererfahrung werden während des Sortierprozesses viele Schritte zwischengespeichert, "
					+ "damit dem Nutzer das Vorgehen so gut wie möglich dargestellt werden kann."
					+ "Dadurch wird dieses Programm inkompatibel mit großen Eingaben. "
					+ "Bitte wechsel den Algorithmus oder verkleinere die Eingabe!");
			alert.showAndWait();
			return;
		}
	}

	/**
	 * Erstellt aus dem Sortierprozess eine (grobe) Übersicht der einzelnen Sortierschritte.
	 * Dies ist für einen Einblick falls keine Kompilierung erwünscht oder möglich.
	 * 
	 * Keine Priorität in der Entwicklung und daher eher grob gehalten.
	 */
	public void setViewElements() {
		for(int i = 0; i < state.getSortingSteps().size(); i++) {
			TitledPane pane;
			pane = View.getInstance().addArrayOutputPane(state.getSortingSteps().get(i).getFromArrayListMap("input").stream()
					.map(e -> e != null ? e.toString() : "-").collect(Collectors.joining(" | ")));

			//Reihenfolge: outputArray, alle Arrays, alle Variablen
			Collection<Node> c = state.getViewElements(i);
			if(i < state.getSortingSteps().size() - 1) {
				Collection<Node> cNext = state.getViewElements(i+1);
				View.getInstance().addArrayOutputContext(pane, c, cNext);
				//TODO(Nebensächlich) performanter machen (viewElemtsNext in viewElements schieben
			}
			else {
				View.getInstance().addArrayOutputContext(pane, c);
			}
		}
	}

	/**
	 * Erstellt aus dem fertigen Sortierprozess ein PDF-Dokument welches den Ablauf zeigt oder gibt das LaTeX-Dokument / LaTeX-Template aus. 
	 * @param steps Für eine zukünftige Erweiterung
	 * @param pivot Für eine zukünftige Erweiterung
	 * @param basicCase Für eine zukünftige Erweiterung
	 * @param worstCaseProtection Für eine zukünftige Erweiterung
	 * @param saveOption gibt an, welche Operation geschehen soll, für die Möglichkeiten siehe Dokumentation des Enums: SaveOption
	 * @param open Reine Test-Option, öffnet das PDF-Dokument nicht und löscht es nach Kompilierung
	 * @return gibt etwaige Errors zurück, wird für eine Nutzerkommunikation via Dialog-Feld benutzt
	 */
	public String save(boolean steps,boolean pivot,boolean basicCase, boolean worstCaseProtection, SaveOption saveOption, boolean open) {
		if(!steps || pivot || basicCase || worstCaseProtection) {
			throw new IllegalArgumentException("Funktionalität noch nicht implementiert");
		}
		try {
			DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss");  
			String myUUID = dtf.format(LocalDateTime.now()).toString();
			myUUID =  myUUID.replace('/', '_');
			myUUID =  myUUID.replace(':', '_');
			switch(saveOption) {
			case PDF:
				TexReporter reporter = new TexReporter(myUUID + "_Sorting_Procedure");
				if(!open) {
					reporter.setDev();
				}
				if(state.getSortingSteps() == null) {
					return "Es muss vorher sortiert werden!";
				}
				else {
					String texString = state.getTexString();
					try {
						reporter.getReport(texString,state.getSorter().getClass().getName().substring(25),state.getDataType());
					}
					catch(InvalidPdflatexPathException e) {
						File file = new File("pdflatexPath.txt");
						file.delete();
						return "Pdflatex Installation konnte nicht gefunden werden";
					}
					catch(RuntimeException e) {
						e.printStackTrace();
						return "Kompilierung fehlgeschlagen";
					}
					if(open && Desktop.isDesktopSupported()) {
						try {
							File myFile = new File(myUUID + "_Sorting_Procedure" +".pdf");
							Desktop.getDesktop().open(myFile);
						} catch (IOException ex) {
							ex.printStackTrace();
							return "Es ist keine Anwendung für PDFs registriert.";
						}
					}
				}		
				break;
			case BEAMER:
				Alert alert = StyledDialogs.getAlert(AlertType.INFORMATION, "Erstelle BEAMER", "Results:", "Noch nicht implementiert!");
				alert.showAndWait();
				break;
			case LATEX:
				try (Writer writer = new BufferedWriter(new OutputStreamWriter(
						new FileOutputStream(myUUID + "_LATEX" + ".txt"), "utf-8"))) {
					try {
						String texString = state.getTexString();
						writer.write(texString);
					} catch (IOException e) {
						e.printStackTrace();
						return "Unbekannter Fehler";
					}
				} catch (UnsupportedEncodingException e1) {
					e1.printStackTrace();
					return "Unbekannter Fehler";
				} catch (FileNotFoundException e1) {
					e1.printStackTrace();
					return "Unbekannter Fehler";
				} catch (IOException e1) {
					e1.printStackTrace();
					return "Unbekannter Fehler";
				}

				if(open && Desktop.isDesktopSupported()) {
					try {
						File myFile = new File(myUUID + "_LATEX" + ".txt");
						Desktop.getDesktop().open(myFile);
					} catch (IOException ex) {
						ex.printStackTrace();
						return "Es ist keine Anwendung für PDFs registriert.";
					}
				}
				else if(!open) {
					File myFile = new File(myUUID + "_LATEX" + ".txt");
					myFile.delete();
				}

				break;
			case TEMPLATE:
				if(open && Desktop.isDesktopSupported()) {
					try {	
						InputStream in = getClass().getResourceAsStream("/Template.tex");
						final File tempFile = File.createTempFile("TempLaTeX-", ".tex");
						tempFile.deleteOnExit();
						try (FileOutputStream out = new FileOutputStream(tempFile)) {
							in.transferTo(out);
						}
						Desktop.getDesktop().open(tempFile);
					} catch (IOException ex) {
						ex.printStackTrace();
						return "Es ist keine Anwendung für PDFs registriert.";
					}
				}
				break;
			default:
				break;
			}

		} catch (InvalidResourceFileException e) {
			e.printStackTrace();
			return "Unbekannter Fehler";
		}
		return "";
	}

	/**
	 * Validierung des Inputs auf Abwesenheit von Zeichen. 
	 * @param arrayInput der zu Validiernde String
	 * @return Validierungsergebnis
	 */
	public boolean checkInputForEmptyString(String arrayInput) {
		Objects.requireNonNull(arrayInput);
		return arrayInput.trim().isEmpty();
	}
	
	/**
	 * Validierung des Inputs auf leere, durch Kommata getrente, Stellen. 
	 * @param arrayInput der zu Validiernde String
	 * @return Validierungsergebnis
	 */
	public boolean checkInputForGaps(String arrayInput) {
		Objects.requireNonNull(arrayInput);

		//nicht möglich...
//		long sumNulls = Arrays.stream(arrayInput.split(",")).filter(e -> e == null).count();
		
		long sumEmpties = Arrays.stream(arrayInput.split(",")).filter(e -> e.length() == 0).count();

//		return !(sumNulls == 0 && sumEmpties == 0);
		return !(sumEmpties == 0);
	}

	/**
	 * Validierung des Inputs nach dem Datentyp
	 * @param arrayInput der zu Validiernde String 
	 * @param selectedDatatype
	 * @return Validierungsergebnis
	 */
	public boolean checkInputForDatatype(String arrayInput,String selectedDatatype) {
		Objects.requireNonNull(arrayInput);
		Objects.requireNonNull(selectedDatatype);

		long sum = 0;
		switch(selectedDatatype) {
		case "Integer":
			sum = Arrays.stream(arrayInput.split(","))
			.filter(e -> streamParseIntExceptionHandling(e) == false).count();
			break;
		case "String":	
			sum = Arrays.stream(arrayInput.split(","))
			.filter(e -> streamToStringExceptionHandling(e) == false).count();
			break; 
		default:
			throw new IllegalArgumentException("Unbekannter Algorithmus ausgewählt.");
		}
		return !(sum == 0); 
	}

	/**
	 * Validiert ein Element auf den Datentyp
	 * @param element das zu Validiernde Element
	 * @return Validierungsergebnis
	 */
	public boolean streamParseIntExceptionHandling(String element) {
		try {
			Integer.parseInt(element);
		}
		catch(NumberFormatException e) {
			return false;
		}
		return true;
	}

	/**
	 * Validiert ein Element auf den Datentyp
	 * @param element das zu Validiernde Element
	 * @return Validierungsergebnis
	 */
	public boolean streamToStringExceptionHandling(String element) {
		try {
			element.toString();
		}
		catch(Exception e) {
			return false;
		}
		return true;
	}

	/**
	 * Validierung des Inputs auf die Länge
	 * @param arrayInput der zu Validiernde String
	 * @return Validierungsergebnis
	 */
	public boolean checkInputForLength(String arrayInput) {
		Objects.requireNonNull(arrayInput);

		long sumElements = Arrays.stream(arrayInput.split(",")).count();
		return !(sumElements < 100);
	}
	
	/**
	 * Validierung des Inputs auf Strings der Länge 1
	 * @param arrayInput der zu Validiernde String
	 * @return Validierungsergebnis
	 */
	public boolean checkInputForTooLargeStrings(String arrayInput) {
		Objects.requireNonNull(arrayInput);

		long sumTooLargeStrings = Arrays.stream(arrayInput.split(",")).filter(e -> e.length() > 1).count();
		return !(sumTooLargeStrings == 0);
	}

	/**
	 * Gibt eine Warnung ab, falls String zu viele Zeichen hat um Kompilierungsfehler vorzubeugen.
	 * <p>
	 * Gebe eine Warnung ab, falls eine Sortiersequenz als PDF gespeichert werden soll,
	 * welche kummuliert mehr als 40 Zeichen besitzt oder einen Wert mit einer Charakterlänge größer gleich 4 besitzt.
	 * @return Validierungsergebnis
	 */
	public boolean inputAlert() {
		long size = 0;
		int maxLength = 0;
		ArrayList<?> arrayList = state.getArrayList();
		for(int i = 0; i < arrayList.size();i++) {
			int length = arrayList.get(i).toString().length();
			maxLength = Math.max(length, maxLength);
			size += length;
		}
		return (size > 40 || maxLength >= 4);
	}

	public SortingState<?> getState(){
		return state;
	}
}
