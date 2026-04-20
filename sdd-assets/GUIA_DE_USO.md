# 🗺️ Guía de Uso — SDD Boilerplate Assets

> **Objetivo:** Aprender a usar los 3 insumos (`CONSTITUTION.md`, `SPEC_TEMPLATE.md`, `BusinessRuleSpecTemplateTest.java`) para desarrollar features de Java SE con asistencia de IA, siguiendo el flujo Specification-Driven Development.

---

## 📦 ¿Qué tienes?

| Archivo | Rol | ¿Cuándo lo usas? |
|---------|-----|-------------------|
| `init_sdd_project.sh` | **El generador de proyecto** — crea todo el scaffolding Maven en un comando | Una sola vez al inicio del proyecto |
| `CONSTITUTION.md` | **Las reglas del juego** — la IA lo lee para saber QUÉ patrones seguir | Siempre. Se adjunta en cada prompt a la IA |
| `SPEC_TEMPLATE.md` | **La plantilla de pedido** — tú la llenas describiendo QUÉ quieres | Una copia por cada feature nueva |
| `BusinessRuleSpecTemplateTest.java` | **El molde de tests** — la IA lo usa para saber CÓMO estructurar los tests | Referencia. La IA lo replica internamente |
| `GUIA_DEDUCCION_SPECS.md` | **La guía de deducción** — te enseña a extraer specs desde un enunciado | Estudio. Léela antes de llenar tu primera SPEC |

---

## 📍 ¿Dónde viven estos archivos?

Esta carpeta `sdd_assests/` es tu **toolkit permanente** — nunca la borras ni la mueves. Desde aquí generas nuevos proyectos.

```
📂 sdd_assests/                         ← 🔒 TU TOOLKIT (fuente maestra)
├── init_sdd_project.sh                 ← Ejecutas desde aquí
├── CONSTITUTION.md                     ← Se copia al proyecto nuevo
├── SPEC_TEMPLATE.md                    ← Se copia al proyecto nuevo
├── BusinessRuleSpecTemplateTest.java   ← Se copia al proyecto nuevo
├── GUIA_DEDUCCION_SPECS.md             ← Se copia al proyecto nuevo
└── GUIA_DE_USO.md                      ← Tu referencia (no se copia)
```

Cuando ejecutas el script, se crea un **proyecto nuevo independiente** con sus propias copias:

```
📂 MisProyectos/
└── 📂 mi-tienda/                       ← PROYECTO GENERADO
    ├── pom.xml                         ← Generado por el script
    ├── README.md
    ├── .gitignore
    ├── sdd_assets/                     ← Copias para usar con la IA
    │   ├── CONSTITUTION.md
    │   ├── SPEC_TEMPLATE.md
    │   └── BusinessRuleSpecTemplateTest.java
    └── src/
        ├── main/java/com/tienda/
        │   ├── config/ConnectionFactory.java
        │   ├── exception/ServiceException.java
        │   ├── model/ dao/ service/ controller/ view/
        ├── main/resources/sql/         ← Aquí irán las tablas generadas
        └── test/java/com/tienda/service/
```

> **Regla simple:** El toolkit genera proyectos. Dentro de cada proyecto trabajas con las copias locales.

---

## 🔄 Flujo Completo (Paso a Paso)

---

### Fase 0: Prerequisitos (UNA SOLA VEZ por proyecto)

> **Antes de tocar cualquiera de los 3 insumos SDD, necesitas tener esto listo.**

#### 0.1 — Software Instalado

| Herramienta | Versión mínima | Verificación |
|-------------|---------------|-------------|
| **JDK** | 17+ (LTS) | `java --version` |
| **Maven** | 3.8+ | `mvn --version` |
| **Git** | 2.x | `git --version` |
| **IDE** | IntelliJ / VS Code + Java Pack / Eclipse | Que soporte JUnit 5 |
| **Motor de BD** | PostgreSQL, MySQL, o H2 | `psql --version` o equivalente |

#### 0.2 — Crear Proyecto (Automático ⚡)

Usa el script `init_sdd_project.sh` para generar TODO en un solo comando.
**Navega primero a donde quieres crear el proyecto**, luego ejecuta el script con su ruta completa:

```bash
# 1. Ve a la carpeta donde quieres crear tu nuevo proyecto
cd ~/Documentos/MisProyectos/

# 2. Ejecuta el script usando su ruta completa
#    Sintaxis: <ruta-al-script>/init_sdd_project.sh <nombre> <paquete> [bd] [ui]

# Ejemplos:
~/ruta/a/sdd_assests/init_sdd_project.sh mi-tienda com.tienda postgresql
~/ruta/a/sdd_assests/init_sdd_project.sh sistema-notas com.notas mysql javafx
~/ruta/a/sdd_assests/init_sdd_project.sh demo-app com.demo h2 swing
~/ruta/a/sdd_assests/init_sdd_project.sh mi-web com.web postgresql web
```

> **💡 Tip:** Para no escribir la ruta completa cada vez, puedes crear un alias en tu terminal:
> ```bash
> # Agrega esto a tu ~/.bashrc o ~/.zshrc
> alias sdd-init='~/ruta/a/sdd_assests/init_sdd_project.sh'
>
> # Después solo usas:
> sdd-init mi-tienda com.tienda postgresql
> sdd-init mi-app com.app mysql javafx
> ```

**El script genera automáticamente:**

| Archivo generado | Qué contiene |
|-----------------|-------------|
| `pom.xml` | Java 17, JUnit 5, driver JDBC (según tu elección), H2 para tests, Surefire + dependencias UI |
| `ConnectionFactory.java` | Conexión producción + conexión H2 tests |
| `ServiceException.java` | Excepción base para errores de infraestructura |
| `MainView.java` / `MainFrame.java` / `HomeServlet.java` | Archivo base de la UI (solo si pasas el 4to parámetro) |
| `.gitignore` | Reglas para Java, IDE, BD, logs |
| `README.md` | Descripción del proyecto con instrucciones |
| `sdd_assets/` | Copia de CONSTITUTION, SPEC_TEMPLATE y BusinessRuleSpecTemplateTest |
| Estructura completa | Todas las carpetas por capa (model, dao, service, etc.) |
| Git | Repositorio inicializado con commit inicial |

#### 0.3 — Crear Proyecto (Manual — alternativa)

<details>
<summary>Haz clic aquí solo si NO puedes usar el script</summary>

```bash
# Crear directorios
mkdir -p mi-proyecto/src/main/java/com/project/{config,model,exception,dao,service,controller,util}
mkdir -p mi-proyecto/src/test/java/com/project/service
```

Necesitarás crear manualmente:
- `pom.xml` con JUnit 5 + driver JDBC + H2 + Surefire plugin
- `ConnectionFactory.java` en `config/`
- `ServiceException.java` en `exception/`
- `.gitignore`
- Copiar los 3 insumos SDD a `sdd_assets/`
- `git init` + commit inicial

> Revisa el contenido del script `init_sdd_project.sh` como referencia de qué debe tener cada archivo.

</details>

#### 0.4 — Base de Datos Lista (si usas PostgreSQL o MySQL)

```bash
# PostgreSQL
sudo -u postgres createdb mi_tienda
# MySQL
mysql -u root -p -e "CREATE DATABASE mi_tienda;"
```

> Si usas **H2**, no necesitas este paso — la BD se crea en memoria automáticamente durante los tests.

#### 0.5 — Verificar que todo funciona

```bash
cd mi-proyecto
mvn compile     # ← debe decir BUILD SUCCESS
mvn test        # ← debe decir BUILD SUCCESS (0 tests por ahora)
```

> **✅ Todo listo → Pasa a la Fase 1.**

---

### Fase 1: Preparar la Especificación (TÚ — sin código)

```
📝 Tiempo estimado: 10-20 minutos
```

**Paso 1.** Copia `SPEC_TEMPLATE.md` y renómbrala:
```bash
cp SPEC_TEMPLATE.md SPEC_UserRegistration.md
```

**Paso 2.** Abre tu copia y llena SOLO estas secciones (sin pensar en código):

| Sección | Qué escribir | Ejemplo |
|---------|-------------|---------|
| **Business Goal** | Quién, qué necesita, para qué | *"As a new user, I need to register an account, so that I can access the platform"* |
| **Hard Business Rules** | Reglas que el sistema DEBE cumplir | `BR-001: Email must not be null or empty` |
| **Acceptance Criteria** | Escenarios Given-When-Then | *"Given a user with null email, When register() is called, Then throw InvalidUserException"* |
| **Technical Notes** | Nombre de clases y campos | `User.java` con campos: `id`, `name`, `email`, `password` |

**Paso 3.** Elimina los escenarios que NO aplican a tu feature.
> Si tu feature solo tiene 4 reglas de negocio, deja solo 4-5 escenarios (happy path + una validación por regla). No dejes los 10 escenarios si no los necesitas.

---

### Fase 2: Pedir Código a la IA (TÚ → IA)

```
📝 Tiempo estimado: 2-5 minutos de prompt
```

**Paso 4.** Abre una conversación con la IA y adjunta DOS archivos como contexto:

```
📎 Adjuntar: CONSTITUTION.md
📎 Adjuntar: SPEC_UserRegistration.md (tu spec ya llena)

Prompt:
"Siguiendo la CONSTITUTION.md adjunta y la especificación SPEC_UserRegistration.md,
genera todo el código en el orden establecido en la Sección 1 (AI Workflow).

Asegúrate de incluir la Integración UI en el archivo principal (Paso 11).
⚠️ Para el Paso 11, proporciona el archivo Main COMPLETO, con todos sus imports y reglas de Layout actualizadas, sin duplicar métodos ni cortar pedazos de código.
Usa el paquete com.project."
```

> **💡 Tip:** La CONSTITUTION ya le dice a la IA exactamente qué hacer. No necesitas explicar patrones, transacciones ni try-with-resources — todo eso está en las reglas.

**Paso 5.** La IA generará los archivos en este orden:

```
1. SQL DDL                       ← Genera tabla en resources/sql/
2. InvalidUserException.java     ← Exception
3. User.java                     ← Model (POJO)
4. UserServiceTest.java          ← Tests (10 o menos, según tus escenarios)
5. UserDao.java                  ← DAO (JDBC puro)
6. UserService.java              ← Service (reglas + transacciones)
7. UserController.java           ← Controller (valida input de UI)
8. UserView.java                 ← View (solo si llenaste la sección UI)
9. MainView/MainFrame/index.jsp  ← ARCHIVO ACTUALIZADO (Paso 11: Integration)
```

---

### Fase 3: Verificar (TÚ — ejecutar y validar)

```
📝 Tiempo estimado: 5-10 minutos
```

**Paso 6.** Coloca los archivos generados en la estructura de paquetes:
```
src/main/resources/sql/
└── user_table.sql

src/main/java/com/project/
├── exception/InvalidUserException.java
├── model/User.java
├── dao/UserDao.java
├── service/UserService.java
└── controller/UserController.java

src/test/java/com/project/
└── service/UserServiceTest.java
```

**Paso 7.** Ejecuta los tests:
```bash
mvn test
# o
gradle test
```

**Paso 8.** Verifica que:

| ✅ Check | Qué revisar |
|----------|------------|
| Tests pasan | `BUILD SUCCESS` en la terminal |
| Arquitectura correcta | DAO **no** tiene `setAutoCommit` ni `commit` |
| try-with-resources | Todo `PreparedStatement` y `ResultSet` está en `try(...)` |
| Transacciones en Service | `setAutoCommit(false)` → `commit()` → `rollback()` solo en Service |
| Constructor injection | Service recibe DAO por constructor, no lo crea internamente |

---

### Fase 4: Commit (TÚ — versionamiento)

**Paso 9.** Haz commits siguiendo el orden SDD:

```bash
git add SPEC_UserRegistration.md
git commit -m "docs(spec): add spec for user registration"

git add src/main/resources/sql/ src/main/java/com/project/exception/ src/main/java/com/project/model/
git commit -m "feat(registration): add User model, InvalidUserException and SQL schema"

git add src/test/
git commit -m "test(registration): add business rule tests for user registration"

git add src/main/java/com/project/dao/ src/main/java/com/project/service/
git commit -m "feat(registration): implement UserDao and UserService"
```

---

## ⚡ Resumen Rápido (Cheat Sheet)

```
┌──────────────────────────────────────────────────────────────────────┐
│                   TU FLUJO DE TRABAJO                                │
│                                                                      │
│  0. ./init_sdd_project.sh mi-app com.app postgresql [javafx]  ⚡     │
│  1. cp SPEC_TEMPLATE.md → SPEC_[Feature].md                          │
│  2. Llena: Business Rules + Scenarios (Given-When-Then)              │
│  3. Adjunta CONSTITUTION + SPEC llena a la IA                        │
│  4. Prompt: "Genera código siguiendo el AI Workflow"                 │
│  5. Recibe: SQL → Exception → Model → Tests → DAO → Service → View   │
│  6. mvn test → verde ✅                                              │
│  7. Commits en orden SDD (docs → test → feat)                        │
│  8. Repite desde el paso 1 para la siguiente feature                 │
└──────────────────────────────────────────────────────────────────────┘
```

---

## 🚫 Errores Comunes

| ❌ Error | ✅ Solución |
|----------|-----------|
| Dejar los 10 escenarios sin llenar | Elimina los que no aplican. Menos escenarios vacíos = mejor output de la IA |
| No adjuntar CONSTITUTION al prompt | Sin ella, la IA no sabe las reglas. **Siempre adjúntala** |
| Pedir solo "genera el Service" | Pide el flujo completo. La IA necesita generar el test ANTES del service |
| Editar el código sin actualizar la spec | Si cambia una regla de negocio, actualiza primero la spec, luego regenera |
| Escribir código antes de la spec | Frena. Llena la spec primero. Ese es el principio #1 de SDD |

---

## 🔁 ¿Y si necesito modificar una feature existente?

1. Abre la spec existente (`SPEC_[Feature].md`)
2. Agrega/modifica las Business Rules y los Scenarios afectados
3. Adjunta CONSTITUTION + spec actualizada a la IA
4. Prompt: *"La spec fue actualizada. Regenera SOLO los tests y el service para los cambios marcados."*
5. Ejecuta tests, valida, commit con `fix()` o `feat()`

---

## 📐 ¿Y si mi feature es muy simple (1-2 reglas)?

Usa una spec mínima. Ejemplo de una spec con solo 2 escenarios:

```markdown
## 2. Hard Business Rules
| Rule ID | Rule Description | Error Behavior |
|---------|-----------------|----------------|
| BR-001  | Product name must not be empty | Throw InvalidProductException |

## 3. Acceptance Criteria
### Scenario 1 — Happy Path
Given a valid Product with name "Laptop"
When  the service method save() is called
Then  the product is persisted successfully

### Scenario 2 — Null Name
Given a Product with null name
When  the service method save() is called
Then  InvalidProductException is thrown
```

> Eso es suficiente. La IA generará 2 tests, no 10. **Menos es más cuando las reglas son pocas.**

---

> **Recuerda:** Estos archivos son tus herramientas, no tus jefes. Adapta la cantidad de escenarios a la complejidad real de cada feature.
