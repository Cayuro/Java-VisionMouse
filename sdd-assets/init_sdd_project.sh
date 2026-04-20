#!/bin/bash

# =============================================================================
# 🚀 SDD Project Initializer for Java SE + Maven
# =============================================================================
#
# Usage:
#   chmod +x init_sdd_project.sh
#   ./init_sdd_project.sh <project-name> <group-id> [db-type] [ui-type]
#
# Example:
#   ./init_sdd_project.sh mi-tienda com.tienda postgresql
#   ./init_sdd_project.sh sistema-notas com.notas mysql javafx
#   ./init_sdd_project.sh demo-app com.demo h2 swing
#   ./init_sdd_project.sh mi-web com.web postgresql web
#
# =============================================================================

set -e

# ─── Validate arguments ───────────────────────────────────────────────────────
if [ $# -lt 2 ]; then
    echo ""
    echo "╔══════════════════════════════════════════════════════════════════╗"
    echo "║   🚀 SDD Project Initializer — Java SE + Maven                 ║"
    echo "╠══════════════════════════════════════════════════════════════════╣"
    echo "║                                                                ║"
    echo "║  Usage:                                                        ║"
    echo "║    ./init_sdd_project.sh <project-name> <group-id> [db] [ui]   ║"
    echo "║                                                                ║"
    echo "║  Arguments:                                                    ║"
    echo "║    project-name  : Folder and artifactId (e.g. my-app)         ║"
    echo "║    group-id      : Java package (e.g. com.mycompany)           ║"
    echo "║    db (optional) : postgresql | mysql | h2 (default: h2)       ║"
    echo "║    ui (optional) : javafx | swing | web | none (default: none) ║"
    echo "║                                                                ║"
    echo "║  Examples:                                                     ║"
    echo "║    ./init_sdd_project.sh my-store com.store postgresql         ║"
    echo "║    ./init_sdd_project.sh my-app com.app mysql javafx           ║"
    echo "║    ./init_sdd_project.sh my-web com.web postgresql web         ║"
    echo "║                                                                ║"
    echo "╚══════════════════════════════════════════════════════════════════╝"
    echo ""
    exit 1
fi

PROJECT_NAME="$1"
GROUP_ID="$2"
DB_TYPE="${3:-h2}"
UI_TYPE="${4:-none}"
PACKAGE_PATH=$(echo "$GROUP_ID" | tr '.' '/')

# ─── Validate UI type ─────────────────────────────────────────────────────────
case "$UI_TYPE" in
    none|javafx|swing|web) ;;
    *)
        echo "❌ Error: Unknown UI type '$UI_TYPE'. Use: javafx, swing, web, or none"
        exit 1
        ;;
esac

echo ""
echo "══════════════════════════════════════════════════════"
echo "  🚀 Creating SDD project: $PROJECT_NAME"
echo "  📦 Package: $GROUP_ID"
echo "  🗄️  Database: $DB_TYPE"
if [ "$UI_TYPE" != "none" ]; then
    echo "  🖥️  UI: $UI_TYPE"
fi
echo "══════════════════════════════════════════════════════"
echo ""

# ─── Create project directory ─────────────────────────────────────────────────
if [ -d "$PROJECT_NAME" ]; then
    echo "❌ Error: Directory '$PROJECT_NAME' already exists."
    exit 1
fi

mkdir -p "$PROJECT_NAME"
cd "$PROJECT_NAME"

# ─── Create package directories ──────────────────────────────────────────────
echo "📁 Creating directory structure..."

mkdir -p "src/main/java/${PACKAGE_PATH}"/{config,model,exception,dao,service,controller,util}
mkdir -p "src/test/java/${PACKAGE_PATH}"/{service,dao}
mkdir -p "src/main/resources/sql"
mkdir -p "src/test/resources"
mkdir -p "sdd_assets"

# Create view directory only if UI is requested
if [ "$UI_TYPE" != "none" ]; then
    mkdir -p "src/main/java/${PACKAGE_PATH}/view"
fi
if [ "$UI_TYPE" = "web" ]; then
    mkdir -p "src/main/webapp/WEB-INF"
fi

echo "   ✅ Directories created"

# ─── Determine JDBC dependency ───────────────────────────────────────────────
case "$DB_TYPE" in
    postgresql|postgres|pg)
        DB_DEPENDENCY='        <!-- PostgreSQL JDBC Driver -->
        <dependency>
            <groupId>org.postgresql</groupId>
            <artifactId>postgresql</artifactId>
            <version>42.7.3</version>
        </dependency>'
        DB_URL="jdbc:postgresql://localhost:5432/${PROJECT_NAME//-/_}"
        DB_USER="postgres"
        DB_PASSWORD="password"
        DB_DRIVER="org.postgresql.Driver"
        DB_TYPE_LABEL="PostgreSQL"
        ;;
    mysql)
        DB_DEPENDENCY='        <!-- MySQL JDBC Driver -->
        <dependency>
            <groupId>com.mysql</groupId>
            <artifactId>mysql-connector-j</artifactId>
            <version>8.3.0</version>
        </dependency>'
        DB_URL="jdbc:mysql://localhost:3306/${PROJECT_NAME//-/_}"
        DB_USER="root"
        DB_PASSWORD="password"
        DB_DRIVER="com.mysql.cj.jdbc.Driver"
        DB_TYPE_LABEL="MySQL"
        ;;
    h2)
        DB_DEPENDENCY='        <!-- H2 JDBC Driver (runtime — used as production DB) -->
        <dependency>
            <groupId>com.h2database</groupId>
            <artifactId>h2</artifactId>
            <version>2.2.224</version>
        </dependency>'
        DB_URL="jdbc:h2:file:./data/${PROJECT_NAME//-/_}"
        DB_USER="sa"
        DB_PASSWORD=""
        DB_DRIVER="org.h2.Driver"
        DB_TYPE_LABEL="H2 (File)"
        ;;
    *)
        echo "❌ Error: Unknown DB type '$DB_TYPE'. Use: postgresql, mysql, or h2"
        exit 1
        ;;
esac

# ─── Determine UI dependency and plugin ───────────────────────────────────────
UI_DEPENDENCY=""
UI_PLUGIN=""
UI_TYPE_LABEL="None (CLI)"
PACKAGING="jar"

case "$UI_TYPE" in
    javafx)
        UI_DEPENDENCY='
        <!-- ═══ JAVAFX ═══ -->
        <dependency>
            <groupId>org.openjfx</groupId>
            <artifactId>javafx-controls</artifactId>
            <version>21.0.2</version>
        </dependency>
        <dependency>
            <groupId>org.openjfx</groupId>
            <artifactId>javafx-fxml</artifactId>
            <version>21.0.2</version>
        </dependency>'
        UI_PLUGIN='
            <plugin>
                <groupId>org.openjfx</groupId>
                <artifactId>javafx-maven-plugin</artifactId>
                <version>0.0.8</version>
                <configuration>
                    <mainClass>'${GROUP_ID}'.view.MainView</mainClass>
                </configuration>
            </plugin>'
        UI_TYPE_LABEL="JavaFX"
        ;;
    swing)
        UI_DEPENDENCY='        <!-- Swing is part of JDK — no extra dependency needed -->'
        UI_TYPE_LABEL="Swing"
        ;;
    web)
        UI_DEPENDENCY='
        <!-- ═══ WEB (Servlet API) ═══ -->
        <dependency>
            <groupId>jakarta.servlet</groupId>
            <artifactId>jakarta.servlet-api</artifactId>
            <version>6.0.0</version>
            <scope>provided</scope>
        </dependency>
        <dependency>
            <groupId>jakarta.servlet.jsp</groupId>
            <artifactId>jakarta.servlet.jsp-api</artifactId>
            <version>3.1.1</version>
            <scope>provided</scope>
        </dependency>'
        UI_PLUGIN='
            <plugin>
                <groupId>org.apache.maven.plugins</groupId>
                <artifactId>maven-war-plugin</artifactId>
                <version>3.4.0</version>
            </plugin>'
        UI_TYPE_LABEL="Web (JSP/Servlet)"
        PACKAGING="war"
        ;;
esac

# ─── Generate pom.xml ────────────────────────────────────────────────────────
echo "📄 Generating pom.xml..."

cat > pom.xml << POMEOF
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>

    <groupId>${GROUP_ID}</groupId>
    <artifactId>${PROJECT_NAME}</artifactId>
    <version>1.0-SNAPSHOT</version>
    <packaging>${PACKAGING}</packaging>

    <name>${PROJECT_NAME}</name>
    <description>Java SE project with SDD methodology</description>

    <properties>
        <maven.compiler.source>17</maven.compiler.source>
        <maven.compiler.target>17</maven.compiler.target>
        <project.build.sourceEncoding>UTF-8</project.build.sourceEncoding>
        <junit.version>5.10.2</junit.version>
        <mockito.version>5.11.0</mockito.version>
    </properties>

    <dependencies>
        <!-- ═══ TESTING ═══ -->
        <dependency>
            <groupId>org.junit.jupiter</groupId>
            <artifactId>junit-jupiter</artifactId>
            <version>\${junit.version}</version>
            <scope>test</scope>
        </dependency>
        <dependency>
            <groupId>org.mockito</groupId>
            <artifactId>mockito-core</artifactId>
            <version>\${mockito.version}</version>
            <scope>test</scope>
        </dependency>
        <dependency>
            <groupId>org.mockito</groupId>
            <artifactId>mockito-junit-jupiter</artifactId>
            <version>\${mockito.version}</version>
            <scope>test</scope>
        </dependency>

        <!-- ═══ DATABASE ═══ -->
${DB_DEPENDENCY}

        <!-- H2 — In-memory DB for unit/integration tests -->
        <dependency>
            <groupId>com.h2database</groupId>
            <artifactId>h2</artifactId>
            <version>2.2.224</version>
            <scope>test</scope>
        </dependency>
${UI_DEPENDENCY}
    </dependencies>

    <build>
        <plugins>
            <plugin>
                <groupId>org.apache.maven.plugins</groupId>
                <artifactId>maven-surefire-plugin</artifactId>
                <version>3.2.5</version>
            </plugin>
            <plugin>
                <groupId>org.apache.maven.plugins</groupId>
                <artifactId>maven-compiler-plugin</artifactId>
                <version>3.12.1</version>
                <configuration>
                    <source>17</source>
                    <target>17</target>
                </configuration>
            </plugin>${UI_PLUGIN}
        </plugins>
    </build>
</project>
POMEOF

echo "   ✅ pom.xml created (${DB_TYPE_LABEL})"

# ─── Generate ConnectionFactory.java ─────────────────────────────────────────
echo "📄 Generating ConnectionFactory.java..."

cat > "src/main/java/${PACKAGE_PATH}/config/ConnectionFactory.java" << CONNEOF
package ${GROUP_ID}.config;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Centralized database connection provider.
 *
 * <p>Production connections use ${DB_TYPE_LABEL}.
 * Test connections use H2 in-memory database.</p>
 *
 * <p>Note: Refer to CONSTITUTION.md §2.2 — Transaction Management</p>
 */
public class ConnectionFactory {

    // ═══ PRODUCTION DATABASE ═══
    private static final String URL = "${DB_URL}";
    private static final String USER = "${DB_USER}";
    private static final String PASSWORD = "${DB_PASSWORD}";

    // ═══ TEST DATABASE (H2 in-memory) ═══
    private static final String TEST_URL = "jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1";
    private static final String TEST_USER = "sa";
    private static final String TEST_PASSWORD = "";

    private ConnectionFactory() {
        // Utility class — prevent instantiation
    }

    /**
     * Returns a connection to the production database.
     *
     * @return a new {@link Connection} instance
     * @throws SQLException if connection cannot be established
     */
    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }

    /**
     * Returns a connection to the H2 in-memory test database.
     * Use this in JUnit test classes to avoid depending on external DB.
     *
     * @return a new {@link Connection} to H2 in-memory DB
     * @throws SQLException if connection cannot be established
     */
    public static Connection getTestConnection() throws SQLException {
        return DriverManager.getConnection(TEST_URL, TEST_USER, TEST_PASSWORD);
    }
}
CONNEOF

echo "   ✅ ConnectionFactory.java created"

# ─── Generate DatabaseInitializer.java ───────────────────────────────────────
echo "📄 Generating DatabaseInitializer.java..."

cat > "src/main/java/${PACKAGE_PATH}/config/DatabaseInitializer.java" << 'DBIEOF'
package ${GROUP_ID}.config;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.Statement;
import java.util.stream.Collectors;

/**
 * Automatic database schema initializer.
 *
 * <p>Reads and executes all SQL scripts located in {@code src/main/resources/sql/}
 * to ensure database tables exist before the application starts querying.</p>
 *
 * <p>Usage: Call {@link #initialize()} once at application startup
 * (e.g., in the View constructor, Controller init, or main method).</p>
 *
 * <p>Note: Refer to CONSTITUTION.md §2.1 — Architecture</p>
 */
public class DatabaseInitializer {

    private DatabaseInitializer() {
        // Utility class — prevent instantiation
    }

    /**
     * Executes all SQL scripts found in the {@code sql/} resource directory.
     * Uses {@code IF NOT EXISTS} semantics — safe to call multiple times.
     *
     * <p>Call this method ONCE at application startup:</p>
     * <pre>
     *   DatabaseInitializer.initialize();
     * </pre>
     */
    public static void initialize() {
        String[] scripts = discoverScripts();
        if (scripts.length == 0) {
            System.out.println("⚠️  No SQL scripts found in resources/sql/");
            return;
        }

        try (Connection conn = ConnectionFactory.getConnection();
             Statement stmt = conn.createStatement()) {

            for (String scriptName : scripts) {
                String sql = readScript("sql/" + scriptName);
                if (sql != null && !sql.isBlank()) {
                    stmt.execute(sql);
                    System.out.println("   ✅ Executed: " + scriptName);
                }
            }
        } catch (Exception e) {
            System.err.println("❌ Database initialization failed: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Discovers SQL script filenames in the {@code sql/} resource directory.
     */
    private static String[] discoverScripts() {
        try (InputStream is = DatabaseInitializer.class.getClassLoader()
                .getResourceAsStream("sql");
             BufferedReader reader = is != null
                     ? new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8))
                     : null) {

            if (reader == null) return new String[0];

            return reader.lines()
                    .filter(name -> name.endsWith(".sql"))
                    .sorted()
                    .toArray(String[]::new);
        } catch (Exception e) {
            return new String[0];
        }
    }

    /**
     * Reads the full content of a SQL script from the classpath.
     */
    private static String readScript(String resourcePath) {
        try (InputStream is = DatabaseInitializer.class.getClassLoader()
                .getResourceAsStream(resourcePath)) {
            if (is == null) return null;
            return new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8))
                    .lines()
                    .collect(Collectors.joining("\n"));
        } catch (Exception e) {
            System.err.println("❌ Could not read: " + resourcePath);
            return null;
        }
    }
}
DBIEOF

# Replace package placeholder with actual group ID
sed -i "s/package \${GROUP_ID}/package ${GROUP_ID}/" "src/main/java/${PACKAGE_PATH}/config/DatabaseInitializer.java"

echo "   ✅ DatabaseInitializer.java created"

# ─── Generate ServiceException.java (base exception) ────────────────────────
echo "📄 Generating ServiceException.java..."

cat > "src/main/java/${PACKAGE_PATH}/exception/ServiceException.java" << EXCEOF
package ${GROUP_ID}.exception;

/**
 * Base exception for service-layer errors.
 *
 * <p>All custom business exceptions should extend {@link RuntimeException}
 * directly (not this class), keeping each exception specific to its
 * business rule. This exception is reserved for unexpected infrastructure
 * errors during service operations (e.g., failed commit/rollback).</p>
 *
 * <p>Note: Refer to CONSTITUTION.md §2.4 — Custom Exception Pattern</p>
 */
public class ServiceException extends RuntimeException {

    public ServiceException(String message) {
        super(message);
    }

    public ServiceException(String message, Throwable cause) {
        super(message, cause);
    }
}
EXCEOF

echo "   ✅ ServiceException.java created"

# ─── Generate UI base files (if applicable) ──────────────────────────────────
if [ "$UI_TYPE" = "javafx" ]; then
    echo "📄 Generating JavaFX MainView.java..."

    cat > "src/main/java/${PACKAGE_PATH}/view/MainView.java" << JFXEOF
package ${GROUP_ID}.view;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

import ${GROUP_ID}.config.DatabaseInitializer;

/**
 * Main entry point for the JavaFX desktop application.
 *
 * <p>This View layer ONLY renders UI and captures user input.
 * All logic is delegated to Controller classes.</p>
 *
 * <p>Note: Refer to CONSTITUTION.md §2.6 — Presentation Layer</p>
 */
public class MainView extends Application {

    // TODO: Inject your Controller here
    // private final [Entity]Controller controller;

    @Override
    public void start(Stage stage) {
        // Initialize database schema
        DatabaseInitializer.initialize();

        Label label = new Label("🚀 ${PROJECT_NAME} — SDD + JavaFX");
        label.setStyle("-fx-font-size: 24px; -fx-font-weight: bold;");

        StackPane root = new StackPane(label);
        Scene scene = new Scene(root, 800, 600);

        stage.setTitle("${PROJECT_NAME}");
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
JFXEOF

    echo "   ✅ MainView.java created (JavaFX)"

elif [ "$UI_TYPE" = "swing" ]; then
    echo "📄 Generating Swing MainFrame.java..."

    cat > "src/main/java/${PACKAGE_PATH}/view/MainFrame.java" << SWEOF
package ${GROUP_ID}.view;

import javax.swing.*;
import java.awt.*;

import ${GROUP_ID}.config.DatabaseInitializer;

/**
 * Main entry point for the Swing desktop application.
 *
 * <p>This View layer ONLY renders UI and captures user input.
 * All logic is delegated to Controller classes.</p>
 *
 * <p>Note: Refer to CONSTITUTION.md §2.6 — Presentation Layer</p>
 */
public class MainFrame extends JFrame {

    // TODO: Inject your Controller here
    // private final [Entity]Controller controller;

    public MainFrame() {
        setTitle("${PROJECT_NAME}");
        setSize(800, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        initComponents();
    }

    private void initComponents() {
        JPanel mainPanel = new JPanel(new BorderLayout());

        JLabel titleLabel = new JLabel("🚀 ${PROJECT_NAME} — SDD + Swing", SwingConstants.CENTER);
        titleLabel.setFont(new Font("SansSerif", Font.BOLD, 24));
        mainPanel.add(titleLabel, BorderLayout.CENTER);

        setContentPane(mainPanel);
    }

    public static void main(String[] args) {
        // Initialize database schema
        DatabaseInitializer.initialize();

        SwingUtilities.invokeLater(() -> {
            MainFrame frame = new MainFrame();
            frame.setVisible(true);
        });
    }
}
SWEOF

    echo "   ✅ MainFrame.java created (Swing)"

elif [ "$UI_TYPE" = "web" ]; then
    echo "📄 Generating Web files (Servlet + JSP)..."

    cat > "src/main/java/${PACKAGE_PATH}/view/HomeServlet.java" << SRVEOF
package ${GROUP_ID}.view;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

import ${GROUP_ID}.config.DatabaseInitializer;

/**
 * Home page servlet — entry point for the web application.
 *
 * <p>This Servlet acts as a thin adapter between HTTP and the Controller layer.
 * All business logic is delegated to Controller classes.</p>
 *
 * <p>Note: Refer to CONSTITUTION.md §2.6 — Presentation Layer</p>
 */
@WebServlet("/")
public class HomeServlet extends HttpServlet {

    // TODO: Inject your Controller here
    // private [Entity]Controller controller;

    @Override
    public void init() throws ServletException {
        // Initialize database schema
        DatabaseInitializer.initialize();

        // TODO: Build dependency chain
        // [Entity]Dao dao = new [Entity]Dao();
        // [Entity]Service service = new [Entity]Service(dao);
        // this.controller = new [Entity]Controller(service);
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        req.setAttribute("projectName", "${PROJECT_NAME}");
        req.getRequestDispatcher("/index.jsp").forward(req, resp);
    }
}
SRVEOF

    cat > "src/main/webapp/index.jsp" << JSPEOF
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>\${projectName} — SDD</title>
    <style>
        body {
            font-family: 'Segoe UI', system-ui, sans-serif;
            display: flex;
            justify-content: center;
            align-items: center;
            min-height: 100vh;
            margin: 0;
            background: #1a1a2e;
            color: #eee;
        }
        h1 { font-size: 2.5rem; }
    </style>
</head>
<body>
    <h1>🚀 \${projectName} — SDD + Web</h1>
</body>
</html>
JSPEOF

    cat > "src/main/webapp/WEB-INF/web.xml" << WEBEOF
<?xml version="1.0" encoding="UTF-8"?>
<web-app xmlns="https://jakarta.ee/xml/ns/jakartaee"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="https://jakarta.ee/xml/ns/jakartaee
         https://jakarta.ee/xml/ns/jakartaee/web-app_6_0.xsd"
         version="6.0">

    <display-name>${PROJECT_NAME}</display-name>

    <welcome-file-list>
        <welcome-file>index.jsp</welcome-file>
    </welcome-file-list>
</web-app>
WEBEOF

    echo "   ✅ HomeServlet.java + index.jsp + web.xml created (Web)"
fi

# ─── Generate .gitignore ─────────────────────────────────────────────────────
echo "📄 Generating .gitignore..."

cat > .gitignore << 'GIEOF'
# ═══ Build ═══
target/
build/
*.class
*.jar
*.war

# ═══ IDE ═══
.idea/
*.iml
.vscode/
.settings/
.project
.classpath
*.swp
*~

# ═══ OS ═══
.DS_Store
Thumbs.db

# ═══ Database ═══
data/
*.db
*.mv.db
*.trace.db

# ═══ Logs ═══
*.log

# ═══ Environment ═══
.env
GIEOF

echo "   ✅ .gitignore created"

# ─── Generate README.md ──────────────────────────────────────────────────────
echo "📄 Generating README.md..."

UI_TREE_ENTRY=""
if [ "$UI_TYPE" != "none" ]; then
    UI_TREE_ENTRY="├── view/           UI layer (${UI_TYPE_LABEL})
"
fi

cat > README.md << READMEEOF
# ${PROJECT_NAME}

> Java SE project built with **Specification-Driven Development (SDD)**.

## Tech Stack

| Component | Technology |
|-----------|-----------|
| Language | Java 17+ |
| Build | Maven |
| Testing | JUnit 5 |
| Database | ${DB_TYPE_LABEL} (Production) / H2 (Tests) |
| DB Access | Pure JDBC |
| UI | ${UI_TYPE_LABEL} |

## Quick Start

\`\`\`bash
# Compile
mvn compile

# Run tests
mvn test

# Package
mvn package
\`\`\`

## SDD Workflow

1. Copy \`sdd_assets/SPEC_TEMPLATE.md\` → \`sdd_assets/SPEC_[Feature].md\`
2. Fill out Business Rules and BDD Scenarios
3. Feed \`CONSTITUTION.md\` + filled spec to AI
4. Receive generated code: SQL → Exception → Model → Tests → DAO → Service → Controller → View
5. Run \`mvn test\` → verify all pass
6. Commit following Conventional Commits (see CONSTITUTION.md §5)

## Project Structure

\`\`\`
src/main/resources/
└── sql/            Database schemas and migration scripts

src/main/java/${PACKAGE_PATH}/
${UI_TREE_ENTRY}├── controller/     Entry points
├── service/        Business logic + transactions
├── dao/            JDBC data access
├── model/          Domain entities
├── exception/      Business exceptions
├── config/         ConnectionFactory
└── util/           Shared utilities
\`\`\`
READMEEOF

echo "   ✅ README.md created"

# ─── Copy SDD assets if available in parent ──────────────────────────────────
SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"

echo "📄 Setting up SDD assets..."

if [ -f "${SCRIPT_DIR}/CONSTITUTION.md" ]; then
    cp "${SCRIPT_DIR}/CONSTITUTION.md" sdd_assets/
    
    # Inject active DB and UI frameworks into the specific project's Constitution
    sed -i "1s/^/> 💡 **ACTIVE DATABASE FOR THIS PROJECT:** ${DB_TYPE_LABEL}\n\n/" sdd_assets/CONSTITUTION.md
    if [ "$UI_TYPE" != "none" ]; then
        sed -i "2s/^/> 💡 **ACTIVE UI FRAMEWORK FOR THIS PROJECT:** ${UI_TYPE_LABEL}\n\n/" sdd_assets/CONSTITUTION.md
    fi

    cp "${SCRIPT_DIR}/SPEC_TEMPLATE.md" sdd_assets/ 2>/dev/null || true
    cp "${SCRIPT_DIR}/BusinessRuleSpecTemplateTest.java" sdd_assets/ 2>/dev/null || true
    cp "${SCRIPT_DIR}/GUIA_DE_USO.md" sdd_assets/ 2>/dev/null || true
    cp "${SCRIPT_DIR}/GUIA_DEDUCCION_SPECS.md" sdd_assets/ 2>/dev/null || true
    echo "   ✅ SDD assets copied from source directory"
else
    echo "   ⚠️  SDD assets not found in script directory."
    echo "      Copy CONSTITUTION.md, SPEC_TEMPLATE.md, and"
    echo "      BusinessRuleSpecTemplateTest.java into sdd_assets/ manually."
fi

# ─── Initialize Git ──────────────────────────────────────────────────────────
echo "📄 Initializing Git..."

git init -q
git add .
git commit -q -m "chore: initialize ${PROJECT_NAME} with SDD project structure"

echo "   ✅ Git initialized with initial commit"

# ─── Validate with Maven ─────────────────────────────────────────────────────
echo ""
echo "🔍 Validating project with Maven..."
echo ""

if mvn compile -q 2>/dev/null; then
    echo "   ✅ Maven compile: SUCCESS"
else
    echo "   ⚠️  Maven compile had issues (likely missing DB driver — expected if DB not installed)"
fi

# ─── Summary ─────────────────────────────────────────────────────────────────
echo ""
echo "══════════════════════════════════════════════════════"
echo "  ✅ Project '$PROJECT_NAME' created successfully!"
echo "══════════════════════════════════════════════════════"
echo ""
echo "  📂 Location:  $(pwd)"
echo "  📦 Package:   ${GROUP_ID}"
echo "  🗄️  Database:  ${DB_TYPE_LABEL}"
if [ "$UI_TYPE" != "none" ]; then
    echo "  🖥️  UI:        ${UI_TYPE_LABEL}"
fi
echo ""
echo "  Next steps:"
echo "    cd ${PROJECT_NAME}"
echo "    cp sdd_assets/SPEC_TEMPLATE.md sdd_assets/SPEC_MyFeature.md"
echo "    # Fill the spec, then feed it + CONSTITUTION.md to the AI"
if [ "$UI_TYPE" = "javafx" ]; then
    echo ""
    echo "  Run JavaFX app:"
    echo "    mvn javafx:run"
elif [ "$UI_TYPE" = "swing" ]; then
    echo ""
    echo "  Run Swing app:"
    echo "    mvn compile exec:java -Dexec.mainClass=\"${GROUP_ID}.view.MainFrame\""
elif [ "$UI_TYPE" = "web" ]; then
    echo ""
    echo "  Build WAR:"
    echo "    mvn package"
    echo "    # Deploy target/${PROJECT_NAME}-1.0-SNAPSHOT.war to Tomcat/Jetty"
fi
echo ""
echo "══════════════════════════════════════════════════════"
echo ""
