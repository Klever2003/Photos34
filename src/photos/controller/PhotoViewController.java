package photos.controller;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;
import photos.model.Photo;
import photos.model.Tag;

/**
 * Controller for the photo view window.
 * Shows a single photo with caption, date, and tags.
 * @author Klever and Shrij
 */
public class PhotoViewController {
    
    @FXML
    private ImageView photoImageView;
    
    @FXML
    private Label captionLabel;
    
    @FXML
    private Label dateTimeLabel;
    
    @FXML
    private ListView<String> tagsListView;
    
    @FXML
    private Button closeButton;
    
    private Photo photo;
    
    /**
     * Initializes the controller with photo data.
     * @param photo The photo to display
     */
    public void initData(Photo photo) {
        this.photo = photo;
        
        // Load photo
        Image image = new Image("file:" + photo.getFilePath());
        photoImageView.setImage(image);
        
        // Set a reasonable maximum size while preserving aspect ratio
        double maxWidth = 700;  // Slightly less than the StackPane width
        double maxHeight = 300; // Slightly less than the StackPane height
        
        // Only constrain dimensions if the image is larger than our max size
        if (image.getWidth() > maxWidth || image.getHeight() > maxHeight) {
            if (image.getWidth() / image.getHeight() > maxWidth / maxHeight) {
                // Image is wider than tall, constrain width
                photoImageView.setFitWidth(maxWidth);
            } else {
                // Image is taller than wide, constrain height
                photoImageView.setFitHeight(maxHeight);
            }
        }
        
        // Set caption
        captionLabel.setText(photo.getCaption());
        
        // Format date
        SimpleDateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy hh:mm:ss a");
        dateTimeLabel.setText("Date: " + dateFormat.format(photo.getDateTime().getTime()));
        
        // Load tags
        refreshTagsList();
    }
    
    /**
     * Updates the tags list display.
     */
    private void refreshTagsList() {
        List<String> tagStrings = new ArrayList<>();
        for (Tag tag : photo.getTags()) {
            tagStrings.add(tag.toString());
        }
        
        ObservableList<String> tags = FXCollections.observableArrayList(tagStrings);
        tagsListView.setItems(tags);
    }
    
    /**
     * This is the close button click.
     */
    @FXML
    public void handleClose(ActionEvent event) {
        Stage stage = (Stage) closeButton.getScene().getWindow();
        stage.close();
    }
}