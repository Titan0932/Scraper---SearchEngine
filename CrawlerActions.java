import java.io.*;
import java.nio.file.*;
import java.nio.file.*;
import java.util.*;

/*
It is inherited by the Project Tester.
Consists of methods used by the crawler in the crawling process
 */
public interface CrawlerActions extends CrawlerHelpers {
/*
returns the idf value of a word
:param uniqueword: a string for the word for which the idf is generated
:param saveFile: a boolean flag which decides whether or not to save the idf values in a file or not.(It is used for the initialization of the idf values storing file)
*/
    public default float generateIdf(String uniqueWord, Boolean saveFile){
    int wordCount = 0;
    File[] allFiles = CrawlerHelpers.get_url_list("webData");
    BufferedReader dataFile;
    String words;
    for(File file : allFiles){
        try {
            dataFile = new BufferedReader(new FileReader("webData/" + file.getName()));
        }catch (Exception e){
            System.out.println(e);
            return -1;
        }

        int totalUrlNum = (allFiles).length;
        try {
            words = dataFile.readLine();  //url
            words= dataFile.readLine();     //title
            words= dataFile.readLine();     //finally words
            if (words.contains(uniqueWord)){
                wordCount += 1;
            }

        }catch (Exception e){
            System.out.println(e);
            return -1;
        }
    }
    float idf = Math.log2(totalUrlNum / (1 + wordCount));
    if (saveFile){
        if (os.path.isfile('idf/words.json')) {
            uniqueIdfsFile = open('idf/words.json', 'r+');

            uniqueWordsData = json.load(uniqueIdfsFile);
            newData = {uniqueWord: float(idf)};
            uniqueWordsData.update(newData);
            uniqueIdfsFile.seek(0);
        } else {
            uniqueIdfsFile = open('idf/words.json', 'w');
            uniqueWordsData = {uniqueWord: float(idf)};
            json.dump(uniqueWordsData, uniqueIdfsFile);
            uniqueIdfsFile.close();
        }
    }
        return idf;

    }

}
