package market.domain.store;

import java.util.List;

/**
 * Interface for managing products in a store.
 * This interface defines operations for adding, removing, updating, and querying products and categories within a store.
 */
public interface IStoreProductsManager {

    /**
     * Adds a new product to the store.
     *
     * @param name     Name of the product.
     * @param price    Price of the product.
     * @param category Category the product belongs to.
     * @param quantity Initial stock quantity.
     * @return {@code true} if the product was successfully added; {@code false} if the product already exists or failed to add.
     */
    boolean addProduct(String name, int price, String category, int quantity);

    /**
     * Removes a product from the store.
     *
     * @param name Name of the product to remove.
     * @return {@code true} if the product was successfully removed; {@code false} if the product was not found.
     */
    boolean removeProduct(String name);

    /**
     * Reduces the quantity of a given product in the store.
     *
     * @param name    Name of the product.
     * @param howMuch Quantity to reduce.
     * @return {@code true} if the quantity was successfully reduced; {@code false} if the product doesn't exist or quantity is insufficient.
     */
    boolean reduceProductQuantity(String name, int howMuch);

    /**
     * Updates the quantity of a product to a specific value.
     *
     * @param name    Name of the product.
     * @param howMuch New quantity value.
     * @return {@code true} if the update was successful; {@code false} otherwise.
     */
    boolean updateQuantity(String name, int howMuch);

    /**
     * Adds a new category to the store's catalog.
     *
     * @param CatName Name of the category to add.
     * @return {@code true} if the category was added successfully; {@code false} if the category already exists.
     */
    boolean addCategory(String CatName);

    /**
     * Retrieves a product by its name.
     *
     * @param Name Name of the product to retrieve.
     * @return The {@link Product} if found; {@code null} otherwise.
     */
    Product getProduct(String Name);

    /**
     * Retrieves all products under a specific category.
     *
     * @param CatName Name of the category.
     * @return A list of {@link Product} instances in the specified category. Returns an empty list if the category has no products.
     */
    List<Product> getCategoryProducts(String CatName);

    /**
     * Moves a product to a different category.
     *
     * @param product Name of the product to move.
     * @param catName Target category name.
     * @return {@code true} if the product was successfully moved; {@code false} otherwise.
     */
    boolean moveProductToCategory(String product, String catName);

    /**
     * Updates the price of a specific product.
     *
     * @param product  Name of the product.
     * @param newPrice New price to assign.
     * @return {@code true} if the price was updated successfully; {@code false} otherwise.
     */
    boolean updateProductPrice(String product, int newPrice);
}
