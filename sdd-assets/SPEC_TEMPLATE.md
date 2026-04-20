# 📋 SPECIFICATION: [Feature Name]

> **Instructions for the AI:**
> 1. The developer will duplicate this file and fill every `[placeholder]`.
> 2. Once filled, the developer feeds `CONSTITUTION.md` + this filled spec to the AI.
> 3. The AI generates code following the CONSTITUTION §1 workflow: Exception → Model → Tests → DAO → Service → Controller.

---

> **Spec ID:** `SPEC-[NNN]`
> **Author:** [Developer Name]
> **Date:** [YYYY-MM-DD]
> **Status:** [ ] Draft | [ ] Approved | [ ] Implemented

---

## 1. Business Goal

**As a** [type of user/actor],
**I need** [what capability],
**So that** [business value achieved].

---

## 2. Hard Business Rules

> _Each rule becomes a validation in the Service layer and a test method in the test class._

| Rule ID | Rule Description | Error Behavior |
|---------|-----------------|----------------|
| `BR-001` | [e.g., "[field] must not be null or empty"] | Throw `[CustomException]("[message]")` |
| `BR-002` | [e.g., "[field] must be greater than zero"] | Throw `[CustomException]("[message]")` |
| `BR-003` | [e.g., "[field] must be unique in the database"] | Throw `[CustomException]("[message]")` |
| `BR-004` | [e.g., "[process] requires [precondition] to be true"] | Throw `[CustomException]("[message]")` |
| `BR-005` | [e.g., "[field] must match format [regex/pattern]"] | Throw `[CustomException]("[message]")` |
| `BR-006` | [e.g., "[entity] cannot be [action] if [state condition]"] | Throw `[CustomException]("[message]")` |

### 2.1 Calculation Rules

| Calc ID | Description | Formula |
|---------|-------------|---------|
| `CALC-001` | [e.g., "Total price"] | `quantity * unitPrice` |
| `CALC-002` | [e.g., "Tax amount"] | `subtotal * taxRate` |

> _Remove this section if no calculations exist._

---

## 3. Acceptance Criteria (BDD)

> **AI Rule:** Generate exactly ONE test method per scenario below.
> Use `BusinessRuleSpecTemplateTest.java` as the structural pattern.

---

### Scenario 1 — Happy Path: Successful [operation]
> **Validates:** `BR-001`, `CALC-001`

```gherkin
Given a valid [Entity] with all required fields populated
  And [field] equals [valid value]
When  the service method [methodName]() is called
Then  the operation completes successfully
  And the returned [field] equals [expected value]
```

---

### Scenario 2 — Null/Empty Validation
> **Validates:** `BR-001`

```gherkin
Given an [Entity] where [field] is null
When  the service method [methodName]() is called
Then  a [CustomException] is thrown with message "[message]"
```

---

### Scenario 3 — Negative/Zero Validation
> **Validates:** `BR-002`

```gherkin
Given an [Entity] where [field] equals [0 or negative value]
When  the service method [methodName]() is called
Then  a [CustomException] is thrown with message "[message]"
```

---

### Scenario 4 — Duplicate Detection
> **Validates:** `BR-003`

```gherkin
Given an [Entity] with [field] that already exists in the database
When  the service method [methodName]() is called
Then  a [CustomException] is thrown with message "[message]"
```

---

### Scenario 5 — Precondition Not Met
> **Validates:** `BR-004`

```gherkin
Given a [process] where [precondition] is false
When  the service method [methodName]() is called
Then  a [CustomException] is thrown with message "[message]"
  And no changes are persisted to the database
```

---

### Scenario 6 — Format Validation
> **Validates:** `BR-005`

```gherkin
Given an [Entity] where [field] does not match [expected format]
When  the service method [methodName]() is called
Then  a [CustomException] is thrown with message "[message]"
```

---

### Scenario 7 — State Conflict
> **Validates:** `BR-006`

```gherkin
Given an [Entity] in state [current state]
When  the service method [methodName]() is called
Then  a [CustomException] is thrown with message "[message]"
```

---

### Scenario 8 — Calculation Verification
> **Validates:** `CALC-001`, `CALC-002`

```gherkin
Given an [Entity] with [input fields and values]
When  the service method [methodName]() is called
Then  [computed field] equals [expected result from formula]
```

---

### Scenario 9 — Boundary Value
> **Validates:** `BR-002`

```gherkin
Given an [Entity] where [field] equals [minimum/maximum valid value]
When  the service method [methodName]() is called
Then  the operation [succeeds/fails] as expected at the boundary
```

---

### Scenario 10 — Cascade / Dependency
> **Validates:** [applicable BR]

```gherkin
Given a [parent Entity] with related [child entities]
When  the service method [methodName]() is called on the parent
Then  [both parent and child are affected as expected]
```

> **Note to developer:** Remove or add scenarios as needed. Each Business Rule (BR-XXX) must have at least one corresponding scenario. If your feature has fewer rules, remove unused scenarios. If it has more, add new ones following this same format.

---

## 4. UI Requirements (Optional)

> **AI Rule:** If this section is filled, generate the corresponding `View` or `JSP` layer using the framework defined in `CONSTITUTION.md`.

| Element Type | Name/Label | Bound to Field | Action/Validation |
|--------------|------------|----------------|-------------------|
| Input Text   | [e.g. Name]| `[field name]` | Required (BR-001) |
| Input Number | [e.g. Price]| `[field name]`| Numeric (BR-003) |
| Button       | [e.g. Save]| N/A            | Calls `controller.save()` |
| Data Table   | [e.g. List]| `List<Entity>` | Displays all records |

> _Remove this section if the feature has no user interface._

---

## 4. Technical Notes

### 4.1 Artifacts to Generate

| Layer | Class Name | Key Responsibility |
|-------|-----------|-------------------|
| Exception | `[CustomException].java` | Business rule violation |
| Model | `[Entity].java` | Fields: [list all fields with types] |
| DAO | `[Entity]Dao.java` | JDBC operations: [list CRUD methods] |
| Service | `[Entity]Service.java` | Rule enforcement + transactions |
| Controller | `[Entity]Controller.java` | User input handling |
| Test | `[Entity]ServiceTest.java` | All scenarios above |

### 4.2 SQL Queries

```sql
-- Find by ID
SELECT [columns] FROM [table] WHERE id = ?;

-- Find by unique field (for duplicate check — BR-003)
SELECT COUNT(*) FROM [table] WHERE [field] = ?;

-- Insert
INSERT INTO [table] ([columns]) VALUES ([placeholders]);

-- Update
UPDATE [table] SET [columns = ?] WHERE id = ?;

-- Delete (if applicable)
DELETE FROM [table] WHERE id = ?;
```

### 4.3 Dependencies

- [ ] DB table `[table_name]` must exist
- [ ] `SPEC-[NNN]` — [dependency if applicable]

---

## 5. Out of Scope

- [Feature X — will be covered in SPEC-NNN]
- [UI design — frontend team]

---

## 6. Approval

| Role | Name | Date | ✓ |
|------|------|------|---|
| Tech Lead | | | [ ] |
| Product Owner | | | [ ] |

> **⚠️ No code generation until this spec is approved. See CONSTITUTION.md §1.**
