import java.util.List;

record Data(List<String> data){


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
