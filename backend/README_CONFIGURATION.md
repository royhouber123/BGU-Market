# BGU Market Configuration Guide

## Admin User Configuration

The BGU Market system now supports automatic admin user creation during startup through configuration files. This ensures that every time the system boots up, an admin user is available with the specified credentials.

## Configuration File

The admin user configuration is managed through the `application.properties` file located at:
```
backend/src/main/resources/application.properties
```

### Admin Configuration Properties

Add the following properties to configure the admin user:

```properties
# Admin User Configuration
# When the system boots, an admin user will be automatically created/updated with these credentials
admin.username=admin
admin.password=admin
```

### Default Configuration

The system is currently configured with:
- **Username**: `admin`
- **Password**: `admin`

### How It Works

1. **Startup Process**: When the application starts, the `StartupConfig` component runs automatically
2. **User Check**: The system checks if a user with the configured admin username exists
3. **User Creation/Update**:
   - If the user doesn't exist: Creates a new admin user with the configured credentials
   - If the user exists: Updates the password to match the configuration
4. **Logging**: All operations are logged for monitoring and debugging

### Configuration Components

The configuration system consists of several components:

1. **AdminConfig** (`market.application.AdminConfig`): Reads admin settings from application.properties
2. **StartupConfig** (`market.application.StartupConfig`): Handles system initialization on startup
3. **UserRepository** (`market.infrastructure.UserRepository`): Spring component for user management

### Changing Admin Credentials

To change the admin credentials:

1. Edit the `admin.username` and `admin.password` properties in `application.properties`
2. Restart the application
3. The system will automatically update the admin user with the new credentials

### Example Usage

After system startup, you can log in with:
- Username: `admin` (or your configured username)
- Password: `admin` (or your configured password)

### Security Notes

- The password is automatically hashed using secure password hashing (BCrypt)
- Never commit sensitive passwords to version control
- Consider using environment variables for production deployments

### Alternative Configuration Methods

While the system currently uses `application.properties`, it can be easily extended to support:
- Environment variables
- External `.env` files
- YAML configuration
- Command-line arguments

### Logging

The system provides detailed logging for all configuration operations:
- Startup initialization messages
- Admin user creation/update events
- Error handling for configuration issues

Check the application logs to monitor the configuration process. 