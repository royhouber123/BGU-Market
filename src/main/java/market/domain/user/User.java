package market.domain.user;

public class User {
    
    private String userName;
    private ShoppingCart shoppingCart;

    public User(String userName) {
        this.userName = userName;
        this.shoppingCart = new ShoppingCart();
    }

    public String getuserName() {
        return userName;
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

    public void clearCart(){
        this.shoppingCart = new ShoppingCart();
    }
}
