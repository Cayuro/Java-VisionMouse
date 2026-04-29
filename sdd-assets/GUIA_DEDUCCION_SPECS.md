# 🔍 Guía de Deducción de Especificaciones — Del Enunciado al SPEC

> **Objetivo:** Aprender a traducir una idea de negocio, un enunciado académico o un problema real en especificaciones (`SPEC_[Feature].md`) precisas y listas para alimentar a la IA.

---

## 🧠 Mentalidad Clave

> **"No leas el enunciado como una historia. Léelo como un arquitecto que busca las piezas del rompecabezas."**

La habilidad que dominarás aquí es la **ingeniería de requisitos**: traducir una necesidad narrativa en componentes lógicos y reglas verificables.

La pregunta guía siempre es:

> *"¿Qué es lo mínimo que debe pasar para que esta operación sea considerada **exitosa y segura**?"*

La respuesta a esa pregunta son tus especificaciones de negocio.

---

## 📋 El Proceso en 5 Pasos

```
┌─────────────────────────────────────────────────────────────────┐
│                  FLUJO DE DEDUCCIÓN                             │
│                                                                 │
│  📖 Enunciado                                                   │
│    ↓                                                            │
│  1️⃣  Identificar Entidades (sustantivos principales)            │
│    ↓                                                            │
│  2️⃣  Extraer Reglas de Negocio (restricciones y validaciones)   │
│    ↓                                                            │
│  3️⃣  Detectar Flujos Transaccionales (operaciones atómicas)     │
│    ↓                                                            │
│  4️⃣  Separar Negocio vs. Infraestructura (qué vs. cómo)        │
│    ↓                                                            │
│  5️⃣  Definir Criterios de Aceptación (escenarios BDD)           │
│    ↓                                                            │
│  📋 SPEC_[Feature].md lista para la IA                          │
└─────────────────────────────────────────────────────────────────┘
```

---

## Paso 1: Identificación de Entidades (Agrupación Lógica)

### ¿Qué hacer?

No veas el enunciado como una lista interminable. Agrúpalo por **entidades** — los sustantivos principales del texto.

### Técnica

Subraya los **sustantivos clave** en el enunciado:

| Lo que lees | Entidad que identificas |
|-------------|------------------------|
| *"El restaurante registra **platos** en su menú"* | `Plato` |
| *"Los **clientes** hacen reservaciones"* | `Cliente` |
| *"Cada **reservación** tiene fecha y número de personas"* | `Reservacion` |
| *"Las **mesas** tienen capacidad máxima"* | `Mesa` |

### La Regla de Oro

> Si una entidad tiene **sus propias reglas de validación** y probablemente **una tabla en la base de datos**, merece su propia `SPEC_[Entidad].md`.

### Ejemplo Práctico

**Enunciado:**
> *"Se necesita un sistema para un restaurante que gestione platos del menú, clientes y reservaciones. Los platos tienen nombre, precio y categoría. Los clientes tienen nombre, teléfono y email. Las reservaciones se asignan a una mesa disponible."*

**Resultado:**

```
📋 SPEC_Plato.md         → Entidad con nombre, precio, categoría
📋 SPEC_Cliente.md       → Entidad con nombre, teléfono, email
📋 SPEC_Reservacion.md   → Flujo transaccional entre Cliente y Mesa
```

---

## Paso 2: Extracción de Reglas de Negocio (BR)

### ¿Qué hacer?

Separar lo que el sistema **hace** de lo que el sistema **prohíbe o exige**.

### Técnica: Buscar Palabras Clave

| Palabra clave en el enunciado | Tipo de regla | Ejemplo de BR |
|-------------------------------|---------------|---------------|
| *"único"*, *"no duplicado"* | Unicidad | `BR-001: Patient ID must be unique` |
| *"no puede"*, *"prohibido"* | Restricción | `BR-002: Cancelled order cannot be modified` |
| *"obligatorio"*, *"requerido"* | Validación null | `BR-003: Product name must not be null or empty` |
| *"solo si"*, *"siempre que"* | Precondición | `BR-004: Appointment requires doctor to be available` |
| *"validar"*, *"verificar"* | Formato | `BR-005: Phone must match format` |
| *"mayor que"*, *"mínimo"* | Rango | `BR-006: Quantity must be >= 1` |

### Ejemplo Práctico

**Enunciado:**
> *"El nombre del plato es obligatorio y no puede repetirse en el menú. El precio debe ser mayor que cero. Cada plato pertenece a una categoría."*

**Resultado en la SPEC:**

```markdown
## 2. Hard Business Rules

| Rule ID | Rule Description                         | Error Behavior                                 |
|---------|------------------------------------------|------------------------------------------------|
| BR-001  | Dish name must not be null or empty      | Throw InvalidDishException("Name required")    |
| BR-002  | Dish name must be unique in the menu     | Throw DuplicateDishException("Dish exists")    |
| BR-003  | Price must be greater than zero          | Throw InvalidDishException("Invalid price")    |
```

> **💡 Tip:** Cada regla de negocio implica **una excepción personalizada** y al menos **un test**. Si no puedes escribir un `assertThrows` para la regla, probablemente no es una regla de negocio real.

---

## Paso 3: Detección de Flujos Transaccionales

### ¿Qué hacer?

Identificar operaciones que **afectan más de una tabla o entidad** — estas requieren transaccionalidad.

### La Señal de Alerta

> Cuando una acción toca **dos o más tablas**, necesitas `setAutoCommit(false)` → `commit()` → `rollback()` en el Service.
> (Ver `CONSTITUTION.md §2.2`)

### Ejemplo Práctico

**Enunciado:**
> *"Cuando un cliente hace un pedido en la tienda online, se registra la orden y se descuenta el inventario de cada producto."*

**Análisis:**

```
Acción "Crear Pedido":
  1. INSERT INTO orden (...)                    ← Tabla Orden
  2. INSERT INTO detalle_orden (...)            ← Tabla DetalleOrden
  3. UPDATE producto SET stock = stock - qty    ← Tabla Producto
  
  → ¿Son 2+ tablas? SÍ → Operación transaccional obligatoria
```

**Resultado en la SPEC:**

```markdown
| BR-005 | Order creation must be atomic (insert order + insert details + update stock) | Rollback all on failure |
```

### Errores Comunes

| ❌ Error | ✅ Correcto |
|----------|------------|
| Tratar un pedido como un simple INSERT | Definirlo como operación atómica de múltiples pasos |
| Poner el `commit()` en el DAO | El `commit()` SOLO va en el Service (CONSTITUTION §2.2) |
| Ignorar el rollback si falla el descuento de stock | Definir BR explícita de atomicidad |

---

## Paso 4: Separación de Negocio vs. Infraestructura

### ¿Qué hacer?

La SPEC debe describir el **QUÉ** (la regla) y el **CÓMO** (el detalle técnico) por separado, para que el desarrollador sepa qué va en cada capa.

### Técnica: La Tabla de Dos Columnas

| Aspecto de Negocio (QUÉ) | Detalle de Infraestructura (CÓMO) |
|---------------------------|----------------------------------|
| *"La cita médica dura 30 minutos"* | Leer el valor desde `app.properties` |
| *"El email del paciente debe ser válido"* | Validar con regex en Service |
| *"No se pueden agendar citas en horario no laboral"* | Validar `hora >= 08:00 && hora <= 18:00` |
| *"Cada doctor tiene una especialidad"* | ENUM o campo String en el Model |

### Dónde va cada cosa en la SPEC

- **Reglas de Negocio (Sección 2)** → El QUÉ
- **Technical Notes (Sección 4)** → El CÓMO

### Ejemplo Práctico

**Enunciado:**
> *"El sistema de la clínica debe calcular el costo de la consulta. Si el paciente tiene seguro, se aplica un descuento del 20%."*

**En la SPEC:**

```markdown
## 2. Hard Business Rules
| CALC-001 | Consultation cost with insurance | basePrice * 0.80 |
| CALC-002 | Consultation cost without insurance | basePrice (no discount) |

## 4. Technical Notes
### 4.3 Configuration
- Insurance discount rate should be configurable via `app.properties`
- Key: `clinic.insurance.discount.rate=0.20`
```

---

## Paso 5: Definición de Criterios de Aceptación (BDD)

### ¿Qué hacer?

Para **cada regla de negocio (BR-XXX)**, imaginar el mejor y peor escenario.

### Técnica: Los 3 Escenarios Mínimos

| Tipo | Pregunta que te haces | Resultado |
|------|----------------------|-----------|
| **Happy Path** 🟢 | *¿Qué pasa si el usuario hace todo bien?* | El sistema completa la operación |
| **Validación** 🔴 | *¿Qué pasa si un campo es inválido?* | Se lanza una excepción específica |
| **Edge Case** 🟡 | *¿Qué pasa en la frontera? (stock = 0, mesa llena, horario límite)* | Comportamiento definido en la frontera |

### Ejemplo Práctico

**BR-004:** *"Solo se puede agendar cita si el doctor tiene disponibilidad en ese horario"*

```gherkin
### Scenario — Happy Path: Schedule appointment successfully
Given a Doctor with id = 1 and no appointments at 10:00
  And a Patient with id = 5
When  the service method scheduleAppointment() is called for 10:00
Then  the appointment is persisted successfully
  And the doctor's schedule is updated

### Scenario — Validation: Doctor not available
Given a Doctor with id = 1 who already has an appointment at 10:00
  And a Patient with id = 5
When  the service method scheduleAppointment() is called for 10:00
Then  a DoctorNotAvailableException is thrown with message "Doctor already has an appointment at this time"
  And no changes are persisted (rollback)

### Scenario — Edge Case: Outside business hours
Given a Doctor with id = 1
  And a Patient with id = 5
When  the service method scheduleAppointment() is called for 21:00
Then  an InvalidScheduleException is thrown with message "Appointments only between 08:00 and 18:00"
```

> 💡 **Resultado:** Cada escenario se convierte en un método `@Test` en tu código.

---

## 🎯 Resumen del Flujo de Deducción (Cheat Sheet)

```
┌──────────────────────────────────────────────────────────────────┐
│              CHECKLIST DE DEDUCCIÓN                              │
│                                                                  │
│  ✅  1. Subrayar Entidades     → Plato, Cliente, Reservación    │
│  ✅  2. Aislar Restricciones   → Nombre único, Precio > 0      │
│  ✅  3. Detectar Cálculos      → Descuentos, totales, impuestos │
│  ✅  4. Detectar Transacciones → ¿Toca 2+ tablas? → Atómica    │
│  ✅  5. Separar Negocio/Infra  → QUÉ (regla) vs. CÓMO (config) │
│  ✅  6. Definir Capas de Error → ¿Qué excepción lanzaré?       │
│  ✅  7. Escribir Escenarios    → Happy + Validación + Edge Case │
│                                                                  │
│  📋 Resultado: SPEC_[Feature].md lista para la IA               │
└──────────────────────────────────────────────────────────────────┘
```

---

## 🏋️ Ejercicio Práctico Guiado

Veamos el proceso completo con un enunciado real.

### Enunciado

> *"Desarrollar un módulo de gestión de empleados para una empresa de recursos humanos. Los empleados tienen nombre, cédula, cargo y salario. La cédula es obligatoria y no puede repetirse. El salario debe ser mayor o igual al salario mínimo vigente. Al registrar un empleado, si el cargo no existe en el catálogo, debe crearse automáticamente."*

### Paso 1 — Entidades

```
📋 SPEC_Empleado.md   → nombre, cédula, cargo, salario
📋 SPEC_Cargo.md      → nombre (puede que sea solo una tabla auxiliar)
```

> ⚠️ **Decisión:** ¿Es el cargo lo suficientemente complejo para su propia SPEC? Si solo es un `id + nombre`, puede ser parte de `SPEC_Empleado.md`. Si tiene sus propias reglas (nombre único, niveles jerárquicos, rangos salariales por cargo), merece su propia SPEC.

### Paso 2 — Reglas de Negocio

| Rule ID | Rule Description | Error Behavior |
|---------|-----------------|----------------|
| `BR-001` | Employee name must not be null or empty | Throw `InvalidEmployeeException("Name required")` |
| `BR-002` | Cédula must be unique | Throw `DuplicateEmployeeException("Cédula already registered")` |
| `BR-003` | Salary must be >= minimum wage | Throw `InvalidEmployeeException("Salary below minimum")` |
| `BR-004` | If position does not exist, create it automatically | Auto-insert position |

### Paso 3 — Flujos Transaccionales

```
BR-004 implica:
  1. SELECT cargo WHERE nombre = ?  (buscar si existe)
  2. INSERT INTO cargo (...)        (crear si no existe)
  3. INSERT INTO empleado (...)     (crear empleado)

  → 2 tablas → Transacción obligatoria
```

### Paso 4 — Negocio vs. Infraestructura

| Negocio | Infraestructura |
|---------|----------------|
| Cédula no puede repetirse | `SELECT COUNT(*) FROM empleado WHERE cedula = ?` |
| Salario >= mínimo | Leer valor mínimo desde `app.properties` |
| Cargo autocreado | Lógica en Service: `findByName()` → si null → `save()` |

### Paso 5 — Escenarios BDD

```gherkin
Scenario 1 — Happy Path
Given a valid Employee with name "Ana García", cédula "1234567890",
      position "Developer", salary 3500000
  And position "Developer" exists in the database
When  the service method save() is called
Then  the employee is persisted successfully

Scenario 2 — Null name
Given an Employee with name = null
When  save() is called
Then  InvalidEmployeeException is thrown

Scenario 3 — Duplicate cédula
Given an Employee with cédula "1234567890" that already exists
When  save() is called
Then  DuplicateEmployeeException is thrown

Scenario 4 — Salary below minimum wage
Given an Employee with salary = 500000 (below minimum wage of 1300000)
When  save() is called
Then  InvalidEmployeeException is thrown with message "Salary below minimum"

Scenario 5 — Auto-create position
Given an Employee with position "Data Analyst" that does NOT exist
When  save() is called
Then  the position is created automatically
  And the employee is persisted with the new position
```

---

## 🚫 Errores Comunes al Deducir Especificaciones

| ❌ Error | ✅ Solución |
|----------|------------|
| Mezclar reglas de varias entidades en una sola SPEC | Una SPEC por entidad/dominio lógico |
| Escribir reglas ambiguas: *"El nombre debe ser válido"* | Ser específico: *"Name must not be null, empty, or exceed 100 chars"* |
| Olvidar los edge cases | Preguntar siempre: *¿Qué pasa si el valor es 0? ¿Y null? ¿Y negativo?* |
| Poner detalles de UI en la SPEC | La SPEC es sobre lógica de negocio, no sobre botones o pantallas |
| No definir qué excepción se lanza | Cada BR debe tener un `Error Behavior` explícito |
| Asumir que un flujo es simple CRUD | Si toca 2 tablas → transacción. Si tiene reglas → no es CRUD |

---

## 💡 Consejo de Senior

> Siempre que leas un requerimiento, hazte estas 4 preguntas en orden:
>
> 1. **¿Quiénes son las entidades?** → Sustantivos del enunciado
> 2. **¿Qué puede salir mal?** → Cada respuesta es una regla de negocio
> 3. **¿Es una operación simple o compuesta?** → Si es compuesta → transacción
> 4. **¿Cómo sé que funciona?** → Cada respuesta es un escenario BDD
>
> Con estas 4 preguntas, puedes deducir una SPEC completa desde cualquier enunciado.

---

> **Siguiente paso:** Toma tu enunciado, aplica los 5 pasos de esta guía, y llena una `SPEC_[Feature].md` usando `SPEC_TEMPLATE.md`. Luego sigue el flujo de la `GUIA_DE_USO.md` para alimentar la IA.
