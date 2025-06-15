# Service Layer Documentation - Visakh Refinery Portal

## 📋 Table of Contents
1. [Service Layer Overview](#service-layer-overview)
2. [WhatsNewService](#whatsnewservice)
3. [MessageBoardService](#messageboardservice)
4. [DataMigrationService](#datamigrationservice)
5. [UserService](#userservice)
6. [Common Patterns & Best Practices](#common-patterns--best-practices)
7. [Transaction Management](#transaction-management)
8. [Error Handling](#error-handling)

---

## 🎯 Service Layer Overview

The service layer in the Visakh Refinery Portal acts as the **business logic layer** between controllers and repositories. It encapsulates complex business rules, transaction management, and data processing logic.

### Key Responsibilities:
- **Business Logic**: Complex operations and validations
- **Transaction Management**: Ensuring data consistency
- **Data Transformation**: Converting between DTOs and entities
- **Orchestration**: Coordinating multiple repository operations
- **Caching**: Performance optimization strategies

### Service Architecture:
```
Controller Layer
      ↓
Service Layer (Business Logic)
      ↓
Repository Layer (Data Access)
      ↓
Database Layer
```

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

// Get items by date range
public Page<WhatsNew> getWhatsNewByDateRange(LocalDate fromDate, LocalDate toDate, int page, int size)

// Search by title
public Page<WhatsNew> searchWhatsNewByTitle(String title, int page, int size)

// Get single item by ID
public Optional<WhatsNew> getWhatsNewById(Long id)
```

**Key Features**:
- **Pagination Support**: All list methods support pagination
- **Active Filtering**: Automatically filters based on validity dates
- **Search Capabilities**: Title-based search with case-insensitive matching
- **Date Range Filtering**: Filter announcements by validity periods

#### **2. Data Persistence Methods**
```java
// Save or update with priority management
public WhatsNew saveWhatsNew(WhatsNew whatsNew)

// Delete item
public void deleteWhatsNew(Long id)

// Toggle enabled status
public WhatsNew toggleEnabled(Long id)
```

**Business Logic**:
- **Priority Management**: Automatically handles display order when priority changes
- **Audit Trail**: Sets creation and modification dates
- **Validation**: Ensures data integrity before saving

#### **3. Bulk Operations**
```java
// Bulk delete
public void deleteMultiple(List<Long> ids)

// Bulk enable
public void enableMultiple(List<Long> ids)

// Bulk disable
public void disableMultiple(List<Long> ids)
```

**Performance Benefits**:
- **Batch Processing**: Reduces database round trips
- **Transaction Efficiency**: Single transaction for multiple operations

#### **4. Priority & Display Order Management**

**Complex Algorithm**: The service implements a sophisticated priority and display order system:

```java
// Calculate global display order based on priority
private Integer calculateGlobalDisplayOrder(Integer targetPriority)

// Handle priority for new items
private void handlePriorityForNewItem(WhatsNew newItem)

// Handle priority changes for existing items
private void handlePriorityForUpdatedItem(WhatsNew updatedItem, Integer oldPriority, Integer oldDisplayOrder)

// Shift items after position change
private void shiftItemsAfterPosition(Integer targetPriority, Integer fromPosition)

// Recalculate all display orders
@Transactional
public void recalculateAllDisplayOrders()
```

**Algorithm Logic**:
1. **Priority 1** = Highest priority (displayed first)
2. **Global Sequential Numbering**: Items numbered 1, 2, 3... across all priorities
3. **Automatic Reordering**: When priority changes, all affected items are reordered
4. **Consistency Maintenance**: System can detect and fix ordering inconsistencies

#### **5. Utility Methods**
```java
// Check existence
public boolean existsById(Long id)

// Count active items
public long getActiveWhatsNewCount()

// Initialize display orders for existing data
@Transactional
public void initializeDisplayOrdersIfNeeded()
```

---

## 📢 MessageBoardService

**Purpose**: Manages message board entries with similar priority management but different business rules.

### Class Structure:
```java
@Service
@Transactional
public class MessageBoardService {
    @Autowired
    private MessageBoardRepository messageBoardRepository;
}
```

### 🔧 Core Methods:

#### **1. Data Retrieval Methods**
```java
// Get all active messages
public List<MessageBoard> getAllActiveMessages()

// Get active messages with pagination for display
public Page<MessageBoard> getActiveMessagesForDisplay(int page, int size)

// Get all messages for management
public Page<MessageBoard> getAllMessages(int page, int size)

// Get messages by enabled status
public Page<MessageBoard> getMessagesByEnabled(Boolean enabled, int page, int size)

// Search methods
public Page<MessageBoard> searchByHeader(String header, int page, int size)
public Page<MessageBoard> searchByMessage(String message, int page, int size)
public Page<MessageBoard> search(String searchTerm, int page, int size)
```

**Key Features**:
- **Multi-field Search**: Search in header, message content, or both
- **Status Filtering**: Filter by enabled/disabled status
- **Display Optimization**: Separate methods for public display vs management

#### **2. Advanced Search Capabilities**
```java
// Search with enabled filter
public Page<MessageBoard> searchMessages(String searchTerm, Boolean enabled, Pageable pageable)

// Search returning list for API
public List<MessageBoard> searchMessagesList(String searchTerm, Boolean enabled)

// Get messages by priority
public Page<MessageBoard> getMessagesByPriority(Integer priority, int page, int size)
```

**Search Features**:
- **Flexible Filtering**: Combine search terms with status filters
- **API Support**: Methods optimized for REST API responses
- **Priority-based Retrieval**: Get messages by specific priority levels

#### **3. Data Persistence**
```java
// Save with priority management
public MessageBoard saveMessage(MessageBoard messageBoard)

// Delete operations
public void deleteMessage(Long id)
public void deleteMessages(List<Long> ids)
```

**Business Logic**:
- **Timestamp Management**: Automatically sets creation and modification timestamps
- **Priority Ordering**: Similar to WhatsNew but with message-specific logic
- **Audit Fields**: Tracks who created/modified messages

#### **4. Specialized Methods**
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

#### **5. Priority Management System**
Similar to WhatsNewService but adapted for messages:

```java
private void handlePriorityForNewMessage(MessageBoard newMessage)
private void handlePriorityForUpdatedMessage(MessageBoard updatedMessage, Integer oldPriority, Integer oldDisplayOrder)
private Integer calculateGlobalDisplayOrder(Integer targetPriority)
private void shiftMessagesAfterPosition(Integer targetPriority, Integer fromPosition)
```

---

## 🔄 DataMigrationService

**Purpose**: Handles data migration from legacy CSV files to the new database structure.

### Class Structure:
```java
@Service
@Transactional
public class DataMigrationService {
    @Autowired
    private MessageBoardRepository messageBoardRepository;
}
```

### 🔧 Core Functionality:

#### **1. CSV Migration**
```java
// Main migration method
public MigrationResult migrateCsvData(String csvFilePath)

// Parse individual CSV lines
private MessageBoard parseCsvLine(String line, int lineNumber)

// Handle CSV field parsing with quotes
private String[] parseCsvFields(String line)

// Parse various date formats
private LocalDate parseDate(String dateStr)
```

**CSV Format Supported**:
```
MSGID,MESSAGE,VALIDUPTO,PRIORITY,MSGBY,DTSTAMP,MSG_HEADER,VALIDFROM,ENABLED,MESSAGE_HINDI,MSG_HEADER_HINDI
```

**Migration Features**:
- **Error Handling**: Continues processing even if individual records fail
- **Data Validation**: Validates required fields and data formats
- **Format Flexibility**: Handles multiple date formats
- **Encoding Support**: Properly handles quoted CSV fields
- **Multilingual Support**: Supports Hindi content migration

#### **2. Migration Result Tracking**
```java
public static class MigrationResult {
    private int totalRecords;
    private int successfulRecords;
    private int failedRecords;
    private List<String> errors;
    
    // Methods for tracking progress
    public void incrementSuccessful()
    public void incrementFailed()
    public void addError(String error)
}
```

**Tracking Features**:
- **Success/Failure Counts**: Detailed statistics
- **Error Collection**: Specific error messages for failed records
- **Progress Monitoring**: Real-time migration progress

#### **3. Data Transformation Logic**
```java
// Field mapping from CSV to entity
messageBoard.setMessage(message);           // MESSAGE field
messageBoard.setHeader(header);             // MSG_HEADER field
messageBoard.setValidFrom(validFrom);       // VALIDFROM field
messageBoard.setValidTo(validTo);           // VALIDUPTO field
messageBoard.setPriority(priority);         // PRIORITY field
messageBoard.setEnabled(enabled);           // ENABLED field (Y/N to boolean)
messageBoard.setCreatedBy(createdBy);       // MSGBY field
messageBoard.setMessageHindi(messageHindi); // MESSAGE_HINDI field
messageBoard.setHeaderHindi(headerHindi);   // MSG_HEADER_HINDI field
```

**Transformation Features**:
- **Data Type Conversion**: String to appropriate Java types
- **Boolean Mapping**: Y/N to true/false conversion
- **Date Parsing**: Multiple date format support
- **Default Values**: Sets sensible defaults for new fields

#### **4. Utility Methods**
```java
// Clear all existing data
public void clearAllMessageBoardData()

// Get migration statistics
public String getMigrationStatistics()
```

---

## 👤 UserService

**Purpose**: Manages user authentication, registration, and user-related operations. Implements Spring Security's `UserDetailsService`.

### Class Structure:
```java
@Service
@Transactional
public class UserService implements UserDetailsService {
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private PasswordEncoder passwordEncoder;
}
```

### 🔧 Core Methods:

#### **1. Spring Security Integration**
```java
// Load user for authentication
@Override
public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException
```

**Security Features**:
- **UserDetailsService Implementation**: Direct integration with Spring Security
- **Exception Handling**: Proper `UsernameNotFoundException` for missing users
- **Logging**: Debug logging for authentication attempts

#### **2. User Registration**
```java
// Register new user with validation
public User registerUser(User user) throws IllegalArgumentException
```

**Registration Logic**:
- **Uniqueness Validation**: Checks username and email uniqueness
- **Password Encoding**: Uses BCrypt for secure password storage
- **Default Values**: Sets enabled=true and role=ADMIN
- **Error Handling**: Throws descriptive exceptions for validation failures

#### **3. User Lookup Methods**
```java
// Find by username
public Optional<User> findByUsername(String username)

// Find by email
public Optional<User> findByEmail(String email)

// Find by ID
public Optional<User> findById(Long id)

// Get all enabled users
public List<User> getAllEnabledUsers()
```

#### **4. User Management**
```java
// Update last login time
public void updateLastLogin(String username)

// Update user information
public User updateUser(User user)

// Soft delete (disable user)
public void deleteUser(Long id)

// Change password
public void changePassword(String username, String newPassword)
```

**Management Features**:
- **Audit Trail**: Tracks last login times
- **Soft Delete**: Disables rather than deletes users
- **Password Security**: Proper encoding for password changes
- **Update Support**: Full user information updates

#### **5. Validation Methods**
```java
// Check username availability
public boolean isUsernameAvailable(String username)

// Check email availability
public boolean isEmailAvailable(String email)
```

---

## 🎯 Common Patterns & Best Practices

### 1. **Dependency Injection**
All services use `@Autowired` for dependency injection:
```java
@Autowired
private WhatsNewRepository whatsNewRepository;
```

### 2. **Transaction Management**
Class-level `@Transactional` ensures data consistency:
```java
@Service
@Transactional
public class WhatsNewService {
    // All methods are transactional by default
}
```

### 3. **Optional Usage**
Proper handling of potentially null values:
```java
public Optional<WhatsNew> getWhatsNewById(Long id) {
    return whatsNewRepository.findById(id);
}
```

### 4. **Pagination Support**
Consistent pagination across all services:
```java
public Page<WhatsNew> getAllWhatsNew(int page, int size, String sortBy, String sortDir) {
    Pageable pageable = PageRequest.of(page, size);
    return whatsNewRepository.findAll(pageable);
}
```

### 5. **Error Handling**
Descriptive exceptions with meaningful messages:
```java
if (userRepository.existsByUsername(user.getUsername())) {
    throw new IllegalArgumentException("Username already exists: " + user.getUsername());
}
```

### 6. **Logging**
Appropriate logging levels for debugging and monitoring:
```java
private static final Logger logger = LoggerFactory.getLogger(UserService.class);
logger.info("User registered successfully: {}", savedUser.getUsername());
```

---

## 🔄 Transaction Management

### **Class-Level Transactions**
```java
@Service
@Transactional
public class WhatsNewService {
    // All public methods are transactional
}
```

### **Method-Level Transactions**
```java
@Transactional
public void recalculateAllDisplayOrders() {
    // Complex operation requiring transaction
}
```

### **Read-Only Transactions** (Optimization)
```java
@Transactional(readOnly = true)
public Page<WhatsNew> getAllWhatsNew(int page, int size) {
    // Read-only operation for better performance
}
```

---

## ⚠️ Error Handling

### **1. Validation Exceptions**
```java
if (whatsNew.getTitle() == null || whatsNew.getTitle().trim().isEmpty()) {
    throw new IllegalArgumentException("Title is required");
}
```

### **2. Not Found Exceptions**
```java
User user = userRepository.findByUsername(username)
    .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));
```

### **3. Business Logic Exceptions**
```java
if (userRepository.existsByUsername(user.getUsername())) {
    throw new IllegalArgumentException("Username already exists");
}
```

### **4. Migration Error Handling**
```java
try {
    MessageBoard messageBoard = parseCsvLine(line, lineNumber);
    messageBoardRepository.save(messageBoard);
    result.incrementSuccessful();
} catch (Exception e) {
    result.incrementFailed();
    result.addError("Line " + lineNumber + ": " + e.getMessage());
}
```

---

## 🚀 Performance Optimizations

### **1. Batch Operations**
```java
public void enableMultiple(List<Long> ids) {
    List<WhatsNew> items = whatsNewRepository.findAllById(ids);
    items.forEach(item -> {
        item.setEnabled(true);
        item.setModifiedDate(LocalDate.now());
    });
    whatsNewRepository.saveAll(items); // Single batch operation
}
```

### **2. Pagination**
All list operations support pagination to prevent memory issues:
```java
Pageable pageable = PageRequest.of(page, size);
return repository.findAll(pageable);
```

### **3. Lazy Loading**
Using `Optional` and proper repository methods to avoid unnecessary data loading.

### **4. Display Order Optimization**
Intelligent display order management that minimizes database updates:
```java
// Only recalculate when necessary
public void initializeDisplayOrdersIfNeeded() {
    if (hasItemsWithoutOrder || hasIncorrectSequence) {
        recalculateAllDisplayOrders();
    }
}
```

---

This service layer documentation provides a comprehensive understanding of the business logic implementation in the Visakh Refinery Portal. Each service is designed with specific responsibilities, proper error handling, and performance considerations. 