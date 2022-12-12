package projectClasses;

import javafx.collections.FXCollections;
import javafx.scene.control.*;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.Pane;
import java.util.ArrayList;
import javafx.scene.control.ToggleButton;

public class view extends Pane{
    private TextField search, amountResults;
    private ListView<String> rList;
    private Button searchButton;
    private ToggleButton pageRank;
    private Crawler crawl;
    private Label label1,label2,label3, label4, error;
    public view(){
        crawl = new Crawler();
        //takes user searchquery input
        label1 = new Label("Search");
        label1.relocate(40,30);
        search = new TextField();
        search.setPrefSize(255,10);
        search.relocate(140,25);
        //asks user how many search result they want
        label4 = new Label("Amount of results");
        label4.relocate(40,60);
        amountResults = new TextField();
        amountResults.setPrefSize(130,10);
        amountResults.relocate(140,55);

        //button for searching
        searchButton = new Button("Search");
        searchButton.setStyle("-fx-font: 12 arial; -fx-base: rgb(0,100,0); -fx-text-fill: rgb(255,255,255);");
        searchButton.relocate(280,55);

        //button for activating pagerank
        pageRank = new ToggleButton("Boost");
        pageRank.relocate(345,55);
        pageRank.setStyle("-fx-font: 12 arial;");
        label3 = new Label("Scores");
        label3.relocate(235,90);

        //attributes for displaying output
        label2 = new Label("Result");
        label2.relocate(110,90);
        rList = new ListView<String>();
        rList.setPrefSize(275,235);
        rList.relocate(80,110);

        //an error label for when they don't provide proper input in amount of results
        error = new Label();
        error.relocate(90,5);


        getChildren().addAll(label1,label2,search, rList,searchButton, pageRank, amountResults,label4, error, label3);
        setPrefSize(435,375);
    }
    //getters for proper event handling on GUI class
    public TextField getSearch(){
        return search;
    }

    public Button getSearchButton(){
        return searchButton;
    }
    public ToggleButton getPageRank(){
        return pageRank;
    }

    public ListView<String> getResultList(){
        return rList;
    }

    //Implements search, runs search method after the user clicks the search button
    public void update(int i){
        try{//try catch block for catching invalid input
            error.setVisible(false);
            ArrayList<String> result = new ArrayList<>();
            Integer amount = Integer.parseInt(amountResults.getText());
            int count = 0;
            for(SearchResult x: crawl.search(search.getText(), pageRank.isSelected(), amount)){
                count ++;
                String s = String.format("%4d.  %-35s%-5.3f",count,x.getTitle(), x.getScore());
                System.out.println(s);
                result.add(s);

            }
            rList.setItems(FXCollections.observableArrayList(result));


            rList.getSelectionModel().select(i);
        }catch(NumberFormatException e){
            error.setText("Please provide an Integer for Amount of Results");
            error.setVisible(true);
        }

    }

    public void updateToggle(){//to change color of pagerank button when selected, should be blue when selected
        if(pageRank.isSelected()){
            pageRank.setStyle("-fx-base: white");
        }
        else{
            pageRank.setStyle("-fx-base: blue");
        }
    }
}
