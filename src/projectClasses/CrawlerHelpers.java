package projectClasses;

import java.io.*;
import java.nio.file.*;
import java.util.*;

public interface CrawlerHelpers {


    static boolean check_dir_exists(String dirname){
        Path path = Paths.get(dirname);
        return ( Files.isDirectory(path));
    };

    /*
       Adds the word, title and links data in json file for every url
        //:param url: url from which the data has been crawled
        //:param words, links: the string of words/ links contained in url seperated by a single space
        //:param title: string of the title of the url
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
    It parses the html file and extracts the links and words from it.

    :param string: the html string
    :param linkQueue: A queue of links to be parsed
    :param url: the url of the page from where we're parsing
    :param pagesWordsCount: a dictionary that keeps track of the number of words in each page
    :param uniqueWords: a dictionary that contains all the unique words in the pages that have been parsed so far
*/
    public static List<String> parseHtml(String string,
                                         List<String> linkQueue,
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
        
        String fulllinks = getLinks(links, url, linkQueue);
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
