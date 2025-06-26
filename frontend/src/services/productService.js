import axios from 'axios';

// Create axios instance with base URL
const api = axios.create({
  baseURL: '/api'
});

// Add JWT token to requests if available
api.interceptors.request.use(config => {
  const token = localStorage.getItem('token');
  if (token) {
    config.headers.Authorization = `Bearer ${token}`;
  }
  return config;
});

// Helper function to get active stores information
const getActiveStoresMap = async () => {
  try {
    const response = await api.get('/stores/info');
    const apiResponse = response.data;
    
    if (apiResponse.success) {
      const activeStoresMap = {};
      apiResponse.data.forEach(storeData => {
        const store = storeData.store;
        // Only include active stores in the map
        if (store.isActive) {
          activeStoresMap[store.storeID] = true;
        }
      });
      return activeStoresMap;
    }
  } catch (error) {
    console.warn('Could not fetch store information for filtering:', error);
  }
  return {};
};

// Helper function to filter products by active stores
const filterProductsByActiveStores = async (products) => {
  if (!products || products.length === 0) {
    return products;
  }

  try {
    const activeStoresMap = await getActiveStoresMap();
    
    // If we couldn't get store information, return all products to avoid breaking the app
    if (Object.keys(activeStoresMap).length === 0) {
      console.warn('No active stores found or failed to fetch store info, returning all products');
      return products;
    }

    // Filter products to only include those from active stores
    return products.filter(product => {
      return activeStoresMap[product.storeId];
    });
  } catch (error) {
    console.warn('Error filtering products by active stores:', error);
    return products; // Return all products if filtering fails
  }
};

export const productService = {
  // Search products by name
  searchProducts: async (query) => {
    try {
      const response = await api.get(`/products/search?query=${encodeURIComponent(query)}`);
      const apiResponse = response.data;
      
      if (apiResponse.success) {
        // Transform backend listings to frontend product format
        const products = apiResponse.data.map(listing => ({
          id: listing.listingId,
          title: listing.productName,
          price: listing.price,
          status: listing.active ? 'active' : 'inactive',
          images: listing.images || [],
          category: listing.category,
          shipping_cost: listing.shippingCost || 0,
          featured: listing.active,
          description: listing.productDescription,
          quantity: listing.quantityAvailable,
          storeId: listing.storeId,
          productId: listing.productId,
          purchaseType: listing.purchaseType,
          seller: {
            id: listing.storeId,
            name: listing.storeName || `Store ${listing.storeId}`,
            rating: listing.storeRating || 0
          },
          created_date: listing.createdDate || new Date().toISOString(),
          rating: listing.rating || 0,
          reviews: listing.reviews || []
        }));

        // Filter products to only include those from active stores
        return await filterProductsByActiveStores(products);
      } else {
        throw new Error('Failed to search products');
      }
    } catch (error) {
      if (error.response?.data?.error) {
        throw new Error('Failed to search products');
      }
      throw new Error('Failed to search products');
    }
  },

  // Get products by product ID
  getProductById: async (productId) => {
    try {
      const response = await api.get(`/products/id/${productId}`);
      const apiResponse = response.data;
      
      if (apiResponse.success) {
        // Transform backend listings to frontend product format
        const products = apiResponse.data.map(listing => ({
          id: listing.listingId,
          title: listing.productName,
          price: listing.price,
          status: listing.active ? 'active' : 'inactive',
          images: listing.images || [],
          category: listing.category,
          shipping_cost: listing.shippingCost || 0,
          featured: listing.active,
          description: listing.productDescription,
          quantity: listing.quantityAvailable,
          storeId: listing.storeId,
          productId: listing.productId,
          purchaseType: listing.purchaseType,
          seller: {
            id: listing.storeId,
            name: listing.storeName || `Store ${listing.storeId}`,
            rating: listing.storeRating || 0
          },
          created_date: listing.createdDate || new Date().toISOString(),
          rating: listing.rating || 0,
          reviews: listing.reviews || []
        }));
        
        // Filter products to only include those from active stores
        const filteredProducts = await filterProductsByActiveStores(products);
        
        // Return the first product if any exist, otherwise null
        return filteredProducts.length > 0 ? filteredProducts[0] : null;
      } else {
        throw new Error('Failed to get product');
      }
    } catch (error) {
      if (error.response?.data?.error) {
        throw new Error('Failed to get product');
      }
      throw new Error('Failed to get product');
    }
  },

  // Get all products from a specific store
  getStoreProducts: async (storeId) => {
    try {
      const response = await api.get(`/products/store/${storeId}`);
      const apiResponse = response.data;
      
      if (apiResponse.success) {
        // Transform backend listings to frontend product format
        const products = apiResponse.data.map(listing => ({
          id: listing.listingId,
          title: listing.productName,
          price: listing.price,
          status: listing.active ? 'active' : 'inactive',
          images: listing.images || [],
          category: listing.category,
          shipping_cost: listing.shippingCost || 0,
          featured: listing.active,
          description: listing.productDescription,
          quantity: listing.quantityAvailable,
          storeId: listing.storeId,
          productId: listing.productId,
          purchaseType: listing.purchaseType,
          seller: {
            id: listing.storeId,
            name: listing.storeName || `Store ${listing.storeId}`,
            rating: listing.storeRating || 0
          },
          created_date: listing.createdDate || new Date().toISOString(),
          rating: listing.rating || 0,
          reviews: listing.reviews || []
        }));

        // Filter products to only include those from active stores
        return await filterProductsByActiveStores(products);
      } else {
        throw new Error('Failed to get store products');
      }
    } catch (error) {
      if (error.response?.data?.error) {
        throw new Error('Failed to get store products');
      }
      throw new Error('Failed to get store products');
    }
  },

  // Search products in a specific store
  searchInStore: async (storeId, query) => {
    try {
      const response = await api.get(`/products/store/${storeId}/search?query=${encodeURIComponent(query)}`);
      const apiResponse = response.data;
      
      if (apiResponse.success) {
        // Transform backend listings to frontend product format
        const products = apiResponse.data.map(listing => ({
          id: listing.listingId,
          title: listing.productName,
          price: listing.price,
          status: listing.active ? 'active' : 'inactive',
          images: listing.images || [],
          category: listing.category,
          shipping_cost: listing.shippingCost || 0,
          featured: listing.active,
          description: listing.productDescription,
          quantity: listing.quantityAvailable,
          storeId: listing.storeId,
          productId: listing.productId,
          purchaseType: listing.purchaseType,
          seller: {
            id: listing.storeId,
            name: listing.storeName || `Store ${listing.storeId}`,
            rating: listing.storeRating || 0
          },
          created_date: listing.createdDate || new Date().toISOString(),
          rating: listing.rating || 0,
          reviews: listing.reviews || []
        }));

        // Filter products to only include those from active stores
        return await filterProductsByActiveStores(products);
      } else {
        throw new Error('Failed to search in store');
      }
    } catch (error) {
      if (error.response?.data?.error) {
        throw new Error('Failed to search in store');
      }
      throw new Error('Failed to search in store');
    }
  },

  // Get all products sorted by price
  getAllProductsSortedByPrice: async () => {
    try {
      const response = await api.get('/products/sorted/price');
      const apiResponse = response.data;
      
      if (apiResponse.success) {
        // Transform backend listings to frontend product format
        const products = apiResponse.data.map(listing => ({
          id: listing.listingId,
          title: listing.productName,
          price: listing.price,
          status: listing.active ? 'active' : 'inactive',
          images: listing.images || [],
          category: listing.category,
          shipping_cost: listing.shippingCost || 0,
          featured: listing.active,
          description: listing.productDescription,
          quantity: listing.quantityAvailable,
          storeId: listing.storeId,
          productId: listing.productId,
          purchaseType: listing.purchaseType,
          seller: {
            id: listing.storeId,
            name: listing.storeName || `Store ${listing.storeId}`,
            rating: listing.storeRating || 0
          },
          created_date: listing.createdDate || new Date().toISOString(),
          rating: listing.rating || 0,
          reviews: listing.reviews || []
        }));

        // Filter products to only include those from active stores
        return await filterProductsByActiveStores(products);
      } else {
        throw new Error('Failed to get sorted products');
      }
    } catch (error) {
      if (error.response?.data?.error) {
        throw new Error('Failed to get sorted products');
      }
      throw new Error('Failed to get sorted products');
    }
  },

  // Get a specific listing by ID
  getListing: async (listingId) => {
    try {
      const response = await api.get(`/products/listing/${listingId}`);
      const apiResponse = response.data;
      
      if (apiResponse.success) {
        const listing = apiResponse.data;
        
        const transformedProduct = {
          id: listing.listingId,
          title: listing.productName,
          price: listing.price,
          status: listing.active ? 'active' : 'inactive',
          images: listing.images || [],
          category: listing.category,
          shipping_cost: listing.shippingCost || 0,
          featured: listing.active,
          description: listing.productDescription,
          quantity: listing.quantityAvailable,
          storeId: listing.storeId,
          productId: listing.productId,
          purchaseType: listing.purchaseType,
          seller: {
            id: listing.storeId,
            name: listing.storeName || `Store ${listing.storeId}`,
            rating: listing.storeRating || 0
          },
          created_date: listing.createdDate || new Date().toISOString(),
          rating: listing.rating || 0,
          reviews: listing.reviews || []
        };
        
        return transformedProduct;
      } else {
        throw new Error('Failed to get listing');
      }
    } catch (error) {
      if (error.response?.data?.error) {
        throw new Error('Failed to get listing');
      }
      throw new Error('Failed to get listing');
    }
  },

  // Get all products (using stores/info endpoint)
  getAllProducts: async () => {
    try {
      const response = await api.get('/stores/info');
      const apiResponse = response.data;
      
      if (apiResponse.success) {
        const products = [];
        
        // Handle new API response format: List of {store, listings}
        apiResponse.data.forEach(storeData => {
          const store = storeData.store;
          const listings = storeData.listings;
          
          // Only include products from active stores
          if (store.isActive) {
            listings.forEach(listing => {
              products.push({
                id: listing.listingId,
                title: listing.productName,
                price: listing.price,
                status: listing.active ? 'active' : 'inactive',
                images: listing.images || [],
                category: listing.category,
                shipping_cost: listing.shippingCost || 0,
                featured: listing.active,
                description: listing.productDescription,
                quantity: listing.quantityAvailable,
                storeId: store.storeID,
                storeName: store.storeName,
                productId: listing.productId,
                purchaseType: listing.purchaseType,
                seller: {
                  id: store.storeID,
                  name: store.storeName,
                  rating: store.rating || 0
                },
                created_date: listing.createdDate || new Date().toISOString(),
                rating: listing.rating || 0,
                reviews: listing.reviews || []
              });
            });
          }
        });
        
        return products;
      } else {
        throw new Error('Failed to get all products');
      }
    } catch (error) {
      if (error.response?.data?.error) {
        throw new Error('Failed to get all products');
      }
      throw new Error('Failed to get all products');
    }
  },

  // Get all products with discounted prices applied - UPDATED VERSION
  getAllProductsWithDiscounts: async () => {
    try {
      const response = await api.get('/stores/info');
      const apiResponse = response.data;
      
      if (apiResponse.success) {
        const productsByStore = {};
        
        // Group products by store first
        apiResponse.data.forEach(storeData => {
          const store = storeData.store;
          const listings = storeData.listings;
          
          // Only include products from active stores
          if (store.isActive) {
            productsByStore[store.storeID] = listings.map(listing => ({
              id: listing.listingId,
              title: listing.productName,
              price: listing.price,
              status: listing.active ? 'active' : 'inactive',
              images: listing.images || [],
              category: listing.category,
              shipping_cost: listing.shippingCost || 0,
              featured: listing.active,
              description: listing.productDescription,
              quantity: listing.quantityAvailable,
              storeId: store.storeID,
              storeName: store.storeName,
              productId: listing.productId,
              purchaseType: listing.purchaseType,
              seller: {
                id: store.storeID,
                name: store.storeName,
                rating: store.rating || 0
              },
              created_date: listing.createdDate || new Date().toISOString(),
              rating: listing.rating || 0,
              reviews: listing.reviews || []
            }));
          }
        });
        
        // Use batch discount fetching
        return await productService.fetchBatchDiscounts(productsByStore);
      } else {
        throw new Error('Failed to get all products');
      }
    } catch (error) {
      if (error.response?.data?.error) {
        throw new Error('Failed to get all products');
      }
      throw new Error('Failed to get all products');
    }
  },

  // Get store products with discounted prices
  getStoreProductsWithDiscounts: async (storeId) => {
    try {
      const response = await api.get(`/products/store/${storeId}`);
      const apiResponse = response.data;
      
      if (apiResponse.success) {
        const products = [];
        
        for (const listing of apiResponse.data) {
          let discountedPrice = listing.price;
          
          // Try to get discounted price for this product
          try {
            const discountResponse = await api.get(`/stores/${storeId}/products/${listing.listingId}/discounted-price`);
            if (discountResponse.data.success && discountResponse.data.data !== undefined) {
              discountedPrice = discountResponse.data.data;
            }
          } catch (discountError) {
            // If discount API fails, use original price
            console.warn(`Could not get discount for ${listing.listingId}:`, discountError.message);
          }
          
          products.push({
            id: listing.listingId,
            title: listing.productName,
            price: listing.price,
            discountedPrice: discountedPrice,
            hasDiscount: discountedPrice < listing.price,
            status: listing.active ? 'active' : 'inactive',
            images: listing.images || [],
            category: listing.category,
            shipping_cost: listing.shippingCost || 0,
            featured: listing.active,
            description: listing.productDescription,
            quantity: listing.quantityAvailable,
            storeId: listing.storeId,
            productId: listing.productId,
            purchaseType: listing.purchaseType,
            seller: {
              id: listing.storeId,
              name: listing.storeName || `Store ${listing.storeId}`,
              rating: listing.storeRating || 0
            },
            created_date: listing.createdDate || new Date().toISOString(),
            rating: listing.rating || 0,
            reviews: listing.reviews || []
          });
        }
        
        return products;
      } else {
        throw new Error('Failed to get store products');
      }
    } catch (error) {
      if (error.response?.data?.error) {
        throw new Error('Failed to get store products');
      }
      throw new Error('Failed to get store products');
    }
  },

  // Get categories (extract from products)
  getCategories: async () => {
    try {
      const products = await productService.getAllProducts();
      const categories = [...new Set(products.map(product => product.category))].filter(Boolean);
      return categories;
    } catch (error) {
      console.error('Get categories error:', error);
      throw error;
    }
  },

  // Filter products by criteria (client-side filtering for now)
  filterProducts: async (filters = {}, sortBy = '-created_date', limit = 10) => {
    try {
      const allProducts = await productService.getAllProducts();
      let filteredProducts = [...allProducts];

      // Apply filters
      if (filters.category) {
        filteredProducts = filteredProducts.filter(p => 
          p.category.toLowerCase() === filters.category.toLowerCase()
        );
      }

      if (filters.minPrice !== undefined) {
        filteredProducts = filteredProducts.filter(p => p.price >= filters.minPrice);
      }

      if (filters.maxPrice !== undefined) {
        filteredProducts = filteredProducts.filter(p => p.price <= filters.maxPrice);
      }

      if (filters.featured !== undefined) {
        filteredProducts = filteredProducts.filter(p => p.featured === filters.featured);
      }

      // Apply sorting
      if (sortBy === 'price') {
        filteredProducts.sort((a, b) => a.price - b.price);
      } else if (sortBy === '-price') {
        filteredProducts.sort((a, b) => b.price - a.price);
      } else if (sortBy === 'title') {
        filteredProducts.sort((a, b) => a.title.localeCompare(b.title));
      }

      // Apply limit
      if (limit > 0) {
        filteredProducts = filteredProducts.slice(0, limit);
      }

      return filteredProducts;
    } catch (error) {
      console.error('Filter products error:', error);
      throw error;
    }
  },

  // Get products by category with discounted prices
  getProductsByCategory: async (category) => {
    try {
      const response = await api.get('/stores/info');
      const apiResponse = response.data;
      
      if (apiResponse.success) {
        const products = [];
        
        // Handle new API response format: List of {store, listings}
        for (const storeData of apiResponse.data) {
          const store = storeData.store;
          const listings = storeData.listings;
          
          // Only include products from active stores
          if (store.isActive) {
            for (const listing of listings) {
              // Filter by category (case-insensitive)
              if (listing.category && listing.category.toLowerCase() === category.toLowerCase()) {
                let discountedPrice = listing.price;
                
                // Try to get discounted price for this product
                try {
                  const discountResponse = await api.get(`/stores/${store.storeID}/products/${listing.listingId}/discounted-price`);
                  if (discountResponse.data.success && discountResponse.data.data !== undefined) {
                    discountedPrice = discountResponse.data.data;
                  }
                } catch (discountError) {
                  // If discount API fails, use original price
                  console.warn(`Could not get discount for ${listing.listingId}:`, discountError.message);
                }
                
                products.push({
                  id: listing.listingId,
                  title: listing.productName,
                  price: listing.price,
                  discountedPrice: discountedPrice,
                  hasDiscount: discountedPrice < listing.price,
                  status: listing.active ? 'active' : 'inactive',
                  images: listing.images || [],
                  category: listing.category,
                  shipping_cost: listing.shippingCost || 0,
                  featured: listing.active,
                  description: listing.productDescription,
                  quantity: listing.quantityAvailable,
                  storeId: store.storeID,
                  storeName: store.storeName,
                  productId: listing.productId,
                  purchaseType: listing.purchaseType,
                  seller: {
                    id: store.storeID,
                    name: store.storeName,
                    rating: store.rating || 0
                  },
                  created_date: listing.createdDate || new Date().toISOString(),
                  rating: listing.rating || 0,
                  reviews: listing.reviews || []
                });
              }
            }
          }
        }
        
        return products;
      } else {
        throw new Error('Failed to get products by category');
      }
    } catch (error) {
      if (error.response?.data?.error) {
        throw new Error('Failed to get products by category');
      }
      throw new Error('Failed to get products by category');
    }
  },

  // Get bag price without discounts
  getBagPrice: async (storeId, productsToQuantity) => {
    try {
      const response = await api.post(`/stores/${storeId}/bag/price`, productsToQuantity);
      if (response.data.success) {
        return response.data.data;
      }
      throw new Error('Failed to get bag price');
    } catch (error) {
      console.error('Error getting bag price:', error);
      throw error;
    }
  },

  // Get bag price with discounts
  getBagDiscountPrice: async (storeId, productsToQuantity) => {
    try {
      const response = await api.post(`/stores/${storeId}/bag/discounted-price`, productsToQuantity);
      if (response.data.success) {
        return response.data.data;
      }
      throw new Error('Failed to get bag discount price');
    } catch (error) {
      console.error('Error getting bag discount price:', error);
      throw error;
    }
  },

  // Batch fetch discounts for multiple products by store
  fetchBatchDiscounts: async (productsByStore) => {
    try {
      const discountPromises = Object.entries(productsByStore).map(async ([storeId, products]) => {
        try {
          // Create quantity map for this store's products
          const productsToQuantity = {};
          products.forEach(product => {
            productsToQuantity[product.id] = 1; // Default quantity of 1 for discount calculation
          });

          // Get both original and discounted prices
          const [originalPrice, discountedPrice] = await Promise.all([
            productService.getBagPrice(storeId, productsToQuantity),
            productService.getBagDiscountPrice(storeId, productsToQuantity)
          ]);

          // Calculate per-product discount ratios
          const totalOriginal = originalPrice;
          const totalDiscounted = discountedPrice;
          const discountRatio = totalOriginal > 0 ? totalDiscounted / totalOriginal : 1;

          // Apply discount ratio to individual products
          return products.map(product => ({
            ...product,
            discountedPrice: discountRatio < 1 ? product.price * discountRatio : product.price,
            hasDiscount: discountRatio < 1
          }));
        } catch (error) {
          console.warn(`Failed to get batch discounts for store ${storeId}:`, error);
          return products; // Return original products if batch fails
        }
      });

      const results = await Promise.all(discountPromises);
      return results.flat();
    } catch (error) {
      console.error('Error in batch discount fetch:', error);
      throw error;
    }
  }
};
