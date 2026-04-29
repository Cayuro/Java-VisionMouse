# TODO - Fix GestureRecognizer Tests

Status: [IN PROGRESS]

## Steps from Approved Plan

### 1. [DONE] Create this TODO.md
### 2. [DONE] Update GestureRecognizer with State enum (compile OK, tests partial)
### 3. Diagnosed builder override bug → Fix HandLandmarksTestBuilder to preserve set points
### 4. Run `mvn test -Dtest=**/*GestureRecognizerTest` → expect 0 failures
### 5. Update TODO step 4 DONE
### 6. Full `mvn test` for other tests
### 7. Complete

**Root cause:** Builder.setupNaturalHandLayout() overrides user-set pinch positions → distance large → no CLICK.

**Fix:** Add isSet array, skip set if already set.

**Current:** 12 failures → after fix 0.


