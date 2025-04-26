package market.domain.policy;


public interface DiscountPolicy {
    void apply(StoreBag bag);
}
