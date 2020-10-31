package de.wwu.Quicksort.sorters;

import java.util.ArrayList;
import java.util.List;

import de.wwu.Quicksort.sorters.TexStringGenerator.TextFontOptions;
import de.wwu.Quicksort.sorters.TexStringGenerator.TextLineOptions;

/**
 * Oberklasse alle Sortierverfahren, stellt benötigte Hilfsmittel und Methoden bereit,
 * um aus den aus dem Sortierprozess entstehenden Sortierschritten einen LaTeX-Code zu erstellen.
 * @author Jonas Stübbe im Zuge der BA
 *
 * Da diese Methode nach und nach entstanden ist und der Umfang dieser BA leider keine ausführliche vorherige Planung erlaubt hat,
 * ist diese Klasse leider nicht die sauberste.
 * Dies ist mir voll und ganz bewusst, dies ist die erste Klasse, welche in einem weiteren Verlauf der Programmentwicklung bearbeitet würde. 
 *
 * @param <I>
 */
public abstract class TexStringGenerator<I extends Comparable<I>> implements Sorter<I> {

	public enum TextLineOptions 
	{
		TEXT, NEW_LINE_TEXT, NEW_LINE_TEXT_NEW_LINE, TEXT_NEW_LINE
	}
	public enum TextFontOptions
	{
		TEXT, BOLD
	}

	@Override
	public String getTexIntro() {
		String texIntro = "";
		String fullName = this.getClass().getName();
		String sorter = fullName.substring(25,fullName.length()-6);
		texIntro += addText("Sortierverfahren: " + sorter + ".", TextLineOptions.TEXT_NEW_LINE, TextFontOptions.TEXT);
		texIntro += addText("Für weitere Details sei auf die begleitende Arbeit verwiesen \\url{https://github.com/jstuebbe/QuicksortOptimization}", TextLineOptions.TEXT_NEW_LINE, TextFontOptions.TEXT);
		return texIntro;
	}
	
	public String getArray(String[] input, ArrayList<Integer> pivots, String[] colours) {
		String texString = "";
		int size = input.length;
		boolean pivotCheck = false;
		if(pivots != null) {
			for(int pivot : pivots) {
				if(0 == pivot) {
					pivotCheck = true;
				}
			}
		}
		if(pivotCheck) {
			texString += "\\hline \\cellcolor{";
			texString += colours != null ? colours[0] : "dunkelgrau";
			texString += "}" + input[0];
		}
		else {
			texString += "\\hline " + input[0]; 
		}
		for(int i =1; i < size; i++) {
			pivotCheck = false;
			if(pivots != null) {
				for(int pivot : pivots) {
					if(i == pivot) {
						pivotCheck = true;
					}
				}
			}
			if(pivotCheck) {
				texString += "& \\cellcolor{";
				texString += colours != null ? colours[0] : "dunkelgrau";
				texString += "}" + input[i];
			}
			else {
				texString += "&" + input[i];
			}
		}
		return texString;
	}

	public String getArray(String[] input,ArrayList<Integer> pivots, Integer blocksize,Integer blocksizeOffsetL, String[] colours) {
		String texString = "";
		int size = input.length;

		boolean pivotCheck = false;
		if(pivots != null) {
			for(int pivot : pivots) {
				if(0 == pivot) {
					pivotCheck = true;
				}
			}
		}
		if(pivotCheck) {
			texString += "\\hline \\cellcolor{";
			texString += colours != null ? colours[0] : "dunkelgrau";
			texString += "}" + input[0];
		}
		else if(blocksizeOffsetL == 0){
			texString += "\\hline \\cellcolor{";
			texString += colours != null ? colours[1] : "gold";
			texString += "}" + input[0];
		}
		else {
			texString += "\\hline " + input[0];
		}
		for(int i = 1; i < size; i++) {

			pivotCheck = false;
			if(pivots != null) {
				for(int pivot : pivots) {
					if(i == pivot) {
						pivotCheck = true;
					}
				}
			}
			if(pivotCheck) {
				texString += "& \\cellcolor{";
				texString += colours != null ? colours[0] : "dunkelgrau";
				texString += "}" + input[i];
			}
			else if(blocksizeOffsetL <= i && i < blocksize+blocksizeOffsetL) {
				texString += "& \\cellcolor{";
				texString += colours != null ? colours[1] : "gold";
				texString += "}" + input[i];
			}
			else {
				texString += "&" + input[i];
			}
		}
		return texString;
	}

	public String getArray(String[] input,Integer pivot, Integer blocksize,Integer blocksizeOffsetL,Integer blocksizeOffsetR, String[] colours) {
		String texString = "";
		int size = input.length;

		if(pivot == 0) {
			texString += "\\hline \\cellcolor{";
			texString += colours != null ? colours[0] : "dunkelgrau"; 
			texString += "}" + input[0];
		}
		else if(blocksizeOffsetL == 0){
			texString += "\\hline \\cellcolor{";
			texString += colours != null ? colours[1] : "gold"; 
			texString += "}" + input[0];
		}
		else {
			texString += "\\hline " + input[0];
		}
		for(int i = 1; i < size; i++) {
			if(i == pivot) {
				texString += "& \\cellcolor{" 
						+ (colours != null ? colours[0] : "dunkelgrau") 
						+ "}" + input[i];
			}
			else if(blocksizeOffsetL <= i && i < blocksize+blocksizeOffsetL) {
				texString += "& \\cellcolor{";
				texString += colours != null ? colours[1] : "gold";
				texString += "}" + input[i];
			}
			else if(size - 1 - blocksizeOffsetR - blocksize <= i && i < size - 1 - blocksizeOffsetR) {
				texString += "& \\cellcolor{"; 
				texString += colours != null ? colours[1] : "gold"; 
				texString += "}" + input[i];
			}
			else {
				texString += "&" + input[i];
			}
		}
		return texString;
	}

	public String getPointers(List<Integer> pointerIndex, List<String> pointerText,int size,boolean isPointer) {
		String texString = "";
		//erstelle String array mit größe von input
		String[] pointers = new String[size];
		int listSize = pointerIndex.size();
		if(listSize != pointerText.size()) {
			throw new IllegalArgumentException("Größe der Pointer ist unterschiedlich");
		}
		for(int i = 0; i < listSize; i++) {
			int index = pointerIndex.get(i);
			if(index > size || index < 0) {
				throw new IllegalArgumentException("index > size oder index < 0");
			}
			String text = pointerText.get(i);
			if(text == null || text.trim().isEmpty()) {
				throw new IllegalArgumentException("pointerText falsch befüllt");
			}
			if(pointers[index] == null) {
				pointers[index] = text;
			}
			else {
				pointers[index] += "," + text;
			}

		}
		for(int i = 0; i < size ; i++) {
			if(i != 0) {
				texString += " & ";
			}
			int counter = 0;
			while(i < size && pointers[i] == null) {
				counter++;
				i++;
			}
			if(counter == 0) {
				if(isPointer == true) {
					texString += " \\multicolumn{1}{c}{" + pointers[i] + "} ";
				}
				else {
					//bei zwei puffern listSize/2
					if(i + 1 == listSize/2 || i + 1 == size || i + 1 == listSize) {
						texString += " \\multicolumn{1}{|c|}{" + pointers[i] + "} ";
					}
					else {
						texString += " \\multicolumn{1}{|c}{" + pointers[i] + "} ";
					}

				}
			}
			else {
				texString += " \\multicolumn{" + counter + "}{c}{} ";
				i--;
			}
		}
		texString += " \\\\ ";	
		return texString;
	}

	public String getTree(String[] input) {
		String texString = "";
		int size = input.length;
		texString += " \\begin{tikzpicture} ";
		//höhe des baumes: log_2(size)
		int  depth  = (int) Math.ceil(Math.log(size) / Math.log(2));
		//berechne maximale breite des baumes:
		int maxWidth = (int) Math.pow(2, depth-1);
		int counter = 0;
		int y = depth - 1;
		int width;
		for(int z = 0; z < size; z++) {
			width = (int) Math.pow(2, depth - 1 - y );
			if(counter == width) {
				counter = 0;
				y--;
			}
			double x = ((maxWidth - 1) / (Math.pow(2, depth - y))) * ((counter*2) + 1);
			texString += "\\node[draw] at (" + x + ", " + y + ") (" + (char) (z+97) + "){" + input[z].toString() + "};";
			counter++;
		}
		char c = 'a';
		char cTo = 'b';
		boolean cInc = false;
		for(int z = 0; z < size-1;z++) {
			texString += "\\draw[] (" + (char)c + ") -- (" + (char)(cTo) + ");";
			cTo++;
			if(cInc) {
				c++;
			}
			cInc = !cInc;
		}
		texString += " \\end{tikzpicture} \\\\ ";
		return texString;	
	}

	public String beginTable(int size,ArrayList<Integer> seperators) {
		String texString = " \\begin{tabular}{";;
		if(seperators != null && seperators.contains(0)) {
			texString += "x{2pt}";
		}
		else {
			texString += "|";
		}
		for(int i = 1; i < size + 1; i++) {
			if(seperators != null && seperators.contains(i)) {
				texString += "cx{2pt}";
			}
			else {
				texString +=  "c|";
			}
		}
		texString += "}";
		return texString;
	}

	public String beginTable(int size, Integer blocksize,Integer blocksizeOffsetL,Integer blocksizeOffsetR) {
		String texString = " \\begin{tabular}{";
		if(blocksize != null) {
			if(blocksizeOffsetL == 0) {
				texString += "x{2pt}";
			}
			else {
				texString += "|";
			}
			for(int i = 1; i < size + 1; i++) {
				if(i == blocksize + blocksizeOffsetL) {
					texString += "cx{2pt}";
				}
				else if(i == blocksizeOffsetL){
					texString += "cx{2pt}";
				}
				else if(size - 1 - i == blocksize + blocksizeOffsetR) { //-2 statt -1 da letztes Element Pivot Element ist.
					texString += "cx{2pt}";
				}
				else if(size - 1 - i == blocksizeOffsetR) {
					texString += "cx{2pt}";
				}
				else {
					texString +=  "c|";
				}
			}
		}
		else {
			texString += "|";
			for(int z = 0; z < size;z++) {
				texString += "c|";
			}
		}	
		texString += "}";
		return texString;
	}

	public String endTable() {
		String texString = "\\\\ \\hline \\end{tabular} \\\\";
		return texString;
	}

	public String seperateTable() {
		String texString = "\\\\ \\hline";
		return texString;
	}

	public String getPuffers(String[] pufferL, String[] pufferR, int size, int blocksize, Integer start_left, Integer start_right) {
		String texString = "";
		if(pufferL != null || pufferR != null) {
			texString += "\\begin{tabular}{|";
			for(int i = 0; i < blocksize; i++) {
				texString +=  "c|";
			}
			for(int i = blocksize;i < size - blocksize - 1;i++) {
				texString +=  "c";
			}
			for(int i = size - blocksize - 1; i < size; i++) {
				texString +=  "c|";
			}

			texString += "}"; 
			//multicolum
			if(start_left != null || start_right != null) {
				if(start_left == null) {
				}
				else if (start_right == null) {
				}
				else {
					if(start_left == 0) {
						texString += "\\multicolumn{1}{c}{$\\downarrow$} &";
					}
					else {
						texString += "\\multicolumn{" + (start_left) + "}{c}{} &";
						texString += "\\multicolumn{1}{c}{$\\downarrow$} &";
					}
					texString += "\\multicolumn{" + (size - (start_left + 1) - (blocksize - start_right) ) + "}{c}{} &";


					texString += "\\multicolumn{1}{c}{$\\downarrow$} ";

					if(blocksize != start_right + 1) {
						texString += "& \\multicolumn{" + (blocksize - (start_right + 1) ) + "}{c}{} ";
					}
					texString += "\\\\";
				}
			}
			texString += "\\cline{1-" + blocksize + "} \\cline{" + (size - blocksize + 1) + "-" + size +"} \\\\";
			if(blocksize > 0) {
				texString += pufferL[0];
			}
			for(int i =1; i < blocksize; i++) {
				texString += "&";
				if(pufferL != null) {
					texString += pufferL[i];
				}
			}
			for(int i = blocksize; i < size - blocksize; i++) {
				texString += "&";
			}
			for(int i = 0 ; i < blocksize; i++) {
				texString += "&";
				if(pufferR != null) {
					texString += pufferR[i];
				}
			}
			texString += "\\\\";
			texString += " \\cline{1-" + blocksize + "} \\cline{" + (size-blocksize + 1) + "-" + size +"} \\\\";
			//multicolum
			if(start_left != null || start_right != null) {
				if(start_left == null) {
				}
				else if (start_right == null) {
				}
				else {			
					if(start_left == 0) {
						texString += "\\multicolumn{1}{c}{offsets$_L$} &";
					}
					else {
						texString += "\\multicolumn{" + (start_left) + "}{c}{} &";
						texString += "\\multicolumn{1}{c}{offsets$_L$} &";
					}
					texString += "\\multicolumn{" + (size - (start_left + 1) - (blocksize - start_right) ) + "}{c}{} &";


					texString += "\\multicolumn{1}{c}{offsets$_R$} ";

					if(blocksize != start_right + 1) {
						texString += "& \\multicolumn{" + (blocksize - (start_right + 1) ) + "}{c}{} ";
					}
					texString += "\\\\";
				}
			}

			texString += "\\end{tabular} \\\\";
		}
		return texString;
	}

	public String beginPufferTable(Integer size,Integer blocksize) {
		String texString = "";
		texString += "\\begin{tabular}{|";
		for(int i = 0; i < blocksize; i++) {
			texString +=  "c|";
		}
		for(int i = blocksize;i < size - blocksize - 1;i++) {
			texString +=  "c";
		}
		for(int i = size - blocksize - 1; i < size; i++) {
			texString +=  "c|";
		}
		texString += "}";
		return texString;
	}

	public String endPufferTable() {
		String texString = "";
		texString += "\\end{tabular} \\\\";
		return texString;
	}

	public String getCline(Integer size, Integer blocksize, boolean lomuto) {
		if(lomuto) {
			return "\\cline{1-" + blocksize + "}" ;
		}
		else {
			return "\\cline{1-" + blocksize + "} \\cline{" + (size - blocksize + 1) + "-" + size +"}";	
		}
	}


	public String addText(String text, TextLineOptions lineOption, TextFontOptions fontOption) {
		String texString = "";

		if(lineOption == TextLineOptions.NEW_LINE_TEXT || lineOption == TextLineOptions.NEW_LINE_TEXT_NEW_LINE) texString += "\\\\";

		switch (fontOption) {
		case TEXT:
			texString += text;
			break;
		case BOLD:
			texString += "\\textbf{" + text + "}";
			break;
		default:
			throw new IllegalArgumentException("Unbekannte fontOption");
		}
		if(lineOption == TextLineOptions.TEXT_NEW_LINE || lineOption == TextLineOptions.NEW_LINE_TEXT_NEW_LINE) texString += "\\\\";
		return texString; 
	}

	public String addVSpace(double mm) {
		return "\\vspace{"+mm+"mm} \\\\";
	}
}
