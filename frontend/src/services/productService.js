import axios from 'axios';

// MOCK DATA IMPLEMENTATION
// TODO: Uncomment the real API implementation when backend is ready

// Mock products data
const mockProducts = [
  { 
    id: "101", 
    title: 'Wireless Headphones', 
    price: 99.99, 
    status: 'active',
    images: ["https://images.unsplash.com/photo-1583394838336-acd977736f90?ixlib=rb-4.0.3&ixid=MnwxMjA3fDB8MHxwaG90by1wYWdlfHx8fGVufDB8fHx8&auto=format&fit=crop&w=640&q=80"],
    category: "Electronics",
    shipping_cost: 0,
    featured: true,
    description: "High-quality wireless headphones with noise cancellation",
    seller: {
      id: "1",
      name: "Electronics Hub",
      rating: 4.8
    },
    created_date: "2025-05-10T12:00:00Z",
    rating: 4.5,
    reviews: [
      { id: "r1", user: "John D.", rating: 5, comment: "Great headphones!", date: "2025-05-12T08:30:00Z" },
      { id: "r2", user: "Sarah M.", rating: 4, comment: "Good sound quality", date: "2025-05-15T14:20:00Z" }
    ]
  },
  { 
    id: "102", 
    title: 'Smartphone Case', 
    price: 19.99, 
    status: 'active',
    images: ["https://images.unsplash.com/photo-1541877407341-d5289f84bf95?ixlib=rb-4.0.3&ixid=MnwxMjA3fDB8MHxwaG90by1wYWdlfHx8fGVufDB8fHx8&auto=format&fit=crop&w=640&q=80"],
    category: "Electronics",
    shipping_cost: 0,
    featured: true,
    description: "Durable smartphone case with drop protection",
    seller: {
      id: "1",
      name: "Electronics Hub",
      rating: 4.8
    },
    created_date: "2025-05-12T10:00:00Z",
    rating: 4.2,
    reviews: [
      { id: "r3", user: "Mike T.", rating: 4, comment: "Good quality", date: "2025-05-14T11:30:00Z" }
    ]
  },
  { 
    id: "103", 
    title: 'Laptop Sleeve', 
    price: 29.99, 
    status: 'pending',
    images: ["https://images.unsplash.com/photo-1576053359219-5f4b925f8ba1?ixlib=rb-4.0.3&ixid=MnwxMjA3fDB8MHxwaG90by1wYWdlfHx8fGVufDB8fHx8&auto=format&fit=crop&w=640&q=80"],
    category: "Electronics",
    shipping_cost: 0,
    featured: false,
    description: "Stylish and protective laptop sleeve",
    seller: {
      id: "1",
      name: "Electronics Hub",
      rating: 4.8
    },
    created_date: "2025-05-18T09:00:00Z",
    rating: 0,
    reviews: []
  },
  { 
    id: "104", 
    title: 'Bluetooth Speaker', 
    price: 79.99, 
    status: 'active',
    images: ["https://images.unsplash.com/photo-1608043152269-423dbba4e7e1?ixlib=rb-4.0.3&ixid=MnwxMjA3fDB8MHxwaG90by1wYWdlfHx8fGVufDB8fHx8&auto=format&fit=crop&w=640&q=80"],
    category: "Electronics",
    shipping_cost: 0,
    featured: true,
    description: "Portable Bluetooth speaker with 20-hour battery life",
    seller: {
      id: "1",
      name: "Electronics Hub",
      rating: 4.8
    },
    created_date: "2025-05-05T15:30:00Z",
    rating: 4.7,
    reviews: [
      { id: "r4", user: "Lisa K.", rating: 5, comment: "Excellent sound!", date: "2025-05-10T16:45:00Z" },
      { id: "r5", user: "David W.", rating: 4.5, comment: "Great for outdoor use", date: "2025-05-12T20:00:00Z" }
    ]
  },
  { 
    id: "105", 
    title: 'Dress Shirt', 
    price: 35.99, 
    status: 'active',
    images: ["https://images.unsplash.com/photo-1603252109303-2751441dd157?ixlib=rb-4.0.3&ixid=MnwxMjA3fDB8MHxwaG90by1wYWdlfHx8fGVufDB8fHx8&auto=format&fit=crop&w=640&q=80"],
    category: "Fashion",
    shipping_cost: 5,
    featured: false,
    description: "Elegant dress shirt for formal occasions",
    seller: {
      id: "2",
      name: "Fashion Outlet",
      rating: 4.5
    },
    created_date: "2025-05-08T11:20:00Z",
    rating: 4.0,
    reviews: [
      { id: "r6", user: "Robert J.", rating: 4, comment: "Good quality material", date: "2025-05-15T09:30:00Z" }
    ]
  },
  { 
    id: "106", 
    title: 'Python Programming Book', 
    price: 49.99, 
    status: 'active',
    images: ["https://images.unsplash.com/photo-1550399105-c4db5fb85c18?ixlib=rb-4.0.3&ixid=MnwxMjA3fDB8MHxwaG90by1wYWdlfHx8fGVufDB8fHx8&auto=format&fit=crop&w=640&q=80"],
    category: "Books",
    shipping_cost: 3,
    featured: true,
    description: "Comprehensive Python programming guide for beginners and intermediate users",
    seller: {
      id: "3",
      name: "Book Haven",
      rating: 4.9
    },
    created_date: "2025-05-01T08:00:00Z",
    rating: 4.9,
    reviews: [
      { id: "r7", user: "Jennifer L.", rating: 5, comment: "Best Python book ever!", date: "2025-05-10T10:15:00Z" },
      { id: "r8", user: "Thomas B.", rating: 4.8, comment: "Very thorough explanations", date: "2025-05-14T14:00:00Z" }
    ]
  }
];

// Create axios instance with base URL (for future use)
const api = axios.create({
  baseURL: '/api'
});

// Add JWT token to requests if available (for future use)
api.interceptors.request.use(config => {
  const token = localStorage.getItem('token');
  if (token) {
    config.headers.Authorization = `Bearer ${token}`;
  }
  return config;
});

/* 
 * MOCK DATA SERVICE IMPLEMENTATION
 * TODO: Replace mock implementations with actual API calls when backend is ready
 * Each function includes commented code for the real implementation
 */
const productService = {
  // Get all products
  getAllProducts: async () => {
    try {
      // MOCK DATA: Return all mock products
      // TODO: Uncomment when backend is ready
      // const response = await api.get('/products');
      // return response.data;
      
      // Simulate network delay
      await new Promise(resolve => setTimeout(resolve, 500));
      return [...mockProducts];
    } catch (error) {
      console.error('Get all products error:', error);
      throw error;
    }
  },

  // Get product by ID
  getProductById: async (id) => {
    try {
      // MOCK DATA: Find product by ID
      // TODO: Uncomment when backend is ready
      // const response = await api.get(`/products/${id}`);
      // return response.data;
      
      // Simulate network delay
      await new Promise(resolve => setTimeout(resolve, 300));
      const product = mockProducts.find(p => p.id === id);
      if (!product) {
        throw new Error('Product not found');
      }
      return product;
    } catch (error) {
      console.error(`Get product ${id} error:`, error);
      throw error;
    }
  },

  // Filter products by criteria
  filterProducts: async (filters = {}, sortBy = '-created_date', limit = 10) => {
    try {
      // MOCK DATA: Filter products by criteria
      // TODO: Uncomment when backend is ready
      // const queryParams = new URLSearchParams();
      // Object.entries(filters).forEach(([key, value]) => {
      //   if (value !== undefined && value !== null && value !== '') {
      //     queryParams.append(key, value);
      //   }
      // });
      // if (sortBy) queryParams.append('sort', sortBy);
      // if (limit) queryParams.append('limit', limit);
      // const response = await api.get(`/products?${queryParams.toString()}`);
      // return response.data;
      
      // Simulate network delay
      await new Promise(resolve => setTimeout(resolve, 400));

      // Filter products based on criteria
      let filteredProducts = [...mockProducts];
      
      Object.entries(filters).forEach(([key, value]) => {
        if (value !== undefined && value !== null && value !== '') {
          if (key === 'featured') {
            filteredProducts = filteredProducts.filter(p => p[key] === value);
          } else if (key === 'category') {
            filteredProducts = filteredProducts.filter(p => p[key].toLowerCase() === value.toLowerCase());
          } else if (key === 'minPrice') {
            filteredProducts = filteredProducts.filter(p => p.price >= value);
          } else if (key === 'maxPrice') {
            filteredProducts = filteredProducts.filter(p => p.price <= value);
          } else {
            filteredProducts = filteredProducts.filter(p => p[key] === value);
          }
        }
      });
      
      // Sort products
      if (sortBy) {
        const isDescending = sortBy.startsWith('-');
        const sortField = isDescending ? sortBy.substring(1) : sortBy;
        
        filteredProducts.sort((a, b) => {
          if (!a[sortField] || !b[sortField]) return 0;
          
          if (typeof a[sortField] === 'string') {
            return isDescending 
              ? b[sortField].localeCompare(a[sortField]) 
              : a[sortField].localeCompare(b[sortField]);
          } else {
            return isDescending 
              ? b[sortField] - a[sortField] 
              : a[sortField] - b[sortField];
          }
        });
      }
      
      // Apply limit
      if (limit && limit > 0) {
        filteredProducts = filteredProducts.slice(0, limit);
      }
      
      return filteredProducts;
    } catch (error) {
      console.error('Filter products error:', error);
      throw error;
    }
  },

  // Search products by query
  searchProducts: async (query) => {
    try {
      // MOCK DATA: Search products by query
      // TODO: Uncomment when backend is ready
      // const response = await api.get(`/products/search?q=${encodeURIComponent(query)}`);
      // return response.data;
      
      // Simulate network delay
      await new Promise(resolve => setTimeout(resolve, 400));

      if (!query) return [];
      
      const searchTerms = query.toLowerCase().split(' ');
      return mockProducts.filter(product => {
        const title = product.title.toLowerCase();
        const description = product.description.toLowerCase();
        const category = product.category.toLowerCase();
        
        return searchTerms.some(term => 
          title.includes(term) || 
          description.includes(term) || 
          category.includes(term)
        );
      });
    } catch (error) {
      console.error('Search products error:', error);
      throw error;
    }
  },

  // Get products by category
  getProductsByCategory: async (category) => {
    try {
      // MOCK DATA: Get products by category
      // TODO: Uncomment when backend is ready
      // const response = await api.get(`/products/category/${encodeURIComponent(category)}`);
      // return response.data;
      
      // Simulate network delay
      await new Promise(resolve => setTimeout(resolve, 400));

      return mockProducts.filter(p => 
        p.category.toLowerCase() === category.toLowerCase()
      );
    } catch (error) {
      console.error(`Get products by category ${category} error:`, error);
      throw error;
    }
  },

  // Get featured products
  getFeaturedProducts: async (limit = 4) => {
    try {
      // MOCK DATA: Get featured products
      // TODO: Uncomment when backend is ready
      // const response = await api.get(`/products/featured?limit=${limit}`);
      // return response.data;
      
      // Simulate network delay
      await new Promise(resolve => setTimeout(resolve, 300));

      const featured = mockProducts.filter(p => p.featured);
      return featured.slice(0, limit);
    } catch (error) {
      console.error('Get featured products error:', error);
      throw error;
    }
  },

  // Create a new product (requires authentication)
  createProduct: async (productData) => {
    try {
      // MOCK DATA: Create a new product
      // TODO: Uncomment when backend is ready
      // const response = await api.post('/products', productData);
      // return response.data;
      
      // Simulate network delay
      await new Promise(resolve => setTimeout(resolve, 800));

      // Generate unique ID
      const newId = String(Math.max(...mockProducts.map(p => parseInt(p.id))) + 1);
      
      const newProduct = {
        id: newId,
        ...productData,
        created_date: new Date().toISOString(),
        rating: 0,
        reviews: []
      };
      
      // In a real app, we would persist this. In our mock, we'll just return it
      // mockProducts.push(newProduct); // Don't actually modify the mock data as it would be reset on page refresh
      
      return newProduct;
    } catch (error) {
      console.error('Create product error:', error);
      throw error;
    }
  },

  // Update a product (requires authentication and ownership)
  updateProduct: async (id, productData) => {
    try {
      // MOCK DATA: Update a product
      // TODO: Uncomment when backend is ready
      // const response = await api.put(`/products/${id}`, productData);
      // return response.data;
      
      // Simulate network delay
      await new Promise(resolve => setTimeout(resolve, 600));

      const productIndex = mockProducts.findIndex(p => p.id === id);
      if (productIndex === -1) {
        throw new Error('Product not found');
      }
      
      // In a real app, we would update the persisted product
      // For our mock, we'll just return a merged object
      const updatedProduct = {
        ...mockProducts[productIndex],
        ...productData
      };
      
      return updatedProduct;
    } catch (error) {
      console.error(`Update product ${id} error:`, error);
      throw error;
    }
  },

  // Delete a product (requires authentication and ownership)
  deleteProduct: async (id) => {
    try {
      // MOCK DATA: Delete a product
      // TODO: Uncomment when backend is ready
      // const response = await api.delete(`/products/${id}`);
      // return response.data;
      
      // Simulate network delay
      await new Promise(resolve => setTimeout(resolve, 500));

      const productIndex = mockProducts.findIndex(p => p.id === id);
      if (productIndex === -1) {
        throw new Error('Product not found');
      }
      
      // In a real app, we would delete the product from persistence
      // For our mock, we'll just return a success message
      return { success: true, message: 'Product deleted successfully' };
    } catch (error) {
      console.error(`Delete product ${id} error:`, error);
      throw error;
    }
  }
};

export default productService;
