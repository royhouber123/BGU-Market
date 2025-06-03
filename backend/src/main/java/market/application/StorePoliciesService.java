package market.application;

import java.util.List;
import java.util.stream.Collectors;

import market.domain.store.IStoreRepository;
import market.domain.store.Store;
import market.domain.store.Policies.DiscountPolicy;
import market.domain.store.Policies.PurchasePolicy;
import market.domain.store.Policies.Discounts.DiscountPolicyFactory;
import market.domain.store.Policies.Policies.PurchasePolicyFactory;
import market.dto.PolicyDTO;
import utils.ApiResponse;
import utils.Logger;

public class StorePoliciesService {

    private final IStoreRepository storeRepository;
    private final ISuspensionRepository suspensionRepository;
    private Logger logger = Logger.getInstance();

    public StorePoliciesService(IStoreRepository storeRepo, ISuspensionRepository suspensionRepository) {
        this.storeRepository = storeRepo;
        this.suspensionRepository = suspensionRepository;
    }

    /**
     * Adds a new discount policy to a specific store by store ID.
     *
     * @param storeId    the store to apply the discount to
     * @param userId     the user attempting the action
     * @param discountDTO the discount DTO to add
     * @return true if successful, false otherwise
     */
    public ApiResponse<Boolean> addDiscount(String storeId, String userId, PolicyDTO.AddDiscountRequest discountDTO) {
        try {
            suspentionRepository.checkNotSuspended(userId);// check if user is suspended
            Store store = storeRepository.getStoreByID(storeId);
            DiscountPolicy policy = DiscountPolicyFactory.fromDTO(discountDTO);
            return ApiResponse.ok(store.addDiscount(userId, policy));
        } catch (Exception e) {
            logger.info("Failed to add discount: " + e.getMessage());
            return ApiResponse.fail(e.getMessage());
        }
    }

    /**
     * Removes an existing discount policy from a specific store by store ID.
     *
     * @param storeId     the store to remove the discount from
     * @param userId      the user attempting the action
     * @param discountDTO the discount DTO to remove
     * @return true if successful, false otherwise
     */
    public ApiResponse<Boolean> removeDiscount(String storeId, String userId, PolicyDTO.AddDiscountRequest discountDTO) {
        try {
            suspentionRepository.checkNotSuspended(userId);// check if user is suspended
            Store store = storeRepository.getStoreByID(storeId);
            DiscountPolicy policy = DiscountPolicyFactory.fromDTO(discountDTO);
            return ApiResponse.ok(store.removeDiscount(userId, policy));
        } catch (Exception e) {
            logger.info("Failed to remove discount: " + e.getMessage());
            return ApiResponse.fail(e.getMessage());
        }
    }

    /**
     * Retrieves all discount policies (as DTOs) from a specific store by store ID.
     *
     * @param storeId the ID of the store
     * @param userId  the user requesting the discounts
     * @return list of discount DTOs or an empty list on failure
     */
    public ApiResponse<List<PolicyDTO.AddDiscountRequest>> getDiscounts(String storeId, String userId) {
        try {
            Store store = storeRepository.getStoreByID(storeId);
            return ApiResponse.ok(store.getDiscountPolicies(userId)
                        .stream()
                        .map(DiscountPolicy::toDTO)
                        .collect(Collectors.toList()));
        } catch (Exception e) {
            logger.info("Failed to retrieve discounts: " + e.getMessage());
            return ApiResponse.fail(e.getMessage());
        }
    }

    /**
     * Adds a new purchase policy to a store.
     *
     * @param storeId ID of the store.
     * @param userId  ID of the user requesting the operation.
     * @param dto     The DTO representing the purchase policy.
     * @return {@code true} if added successfully, {@code false} otherwise.
     */
    public ApiResponse<Boolean> addPurchasePolicy(String storeId, String userId, PolicyDTO.AddPurchasePolicyRequest dto) {
        try {
            suspentionRepository.checkNotSuspended(userId);// check if user is suspended
            Store store = storeRepository.getStoreByID(storeId);
            PurchasePolicy policy = PurchasePolicyFactory.fromDTO(dto);
            return ApiResponse.ok(store.addPolicy(userId, policy));
        } catch (Exception e) {
            // Log or handle exception
            logger.info("Failed to add purchase policy: " + e.getMessage());
            return ApiResponse.fail(e.getMessage());
        }
    }

    /**
     * Removes a purchase policy from a store.
     *
     * @param storeId ID of the store.
     * @param userId  ID of the user requesting the operation.
     * @param dto     The DTO representing the purchase policy to remove.
     * @return {@code true} if removed successfully, {@code false} otherwise.
     */
    public ApiResponse<Boolean> removePurchasePolicy(String storeId, String userId, PolicyDTO.AddPurchasePolicyRequest dto) {
        try {
            suspentionRepository.checkNotSuspended(userId);// check if user is suspended
            Store store = storeRepository.getStoreByID(storeId);
            PurchasePolicy policy = PurchasePolicyFactory.fromDTO(dto);
            return ApiResponse.ok(store.removePolicy(userId, policy));
        } catch (Exception e) {
            logger.info("Failed to remove purchase policy: " + e.getMessage());
            return ApiResponse.fail(e.getMessage());
        }
    }

    /**
     * Retrieves all purchase policies of a store.
     *
     * @param storeId ID of the store.
     * @param userId  ID of the user requesting the policies.
     * @return List of DTOs representing the purchase policies, or an empty list on failure.
     */
    public ApiResponse<List<PolicyDTO.AddPurchasePolicyRequest>> getPurchasePolicies(String storeId, String userId) {
        try {
            Store store = storeRepository.getStoreByID(storeId);
            return ApiResponse.ok(store.getPolicies(userId).stream()
                    .map(PurchasePolicy::toDTO)
                    .collect(Collectors.toList()));
        } catch (Exception e) {
            logger.info("Failed to retrieve purchase policies: " + e.getMessage());
            return ApiResponse.fail(e.getMessage());
        }
    }
}
