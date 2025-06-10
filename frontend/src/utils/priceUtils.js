// Utility functions for fetching real-time product prices

/**
 * Fetches the current discounted price for a product
 * @param {Object} product - The product object with storeId and id
 * @returns {Promise<number|null>} - The discounted price or null if no discount
 */
export const fetchDiscountedPrice = async (product) => {
  if (!product || !product.storeId || !product.id) {
    return null;
  }

  try {
    const response = await fetch(`http://localhost:8080/api/stores/${product.storeId}/products/${product.id}/discounted-price`);
    const apiResponse = await response.json();

    if (apiResponse.success && apiResponse.data !== undefined) {
      const discountPrice = apiResponse.data;
      // Only return discounted price if it's actually different from the original price
      if (discountPrice < product.price) {
        return discountPrice;
      }
    }
    return null;
  } catch (error) {
    console.warn(`Could not fetch discounted price for product ${product.id}:`, error);
    return null;
  }
};

/**
 * Gets the effective price for a product (discounted price if available, otherwise original price)
 * @param {Object} product - The product object
 * @param {number|null} discountedPrice - The discounted price (if available)
 * @returns {number} - The effective price to display
 */
export const getEffectivePrice = (product, discountedPrice = null) => {
  return discountedPrice !== null && discountedPrice < product.price ? discountedPrice : product.price;
};

/**
 * Checks if a product has a discount
 * @param {Object} product - The product object
 * @param {number|null} discountedPrice - The discounted price (if available)
 * @returns {boolean} - Whether the product has a discount
 */
export const hasDiscount = (product, discountedPrice = null) => {
  return discountedPrice !== null && discountedPrice < product.price;
};

/**
 * Calculates savings amount
 * @param {Object} product - The product object
 * @param {number|null} discountedPrice - The discounted price (if available)
 * @returns {number} - The savings amount
 */
export const calculateSavings = (product, discountedPrice = null) => {
  if (hasDiscount(product, discountedPrice)) {
    return product.price - discountedPrice;
  }
  return 0;
};

/**
 * Formats price for display
 * @param {number} price - The price to format
 * @returns {string} - Formatted price string
 */
export const formatPrice = (price) => {
  // Check if price is a valid number and not NaN
  if (typeof price === 'number' && !isNaN(price) && isFinite(price)) {
    return price.toFixed(2);
  }
  
  // Try to convert to number if it's a string
  const numPrice = Number(price);
  if (!isNaN(numPrice) && isFinite(numPrice)) {
    return numPrice.toFixed(2);
  }
  
  // Return default if invalid
  return '0.00';
}; 