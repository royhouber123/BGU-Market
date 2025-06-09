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
    
    // Payment details for tests
    private String currency;
    private String cardNumber;
    private String month;
    private String year;
    private String holder;
    private String ccv;

    @BeforeEach
    void setUp() throws Exception {
        userId = "user1";
        storeId = "store1";
        productId = "prod1";
        shippingAddress = "John Doe, 123 Main St, New York, USA, 12345";
        contactInfo = "555-555-5555";
        
        // Initialize payment details
        currency = "USD";
        cardNumber = "4111111111111111";
        month = "12";
        year = "2025";
        holder = "John Doe";
        ccv = "123";

        paymentService = mock(IPaymentService.class);
        shipmentService = mock(IShipmentService.class);
        storeRepository = mock(IStoreRepository.class);
        store = mock(Store.class);

        // Updated mock configurations to match new interface signatures
        when(paymentService.processPayment(anyString(), anyDouble(), anyString(), anyString(), anyString(), anyString(), anyString()))
            .thenReturn("payment-id-123");
        when(paymentService.cancelPayment(anyString())).thenReturn(true);
        when(shipmentService.ship(anyString(), anyString(), anyString(), anyString(), anyString()))
            .thenReturn("tracking-id-123");
        when(shipmentService.cancel(anyString())).thenReturn(true);

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
        
        // Updated to include payment details parameters
        Purchase purchase = regularPurchase.purchase(
            userId, 
            products, 
            shippingAddress, 
            contactInfo, 
            10.0, 
            paymentService, 
            shipmentService,
            currency,
            cardNumber,
            month,
            year,
            holder,
            ccv
        );

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

        // Updated to include payment details parameters
        Purchase purchase = regularPurchase.purchase(
            userId, 
            products, 
            shippingAddress, 
            contactInfo, 
            25.0, 
            paymentService, 
            shipmentService,
            currency,
            cardNumber,
            month,
            year,
            holder,
            ccv
        );

        assertEquals(75.0, purchase.getTotalPrice());
    }

    @Test
    void purchase_zeroDiscount_shouldReturnFullPrice() {
        PurchasedProduct product = new PurchasedProduct(productId, storeId, 1, 100.0);
        List<PurchasedProduct> products = List.of(product);

        // Updated to include payment details parameters
        Purchase purchase = regularPurchase.purchase(
            userId, 
            products, 
            shippingAddress, 
            contactInfo, 
            0.0, 
            paymentService, 
            shipmentService,
            currency,
            cardNumber,
            month,
            year,
            holder,
            ccv
        );

        assertEquals(100.0, purchase.getTotalPrice());
    }

    @Test
    void purchase_shouldInvokePaymentAndShipmentServices() {
        PurchasedProduct product = new PurchasedProduct(productId, storeId, 1, 70.0);
        List<PurchasedProduct> products = List.of(product);

        // Updated to include payment details parameters
        regularPurchase.purchase(
            userId, 
            products, 
            shippingAddress, 
            contactInfo, 
            20.0, 
            paymentService, 
            shipmentService,
            currency,
            cardNumber,
            month,
            year,
            holder,
            ccv
        );

        // Updated verification to match new interface signatures
        verify(paymentService).processPayment(eq(currency), eq(50.0), eq(cardNumber), eq(month), eq(year), eq(holder), eq(ccv));
        verify(shipmentService).ship(eq("John Doe"), eq("123 Main St"), eq("New York"), eq("USA"), eq("12345"));
    }

    @Test
    void purchase_emptyProductList_shouldThrowException() {
        List<PurchasedProduct> products = List.of();
        Exception e = assertThrows(IllegalArgumentException.class, () -> {
            // Updated to include payment details parameters
            regularPurchase.purchase(
                userId, 
                products, 
                shippingAddress, 
                contactInfo, 
                0.0, 
                paymentService, 
                shipmentService,
                currency,
                cardNumber,
                month,
                year,
                holder,
                ccv
            );
        });
        assertTrue(e.getMessage().contains("Items list cannot be null or empty"));
    }

    @Test
    void purchase_discountGreaterThanTotal_shouldPayZero() {
        PurchasedProduct product = new PurchasedProduct(productId, storeId, 1, 20.0);
        List<PurchasedProduct> products = List.of(product);
        
        // Updated to include payment details parameters
        Purchase purchase = regularPurchase.purchase(
            userId, 
            products, 
            shippingAddress, 
            contactInfo, 
            30.0, 
            paymentService, 
            shipmentService,
            currency,
            cardNumber,
            month,
            year,
            holder,
            ccv
        );
        
        assertEquals(0.0, purchase.getTotalPrice());
    }

    @Test
    void purchase_paymentFailure_shouldThrowException() {
        // Configure payment service to throw exception
        when(paymentService.processPayment(anyString(), anyDouble(), anyString(), anyString(), anyString(), anyString(), anyString()))
            .thenThrow(new RuntimeException("Payment failed: Insufficient funds"));

        PurchasedProduct product = new PurchasedProduct(productId, storeId, 1, 50.0);
        List<PurchasedProduct> products = List.of(product);

        Exception e = assertThrows(RuntimeException.class, () -> {
            regularPurchase.purchase(
                userId, 
                products, 
                shippingAddress, 
                contactInfo, 
                0.0, 
                paymentService, 
                shipmentService,
                currency,
                cardNumber,
                month,
                year,
                holder,
                ccv
            );
        });

        assertTrue(e.getMessage().contains("Payment failed"));
        // Verify payment was attempted
        verify(paymentService).processPayment(eq(currency), eq(50.0), eq(cardNumber), eq(month), eq(year), eq(holder), eq(ccv));
        // Verify shipment was not attempted after payment failure
        verify(shipmentService, never()).ship(anyString(), anyString(), anyString(), anyString(), anyString());
    }

    @Test
    void purchase_shipmentFailure_shouldCancelPaymentAndThrowException() {
        // Configure shipment service to return null (failure)
        when(shipmentService.ship(anyString(), anyString(), anyString(), anyString(), anyString()))
            .thenReturn(null);

        PurchasedProduct product = new PurchasedProduct(productId, storeId, 1, 50.0);
        List<PurchasedProduct> products = List.of(product);

        Exception e = assertThrows(RuntimeException.class, () -> {
            regularPurchase.purchase(
                userId, 
                products, 
                shippingAddress, 
                contactInfo, 
                0.0, 
                paymentService, 
                shipmentService,
                currency,
                cardNumber,
                month,
                year,
                holder,
                ccv
            );
        });

        assertTrue(e.getMessage().contains("Shipment failed"));
        // Verify payment was attempted and then cancelled
        verify(paymentService).processPayment(eq(currency), eq(50.0), eq(cardNumber), eq(month), eq(year), eq(holder), eq(ccv));
        verify(paymentService).cancelPayment("payment-id-123");
        // Verify shipment was attempted
        verify(shipmentService).ship(eq("John Doe"), eq("123 Main St"), eq("New York"), eq("USA"), eq("12345"));
    }
}
