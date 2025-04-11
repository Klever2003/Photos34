package photos.model;

import java.io.Serializable;

/**
 * Represents a tag for photos with a name and value pair.
 * @author Klever and Shrij
 */
public class Tag implements Serializable {
    private static final long serialVersionUID = 1L;
    
    private String name;
    private String value;
    
    /**
     * Constructs a new tag with the specified name and value.
     * @param name The name of the tag 
     * @param value The value of the tag (e.g., "New Brunswick")
     */
    public Tag(String name, String value) {
        this.name = name;
        this.value = value;
    }
    
    /**
     * Gets the name of tag
     * @return
     */
    public String getName() {
        return name;
    }
    
    /**
     * Gets the value of this tag
     * @return
     */
    public String getValue() {
        return value;
    }
    
    /**
     * Sets the value
     * @param value The new value
     */
    public void setValue(String value) {
        this.value = value;
    }
    
    /**
     * Checks if this tag equals another object
     * Tags will be considered equal if they have the same name and value
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        
        Tag other = (Tag) obj;
        return name.equals(other.name) && value.equals(other.value);
    }
    
    /**
     * Returns a hash code
     */
    @Override
    public int hashCode() {
        return 31 * name.hashCode() + value.hashCode();
    }
    
    /**
     * Returns a string representation of this tag
     */
    @Override
    public String toString() {
        return name + ": " + value;
    }
}
