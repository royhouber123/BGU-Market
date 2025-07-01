# BGU Market Configuration Guide

## Overview

The BGU Market system provides a comprehensive configuration system that supports:
- **Automatic admin user creation** during startup
- **Demo data population** for testing and development
- **Profile-based configuration** (MySQL, H2)
- **External service integration** (Payment & Shipment)
- **Multiple initialization modes** (Demo vs. Production)

This guide covers all configuration options and how they work together.

## Configuration Files

### Primary Configuration
- **Main Config**: `backend/src/main/resources/application.properties`
- **Profile Configs**: 
  - `application-mysql.properties` - MySQL database configuration
  - `application-h2.properties` - H2 in-memory database configuration
- **Example Config**: `config.properties.example` - Template for custom configurations

### Demo Data Configuration
- **Demo Data File**: `demo-data.txt` - Default demo data definition
- **Test Data File**: `test-demo-data.txt` - Test-specific demo data

## Admin User Configuration

### Current Default Configuration

The system is currently configured with these default admin credentials:
- **Username**: `u1`
- **Password**: `password123`

### Configuration Properties

```properties
# Admin User Configuration
admin.username=u1
admin.password=password123
```

### How Admin Initialization Works

The system supports two initialization modes:

#### 1. Demo Mode (Default)
When `bgu.market.populate-demo-data=true`:
- System reads admin credentials from the demo data file
- If no admin is defined in demo data, falls back to `AdminConfig` properties
- **DemoDataPopulator** handles the complete system initialization
- Runs after application startup via `ApplicationReadyEvent`

#### 2. Production Mode
When `bgu.market.populate-demo-data=false`:
- **StartupConfig** directly initializes admin using `AdminConfig` properties
- Runs immediately on application startup via `CommandLineRunner`
- No demo data is populated

### Admin Creation Logic

1. **User Check**: System checks if admin user exists
2. **User Creation/Update**:
   - If user doesn't exist: Creates new admin with configured credentials
   - If user exists: Updates password to match configuration
3. **Password Security**: All passwords are automatically hashed with BCrypt
4. **Logging**: All operations are logged for monitoring

## Demo Data System

### Configuration Properties

```properties
# Demo data population (true/false)
bgu.market.populate-demo-data=true

# Demo data file location (relative to classpath)
bgu.market.demo-data-file=demo-data.txt
```

### Demo Data File Format

The `demo-data.txt` file supports the following entities:

```
# Admin (optional - if not specified, uses application.properties)
ADMIN u1,password123

# Users: username,password
USER u2,password123
USER u3,password123

# Stores: storeName,founderId
STORE Tech Paradise,u2
STORE Fashion Hub,u3

# Products: userName,storeName,productId,productName,category,description,quantity,price,purchaseType
PRODUCT u2,Tech Paradise,bamba_001,Bamba,Snacks,Peanut snack,20,30.0,REGULAR

# Store Managers: storeName,managerUsername,appointerUsername,permission1,permission2...
# Permissions: 0=view, 1=edit products, 2=edit policies, 3=bid approval
MANAGER Tech Paradise,u3,u2,1,2

# Store Owners: storeName,ownerUsername,appointerUsername
OWNER Tech Paradise,u4,u2
```

### Demo Data Population Process

1. **Admin Initialization**: Creates/updates admin user
2. **User Registration**: Registers all demo users
3. **Store Creation**: Creates stores and assigns founders
4. **Product Addition**: Adds products to stores
5. **Role Assignment**: Assigns managers and owners with permissions

## Database Configuration

### Active Profile Selection

```properties
# Choose your database profile
spring.profiles.active=mysql  # or h2
```

### MySQL Configuration (Production)
Defined in `application-mysql.properties`:
```properties
spring.datasource.url=jdbc:mysql://localhost:3306/bgumarket
spring.datasource.username=bgu
spring.datasource.password=changeme
spring.datasource.driver-class-name=com.mysql.cj.jdbc.Driver
```

### H2 Configuration (Development)
Defined in `application-h2.properties`:
```properties
spring.datasource.url=jdbc:h2:mem:bgumarket
spring.datasource.username=sa
spring.datasource.password=
spring.h2.console.enabled=true
spring.h2.console.path=/h2-console
```

## External Services Configuration

### Payment Service Configuration

```properties
# Payment service type: "external" or "mock"
external.services.payment.type=mock
external.payment.url=https://damp-lynna-wsep-1984852e.koyeb.app/
```

### Shipment Service Configuration

```properties
# Shipment service type: "external" or "mock"
external.services.shipment.type=mock
external.shipment.url=https://damp-lynna-wsep-1984852e.koyeb.app/
```

## JWT Configuration

```properties
# JWT security settings
jwt.secret=bguMarketSecretKey
jwt.expiration=86400000  # 24 hours in milliseconds
```

## Server Configuration

```properties
# Server settings
server.port=8080
server.tomcat.basedir=./tomcat-tmp
server.servlet.session.persistent=false
server.servlet.session.store-dir=./session-store
```

## JPA/Hibernate Configuration

```properties
# Database schema management
spring.jpa.hibernate.ddl-auto=update  # create, update, validate, none

# SQL logging (disable in production)
spring.jpa.show-sql=true
spring.jpa.properties.hibernate.format_sql=true

# Performance optimization
spring.jpa.open-in-view=false
```

## Configuration Components Architecture

### Core Components

1. **AdminConfig** (`market.application.AdminConfig`)
   - Reads admin settings from `application.properties`
   - Provides default values: `u1` and `password123`
   - Injectable Spring component with `@Value` annotations

2. **StartupConfig** (`market.application.StartupConfig`)
   - Implements `CommandLineRunner` for immediate startup initialization
   - Handles production mode admin initialization
   - Delegates to `DemoDataPopulator` in demo mode

3. **DemoDataPopulator** (`market.application.Init.DemoDataPopulator`)
   - Listens for `ApplicationReadyEvent` to ensure full system initialization
   - Parses demo data files using `DemoDataParser`
   - Makes HTTP requests to system APIs for realistic data population

4. **UserRepository Implementations**
   - **Persistent**: `UserRepositoryPersistance` (JPA-based, production)
   - **In-Memory**: `UserRepository` (testing and development)

## Usage Examples

### Basic Login (Default Configuration)

After system startup, log in with:
```
Username: u1
Password: password123
```

### Custom Admin Configuration

1. Edit `application.properties`:
```properties
admin.username=customadmin
admin.password=securepassword123
```

2. Restart application - admin will be updated automatically

### Switching to Production Mode

1. Disable demo data:
```properties
bgu.market.populate-demo-data=false
```

2. Configure production database:
```properties
spring.profiles.active=mysql
```

3. Set secure admin credentials:
```properties
admin.username=admin
admin.password=strongProductionPassword
```

### Custom Demo Data

1. Create your own demo data file:
```properties
bgu.market.demo-data-file=my-custom-data.txt
```

2. Place file in `src/main/resources/`

3. Follow the demo data format specification

## Environment-Specific Configuration

### Development Environment
```properties
spring.profiles.active=h2
bgu.market.populate-demo-data=true
spring.jpa.show-sql=true
external.services.payment.type=mock
external.services.shipment.type=mock
```

### Production Environment
```properties
spring.profiles.active=mysql
bgu.market.populate-demo-data=false
spring.jpa.show-sql=false
external.services.payment.type=external
external.services.shipment.type=external
admin.username=${ADMIN_USERNAME:admin}
admin.password=${ADMIN_PASSWORD:securePassword}
```

## Security Considerations

### Password Security
- All passwords are automatically hashed using **BCrypt**
- Never commit production passwords to version control
- Use environment variables for sensitive credentials
- Consider password complexity requirements

### Environment Variables
```bash
# Production deployment example
export ADMIN_USERNAME=secureadmin
export ADMIN_PASSWORD=verySecurePassword123!
export DB_PASSWORD=strongDatabasePassword
```

### Configuration Security
```properties
# Use environment variables in production
admin.username=${ADMIN_USERNAME:defaultadmin}
admin.password=${ADMIN_PASSWORD:defaultpassword}
spring.datasource.password=${DB_PASSWORD:}
```

## Logging and Monitoring

### Configuration Logging

The system provides detailed logging for all configuration operations:

```
[StartupConfig] Initializing system on startup...
[StartupConfig] Demo mode enabled - admin initialization delegated to DemoDataPopulator
[DemoDataPopulator] Starting demo data population from file: demo-data.txt
[DemoDataPopulator] Using ADMIN from demo data: u1
[StartupConfig] Admin user created with username: u1
[DemoDataPopulator] Created user: u2
[DemoDataPopulator] Demo data population completed successfully!
```

### Log Levels
- **INFO**: Normal operations, user creation/updates
- **DEBUG**: Detailed parameter information
- **ERROR**: Configuration failures, missing credentials
- **WARN**: Fallback scenarios, configuration conflicts

## Troubleshooting

### Common Issues

#### Admin User Not Created
**Problem**: Cannot log in with configured admin credentials
**Solution**: Check logs for startup errors, verify properties syntax

#### Demo Data Population Fails
**Problem**: Demo data population throws exceptions
**Solution**: 
- Ensure backend is running on correct port
- Check demo data file format
- Verify all referenced users exist before role assignments

#### Database Connection Issues
**Problem**: Application fails to start with database errors
**Solution**: 
- Verify database is running and accessible
- Check connection credentials
- Ensure database exists (for MySQL)

#### Configuration Not Loading
**Problem**: Changes to `application.properties` not taking effect
**Solution**: 
- Restart the application completely
- Check for typos in property names
- Verify file is in correct location

### Debug Mode

Enable debug logging for configuration components:
```properties
logging.level.market.application.StartupConfig=DEBUG
logging.level.market.application.AdminConfig=DEBUG
logging.level.market.application.Init.DemoDataPopulator=DEBUG
```

## Alternative Configuration Methods

### Environment-Based Configuration
```bash
# Set via environment variables
export SPRING_PROFILES_ACTIVE=mysql
export ADMIN_USERNAME=prodadmin
export ADMIN_PASSWORD=securepass123
export BGU_MARKET_POPULATE_DEMO_DATA=false
```

### YAML Configuration (Optional)
Create `application.yml` as alternative to properties:
```yaml
admin:
  username: u1
  password: password123

bgu:
  market:
    populate-demo-data: true
    demo-data-file: demo-data.txt

spring:
  profiles:
    active: mysql
```

### External Configuration Files
```bash
# Run with external config
java -jar bgu-market.jar --spring.config.location=file:./config/application.properties
```

## Configuration Validation

The system validates configuration on startup:

1. **Admin Credentials**: Ensures admin username and password are provided
2. **Demo Data File**: Validates file exists if demo mode is enabled
3. **Database Connection**: Verifies database connectivity
4. **External Services**: Tests external service URLs if configured

## Migration Guide

### Updating from Old Configuration

If you have an old configuration with:
```properties
admin.username=admin
admin.password=admin
```

Update to current format:
```properties
admin.username=u1
admin.password=password123
bgu.market.populate-demo-data=true
```

### Profile Migration

Old single configuration → New profile-based:
1. Move database-specific settings to profile files
2. Set active profile in main configuration
3. Test with both H2 and MySQL profiles

---

## Quick Reference

### Essential Properties
```properties
# Basic setup
admin.username=u1
admin.password=password123
server.port=8080
spring.profiles.active=mysql

# Demo data
bgu.market.populate-demo-data=true
bgu.market.demo-data-file=demo-data.txt

# External services
external.services.payment.type=mock
external.services.shipment.type=mock
```

### Component Locations
- **AdminConfig**: `market.application.AdminConfig`
- **StartupConfig**: `market.application.StartupConfig`
- **DemoDataPopulator**: `market.application.Init.DemoDataPopulator`
- **UserRepository**: `market.infrastructure.UserRepository`
- **UserRepositoryPersistance**: `market.infrastructure.PersistenceRepositories.UserRepositoryPersistance`

### Default Login Credentials
- **Username**: `u1`
- **Password**: `password123`

Check the application logs for detailed startup and configuration information. 