package photos.model;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Admin class to manage users in the photo album app.
 * Separate from regular users since admin has special powers.
 * @author Klever and Shrij
 */
public class Admin implements Serializable {
    // For serialization
    private static final long serialVersionUID = 1L;
    
    // Special username constant that won't change
    private static final String ADMIN_USERNAME = "admin";
    
    // List of all regular usernames in the system
    private final List<String> usernames;
    
    /**
     * Creates a new Admin with empty user list.
     * Adds stock user by default since we need that per the specs.
     */
    public Admin() {
        this.usernames = new ArrayList<>();
        
        // Add stock user by default
        this.usernames.add("stock");
    }
    
    /**
     * Gets the admin username.
     * @return The admin username
     */
    public static String getAdminUsername() {
        return ADMIN_USERNAME;
    }
    
    /**
     * Gets all regular usernames in the system.
     * @return Copy of the usernames list
     */
    public List<String> getUsernames() {
        return new ArrayList<>(usernames);
    }
    
    /**
     * Creates a new user in the system.
     * @param username The username to add
     * @return true if added, false if already exists or tried to add admin
     */
    public boolean addUsername(String username) {
        // Don't allow adding the admin username as a regular user
        if (!usernames.contains(username) && !username.equals(ADMIN_USERNAME)) {
            return usernames.add(username);
        }
        return false;
    }
    
    /**
     * Removes a user from the system.
     * @param username The username to remove
     * @return true if removed, false if not found
     */
    public boolean removeUsername(String username) {
        return usernames.remove(username);
    }
}