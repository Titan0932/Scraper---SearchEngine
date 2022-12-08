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
        rList.setPrefSize(275,150);
        rList.relocate(120,65);
        searchButton = new Button("Search");
        searchButton.setStyle("-fx-font: 12 arial; -fx-base: rgb(0,100,0); -fx-text-fill: rgb(255,255,255);");
        searchButton.relocate(170,240);
        pageRank = new ToggleButton("Boost");
        pageRank.relocate(300,240);

        getChildren().addAll(label1,label2,search, rList,searchButton, pageRank);
        setPrefSize(500,300);
    }
    public TextField getSearch(){
        return search;
    }
    public ListView<String> getrList(){
        return rList;
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
    public void update(){
        ArrayList<String> result = new ArrayList<>();
        for(SearchResult x: crawl.search(search.getText(), pageRank.isSelected(), 10)){
            result.add(x.getTitle());
        }
        rList.setItems(FXCollections.observableArrayList(result));
    }
}
