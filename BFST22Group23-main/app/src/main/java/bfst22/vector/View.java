package bfst22.vector;

import java.io.IOException;

import bfst22.vector.model.Model;
import javafx.fxml.FXMLLoader;
import javafx.stage.Stage;

public class View {
    private Stage primaryStage;

    public View(Model model, Stage primaryStage) throws IOException {
        this.primaryStage = primaryStage;
        primaryStage.show();
        var loader = new FXMLLoader(View.class.getResource("View.fxml"));
        primaryStage.setScene(loader.load());
        Controller controller = loader.getController();
        controller.init(model, this);

        primaryStage.widthProperty().addListener((obs, oldVal, newVal) -> { controller.onResizeWindow(); });
        primaryStage.heightProperty().addListener((obs, oldVal, newVal) -> { controller.onResizeWindow(); });

        primaryStage.setTitle("TerraVision");
    }

    public Stage getStage() {
        return primaryStage;
    }
}
