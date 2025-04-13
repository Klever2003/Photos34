package photos.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Represents a user in the photo album app.
 * Tracks all their albums and tag types they've created.
 * @author Klever and Shrij
 */
public class User implements Serializable {
    private static final long serialVersionUID = 1L;
    
    private final String username;     // User's login name
    private final List<Album> albums;  // Their photo albums
    private final List<String> tagTypes; // Custom tag types they've defined
    
    /**
     * Creates a new user account.
     * @param username Their login name
     */
    public User(String username) {
        this.username = username;
        this.albums = new ArrayList<>();
        this.tagTypes = new ArrayList<>();
        
        // Add default tag types everyone starts with
        tagTypes.add("location");
        tagTypes.add("person");
    }
    
    /**
     * Gets this user's login name.
     */
    public String getUsername() {
        return username;
    }
    
    /**
     * Gets all albums for this user.
     * Returns a copy of the list so outside code can't mess with our data.
     */
    public List<Album> getAlbums() {
        return new ArrayList<>(albums);
    }
    
    /**
     * Finds a specific album by name.
     * @param name The album name to look for
     * @return The album if found, or null if no match
     */
    public Album getAlbum(String name) {
        for (Album album : albums) {
            if (album.getName().equals(name)) {
                return album;
            }
        }
        return null;
    }
    
    /**
     * Makes a new album for this user.
     * @param name What to call the new album
     * @return The new album, or null if an album with this name already exists
     */
    public Album createAlbum(String name) {
        if (getAlbum(name) != null) {
            return null; // Album with this name already exists
        }
        
        Album album = new Album(name);
        albums.add(album);
        return album;
    }
    
    /**
     * Deletes an album.
     * @param name The name of the album to delete
     * @return true if deleted, false if album wasn't found
     */
    public boolean deleteAlbum(String name) {
        Album album = getAlbum(name);
        if (album != null) {
            return albums.remove(album);
        }
        return false;
    }
    
    /**
     * Changes an album's name.
     * @param oldName Current album name
     * @param newName New album name
     * @return true if renamed, false if couldn't rename (album not found or name conflict)
     */
    public boolean renameAlbum(String oldName, String newName) {
        Album album = getAlbum(oldName);
        if (album == null || getAlbum(newName) != null) {
            return false;
        }
        
        album.setName(newName);
        return true;
    }
    
    /**
     * Gets all tag types this user has defined.
     */
    public List<String> getTagTypes() {
        return new ArrayList<>(tagTypes);
    }
    
    /**
     * Adds a new custom tag type.
     * @param tagType The tag type to add
     * @return true if added, false if it already exists
     */
    public boolean addTagType(String tagType) {
        if (!tagTypes.contains(tagType)) {
            return tagTypes.add(tagType);
        }
        return false;
    }
}