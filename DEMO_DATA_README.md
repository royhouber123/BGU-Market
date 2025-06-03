# Demo Data Population Script

This script populates your BGU Market application with demo data to make it ready for frontend testing and development.

## Prerequisites

1. **Server Running**: Make sure your BGU Market backend server is running on `http://localhost:8080`
2. **Clean Database**: For best results, start with a clean database state
3. **curl**: The script uses curl for API calls (should be available on most systems)

## How to Run

1. **Start the Backend Server** (if not already running):
   ```bash
   cd backend
   mvn spring-boot:run
   ```

2. **Run the Demo Data Script**:
   ```bash
   ./populate_demo_data.sh
   ```

## What Gets Created

### 👥 Demo Users
The script creates 5 regular users and 3 guest users:

**Regular Users** (username:password):
- `alice:password123` - Owner of TechHub
- `bob:password123` - Owner of BookWorld  
- `charlie:password123` - Owner of FashionForward
- `diana:password123` - Owner of HomeEssentials
- `eve:password123` - Owner of SportsZone

**Guest Users**:
- `guest_shopper1`
- `guest_shopper2` 
- `guest_browser`

### 🏪 Demo Stores
5 stores with different categories:
- **TechHub** (Electronics) - owned by alice
- **BookWorld** (Books) - owned by bob
- **FashionForward** (Clothing) - owned by charlie
- **HomeEssentials** (Home) - owned by diana
- **SportsZone** (Sports) - owned by eve

### 📦 Demo Products
20 products across 5 categories:

**Electronics (TechHub)**:
- iPhone 15 ($999.99)
- MacBook Pro ($2499.99)
- AirPods Pro ($249.99)
- iPad Air ($599.99)

**Books (BookWorld)**:
- The Great Gatsby ($12.99)
- Programming Pearls ($29.99)
- Dune ($15.99)
- Clean Code ($39.99)

**Clothing (FashionForward)**:
- Designer Jeans ($89.99)
- Silk Blouse ($129.99)
- Leather Jacket ($299.99)
- Summer Dress ($79.99)

**Home (HomeEssentials)**:
- Coffee Maker ($149.99)
- Vacuum Cleaner ($299.99)
- Bed Sheets Set ($89.99)
- Kitchen Knife Set ($199.99)

**Sports (SportsZone)**:
- Running Shoes ($159.99)
- Yoga Mat ($39.99)
- Basketball ($29.99)
- Fitness Tracker ($199.99)

## Testing the Frontend

After running the script, you can test the frontend with:

1. **Login** with any demo user (e.g., `alice:password123`)
2. **Browse stores** and view products
3. **Search for products** (try searching "iPhone" or "Book")
4. **Add products to cart**
5. **Execute purchases**
6. **Manage stores** (if logged in as store owner)

## Verification

The script includes verification steps that:
- ✅ Check all stores and products were created
- ✅ Test product search functionality  
- ✅ Test product sorting by price

## Troubleshooting

**Script fails with connection errors**:
- Make sure the backend server is running on `http://localhost:8080`
- Check if the server is fully started (look for "Started BguMarketApplication" in logs)

**Some operations fail**:
- This is normal - some business logic constraints may prevent certain operations
- The script continues on failures to populate as much data as possible

**Want to reset data**:
- Restart the backend server to reset to clean state
- Run the script again

## Customization

You can modify the script to:
- Change the `BASE_URL` if your server runs on a different port
- Add more users, stores, or products by editing the arrays
- Modify product prices, quantities, or descriptions
- Add different product categories

## Next Steps

Once the demo data is populated, your application is ready for:
- Frontend development and testing
- User acceptance testing
- Demo presentations
- Integration testing

The data provides a realistic marketplace scenario with multiple stores, diverse products, and various user types to test all application features. 