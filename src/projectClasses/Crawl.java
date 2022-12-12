package projectClasses;

import java.io.*;
import java.util.*;

public class Crawl{

    private List<String> linkQueue = new ArrayList<>();
    private ProjectTesterImp tester;



    public Crawl(ProjectTesterImp tester){
        this.tester= tester;
    }


    /*
          This method performs a crawl starting at the given seed URL.
          It should visit each page it can find once.
          It should not stop until it has visited all reachable pages.
          All data required for later search queries should be saved in files once this completes.
           */
    public void crawl(String seedURL) {
        List<String> uniqueWords = new ArrayList<>(); // list of all the unique words that are present in all of the urls crawled
        int linksAccessed = 0; // the counter for the number of links accessed
        this.linkQueue.add(seedURL);
        HashMap<String, HashMap<String, Integer>> pagesWordsCount = new HashMap<>();
    /*      pagesWordsCount= {
                    pageURL:
                    {
                        'totalWordNum':totalWordsInURL, 'uniqueWord1':count1, uniqueWord2:count2,.........,}
                }   // this dictionary stores the tfs for every page.This dict is used such that we can compute the values as
                the crawl loop is active simultaneously such that another loop does not have to be used to compute these values.
*/
        List<String> urlIndexMap = new ArrayList<>(); // A list of all urls used as a map to map a url to an index value to make it easier to access the pagerank score from the result matrix in o(N) time later .
        HashMap<String, String[]> urlOutgoings = new HashMap<>();  // A hashMap for all the urls and their respective outgoing links.
        String data = null;
        for (int index = 0; index < linkQueue.size(); index++) {
            String curr = linkQueue.get(linksAccessed);
            try {
                data = WebRequester.readURL(curr);     // retrieving data from the webpage
            } catch (IOException e) {
                e.printStackTrace();
            }
            //            System.out.println(curr);
            HashMap<String, Integer> temp = new HashMap<>();
            temp.put("totalWordNum", 0);
            pagesWordsCount.put(curr, temp);
            Data parsedData = new Data(   //storing the parsed data as an immutable record object
                    parseHtml(
                            data,
                            curr,
                            pagesWordsCount,
                            uniqueWords
                    )
            );
            //            System.out.println(parsedData);
            CrawlerHelpers.addDataToFile(curr, parsedData); //adding all the parsed data to the respective files
            linksAccessed++;

            urlIndexMap.add(curr);
            urlOutgoings.put(curr, parsedData.getLinks().strip().split("\\s+"));
        }
        // generate and save the idf value for all unique words in a file
        for (String uniqueItem : uniqueWords) {
            generateIdf(uniqueItem, true);
        }
        generate_tf_tfIdf(pagesWordsCount, uniqueWords);
        String[] allUrls = new File("webdata").list();

        generate_pageRank(allUrls, urlIndexMap, urlOutgoings);
    }

    /*
     * generates the tf-idf and tf values and stores them in respective files
     */
    private void generate_tf_tfIdf(
            HashMap<String, HashMap<String, Integer>> pagesWordsCount,
            List<String> uniqueWords
    ) {
        PrintWriter tfFile;
        PrintWriter tfidfFile;
        for (String url : pagesWordsCount.keySet()) {
            double newTf = 0;

            try {
                tfFile =
                        new PrintWriter(
                                new FileWriter(
                                        "tf" +
                                                File.separator +
                                                CrawlerHelpers.changeLinkToFileName(url) +
                                                ".txt"
                                )
                        );
                tfidfFile =
                        new PrintWriter(
                                new FileWriter(
                                        "tfidf" +
                                                File.separator +
                                                CrawlerHelpers.changeLinkToFileName(url) +
                                                ".txt"
                                )
                        );
                for (String uniqueItem : uniqueWords) {
                    double idf = this.tester.getIDF(uniqueItem);
                    if (!pagesWordsCount.get(url).containsKey(uniqueItem)) {
                        newTf = 0;
                    } else {
                        newTf =
                                (double) pagesWordsCount.get(url).get(uniqueItem) /
                                        pagesWordsCount.get(url).get("totalWordNum");
                    }
                    double newTfIdf = (Math.log(1 + newTf) * idf) / Math.log(2);
                    tfFile.println(uniqueItem);
                    tfFile.println(newTf);
                    tfidfFile.println(uniqueItem);
                    tfidfFile.println(newTfIdf);
                }
                tfidfFile.close();
                tfFile.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }


    /*
    returns the idf value of a word
    :param uniqueword: a string for the word for which the idf is generated
    :param saveFile: a boolean flag which decides whether or not to save the idf values in a file or not.(It is used for the initialization of the idf values storing file)
    */
    private double generateIdf(String uniqueWord, Boolean saveFile){
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



    /*
     * generates pageRank for all url and stores them in specific files
     * @param allUrls :  array of all urls scraped
     * @param urlIndexMap: a list of all urls used as a mapping for its index values for final value retieval: definition in crawler.java
     * @param urlOutgoings: a hashmap of urls and their outgoing links
     */
    private void generate_pageRank(String[] allUrls,List<String> urlIndexMap,HashMap<String, String[]> urlOutgoings){
        List<List<Double>> probabilityTransitionMatrix = CrawlerHelpers.generate_probabilityTransitionMatrix(urlIndexMap, urlOutgoings);
        List<List<Double>> scaledAdjacentMatrix = CrawlerHelpers.generate_scaled_adjacentMatrix(probabilityTransitionMatrix);
        List<List<Double>>  finalMatrix = CrawlerHelpers.generate_finalMatrix(scaledAdjacentMatrix);
        List<List<Double>> piVector = new ArrayList<>();
        piVector.add(new ArrayList<>());
        List<List<Double>> newVector = new ArrayList<>();
        piVector.get(0).add((double) 1);
        for(int counter=1; counter< urlIndexMap.size(); counter++){
            piVector.get(0).add((double) 0);
        }
        Double euclidianDist = 1.0d;
        while (euclidianDist > Constants.CONST_DROPTHRESHOLD){
            newVector = CrawlerHelpers.mult_matrix(piVector, finalMatrix);
            euclidianDist = CrawlerHelpers.euclidean_dist(piVector, newVector);
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
    It parses the html file and extracts the links and words from it.

    :param string: the html string
    :param url: the url of the page from where we're parsing
    :param pagesWordsCount: a dictionary that keeps track of the number of words in each page
    :param uniqueWords: a dictionary that contains all the unique words in the pages that have been parsed so far
    */
    private List<String> parseHtml(String string,
                                   String url,
                                   HashMap<String, HashMap<String, Integer>> pagesWordsCount,
                                   List<String> uniqueWords) {
        boolean activeLoop = true;

        String words="";
        String links="";
        boolean titleFound = false;
        int endIndex;
        String title= null;
        // System.out.println("STRINGG: "+ string);
        while (activeLoop) {
            //for <title >
            if (!titleFound) {
                int titleStartIndex = string.indexOf("<title>");
                endIndex = string.indexOf("</title>");
                title = string.substring(titleStartIndex + 7,endIndex);
                string = string.replace(">"+title+"<","");
                titleFound = true;
            }
            //for <p >
            int pStartIndex = string.indexOf("<p>");
            if (pStartIndex != -1){
                endIndex = string.indexOf("</p>");
                words+=(' ')+(string.substring(pStartIndex + 3, endIndex).replace("\n", " "));
                string = string.replace(string.substring(pStartIndex,endIndex+3),"");
            }
            //for <a > tag
            int aStartIndex = string.indexOf("<a ");
            if (aStartIndex != -1){
                endIndex = aStartIndex + string.substring(aStartIndex).indexOf(">");
                //reference position of href attribute from the postion of the <a tag, taken from 0.
                int hrefIndex = string.substring(aStartIndex,endIndex).indexOf("href=");
//                links.append(' ').append(string.substring(aStartIndex + hrefIndex + 6, endIndex - 1));
                links+= (' ')+(string.substring(aStartIndex + hrefIndex + 6, endIndex - 1));
                int closingTag = string.indexOf("</a>");
                string = string.replace(string.substring(aStartIndex,closingTag + 3),"");
            }
            // for <p....>tag with attributes
            int pAttributeStartIndex = string.indexOf("<p ");
            if (pAttributeStartIndex != -1) {
                int end_IndexOf_StartingTag = string.substring(pAttributeStartIndex).indexOf(">");
                int closingTag = string.indexOf("</p>");
//                words.append(' ').append(string.substring(end_IndexOf_StartingTag + 1, closingTag));
                words+=(' ')+(string.substring(end_IndexOf_StartingTag + 1, closingTag));
            }
            if (titleFound && pStartIndex == -1 && aStartIndex ==-1 && pAttributeStartIndex ==-1){
                activeLoop = false;
            }
        }

        String fulllinks = CrawlerHelpers.getLinks(links, url, linkQueue);
        for (String word : words.strip().split("\\s+")){
            if(!Objects.equals(word, "")){
                pagesWordsCount.get(url).put("totalWordNum",pagesWordsCount.get(url).get("totalWordNum") +1 );
            }
            if (!uniqueWords.contains(word)){
                uniqueWords.add(word);
            }
            if (pagesWordsCount.get(url).containsKey(word)){
                pagesWordsCount.get(url).put(word,pagesWordsCount.get(url).get(word)+1 );
            }
            else{
                pagesWordsCount.get(url).put(word, 1 );
            }
        }
        return Arrays.asList(title, words, fulllinks);
    }



}
