package projectClasses;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;

public class PagerankData implements Comparable<PagerankData>, SearchResult {
    private String url;
    private String ORDER;  //descending or ascending order
    private Double score;
    private String title;


    public PagerankData(String newUrl, Double newScore){
        this(newUrl, newScore, "desc");
    }

    public PagerankData(String newUrl, Double newScore, String order){
        this.url=newUrl;
        this.ORDER= order.toLowerCase();
        this.score=newScore;
        this.title=findtitle(newUrl);
    }



    public int compareTo(PagerankData urlData){
        if(this.ORDER.equals("asc")){
            return this.score.compareTo(urlData.getScore());   //for descending order
        }
        // double urlScore= urlData.getScore();
        return urlData.score.compareTo(this.score);
    }


    public String getUrl(){
        return this.url;
    }

    public Double getDataScore(){
        return this.score;
    }

    /*
It calculates the score by multiplying the score of each url by its pagerank value and returns the updated score dict.
:param cosine_similarity: dictionary of the cosine similarity of every url.
*/
public void setScore(Double pagerank){
    // HashMap<String,Double> score=new HashMap<>();
    // for (PagerankData dataSet : treeData){
        // dataSet.setScore(dataSet.getDataScore() * this.getPageRank(dataSet.getUrl()));
        // score.put(url, cosine_similarity.get(url) * this.getPageRank(url));
    // }
    // return score;
    // System.out.println("coSIne: "+ this.score);
    // System.out.println("URL: "+url+ " => "+ pagerank);
    this.score= (this.score * pagerank);
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
