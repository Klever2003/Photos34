package photos;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import photos.model.DataManager;

public class Photos34 extends Application {
    
    @Override
    public void start(Stage primaryStage) {
        try {
            // Load login scene
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/photos/view/login.fxml"));
            Parent root = loader.load();
            
            // Seting up the stage
            primaryStage.setTitle("Photo Album");
            primaryStage.setScene(new Scene(root, 400, 300));
            primaryStage.setResizable(false);
            primaryStage.show();
            
            // Seting up close request handler to save data when the application is closed
            primaryStage.setOnCloseRequest(event -> {
                DataManager.getInstance().saveData();
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    public static void main(String[] args) {
        launch(args);
    }
}