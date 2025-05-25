/**
 * Utility functions for the BGU Market application
 */

/**
 * Creates a URL for navigating to a page
 * @param {string} pageName - Name of the page
 * @returns {string} URL for the page
 */
export const createPageUrl = (page) => {
  const pageRoutes = {
    Home: '/',
    Dashboard: '/',
    Product: '/product',
    Cart: '/cart',
    Login: '/login',
    Register: '/register',
    SearchResults: '/search',
    Watchlist: '/watchlist',
    Checkout: '/checkout',
    OrderConfirmation: '/order-confirmation',
  };

  return pageRoutes[page] || '/';
};

/**
 * Formats a price with currency symbol
 * @param {number} price - Price to format
 * @param {string} currency - Currency code (default: USD)
 * @returns {string} Formatted price
 */
export const formatPrice = (price, currency = 'USD') => {
  if (price === undefined || price === null) return '';
  
  const formatter = new Intl.NumberFormat('en-US', {
    style: 'currency',
    currency,
    minimumFractionDigits: 2,
  });

  return formatter.format(price);
};

/**
 * Truncates text to a specific length
 * @param {string} text - Text to truncate
 * @param {number} length - Maximum length
 * @returns {string} Truncated text
 */
export const truncateText = (text, length = 50) => {
  if (!text) return '';
  if (text.length <= length) return text;
  
  return text.substring(0, length) + '...';
};

/**
 * Format date into a readable string
 * @param {string} dateString - ISO date string
 * @returns {string} Formatted date
 */
export const formatDate = (dateString) => {
  if (!dateString) return '';
  
  const date = new Date(dateString);
  return date.toLocaleDateString('en-US', {
    year: 'numeric', 
    month: 'long', 
    day: 'numeric'
  });
};
