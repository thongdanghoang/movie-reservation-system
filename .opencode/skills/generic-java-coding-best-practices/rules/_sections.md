# Sections

This file defines all sections, their ordering, impact levels, and descriptions.
The section ID (in parentheses) is the filename prefix used to group rules.

---

## 1. General Programming (general)

**Impact:** CRITICAL  
**Description:** Core programming practices that prevent common bugs and improve code readability. Covers loops,
comparisons, variable scope, and constants.

## 2. Exception Handling (exception)

**Impact:** CRITICAL  
**Description:** Proper exception handling makes code more robust, debuggable, and maintainable. Prevents silent
failures and provides actionable feedback.

## 3. Resources Management (resource)

**Impact:** HIGH  
**Description:** Java resources (files, streams, connections) must be properly managed to prevent leaks and ensure
system stability.

## 4. Design (design)

**Impact:** HIGH  
**Description:** Good design practices lead to maintainable, extensible, and testable code. Covers stateless components,
final keyword, and API design.

## 5. Transaction Management (transaction)

**Impact:** HIGH  
**Description:** Proper transaction handling ensures data consistency and integrity. Covers DDL statements, rollback
behavior, and transaction boundaries.

## 6. Performance (performance)

**Impact:** MEDIUM  
**Description:** Performance optimizations should be applied judiciously after profiling. Premature optimization can
harm readability.

## 7. Security (security)

**Impact:** HIGH  
**Description:** Security practices prevent vulnerabilities and protect sensitive data. Covers input validation,
authentication, and secure coding.
