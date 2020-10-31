package de.wwu.Quicksort;

import java.util.Collection;
import java.util.Optional;

import de.wwu.Quicksort.Controller.SaveOption;
import de.wwu.Quicksort.utils.StyledDialogs;
import de.wwu.Quicksort.utils.Utils;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.Node;
import javafx.scene.control.Accordion;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.control.TitledPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

/**
 * Singleton Klasse, dient als View in einer abgewandelten MVC-Struktur.
 * Bietet dem Nutzer eine Oberfläche um unter anderem den Sortierprozess und dessen Parameter auswählen zu können,
 * diesen dann zu starten und abschließend als PDF-Dokument abrufen zu koennen.  
 * @author Jonas Stübbe im Zuge der BA
 */
public class View {
	//siehe Singleton
	private static View instance;

	private TextField arrayInput;
	private Accordion outputAccordion;
	private ComboBox<String> algoComboBox, algoOptionComboBox,datatypeComboBox,inputComboBox;
	private HBox actionsHBox,inputHBox;
	private VBox inputVBox;
	private CheckBox stepsCheckBox, pivotCheckBox, basicCaseCheckBox, worstCaseProtectionCheckBox;
	private BorderPane root;

	//siehe Singleton
	private View() {
	}

	public static View getInstance() {
		if(instance==null) {
			instance = new View();
		}
		return instance;
	}

	//Wurzel Element der View
	public BorderPane getRoot() {
		root = new BorderPane();
		root.setTop(getInputVBox());
		root.setCenter(getOutputScrollPane());
		root.setBottom(getActionsHBox());
		return root;
	}

	public ScrollPane getOutputScrollPane() {
		ScrollPane scrollPane = new ScrollPane();
		scrollPane.setContent(getOutputAccordion());
		return scrollPane;
	}

	public Accordion getOutputAccordion() {
		outputAccordion = new Accordion();
		return outputAccordion;
	}

	public VBox getInputVBox() {
		inputVBox = new VBox();
		inputVBox.getChildren().addAll(
				getInputHBox()
				);
		return inputVBox;
	}

	public HBox getInputHBox() {
		inputHBox = new HBox();
		try {
			inputHBox.getChildren().addAll(
					getInputComboBox(),
					getArrayInput(),
					getAlgoComboBox(),
					getAlgoOptionComboBox(),
					getDatatypeComboBox(),
					getSortButton());
		}
		catch(IllegalArgumentException e) {
			Alert alert = StyledDialogs.getAlert(AlertType.ERROR, "Error", "Fehlerhafte Erweiterung", 
					"Ein Fehler ist aufgetreten, "
							+ "wahrscheinlich wurde eine Erweiterung fehlerhaft oder unvollständig durchgeführt!");
			alert.showAndWait();
		}
		return inputHBox;
	}

	public HBox getActionsHBox() {
		actionsHBox = new HBox();
		actionsHBox.getChildren().addAll(
				//SaveOptions
				getSaveButton(),
				getBeamerButton(),
				getLatexButton(),
				getTemplateButton(),
				//Erweiterung:
				getStepsLabel(),
				getStepsCheckBox(),
				getPivotLabel(),
				getPivotCheckBox(),
				getBasicCaseLabel(),
				getBasicCaseCheckBox(),
				getWorstCaseProtectionLabel(),
				getWorstCaseProtectionCheckBox()
				);
		actionsHBox.setVisible(false);
		return actionsHBox;
	}

	public Label getStepsLabel() {
		Label stepsLabel = new Label("Zwischenschritte");
		return stepsLabel;
	}

	public Label getPivotLabel() {
		Label stepsLabel = new Label("Pivot-Berechnung");
		return stepsLabel;
	}

	public Label getBasicCaseLabel() {
		Label stepsLabel = new Label("Basisfall");
		return stepsLabel;
	}

	public Label getWorstCaseProtectionLabel() {
		Label stepsLabel = new Label("Worst-Case Sicherung");
		return stepsLabel;
	}

	public CheckBox getStepsCheckBox() {
		stepsCheckBox = new CheckBox(); 
		stepsCheckBox.setSelected(true);
		stepsCheckBox.setOnAction((e) ->{
			Alert alert = StyledDialogs.getAlert(AlertType.INFORMATION, "Erweiterung möglich", "Zwischenschritte",
					"Dies Option ist noch nicht implementiert!");
			alert.showAndWait();
			stepsCheckBox.setSelected(true);
		});
		return stepsCheckBox;
	}

	public CheckBox getPivotCheckBox() {
		pivotCheckBox = new CheckBox();
		pivotCheckBox.setSelected(false);
		pivotCheckBox.setOnAction((e) ->{
			Alert alert = StyledDialogs.getAlert(AlertType.INFORMATION, "Erweiterung möglich", "Pivot-Berechnung",
					"Dies Option ist noch nicht implementiert!");
			alert.showAndWait();
			pivotCheckBox.setSelected(false);
		});
		return pivotCheckBox;
	}

	public CheckBox getBasicCaseCheckBox() {
		basicCaseCheckBox = new CheckBox();
		basicCaseCheckBox.setSelected(false);
		basicCaseCheckBox.setOnAction((e) ->{
			Alert alert = StyledDialogs.getAlert(AlertType.INFORMATION, "Erweiterung möglich", "Basisfall",
					"Dies Option ist noch nicht implementiert!");
			alert.showAndWait();
			basicCaseCheckBox.setSelected(false);
		});
		return basicCaseCheckBox;
	}

	public CheckBox getWorstCaseProtectionCheckBox() {
		worstCaseProtectionCheckBox = new CheckBox();
		worstCaseProtectionCheckBox.setSelected(false);
		worstCaseProtectionCheckBox.setOnAction((e) ->{
			Alert alert = StyledDialogs.getAlert(AlertType.INFORMATION, "Erweiterung möglich", "Worst-Case Sicherung",
					"Dies Option ist noch nicht implementiert!");
			alert.showAndWait();
			worstCaseProtectionCheckBox.setSelected(false);
		});
		return worstCaseProtectionCheckBox;
	}

	public TextField getArrayInput() {
		arrayInput = new TextField();
		return arrayInput;
	}

	public void setActionsHBoxVisibility(boolean value) {
		actionsHBox.setVisible(value);
	}

	/**
	 * Gibt die Möglichkeit aus einer Vorauswahl von Eingabesequenzen auszuwählen. 
	 * @return inputComboBox ComboBox in der man vorausgewählte Auswählen kann.
	 * @throws IllegalArgumentException wird geworfen falls ein unbekannter Datentyp oder 
	 * eine unbekannte Eingabeoption ausgewählt wurde.
	 */
	public ComboBox<String> getInputComboBox() throws IllegalArgumentException{
		inputComboBox = new ComboBox<String>();
		inputComboBox.getItems().addAll(
				"Eingabe",
				"Zufall",
				"Zufall (groß)",
				"10-1",
				"20-1",
				"30-1",
				"40-1",
				"50-1",
				"1-10",
				"1-40" 
				);
		inputComboBox.getSelectionModel().selectedItemProperty().addListener( (options, oldValue, newValue) -> {
			String text = "";

			if(newValue != null) {
				switch(newValue){
				case "Eingabe":
					text = "";
					break;
				case "Zufall":
					if(getSelectedDatatype() == "Integer") {
						text = Utils.getRandomNumbers(20,30);	
					}
					else if(getSelectedDatatype() == "String") {
						text = Utils.getRandomStrings(20,30);
					}
					else {
						throw new IllegalArgumentException("Unbekannter Datentyp ausgewählt.");
					}
					break;
				case "Zufall (groß)":
					if(getSelectedDatatype() == "Integer") {
						text = Utils.getRandomNumbers(25,30);	
					}
					else if(getSelectedDatatype() == "String") {
						text = Utils.getRandomStrings(25,30);
					}
					else {
						throw new IllegalArgumentException("Unbekannter Datentyp ausgewählt.");
					}
					break;
				case "10-1":
					text = "10,9,8,7,6,5,4,3,2,1";
					break;
				case "20-1":
					text = "20,19,18,17,16,15,14,13,12,11,10,9,8,7,6,5,4,3,2,1";
					break;
				case "30-1":
					text = "30,29,28,27,26,25,24,23,22,21,20,19,18,17,16,15,14,13,12,11,10,9,8,7,6,5,4,3,2,1";
					break;
				case "40-1":
					text = "40,39,38,37,36,35,34,33,32,31,30,29,28,27,26,25,24,23,22,21,20,"
							+ "19,18,17,16,15,14,13,12,11,10,9,8,7,6,5,4,3,2,1";
					break;
				case "50-1":
					text = "50,49,48,47,46,45,44,43,42,41,40,39,38,37,36,35,34,33,32,31,30,"
							+ "29,28,27,26,25,24,23,22,21,20,19,18,17,16,15,14,13,12,11,10,9,8,7,6,5,4,3,2,1";
					break;
				case "1-10":
					text = "1,2,3,4,5,6,7,8,9,10";
					break;
				case "1-40":
					text = "1,2,3,4,5,6,7,8,9,10,11,12,13,14,15,16,17,18,19,20,"
							+ "21,22,23,24,25,26,27,28,29,30,31,32,34,35,36,37,38,39,40";
					break;
				case "a-z":
					text = "a,b,c,d,e,f,g,h,i,j,k,l,m,n,o,p,q,r,s,t,u,v,w,x,y,z";
					break;
				case "z-a":
					text = "z,y,x,w,v,u,t,s,r,q,p,o,n,m,l,k,j,i,h,g,f,e,d,c,b,a";
					break;
				case "A-z":
					text = "A,B,C,D,E,F,G,H,I,J,K,L,M,N,O,P,Q,R,S,T,U,V,W,X,Y,Z,a,b,c,d,e,f,"
							+ "g,h,i,j,k,l,m,n,o,p,q,r,s,t,u,v,w,x,y,z";
					break;
				case "z-A":
					text = "z,y,x,w,v,u,t,s,r,q,p,o,n,m,l,k,j,i,h,g,f,e,d,c,b,a,Z,Y,X,W,V,U,"
							+ "T,S,R,Q,P,O,N,M,L,K,J,I,H,G,F,E,D,C,B,A";
					break;
				default:
					throw new IllegalArgumentException("Unbekannte Eingabeoption");
				}
				//Falls der Array Input noch nicht initalisiert ist
				if(arrayInput != null) {
					setArrayInputText(text);
				}
			}
		}
				);

		inputComboBox.getSelectionModel().selectFirst();
		return inputComboBox;
	}


	/**
	 * Gibt die Möglichkeit aus verschiedenen Sortierverfahren auszuwählen.
	 * @return algoComboBox ComboBox in welcher das Sortierverfahren ausgewählt werden kann.
	 * @throws IllegalArgumentException wird geworfen falls ein unbekannter Algorithmus ausgewählt wurde.
	 */
	public ComboBox<String> getAlgoComboBox() throws IllegalArgumentException{
		algoComboBox = new ComboBox<String>();

		algoComboBox.getItems().addAll(
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

		algoComboBox.getSelectionModel().selectedItemProperty().addListener( (options, oldValue, newValue) -> {
			ObservableList<String> items = FXCollections.observableArrayList();
			switch(newValue){
			case "Insertionsort":
				items.addAll("Standard","Erweiterung möglich");
				break;
			case "Heapsort":
				items.addAll("Standard","Erweiterung möglich");
				break;
			case "Quicksort":
				items.addAll("Standard","Erweiterung möglich");
				break;
			case "Lomuto Quicksort":
				items.addAll("Standard","Erweiterung möglich");
				break;
			case "BlockQuicksort":
				items.addAll("Standard","Erweiterung möglich");
				break;
			case "BlockLomuto":
				items.addAll("Standard","Erweiterung möglich");
				break;
			case "Dual-Pivot BlockLomuto":
				items.addAll("Standard","Erweiterung möglich");
				break;
			case "Samplesort":
				items.addAll("Standard","Erweiterung möglich");
				break;
			case "Super Scalar Samplesort":
				items.addAll("Standard","Erweiterung möglich");
				break;
			case "In-Place Parallel Super Scalar Samplesort":
				items.addAll("Standard","Erweiterung möglich");
				break;
			default:
				throw new IllegalArgumentException("Unbekannter Algorithmus ausgewählt");
			}
			if(algoOptionComboBox != null) {
				algoOptionComboBox.setItems(items);
				algoOptionComboBox.getSelectionModel().selectFirst();
			}
		}
				);

		algoComboBox.getSelectionModel().selectFirst();
		return 	algoComboBox;	
	}

	/**
	 * Gibt die Möglichkeit aus verschiedenen Varianten oder Pivot-Wahl Strategien für ein Sortierverfahren auszuwählen.
	 * @return algoOptionComboBox die ComboBox in der die Variante oder die Pivot-Wahl Strategie ausgewählt werden kann.
	 * @throws IllegalArgumentException wird geworfen falls eine Unbekannte Algorithmus-Option ausgewählt wurde.
	 */
	public ComboBox<String> getAlgoOptionComboBox() throws IllegalArgumentException{
		algoOptionComboBox = new ComboBox<String>();
		algoOptionComboBox.getItems().addAll("Standard","Erweiterung möglich");

		algoOptionComboBox.getSelectionModel().selectedItemProperty().addListener( (options, oldValue, newValue) -> {
			if(newValue != null) {
				switch(newValue){
				case "Standard":

					break;
				case "Erweiterung möglich":
					Alert alert = StyledDialogs.getAlert(AlertType.INFORMATION, "Erweiterung möglich", "Variante/Pivot-Wahl",
							"Dies Option ist noch nicht implementiert!");
					alert.showAndWait();
					algoOptionComboBox.getSelectionModel().selectFirst();
					break;
				default:
					throw new IllegalArgumentException("Unbekannte Algorithmus-Option ausgewählt.");
				}
			}
		}
				);
		algoOptionComboBox.getSelectionModel().selectFirst();
		return algoOptionComboBox;
	}

	/**
	 * Gibt die Möglichkeit einen Datentyp auszuwählen.
	 * @return datatypeComboBox ComboBox in der der Datentyp ausgewählt wird.
	 * @throws IllegalArgumentException wird geworfen falls ein Unbekannter Datentyp ausgewählt wurde.
	 */
	public ComboBox<String> getDatatypeComboBox() throws IllegalArgumentException{
		datatypeComboBox = new ComboBox<String>();

		datatypeComboBox.getItems().addAll(
				"Integer",
				"String",
				"Erweiterung möglich"
				);

		datatypeComboBox.getSelectionModel().selectedItemProperty().addListener( (options, oldValue, newValue) -> {
			ObservableList<String> items = FXCollections.observableArrayList();
			if(newValue != null) {
				switch(newValue){
				case "Integer":
					items.addAll(
							"Eingabe",
							"Zufall",
							"Zufall (groß)",
							"10-1",
							"20-1",
							"30-1",
							"40-1",
							"50-1",
							"1-10",
							"1-40" 
							);
					break;
				case "String":
					items.addAll(
							"Eingabe",
							"Zufall",
							"Zufall (groß)",
							"a-z",
							"z-a",
							"A-z",
							"z-A"
							);
					break;
				case "Erweiterung möglich":
					Alert alert = StyledDialogs.getAlert(AlertType.INFORMATION, "Erweiterung möglich", "Datentyp",
							"Dies Option ist noch nicht implementiert!");
					alert.showAndWait();
					datatypeComboBox.getSelectionModel().selectFirst();
					return;
				default:
					throw new IllegalArgumentException("Unbekannter Datentyp ausgewählt.");
				}
				if(inputComboBox != null) {
					inputComboBox.setItems(items);
					inputComboBox.getSelectionModel().selectFirst();
				}
			}
		}
				);

		datatypeComboBox.getSelectionModel().selectFirst();
		return 	datatypeComboBox; 	
	}

	/**
	 * Signalisiert dem Controller, dass die Eingabesequenz sortiert werden soll.
	 * Dafür werden Controller-Methoden zur Validierung der Eingabe benutzt
	 * und abschließend die Sortier-Methode im Controller mit allen benötigten Parametern aufgerufen.
	 * @return Gibt den Button zurück, mit dem die Sortier-Routine gestartet wird
	 */
	public Button getSortButton() {
		Button sortButton = new Button("Sortieren");
		sortButton.setOnAction((e) ->{

			String arrayInput = getArrayInputText();
			//Kann eigentlich nicht passieren
			if(arrayInput != null) {
				arrayInput = arrayInput.replaceAll("\\s+","");
				if(Controller.getInstance().checkInputForEmptyString(arrayInput)) {
					Alert alert = StyledDialogs.getAlert(AlertType.INFORMATION, "Sortieren", "Fehler in der Eingabe",
							"Eingabe ist leer");
					alert.showAndWait();
					return;
				}
				if(Controller.getInstance().checkInputForDatatype(arrayInput,getSelectedDatatype())) {
					Alert alert = StyledDialogs.getAlert(AlertType.INFORMATION, "Sortieren", "Fehler in der Eingabe",
							"Eingabe hat Elemente vom falschen Datentyp oder zu große Elemente");
					alert.showAndWait();
					return;
				}
				if(Controller.getInstance().checkInputForGaps(arrayInput)) {
					Alert alert = StyledDialogs.getAlert(AlertType.INFORMATION, "Sortieren", "Fehler in der Eingabe",
							"Eingabe hat leere Stellen.");
					alert.showAndWait();
					return;
				}
				if(Controller.getInstance().checkInputForLength(arrayInput)) {
					Alert alert = StyledDialogs.getAlert(AlertType.INFORMATION, "Sortieren", "Fehler in der Eingabe",
							"Eingabe ist zu groß.");
					alert.showAndWait();
					return;
				}
				if(getSelectedDatatype() == "String" && Controller.getInstance().checkInputForTooLargeStrings(arrayInput)) {
					Alert alert = StyledDialogs.getAlert(AlertType.INFORMATION, "Sortieren", "Fehler in der Eingabe",
							"Leider ist der Datentyp String derzeit noch auf einen Charakter pro Element beschränkt.");
					alert.showAndWait();
					return;
				}
				root.setCenter(getOutputScrollPane());
				Controller.getInstance().sort(arrayInput,getSelectedAlgo(),getSelectedDatatype());
				setActionsHBoxVisibility(true);
				resetArrayOutputPanes();
				Controller.getInstance().setViewElements();
			}
		});
		return sortButton;
	}

	/**
	 * Signalisiert dem Controller, dass der Ablauf des Sortierprozesses gespeichert werden soll.
	 * Dafür wird die Save-Methode im Controller mit allen benötigten Parametern aufgerufen.
	 * @return Gibt den Button zurück, mit dem die Save-(PDF)-Routine gestartet wird
	 */
	public Button getSaveButton() {
		Button saveButton = new Button("Erstelle PDF");
		saveButton.setOnAction((e) ->{
			//Noch nicht implementiert; Erweiterbar
			boolean steps = stepsCheckBox.isSelected();
			boolean pivot = pivotCheckBox.isSelected();
			boolean basicCase = basicCaseCheckBox.isSelected();
			boolean worstCaseProtection = worstCaseProtectionCheckBox.isSelected();

			if(Controller.getInstance().inputAlert()) {
				Alert alert = StyledDialogs.getAlert(AlertType.CONFIRMATION, "Speichern",
						"Mögliche Komplikationen",
						"Achtung! Die Eingabesequenz oder ein einzelner Wert ist möglicherweise zu groß "
						+ "für eine graphische Darstellung."
						+ "Möchten Sie trotzdem fortfahren?");
				Optional<ButtonType> save = alert.showAndWait();
				if(save.get() != ButtonType.OK) {
					return;
				}
			}
			String result = Controller.getInstance().save(steps,pivot,basicCase,worstCaseProtection,SaveOption.PDF,true);
			if(!result.isEmpty()) {
				Alert alert = StyledDialogs.getAlert(AlertType.INFORMATION, "Speichern", "Komplikation",
						"Die Speicherung war nicht erfolgreich. " + result);
				alert.showAndWait();
			}
		});
		return saveButton;
	}

	/**
	 * Noch nicht implementierte Funktion.
	 * Zukünftige Erweiterung sieht die Ausgabe als PDF-Dokument wie bei "Erstelle PDF" vor,
	 * jedoch in einem anderen Format. 
	 * @return Gibt den Button zurück, mit dem die Save-(BEAMER)-Routine gestartet wird
	 */
	public Button getBeamerButton() {
		Button beamerButton = new Button("Erstelle Beamer");
		beamerButton.setOnAction((e) ->{
			boolean steps = stepsCheckBox.isSelected();
			boolean pivot = pivotCheckBox.isSelected();
			boolean basicCase = basicCaseCheckBox.isSelected();
			boolean worstCaseProtection = worstCaseProtectionCheckBox.isSelected();
			Controller.getInstance().save(steps,pivot,basicCase,worstCaseProtection,SaveOption.BEAMER,true);
		});
		return beamerButton;
	}

	/**
	 * Signalisiert dem Controller, dass der Ablauf des Sortierprozesses als LaTeX-Code gespeichert werden soll.
	 * Dafür wird die Save-Methode im Controller mit allen benötigten Parametern aufgerufen.
	 * @return Gibt den Button zurück, mit dem die Save-(LATEX)-Routine gestartet wird
	 */
	public Button getLatexButton() {
		Button latexButton = new Button("Erstelle LaTeX");
		latexButton.setOnAction((e) ->{
			boolean steps = stepsCheckBox.isSelected();
			boolean pivot = pivotCheckBox.isSelected();
			boolean basicCase = basicCaseCheckBox.isSelected();
			boolean worstCaseProtection = worstCaseProtectionCheckBox.isSelected();
			Controller.getInstance().save(steps,pivot,basicCase,worstCaseProtection,SaveOption.LATEX,true);
		});
		return latexButton;
	}

	/**
	 * Signalisiert dem Controller, dass das TEMPLATE zur Erstellung eines PDF-Dokumentes aus dem LaTeX-Code geöffnet werden soll.
	 * Dafür wird die Save-Methode im Controller mit allen benötigten Parametern aufgerufen.
	 * @return Gibt den Button zurück, mit dem die Save-(TEMPLATE)-Routine gestartet wird
	 */
	public Button getTemplateButton() {
		Button templateButton = new Button("Öffne Template");
		templateButton.setOnAction((e) ->{
			boolean steps = stepsCheckBox.isSelected();
			boolean pivot = pivotCheckBox.isSelected();
			boolean basicCase = basicCaseCheckBox.isSelected();
			boolean worstCaseProtection = worstCaseProtectionCheckBox.isSelected();
			Controller.getInstance().save(steps,pivot,basicCase,worstCaseProtection,SaveOption.TEMPLATE,true);
		});
		return templateButton;
	}

	public TitledPane addArrayOutputPane(String text) {
		TitledPane arrayOutputPane = new TitledPane();
		arrayOutputPane.setText(text);
		outputAccordion.getPanes().add(arrayOutputPane);
		return arrayOutputPane;
	}

	public void resetArrayOutputPanes() {
		outputAccordion.getPanes().clear();
	}

	public void addArrayOutputContext(TitledPane pane, Collection<Node> c){
		VBox content = new VBox();
		content.getChildren().addAll(c);
		pane.setContent(content);
	}
	public void addArrayOutputContext(TitledPane pane, Collection<Node> c, Collection<Node> cNext){
		VBox content = new VBox();
		content.getChildren().addAll(c);
		Label seperator = new Label("---------- Übergang zu: ----------");
		content.getChildren().add(seperator);
		content.getChildren().addAll(cNext);
		pane.setContent(content);
	}

	public void addArrayOutput(String text) {
		Label outputLabel = new Label(text);
		VBox content1 = new VBox();
		content1.getChildren().add(outputLabel);
		TitledPane arrayOutputPane = new TitledPane();
		arrayOutputPane.setText("Array ...");
		arrayOutputPane.setContent(content1);
		outputAccordion.getPanes().add(arrayOutputPane);
	}

	public String getArrayInputText() {
		return arrayInput.getText();
	}
	public void setArrayInputText(String text) {
		arrayInput.setText(text);
	}

	public String getSelectedAlgo() {
		return algoComboBox.getSelectionModel().getSelectedItem().toString();
	}

	public String getSelectedDatatype() {
		return datatypeComboBox.getSelectionModel().getSelectedItem().toString();
	}   

	public String getSelectedInputOption() {
		return inputComboBox.getSelectionModel().getSelectedItem().toString();
	}
}