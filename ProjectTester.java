import java.io.*;
import java.lang.reflect.Array;
import java.nio.file.*;
import java.util.*;

public interface ProjectTester extends CrawlerHelpers, CrawlerActions {
    List<String> linkQueue = new ArrayList<>();


    /*
        This method must delete any existing data that has been stored from any previous crawl.
        This method should also perform any other initialization needed by your system.
        This method will always be called before executing the crawl for a new dataset
         */
    default void initialize(){
        for (String dirName: Constants.allDirs) {
            if (!CrawlerHelpers.check_dir_exists(dirName)){
                try{
                    Files.createDirectories(Paths.get(dirName));
                }catch(Exception e) {
                    e.printStackTrace();
                }
                //new File(dirName).mkdirs();
            }
            else{
                Arrays.stream(Objects.requireNonNull(new File(dirName).listFiles())).forEach(File::delete);
                /*File directoryPath = new File(dirName);
                File[] dirFiles = directoryPath.listFiles();
                if(dirFiles !=null) {
                    for (File file : dirFiles) {
                        file.delete();
                    }
                }*/
            }
        }
    }

// ===================================CRAWL METHODS::===================================================================

    /*
    This method performs a crawl starting at the given seed URL.
    It should visit each page it can find once.
    It should not stop until it has visited all reachable pages.
    All data required for later search queries should be saved in files once this completes.
     */
    default void crawl(String seedURL) {
        List<String> uniqueWords = new ArrayList<>();  // list of all the unique words that are present in all of the urls crawled
        int linksAccessed = 0;   // the counter for the number of links accessed
        initialize();
        this.linkQueue.add(seedURL);
        HashMap<String, HashMap<String, Integer>> pagesWordsCount = new HashMap<>();
   /*      pagesWordsCount= {
                    pageURL:
                    {
                        'totalWordNum':totalWordsInURL, 'uniqueWord1':count1, uniqueWord2:count2,.........,}
                }   // this dictionary stores the tfs for every page.This dict is used such that we can compute the values as
                the crawl loop is active simultaneously such that another loop does not have to be used to compute these values.
*/
        int indexCounter = 0;
        HashMap<String, Integer> urlIndexMap = new HashMap<>();
        HashMap<String, String[]> urlOutgoings = new HashMap<>();
        String data = null;
        linksAccessed = linkQueue.size();
        while (!linkQueue.isEmpty()){
            String curr = linkQueue.remove(0);
            try {
                data = WebRequester.readURL(curr);
            } catch (IOException e) {
                e.printStackTrace();
            }
            HashMap<String, Integer> temp =new HashMap<>();
            temp.put("totalWordNum", 0);
            pagesWordsCount.put(curr, temp);
            Data parsedData = new Data(CrawlerHelpers.parseHtml(data, linkQueue, curr, pagesWordsCount, uniqueWords));
            CrawlerHelpers.addDataToFile(curr, parsedData);
            linksAccessed++;
            //convert to array sized linkque
            urlIndexMap.put(curr, indexCounter);
            indexCounter += 1;
            urlOutgoings.put(curr, parsedData.getLinks().strip().split(" "));

        }
//        for (String url : linkQueue) {
//            try {
//                data = WebRequester.readURL(url);
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
//            HashMap<String, Integer> temp =new HashMap<>();
//            temp.put("totalWordNum", 0);
//            pagesWordsCount.put(url, temp);
//            List<String> parsedData = parseHtml(data, linkQueue, url, pagesWordsCount, uniqueWords);
//            String title = parsedData.get(0);
//            String words = parsedData.get(1);
//            String links = parsedData.get(2);
//            addDataToFile(url, words, links, title);
//            linkQueue.remove(0);
//            linksAccessed++;
//            urlIndexMap.put(url, indexCounter);
//            indexCounter += 1;
//            urlOutgoings.put(url, links.strip().split(" "));
//        }
        // generate and save the idf value for all unique words in a file
        for (String uniqueItem : uniqueWords) {
            generateIdf(uniqueItem, true);
        }
        generate_tf_tfIdf(pagesWordsCount, uniqueWords);
        String[] allUrls = {"webData"};

        for (String doc : allUrls) {
            generate_pageRank(doc, allUrls, urlIndexMap, urlOutgoings);
        }
    }



// ===================================SEARCH DATA METHODS::===================================================================

    /*
    Returns a list of the outgoing links of the page with the given URL.
    That is, the URLs that the page with the given URL links to.
    If no page with the given URL exists, returns null.
     */
    default List<String> getOutgoingLinks(String url) throws IOException {
        BufferedReader file;
        String filename= CrawlerHelpers.changeLinkToFileName(url)+".txt";
        file = new BufferedReader(new FileReader(filename));
        String urlData= null;
        for(int lineNum=0; lineNum<= 2;lineNum++){
            urlData= file.readLine();
        }
        urlData= urlData.strip();
        if ((urlData).length() == 0 || (!urlData.contains("http://") && !urlData.contains("https://"))){
            return null;
        }
        return new ArrayList<>(List.of(urlData.split(" ")));
    }

    /*
    Returns a list of the incoming links for the page with the given URL.
    That is, the URLs that link to the page with the given URL
    If no page with the given URL exists, returns null.
     */
    default List<String> getIncomingLinks(String url) throws IOException {
        File directoryPath = new File("webData");
        File[] allUrls = directoryPath.listFiles();
        List <String> urlList = new ArrayList<>();
        String fileData = null;
        String anIncomingUrl=null;
        for (File aUrl : allUrls){
            BufferedReader file;
            String filename= "webData/"+ aUrl.getName();
            file = new BufferedReader(new FileReader(filename));
            anIncomingUrl= CrawlerHelpers.changeFilenameToLink(aUrl.getName()).replace(".txt","");
            for(int lineNum=0; lineNum<= 2;lineNum++){
                fileData= file.readLine();
            }
            fileData.strip();
            if(fileData.length()>0){
                if(fileData.contains(url)){
                    urlList.add(anIncomingUrl);
                }
            }
        }
        if ((urlList).size() == 0){
            return null;
        }
        return urlList;
    }

    /*
    Returns the PageRank value for the page with the given URL.
    If no page with the given URL exists, returns -1.
     */
    default double getPageRank(String url){
        String filename = CrawlerHelpers.changeLinkToFileName(url) + ".txt";
        BufferedReader file;
        float pageRank;
        try {
            file = new BufferedReader(new FileReader(filename));
            pageRank= Float.parseFloat(file.readLine());
        }
        catch(IOException e){
            e.printStackTrace();
            return -1;
        }
        return (pageRank);
    }
    /*
    Returns the IDF value for the given word.
    A word that did not show up during the crawl should have an IDF of 0.
     */
    default double getIDF(String word) throws IOException {
        BufferedReader file;
        String aWord;
        file = new BufferedReader(new FileReader("idf/words.txt"));
        aWord = (file.readLine());
        while(aWord!=null){
            if(aWord== word){
                return Double.parseDouble(file.readLine());
            }
            aWord=file.readLine();
        }
        return 0;
    }

    /*
    Returns the term frequency of the given word within the page with the given URL.
    If the word did not appear on the given page, the TF should be 0.
     */
    default double getTF(String url, String word) throws IOException {
        String filename = "tf/" + CrawlerHelpers.changeLinkToFileName(url) + ".txt";
        BufferedReader file;
        String aWord=null;
        file = new BufferedReader(new FileReader(filename));
        aWord=file.readLine();
        while(aWord!=null){
            if(aWord== word){
                return Double.parseDouble(file.readLine());
            }
            aWord=file.readLine();
        }
        return 0;
    }

    /*
    Returns the TF-IDF value of the given word within the page with the given URL.
     */
    default double getTFIDF(String url, String word) throws IOException {
        String filename = "tfidf/" + CrawlerHelpers.changeLinkToFileName(url) + ".txt";
        BufferedReader file;
        String aWord=null;
        file = new BufferedReader(new FileReader(filename));
        aWord=file.readLine();
        while(aWord!=null){
            if(aWord== word){
                return Double.parseDouble(file.readLine());
            }
            aWord=file.readLine();
        }
        return 0;
    }

    /*
    Performs a search using the given query.
    If boost is true, the search score for a page should be boosted by the page's PageRank value.
    If boost is false, the search score for a page will be only based on cosine similarity.
    This method must return a list of objects that implement the SearchResult interface.
    The list should return the top X search results for the given query/boost values.
    Results should be sorted from highest score to lowest.
    If two search results have the same score when rounded to 3 decimal places,
    the scores for those two results should be considered identical and their
    lexicographical ordering (this is what Java's String compareTo() method uses)
    should be used to determine which goes before the other.
    A copy of this interface is included on the project's BrightSpace page.
     */
    List<SearchResult> search(String query, boolean boost, int X);
}
