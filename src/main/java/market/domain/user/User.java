package market.domain.user;

public class User {

    private String userName;                  
    private ShoppingCart shoppingCart;

    public User(String userName) {
        this.userName      = userName;
        this.shoppingCart  = new ShoppingCart();
    }

    public String getUserName()              { return userName; }
    public ShoppingCart getShoppingCart()    { return shoppingCart; }

    public void setUserName(String newName)         { this.userName = newName; }

    public void addProductToCart(int storeId, String productName, int qty) {
        shoppingCart.addProduct(storeId, productName, qty);
    }
    public void removeProductFromCart(int storeId, String productName, int qty) {
        shoppingCart.removeProduct(storeId, productName, qty);
    }

    public void clearCart(){
        this.shoppingCart = new ShoppingCart();
    }
}
