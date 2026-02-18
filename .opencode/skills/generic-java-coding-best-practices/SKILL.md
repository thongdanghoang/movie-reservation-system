---
name: generic-java-coding-best-practices
description: Java coding best practices for writing clean, maintainable, and reliable code. This skill should be used when writing, reviewing, or refactoring Java code. Triggers on tasks involving Java classes, methods, exception handling, resources, transactions, or code quality improvements.
license: MIT
metadata:
  author: community
  version: "1.0.0"
---

# Generic Java Coding Best Practices

Comprehensive coding standards and best practices for Java applications. Contains rules across multiple categories,
prioritized by impact to guide automated refactoring and code generation.

## When to Apply

Reference these guidelines when:

- Writing new Java classes or methods
- Implementing exception handling
- Managing resources (streams, connections, sessions)
- Designing class hierarchies and APIs
- Working with transactions
- Optimizing performance
- Implementing security measures

## Rule Categories by Priority

| Priority | Category               | Impact   | Prefix         |
|----------|------------------------|----------|----------------|
| 1        | General Programming    | CRITICAL | `general-`     |
| 2        | Exception Handling     | CRITICAL | `exception-`   |
| 3        | Resources Management   | HIGH     | `resource-`    |
| 4        | Design                 | HIGH     | `design-`      |
| 5        | Transaction Management | HIGH     | `transaction-` |
| 6        | Performance            | MEDIUM   | `performance-` |
| 7        | Security               | HIGH     | `security-`    |

## Quick Reference

### 1. General Programming (CRITICAL)

- `general-for-each-loop` - Prefer for-each to traditional loops
- `general-avoid-assert` - Do not use Assert except for unit tests
- `general-todo-comments` - Be careful of TODO leftovers
- `general-equals-comparison` - Use .equals() instead of ==
- `general-equals-constants` - Invoke equals() on constants first
- `general-variable-scope` - Minimize scope of local variables
- `general-hardcoded-values` - Avoid hard-coded values

### 2. Exception Handling (CRITICAL)

- `exception-empty-catch` - Never leave empty catch block
- `exception-specific-throw` - Be specific when throwing exception
- `exception-failure-info` - Include information about failure
- `exception-finally-catch` - Always put try..catch in finally
- `exception-log-severity` - Log exception with appropriate severity

### 3. Resources Management (HIGH)

- `resource-close-all` - All resources must be closed after used
- `resource-lazy-init` - Don't do lazy initialization unless needed
- `resource-double-check` - Use double-check idiom for lazy-init in threads

### 4. Design (HIGH)

- `design-stateless` - Never store information in stateless components
- `design-final-keyword` - Mark classes/methods as final if not extensible
- `design-empty-collection` - Prefer empty list/array over null
- `design-constants-vs-config` - Constants and configurable parameters differ
- `design-enums` - Use enums instead of integer constants

### 5. Transaction Management (HIGH)

- `transaction-ddl-autocommit` - DDL statements are auto-committed

### 6. Performance (MEDIUM)

- Performance rules for optimization

### 7. Security (HIGH)

- Security rules for safe coding

## How to Use

Read individual rule files for detailed explanations and code examples:

```
rules/general-for-each-loop.md
rules/exception-empty-catch.md
```

Each rule file contains:

- Brief explanation of why it matters
- Incorrect code example with explanation
- Correct code example with explanation
- Additional context and comments

## Full Compiled Document

For the complete guide with all rules expanded: `AGENTS.md`
