package market.domain.user;

/**
 * Represents a user in the system with a username and shopping cart.
 */
public class User {

    private String userName;                  
    private ShoppingCart shoppingCart;
    private boolean banned = false; // Default value is false

    /**
     * Creates a new user with the given username and an empty shopping cart.
     *
     * @param userName The username for this user
     */
    public User(String userName) {
        this.userName      = userName;
        this.shoppingCart  = new ShoppingCart();
    }

    /**
     * Gets the username of this user.
     *
     * @return The username
     */
    public String getUserName() { return userName; }
    
    /**
     * Gets the shopping cart of this user.
     *
     * @return The shopping cart
     */
    public ShoppingCart getShoppingCart() { return shoppingCart; }

    /**
     * Sets a new username for this user.
     *
     * @param newName The new username
     */
    public void setUserName(String newName) { this.userName = newName; }

    /**
     * Adds a product to the user's shopping cart.
     * Validates the quantity is positive before adding.
     *
     * @param storeId The ID of the store
     * @param productName The name of the product
     * @param qty The quantity to add (must be positive)
     * @throws IllegalArgumentException if quantity is not positive
     */
    public void addProductToCart(String storeId, String productName, int qty) {
        if (qty <= 0) {
            throw new IllegalArgumentException("Product quantity must be positive");
        }
        shoppingCart.addProduct(storeId, productName, qty);
    }
    
    /**
     * Removes a product from the user's shopping cart.
     * Delegates validation to the shopping cart implementation.
     *
     * @param storeId The ID of the store
     * @param productName The name of the product
     * @param qty The quantity to remove
     * @throws IllegalArgumentException if store/product doesn't exist or quantity issues
     */
    public void removeProductFromCart(String storeId, String productName, int qty) {
        shoppingCart.removeProduct(storeId, productName, qty);
    }

    /**
     * Clears the user's shopping cart by replacing it with a new empty cart.
     */
    public void clearCart() {
        this.shoppingCart = new ShoppingCart();
    }
    
    /**
     * Checks if the user is banned from the system.
     *
     * @return true if the user is banned, false otherwise
     */
    public boolean isBanned() {
        return banned;
    }
    
    /**
     * Sets the banned status of this user.
     *
     * @param banned The new banned status
     */
    public void setBanned(boolean banned) {
        this.banned = banned;
    }
}
