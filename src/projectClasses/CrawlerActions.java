package projectClasses;

import java.io.*;
import java.util.*;

/*
It is inherited by the Project Tester.
Consists of methods used by the crawler in the crawling process
 */
public interface CrawlerActions extends CrawlerHelpers {
    
    /*
returns the idf value of a word
:param uniqueword: a string for the word for which the idf is generated
:param saveFile: a boolean flag which decides whether or not to save the idf values in a file or not.(It is used for the initialization of the idf values storing file)
*/
static double generateIdf(String uniqueWord, Boolean saveFile){
    int wordCount = 0;
    File[] allFiles = CrawlerHelpers.get_url_list("webData");
    BufferedReader dataFile;
    String words;
    float totalUrlNum = (allFiles).length;
    for(File file : allFiles){
        try {
            dataFile = new BufferedReader(new FileReader("webData"+File.separator + file.getName()));
            words = dataFile.readLine();  //url
            words= dataFile.readLine();     //title
            words= dataFile.readLine();     //finally words
            if (words.contains(uniqueWord)){
                wordCount += 1;
            }
            dataFile.close();
        }catch (Exception e){
            e.printStackTrace();;
            return -1;
        }
    }
    double idf = Math.log(totalUrlNum / (1 + wordCount))/ Math.log(2);
    // System.out.println("idf: "+ idf);
    if (saveFile){
        PrintWriter uniqueIdfsFile;
        String filepath= "idf"+File.separator+uniqueWord + ".txt";
        try {
            uniqueIdfsFile = new PrintWriter(new FileWriter(filepath));
            uniqueIdfsFile.println(idf);
            uniqueIdfsFile.close();
        }catch (Exception e){
            e.printStackTrace();
        }
    }
        return 0;
}




static void generate_pageRank(String[] allUrls,List<String> urlIndexMap,HashMap<String, String[]> urlOutgoings){
    List<List<Double>> probabilityTransitionMatrix = generate_probabilityTransitionMatrix(urlIndexMap, urlOutgoings);
    List<List<Double>> scaledAdjacentMatrix = generate_scaled_adjacentMatrix(probabilityTransitionMatrix);
    List<List<Double>>  finalMatrix = generate_finalMatrix(scaledAdjacentMatrix);
    List<List<Double>> piVector = new ArrayList<>();
    piVector.add(new ArrayList<>());
    List<List<Double>> newVector = new ArrayList<>();
    piVector.get(0).add((double) 1);
    for(int counter=1; counter< urlIndexMap.size(); counter++){
        piVector.get(0).add((double) 0);
    }
    Double euclidianDist = 1.0d;
    while (euclidianDist > Constants.CONST_DROPTHRESHOLD){
        newVector = mult_matrix(piVector, finalMatrix);
        euclidianDist = euclidean_dist(piVector, newVector);
        piVector=newVector;
    }
    // System.out.println(piVector);
    for(String doc: allUrls){
        String link = CrawlerHelpers.changeFilenameToLink(doc).replace(".txt", "");
       
        try{
            PrintWriter pageRankFile= new PrintWriter(new FileWriter("pageRank"+File.separator+doc));
            pageRankFile.println(piVector.get(0).get(urlIndexMap.indexOf(link)));
            pageRankFile.close();
        }
        catch(Exception e) {
            return;
        }
    }
}

/*
It generates a new matrix after scalar multiplication with the generated adj matrix with a factor of (1-Alpha)
:param probabilityTransitionMatrix: 2-D matrix which already has all the probabilitities mapped for all links
*/
static List<List<Double>> generate_scaled_adjacentMatrix(List<List<Double>> probabilityTransitionMatrix) {
    List<List<Double>> newMatrix = new ArrayList<>();
    newMatrix.addAll(probabilityTransitionMatrix);
    int rowIndex=0;
    for (List<Double> row : (probabilityTransitionMatrix)){
        int itemIndex=0;
        for (double item : (row)){
            newMatrix.get(rowIndex).set(itemIndex, item * (1 - Constants.CONST_ALPHA));
            itemIndex++;
        }
        rowIndex++;
    }
    return newMatrix;
}


/*
It generates a link transition matrix from the given urlIndexMap.
:param urlIndexMap: A dictionary that maps a URL to an index.
:param urlOutgoings: A dictionary that maps a url to its outgoing links
*/
static List<List<Double>> generate_probabilityTransitionMatrix(List<String> urlIndexMap,HashMap<String, String[]> urlOutgoings) {
    int totalPages = (urlIndexMap).size();
    List<List<Double>> adjacencyMatrix= new ArrayList<>();
    for (String pageUrl : urlIndexMap) {
        String[] urlOutgoingLinks = urlOutgoings.get(pageUrl);
        float totalOnes = (urlOutgoingLinks).length;
        List<Double> newMatrix= new ArrayList<>();
        int availableones = (urlOutgoingLinks).length;
        if (totalOnes == 0){  //if no put going links then all elements = 1 / N
            for(int index=0; index<totalPages; index++) {
                newMatrix.add(0d);
            }
        }
        else {
            //            for pageIndex, page in enumerate (urlIndexMap)
            int pageIndex=0;
            for (String page: urlIndexMap) {
                if (availableones == 0) { //To make it a bit efficient as if no more 1 s is left we know all that follows is 0 and we can terminate loop
                    for (int index = 0; index < totalPages - pageIndex; index++) {
                        newMatrix.add(0d);
                    }
                    break;
                }
                if (Arrays.asList(urlOutgoingLinks).contains(page)) {
                    newMatrix.add((double)1 / totalOnes);
                    availableones -= 1;
                } else {
                    newMatrix.add(0d);
                }
                pageIndex++;
            }
        }
        adjacencyMatrix.add(newMatrix);
    }
    return adjacencyMatrix;
}

/*
It generates the final matrix after considering the random transport value and adds it to the result of (1-Alpha)*probabilitytransitionMatrix
:param scaledAdjacentMatrix: result matrix of (1-Alpha)*probabilitytransitionMatrix
*/
static List<List<Double>> generate_finalMatrix(List<List<Double>> scaledAdjacentMatrix) {
    List<List<Double>> newMatrix = new ArrayList<>();
    newMatrix.addAll(scaledAdjacentMatrix);
    Float matrixLength = (float) (scaledAdjacentMatrix).size();
    int rowIndex=0;
    for (List<Double> row : (scaledAdjacentMatrix)){
        for (int itemIndex=0; itemIndex< row.size();itemIndex++ ) {
            newMatrix.get(rowIndex).set(itemIndex,newMatrix.get(rowIndex).get(itemIndex)+ (double) (Constants.CONST_ALPHA / matrixLength));
        }
        rowIndex++;
    }
    return newMatrix;
}

/*
It multiplies two matrices together.
:param a,b: matrices to be multiplied
*/
static List<List<Double>> mult_matrix(List<List<Double>> matrixX, List<List<Double>> matrixY) {
    List<List<Double>> resultMatrix = new ArrayList<>();
    resultMatrix.add(new ArrayList<>());
    for (int coly=0; coly< (matrixY.get(0)).size(); coly++ ){
        Double colValue = 0.0d;
        for (int rowy=0; rowy <(matrixY).size(); rowy++){
            colValue += matrixX.get(0).get(rowy) * matrixY.get(rowy).get(coly);
        }
        resultMatrix.get(0).add((double) colValue);
    }
    return resultMatrix;
}

/*
Finds the euclidian distance between two matrices
:param a,b: matrices of whose the euclidian distance is to be calculated
*/
static Double euclidean_dist(List<List<Double>> a, List<List<Double>> b) {
    Double eDistance = 0.0d;
    if ((a).size() > 1 || (b).size() > 1){
        return -1.0d;
    }
    for (int counter=0; counter<a.size(); counter++){
        eDistance += (double)Math.pow((a.get(0).get(counter) - b.get(0).get(counter)), 2);
    }
    return (double)Math.pow(eDistance, 0.5);
}



}
