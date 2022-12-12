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

    @Override
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
        v.getSearchButton().setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent actionEvent) {
                v.update(0);
            }
        });
        v.getResultList().setOnMousePressed(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent mouseEvent) {
                int index = v.getResultList().getSelectionModel().getSelectedIndex();
                v.update2(index);
            }
        });
        v.getNumber().setOnMousePressed(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent mouseEvent) {
                int index = v.getNumber().getSelectionModel().getSelectedIndex();
                v.update2(index);
            }
        });
        v.getScores().setOnMousePressed(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent mouseEvent) {
                int index = v.getScores().getSelectionModel().getSelectedIndex();
                v.update2(index);
            }
        });
        v.getPageRank().setOnMousePressed(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent mouseEvent) {
                int index = v.getResultList().getSelectionModel().getSelectedIndex();
                v.updateToggle();
            }
        });
    }
}
