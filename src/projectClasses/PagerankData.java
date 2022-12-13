package projectClasses;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.text.DecimalFormat;

public class PagerankData implements SearchResult {
    /*
     * Implements the SearchResult interface
     * Stores the score calculated from search query and the title for a url
     */

    private String url;
    // private String ORDER;  //descending or ascending order
    private Double score;
    private String title;


    public PagerankData(String newUrl, Double newScore){
        this(newUrl, newScore, "desc");
    }

    public PagerankData(String newUrl, Double newScore, String order){
        this.url=newUrl;
        // this.ORDER= order.toLowerCase();
        this.score=newScore;
        this.title=findtitle(newUrl);
    }


    public String getUrl(){ 
        return this.url;
    }


    /*
It calculates the score by multiplying the score of each url by its pagerank value and returns the updated score dict.
:param cosine_similarity: dictionary of the cosine similarity of every url.
*/
public void setScore(Double pagerank){
    DecimalFormat df = new DecimalFormat("#.###");  //rounding to 3 decimal pts
    this.score= Double.valueOf(df.format(this.score * pagerank));
    // this.score= Math.round(this.score * pagerank *1000d)/1000d;
}

public String getTitle(){
    return this.title;
}

public double getScore(){
    return this.score;
}

/*
It returns the title of the passed url by lookup from its crawled data in webData directory.
*/
static String findtitle(String url){
    String filename=CrawlerHelpers.changeLinkToFileName(url)+".txt";
    BufferedReader file;
    String title=null;
    try{
        file= new BufferedReader(new FileReader("webData"+ File.separator+ filename));
        title= file.readLine();
        title=file.readLine();
        file.close();
    }catch(Exception e){
        e.printStackTrace();
    }
    return title;
}
}
