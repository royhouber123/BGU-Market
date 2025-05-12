package market.domain;

import market.application.External.IPaymentService;
import market.application.External.IShipmentService;
import market.domain.purchase.*;
import market.domain.store.Store;
import market.domain.store.IStoreRepository;
import market.domain.user.*;
import org.junit.jupiter.api.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@TestInstance(TestInstance.Lifecycle.PER_METHOD)
class PurchaseServiceTest {

    private IPaymentService paymentService;
    private IShipmentService shipmentService;
    private IStoreRepository storeRepository;
    private Store store;
    private ShoppingCart cart;
    private RegularPurchase regularPurchase;

    private String userId;
    private String storeId;
    private String productId;
    private String shippingAddress;
    private String contactInfo;

    @BeforeEach
    void setUp() throws Exception {
        userId = "user1";
        storeId = "store1";
        productId = "prod1";
        shippingAddress = "123 Main St";
        contactInfo = "555-555-5555";

        paymentService = mock(IPaymentService.class);
        shipmentService = mock(IShipmentService.class);
        storeRepository = mock(IStoreRepository.class);
        store = mock(Store.class);

        when(paymentService.processPayment(anyString())).thenReturn(true);
        when(shipmentService.ship(anyString(), anyString(), anyDouble())).thenReturn("OK");

        when(storeRepository.getStoreByID(storeId)).thenReturn(store);
        when(store.isPurchaseAllowed(anyMap())).thenReturn(true);
        when(store.ProductPrice(productId)).thenReturn(50.0);
        when(store.calculateStoreBagWithDiscount(anyMap())).thenReturn(10.0);

        cart = new ShoppingCart();
        cart.addProduct(storeId, productId, 2);

        regularPurchase = new RegularPurchase();
    }

    @AfterEach
    void tearDown() {
        paymentService = null;
        shipmentService = null;
        storeRepository = null;
        store = null;
        cart = null;
        regularPurchase = null;
    }

    @Test
    void purchase_validInput_shouldCreateCorrectPurchase() {
        List<PurchasedProduct> products = List.of(new PurchasedProduct(productId, storeId, 2, 50.0));
        Purchase purchase = regularPurchase.purchase(userId, products, shippingAddress, contactInfo, 10.0, paymentService, shipmentService);

        assertNotNull(purchase);
        assertEquals(userId, purchase.getUserId());
        assertEquals(shippingAddress, purchase.getShippingAddress());
        assertEquals(contactInfo, purchase.getContactInfo());
        assertEquals(1, purchase.getProducts().size());
        assertEquals(90.0, purchase.getTotalPrice());
    }

    @Test
    void purchase_multipleProducts_shouldSumPricesAndApplyDiscount() {
        PurchasedProduct p1 = new PurchasedProduct("p1", storeId, 1, 40.0);
        PurchasedProduct p2 = new PurchasedProduct("p2", storeId, 1, 60.0);
        List<PurchasedProduct> products = List.of(p1, p2);

        Purchase purchase = regularPurchase.purchase(userId, products, shippingAddress, contactInfo, 25.0, paymentService, shipmentService);

        assertEquals(75.0, purchase.getTotalPrice());
    }

    @Test
    void purchase_zeroDiscount_shouldReturnFullPrice() {
        PurchasedProduct product = new PurchasedProduct(productId, storeId, 1, 100.0);
        List<PurchasedProduct> products = List.of(product);

        Purchase purchase = regularPurchase.purchase(userId, products, shippingAddress, contactInfo, 0.0, paymentService, shipmentService);

        assertEquals(100.0, purchase.getTotalPrice());
    }

    @Test
    void purchase_shouldInvokePaymentAndShipmentServices() {
        PurchasedProduct product = new PurchasedProduct(productId, storeId, 1, 70.0);
        List<PurchasedProduct> products = List.of(product);

        regularPurchase.purchase(userId, products, shippingAddress, contactInfo, 20.0, paymentService, shipmentService);

        verify(paymentService).processPayment(contains("User: " + userId));
        verify(shipmentService).ship(eq(shippingAddress), eq(userId), anyDouble());
    }

    @Test
    void purchase_emptyProductList_shouldReturnZeroTotalPrice() {
        List<PurchasedProduct> products = List.of();
        Purchase purchase = regularPurchase.purchase(userId, products, shippingAddress, contactInfo, 0.0, paymentService, shipmentService);
        assertEquals(0.0, purchase.getTotalPrice());
        assertEquals(0, purchase.getProducts().size());
    }

    @Test
    void purchase_discountGreaterThanTotal_shouldAllowNegativeTotal() {
        PurchasedProduct product = new PurchasedProduct(productId, storeId, 1, 20.0);
        List<PurchasedProduct> products = List.of(product);

        Purchase purchase = regularPurchase.purchase(userId, products, shippingAddress, contactInfo, 30.0, paymentService, shipmentService);

        assertEquals(-10.0, purchase.getTotalPrice());
    }
}
