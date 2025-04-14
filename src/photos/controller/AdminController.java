package photos.controller;

import java.io.IOException;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import photos.model.Admin;
import photos.model.DataManager;

/**
 * Controller for the admin panel.
 * In charge of creating, listing, and deleting users.
 * @author Klever and Shrij
 */
public class AdminController {
    
    @FXML
    private ListView<String> userListView;
    
    @FXML
    private TextField usernameField;
    
    @FXML
    private Button createUserButton;
    
    @FXML
    private Button deleteUserButton;
    
    @FXML
    private Button logoutButton;
    
    /**
     * Initializes the controller.
     * Loads existing users into the list view.
     */
    @FXML
    public void initialize() {
        refreshUserList();
    }
    
    /**
     * Updates the user list with current data.
     */
    private void refreshUserList() {
        Admin admin = DataManager.getInstance().getAdmin();
        ObservableList<String> usernames = FXCollections.observableArrayList(admin.getUsernames());
        userListView.setItems(usernames);
    }
    
    /**
     * Creating a new user.
     */
    @FXML
    public void handleCreateUser(ActionEvent event) {
        String username = usernameField.getText().trim();
        
        if (username.isEmpty()) {
            showAlert("Error", "Username cannot be empty", AlertType.ERROR);
            return;
        }
        
        if (username.equals(Admin.getAdminUsername())) {
            showAlert("Error", "Cannot create a user with the admin username", AlertType.ERROR);
            return;
        }
        
        if (DataManager.getInstance().createUser(username) != null) {
            showAlert("Success", "User created successfully", AlertType.INFORMATION);
            usernameField.clear();
            refreshUserList();
        } else {
            showAlert("Error", "User already exists", AlertType.ERROR);
        }
    }
    
    /**
     * Handles deleting the selected user.
     */
    @FXML
    public void handleDeleteUser(ActionEvent event) {
        String username = userListView.getSelectionModel().getSelectedItem();
        
        if (username == null) {
            showAlert("Error", "No user selected", AlertType.ERROR);
            return;
        }
        
        if (DataManager.getInstance().deleteUser(username)) {
            showAlert("Success", "User deleted successfully", AlertType.INFORMATION);
            refreshUserList();
        } else {
            showAlert("Error", "Failed to delete user", AlertType.ERROR);
        }
    }
    
    /**
     * Process in charge of logging out and returning to login screen.
     */
    @FXML
    public void handleLogout(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/photos/view/login.fxml"));
            Parent root = loader.load();
            
            Stage stage = (Stage) logoutButton.getScene().getWindow();
            stage.setTitle("Photo Album");
            stage.setScene(new Scene(root, 400, 300));
            stage.setResizable(false);
            stage.show();
        } catch (IOException e) {
            showAlert("Error", "Failed to logout: " + e.getMessage(), AlertType.ERROR);
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