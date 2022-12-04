package projectClasses;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

public class Crawler implements ProjectTester, CrawlerActions {

  List<String> linkQueue = new ArrayList<>();

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
        //new File(dirName).mkdirs();
      } else {
        Arrays
          .stream(Objects.requireNonNull(new File(dirName).listFiles()))
          .forEach(File::delete);
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
  public void crawl(String seedURL) {
    List<String> uniqueWords = new ArrayList<>(); // list of all the unique words that are present in all of the urls crawled
    this.initialize();
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
    int indexCounter = 0;
    List<String> urlIndexMap = new ArrayList<>();
    HashMap<String, String[]> urlOutgoings = new HashMap<>();
    String data = null;
    for (int index = 0; index < linkQueue.size(); index++) {
      String curr = linkQueue.get(linksAccessed);
      try {
        data = WebRequester.readURL(curr);
      } catch (IOException e) {
        e.printStackTrace();
      }
      //            System.out.println(curr);
      HashMap<String, Integer> temp = new HashMap<>();
      temp.put("totalWordNum", 0);
      pagesWordsCount.put(curr, temp);
      Data parsedData = new Data(
        CrawlerHelpers.parseHtml(
          data,
          linkQueue,
          curr,
          pagesWordsCount,
          uniqueWords
        )
      );
      //            System.out.println(parsedData);
      CrawlerHelpers.addDataToFile(curr, parsedData);
      linksAccessed++;
      //convert to array sized linkque
      urlIndexMap.add(curr);
      indexCounter += 1;
      urlOutgoings.put(curr, parsedData.getLinks().strip().split("\\s+"));
    }
    // generate and save the idf value for all unique words in a file
    for (String uniqueItem : uniqueWords) {
      CrawlerActions.generateIdf(uniqueItem, true);
    }
    generate_tf_tfIdf(pagesWordsCount, uniqueWords);
    String[] allUrls = new File("webdata").list();
    System.out.println("TFS all done");
    //    for (String doc : allUrls) {
    //     CrawlerActions.generate_pageRank(doc, allUrls, urlIndexMap, urlOutgoings);
    //    }
    CrawlerActions.generate_pageRank(allUrls, urlIndexMap, urlOutgoings);
  }

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
          double idf = this.getIDF(uniqueItem);
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

  private HashMap<String, Double> get_query_tfIdf(String phrase) {
    HashMap<String, Double> queryVector = new HashMap<>(); //map of words and their tfIdf values in the query
    List<String> uniqueWords = new ArrayList<>(); //list of unique words in the query
    String[] phraseList = phrase.strip().split("\\s+"); //list of words in the query
    float totalWords = (phraseList).length;
    HashMap<String, Integer> wordCount = new HashMap<>(); //dictionary which counts the frequency of words in the query entered
    for (String word : phraseList) {
      if (!uniqueWords.contains(word)) {
        uniqueWords.add(word);
        wordCount.put(word, 0); //initializing values
      }
    }
    for (String word : phraseList) {
      wordCount.put(word, wordCount.get(word) + 1);
    }
    for (String uniqueWord : uniqueWords) {
      double tf = wordCount.get(uniqueWord) / totalWords;
      double idf = getIDF(uniqueWord);
      queryVector.put(uniqueWord, (Math.log(1 + tf) * idf) / Math.log(2));
    }
    return queryVector;
  }

 /**
  * It implements the Comparator interface, which is a Java interface that defines a method called
  * compare. The compare method takes two objects of the same type and returns an integer. The integer
  * is negative if the first object is less than the second object, zero if the two objects are equal,
  * and positive if the first object is greater than the second object
  */
  class SearchResultComparator implements Comparator<PagerankData> {

    /**
     * It compares two PagerankData objects and returns the result of the comparison.
     * 
     * @param p1 The first object to be compared.
     * @param p2 The second object to compare.
     * @return The difference between the two scores.
     */
    public int compare(PagerankData p1, PagerankData p2) {
      if (p1.getScore() == p2.getScore()) return 0; else if (
        p1.getScore() < p2.getScore()
      ) return 1; else return -1;
    }
  }

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
    List<PagerankData> dataList = new ArrayList<>();
    HashMap<String, Double> queryVectorDict = get_query_tfIdf(query);
    File[] allFiles = CrawlerHelpers.get_url_list("webData");
    for (File aFile : allFiles) {
      String link = aFile.getName();
      String url = CrawlerHelpers
        .changeFilenameToLink(link)
        .replace(".txt", "");
      double numerator = 0d;
      double leftDenominator = 0d;
      double rightDenominator = 0d;
      for (String wordInQuery : queryVectorDict.keySet()) {
        double doc_tfIdf = (double) this.getTFIDF(url, wordInQuery);
        double query_tfIdf = queryVectorDict.get(wordInQuery);
        numerator += query_tfIdf * doc_tfIdf;
        leftDenominator += Math.pow(query_tfIdf, 2);
        rightDenominator += Math.pow(doc_tfIdf, 2);
      }
      double denominator =
        (
          (double) Math.sqrt(leftDenominator) *
          (double) Math.sqrt(rightDenominator)
        );
      double cosineSim;
      if (denominator == 0d) {
        cosineSim = 0d;
      } else {
        cosineSim = (double) (numerator) / denominator;
      }
      dataList.add(new PagerankData(url, cosineSim));
    }
    if (boost) {
      for (PagerankData data : dataList) {
        data.setScore(this.getPageRank(data.getUrl()));
      }
    }
    Collections.sort(dataList, new SearchResultComparator());
    List<SearchResult> result = new ArrayList<>();

    int numOfResults;
    if (topIndex <= dataList.size()) {
      numOfResults = topIndex;
    } else {
      numOfResults = dataList.size();
    }

    int resultCounter = 1;
    for (PagerankData data : dataList) {
      // System.out.println("URL: "+ data.getUrl()+ " ==> "+ data.getScore());
      if (resultCounter <= numOfResults) {
        result.add((SearchResult) data);
      } else {
        break;
      }
      resultCounter++;
    }
    return result;
    // return null;
  }

  public static void main(String[] args) {
    //         Crawler test= new Crawler();
    // // //        test.initialize();
    // //         test.crawl("https://people.scs.carleton.ca/~davidmckenney/tinyfruits/N-0.html");
    //         List<SearchResult> res = (test.search("peach papaya", false, 10));
    //         for(SearchResult r : res){
    //             System.out.println(r.getTitle()+ " "+ r.getScore());
    //         }

  }
}
