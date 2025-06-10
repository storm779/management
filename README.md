# Visakh Refinery Portal

A comprehensive web application replicating the visual layout and functionality of the Visakh Refinery portal with a focus on the "What's New" section and Message Board system. Built using Spring Boot, Thymeleaf, and Bootstrap with MariaDB database.

## 🌟 Features

### What's New Section
- **Dashboard Widget**: Small, card-style widget with green panel heading and white body
- **Full CRUD Operations**: Add, Edit, Delete, and View announcements
- **DataTables Integration**: Professional table display with search, sorting, and pagination
- **Priority System**: Announcements can be prioritized for display order
- **URL Integration**: Optional links for announcements that open in new tabs
- **Bulk Operations**: Delete multiple items at once (Admin only)
- **Responsive Design**: Works seamlessly on desktop and mobile devices

### Message Board System
- **Comprehensive CRUD**: Complete Create, Read, Update, Delete operations
- **Multi-language Support**: English and Hindi content fields
- **DataTables Integration**: Advanced table functionality with built-in search and pagination
- **Priority & Display Control**: Priority ordering and display sequence management
- **Color Customization**: Custom text and background colors for messages
- **Bulk Operations**: Multiple message management capabilities
- **CSV Data Migration**: Import existing data from CSV files

### Technical Features
- **Spring Boot 3.2.0**: Modern Java framework with auto-configuration
- **Spring Data JPA**: Repository pattern with custom queries
- **Thymeleaf Templates**: Server-side rendering with reusable fragments
- **Bootstrap 5**: Modern, responsive UI components
- **jQuery DataTables**: Professional table display with sorting, searching, and pagination
- **MariaDB Database**: Robust relational database with proper indexing
- **Spring Security**: Role-based access control (Admin/Public)
- **Validation**: Server-side and client-side form validation
- **Error Handling**: Comprehensive error handling with user-friendly messages

## 🏗️ Database Architecture & Entity Mapping

### Database Schema Overview

The portal uses a comprehensive database schema with main tables designed for scalability:

```sql
refweb_portal/
├── vrp_whatsnew        # What's New announcements
├── vrp_scrollmsg       # Message Board (scrolling messages)
├── vrp_users          # User authentication
└── Additional tables for future enhancements
```

### Entity-to-Table Mapping

#### 1. What's New System (`vrp_whatsnew`)

**Entity**: `WhatsNew.java`
```java
@Entity
@Table(name = "vrp_whatsnew")
public class WhatsNew {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String title;           // Announcement title
    private String description;     // Detailed description
    private String url;            // Optional external link
    private LocalDate validFrom;   // Start date
    private LocalDate validTo;     // End date (optional)
    private Boolean enabled;       // Active status
    private Integer priority;      // Display priority
    private Integer displayOrder;  // Display sequence
    private LocalDate createdDate;
    private LocalDate modifiedDate;
}
```

#### 2. Message Board System (`vrp_scrollmsg`)

**Entity**: `MessageBoard.java`
```java
@Entity
@Table(name = "vrp_scrollmsg")
public class MessageBoard {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String header;          // Message header
    private String message;         // Message content
    private String messageHindi;    // Hindi message content
    private String headerHindi;     // Hindi header
    private LocalDate validFrom;    // Start date
    private LocalDate validTo;      // End date (optional)
    private Boolean enabled;        // Active status
    private Integer priority;       // Display priority
    private Integer displayOrder;   // Display sequence
    private String speed;          // Scroll speed
    private String color;          // Text color
    private String backgroundColor; // Background color
    private String createdBy;      // Creator
    private LocalDate createdDate;
    private LocalDate modifiedDate;
    private LocalDateTime dateTimeStamp;
}
```

#### 3. User Authentication (`vrp_users`)

**Entity**: `User.java`
```java
@Entity
@Table(name = "vrp_users")
public class User implements UserDetails {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String username;       // Unique username
    private String email;         // Email address
    private String password;      // Encrypted password
    private String fullName;      // Display name
    private Role role;           // ADMIN/USER
    private boolean enabled;     // Account status
    private LocalDateTime createdDate;
    private LocalDateTime lastLogin;
}
```

## 🚀 Quick Start

### Prerequisites
- Java 17 or higher
- Maven 3.6+
- MariaDB 10.5+

### Installation Steps

1. **Clone the repository**
```bash
git clone <repository-url>
cd management
```

2. **Setup MariaDB Database**
```bash
# Create database
mysql -u root -p
CREATE DATABASE refweb_portal;
CREATE USER 'storm'@'localhost' IDENTIFIED BY 'your_password';
GRANT ALL PRIVILEGES ON refweb_portal.* TO 'storm'@'localhost';
FLUSH PRIVILEGES;

# Run setup script
chmod +x setup_db.sh
./setup_db.sh
```

3. **Configure Database Connection**
Update `src/main/resources/application.properties`:
```properties
spring.datasource.url=jdbc:mariadb://localhost/refweb_portal
spring.datasource.username=storm
spring.datasource.password=your_password
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=false
```

4. **Build and Run**
```bash
# Build the application
mvn clean compile

# Run the application
mvn spring-boot:run

# Or use the convenience script
chmod +x run.sh
./run.sh
```

5. **Access the Application**
- **URL**: http://localhost:8080
- **Default Admin**: Create account via registration
- **Port**: 8080 (configurable in application.properties)

## 📱 User Interface & Navigation

### Public Access
- **Dashboard**: Overview with widgets for What's New and Message Board
- **What's New List**: View all active announcements
- **Message Board List**: View all active messages
- **Individual Item Views**: Detailed view of announcements and messages

### Admin Features (Requires Login)
- **Full CRUD Operations**: Create, edit, and delete items
- **Bulk Operations**: Manage multiple items simultaneously
- **User Management**: Account creation and management
- **Data Migration**: CSV import functionality

### DataTables Features
Both What's New and Message Board pages include:
- **Search**: Real-time search across all columns
- **Sorting**: Click column headers to sort
- **Pagination**: Configurable page sizes (10, 25, 50, 100, All)
- **Responsive**: Mobile-friendly table display
- **Info Display**: Shows current page info and total entries

## 🔧 Configuration

### Database Configuration
Edit `src/main/resources/application.properties`:
```properties
# Database Configuration
spring.datasource.url=jdbc:mariadb://localhost/refweb_portal
spring.datasource.username=your_username
spring.datasource.password=your_password
spring.datasource.driver-class-name=org.mariadb.jdbc.Driver

# JPA Configuration
spring.jpa.hibernate.ddl-auto=update
spring.jpa.database-platform=org.hibernate.dialect.MariaDBDialect
spring.jpa.show-sql=false

# Server Configuration
server.port=8080

# Thymeleaf Configuration
spring.thymeleaf.cache=false
spring.thymeleaf.mode=HTML

# Security Configuration
spring.security.user.name=admin
spring.security.user.password=admin123
```

### Maven Dependencies
Key dependencies in `pom.xml`:
```xml
<dependencies>
    <!-- Spring Boot Starters -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-web</artifactId>
    </dependency>
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-data-jpa</artifactId>
    </dependency>
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-thymeleaf</artifactId>
    </dependency>
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-security</artifactId>
    </dependency>
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-validation</artifactId>
    </dependency>
    
    <!-- Database -->
    <dependency>
        <groupId>org.mariadb.jdbc</groupId>
        <artifactId>mariadb-java-client</artifactId>
    </dependency>
    
    <!-- Development Tools -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-devtools</artifactId>
        <scope>runtime</scope>
    </dependency>
</dependencies>
```

## 🎨 UI Framework & Styling

### Frontend Technologies
- **Bootstrap 5.3.0**: Modern responsive framework
- **Bootstrap Icons 1.11.1**: Icon library
- **jQuery 3.7.0**: JavaScript library
- **DataTables 2.3.2**: Advanced table functionality
- **Custom CSS**: Enhanced styling and theming

### Key UI Components
- **Navigation**: Fixed header with responsive menu
- **Cards**: Clean card-based layout for content
- **Tables**: DataTables integration for data display
- **Forms**: Bootstrap-styled forms with validation
- **Modals**: Bootstrap modals for confirmations
- **Alerts**: Success/error message display

### Color Scheme
- **Primary Green**: #198754 (Success/Active)
- **Secondary Blue**: #0d6efd (Info/Actions)
- **Warning Orange**: #ffc107 (Warnings)
- **Danger Red**: #dc3545 (Errors/Delete)
- **Light Gray**: #f8f9fa (Backgrounds)

## 🔒 Security & Access Control

### Authentication
- **Spring Security**: Role-based access control
- **User Registration**: Account creation system
- **Password Encryption**: BCrypt password hashing
- **Session Management**: Secure session handling

### Authorization Levels
- **Public Access**: View announcements and messages
- **Admin Access**: Full CRUD operations and user management
- **Role-Based Views**: Different UI based on user role

### Security Features
- **CSRF Protection**: Cross-site request forgery prevention
- **Input Validation**: Server-side and client-side validation
- **SQL Injection Prevention**: Parameterized queries
- **XSS Protection**: Output encoding and sanitization

## 📊 Data Management

### CSV Data Migration
Import existing data from CSV files:

1. **Prepare CSV File**: Place `vrp_scrollmsg.csv` in project root
2. **CSV Format**: 
   ```
   MSGID,MESSAGE,VALIDUPTO,PRIORITY,MSGBY,DTSTAMP,MSG_HEADER,VALIDFROM,ENABLED,MESSAGE_HINDI,MSG_HEADER_HINDI
   ```
3. **Run Migration**: Use the data migration endpoint (Admin only)

### Database Operations
- **Bulk Insert**: Efficient batch operations
- **Data Validation**: Comprehensive validation rules
- **Error Handling**: Detailed error reporting
- **Transaction Management**: ACID compliance

## 🧪 Testing & Development

### Development Mode
```bash
# Enable development features
spring.profiles.active=dev

# Enable SQL logging
spring.jpa.show-sql=true

# Disable template caching
spring.thymeleaf.cache=false
```

### Testing the Application
1. **Start Application**: `mvn spring-boot:run`
2. **Check Health**: Visit http://localhost:8080
3. **Test Features**:
   - Create new announcements/messages
   - Test DataTables functionality (search, sort, pagination)
   - Verify responsive design on different screen sizes
   - Test bulk operations
   - Verify security restrictions

### Development Tools
- **Spring Boot DevTools**: Automatic restart on code changes
- **H2 Console**: Database inspection (if H2 is configured)
- **Actuator**: Application monitoring endpoints
- **Logging**: Configurable logging levels

## 🔄 Recent Updates & Changes

### DataTables Integration (Latest)
- **What's New Page**: Replaced basic pagination with jQuery DataTables
- **Message Board Page**: Added DataTables with search and sorting
- **Features Added**:
  - Real-time search across all columns
  - Column sorting (except checkbox and actions)
  - Responsive design with mobile breakpoints
  - Configurable page sizes (10, 25, 50, 100, All)
  - Professional styling with Bootstrap 5 theme

### Advanced Filters Removal (Latest)
- **Removed Advanced Filter Sections**: Simplified both What's New and Message Board pages
- **Maintained Core Features**: All CRUD operations and DataTables functionality preserved
- **Cleaned JavaScript**: Removed filter-specific functions while maintaining other features
- **Performance Improvement**: Reduced page complexity and load times

### Status Features Removal
- **Removed Status Columns**: No longer display enabled/disabled status in tables
- **Removed Toggle Actions**: Simplified bulk operations to focus on delete only
- **Simplified Forms**: Removed enabled checkboxes from add/edit forms
- **Streamlined UI**: Cleaner table layout and form design

### UI Enhancements
- **Reduced Card Padding**: More compact statistics display
- **Enhanced Navigation**: Improved header and footer styling
- **Mobile Optimization**: Better responsive design
- **Color Consistency**: Unified color scheme throughout

## 🚨 Troubleshooting

### Common Issues

#### Database Connection Issues
```bash
# Check MariaDB service
sudo systemctl status mariadb

# Test connection
mysql -u storm -p refweb_portal

# Verify user permissions
SHOW GRANTS FOR 'storm'@'localhost';
```

#### Port Already in Use
```bash
# Check what's using port 8080
lsof -i :8080

# Kill the process
kill -9 <PID>

# Or change port in application.properties
server.port=8081
```

#### Maven Build Issues
```bash
# Clean and rebuild
mvn clean compile

# Skip tests if needed
mvn clean compile -DskipTests

# Check Java version
java --version
mvn --version
```

#### DataTables Not Loading
- Check browser console for JavaScript errors
- Verify jQuery and DataTables CDN links are accessible
- Ensure table HTML structure is correct
- Check for JavaScript conflicts

### Performance Optimization
- **Database Indexing**: Ensure proper indexes on frequently queried columns
- **Connection Pooling**: Configure HikariCP for optimal performance
- **Caching**: Enable appropriate caching strategies
- **Static Resources**: Use CDN for Bootstrap and jQuery

## 📋 API Endpoints

### What's New Endpoints
- `GET /whatsnew/list` - List all announcements
- `GET /whatsnew/view/{id}` - View specific announcement
- `POST /whatsnew/add` - Create new announcement (Admin)
- `POST /whatsnew/edit/{id}` - Update announcement (Admin)
- `POST /whatsnew/delete/{id}` - Delete announcement (Admin)
- `POST /whatsnew/bulk-delete` - Delete multiple announcements (Admin)

### Message Board Endpoints
- `GET /messageboard/list` - List all messages
- `GET /messageboard/view/{id}` - View specific message
- `POST /messageboard/add` - Create new message (Admin)
- `POST /messageboard/edit/{id}` - Update message (Admin)
- `POST /messageboard/delete/{id}` - Delete message (Admin)
- `POST /messageboard/bulk-delete` - Delete multiple messages (Admin)

### Authentication Endpoints
- `GET /login` - Login page
- `POST /perform_login` - Process login
- `GET /register` - Registration page
- `POST /register` - Process registration
- `POST /logout` - Logout user

### Utility Endpoints
- `GET /api/check-username` - Check username availability
- `GET /api/check-email` - Check email availability
- `GET /admin/migration` - Data migration page (Admin)
- `POST /admin/migration/csv` - Import CSV data (Admin)

## 🎯 Future Enhancements

### Planned Features
- **Advanced User Roles**: More granular permission system
- **File Upload**: Support for image and document attachments
- **Email Notifications**: Automatic notifications for new announcements
- **Dashboard Analytics**: Usage statistics and reporting
- **API Integration**: REST API for external systems
- **Mobile App**: Native mobile application
- **Audit Trail**: Complete change history tracking

### Technical Improvements
- **Caching**: Redis integration for improved performance
- **Search**: Full-text search capabilities
- **Backup**: Automated database backup system
- **Monitoring**: Application health monitoring
- **Testing**: Comprehensive test suite
- **Documentation**: API documentation with Swagger

## 📞 Support & Contact

### Getting Help
- **Issues**: Create GitHub issues for bugs or feature requests
- **Documentation**: Refer to this README for setup and usage
- **Code Comments**: Check inline code documentation

### Development Team
- **Project Type**: Educational/Portfolio Project
- **Technology Stack**: Spring Boot, Thymeleaf, Bootstrap, MariaDB
- **Architecture**: MVC pattern with repository layer

### Contributing
1. Fork the repository
2. Create a feature branch
3. Make your changes
4. Test thoroughly
5. Submit a pull request

---

## 📄 License

This project is created for educational and portfolio purposes. Please respect the original design inspiration and use responsibly.

---

**Version**: 2.0.1 | **Last Updated**: December 2024 | **Built with Spring Boot 3.2.0** 