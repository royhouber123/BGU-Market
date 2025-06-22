package market.domain;

import market.application.External.IPaymentService;
import market.application.External.IShipmentService;
import market.domain.purchase.*;
import utils.ApiResponse;

import org.junit.jupiter.api.*;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyDouble;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@TestInstance(TestInstance.Lifecycle.PER_METHOD)
class RegularPurchaseTest {

    private IPaymentService paymentService;
    private IShipmentService shipmentService;
    private RegularPurchase regularPurchase;

    private String userId;
    private String storeId;
    private String productId;
    private String productName;
    private String shippingAddress;
    private String contactInfo;

    @BeforeEach
    void setUp() {
        userId = "user1";
        storeId = "store1";
        productId = "prod1";
        productName = "nameProduct1";
        shippingAddress = "123 Main St";
        contactInfo = "555-555-5555";

        paymentService = mock(IPaymentService.class);
        shipmentService = mock(IShipmentService.class);

        when(paymentService.processPayment(anyString())).thenReturn(ApiResponse.ok(true));
        when(shipmentService.ship(anyString(), anyString(), anyDouble())).thenReturn(ApiResponse.ok("trackingId"));

        regularPurchase = new RegularPurchase();
    }

    @AfterEach
    void tearDown() {
        paymentService = null;
        shipmentService = null;
        regularPurchase = null;
    }

    @Test
    void purchase_validInput_shouldCreateCorrectPurchase() {
        PurchasedProduct product = new PurchasedProduct(productId, productName, storeId, 2, 50.0);
        List<PurchasedProduct> products = List.of(product);

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
        PurchasedProduct p1 = new PurchasedProduct("p1", "name1", storeId, 1, 40.0);
        PurchasedProduct p2 = new PurchasedProduct("p2", "name2", storeId, 1, 60.0);
        List<PurchasedProduct> products = List.of(p1, p2);

        Purchase purchase = regularPurchase.purchase(userId, products, shippingAddress, contactInfo, 25.0, paymentService, shipmentService);

        assertEquals(75.0, purchase.getTotalPrice());
    }

    @Test
    void purchase_zeroDiscount_shouldReturnFullPrice() {
        PurchasedProduct product = new PurchasedProduct(productId, productName, storeId, 1, 100.0);
        List<PurchasedProduct> products = List.of(product);

        Purchase purchase = regularPurchase.purchase(userId, products, shippingAddress, contactInfo, 0.0, paymentService, shipmentService);

        assertEquals(100.0, purchase.getTotalPrice());
    }

    @Test
    void purchase_shouldInvokePaymentAndShipmentServices() {
        PurchasedProduct product = new PurchasedProduct(productId, productName, storeId, 1, 70.0);
        List<PurchasedProduct> products = List.of(product);

        regularPurchase.purchase(userId, products, shippingAddress, contactInfo, 20.0, paymentService, shipmentService);

        verify(paymentService).processPayment(eq(contactInfo));
        verify(shipmentService).ship(eq(shippingAddress), eq(userId), anyDouble());
    }

    @Test
    void purchase_emptyProductList_shouldThrowException() {
        List<PurchasedProduct> products = List.of();
        Exception e = assertThrows(IllegalArgumentException.class, () -> {
            regularPurchase.purchase(userId, products, shippingAddress, contactInfo, 0.0, paymentService, shipmentService);
        });
        assertTrue(e.getMessage().contains("Items list cannot be null or empty"));
    }

    @Test
    void purchase_discountGreaterThanTotal_shouldPayZero() {
        PurchasedProduct product = new PurchasedProduct(productId, productName, storeId, 1, 20.0);
        List<PurchasedProduct> products = List.of(product);
        Purchase purchase = regularPurchase.purchase(userId, products, shippingAddress, contactInfo, 30.0, paymentService, shipmentService);
        assertEquals(0.0, purchase.getTotalPrice());
    }


}
