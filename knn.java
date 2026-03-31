// käännetään ada-kurssilla tehty knn-algoritmi pythonista javalle
// wlan-datassa (2000 riviä) kaikki arvot ovat numeroita (tosin viimeinen = huoneen nro)

// knn-algorithm, ie. machine learning and predictions with k nearest neighbours
// testing with wireless indoor localization data set

// these are needed to read data from a text file
import java.io.File;  // import the File class to open a file
import java.io.FileNotFoundException;  // import this class to handle errors
import java.util.Scanner; // import the Scanner class to read text files

import java.util.ArrayList; // import the ArrayList class to handle all the data
import java.lang.Math; // import Math to calculate distances

public class knn{
	
	// annettu data tekstinä ja numeromuodossa:
	static ArrayList<String> dataSet = new ArrayList<String>(); // alkuperäinen string-muotoinen data, yksi rivi = yksi string
	static ArrayList<ArrayList<String>> dataSetWords = new ArrayList<ArrayList<String>>(); // data string-muodossa ja pilkuilla jaettuna
	static ArrayList<ArrayList<Float>> dataSetValues = new ArrayList<ArrayList<Float>>(); // numeromuotoon muunnettu data
		// käytetyn datan joka rivin viimeinen solu on se, jota koetetaan päätellä muiden vastaavien rivien perusteella
		// tämä viimeinen solu on korvattu rivinumerolla, jotta ennustetta  voidaan myöhemmin verrata alkuperäiseen arvoon
		// Sama rivinumero kulkeutuu siis myös trainingSettiin ja testSettiin!!
	static ArrayList<String> rightAnswers = new ArrayList<String>(); // datasetistä edellä poistetut viimeiset solut kopioidaan tähän

	// jaettu kahtia leikkaamalla poikki sopivasta kohdasta:
	static ArrayList<ArrayList<Float>> trainingSet = new ArrayList<ArrayList<Float>>(); // opetukseen käytettävä data
	static ArrayList<ArrayList<Float>> testSet = new ArrayList<ArrayList<Float>>(); // testaukseen käytettävä data

	// jaettu kahtia hieman nätimmin:
	static ArrayList<ArrayList<Float>> trainingSetD = new ArrayList<ArrayList<Float>>(); // opetukseen käytettävä data
	static ArrayList<ArrayList<Float>> testSetD = new ArrayList<ArrayList<Float>>(); // testaukseen käytettävä data
	
	public static void main(String args[]){
		long startTime = System.currentTimeMillis();
		openDataFile();
		convertToCsv();
		// splitDataSet(); // don't use this, next one is better
		divideDataSet();

		ArrayList<ArrayList<Float>> neighbors = new ArrayList<ArrayList<Float>>();
		ArrayList<String> predictions = new ArrayList<String>();
		int k=9; // etsitään k lähintä naapuria
		// int k = args[0]; // naapurien määrä voidaan ottaa myös syötteenä
		
		// etsi jokaiselle testsetin riville k lähintä naapuria trainingsetistä
		// etsi sitten naapureiden yleisin arvo ja tallennetaan se rivin ennustetuksi arvoksi
		for (int i=0; i<testSetD.size(); i++){
			neighbors = getNeighbors(testSetD.get(i), k);
			predictions.add( getResponse(neighbors) );
		} //for(i)

		// lasketaan, miten iso osa saaduista tuloksista oli oikein (vastaus välillä 0...1)
		float accuracy = getAccuracy(predictions);
		// muutetaan edellinen luku prosenteiksi
		float res = accuracy*10000;
		res = Math.round(res);
		res = res/100;
		long stopTime = System.currentTimeMillis();
		int runnigTime = (int) (stopTime-startTime);

		// saatu tulos tulostetaan ruudulle:
		System.out.println("There is " + dataSet.size() + " rows in your data");
		System.out.println("The amount of right answers is " + res + "%.");
		System.out.println("Running time of this algorithm is " + runnigTime + "ms");

	} //main()
	
	// open and read the data file
	public static void openDataFile(){
		try {
			File dataFile = new File("wifi_localization_data.txt");
			Scanner dataReader = new Scanner(dataFile);
			while(dataReader.hasNextLine()){
				dataSet.add( dataReader.nextLine() );
			} //while
			dataReader.close();
		} //try
		catch (FileNotFoundException e) {
			System.out.println("file was not found");
		} //catch
	} //openDataFile()

	// convert the string-formed data to csv-format and numbers
	public static void convertToCsv(){
		String delimiter = ",";
		String row; // yksi tiedostosta luettu rivi yhtenä pötkönä
		ArrayList<String> words = new ArrayList<String>(); // edellinen pätkittynä pilkkujen kohdalta
		ArrayList<Float> numbers = new ArrayList<Float>(); // edellinen muutettuna numeromuotoon
		float tempNumber = 0.0f;

		// käy datan läpi rivi kerrallaan:
		for(int i=0; i<dataSet.size(); i++){
			row = dataSet.get(i);
			words = new ArrayList<String>();
			numbers = new ArrayList<Float>();
			Scanner rowScanner = new Scanner(row);
			rowScanner.useDelimiter(delimiter);
			while (rowScanner.hasNext()) {
	            words.add(rowScanner.next());
	        } //while
			if(words.size()>0){
				// jokainen pilkulla erotettu merkkijono muutetaan float-luvuksi:
				// (paitsi viimeinen, joka on sana)
				for(int j=0; j<words.size()-1; j++){
					tempNumber = Float.parseFloat( words.get(j) );
					numbers.add( tempNumber );
				} //for(j)
				// lisätään rivinumero joka rivin loppuun, jotta löydetään sama rivi myöhemminkin:
				tempNumber = i;
				numbers.add( tempNumber );
			} //if
			dataSetWords.add(words);
			dataSetValues.add(numbers);
		} //for(i)
	} //convertToCsv()

	// split the data to training and testing sets 		!!! DON'T USE THIS !!!
	// very bad idea, if data is ordered by the same value we are interested about
	public static void splitDataSet(){
		int split = (int)dataSetValues.size()/3;

		// kopioidaan alkuosa trainingSettiin...
		for (int i=0; i<split*2; i++){
			trainingSet.add( dataSetValues.get(i) );
		}
		// ... ja loppuosa testisettiin
		for (int i=split*2; i<dataSetValues.size(); i++){
			testSet.add( dataSetValues.get(i) );
			ArrayList<String> thisRow = dataSetWords.get(i);
			rightAnswers.add( thisRow.get(thisRow.size()-1) );
		}
	} //splitDataSet()

	// divide the data to two sets
	// a bit clever way than direct split
	public static void divideDataSet(){
		ArrayList<String> thisRow = new ArrayList<String>();
		for (int i=0; i<dataSetValues.size(); i++){
			if(i%3==0){
				thisRow = new ArrayList<String>();
				testSetD.add( dataSetValues.get(i) );
				thisRow = dataSetWords.get(i);
				rightAnswers.add( thisRow.get(thisRow.size()-1) );
			}
			else {
				trainingSetD.add( dataSetValues.get(i) );
			}
		} //for(i)
	} //divideDataSet()

/**	// data can be divided randomly, too:
	public static void splitDataSetRandomly(){}
*/

	// find k nearest neighbors of testData from trainingSet
	public static ArrayList<ArrayList<Float>> getNeighbors(ArrayList<Float> testRow, int k){
		double[] distances = new double[ trainingSetD.size() ];
		int[] away = new int[ trainingSetD.size() ];
		ArrayList<ArrayList<Float>> neighbors = new ArrayList<ArrayList<Float>>();

		// calculate the dstance of testRow and each row on training data
		for(int i=0; i<trainingSetD.size(); i++){
			distances[i] = getDistance(trainingSetD.get(i), testRow);
		}
		// how many distance is smaller than distance[i]
		for(int i=0; i<distances.length; i++){
			for(int j=0; j<distances.length; j++){
				if( distances[i] > distances[j] ) { away[i]++; }
			}
		}
		// find k nearest neighbors for testData
		for(int i=0; i<distances.length; i++){
			if( away[i]<k ){
				neighbors.add( trainingSetD.get(i) );
			}
		}
		return neighbors;
	} //getNeighbors(ArrayList<Float>, int)

	// calculate the distance of given points
	public static double getDistance(ArrayList<Float> node1, ArrayList<Float> node2){
		double dist = 0.0;
		// noden viimeinen luku on rivin järjestysnumero, sitä ei pidä ottaa tässä huomioon:
		for(int i=0; i<node1.size()-1; i++){
			dist += (node1.get(i)-node2.get(i))*(node1.get(i)-node2.get(i));
		}
		return Math.sqrt(dist);
	} //getDistance(ArrayList<Float>, ArrayList<Float>)

	// find the most common value (room number) of neighbors
	public static String getResponse(ArrayList<ArrayList<Float>> nodes){
		ArrayList<String> rooms = new ArrayList<String>();
		ArrayList<Integer> votes = new ArrayList<Integer>();
		ArrayList<Float> floatRow = new ArrayList<Float>();
		ArrayList<String> stringRow = new ArrayList<String>();
		int rowNumber = 0;
		int roomNumber = 0;
		String word = "";
		// lasketaan ensin, montako kertaa mikäkin huone esiintyy naapurien joukossa:
		for(int i=0; i<nodes.size(); i++){
			floatRow = nodes.get(i);
			rowNumber = Math.round( floatRow.get( floatRow.size()-1 ) );
			stringRow = dataSetWords.get(rowNumber);
			word = stringRow.get( stringRow.size()-1 );
			roomNumber = rooms.indexOf(word);
			if( roomNumber == -1 ){
				rooms.add( word );
				votes.add( 1 );
			}
			else{
				votes.set( roomNumber, votes.get(roomNumber)+1 );
			}
		}//for()
		// etsitään useimmin mainittu huone ja palautetaan se
		int winnerVotes = 0;
		String winner = "";
		for(int i=0; i<rooms.size(); i++){
			if( votes.get(i)>winnerVotes ){
				winnerVotes = votes.get(i);
				winner = rooms.get(i);
			}
		}
		return winner;
	} //getResponse(ArrayList<ArrayList<Float>>)

	// calculate the accuracy of predictions, ie how many answer was correct
	// returns the accuracy between 0.0-1.0
	public static float getAccuracy(ArrayList<String> preds){
		int counter = 0;
		int lkm = preds.size();
		for(int i=0; i<lkm; i++){
			if( (preds.get(i)).equals(rightAnswers.get(i)) ){ counter++; }
		}
		counter = counter*1000000;
		float res = counter/lkm;
		res = res/1000000;
		return res;
	}// getAcuracy(ArrayList<String>)
	
} //knn