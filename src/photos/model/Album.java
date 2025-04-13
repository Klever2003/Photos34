package photos.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

/**
 * Album class to store and organize photos.
 * Had to make it Serializable so we can save/load albums between sessions.
 * @author Klever and Shrij
 */
public class Album implements Serializable {
    // Need this for serialization - keeps Java from complaining
    private static final long serialVersionUID = 1L;
    
    private String name;         // What the user named this album
    private final List<Photo> photos;  // All the photos inside this album
    
    /**
     * Creates a new empty album.
     * @param name What to call this album
     */
    public Album(String name) {
        this.name = name;
        this.photos = new ArrayList<>(); // Start with empty list, user will add photos later
    }
    
    /**
     * Gets the album name.
     */
    public String getName() {
        return name;
    }
    
    /**
     * Changes the album name.
     * Users might want to rename their albums later.
     */
    public void setName(String name) {
        this.name = name;
    }
    
    /**
     * Gives you a copy of the photos list.
     * Using a new ArrayList to prevent outside code from messing with our actual list.
     */
    public List<Photo> getPhotos() {
        return new ArrayList<>(photos);
    }
    
    /**
     * Counts how many photos are in this album.
     * Faster than getting the whole list and checking its size.
     */
    public int getPhotoCount() {
        return photos.size();
    }
    
    /**
     * Finds the oldest photo's date in the album.
     * Returns null for empty albums since there's no earliest date.
     */
    public Calendar getEarliestDate() {
        if (photos.isEmpty()) {
            return null;
        }
        
        // Start with the first photo's date
        Calendar earliest = photos.get(0).getDateTime();
        // Check all other photos to find the earliest
        for (int i = 1; i < photos.size(); i++) {
            Calendar current = photos.get(i).getDateTime();
            if (current.before(earliest)) {
                earliest = current;
            }
        }
        return earliest;
    }
    
    /**
     * Finds the newest photo's date in the album.
     * Basically the opposite of getEarliestDate().
     */
    public Calendar getLatestDate() {
        if (photos.isEmpty()) {
            return null;
        }
        
        Calendar latest = photos.get(0).getDateTime();
        for (int i = 1; i < photos.size(); i++) {
            Calendar current = photos.get(i).getDateTime();
            if (current.after(latest)) {
                latest = current;
            }
        }
        return latest;
    }
    
    /**
     * Adds a photo to this album.
     * Makes sure we don't add duplicates - specs say no dupes allowed.
     */
    public boolean addPhoto(Photo photo) {
        if (!photos.contains(photo)) {
            return photos.add(photo);
        }
        return false;
    }
    
    /**
     * Kicks a photo out of this album.
     */
    public boolean removePhoto(Photo photo) {
        return photos.remove(photo);
    }
    
    /**
     * Checks if this album equals another one.
     * Only checks the name since album names must be unique for a user.
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        
        Album other = (Album) obj;
        return name.equals(other.name);
    }
    
    /**
     * Just using the name for the hash code.
     * Since we're using name for equality checks, this makes sense.
     */
    @Override
    public int hashCode() {
        return name.hashCode();
    }
}
