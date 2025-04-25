package market.domain.user;

public class User {
    
    private String userId;
    private ShoppingCart shoppingCart;

    public User(String userId) {
        this.userId = userId;
        this.shoppingCart = new ShoppingCart();
    }

    public String getUserId() {
        return userId;
    }

    public ShoppingCart getShoppingCart() {
        return shoppingCart;
    }

    public void addProductToCart(String storeId, String productId, int quantity) {
        shoppingCart.addProduct(storeId, productId, quantity);
    }

    public void removeProductFromCart(String storeId, String productId,  int quantity) {
        shoppingCart.removeProduct(storeId, productId, quantity);
    }
}
