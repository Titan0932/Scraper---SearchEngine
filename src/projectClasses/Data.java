package projectClasses;

import java.util.List;

record Data(List<String> data){
    // This is an immutable record structure that stores all parsed data. It makes sure that your parsed data doesn't get changed by accident while performing analysis. Also, if there's many elements of data 
    // parsed then, this makes it easier to understand the data and use them as well.

    public String getTitle(){
        return data.get(0);
    }
    public String getWords(){
        return data.get(1);
    }

    public String getLinks(){
        return data.get(2);
    }


}
