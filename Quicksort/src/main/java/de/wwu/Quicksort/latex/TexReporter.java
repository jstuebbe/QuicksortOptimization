package de.wwu.Quicksort.latex;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Scanner;

import de.wwu.Quicksort.utils.Utils;

/**
 * Klasse um einen PDF-Datei zu erstellen. Ursprünglich im Zuge des ProSem von CW erstellt um einen PDF-Report zu erstellen.
 * Alle Kommentare wurden ins Deutsche übersetzt.
 * <p>
 * Die PDF-Datei wird erstellt, indem mithilfe der TeXLive-Distribution und dem Befehl pdflatex eine TeX-Datei generiert wird. 
 *
 * @Author Carolin Wortman: Arbeit aus dem Projektseminar FDA begleitet von Prof. Dr. Vahrenhold,
 * mit Erlaubnis für dieses Projekt abgeändert und bearbeitet von Jonas Stübbe.
 *
 */
public class TexReporter {

	private static final List<String> FILE_ENDINGS = Collections.unmodifiableList(Arrays.asList(".aux", ".log", ".out", ".tex", ".toc")); 
	private static final List<String> FILE_ENDINGS_DEV = Collections.unmodifiableList(Arrays.asList(".aux", ".log", ".out", ".tex", ".toc", ".pdf")); 

	private String id;

	private String templateContent;

	private boolean dev = false;

	/**
	 * Erstellt einen neuen Tex-Reporter
	 *
	 * @param id die Sortierprozess-Id für die ein PDF-Dokument erstellt werden soll.
	 * @throws InvalidResourceFileException wenn die Template-Datei nicht gelesen werden kann.
	 */
	public TexReporter(String id) throws InvalidResourceFileException {
		this.id = id;
		// Hole das Template aus den Ressourcen
		//Hier wurde der Code verädnert, da die Ressource in der JAR anders geholt werden muss als Eclipse.
		//Um beide trotzdem beide Möglichkeiten zu erhalten, wurde auf getResourceAsStream gewechselt.
		InputStream in = getClass().getResourceAsStream("/Template.tex"); 
		Scanner s = new Scanner(in);
		s.useDelimiter("\\A");
		this.templateContent = s.hasNext() ? s.next() : "";
		s.close();
	}

	public byte[] getReport(String inputText,String algo, String datatype) throws InvalidPdflatexPathException {
		String content = this.templateContent;
		DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy/MM/dd");  
		LocalDateTime now = LocalDateTime.now();
		content = content.replaceAll("dateVar", dtf.format(now));
		dtf = DateTimeFormatter.ofPattern("HH:mm:ss");
		content = content.replaceAll("timeVar", dtf.format(now));
		content = content.replaceAll("algoVar", algo);
		content = content.replaceAll("datatypeVar", datatype);
		int splitInt = content.indexOf("\\end{document}");
		String setupString = content.substring(0, splitInt);
		String endString = content.substring(splitInt);
		List<String> lines = Arrays.asList(setupString + inputText + endString);

		// Erstelle TeX-Datei
		Path file = Paths.get(this.id + ".tex");
		byte[] pdfToBytes = null;
		try {
			Files.write(file, lines, StandardCharsets.UTF_8);
			try {
				// Kompiliere
				compileTex(this.id);

				// Kompiliere erneut für TOC und hole bytes
				pdfToBytes = compileTex(this.id);
			}
			catch(InvalidPdflatexPathException e) {
				throw e;	
			}
			// Lösche Hilfsdateien
			clearGeneratedFiles(this.id);
			return pdfToBytes;
		} catch (IOException e) {
			throw new RuntimeException("Could not create report due to I/O Exception.", e);
		}
	}

	/**
	 * Löscht alle Dateien welche erstellt wurden um die PDF-Datei zu erstellen.
	 *
	 * @param filename der Name der Datei
	 * @throws IOException wenn ein I/O Error während dem Löschvorgang auftritt
	 */
	private void clearGeneratedFiles(String filename) throws IOException {
		// Remove all Files that have been created
		if(dev) {
			for (String s : FILE_ENDINGS_DEV) {
				Path path = Paths.get(filename + s);
				Files.deleteIfExists(path);
			}
		}
		else {
			for (String s : FILE_ENDINGS) {
				Path path = Paths.get(filename + s);
				Files.deleteIfExists(path);
			}
		}

	}

	/**
	 * Kompiliert die gegebene TeX-Datei
	 *
	 * @param filename der Name der Datei
	 * @return die bytes der resultierenden PDF-Datei
	 * @throws IOException wenn ein I/O Error auftritt
	 * @throws InvalidPdflatexPathException 
	 */
	private byte[] compileTex(String filename) throws IOException, InvalidPdflatexPathException {
		//Hole den Pfad zur pdflatex Installation
		String path = "";
		
		//--------------------------------------------------------
		// Damit die LaTeX Tests funktionieren: Füge den Pfad der pdflatex Installation per Hand hinzu.
		
//		path = "/Library/TeX/texbin/pdflatex";
		path = Utils.readPdfLatexPath();
		
		
		//--------------------------------------------------------
		
		
		
		//Erstelle Prozess welcher pdflatex für eine gegebene Datei auf dem System initialisiert
		ProcessBuilder texBuilder = new ProcessBuilder(path, filename + ".tex");
		Process p = null;
		texBuilder.redirectErrorStream(true);
		try {
			p = texBuilder.start();
		}
		catch(Exception e) {
			throw new InvalidPdflatexPathException(path);
		}

		// Lese den inputStream log um Puffer des inputStreams vom blockieren abzuhalten
		BufferedReader br = new BufferedReader(new InputStreamReader(p.getInputStream()));

		// Lese um zu versichern, dass es auf Grund von Groessen-Restriktionen hängen bleibt
		try {
			String tmp = null;
			while ((tmp = br.readLine()) != null) {
				//              Log.getLogger().info(tmp);
//				            	System.out.println(tmp);
			}
			p.waitFor();
		} catch (InterruptedException e) {
			throw new RuntimeException("Compilation of generated TeX-file was interrupted.", e);
		}
		byte[] pdfToBytes = null;

		//Prüfe ob der Prozess normal terminiert ist.
		if (p.exitValue() == 0) {
			pdfToBytes = Files.readAllBytes(Paths.get(filename + ".pdf"));
		} else {
			throw new RuntimeException("Compilation failed.");
		}

		return pdfToBytes;
	}

	public void setDev() {
		dev = true;
	}
}
