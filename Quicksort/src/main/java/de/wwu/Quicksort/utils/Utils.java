package de.wwu.Quicksort.utils;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Optional;
import java.util.Random;

import javafx.scene.control.TextInputDialog;

/**
 * Klasse für Hilfsfunktionen die im gesamten Programm Nützlich sind und benutzt werden können.
 * Zukünftige sollen weitere Hilfsfunktionen in diese Klasse ausgelagert werden.
 * @author Jonas Stübbe im Zuge der BA
 *
 */
public abstract class Utils {

	public static <I> void swap(ArrayList<I> input, int a, int b) {
		I temp = input.get(a);
		input.set(a, input.get(b));
		input.set(b, temp);
	}

	/**
	 * In unserem Fall ist das Sortierverfahren smallSort,
	 * welches für kleine Eingabesequenzen benutzt wird durch das Sortierverfahren: Insertionsort ralisiert.
	 * @param <I>
	 * @param input zu sortierende Eingabesequenz
	 * @param l linke Grenze
	 * @param r rechte Grenze
	 */
	public static <I extends Comparable<I>> void smallSort(ArrayList<I> input, int l, int r) {
		for(int i=l;i<r + 1;i++) {
			I temp = input.get(i);
			int j = i;
			while(j > l && input.get(j-1).compareTo(temp) > 0) {
				input.set(j, input.get(j-1));
				j = j-1;
			}
			input.set(j, temp);
		}
	}

	public static int alignToNextBlock(int p, int PUFFERSIZE) {
		return p % PUFFERSIZE == 0 ? p : p + PUFFERSIZE - (p % PUFFERSIZE);
	}

	public static String readPdfLatexPath() {
		String path = "";
		File file = new File("pdflatexPath.txt");
		if (file.exists()) {
			BufferedReader in = null;
			try {
				in = new BufferedReader(new FileReader("pdflatexPath.txt"));
				path = in.readLine();
			}
			catch(IOException e) {
				e.printStackTrace();
			}
			finally {
				if (in != null)
					try {
						in.close();
					} catch (IOException e) {
					}
			}
			return path;
		}
		else {
			TextInputDialog dialog = StyledDialogs.getInputDialog("/Library/TeX/texbin/pdflatex",
					"Pdflatex Pfad", "Für eine reibungslose Kompilierung zu einem PDF-Dokument mithilfe von LaTeX wird der Pfad der pdflatex installation benötigt.",
					"Der Pfad kann mithilfe des Konsolenbefehls 'which pdflatex' herausgefunden werden");
			Optional<String> result = dialog.showAndWait();
			if (result.isPresent()){
				path = result.get();
			}
			PrintWriter pWriter = null;
			try {
				pWriter = new PrintWriter(new BufferedWriter(new FileWriter("pdflatexPath.txt")));
				pWriter.print(path);
			} catch (IOException ioe) {
				ioe.printStackTrace();
			} finally {
				if (pWriter != null){
					pWriter.flush();
					pWriter.close();
				}
			}
		}
		return path;
	}

	public static String getRandomNumbers(int quantity, int bound) {
		String str = "";
		Random rand = new Random();
		for(int i = 0; i < quantity; i++) {
			if(i != 0) {
				str += ",";
			}
			int j = rand.nextInt(bound);
			str += j;
		}
		return str;
	}

	public static String getRandomStrings(int quantity, int bound) {
		String str = "";
		for(int i = 0; i < quantity; i++) {
			if(i != 0) {
				str += ",";
			}
			str += getRndChar();
		}
		return str;
	}

	public static char getRndChar() {
		int rnd = (int) (Math.random() * 52); // or use Random or whatever
		char base = (rnd < 26) ? 'A' : 'a';
		return (char) (base + rnd % 26);
	}
}
