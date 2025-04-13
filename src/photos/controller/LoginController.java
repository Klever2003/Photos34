package photos.controller;
import java.io.IOException;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import photos.model.Admin;
import photos.model.DataManager;
import photos.model.User;

/**
 * Controller for the login screen.
 * Handles user login and directing to appropriate screens.
 * @author Klever and Shrij
 */
public class LoginController {
    
    @FXML
    private TextField usernameField;
    
    @FXML
    private Button loginButton;
    
    /**
     * Handles the login button click.
     * Checks if username exists and routes to appropriate screen.
     */
    @FXML
    public void handleLogin(ActionEvent event) {
        String username = usernameField.getText().trim();
        
        if (username.isEmpty()) {
            showAlert("Login Error", "Username cannot be empty", AlertType.ERROR);
            return;
        }
        
        // Check if admin
        if (username.equals(Admin.getAdminUsername())) {
            DataManager.getInstance().setCurrentUser(username);
            openAdminView();
            return;
        }
        
        // Check if user exists
        if (DataManager.getInstance().setCurrentUser(username)) {
            openAlbumListView();
            return;
        }
        
        // User does not exist
        showAlert("Login Error", "User does not exist", AlertType.ERROR);
    }
    
    /**
     * Opens the admin view.
     */
    private void openAdminView() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/photos/view/admin.fxml"));
            Parent root = loader.load();
            
            Stage stage = (Stage) loginButton.getScene().getWindow();
            stage.setTitle("Photo Album - Admin");
            stage.setScene(new Scene(root, 600, 400));
            stage.setResizable(true);
            stage.show();
        } catch (IOException e) {
            showAlert("Error", "Failed to open admin view: " + e.getMessage(), AlertType.ERROR);
            e.printStackTrace();
        }
    }
    
    /**
     * Opens the album list view for a regular user.
     */
    private void openAlbumListView() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/photos/view/albumList.fxml"));
            Parent root = loader.load();
            
            // Will need to implement this controller next
            AlbumListController controller = loader.getController();
            controller.initData(DataManager.getInstance().getCurrentUser());
            
            Stage stage = (Stage) loginButton.getScene().getWindow();
            stage.setTitle("Photo Album - " + DataManager.getInstance().getCurrentUser().getUsername());
            stage.setScene(new Scene(root, 800, 600));
            stage.setResizable(true);
            stage.show();
        } catch (IOException e) {
            showAlert("Error", "Failed to open album list view: " + e.getMessage(), AlertType.ERROR);
            e.printStackTrace();
        }
    }
    
    /**
     * Shows an alert dialog.
     */
    private void showAlert(String title, String content, AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}