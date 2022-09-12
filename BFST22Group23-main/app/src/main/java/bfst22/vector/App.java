package bfst22.vector;

import java.io.FileNotFoundException;

import bfst22.vector.model.Model;
import javafx.application.Application;
import javafx.stage.Stage;

public class App extends Application {
    @Override
    public void start(Stage primaryStage) throws Exception {
        String filename = "denmark.osm.obj";
        new View(new Model(filename), primaryStage);
    }
}