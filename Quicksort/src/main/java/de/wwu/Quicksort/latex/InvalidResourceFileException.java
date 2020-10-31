package de.wwu.Quicksort.latex;

/**
 * Zeigt an, dass eine notwendige Datei aus den Ressoucen nicht geladen werden kann. Wird für den TexReporter gebraucht.
 * @author CW aus ProSem
 *
 */
public class InvalidResourceFileException extends Exception {

	private static final long serialVersionUID = -7814880657040723464L;

	/**
	 * Erstellt eine neue InvalidResourceFileException.
	 * 
	 * @param path der Pfad zur Ressource
	 */
	public InvalidResourceFileException(String path) {
		super("Could not get file " + path);
	}

}
