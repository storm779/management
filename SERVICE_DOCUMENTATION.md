# Service Layer Documentation - Visakh Refinery Portal

## 📋 Table of Contents
1. [Service Layer Overview](#service-layer-overview)
2. [WhatsNewService](#whatsnewservice)
3. [MessageBoardService](#messageboardservice)
4. [DataMigrationService](#datamigrationservice)
5. [UserService](#userservice)
6. [Common Patterns & Best Practices](#common-patterns--best-practices)

---

## 🎯 Service Layer Overview

The service layer acts as the **business logic layer** between controllers and repositories, encapsulating complex business rules, transaction management, and data processing logic.

### Key Responsibilities:
- **Business Logic**: Complex operations and validations
- **Transaction Management**: Ensuring data consistency
- **Data Transformation**: Converting between DTOs and entities
- **Orchestration**: Coordinating multiple repository operations

---

## 🗃️ WhatsNewService

**Purpose**: Manages "What's New" announcements with complex priority ordering and display management.

### Class Structure:
```java
@Service
@Transactional
public class WhatsNewService {
    @Autowired
    private WhatsNewRepository whatsNewRepository;
}
```

### 🔧 Core Methods:

#### **1. Data Retrieval Methods**
```java
// Get active items for dashboard (top 5)
public List<WhatsNew> getActiveWhatsNewForDashboard()

// Get all active items
public List<WhatsNew> getAllActiveWhatsNew()

// Get all items with pagination
public Page<WhatsNew> getAllWhatsNew(int page, int size, String sortBy, String sortDir)

// Get items by enabled status
public Page<WhatsNew> getWhatsNewByEnabled(Boolean enabled, int page, int size)

// Search by title
public Page<WhatsNew> searchWhatsNewByTitle(String title, int page, int size)
```

**Key Features**:
- **Pagination Support**: All list methods support pagination
- **Active Filtering**: Automatically filters based on validity dates
- **Search Capabilities**: Title-based search with case-insensitive matching

#### **2. Priority & Display Order Management**

**Complex Algorithm**: Implements sophisticated priority and display order system:

```java
// Calculate global display order based on priority
private Integer calculateGlobalDisplayOrder(Integer targetPriority)

// Handle priority for new items
private void handlePriorityForNewItem(WhatsNew newItem)

// Recalculate all display orders
@Transactional
public void recalculateAllDisplayOrders()
```

**Algorithm Logic**:
1. **Priority 1** = Highest priority (displayed first)
2. **Global Sequential Numbering**: Items numbered 1, 2, 3... across all priorities
3. **Automatic Reordering**: When priority changes, all affected items are reordered
4. **Consistency Maintenance**: System can detect and fix ordering inconsistencies

#### **3. Bulk Operations**
```java
// Bulk delete
public void deleteMultiple(List<Long> ids)

// Bulk enable/disable
public void enableMultiple(List<Long> ids)
public void disableMultiple(List<Long> ids)
```

**Performance Benefits**:
- **Batch Processing**: Reduces database round trips
- **Transaction Efficiency**: Single transaction for multiple operations

---

## 📢 MessageBoardService

**Purpose**: Manages message board entries with similar priority management but different business rules.

### 🔧 Core Methods:

#### **1. Data Retrieval Methods**
```java
// Get all active messages
public List<MessageBoard> getAllActiveMessages()

// Get active messages with pagination for display
public Page<MessageBoard> getActiveMessagesForDisplay(int page, int size)

// Search methods
public Page<MessageBoard> searchByHeader(String header, int page, int size)
public Page<MessageBoard> searchByMessage(String message, int page, int size)
public Page<MessageBoard> search(String searchTerm, int page, int size)
```

**Key Features**:
- **Multi-field Search**: Search in header, message content, or both
- **Status Filtering**: Filter by enabled/disabled status
- **Display Optimization**: Separate methods for public display vs management

#### **2. Specialized Methods**
```java
// Get messages for scrolling display
public List<MessageBoard> getMessagesForScrolling()

// Get top active messages with limit
public List<MessageBoard> getTopActiveMessages(int limit)

// Count active messages
public long countActiveMessages()
```

**Use Cases**:
- **Scrolling Ticker**: Messages for continuous display
- **Dashboard Widgets**: Limited number of top messages
- **Statistics**: Count for dashboard metrics

---

## 🔄 DataMigrationService

**Purpose**: Handles data migration from legacy CSV files to the new database structure.

### 🔧 Core Functionality:

#### **1. CSV Migration**
```java
// Main migration method
public MigrationResult migrateCsvData(String csvFilePath)

// Parse individual CSV lines
private MessageBoard parseCsvLine(String line, int lineNumber)
```

**CSV Format Supported**:
```
MSGID,MESSAGE,VALIDUPTO,PRIORITY,MSGBY,DTSTAMP,MSG_HEADER,VALIDFROM,ENABLED,MESSAGE_HINDI,MSG_HEADER_HINDI
```

**Migration Features**:
- **Error Handling**: Continues processing even if individual records fail
- **Data Validation**: Validates required fields and data formats
- **Format Flexibility**: Handles multiple date formats
- **Multilingual Support**: Supports Hindi content migration

#### **2. Migration Result Tracking**
```java
public static class MigrationResult {
    private int totalRecords;
    private int successfulRecords;
    private int failedRecords;
    private List<String> errors;
}
```

**Tracking Features**:
- **Success/Failure Counts**: Detailed statistics
- **Error Collection**: Specific error messages for failed records
- **Progress Monitoring**: Real-time migration progress

---

## 👤 UserService

**Purpose**: Manages user authentication, registration, and user-related operations. Implements Spring Security's `UserDetailsService`.

### 🔧 Core Methods:

#### **1. Spring Security Integration**
```java
// Load user for authentication
@Override
public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException
```

#### **2. User Registration**
```java
// Register new user with validation
public User registerUser(User user) throws IllegalArgumentException
```

**Registration Logic**:
- **Uniqueness Validation**: Checks username and email uniqueness
- **Password Encoding**: Uses BCrypt for secure password storage
- **Default Values**: Sets enabled=true and role=ADMIN

#### **3. User Management**
```java
// Update last login time
public void updateLastLogin(String username)

// Soft delete (disable user)
public void deleteUser(Long id)

// Change password
public void changePassword(String username, String newPassword)
```

**Management Features**:
- **Audit Trail**: Tracks last login times
- **Soft Delete**: Disables rather than deletes users
- **Password Security**: Proper encoding for password changes

---

## 🎯 Common Patterns & Best Practices

### 1. **Dependency Injection**
```java
@Autowired
private WhatsNewRepository whatsNewRepository;
```

### 2. **Transaction Management**
```java
@Service
@Transactional
public class WhatsNewService {
    // All methods are transactional by default
}
```

### 3. **Optional Usage**
```java
public Optional<WhatsNew> getWhatsNewById(Long id) {
    return whatsNewRepository.findById(id);
}
```

### 4. **Pagination Support**
```java
public Page<WhatsNew> getAllWhatsNew(int page, int size) {
    Pageable pageable = PageRequest.of(page, size);
    return whatsNewRepository.findAll(pageable);
}
```

### 5. **Error Handling**
```java
if (userRepository.existsByUsername(user.getUsername())) {
    throw new IllegalArgumentException("Username already exists");
}
```

### 6. **Bulk Operations Optimization**
```java
public void enableMultiple(List<Long> ids) {
    List<WhatsNew> items = whatsNewRepository.findAllById(ids);
    items.forEach(item -> item.setEnabled(true));
    whatsNewRepository.saveAll(items); // Single batch operation
}
```

---

## 🚀 Key Features Summary

### **WhatsNewService**:
- ✅ Complex priority management with global sequential ordering
- ✅ Bulk operations for efficiency
- ✅ Active/inactive filtering based on date validity
- ✅ Comprehensive search and pagination

### **MessageBoardService**:
- ✅ Multi-field search capabilities
- ✅ Specialized display methods for different use cases
- ✅ Similar priority management adapted for messages
- ✅ Scrolling ticker support

### **DataMigrationService**:
- ✅ Robust CSV parsing with error handling
- ✅ Data validation and transformation
- ✅ Progress tracking and error reporting
- ✅ Multilingual content support

### **UserService**:
- ✅ Spring Security integration
- ✅ Secure password handling
- ✅ User lifecycle management
- ✅ Audit trail maintenance

This service layer provides a robust foundation for the Visakh Refinery Portal with proper separation of concerns, transaction management, and business logic encapsulation. 