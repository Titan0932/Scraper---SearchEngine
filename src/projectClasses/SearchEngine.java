package projectClasses;

import java.io.File;
import java.util.*;

public class SearchEngine{
    private ProjectTesterImp tester;


    public SearchEngine(ProjectTesterImp tester){
        this.tester= tester;
    }

    // returns hashmap of the tfidf values of the unique words entered through the search query
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
            double idf = this.tester.getIDF(uniqueWord);
            queryVector.put(uniqueWord, (Math.log(1 + tf) * idf) / Math.log(2));
        }
        return queryVector;
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
        List<PagerankData> dataList = new ArrayList<>();  //list of all the pageRankData for all the urls
        HashMap<String, Double> queryVectorDict = get_query_tfIdf(query);
        File[] allFiles = CrawlerHelpers.get_url_list("webData");
        for (File aFile : allFiles) {
            String link = aFile.getName();
            String url = CrawlerHelpers
                    .changeFilenameToLink(link)
                    .replace(".txt", "");
            //calculating cosineSim
            double numerator = 0d;
            double leftDenominator = 0d;
            double rightDenominator = 0d;
            for (String wordInQuery : queryVectorDict.keySet()) {
                double doc_tfIdf = (double) this.tester.getTFIDF(url, wordInQuery);
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
        //calculation score based on boost input
        if (boost) {
            for (PagerankData data : dataList) {
                data.setScore(this.tester.getPageRank(data.getUrl()));
            }
        } else { //this is done to round the cosineSim data to 3d places. We aren't directly storing 3d in cosineSim as boosting values might get affected by the rounding off. So only rounding later once boosting=false
            for (PagerankData data : dataList) {
                data.setScore(1d);
            }
        }

        Collections.sort(dataList, new SearchResultComparator());  //sorting all our results.
        List<SearchResult> result = new ArrayList<>();

        int numOfResults; //how many results to display?
        if (topIndex <= dataList.size()) {
            numOfResults = topIndex;
        } else { // eg: 10 results cannot be displayed on array result of 8
            numOfResults = dataList.size();
        }

        int resultCounter = 1;
        for (SearchResult data : dataList) {
            // System.out.println("URL: "+ data.getUrl()+ " ==> "+ data.getScore());
            if (resultCounter <= numOfResults) {
                result.add(data);
            } else {
                break;
            }
            resultCounter++;
        }
        return result;
        // return null;
    }

}
