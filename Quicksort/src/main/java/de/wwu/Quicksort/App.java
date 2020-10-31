package de.wwu.Quicksort;


import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;


/**
 * JavaFX App. Initialisiert die JavaFX Umgebung der View mit entsprechenden Parametern: Fenstergroesse, Titel, Css-Datei. 
 * @author Jonas Stübbe im Zuge der BA
 */
public class App extends Application {

	@Override
	public void start(Stage stage) {    
		var scene = new Scene(View.getInstance().getRoot(),1024, 768);

		stage.setTitle("Didaktische Darstellung des Sortierprozesses");
		stage.setScene(scene);
		//CSS-Datei wird benötigt, da JavaFX (auf MacOS) sonst kryptische Formatierungen hervorruft (nur in der JAR-Datei). 
		scene.getStylesheets().add("stylesheet.css");
		stage.show();
	}

	/**
	 * Die Methode launch gehoert zum Lebenszyklus der JavaFX App. 
	 */
	public static void main(String[] args) {
		launch();
	}

}