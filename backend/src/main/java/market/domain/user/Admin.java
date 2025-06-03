package market.domain.user;

/**
 * Represents a system administrator in the marketplace.
 * Logic is handled externally by AdminService.
 */
public class Admin extends Subscriber {

    public Admin(String userName) {
        super(userName);
    }
}