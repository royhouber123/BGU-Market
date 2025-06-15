package market.infrastructure.converters;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import market.domain.user.ShoppingCart;
import utils.Logger;

@Converter
public class ShoppingCartConverter implements AttributeConverter<ShoppingCart, String> {

    private static final Logger logger = Logger.getInstance();
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public String convertToDatabaseColumn(ShoppingCart shoppingCart) {
        if (shoppingCart == null) {
            return null;
        }
        try {
            return objectMapper.writeValueAsString(shoppingCart);
        } catch (JsonProcessingException e) {
            logger.error("Error converting ShoppingCart to JSON: " + e.getMessage());
            throw new IllegalArgumentException("Error converting ShoppingCart to JSON", e);
        }
    }

    @Override
    public ShoppingCart convertToEntityAttribute(String dbData) {
        if (dbData == null || dbData.isEmpty()) {
            return new ShoppingCart();
        }
        try {
            return objectMapper.readValue(dbData, ShoppingCart.class);
        } catch (JsonProcessingException e) {
            logger.error("Error converting JSON to ShoppingCart: " + e.getMessage());
            throw new IllegalArgumentException("Error converting JSON to ShoppingCart", e);
        }
    }
} 