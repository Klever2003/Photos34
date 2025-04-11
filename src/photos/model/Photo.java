package photos.model;

import java.io.File;
import java.io.Serializable;
import java.nio.file.Files;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileTime;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Represents a photo in the photo album application.
 * @author Klever and Shrij
 */
public class Photo implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    private String filePath;     // Path to the photo file
    private String caption;      // Caption for the photo
    private Calendar dateTime;   // Date and time the photo was taken (or last modified)
    private Set<Tag> tags;       // Set of tags associated with photo
    
    /**
     * Constructs a new Photo with the specified file.
     * @param file The photo file
     * @throws Exception If there's an error accessing the file
     */
    public Photo(File file) throws Exception {
        this.filePath = file.getAbsolutePath();
        this.caption = file.getName();
        this.tags = new HashSet<>();
        
        // Gets the last modified time of the file as a proxy for when the photo was taken
        BasicFileAttributes attrs = Files.readAttributes(file.toPath(), BasicFileAttributes.class);
        FileTime fileTime = attrs.lastModifiedTime();
        
        // Convert to Calendar
        dateTime = Calendar.getInstance();
        dateTime.setTimeInMillis(fileTime.toMillis());
        dateTime.set(Calendar.MILLISECOND, 0); // Set milliseconds to zero for correct equality checks
    }
    
    /**
     * Gets the file path
     * @return
     */
    public String getFilePath() {
        return filePath;
    }
    
    /**
     * Gets the caption
     * @return The caption
     */
    public String getCaption() {
        return caption;
    }
    
    /**
     * Sets the caption
     * @param caption The new caption
     */
    public void setCaption(String caption) {
        this.caption = caption;
    }
    
    /**
     * Gets the date and time photo was taken (or last modified).
     * @return The date and time
     */
    public Calendar getDateTime() {
        return dateTime;
    }
    
    /**
     * Gets all tags associated with photo
     * @return An unmodifiable view of the tags
     */
    public Set<Tag> getTags() {
        return new HashSet<>(tags);
    }
    
    /**
     * Adds a tag to photo
     * @param tag The tag to add
     * @return true if the tag was added, false if it was already present
     */
    public boolean addTag(Tag tag) {
        return tags.add(tag);
    }
    
    /**
     * Removes a tag
     * @param tag
     * @return 
     */
    public boolean removeTag(Tag tag) {
        return tags.remove(tag);
    }
    
    /**
     * Gets all tags with the specified name
     * @param name
     * @return
     */
    public List<Tag> getTagsByName(String name) {
        List<Tag> result = new ArrayList<>();
        for (Tag tag : tags) {
            if (tag.getName().equals(name)) {
                result.add(tag);
            }
        }
        return result;
    }
    
    /**
     * Checks if this photo has a tag with the specified name and value.
     * @param name
     * @param value
     * @return true if the photo has such a tag, false otherwise
     */
    public boolean hasTag(String name, String value) {
        return tags.contains(new Tag(name, value));
    }
    
    /**
     * Checks if the photo is equal to another
     * Photos should be considered equal if they have the same file path
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        
        Photo other = (Photo) obj;
        return filePath.equals(other.filePath);
    }
    
    /**
     * Returns hash code for photo
     */
    @Override
    public int hashCode() {
        return filePath.hashCode();
    }
}