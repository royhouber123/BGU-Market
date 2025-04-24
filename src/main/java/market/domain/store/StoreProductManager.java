package market.domain.store;

import java.util.List;


/*
need to implement!!!!!!!!!!!!!
 */
public class StoreProductManager implements IStoreProductsManager {

    @Override
    public boolean addProduct(String name, int price, String category, int quantity) {
        return false;
    }

    @Override
    public boolean removeProduct(String name) {
        return false;
    }

    @Override
    public boolean reduceProductQuantity(String name, int howMuch) {
        return false;
    }

    @Override
    public boolean updateQuantity(String name, int howMuch) {
        return false;
    }

    @Override
    public boolean addCategory(String CatName) {
        return false;
    }

    @Override
    public Product getProduct(String Name) {
        return null;
    }

    @Override
    public List<Product> getCategoryProducts(String CatName) {
        return null;
    }

    @Override
    public boolean moveProductToCategory(String product, String catName) {
        return false;
    }

    @Override
    public boolean updateProductPrice(String product, int newPrice) {
        return false;
    }
}
