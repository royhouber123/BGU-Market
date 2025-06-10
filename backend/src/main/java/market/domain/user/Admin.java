package market.domain.user;

import jakarta.persistence.*;

/**
 * Represents a system administrator in the marketplace.
 * Logic is handled externally by AdminService.
 */
@Entity
@DiscriminatorValue("ADMIN")
public class Admin extends Subscriber {

    /**
     * Default constructor for JPA
     */
    public Admin() {
        super();
    }

    public Admin(String userName) {
        super(userName);
    }
}