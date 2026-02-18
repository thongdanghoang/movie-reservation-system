# Generic Java Coding Best Practices

A structured repository for creating and maintaining Java coding best practices optimized for agents and LLMs.

## Structure

- `rules/` - Individual rule files (one per rule)
    - `_sections.md` - Section metadata (titles, impacts, descriptions)
    - `_template.md` - Template for creating new rules
    - `area-description.md` - Individual rule files
- `metadata.json` - Document metadata (version, organization, abstract)
- __`AGENTS.md`__ - Compiled output with all rules

## Creating a New Rule

1. Copy `rules/_template.md` to `rules/area-description.md`
2. Choose the appropriate area prefix:
    - `general-` for General Programming (Section 1)
    - `exception-` for Exception Handling (Section 2)
    - `resource-` for Resources Management (Section 3)
    - `design-` for Design (Section 4)
    - `transaction-` for Transaction Management (Section 5)
    - `performance-` for Performance (Section 6)
    - `security-` for Security (Section 7)
3. Fill in the frontmatter and content
4. Ensure you have clear examples with explanations
5. Update AGENTS.md with the new rule

## Rule File Structure

Each rule file should follow this structure:

```markdown
---
title: Rule Title Here
impact: MEDIUM
impactDescription: Optional description
tags: tag1, tag2, tag3
---

## Rule Title Here

**Problem**: Description of the problem

**Type**: Reliability | Functionality | Maintainability | Efficiency

**Severity**: Critical | Serious | Non critical

**Bad code / behavior:**

```java
// Bad code example
```

**Description:**

Explanation of why it's problematic.

**Good code / behavior:**

```java
// Good code example
```

**Comment:**

Optional notes.

Reference: [Link](https://example.com)

## File Naming Convention

- Files starting with `_` are special (excluded from build)
- Rule files: `area-description.md` (e.g., `general-for-each-loop.md`)
- Section is automatically inferred from filename prefix
- Rules are sorted by their BP code (e.g., BP201, BP301)

## Severity Levels

- `Critical` - Highest priority, causes crashes or data loss
- `Serious` - Significant issues, affects functionality or reliability
- `Non critical` - Minor issues, affects maintainability

## Impact Levels

- `CRITICAL` - Highest priority, prevents major bugs
- `HIGH` - Significant quality/reliability improvements
- `MEDIUM` - Moderate improvements
- `LOW` - Incremental improvements

## Rule Types

- `Reliability` - Affects system stability and correctness
- `Functionality` - Affects expected behavior
- `Maintainability` - Affects code readability and maintenance
- `Efficiency` - Affects resource usage and performance

## Contributing

When adding or modifying rules:

1. Use the correct filename prefix for your section
2. Follow the `_template.md` structure
3. Include clear bad/good examples with explanations
4. Add appropriate tags and severity
5. Update AGENTS.md to include the new rule
