package projectClasses;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

public class ProjectTesterImp implements ProjectTester {


  /*
        This method must delete any existing data that has been stored from any previous crawl.
        This method should also perform any other initialization needed by your system.
        This method will always be called before executing the crawl for a new dataset
         */
  public void initialize() {
    for (String dirName : Constants.allDirs) {
      if (!CrawlerHelpers.check_dir_exists(dirName)) {
        try {
          Files.createDirectories(Paths.get(dirName));
        } catch (Exception e) {
          e.printStackTrace();
        }
      } else {
        Arrays
          .stream(Objects.requireNonNull(new File(dirName).listFiles()))
          .forEach(File::delete);
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
  public void crawl(String seedURL) {
    Crawl crawler= new Crawl(this);
    this.initialize();
    crawler.crawl(seedURL);
  }
//
//  /*
//   * generates the tf-idf and tf values and stores them in respective files
//  */
//  private void generate_tf_tfIdf(
//    HashMap<String, HashMap<String, Integer>> pagesWordsCount,
//    List<String> uniqueWords
//  ) {
//    PrintWriter tfFile;
//    PrintWriter tfidfFile;
//    for (String url : pagesWordsCount.keySet()) {
//      double newTf = 0;
//
//      try {
//        tfFile =
//          new PrintWriter(
//            new FileWriter(
//              "tf" +
//              File.separator +
//              CrawlerHelpers.changeLinkToFileName(url) +
//              ".txt"
//            )
//          );
//        tfidfFile =
//          new PrintWriter(
//            new FileWriter(
//              "tfidf" +
//              File.separator +
//              CrawlerHelpers.changeLinkToFileName(url) +
//              ".txt"
//            )
//          );
//        for (String uniqueItem : uniqueWords) {
//          double idf = this.getIDF(uniqueItem);
//          if (!pagesWordsCount.get(url).containsKey(uniqueItem)) {
//            newTf = 0;
//          } else {
//            newTf =
//              (double) pagesWordsCount.get(url).get(uniqueItem) /
//              pagesWordsCount.get(url).get("totalWordNum");
//          }
//          double newTfIdf = (Math.log(1 + newTf) * idf) / Math.log(2);
//          tfFile.println(uniqueItem);
//          tfFile.println(newTf);
//          tfidfFile.println(uniqueItem);
//          tfidfFile.println(newTfIdf);
//        }
//        tfidfFile.close();
//        tfFile.close();
//      } catch (Exception e) {
//        e.printStackTrace();
//      }
//    }
//  }
//
//
//  /*
//  returns the idf value of a word
//  :param uniqueword: a string for the word for which the idf is generated
//  :param saveFile: a boolean flag which decides whether or not to save the idf values in a file or not.(It is used for the initialization of the idf values storing file)
//  */
//  private double generateIdf(String uniqueWord, Boolean saveFile){
//    int wordCount = 0;
//    File[] allFiles = CrawlerHelpers.get_url_list("webData");
//    BufferedReader dataFile;
//    String words;
//    float totalUrlNum = (allFiles).length;
//    for(File file : allFiles){
//      try {
//        dataFile = new BufferedReader(new FileReader("webData"+File.separator + file.getName()));
//        words = dataFile.readLine();  //url
//        words= dataFile.readLine();     //title
//        words= dataFile.readLine();     //finally words
//        if (words.contains(uniqueWord)){
//          wordCount += 1;
//        }
//        dataFile.close();
//      }catch (Exception e){
//        e.printStackTrace();;
//        return -1;
//      }
//    }
//    double idf = Math.log(totalUrlNum / (1 + wordCount))/ Math.log(2);
//    // System.out.println("idf: "+ idf);
//    if (saveFile){
//      PrintWriter uniqueIdfsFile;
//      String filepath= "idf"+File.separator+uniqueWord + ".txt";
//      try {
//        uniqueIdfsFile = new PrintWriter(new FileWriter(filepath));
//        uniqueIdfsFile.println(idf);
//        uniqueIdfsFile.close();
//      }catch (Exception e){
//        e.printStackTrace();
//      }
//    }
//    return 0;
//  }
//
//
//
//  /*
//   * generates pageRank for all url and stores them in specific files
//   * @param allUrls :  array of all urls scraped
//   * @param urlIndexMap: a list of all urls used as a mapping for its index values for final value retieval: definition in crawler.java
//   * @param urlOutgoings: a hashmap of urls and their outgoing links
//   */
//  private void generate_pageRank(String[] allUrls,List<String> urlIndexMap,HashMap<String, String[]> urlOutgoings){
//    List<List<Double>> probabilityTransitionMatrix = CrawlerHelpers.generate_probabilityTransitionMatrix(urlIndexMap, urlOutgoings);
//    List<List<Double>> scaledAdjacentMatrix = CrawlerHelpers.generate_scaled_adjacentMatrix(probabilityTransitionMatrix);
//    List<List<Double>>  finalMatrix = CrawlerHelpers.generate_finalMatrix(scaledAdjacentMatrix);
//    List<List<Double>> piVector = new ArrayList<>();
//    piVector.add(new ArrayList<>());
//    List<List<Double>> newVector = new ArrayList<>();
//    piVector.get(0).add((double) 1);
//    for(int counter=1; counter< urlIndexMap.size(); counter++){
//      piVector.get(0).add((double) 0);
//    }
//    Double euclidianDist = 1.0d;
//    while (euclidianDist > Constants.CONST_DROPTHRESHOLD){
//      newVector = CrawlerHelpers.mult_matrix(piVector, finalMatrix);
//      euclidianDist = CrawlerHelpers.euclidean_dist(piVector, newVector);
//      piVector=newVector;
//    }
//    // System.out.println(piVector);
//    for(String doc: allUrls){
//      String link = CrawlerHelpers.changeFilenameToLink(doc).replace(".txt", "");
//
//      try{
//        PrintWriter pageRankFile= new PrintWriter(new FileWriter("pageRank"+File.separator+doc));
//        pageRankFile.println(piVector.get(0).get(urlIndexMap.indexOf(link)));
//        pageRankFile.close();
//      }
//      catch(Exception e) {
//        return;
//      }
//    }
//  }
//
//
//  /*
//  It parses the html file and extracts the links and words from it.
//
//  :param string: the html string
//  :param url: the url of the page from where we're parsing
//  :param pagesWordsCount: a dictionary that keeps track of the number of words in each page
//  :param uniqueWords: a dictionary that contains all the unique words in the pages that have been parsed so far
//  */
//  private List<String> parseHtml(String string,
//                                String url,
//                                HashMap<String, HashMap<String, Integer>> pagesWordsCount,
//                                List<String> uniqueWords) {
//    boolean activeLoop = true;
//
//    String words="";
//    String links="";
//    boolean titleFound = false;
//    int endIndex;
//    String title= null;
//    // System.out.println("STRINGG: "+ string);
//    while (activeLoop) {
//      //for <title >
//      if (!titleFound) {
//        int titleStartIndex = string.indexOf("<title>");
//        endIndex = string.indexOf("</title>");
//        title = string.substring(titleStartIndex + 7,endIndex);
//        string = string.replace(">"+title+"<","");
//        titleFound = true;
//      }
//      //for <p >
//      int pStartIndex = string.indexOf("<p>");
//      if (pStartIndex != -1){
//        endIndex = string.indexOf("</p>");
//        words+=(' ')+(string.substring(pStartIndex + 3, endIndex).replace("\n", " "));
//        string = string.replace(string.substring(pStartIndex,endIndex+3),"");
//      }
//      //for <a > tag
//      int aStartIndex = string.indexOf("<a ");
//      if (aStartIndex != -1){
//        endIndex = aStartIndex + string.substring(aStartIndex).indexOf(">");
//        //reference position of href attribute from the postion of the <a tag, taken from 0.
//        int hrefIndex = string.substring(aStartIndex,endIndex).indexOf("href=");
////                links.append(' ').append(string.substring(aStartIndex + hrefIndex + 6, endIndex - 1));
//        links+= (' ')+(string.substring(aStartIndex + hrefIndex + 6, endIndex - 1));
//        int closingTag = string.indexOf("</a>");
//        string = string.replace(string.substring(aStartIndex,closingTag + 3),"");
//      }
//      // for <p....>tag with attributes
//      int pAttributeStartIndex = string.indexOf("<p ");
//      if (pAttributeStartIndex != -1) {
//        int end_IndexOf_StartingTag = string.substring(pAttributeStartIndex).indexOf(">");
//        int closingTag = string.indexOf("</p>");
////                words.append(' ').append(string.substring(end_IndexOf_StartingTag + 1, closingTag));
//        words+=(' ')+(string.substring(end_IndexOf_StartingTag + 1, closingTag));
//      }
//      if (titleFound && pStartIndex == -1 && aStartIndex ==-1 && pAttributeStartIndex ==-1){
//        activeLoop = false;
//      }
//    }
//
//    String fulllinks = CrawlerHelpers.getLinks(links, url, linkQueue);
//    for (String word : words.strip().split("\\s+")){
//      if(!Objects.equals(word, "")){
//        pagesWordsCount.get(url).put("totalWordNum",pagesWordsCount.get(url).get("totalWordNum") +1 );
//      }
//      if (!uniqueWords.contains(word)){
//        uniqueWords.add(word);
//      }
//      if (pagesWordsCount.get(url).containsKey(word)){
//        pagesWordsCount.get(url).put(word,pagesWordsCount.get(url).get(word)+1 );
//      }
//      else{
//        pagesWordsCount.get(url).put(word, 1 );
//      }
//    }
//    return Arrays.asList(title, words, fulllinks);
//  }



  // ===================================SEARCH DATA METHODS::===================================================================

  /*
    Returns a list of the outgoing links of the page with the given URL.
    That is, the URLs that the page with the given URL links to.
    If no page with the given URL exists, returns null.
     */
  public List<String> getOutgoingLinks(String url) {
    BufferedReader file;
    String filename = CrawlerHelpers.changeLinkToFileName(url);
    try {
      Path filePath = Paths.get("webData" + File.separator + filename + ".txt");
      file =
        new BufferedReader(
          new FileReader("webData" + File.separator + filename + ".txt")
        );
      String urlData = null;
      long numLines = Files.lines(filePath).count();
      for (int lineNum = 0; lineNum < numLines; lineNum++) {
        urlData = file.readLine();
      }
      urlData = urlData.strip();
      // System.out.println(urlData);
      if (
        (urlData).length() == 0 ||
        (!urlData.contains("http://") && !urlData.contains("https://"))
      ) {
        file.close();
        return null;
      }
      file.close();
      return (List.of(urlData.split("\\s+")));
    } catch (Exception e) {
      e.printStackTrace();
      return null;
    }
  }

  /*
    Returns a list of the incoming links for the page with the given URL.
    That is, the URLs that link to the page with the given URL
    If no page with the given URL exists, returns null.
     */
  public List<String> getIncomingLinks(String url) {
    File directoryPath = new File("webData");
    File[] allUrls = directoryPath.listFiles();
    List<String> urlList = new ArrayList<>();
    String fileData = null;
    String anIncomingUrl = null;
    for (File aUrl : allUrls) {
      BufferedReader file;
      String filename = "webData/" + aUrl.getName();
      try {
        file = new BufferedReader(new FileReader(filename));
        Path filePath = Paths.get(filename);
        anIncomingUrl =
          CrawlerHelpers
            .changeFilenameToLink(aUrl.getName())
            .replace(".txt", "");
        long numLines = Files.lines(filePath).count();
        for (int lineNum = 0; lineNum < numLines; lineNum++) {
          fileData = file.readLine();
        }
        fileData.strip();
        if (fileData.length() > 0) {
          if (fileData.contains(url)) {
            urlList.add(anIncomingUrl);
          }
        }
        file.close();
      } catch (Exception e) {
        e.printStackTrace();
        return null;
      }
    }
    if ((urlList).size() == 0) {
      return null;
    }
    return urlList;
  }

  /*
    Returns the PageRank value for the page with the given URL.
    If no page with the given URL exists, returns -1.
     */
  public double getPageRank(String url) {
    String filename =
      "pageRank" +
      File.separator +
      CrawlerHelpers.changeLinkToFileName(url) +
      ".txt";
    BufferedReader file;
    Double pageRank;
    try {
      file = new BufferedReader(new FileReader(filename));
      pageRank = Double.parseDouble(file.readLine());
      file.close();
    } catch (IOException e) {
      e.printStackTrace();
      return -1;
    }
    return (pageRank);
  }

  /*
    Returns the IDF value for the given word.
    A word that did not show up during the crawl should have an IDF of 0.
     */
  public double getIDF(String word) {
    BufferedReader file;
    File directory = new File("idf/" + word + ".txt");
    if (directory.exists()) {
      try {
        file = new BufferedReader(new FileReader("idf/" + word + ".txt"));
        double idfVal = Double.parseDouble(file.readLine());
        file.close();
        return idfVal;
      } catch (Exception e) {
        e.printStackTrace();
      }
    }
    return 0;
  }

  /*
    Returns the term frequency of the given word within the page with the given URL.
    If the word did not appear on the given page, the TF should be 0.
     */
  public double getTF(String url, String word) {
    String filename =
      "tf" + File.separator + CrawlerHelpers.changeLinkToFileName(url) + ".txt";
    BufferedReader file;
    String aWord = null;
    try {
      file = new BufferedReader(new FileReader(filename));
      aWord = file.readLine();
      while (aWord != null) {
        // System.out.println("CURR WORD: "+ aWord);
        // System.out.println("TO FIND WORD: "+ word);
        if (aWord.equals(word)) {
          double tfVal = Double.parseDouble(file.readLine());
          file.close();
          return tfVal;
        }
        aWord = file.readLine();
        aWord = file.readLine();
      }
      file.close();
    } catch (Exception e) {
      e.printStackTrace();
    }
    return 0;
  }

  /*
    Returns the TF-IDF value of the given word within the page with the given URL.
     */
  public double getTFIDF(String url, String word) {
    String filename =
      "tfidf/" + CrawlerHelpers.changeLinkToFileName(url) + ".txt";
    BufferedReader file;
    String aWord = null;
    try {
      file = new BufferedReader(new FileReader(filename));
      aWord = file.readLine();
      while (aWord != null) {
        if (aWord.equals(word)) {
          double tfidfVal = Double.parseDouble(file.readLine());
          file.close();
          return tfidfVal;
        }
        aWord = file.readLine();
        aWord = file.readLine();
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
    return 0;
  }

  //=========================================== SEARCH =======================================================================

//  // returns hashmap of the tfidf values of the unique words entered through the search query
//  private HashMap<String, Double> get_query_tfIdf(String phrase) {
//    HashMap<String, Double> queryVector = new HashMap<>(); //map of words and their tfIdf values in the query
//    List<String> uniqueWords = new ArrayList<>(); //list of unique words in the query
//    String[] phraseList = phrase.strip().split("\\s+"); //list of words in the query
//    float totalWords = (phraseList).length;
//    HashMap<String, Integer> wordCount = new HashMap<>(); //dictionary which counts the frequency of words in the query entered
//    for (String word : phraseList) {
//      if (!uniqueWords.contains(word)) {
//        uniqueWords.add(word);
//        wordCount.put(word, 0); //initializing values
//      }
//    }
//    for (String word : phraseList) {
//      wordCount.put(word, wordCount.get(word) + 1);
//    }
//    for (String uniqueWord : uniqueWords) {
//      double tf = wordCount.get(uniqueWord) / totalWords;
//      double idf = getIDF(uniqueWord);
//      queryVector.put(uniqueWord, (Math.log(1 + tf) * idf) / Math.log(2));
//    }
//    return queryVector;
//  }
//
//  /**
//   * It implements the Comparator interface, which is a Java interface that defines a method called
//   * compare. The compare method takes two pagerank objects and returns an integer.
//   * The integer is negative if the first object's score is is less than the second object's score,
//   * and positive it's greater
//   * if the scores are equal then it returns the integer of the similar logic but uses the title of the objects for comparision
//   */
//  class SearchResultComparator implements Comparator<PagerankData> {
//
//    /**
//     * It compares two PagerankData objects and returns the result of the comparison.
//     *
//     * @param p1 The first object to be compared.
//     * @param p2 The second object to compare.
//     * @return 1 or -1 based on which is greater.
//     */
//    public int compare(PagerankData p1, PagerankData p2) {
//      if (p1.getScore() == p2.getScore()) {
//        return ((p1.getTitle()).compareTo(p2.getTitle()));
//      } else if (p1.getScore() < p2.getScore()) {
//        return 1;
//      } else return -1;
//    }
//  }

  /*
    Performs a search using the given query.
    If boost is true, the search score for a page should be boosted by the page's PageRank value.
    If boost is false, the search score for a page will be only based on cosine similarity.
    This method must return a list of objects that implement the projectClasses.SearchResult interface.
    The list should return the top X search results for the given query/boost values.
    Results should be sorted from highest score to lowest.
    If two search results have the same score when rounded to 3 decimal places,
    the scores for those two results should be considered identical and their
    lexicographical ordering (this is what Java's String compareTo() method uses)
    should be used to determine which goes before the other.
    A copy of this interface is included on the project's BrightSpace page.
     */
  public List<SearchResult> search(String query, boolean boost, int topIndex) {
    SearchEngine searcher= new SearchEngine(this);
    return searcher.search(query, boost, topIndex);
  }

  public static void main(String[] args) {
    //   Crawler test= new ProjectTesterImp();
    //  test.initialize();
    //   test.crawl("https://people.scs.carleton.ca/~davidmckenney/fruits/N-0.html");
    //   List<SearchResult> res = (test.search("apple peach pear tomato pear", true, 10));
    //   for(SearchResult r:res){
    //     System.out.println(r.getTitle()+" "+ r.getScore());
    //   }
  }
}
