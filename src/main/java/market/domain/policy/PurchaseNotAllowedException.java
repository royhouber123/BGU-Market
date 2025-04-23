package market.domain.policy;

public class PurchaseNotAllowedException extends RuntimeException {
    public PurchaseNotAllowedException(String message) {
        super(message);
    }
}
