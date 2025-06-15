# Backend Code Documentation - Visakh Refinery Portal

## 📋 Table of Contents
1. [Project Overview](#project-overview)
2. [Architecture & Structure](#architecture--structure)
3. [Entity Layer](#entity-layer)
4. [Repository Layer](#repository-layer)
5. [Service Layer](#service-layer)
6. [Controller Layer](#controller-layer)
7. [Configuration & Security](#configuration--security)
8. [Database Schema](#database-schema)
9. [API Endpoints](#api-endpoints)
10. [Error Handling](#error-handling)
11. [Data Flow](#data-flow)

---

## 🎯 Project Overview

The **Visakh Refinery Portal** is a Spring Boot web application designed for managing announcements and messages within a refinery environment. It follows the **MVC (Model-View-Controller)** pattern with a layered architecture.

### Key Features:
- **What's New Announcements** management
- **Message Board** functionality  
- **Role-based access control** (Admin/User)
- **Priority-based content** organization
- **Date-based validity** for announcements
- **Responsive web interface** with DataTables

### Technology Stack:
- **Framework**: Spring Boot 3.x
- **Security**: Spring Security 6.x
- **Database**: H2 (Development) / MySQL/PostgreSQL (Production)
- **ORM**: Spring Data JPA / Hibernate
- **Template Engine**: Thymeleaf
- **Frontend**: Bootstrap 5, DataTables, jQuery
- **Build Tool**: Maven

---

## 🏗️ Architecture & Structure

```
src/main/java/com/example/management/
├── entity/           # Data models (JPA entities)
├── repository/       # Data access layer (Spring Data JPA)
├── service/          # Business logic layer
├── controller/       # Web controllers (MVC)
├── config/           # Configuration classes
├── security/         # Security configuration
└── ManagementApplication.java  # Main application class
```

### Architectural Patterns:
- **Layered Architecture**: Clear separation of concerns
- **Repository Pattern**: Data access abstraction
- **Service Layer Pattern**: Business logic encapsulation
- **MVC Pattern**: Web layer organization
- **Dependency Injection**: Spring IoC container

---

## 🗃️ Entity Layer

### WhatsNew Entity
```java
@Entity
@Table(name = "whats_new")
public class WhatsNew {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false, length = 255)
    private String title;
    
    @Column(columnDefinition = "TEXT")
    private String description;
    
    @Column(name = "priority", nullable = false)
    private Integer priority;
    
    @Column(name = "valid_from", nullable = false)
    private LocalDate validFrom;
    
    @Column(name = "valid_to")
    private LocalDate validTo;
    
    @Column(name = "url", length = 500)
    private String url;
    
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    // Constructors, getters, setters, lifecycle callbacks
}
```

#### Key Features:
- **Auto-generated ID**: Primary key with auto-increment
- **Validation**: Non-null constraints on required fields
- **Date Management**: Separate creation and update timestamps
- **Priority System**: Integer-based priority (1-5, where 1 is highest)
- **Flexible Validity**: Optional end date for announcements
- **URL Support**: Optional external links

#### Lifecycle Callbacks:
```java
@PrePersist
protected void onCreate() {
    createdAt = LocalDateTime.now();
}

@PreUpdate
protected void onUpdate() {
    updatedAt = LocalDateTime.now();
}
```

---

## 🗄️ Repository Layer

### WhatsNewRepository
```java
@Repository
public interface WhatsNewRepository extends JpaRepository<WhatsNew, Long> {
    
    // Find active announcements (current date within validity period)
    @Query("SELECT w FROM WhatsNew w WHERE w.validFrom <= :currentDate " +
           "AND (w.validTo IS NULL OR w.validTo >= :currentDate)")
    List<WhatsNew> findActiveAnnouncements(@Param("currentDate") LocalDate currentDate);
    
    // Find by priority with pagination
    Page<WhatsNew> findByPriorityOrderByValidFromDesc(Integer priority, Pageable pageable);
    
    // Count active announcements
    @Query("SELECT COUNT(w) FROM WhatsNew w WHERE w.validFrom <= :currentDate " +
           "AND (w.validTo IS NULL OR w.validTo >= :currentDate)")
    long countActiveAnnouncements(@Param("currentDate") LocalDate currentDate);
}
```

#### Key Repository Features:
- **Custom Queries**: JPQL for complex business logic
- **Pagination Support**: Built-in Spring Data pagination
- **Date-based Filtering**: Active/expired announcement logic
- **Priority Sorting**: Multi-level sorting capabilities
- **Count Queries**: For statistics and dashboard metrics

---

## 🔧 Service Layer

### WhatsNewService
```java
@Service
@Transactional
public class WhatsNewService {
    
    private final WhatsNewRepository whatsNewRepository;
    
    public WhatsNewService(WhatsNewRepository whatsNewRepository) {
        this.whatsNewRepository = whatsNewRepository;
    }
    
    // Get paginated list with optional priority filter
    @Transactional(readOnly = true)
    public Page<WhatsNew> getWhatsNewPage(int page, int size, Integer priority) {
        Pageable pageable = PageRequest.of(page, size, 
            Sort.by(Sort.Direction.DESC, "validFrom"));
        
        if (priority != null) {
            return whatsNewRepository.findByPriorityOrderByValidFromDesc(priority, pageable);
        }
        return whatsNewRepository.findAll(pageable);
    }
    
    // Save or update announcement
    public WhatsNew saveWhatsNew(WhatsNew whatsNew) {
        validateWhatsNew(whatsNew);
        return whatsNewRepository.save(whatsNew);
    }
    
    // Validation logic
    private void validateWhatsNew(WhatsNew whatsNew) {
        if (whatsNew.getTitle() == null || whatsNew.getTitle().trim().isEmpty()) {
            throw new IllegalArgumentException("Title is required");
        }
        
        if (whatsNew.getPriority() == null || whatsNew.getPriority() < 1 || whatsNew.getPriority() > 5) {
            throw new IllegalArgumentException("Priority must be between 1 and 5");
        }
        
        if (whatsNew.getValidFrom() == null) {
            throw new IllegalArgumentException("Valid from date is required");
        }
        
        if (whatsNew.getValidTo() != null && whatsNew.getValidTo().isBefore(whatsNew.getValidFrom())) {
            throw new IllegalArgumentException("Valid to date must be after valid from date");
        }
    }
}
```

#### Service Layer Features:
- **Transaction Management**: `@Transactional` for data consistency
- **Business Logic**: Validation, statistics calculation
- **Exception Handling**: Custom exceptions for different scenarios
- **Read-Only Optimization**: Separate read-only transactions

---

## 🎮 Controller Layer

### WhatsNewController
```java
@Controller
@RequestMapping("/whatsnew")
public class WhatsNewController {
    
    private final WhatsNewService whatsNewService;
    
    public WhatsNewController(WhatsNewService whatsNewService) {
        this.whatsNewService = whatsNewService;
    }
    
    // Display paginated list of announcements
    @GetMapping("/list")
    public String listWhatsNew(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) Integer priority,
            Model model) {
        
        try {
            Page<WhatsNew> whatsNewPage = whatsNewService.getWhatsNewPage(page, size, priority);
            
            model.addAttribute("whatsNewPage", whatsNewPage);
            model.addAttribute("currentPage", page);
            model.addAttribute("selectedPriority", priority);
            
            return "whatsnew/list";
        } catch (Exception e) {
            model.addAttribute("error", "Error loading announcements: " + e.getMessage());
            return "error";
        }
    }
    
    // Show add form (Admin only)
    @GetMapping("/add")
    @PreAuthorize("hasRole('ADMIN')")
    public String showAddForm(Model model) {
        model.addAttribute("whatsNew", new WhatsNew());
        model.addAttribute("isEdit", false);
        return "whatsnew/form";
    }
    
    // Handle form submission
    @PostMapping("/add")
    @PreAuthorize("hasRole('ADMIN')")
    public String addWhatsNew(
            @Valid @ModelAttribute WhatsNew whatsNew,
            BindingResult bindingResult,
            RedirectAttributes redirectAttributes) {
        
        if (bindingResult.hasErrors()) {
            return "whatsnew/form";
        }
        
        try {
            whatsNewService.saveWhatsNew(whatsNew);
            redirectAttributes.addFlashAttribute("success", 
                "Announcement added successfully!");
            return "redirect:/whatsnew/list";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", 
                "Error adding announcement: " + e.getMessage());
            return "redirect:/whatsnew/add";
        }
    }
}
```

#### Controller Features:
- **RESTful URLs**: Clean, semantic URL structure
- **Security Integration**: Method-level security with `@PreAuthorize`
- **Validation**: Bean validation with error handling
- **Flash Attributes**: Success/error messages across redirects
- **Model Binding**: Automatic form-to-object mapping

---

## 🔐 Configuration & Security

### SecurityConfig
```java
@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
public class SecurityConfig {
    
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .authorizeHttpRequests(authz -> authz
                .requestMatchers("/", "/css/**", "/js/**", "/images/**").permitAll()
                .requestMatchers("/whatsnew/list", "/messageboard/list").permitAll()
                .requestMatchers("/whatsnew/add", "/whatsnew/edit/**").hasRole("ADMIN")
                .anyRequest().authenticated()
            )
            .formLogin(form -> form
                .loginPage("/login")
                .defaultSuccessUrl("/", true)
                .permitAll()
            )
            .logout(logout -> logout
                .logoutUrl("/logout")
                .logoutSuccessUrl("/")
                .permitAll()
            );
        
        return http.build();
    }
    
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
```

#### Security Features:
- **Role-based Access**: Admin vs User permissions
- **Method Security**: `@PreAuthorize` annotations
- **Password Encoding**: BCrypt for secure password storage
- **CSRF Protection**: Built-in CSRF token handling

---

## 🗃️ Database Schema

### WhatsNew Table
```sql
CREATE TABLE whats_new (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    title VARCHAR(255) NOT NULL,
    description TEXT,
    priority INTEGER NOT NULL CHECK (priority BETWEEN 1 AND 5),
    valid_from DATE NOT NULL,
    valid_to DATE,
    url VARCHAR(500),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP,
    
    INDEX idx_priority (priority),
    INDEX idx_valid_from (valid_from),
    INDEX idx_created_at (created_at)
);
```

#### Database Design Features:
- **Primary Keys**: Auto-incrementing BIGINT for scalability
- **Constraints**: Check constraints for data integrity
- **Indexes**: Performance optimization for common queries
- **Timestamps**: Audit trail with creation and update times

---

## 🔗 API Endpoints

### WhatsNew Endpoints
| Method | URL | Description | Access |
|--------|-----|-------------|--------|
| GET | `/whatsnew/list` | List announcements | Public |
| GET | `/whatsnew/add` | Show add form | Admin |
| POST | `/whatsnew/add` | Create announcement | Admin |
| GET | `/whatsnew/edit/{id}` | Show edit form | Admin |
| POST | `/whatsnew/edit/{id}` | Update announcement | Admin |
| POST | `/whatsnew/delete/{id}` | Delete announcement | Admin |

### Common Endpoints
| Method | URL | Description | Access |
|--------|-----|-------------|--------|
| GET | `/` | Dashboard/Home | Public |
| GET | `/login` | Login page | Public |
| POST | `/logout` | Logout | Authenticated |

---

## ⚠️ Error Handling

### Global Exception Handler
```java
@ControllerAdvice
public class GlobalExceptionHandler {
    
    @ExceptionHandler(EntityNotFoundException.class)
    public String handleEntityNotFound(EntityNotFoundException e, Model model) {
        model.addAttribute("error", e.getMessage());
        return "error";
    }
    
    @ExceptionHandler(IllegalArgumentException.class)
    public String handleIllegalArgument(IllegalArgumentException e, Model model) {
        model.addAttribute("error", "Invalid input: " + e.getMessage());
        return "error";
    }
}
```

---

## 🔄 Data Flow

### Typical Request Flow:
1. **HTTP Request** → Controller
2. **Controller** → Service (business logic)
3. **Service** → Repository (data access)
4. **Repository** → Database
5. **Database** → Repository (results)
6. **Repository** → Service (entities)
7. **Service** → Controller (processed data)
8. **Controller** → View (Thymeleaf template)
9. **View** → HTTP Response

### Example: Adding New Announcement
```
1. User submits form → WhatsNewController.addWhatsNew()
2. Controller validates input → @Valid annotation
3. Controller calls → WhatsNewService.saveWhatsNew()
4. Service validates business rules → validateWhatsNew()
5. Service calls → WhatsNewRepository.save()
6. Repository persists → Database INSERT
7. Success response → Redirect with flash message
8. User sees → Success confirmation
```

---

## 🎯 Key Design Patterns

### 1. Repository Pattern
- **Purpose**: Abstract data access logic
- **Implementation**: Spring Data JPA interfaces
- **Benefits**: Testability, maintainability, database independence

### 2. Service Layer Pattern
- **Purpose**: Encapsulate business logic
- **Implementation**: `@Service` annotated classes
- **Benefits**: Transaction management, reusability, separation of concerns

### 3. MVC Pattern
- **Purpose**: Separate presentation, business logic, and data
- **Implementation**: Spring MVC with Thymeleaf
- **Benefits**: Maintainability, testability, clear responsibilities

### 4. Dependency Injection
- **Purpose**: Loose coupling between components
- **Implementation**: Spring IoC container
- **Benefits**: Testability, flexibility, configuration management

---

This documentation provides a comprehensive overview of the backend architecture and implementation details. The code follows Spring Boot best practices with clear separation of concerns, proper error handling, and security considerations. 