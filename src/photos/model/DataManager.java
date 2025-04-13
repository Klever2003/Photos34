package photos.model;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.HashMap;
import java.util.Map;

/**
 * Handles saving and loading data between app sessions.
 * Uses serialization to store users, albums and photos.
 * @author Klever and Shrij
 */
public class DataManager {
    private static final String DATA_DIR = "data";
    private static final String ADMIN_FILE = DATA_DIR + File.separator + "admin.dat";
    private static final String USER_DIR = DATA_DIR + File.separator + "users";
    
    private Admin admin;
    private Map<String, User> users;
    private User currentUser;
    
    // Singleton pattern - only need one data manager
    private static DataManager instance;
    
    /**
     * Gets the single instance of DataManager.
     * Creates it if it doesn't exist yet.
     */
    public static DataManager getInstance() {
        if (instance == null) {
            instance = new DataManager();
        }
        return instance;
    }
    
    /**
     * Private constructor so only getInstance() can create DataManager.
     * Loads existing data from disk if available.
     */
    private DataManager() {
        users = new HashMap<>();
        
        // Create directories if they don't exist
        new File(DATA_DIR).mkdirs();
        new File(USER_DIR).mkdirs();
        
        loadData();
    }
    
    /**
     * Gets the admin object.
     */
    public Admin getAdmin() {
        return admin;
    }
    
    /**
     * Gets the currently logged in user.
     * @return Current user or null if nobody is logged in
     */
    public User getCurrentUser() {
        return currentUser;
    }
    
    /**
     * Logs in a user.
     * @param username The username to log in
     * @return true if login successful, false if user doesn't exist
     */
    public boolean setCurrentUser(String username) {
        if (username.equals(Admin.getAdminUsername())) {
            currentUser = null; // Admin isn't a regular user
            return true;
        }
        
        User user = users.get(username);
        if (user != null) {
            currentUser = user;
            return true;
        }
        return false;
    }
    
    /**
     * Logs out the current user and saves all data.
     */
    public void logout() {
        saveData();
        currentUser = null;
    }
    
    /**
     * Gets a user by username.
     * @param username The username
     * @return The user or null if not found
     */
    public User getUser(String username) {
        return users.get(username);
    }
    
    /**
     * Creates a new user.
     * @param username The username for the new user
     * @return The new user or null if username taken
     */
    public User createUser(String username) {
        if (users.containsKey(username) || username.equals(Admin.getAdminUsername())) {
            return null;
        }
        
        User user = new User(username);
        users.put(username, user);
        admin.addUsername(username);
        saveData();
        return user;
    }
    
    /**
     * Deletes a user.
     * @param username The username to delete
     * @return true if deleted, false otherwise
     */
    public boolean deleteUser(String username) {
        if (username.equals("stock")) {
            return false; // Can't delete stock user
        }
        
        User user = users.remove(username);
        if (user != null) {
            admin.removeUsername(username);
            // Delete user file
            File userFile = getUserFile(username);
            if (userFile.exists()) {
                userFile.delete();
            }
            saveAdmin();
            return true;
        }
        return false;
    }
    
    /**
     * Loads all data from disk.
     */
    private void loadData() {
        loadAdmin();
        
        // Load users
        if (admin != null) {
            for (String username : admin.getUsernames()) {
                loadUser(username);
            }
        }
        
        // Create stock user and album if they don't exist
        if (!users.containsKey("stock")) {
            User stockUser = new User("stock");
            users.put("stock", stockUser);
            
            // Add stock album if it doesn't exist
            if (stockUser.getAlbum("stock") == null) {
                stockUser.createAlbum("stock");
            }
            
            if (admin != null && !admin.getUsernames().contains("stock")) {
                admin.addUsername("stock");
            }
            
            saveUser(stockUser);
        }
    }
    
    /**
     * Loads the admin data from disk.
     */
    private void loadAdmin() {
        File adminFile = new File(ADMIN_FILE);
        if (adminFile.exists()) {
            try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(adminFile))) {
                admin = (Admin) ois.readObject();
            } catch (IOException | ClassNotFoundException e) {
                System.err.println("Error loading admin: " + e.getMessage());
                admin = new Admin();
            }
        } else {
            admin = new Admin();
            saveAdmin();
        }
    }
    
    /**
     * Loads a user from disk.
     * @param username The username to load
     */
    private void loadUser(String username) {
        File userFile = getUserFile(username);
        if (userFile.exists()) {
            try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(userFile))) {
                User user = (User) ois.readObject();
                users.put(username, user);
            } catch (IOException | ClassNotFoundException e) {
                System.err.println("Error loading user " + username + ": " + e.getMessage());
            }
        }
    }
    
    /**
     * Saves all data to disk.
     */
    public void saveData() {
        saveAdmin();
        saveUsers();
    }
    
    /**
     * Saves the admin to disk.
     */
    private void saveAdmin() {
        if (admin == null) {
            return;
        }
        
        try {
            // Create the data directory if it doesn't exist
            File dataDir = new File(DATA_DIR);
            if (!dataDir.exists()) {
                dataDir.mkdirs();
            }
            
            try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(ADMIN_FILE))) {
                oos.writeObject(admin);
            }
        } catch (IOException e) {
            System.err.println("Error saving admin: " + e.getMessage());
        }
    }
    
    /**
     * Saves all users to disk.
     */
    private void saveUsers() {
        for (User user : users.values()) {
            saveUser(user);
        }
    }
    
    /**
     * Saves a user to disk.
     * @param user The user to save
     */
    private void saveUser(User user) {
        if (user == null) {
            return;
        }
        
        try {
            // Create the users directory if it doesn't exist
            File userDir = new File(USER_DIR);
            if (!userDir.exists()) {
                userDir.mkdirs();
            }
            
            try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(getUserFile(user.getUsername())))) {
                oos.writeObject(user);
            }
        } catch (IOException e) {
            System.err.println("Error saving user " + user.getUsername() + ": " + e.getMessage());
        }
    }
    
    /**
     * Gets the file for a user.
     * @param username The username
     * @return The user file
     */
    private File getUserFile(String username) {
        return new File(USER_DIR + File.separator + username + ".dat");
    }
}