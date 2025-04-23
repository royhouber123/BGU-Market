package market.domain.purchase;

import market.model.Purchase;
import market.model.ShoppingCart;

public interface IPurchase {
    /**
     * Executes the purchase logic for the given user and shopping cart.
     *
     * @param userId  the ID of the purchasing user
     * @param cart    the user's shopping cart
     * @param shippingAddress the address to deliver the purchase to
     * @param contactInfo     contact info of the buyer
     * @return a Purchase object representing the completed transaction
     */
    Purchase purchase(String userId, ShoppingCart cart, String shippingAddress, String contactInfo);
}
