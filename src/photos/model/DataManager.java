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
 * Manages data persistence for the photo album application.
 * @author Klever and Shrij
 */
public class DataManager {
    private static final String DATA_DIR = "data";
    private static final String ADMIN_FILE = DATA_DIR + File.separator + "admin.dat";
    private static final String USER_DIR = DATA_DIR + File.separator + "users";
    
    private Admin admin;
    private Map<String, User> users;
    private User currentUser;
    
    private static DataManager instance;
    
    /**
     * Gets the singleton instance of the DataManager.
     * @return The DataManager instance
     */
    public static DataManager getInstance() {
        if (instance == null) {
            instance = new DataManager();
        }
        return instance;
    }
    
    /**
     * Private constructor to enforce singleton pattern.
     */
    private DataManager() {
        users = new HashMap<>();
        
        // Create directories if they don't exist
        new File(DATA_DIR).mkdirs();
        new File(USER_DIR).mkdirs();
        
        loadData();
    }
    
    /**
     * Gets the admin.
     * @return The admin
     */
    public Admin getAdmin() {
        return admin;
    }
    
    /**
     * Gets the current user.
     * @return The current user, or null if no user is logged in
     */
    public User getCurrentUser() {
        return currentUser;
    }
    
    /**
     * Sets the current user.
     * @param username The username of the user to set as current
     * @return true if successful, false if no such user exists
     */
    public boolean setCurrentUser(String username) {
        if (username.equals(Admin.getAdminUsername())) {
            currentUser = null; // Admin is not a regular user
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
     * @return The user, or null if no such user exists
     */
    public User getUser(String username) {
        return users.get(username);
    }
    
    /**
     * Creates a new user.
     * @param username The username for the new user
     * @return The newly created user, or null if a user with the same username already exists
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
     * @param username The username of the user to delete
     * @return true if the user was deleted, false otherwise
     */
    public boolean deleteUser(String username) {
        if (username.equals("stock")) {
            return false; // Cannot delete stock user
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
        
        // Load stock photos
        loadStockPhotos();
    }
    
    /**
     * Loads stock photos into the stock album.
     */
    private void loadStockPhotos() {
        User stockUser = users.get("stock");
        if (stockUser == null) return;
        
        Album stockAlbum = stockUser.getAlbum("stock");
        if (stockAlbum == null) return;
        
        // Skip loading if the album already has photos
        if (!stockAlbum.getPhotos().isEmpty()) return;
        
        File stockDir = new File(DATA_DIR + File.separator + "stock");
        if (!stockDir.exists() || !stockDir.isDirectory()) {
            stockDir.mkdirs();
            return;
        }
        
        // Load all image files from stock directory
        File[] files = stockDir.listFiles((dir, name) -> {
            String lowercaseName = name.toLowerCase();
            return lowercaseName.endsWith(".jpg") || lowercaseName.endsWith(".jpeg") ||
                   lowercaseName.endsWith(".png") || lowercaseName.endsWith(".gif") ||
                   lowercaseName.endsWith(".bmp");
        });
        
        if (files != null) {
            for (File file : files) {
                try {
                    Photo photo = new Photo(file);
                    stockAlbum.addPhoto(photo);
                } catch (Exception e) {
                    System.err.println("Error loading stock photo: " + file.getName() + " - " + e.getMessage());
                }
            }
        }
        
        // Save the stock user with the loaded photos
        saveUser(stockUser);
    }
    
    /**
     * Loads the admin from disk.
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
     * @param username The username of the user to load
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