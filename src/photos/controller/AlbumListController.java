package photos.controller;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Optional;

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
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.TextInputDialog;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.stage.Stage;
import photos.model.Album;
import photos.model.DataManager;
import photos.model.User;

/**
 * Controller for the album list view.
 * Shows all albums for a user and provides album management functions.
 * @author YourName
 */
public class AlbumListController {
    
    @FXML
    private Label welcomeLabel;
    
    @FXML
    private ListView<Album> albumListView;
    
    @FXML
    private TextField albumNameField;
    
    @FXML
    private Button createAlbumButton;
    
    @FXML
    private Button renameAlbumButton;
    
    @FXML
    private Button deleteAlbumButton;
    
    @FXML
    private Button openAlbumButton;
    
    @FXML
    private Button searchButton;
    
    @FXML
    private Button logoutButton;
    
    private User user;
    
    /**
     * Initializes the controller with user data.
     * Called after the FXML is loaded.
     * @param user The logged-in user
     */
    public void initData(User user) {
        this.user = user;
        welcomeLabel.setText("Welcome, " + user.getUsername());
        refreshAlbumList();
        
        // Set up custom cell factory to display album info
        albumListView.setCellFactory(listView -> new AlbumListCell());
    }
    
    /**
     * Updates the album list display with current data.
     */
    private void refreshAlbumList() {
        ObservableList<Album> albums = FXCollections.observableArrayList(user.getAlbums());
        albumListView.setItems(albums);
    }
    
    /**
     * Handles creating a new album.
     */
    @FXML
    public void handleCreateAlbum(ActionEvent event) {
        String albumName = albumNameField.getText().trim();
        
        if (albumName.isEmpty()) {
            showAlert("Error", "Album name cannot be empty", AlertType.ERROR);
            return;
        }
        
        if (user.createAlbum(albumName) != null) {
            showAlert("Success", "Album created successfully", AlertType.INFORMATION);
            albumNameField.clear();
            refreshAlbumList();
            DataManager.getInstance().saveData(); // Save changes
        } else {
            showAlert("Error", "Album already exists", AlertType.ERROR);
        }
    }
    
    /**
     * Handles renaming the selected album.
     */
    @FXML
    public void handleRenameAlbum(ActionEvent event) {
        Album album = albumListView.getSelectionModel().getSelectedItem();
        
        if (album == null) {
            showAlert("Error", "No album selected", AlertType.ERROR);
            return;
        }
        
        TextInputDialog dialog = new TextInputDialog(album.getName());
        dialog.setTitle("Rename Album");
        dialog.setHeaderText("Enter new album name:");
        dialog.setContentText("Name:");
        
        Optional<String> result = dialog.showAndWait();
        if (result.isPresent()) {
            String newName = result.get().trim();
            
            if (newName.isEmpty()) {
                showAlert("Error", "Album name cannot be empty", AlertType.ERROR);
                return;
            }
            
            if (user.renameAlbum(album.getName(), newName)) {
                showAlert("Success", "Album renamed successfully", AlertType.INFORMATION);
                refreshAlbumList();
                DataManager.getInstance().saveData(); // Save changes
            } else {
                showAlert("Error", "Failed to rename album. Name may already be in use.", AlertType.ERROR);
            }
        }
    }
    
    /**
     * Handles deleting the selected album.
     */
    @FXML
    public void handleDeleteAlbum(ActionEvent event) {
        Album album = albumListView.getSelectionModel().getSelectedItem();
        
        if (album == null) {
            showAlert("Error", "No album selected", AlertType.ERROR);
            return;
        }
        
        if (user.deleteAlbum(album.getName())) {
            showAlert("Success", "Album deleted successfully", AlertType.INFORMATION);
            refreshAlbumList();
            DataManager.getInstance().saveData(); // Save changes
        } else {
            showAlert("Error", "Failed to delete album", AlertType.ERROR);
        }
    }
    
    /**
     * Handles opening the selected album.
     */
    @FXML
    public void handleOpenAlbum(ActionEvent event) {
        Album album = albumListView.getSelectionModel().getSelectedItem();
        
        if (album == null) {
            showAlert("Error", "No album selected", AlertType.ERROR);
            return;
        }
        
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/photos/view/albumView.fxml"));
            Parent root = loader.load();
            
            AlbumViewController controller = loader.getController();
            controller.initData(user, album);
            
            Stage stage = (Stage) openAlbumButton.getScene().getWindow();
            stage.setTitle("Photo Album - " + album.getName());
            stage.setScene(new Scene(root, 800, 600));
            stage.show();
        } catch (IOException e) {
            showAlert("Error", "Failed to open album: " + e.getMessage(), AlertType.ERROR);
            e.printStackTrace();
        }
    }
    
    /**
     * Handles opening the search dialog.
     */
    @FXML
    public void handleSearch(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/photos/view/search.fxml"));
            Parent root = loader.load();
            
            SearchController controller = loader.getController();
            controller.initData(user);
            
            Stage stage = (Stage) searchButton.getScene().getWindow();
            stage.setTitle("Photo Album - Search");
            stage.setScene(new Scene(root, 800, 600));
            stage.show();
        } catch (IOException e) {
            showAlert("Error", "Failed to open search: " + e.getMessage(), AlertType.ERROR);
            e.printStackTrace();
        }
    }
    
    /**
     * Handles logging out and returning to login screen.
     */
    @FXML
    public void handleLogout(ActionEvent event) {
        DataManager.getInstance().logout(); // Save data and log out
        
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
    
    /**
     * Custom cell for displaying album information.
     * Shows album name, photo count, and date range.
     */
    private class AlbumListCell extends ListCell<Album> {
        private VBox container;
        private Label nameLabel;
        private Label photoCountLabel;
        private Label dateRangeLabel;
        
        public AlbumListCell() {
            container = new VBox(5);
            nameLabel = new Label();
            nameLabel.setFont(Font.font(16));
            photoCountLabel = new Label();
            dateRangeLabel = new Label();
            container.getChildren().addAll(nameLabel, photoCountLabel, dateRangeLabel);
            container.setPadding(new javafx.geometry.Insets(5, 10, 5, 10));
        }
        
        @Override
        protected void updateItem(Album album, boolean empty) {
            super.updateItem(album, empty);
            
            if (empty || album == null) {
                setGraphic(null);
            } else {
                nameLabel.setText(album.getName());
                photoCountLabel.setText("Photos: " + album.getPhotoCount());
                
                // Format date range
                SimpleDateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy");
                Calendar earliest = album.getEarliestDate();
                Calendar latest = album.getLatestDate();
                
                if (earliest != null && latest != null) {
                    String earliestStr = dateFormat.format(earliest.getTime());
                    String latestStr = dateFormat.format(latest.getTime());
                    dateRangeLabel.setText("Date Range: " + earliestStr + " - " + latestStr);
                } else {
                    dateRangeLabel.setText("Date Range: N/A");
                }
                
                setGraphic(container);
            }
        }
    }
}