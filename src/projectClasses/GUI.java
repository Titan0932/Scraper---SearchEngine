package projectClasses;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;
import javafx.event.*;

public class GUI extends Application {

    public static void main(String[] args) {
        launch(args);
    }

    @Override//Creates stage
    public void start(Stage primaryStage) {
        ProjectTesterImp crawl = new ProjectTesterImp();
//        crawl.crawl("https://people.scs.carleton.ca/~davidmckenney/fruits/N-2.html");
        Pane aPane = new Pane();
        view v = new view();
        aPane.getChildren().add(v);
        primaryStage.setTitle("SearchBro");
        primaryStage.setResizable(false);
        primaryStage.setScene(new Scene(aPane));

        primaryStage.show();
        //Checks for search button click, after it is clicked the program should perform search using the given parameters
        v.getSearchButton().setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent actionEvent) {
                v.update(0);
            }
        });
        //For user to decide whether they want the page rank boost in their
        v.getPageRank().setOnMousePressed(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent mouseEvent) {
                int index = v.getResultList().getSelectionModel().getSelectedIndex();
                v.updateToggle();
            }
        });
    }
}
