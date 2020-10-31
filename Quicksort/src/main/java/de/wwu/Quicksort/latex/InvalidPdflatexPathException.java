package de.wwu.Quicksort.latex;

/**
 * Exception die geworfen wird, falls der Pdflatex-Pfad falsch ist und die LaTeX-Datei somit nicht richtig kompiliert werden kann.
 * @author Jonas Stübbe im Zuge der BA
 *
 */
public class InvalidPdflatexPathException extends Exception {

	private static final long serialVersionUID = -5402075806825087369L;

	/**
	 * Erstellt eine neue InvalidPdflatexPathException.
	 * 
	 * @param path Der Pfad zur Pdflatex installation
	 */
	public InvalidPdflatexPathException(String path) {
		super("Pdflatex Installation konnte nicht in " + path + " gefunden werden");
	}
}
