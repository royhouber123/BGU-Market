package market.application.External.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class ExternalServiceConfig {
    
    @Value("${external.payment.url:https://damp-lynna-wsep-1984852e.koyeb.app/}")
    private String paymentUrl;
    
    @Value("${external.shipment.url:https://damp-lynna-wsep-1984852e.koyeb.app/}")
    private String shipmentUrl;
    
    public String getPaymentUrl() {
        return paymentUrl;
    }
    
    public String getShipmentUrl() {
        return shipmentUrl;
    }
}
