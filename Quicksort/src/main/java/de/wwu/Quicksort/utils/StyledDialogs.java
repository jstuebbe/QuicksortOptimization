package de.wwu.Quicksort.utils;

import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.DialogPane;
import javafx.scene.control.TextInputDialog;
import javafx.scene.text.Text;

/**
 * Diese Klasse wird gebraucht, da die JAR-Datei sonst kryptische Zeichen enthält.
 * Es soll sichergestellt werden, dass jeder benutzte Dialog die CSS Datei: "stylesheet.css" benutzt.
 * @author Jonas Stübbe im Zuge der BA
 *
 */
public abstract class StyledDialogs {

	public static Alert getAlert(AlertType type, String title, String header, String content) {
		Alert alert = new Alert(type);
		DialogPane dialogPane = alert.getDialogPane();
		//Bugfix für kryptische Zeichen (MacOS Bug)
		dialogPane.getStylesheets().add("stylesheet.css");
		alert.setTitle(title);
		alert.setHeaderText(header);

		//BUGFIX: Schneidet den Text auf Windows zu
		alert.setResizable(true);
		alert.setContentText(content);
		return alert;
	}

	public static TextInputDialog getInputDialog(String initText, String title, String header, String content) {
		TextInputDialog dialog = new TextInputDialog(initText);
		DialogPane dialogPane = dialog.getDialogPane();
		//Bugfix für kryptische Zeichen (MacOS Bug)
		dialogPane.getStylesheets().add("stylesheet.css");
		dialog.setTitle(title);
		dialog.setHeaderText(header);
		
		//BUG: Schneidet den Text auf Windows zu
		dialog.setResizable(true);
		dialog.setContentText(content);
		return dialog;
	}

}
