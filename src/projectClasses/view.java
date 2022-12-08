package projectClasses;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleButton;
import javafx.scene.layout.Pane;
import javafx.scene.control.Button;
import java.awt.*;
import java.lang.reflect.Array;
import java.util.ArrayList;

import javafx.scene.control.ToggleButton;

public class view extends Pane{
    private TextField search;
    private ListView<String> rList;
    private Button searchButton;
    private ToggleButton pageRank;
    private Crawler crawl;
    private ListView<Integer> number;
    public view(){
        crawl = new Crawler();
        Label label1 = new Label("Search");
        label1.relocate(60,30);

        search = new TextField();
        search.setPrefSize(275,10);
        search.relocate(120,25);
        Label label2 = new Label("Result");
        label2.relocate(60,65);
        rList = new ListView<String>();
        rList.setPrefSize(225,235);
        rList.relocate(170,65);
        searchButton = new Button("Search");
        searchButton.setStyle("-fx-font: 12 arial; -fx-base: rgb(0,100,0); -fx-text-fill: rgb(255,255,255);");
        searchButton.relocate(170,340);
        pageRank = new ToggleButton("Boost");
        pageRank.relocate(300,340);
        Integer i[] = {1,2,3,4,5,6,7,8,9,10};
        number = new ListView<Integer>();
        number.setPrefSize(35, 235);
        number.relocate(120,65);
        number.setItems(FXCollections.observableArrayList(i));

        getChildren().addAll(label1,label2,search, rList,searchButton, pageRank, number);
        setPrefSize(500,400);
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
    public void update(int i){
        ArrayList<String> result = new ArrayList<>();
        for(SearchResult x: crawl.search(search.getText(), pageRank.isSelected(), 10)){
            result.add(x.getTitle());
        }
        rList.setItems(FXCollections.observableArrayList(result));
        number.getSelectionModel().select(i);
    }
}
