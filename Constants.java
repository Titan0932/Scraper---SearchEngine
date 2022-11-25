import java.util.HashMap;
import java.io.File;

public final class Constants {

    private Constants() {
        // restrict instantiation
    }
    //  These are the constant values that are used throughout the project.


//              To name the files as their urls. However, since some symbols are not supported in folder names, I want to replace the symbols with supporting ones (for windows| also works for apple).
    public static final HashMap<String, String> symbolsMap = new HashMap<String,String>(){
    {
        put("/", "^__^");
            put(":", ";__;");
            put("&#47", "@__@");  // for forward slash(\) symbol
        put("", "%__%");
            put("*", ".__.");
            put("?", "!__!");
            put(">", "_]_");
            put("<", "_[_");
            put("\"", "_'_");
    }
};



    public static final float CONST_ALPHA = (float)0.1;                   //To calculate the transition probability and the transport matrices
    public static final double CONST_DROPTHRESHOLD= 0.0001 ;            //Used to find out when the euclidian distance converges

    public static final String[] allDirs={"webData", "tf", "idf", "tfidf", "pageRank"};   //all the directories that are used to store data
}