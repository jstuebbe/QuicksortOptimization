# QuicksortOptimierungen
Code von Quicksort und Quicksort-Optimierungen aus der Abschlussarbeit:
"Optimierungsansätze für das Quicksort-Verfahren"
von Jonas Stübbe (verfügbar auf https://github.com/jstuebbe/QuicksortOptimization).

<!-------------------------------------->

Dieses Programm dient der didaktischen Darstellung verschiedener Sortierverfahren. Für die beste Nutzererfahrung wird der Pfad zu der 'pdflatex' Installation des Nutzers benötigt. Diese kann auf linux oder MacOS Systemen zum Beispiel durch den Terminal-Befehl 'which pdflatex' herausgefunden werden.

<!-------------------------------------->

mvn clean
mvn test
mvn package
java -jar target/Quicksort-1.jar
<!- Wichtig: compile und javadoc:javadoc zusammen ausführen ->
mvn compile javadoc:javadoc
<!- javadoc zu finden im Ordner /target/site/apidocs/de.wwu.Quicksort/de/wwu/Quicksort ->

<!-------------------------------------->