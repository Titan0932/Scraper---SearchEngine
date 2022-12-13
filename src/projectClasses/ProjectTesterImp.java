package projectClasses;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
/*
* A class that implements the ProjectTester interface and glues together the entire project such that testing can be carried out on it.
* */
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
