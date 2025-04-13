package photos.controller;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceDialog;
import javafx.scene.control.Label;
import javafx.scene.control.TextInputDialog;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import photos.model.Album;
import photos.model.DataManager;
import photos.model.Photo;
import photos.model.Tag;
import photos.model.User;

/**
 * Controller for the album view screen.
 * Handles displaying and managing photos in an album.
 * @author YourName
 */
public class AlbumViewController {
    
    @FXML
    private Label albumNameLabel;
    
    @FXML
    private FlowPane photoFlowPane;
    
    @FXML
    private Button addPhotoButton;
    
    @FXML
    private Button removePhotoButton;
    
    @FXML
    private Button captionPhotoButton;
    
    @FXML
    private Button displayPhotoButton;
    
    @FXML
    private Button slideshowBackButton;
    
    @FXML
    private Button slideshowForwardButton;
    
    @FXML
    private Button copyPhotoButton;
    
    @FXML
    private Button movePhotoButton;
    
    @FXML
    private Button tagPhotoButton;
    
    @FXML
    private Button removeTagButton;
    
    @FXML
    private Button backButton;
    
    private User user;
    private Album album;
    private Photo selectedPhoto;
    private int currentPhotoIndex = -1;
    
    /**
     * Initializes the controller with user and album data.
     * @param user The current user
     * @param album The album to display
     */
    public void initData(User user, Album album) {
        this.user = user;
        this.album = album;
        albumNameLabel.setText(album.getName());
        
        refreshPhotoView();
        
        // Disable slideshow buttons initially
        updateSlideshowButtons();
    }
    
    /**
     * Updates the photo grid with current photos.
     */
    private void refreshPhotoView() {
        photoFlowPane.getChildren().clear();
        
        for (Photo photo : album.getPhotos()) {
            try {
                // Create thumbnail
                ImageView imageView = createThumbnail(photo);
                
                // Create caption label
                Label captionLabel = new Label(photo.getCaption());
                captionLabel.setWrapText(true);
                captionLabel.setMaxWidth(150);
                captionLabel.setAlignment(Pos.CENTER);
                
                // Create container
                VBox photoBox = new VBox(5);
                photoBox.setAlignment(Pos.CENTER);
                photoBox.getChildren().addAll(imageView, captionLabel);
                
                // Add selection handler
                photoBox.setOnMouseClicked(event -> {
                    selectedPhoto = photo;
                    currentPhotoIndex = album.getPhotos().indexOf(photo);
                    
                    // Highlight selected photo
                    for (int i = 0; i < photoFlowPane.getChildren().size(); i++) {
                        VBox box = (VBox) photoFlowPane.getChildren().get(i);
                        if (box == photoBox) {
                            box.setStyle("-fx-background-color: lightblue; -fx-padding: 5px;");
                        } else {
                            box.setStyle("-fx-background-color: transparent; -fx-padding: 5px;");
                        }
                    }
                    
                    updateSlideshowButtons();
                });
                
                // Add to flow pane
                photoFlowPane.getChildren().add(photoBox);
            } catch (Exception e) {
                System.err.println("Error loading image: " + e.getMessage());
            }
        }
    }
    
    /**
     * Creates a thumbnail for a photo.
     */
    private ImageView createThumbnail(Photo photo) {
        Image image = new Image("file:" + photo.getFilePath(), 150, 150, true, true);
        ImageView imageView = new ImageView(image);
        imageView.setFitWidth(150);
        imageView.setFitHeight(150);
        imageView.setPreserveRatio(true);
        return imageView;
    }
    
    /**
     * Updates the slideshow navigation buttons based on current position.
     */
    private void updateSlideshowButtons() {
        int size = album.getPhotos().size();
        
        if (size == 0) {
            slideshowBackButton.setDisable(true);
            slideshowForwardButton.setDisable(true);
            return;
        }
        
        slideshowBackButton.setDisable(currentPhotoIndex <= 0);
        slideshowForwardButton.setDisable(currentPhotoIndex >= size - 1 || currentPhotoIndex == -1);
    }
    
    /**
     * Handles adding a new photo to the album.
     */
    @FXML
    public void handleAddPhoto(ActionEvent event) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select Photo");
        fileChooser.getExtensionFilters().addAll(
            new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg", "*.gif", "*.bmp")
        );
        
        File selectedFile = fileChooser.showOpenDialog(addPhotoButton.getScene().getWindow());
        if (selectedFile != null) {
            try {
                Photo photo = new Photo(selectedFile);
                if (album.addPhoto(photo)) {
                    refreshPhotoView();
                    showAlert("Success", "Photo added successfully", AlertType.INFORMATION);
                    DataManager.getInstance().saveData();
                } else {
                    showAlert("Error", "Photo already exists in this album", AlertType.ERROR);
                }
            } catch (Exception e) {
                showAlert("Error", "Failed to add photo: " + e.getMessage(), AlertType.ERROR);
                e.printStackTrace();
            }
        }
    }
    
    /**
     * Handles removing the selected photo from the album.
     */
    @FXML
    public void handleRemovePhoto(ActionEvent event) {
        if (selectedPhoto == null) {
            showAlert("Error", "No photo selected", AlertType.ERROR);
            return;
        }
        
        if (album.removePhoto(selectedPhoto)) {
            selectedPhoto = null;
            currentPhotoIndex = -1;
            refreshPhotoView();
            showAlert("Success", "Photo removed successfully", AlertType.INFORMATION);
            DataManager.getInstance().saveData();
        } else {
            showAlert("Error", "Failed to remove photo", AlertType.ERROR);
        }
    }
    
    /**
     * Handles changing the caption of the selected photo.
     */
    @FXML
    public void handleCaptionPhoto(ActionEvent event) {
        if (selectedPhoto == null) {
            showAlert("Error", "No photo selected", AlertType.ERROR);
            return;
        }
        
        TextInputDialog dialog = new TextInputDialog(selectedPhoto.getCaption());
        dialog.setTitle("Caption Photo");
        dialog.setHeaderText("Enter caption for the photo:");
        dialog.setContentText("Caption:");
        
        Optional<String> result = dialog.showAndWait();
        if (result.isPresent()) {
            selectedPhoto.setCaption(result.get());
            refreshPhotoView();
            showAlert("Success", "Caption updated successfully", AlertType.INFORMATION);
            DataManager.getInstance().saveData();
        }
    }
    
    /**
     * Handles displaying the selected photo in a separate window.
     */
    @FXML
    public void handleDisplayPhoto(ActionEvent event) {
        if (selectedPhoto == null) {
            showAlert("Error", "No photo selected", AlertType.ERROR);
            return;
        }
        
        try {
            // Create a new stage for displaying the photo
            Stage photoStage = new Stage();
            photoStage.initModality(Modality.APPLICATION_MODAL);
            photoStage.setTitle("Photo: " + selectedPhoto.getCaption());
            
            // Load photo view
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/photos/view/photoView.fxml"));
            Parent root = loader.load();
            
            // Initialize controller
            PhotoViewController controller = loader.getController();
            controller.initData(selectedPhoto);
            
            // Set up scene
            Scene scene = new Scene(root, 800, 600);
            photoStage.setScene(scene);
            photoStage.show();
        } catch (IOException e) {
            showAlert("Error", "Failed to display photo: " + e.getMessage(), AlertType.ERROR);
            e.printStackTrace();
        }
    }
    
    /**
     * Handles navigating to the previous photo in slideshow mode.
     */
    @FXML
    public void handleSlideshowBack(ActionEvent event) {
        if (currentPhotoIndex > 0) {
            currentPhotoIndex--;
            selectedPhoto = album.getPhotos().get(currentPhotoIndex);
            
            // Update selection in UI
            refreshPhotoView();
            VBox photoBox = (VBox) photoFlowPane.getChildren().get(currentPhotoIndex);
            photoBox.setStyle("-fx-background-color: lightblue; -fx-padding: 5px;");
            
            updateSlideshowButtons();
        }
    }
    
    /**
     * Handles navigating to the next photo in slideshow mode.
     */
    @FXML
    public void handleSlideshowForward(ActionEvent event) {
        if (currentPhotoIndex < album.getPhotos().size() - 1) {
            currentPhotoIndex++;
            selectedPhoto = album.getPhotos().get(currentPhotoIndex);
            
            // Update selection in UI
            refreshPhotoView();
            VBox photoBox = (VBox) photoFlowPane.getChildren().get(currentPhotoIndex);
            photoBox.setStyle("-fx-background-color: lightblue; -fx-padding: 5px;");
            
            updateSlideshowButtons();
        }
    }
    
    /**
     * Handles copying the selected photo to another album.
     */
    @FXML
    public void handleCopyPhoto(ActionEvent event) {
        if (selectedPhoto == null) {
            showAlert("Error", "No photo selected", AlertType.ERROR);
            return;
        }
        
        // Get list of albums (excluding current one)
        List<String> albumNames = new ArrayList<>();
        for (Album a : user.getAlbums()) {
            if (!a.getName().equals(album.getName())) {
                albumNames.add(a.getName());
            }
        }
        
        if (albumNames.isEmpty()) {
            showAlert("Error", "No other albums available. Create another album first.", AlertType.ERROR);
            return;
        }
        
        // Show album selection dialog
        ChoiceDialog<String> dialog = new ChoiceDialog<>(albumNames.get(0), albumNames);
        dialog.setTitle("Copy Photo");
        dialog.setHeaderText("Select destination album:");
        dialog.setContentText("Album:");
        
        Optional<String> result = dialog.showAndWait();
        if (result.isPresent()) {
            String destAlbumName = result.get();
            Album destAlbum = user.getAlbum(destAlbumName);
            
            if (destAlbum.addPhoto(selectedPhoto)) {
                showAlert("Success", "Photo copied successfully", AlertType.INFORMATION);
                DataManager.getInstance().saveData();
            } else {
                showAlert("Error", "Photo already exists in the destination album", AlertType.ERROR);
            }
        }
    }
    
    /**
     * Handles moving the selected photo to another album.
     */
    @FXML
    public void handleMovePhoto(ActionEvent event) {
        if (selectedPhoto == null) {
            showAlert("Error", "No photo selected", AlertType.ERROR);
            return;
        }
        
        // Get list of albums (excluding current one)
        List<String> albumNames = new ArrayList<>();
        for (Album a : user.getAlbums()) {
            if (!a.getName().equals(album.getName())) {
                albumNames.add(a.getName());
            }
        }
        
        if (albumNames.isEmpty()) {
            showAlert("Error", "No other albums available. Create another album first.", AlertType.ERROR);
            return;
        }
        
        // Show album selection dialog
        ChoiceDialog<String> dialog = new ChoiceDialog<>(albumNames.get(0), albumNames);
        dialog.setTitle("Move Photo");
        dialog.setHeaderText("Select destination album:");
        dialog.setContentText("Album:");
        
        Optional<String> result = dialog.showAndWait();
        if (result.isPresent()) {
            String destAlbumName = result.get();
            Album destAlbum = user.getAlbum(destAlbumName);
            
            if (destAlbum.addPhoto(selectedPhoto) && album.removePhoto(selectedPhoto)) {
                selectedPhoto = null;
                currentPhotoIndex = -1;
                refreshPhotoView();
                showAlert("Success", "Photo moved successfully", AlertType.INFORMATION);
                DataManager.getInstance().saveData();
            } else {
                showAlert("Error", "Failed to move photo", AlertType.ERROR);
            }
        }
    }
    
    /**
     * Handles adding a tag to the selected photo.
     */
    @FXML
    public void handleTagPhoto(ActionEvent event) {
        if (selectedPhoto == null) {
            showAlert("Error", "No photo selected", AlertType.ERROR);
            return;
        }
        
        // Get user's tag types and add option for new type
        List<String> tagTypes = new ArrayList<>(user.getTagTypes());
        tagTypes.add("Add new tag type...");
        
        // Show tag type selection dialog
        ChoiceDialog<String> typeDialog = new ChoiceDialog<>(tagTypes.get(0), tagTypes);
        typeDialog.setTitle("Add Tag");
        typeDialog.setHeaderText("Select tag type:");
        typeDialog.setContentText("Type:");
        
        Optional<String> typeResult = typeDialog.showAndWait();
        if (!typeResult.isPresent()) {
            return;
        }
        
        String tagType = typeResult.get();
        
        // Handle creating new tag type
        if (tagType.equals("Add new tag type...")) {
            TextInputDialog newTypeDialog = new TextInputDialog();
            newTypeDialog.setTitle("New Tag Type");
            newTypeDialog.setHeaderText("Enter new tag type:");
            newTypeDialog.setContentText("Type:");
            
            Optional<String> newTypeResult = newTypeDialog.showAndWait();
            if (!newTypeResult.isPresent() || newTypeResult.get().trim().isEmpty()) {
                return;
            }
            
            tagType = newTypeResult.get().trim();
            if (user.addTagType(tagType)) {
                showAlert("Success", "New tag type added", AlertType.INFORMATION);
            } else {
                showAlert("Error", "Tag type already exists", AlertType.ERROR);
                return;
            }
        }
        
        // Get tag value
        TextInputDialog valueDialog = new TextInputDialog();
        valueDialog.setTitle("Tag Value");
        valueDialog.setHeaderText("Enter value for '" + tagType + "' tag:");
        valueDialog.setContentText("Value:");
        
        Optional<String> valueResult = valueDialog.showAndWait();
        if (!valueResult.isPresent() || valueResult.get().trim().isEmpty()) {
            return;
        }
        
        String tagValue = valueResult.get().trim();
        
        // Add tag to photo
        Tag tag = new Tag(tagType, tagValue);
        if (selectedPhoto.addTag(tag)) {
            showAlert("Success", "Tag added successfully", AlertType.INFORMATION);
            DataManager.getInstance().saveData();
        } else {
            showAlert("Error", "Tag already exists for this photo", AlertType.ERROR);
        }
    }
    
    /**
     * Handles removing a tag from the selected photo.
     */
    @FXML
    public void handleRemoveTag(ActionEvent event) {
        if (selectedPhoto == null) {
            showAlert("Error", "No photo selected", AlertType.ERROR);
            return;
        }
        
        // Get all tags for the photo
        List<Tag> tags = new ArrayList<>(selectedPhoto.getTags());
        
        if (tags.isEmpty()) {
            showAlert("Error", "No tags to remove", AlertType.ERROR);
            return;
        }
        
        // Convert tags to strings for the dialog
        List<String> tagStrings = new ArrayList<>();
        for (Tag tag : tags) {
            tagStrings.add(tag.toString());
        }
        
        // Show tag selection dialog
        ChoiceDialog<String> dialog = new ChoiceDialog<>(tagStrings.get(0), tagStrings);
        dialog.setTitle("Remove Tag");
        dialog.setHeaderText("Select tag to remove:");
        dialog.setContentText("Tag:");
        
        Optional<String> result = dialog.showAndWait();
        if (result.isPresent()) {
            String tagString = result.get();
            int index = tagStrings.indexOf(tagString);
            Tag tagToRemove = tags.get(index);
            
            if (selectedPhoto.removeTag(tagToRemove)) {
                showAlert("Success", "Tag removed successfully", AlertType.INFORMATION);
                DataManager.getInstance().saveData();
            } else {
                showAlert("Error", "Failed to remove tag", AlertType.ERROR);
            }
        }
    }
    
    /**
     * Handles returning to the album list view.
     */
    @FXML
    public void handleBack(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/photos/view/albumList.fxml"));
            Parent root = loader.load();
            
            AlbumListController controller = loader.getController();
            controller.initData(user);
            
            Stage stage = (Stage) backButton.getScene().getWindow();
            stage.setTitle("Photo Album - " + user.getUsername());
            stage.setScene(new Scene(root, 800, 600));
            stage.show();
        } catch (IOException e) {
            showAlert("Error", "Failed to go back: " + e.getMessage(), AlertType.ERROR);
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
