package market.domain.policy;
import market.domain.store.StoreBag;

public interface PurchasePolicy {
    void validate(String userId, StoreBag bag) throws RuntimeException;
}
