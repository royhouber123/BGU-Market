package market.model;

import java.util.List;

public class ShoppingCart {
    private final List<StoreBag> bags;

    public ShoppingCart(List<StoreBag> bags) {
        this.bags = bags;
    }

    public List<StoreBag> getStoreBags() {
        return bags;
    }
}
