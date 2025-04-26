package market.domain.policy;

public interface PurchasePolicy {
    void validate(String userId, StoreBag bag) throws RuntimeException;
}
