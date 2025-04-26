package market.domain.policy;
import market.domain.user.StoreBag;


public interface DiscountPolicy {
    void apply(StoreBag bag);
}
