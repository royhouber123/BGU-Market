package market.domain.user;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.util.UUID;
import org.springframework.stereotype.Controller;
import java.beans.ConstructorProperties;
import market.infrastructure.converters.ShoppingCartConverter;


/**
 * Represents a user in the system with a username and shopping cart.
 */
@Entity
@Table(name = "users")
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "user_type", discriminatorType = DiscriminatorType.STRING)
@DiscriminatorValue("USER")
public class User {

    @Id
    @Column(name = "user_name", nullable = false, unique = true)
    private String userName;                  
    
    @Convert(converter = ShoppingCartConverter.class)
    @Column(columnDefinition = "TEXT")
    private ShoppingCart shoppingCart;

    /**
     * Default constructor for JPA
     */
    public User() {
        this.shoppingCart = new ShoppingCart();
    }

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
}
