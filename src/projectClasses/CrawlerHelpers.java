package projectClasses;

import java.io.*;
import java.nio.file.*;
import java.util.*;

public class CrawlerHelpers {
    //a class that is used by the helper functions for minute tasks. These are not instance specific.

    static boolean check_dir_exists(String dirname){
        Path path = Paths.get(dirname);
        return ( Files.isDirectory(path));
    };

    /*
       Adds the word, title and links data in json file for every url
        //:param url: url from which the data has been crawled
        //:param Data: a record object that stored the parsed word, url, title for a url
    */
    public static void addDataToFile(String url, Data parsedData){
        String filename = changeLinkToFileName(url) + ".txt";
        PrintWriter file;
        try {
            file = new PrintWriter(new FileWriter("webData/" + filename));
            file.println(url);
            file.println(parsedData.getTitle());
            file.println(parsedData.getWords());
            file.print(parsedData.getLinks());

            file.close();
        }
        catch(IOException e) {
            e.printStackTrace();
            return;
        }
    }


    //It changes the link to the file name by replacing unsupported symbols as in the symbolsMap variable.
//:param link: link to be renamed
    public static String changeLinkToFileName( String link) {
        for (String key: Constants.symbolsMap.keySet()){
            link= link.replace(key, Constants.symbolsMap.get(key));
        }
        return link;
    }

    /*
    It takes the link that had some unsupported symbols so that it could be used as a folder name and returns the actual link by replacing the replaced symbols
    :param name: The name of the file to be changed to a link
    */
    public static String changeFilenameToLink(String name) {
        for (String key: Constants.symbolsMap.keySet()){
            name = name.replace(Constants.symbolsMap.get(key), key);
        }
        return name;
    }

    /*
        returns the list of urls crawled that is present in the webData directory
    */
    public static File[] get_url_list(String dir){
        File aDir= new File(dir);
        if(aDir.exists()){
            return aDir.listFiles();
        }
        return null;
    }


    /*
    It generates a new matrix after scalar multiplication with the generated adj matrix with a factor of (1-Alpha)
    :param probabilityTransitionMatrix: 2-D matrix which already has all the probabilitities mapped for all links
    */
    public static List<List<Double>> generate_scaled_adjacentMatrix(List<List<Double>> probabilityTransitionMatrix) {
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
    public static List<List<Double>> generate_probabilityTransitionMatrix(List<String> urlIndexMap,HashMap<String, String[]> urlOutgoings) {
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
    public static List<List<Double>> generate_finalMatrix(List<List<Double>> scaledAdjacentMatrix) {
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
    public static List<List<Double>> mult_matrix(List<List<Double>> matrixX, List<List<Double>> matrixY) {
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
    public static Double euclidean_dist(List<List<Double>> a, List<List<Double>> b) {
        Double eDistance = 0.0d;
        if ((a).size() > 1 || (b).size() > 1){
            return -1.0d;
        }
        for (int counter=0; counter<a.size(); counter++){
            eDistance += (double)Math.pow((a.get(0).get(counter) - b.get(0).get(counter)), 2);
        }
        return (double)Math.pow(eDistance, 0.5);
    }


    /*
    It takes a string of an html page, parses it and returns the title, words , and links present in the page seperately.
        :param linkString: The string that contains the Html page elements
        :param activeUrl: The URL of the page that the user is currently on from which the string of links is obtained. This is necessary as if there is a relative url then the activeUrl can be used to form the whole url.
        :param linkQueue: The list of unique urls which are sequentially to be crawled from.
    */
    public static String getLinks(String linkString, String aurl, List<String> linkQueue){
        String[] data = (linkString.strip().split(" "));
        List<String> activeUrl = new ArrayList<>();
        Collections.addAll(activeUrl, aurl.split("/"));
        StringBuilder urls = new StringBuilder();
        String finalUrl;
        for (String url : data){
            //removes the last index of the absolute url (i.e. this page 's address)
            activeUrl.remove(activeUrl.size()-1);
            if (url.contains("http://")){  //check if it's an absolute url or a relative url.
                finalUrl = url;
            }
            else{
                //only takes the link from the anchor tag
                url = url.replace("./", "");
                activeUrl.add(url);  //and adds the current link 's url
                finalUrl= String.join("/", activeUrl);

            }
            urls.append(finalUrl).append(' ');
            if (!linkQueue.contains(finalUrl)){  //check if the url is already in the queue
                linkQueue.add(finalUrl);
            }
        }
        return urls.toString();
    }




}
