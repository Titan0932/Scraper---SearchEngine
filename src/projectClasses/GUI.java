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
        Pane aPane = new Pane();
        view v = new view();
        aPane.getChildren().add(v);
        primaryStage.setTitle("Search Engine");
        primaryStage.setResizable(false);
        primaryStage.setScene(new Scene(aPane));

        primaryStage.show();

    }
}
