# 📜 PROJECT CONSTITUTION — SDD for Java SE

> **This file is the single source of truth for AI-assisted code generation.**
> Feed this document as context to the AI before requesting any code.

---

## 1. AI Workflow — Mandatory Sequence

> **When a developer provides a filled SPEC file, the AI MUST follow this exact sequence:**

```
STEP 1: READ         → Read CONSTITUTION.md (this file) + the filled SPEC_[FeatureName].md
STEP 2: GENERATE EXCEPTION → Create custom exception class(es) defined in the spec
STEP 3: GENERATE MODEL     → Create the domain entity/DTO with all fields from the spec
                              (🔴 CRITICAL: In toString(), use `+ "'"` to wrap String fields.
                               NEVER use three consecutive single quotes '''.
                               See §2.5 for the MANDATORY pattern.)
STEP 4: GENERATE SQL DDL   → Generate the `CREATE TABLE` script using the Active Database Dialect
                              Save it under `src/main/resources/sql/`
STEP 5: GENERATE TESTS     → Create the JUnit 5 test class (ALL scenarios from the spec)
                              Use BusinessRuleSpecTemplateTest.java as the structural pattern
                              (⚠️ ENFORCE: Add `throws Exception` to ALL test method signatures to prevent Mockito SQLException errors).
STEP 6: VERIFY RED         → Confirm tests reference classes that do not yet exist (RED phase)
                              (ℹ️ EXPECTED: `cannot find symbol` compilation errors for DAO/Service. Do not fix, proceed to Step 7)
STEP 7: GENERATE DAO       → Create the DAO with pure JDBC + try-with-resources
STEP 8: GENERATE SERVICE   → Create the Service with business rules + manual transactions
STEP 9: GENERATE CONTROLLER→ Create the Controller/CLI handler (if applicable)
STEP 10: GENERATE VIEW     → Create the UI class using active framework (if UI is tracked in Spec)
STEP 11: INTEGRATE UI      → Modify MainView/MainFrame/index.jsp to add a button/link loading the new View
                              (⚠️ OUTPUT THE FULL FILE: include all imports and verify Layout logic to avoid duplicate code blocks)
STEP 12: VERIFY GREEN      → Confirm all tests now pass (GREEN phase)
```

> **❌ NEVER skip steps. ❌ ALWAYS generate the SQL table BEFORE the tests to ensure fields map correctly.**

---

## 2. Non-Negotiable Rules

### 2.1 Architecture

| Rule | Constraint |
|------|-----------|
| Language | Java 17+ |
| ORM | ❌ **FORBIDDEN** (No Hibernate, JPA, Spring, MyBatis) |
| Database Access | **Pure JDBC only** — `PreparedStatement`, `ResultSet` |
| Resource Management | **`try-with-resources` is MANDATORY** for all JDBC objects |
| Architecture | Strict layered: `View/UI → Controller → Service → DAO → Model` |
| Dependencies | Unidirectional only — no layer may skip or call upward |
| Presentation | **OPTIONAL** — JavaFX, Swing, or Web (JSP/Servlet). View ONLY calls Controller |

### 2.2 Transaction Management

> **Transactions are EXCLUSIVELY managed in the Service Layer.**

```java
// ✅ PATTERN — The AI MUST replicate this structure in every Service method that writes data
public class [Entity]Service {

    private final [Entity]Dao dao;

    public [Entity]Service([Entity]Dao dao) {
        this.dao = dao; // Constructor injection — MANDATORY
    }

    public [ReturnType] [methodName]([Entity] entity) {
        Connection conn = null;
        try {
            conn = ConnectionFactory.getConnection();
            conn.setAutoCommit(false);

            // === BUSINESS RULE VALIDATIONS (throw custom exceptions) ===
            if (entity.getField() == null || entity.getField().isBlank()) {
                throw new [CustomException]("Field must not be null or empty");
            }

            // === DAO OPERATIONS ===
            [ReturnType] result = dao.[method](conn, entity);

            conn.commit();
            return result;

        } catch ([CustomException] e) {
            if (conn != null) { try { conn.rollback(); } catch (SQLException ex) { /* log */ } }
            throw e; // Re-throw business exceptions
        } catch (Exception e) {
            if (conn != null) { try { conn.rollback(); } catch (SQLException ex) { /* log */ } }
            throw new ServiceException("Operation failed", e);
        } finally {
            if (conn != null) {
                try { conn.setAutoCommit(true); conn.close(); } catch (SQLException e) { /* log */ }
            }
        }
    }
}
```

### 2.3 DAO Pattern

```java
// ✅ PATTERN — The AI MUST replicate this structure in every DAO method
public class [Entity]Dao {

    public Optional<[Entity]> findById(Connection conn, int id) throws SQLException {
        String sql = "SELECT id, name, email FROM [table] WHERE id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapRow(rs));
                }
            }
        }
        return Optional.empty();
    }

    public void save(Connection conn, [Entity] entity) throws SQLException {
        String sql = "INSERT INTO [table] (name, email) VALUES (?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, entity.getName());
            ps.setString(2, entity.getEmail());
            ps.executeUpdate();
        }
    }

    private [Entity] mapRow(ResultSet rs) throws SQLException {
        [Entity] entity = new [Entity]();
        entity.setId(rs.getInt("id"));
        entity.setName(rs.getString("name"));
        entity.setEmail(rs.getString("email"));
        return entity;
    }
}
```

### 2.4 Custom Exception Pattern

```java
// ✅ PATTERN — One exception per business rule category
public class [CustomException] extends RuntimeException {
    public [CustomException](String message) {
        super(message);
    }
    public [CustomException](String message, Throwable cause) {
        super(message, cause);
    }
}
```

### 2.5 Model Pattern

```java
// ✅ PATTERN — Pure POJO, NO framework annotations
public class [Entity] {
    private int id;
    private String name;
    private String email;

    public [Entity]() {}

    public [Entity](int id, String name, String email) {
        this.id = id;
        this.name = name;
        this.email = email;
    }

    // Getters and setters for all fields
    // Override equals(), hashCode() when appropriate
}
```

#### 🔴 MANDATORY `toString()` Pattern

> **The AI MUST use `+ "'"` (string concatenation with a single-quote string) to wrap String fields in `toString()`.**
> **Using three consecutive single quotes `'''` causes a compilation error. This is a BLOCKER.**

```java
// ✅ CORRECT — Use + "'" to wrap String field values
@Override
public String toString() {
    return "[Entity]{" +
            "id=" + id +
            ", name='" + name + "'" +
            ", email='" + email + "'" +
            "}";
}
```

```java
// ❌ WRONG — Three consecutive single quotes ''' causes: "empty character literal" compiler error
@Override
public String toString() {
    return "[Entity]{" +
            "id=" + id +
            ", name='" + name + ''' +   // ← COMPILATION ERROR
            ", email='" + email + ''' + // ← COMPILATION ERROR
            '}';
}
```

### 2.6 Presentation Layer (Optional)

> **The View/UI layer is OPTIONAL.** Without it, the Controller acts as the entry point (CLI, scripts, etc.).
> When added, the View ONLY communicates with Controller — **NEVER** with Service or DAO directly.

```
✅ ALLOWED                          ❌ FORBIDDEN
─────────────────────────           ─────────────────────────
View → Controller → Service         View → Service (skip)
View → Controller                   View → DAO (skip)
                                    View → Model direct save
```

#### Desktop — JavaFX Pattern

```java
// ⚠️ MANDATORY IMPORTS — The AI MUST include the exception classes
// import [Package].exception.[CustomException];
// import [Package].exception.ServiceException;

// ✅ PATTERN — View delegates ALL logic to Controller
public class [Entity]View extends Application {

    private final [Entity]Controller controller;

    public [Entity]View() {
        // ⚠️ Initialize database schema FIRST
        DatabaseInitializer.initialize();

        // Build the dependency chain
        [Entity]Dao dao = new [Entity]Dao();
        [Entity]Service service = new [Entity]Service(dao);
        this.controller = new [Entity]Controller(service);
    }

    @Override
    public void start(Stage stage) {
        // Build UI components (labels, fields, buttons, tables)
        // All event handlers delegate to controller:
        saveButton.setOnAction(e -> {
            try {
                controller.handleSave(nameField.getText(), priceField.getText());
                showSuccess("Record saved");
            } catch (RuntimeException ex) {
                showError(ex.getMessage()); // Display business exception to user
            }
        });
    }
}
```

#### Desktop — Swing Pattern

```java
// ⚠️ MANDATORY IMPORTS — The AI MUST include the exception classes
// import [Package].exception.[CustomException];
// import [Package].exception.ServiceException;

// ✅ PATTERN — JFrame delegates ALL logic to Controller
public class [Entity]Frame extends JFrame {

    private final [Entity]Controller controller;

    public [Entity]Frame([Entity]Controller controller) {
        this.controller = controller;
        initComponents();
    }

    private void initComponents() {
        // Build UI components (JLabel, JTextField, JButton, JTable)
        saveButton.addActionListener(e -> {
            try {
                controller.handleSave(nameField.getText(), priceField.getText());
                JOptionPane.showMessageDialog(this, "Record saved");
            } catch (RuntimeException ex) {
                JOptionPane.showMessageDialog(this, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        });
    }
}
```

#### Web — JSP/Servlet Pattern

```java
// ⚠️ MANDATORY IMPORTS — The AI MUST include the exception classes
// import [Package].exception.[CustomException];
// import [Package].exception.ServiceException;

// ✅ PATTERN — Servlet acts as Controller, JSP is the View
@WebServlet("/[entity]")
public class [Entity]Servlet extends HttpServlet {

    private [Entity]Controller controller;

    @Override
    public void init() {
        // ⚠️ Initialize database schema FIRST
        DatabaseInitializer.initialize();

        [Entity]Dao dao = new [Entity]Dao();
        [Entity]Service service = new [Entity]Service(dao);
        this.controller = new [Entity]Controller(service);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        try {
            controller.handleSave(req.getParameter("name"), req.getParameter("price"));
            req.setAttribute("message", "Record saved");
        } catch (RuntimeException e) {
            req.setAttribute("error", e.getMessage());
        }
        req.getRequestDispatcher("/[entity].jsp").forward(req, resp);
    }
}
```

| Rule | Constraint |
|------|-----------|
| View responsibility | **ONLY** render data and capture user input |
| Business logic in View | ❌ **FORBIDDEN** — no validations, no calculations, no SQL |
| View → Service | ❌ **FORBIDDEN** — always go through Controller |
| Error handling | View catches `RuntimeException` from Controller and displays message to user |
| Dependency construction | Build chain in View's constructor or `init()`: `DAO → Service → Controller` |
| DB initialization | Call `DatabaseInitializer.initialize()` **BEFORE** building the dependency chain |

---

## 3. Package Structure

```
src/main/java/com/project/
├── view/           # (OPTIONAL) UI layer — JavaFX, Swing, or JSP/Servlet
├── controller/     # Entry points (receives user input, delegates to Service)
├── service/        # Business rules + transaction management (setAutoCommit/commit/rollback)
├── dao/            # Pure JDBC data access (receives Connection, NEVER manages transactions)
├── model/          # Domain entities and DTOs
├── exception/      # Custom business exceptions (extends RuntimeException)
├── util/           # Shared utilities
└── config/         # ConnectionFactory, app configuration

src/main/resources/
└── sql/            # Database schemas and DDL migration scripts

src/main/java/com/project/view/   # ← Only if UI is needed
├── [Entity]View.java             # JavaFX Application class
├── [Entity]Frame.java            # OR Swing JFrame class
└── components/                   # Reusable UI components (optional)

src/main/webapp/                  # ← Only for web (JSP/Servlet)
├── WEB-INF/web.xml
└── [entity].jsp

src/test/java/com/project/
├── service/        # Business rule tests (PRIMARY — one test class per spec)
├── dao/            # Integration tests with H2 (SECONDARY)
└── util/           # Utility tests
```

---

## 4. Test Generation Rules

> **The AI MUST generate one test method per Acceptance Criterion (Scenario) in the spec.**

| Scenario Type | Test Pattern | Assertion |
|--------------|-------------|-----------|
| Happy Path | Arrange valid data → Act → Assert result | `assertNotNull`, `assertEquals` |
| Validation Failure | Arrange invalid data → Act → Assert exception | `assertThrows` + `getMessage()` |
| Boundary / Edge Case | Arrange boundary value → Act → Assert boundary behavior | `assertEquals`, `assertTrue` |
| Duplicate Detection | Arrange existing data → Act → Assert conflict exception | `assertThrows` |
| Precondition Not Met | Arrange incomplete state → Act → Assert rejection | `assertThrows` |
| Calculation | Arrange inputs → Act → Assert computed result | `assertEquals(expected, actual, delta)` |
| State Transition | Arrange initial state → Act → Assert new state | `assertEquals` on state field |
| Cascade / Dependency | Arrange parent+child → Act → Assert both affected | Multiple `assertNotNull` / `assertEquals` |

### 4.1 Mockito Mocking Rules — MANDATORY for Service Tests

> **The AI MUST use Mockito to mock DAO dependencies in ALL service tests.**
> **Failing to handle checked exceptions from mocked DAO methods causes compilation errors.**

```java
// ✅ MANDATORY TEST CLASS STRUCTURE
@ExtendWith(MockitoExtension.class)
@DisplayName("[Entity]Service — Business Rule Tests")
class [Entity]ServiceTest {

    @Mock
    private [Entity]Dao [entity]Dao;       // Mocked — never hits a real DB

    @InjectMocks
    private [Entity]Service [entity]Service; // SUT receives the mock via constructor

    // ─── RULE 1: ALL test methods MUST declare `throws Exception` ───
    @Test
    void shouldDoSomething() throws Exception { ... }

    // ─── RULE 2: Wrap DAO mock setup in try-catch(SQLException) ───
    //     DAO methods declare `throws SQLException`. Even though they are
    //     mocked, the compiler enforces the checked exception contract.
    @Test
    void exampleWithMockSetup() throws Exception {
        // GIVEN
        [Entity] entity = new [Entity](1, "Test", "test@mail.com");

        try {
            doReturn(entity).when([entity]Dao).findById(any(), eq(1));
        } catch (SQLException e) {
            fail("Mock setup should not throw");
        }

        // WHEN
        [Entity] result = [entity]Service.findById(1);

        // THEN
        assertNotNull(result);
    }

    // ─── RULE 3: Use helper methods for service calls inside assertThrows ───
    //     When calling service methods that internally catch SQLException
    //     from the DAO, route through a helper that declares `throws SQLException`.
    private [Entity] callCreate([Entity] entity) throws SQLException {
        return [entity]Service.create(entity);
    }

    private [Entity] callUpdate([Entity] entity) throws SQLException {
        return [entity]Service.update(entity);
    }

    @Test
    void shouldThrowWhenFieldIsNull() throws Exception {
        [Entity] invalid = new [Entity]();
        invalid.setName(null);

        try {
            doReturn(null).when([entity]Dao).findByName(any(), eq(null));
        } catch (SQLException e) {
            fail("Mock setup should not throw");
        }

        assertThrows([CustomException].class, () -> callCreate(invalid));
    }
}
```

| Rule | Constraint |
|------|-----------|
| Class annotation | `@ExtendWith(MockitoExtension.class)` — **MANDATORY** |
| DAO field | `@Mock` annotated — never instantiate a real DAO in service tests |
| Service field | `@InjectMocks` — Mockito injects the mock DAO automatically |
| Test signatures | **ALL** must declare `throws Exception` |
| Mock DAO setup | Wrap `doReturn().when()` in `try { ... } catch (SQLException)` |
| Service calls in lambdas | Route through `private` helper methods that declare `throws SQLException` |
| Braces | **NEVER** leave orphan `}` — verify class structure closes exactly once |

---

## 5. Conventional Commits — SDD Aligned

The commit history MUST reflect the SDD lifecycle per feature:

```
1. docs(spec):       add spec for [feature]
2. feat(model):      add [Entity] domain class
3. feat(db):         add SQL DDL schema for [Entity]
4. feat(exception):  add [CustomException] class
5. test(feature):    add failing tests for [feature] business rules
6. feat(dao):        implement [Entity]Dao with JDBC
7. feat(service):    implement [Entity]Service with business rules
8. feat(controller): implement [Entity]Controller
9. feat(view):       implement [Entity]View/Frame (if UI applies)
10. refactor:         [optional cleanup]
```

---

## 6. Violation Policy

| Severity | Violation |
|----------|-----------|
| 🔴 BLOCKER | Using an ORM or prohibited framework |
| 🔴 BLOCKER | Generating production code without tests first |
| 🔴 BLOCKER | DAO managing transactions |
| 🔴 BLOCKER | View calling Service or DAO directly (skipping Controller) |
| 🔴 BLOCKER | Using `'''` in `toString()` instead of `+ "'"` (causes compilation error) |
| 🟡 MAJOR | Not using `try-with-resources` |
| 🟡 MAJOR | Business logic (validations, calculations, SQL) inside View |
| 🟡 MAJOR | Service without constructor injection for DAO |
| 🟠 MINOR | Non-English technical code or comments |

---

> **This constitution is the law. The AI must follow every rule without exception.**
