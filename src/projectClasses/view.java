package projectClasses;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.*;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.Pane;

import java.awt.*;
import java.lang.reflect.Array;
import java.util.ArrayList;

import javafx.scene.control.ToggleButton;

public class view extends Pane{
    private TextField search, amountResults;
    private ListView<String> rList;
    private Button searchButton;
    private ToggleButton pageRank;
    private Crawler crawl;
    private ListView<Integer> number;
    private ListView<Double> scores;
    private Label label1,label2,label3, label4;
    public view(){
        crawl = new Crawler();
        label1 = new Label("Search");
        label1.relocate(40,30);
        search = new TextField();
        search.setPrefSize(255,10);
        search.relocate(140,25);

        label4 = new Label("Amount of results");
        label4.relocate(40,60);
        amountResults = new TextField();
        amountResults.setPrefSize(130,10);
        amountResults.relocate(140,55);

        label2 = new Label("Result");
        label2.relocate(140,90);
        rList = new ListView<String>();
        rList.setPrefSize(115,235);
        rList.relocate(140,110);
        searchButton = new Button("Search");
        searchButton.setStyle("-fx-font: 12 arial; -fx-base: rgb(0,100,0); -fx-text-fill: rgb(255,255,255);");
        searchButton.relocate(280,55);
        pageRank = new ToggleButton("Boost");
        pageRank.relocate(345,55);
        pageRank.setStyle("-fx-font: 12 arial;");

        number = new ListView<Integer>();

        number.setPrefSize(35, 235);
        number.relocate(90,110);

        label3 = new Label("Scores");
        label3.relocate(275,90);
        scores = new ListView<Double>();
        scores.setPrefSize(90,235);
        scores.relocate(275,110);

        getChildren().addAll(label1,label2,search, rList,searchButton, pageRank, number, scores,label3, amountResults,label4);
        setPrefSize(435,375);
    }
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
    public ListView<Integer> getNumber(){
        return number;
    }
    public ListView<Double> getScores(){
        return  scores;
    }
    public void update(int i){
        ArrayList<String> result = new ArrayList<>();
        Integer amount = Integer.parseInt(amountResults.getText());
        Integer[] numbers = new Integer[amount];
        ArrayList<Double> score= new ArrayList<>();
        for(int j = 0; j<amount; j++){
            numbers[j] = j+1;

        }

        for(SearchResult x: crawl.search(search.getText(), pageRank.isSelected(), amount)){
            result.add(x.getTitle());
            score.add(x.getScore());
        }
        rList.setItems(FXCollections.observableArrayList(result));
        number.setItems(FXCollections.observableArrayList(numbers));
        scores.setItems(FXCollections.observableArrayList(score));
        number.getSelectionModel().select(i);
        scores.getSelectionModel().select(i);
        rList.getSelectionModel().select(i);
    }
    public void update2(int i){
        number.getSelectionModel().select(i);
        scores.getSelectionModel().select(i);
        rList.getSelectionModel().select(i);
    }
    public void updateToggle(){
        if(pageRank.isSelected()){
            pageRank.setStyle("-fx-base: white");
        }
        else{
            pageRank.setStyle("-fx-base: blue");
        }
    }
}
