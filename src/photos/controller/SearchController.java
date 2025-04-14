package photos.controller;

import java.io.IOException;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextInputDialog;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleGroup;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import photos.model.Album;
import photos.model.DataManager;
import photos.model.Photo;
import photos.model.Tag;
import photos.model.User;

/**
 * Controller for the search view.
 * Being able to search photos by date range or tags.
 * @author Klever and Shrij
 */
public class SearchController {
    
    @FXML
    private RadioButton dateSearchRadio;
    
    @FXML
    private RadioButton tagSearchRadio;
    
    @FXML
    private ToggleGroup searchTypeGroup;
    
    @FXML
    private VBox dateSearchPane;
    
    @FXML
    private DatePicker fromDatePicker;
    
    @FXML
    private DatePicker toDatePicker;
    
    @FXML
    private VBox tagSearchPane;
    
    @FXML
    private ComboBox<String> tagTypeComboBox;
    
    @FXML
    private TextField tagValueField;
    
    @FXML
    private RadioButton singleTagRadio;
    
    @FXML
    private RadioButton conjunctionRadio;
    
    @FXML
    private RadioButton disjunctionRadio;
    
    @FXML
    private ToggleGroup tagSearchTypeGroup;
    
    @FXML
    private HBox secondTagPane;
    
    @FXML
    private ComboBox<String> secondTagTypeComboBox;
    
    @FXML
    private TextField secondTagValueField;
    
    @FXML
    private Button searchButton;
    
    @FXML
    private Button createAlbumButton;
    
    @FXML
    private FlowPane resultsFlowPane;
    
    @FXML
    private Button backButton;
    
    private User user;
    private List<Photo> searchResults = new ArrayList<>();
    
    /**
     * Initializes the controller with user data.
     * @param user The logged-in user
     */
    public void initData(User user) {
        this.user = user;
        
        // Initialize UI components
        createAlbumButton.setDisable(true); // Disable until search is performed
        
        // Set up tag type combo boxes
        refreshTagTypes();
        
        // Default to date search
        handleSearchTypeChange(null);
    }
    
    /**
     * Updates tag type combo boxes with user's tag types.
     */
    private void refreshTagTypes() {
        List<String> tagTypes = user.getTagTypes();
        tagTypeComboBox.setItems(FXCollections.observableArrayList(tagTypes));
        secondTagTypeComboBox.setItems(FXCollections.observableArrayList(tagTypes));
        
        if (!tagTypes.isEmpty()) {
            tagTypeComboBox.setValue(tagTypes.get(0));
            secondTagTypeComboBox.setValue(tagTypes.get(0));
        }
    }
    
    /**
     * Being able to change between date and tag search.
     */
    @FXML
    public void handleSearchTypeChange(ActionEvent event) {
        if (dateSearchRadio.isSelected()) {
            dateSearchPane.setVisible(true);
            tagSearchPane.setVisible(false);
        } else {
            dateSearchPane.setVisible(false);
            tagSearchPane.setVisible(true);
        }
    }
    
    /**
     * Changing between single tag, AND, and OR searches.
     */
    @FXML
    public void handleTagSearchTypeChange(ActionEvent event) {
        secondTagPane.setVisible(!singleTagRadio.isSelected());
    }
    
    /**
     * Performing the search.
     */
    @FXML
    public void handleSearch(ActionEvent event) {
        resultsFlowPane.getChildren().clear();
        searchResults.clear();
        
        if (dateSearchRadio.isSelected()) {
            searchByDate();
        } else {
            searchByTags();
        }
        
        displayResults();
        
        // Enable/disable create album button based on results
        createAlbumButton.setDisable(searchResults.isEmpty());
    }
    
    /**
     * Performs a search by date range.
     */
    private void searchByDate() {
        LocalDate fromDate = fromDatePicker.getValue();
        LocalDate toDate = toDatePicker.getValue();
        
        if (fromDate == null || toDate == null) {
            showAlert("Error", "Please select both From and To dates", AlertType.ERROR);
            return;
        }
        
        if (fromDate.isAfter(toDate)) {
            showAlert("Error", "From date must be before To date", AlertType.ERROR);
            return;
        }
        
        // Convert LocalDate to Calendar for comparison
        Calendar fromCal = localDateToCalendar(fromDate);
        Calendar toCal = localDateToCalendar(toDate);
        
        // Add one day to include the end date in search results
        toCal.add(Calendar.DAY_OF_MONTH, 1);
        
        // Search for photos in the date range
        for (Album album : user.getAlbums()) {
            for (Photo photo : album.getPhotos()) {
                Calendar photoDate = photo.getDateTime();
                
                if ((photoDate.after(fromCal) || photoDate.equals(fromCal)) && 
                    (photoDate.before(toCal))) {
                    if (!searchResults.contains(photo)) {
                        searchResults.add(photo);
                    }
                }
            }
        }
    }
    
    /**
     * Converts a LocalDate to Calendar.
     */
    private Calendar localDateToCalendar(LocalDate localDate) {
        Date date = Date.from(localDate.atStartOfDay(ZoneId.systemDefault()).toInstant());
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        return calendar;
    }
    
    /**
     * Performs a search by tags.
     */
    private void searchByTags() {
        String tagType = tagTypeComboBox.getValue();
        String tagValue = tagValueField.getText().trim();
        
        if (tagType == null || tagValue.isEmpty()) {
            showAlert("Error", "Please select a tag type and enter a value", AlertType.ERROR);
            return;
        }
        
        if (singleTagRadio.isSelected()) {
            // Single tag search
            searchBySingleTag(tagType, tagValue);
        } else {
            // Conjunction or disjunction search
            String secondTagType = secondTagTypeComboBox.getValue();
            String secondTagValue = secondTagValueField.getText().trim();
            
            if (secondTagType == null || secondTagValue.isEmpty()) {
                showAlert("Error", "Please select a second tag type and enter a value", AlertType.ERROR);
                return;
            }
            
            if (conjunctionRadio.isSelected()) {
                // AND search
                searchByConjunction(tagType, tagValue, secondTagType, secondTagValue);
            } else {
                // OR search
                searchByDisjunction(tagType, tagValue, secondTagType, secondTagValue);
            }
        }
    }
    
    /**
     * Searches for photos with a single tag.
     */
    private void searchBySingleTag(String tagType, String tagValue) {
        Tag searchTag = new Tag(tagType, tagValue);
        
        for (Album album : user.getAlbums()) {
            for (Photo photo : album.getPhotos()) {
                if (photo.getTags().contains(searchTag) && !searchResults.contains(photo)) {
                    searchResults.add(photo);
                }
            }
        }
    }
    
    /**
     * Searches for photos with both tags (AND).
     */
    private void searchByConjunction(String tagType1, String tagValue1, String tagType2, String tagValue2) {
        Tag searchTag1 = new Tag(tagType1, tagValue1);
        Tag searchTag2 = new Tag(tagType2, tagValue2);
        
        for (Album album : user.getAlbums()) {
            for (Photo photo : album.getPhotos()) {
                Set<Tag> photoTags = photo.getTags();
                if (photoTags.contains(searchTag1) && photoTags.contains(searchTag2) && 
                    !searchResults.contains(photo)) {
                    searchResults.add(photo);
                }
            }
        }
    }
    
    /**
     * Searches for photos with either tag (OR).
     */
    private void searchByDisjunction(String tagType1, String tagValue1, String tagType2, String tagValue2) {
        Tag searchTag1 = new Tag(tagType1, tagValue1);
        Tag searchTag2 = new Tag(tagType2, tagValue2);
        
        for (Album album : user.getAlbums()) {
            for (Photo photo : album.getPhotos()) {
                Set<Tag> photoTags = photo.getTags();
                if ((photoTags.contains(searchTag1) || photoTags.contains(searchTag2)) && 
                    !searchResults.contains(photo)) {
                    searchResults.add(photo);
                }
            }
        }
    }
    
    /**
     * Displays search results in the UI.
     */
    private void displayResults() {
        if (searchResults.isEmpty()) {
            Label noResultsLabel = new Label("No matching photos found");
            noResultsLabel.setStyle("-fx-font-size: 16px;");
            resultsFlowPane.getChildren().add(noResultsLabel);
            return;
        }
        
        // Display thumbnails of matching photos
        for (Photo photo : searchResults) {
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
                
                // Add to flow pane
                resultsFlowPane.getChildren().add(photoBox);
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
     * Creation of an album from search results.
     */
    @FXML
    public void handleCreateAlbum(ActionEvent event) {
        if (searchResults.isEmpty()) {
            showAlert("Error", "No search results to create album from", AlertType.ERROR);
            return;
        }
        
        TextInputDialog dialog = new TextInputDialog("Search Results");
        dialog.setTitle("Create Album");
        dialog.setHeaderText("Enter a name for the new album:");
        dialog.setContentText("Album Name:");
        
        Optional<String> result = dialog.showAndWait();
        if (result.isPresent()) {
            String albumName = result.get().trim();
            
            if (albumName.isEmpty()) {
                showAlert("Error", "Album name cannot be empty", AlertType.ERROR);
                return;
            }
            
            Album album = user.createAlbum(albumName);
            if (album == null) {
                showAlert("Error", "An album with this name already exists", AlertType.ERROR);
                return;
            }
            
            // Add all search results to the new album
            for (Photo photo : searchResults) {
                album.addPhoto(photo);
            }
            
            DataManager.getInstance().saveData();
            showAlert("Success", "Album created with " + searchResults.size() + " photos", AlertType.INFORMATION);
        }
    }
    
    /**
     * Going back to the album list.
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