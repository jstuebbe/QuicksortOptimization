package de.wwu.Quicksort.utils;

import java.util.ArrayList;
import java.util.Random;

import javafx.util.Pair;

public class TestUtils {

	/*
	 * einfacher Sortieralgorithmus (z.B. hier Insertionsort)
	 * 
	 */
	public static <I extends Comparable<I>> void sort(ArrayList<I> input) {
		for(int i=0;i<input.size();i++) {
			I temp = input.get(i);
			int j = i;
			while(j > 0 && input.get(j-1).compareTo(temp) > 0) {
				input.set(j, input.get(j-1));
				j = j-1;
			}
			input.set(j, temp);
		}
	}
	
	public static char getRndChar() {
		int rnd = (int) (Math.random() * 52); // or use Random or whatever
		char base = (rnd < 26) ? 'A' : 'a';
		return (char) (base + rnd % 26);
	}
	
	public static Pair<String,ArrayList<String>> getStringArrayString(int size,Integer bound) {
		Random rand = new Random();
		String  stringInputString = "";
		ArrayList<String>  stringArrayList = new ArrayList<String>();

		for(int i=0;i<size;i++) {
			if(i != 0) {
				stringInputString += ",";
			}
			int length;
			if(bound != null) {
				length = rand.nextInt(bound) + 1;
			}
			else {
				length = rand.nextInt(9) + 1;	
			}

			String characters = "";
			for(int j = 0; j < length; j++) {
				characters += Integer.toString(TestUtils.getRndChar());
			}
			stringInputString += characters;
			stringArrayList.add(characters);
		}
		return new Pair<String,ArrayList<String>>(stringInputString,stringArrayList);
	}
	
	public static Pair<String,ArrayList<Integer>> getIntegerArrayString(int size,Integer bound) {
		Random rand = new Random();
		String  integerInputString = "";
		ArrayList<Integer>  integerArrayList = new ArrayList<Integer>();

		for(int i=0;i<size;i++) {
			if(i != 0) {
				integerInputString += ",";
			}
			int randInt;
			if(bound != null) {
				randInt = rand.nextInt(bound);
			}
			else {
				randInt = rand.nextInt();	
			}

			integerArrayList.add(randInt);
			integerInputString += Integer.toString(randInt);
		}
		return new Pair<String,ArrayList<Integer>>(integerInputString,integerArrayList);
	}
	
	public static ArrayList<Integer> getIntegerArrayList(int size) {
		Random rand = new Random();
		ArrayList<Integer> arrayList = new ArrayList<Integer>();

		for(int i=0;i<size;i++) {
			arrayList.add(rand.nextInt());
		}
		return arrayList;
	}

	public static ArrayList<String> getStringArrayList(int size) {
		Random rand = new Random();
		ArrayList<String> arrayList = new ArrayList<String>();

		for(int i=0;i<size;i++) {
			int length = rand.nextInt(9) + 1;
			String str = "";
			for(int j = 0; j < length; j++) {
				str += TestUtils.getRndChar();
			}
			arrayList.add(str);
		}
		return arrayList;
	}
	
	public static ArrayList<Integer> getIntegerArrayListLimitedCharacters(int characterLimit,int maxSize) {
		Random rand = new Random();
		ArrayList<Integer> arrayList = new ArrayList<Integer>();

		int i = 0;
		while(i < characterLimit) {
			Integer randInt = rand.nextInt((int) (9 * (Math.pow(10,maxSize - 1))));
			arrayList.add(randInt);
			i += randInt.toString().length();
		}
		return arrayList;
	}

	public static ArrayList<String> getStringArrayListLimitedCharacters(int characterLimit,int maxSize) {
		Random rand = new Random();
		ArrayList<String> arrayList = new ArrayList<String>();

		int i = 0;
		while(i < characterLimit) {
			String randString = "";
			int size = rand.nextInt(maxSize);
			for(int j = 0; j < size; j++) {
				randString += TestUtils.getRndChar();
			}
			arrayList.add(randString);
			i += size;
		}
		return arrayList;
	}

	
}
