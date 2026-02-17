# Project Code Style & Best Practices Guide

This document consolidates coding standards, conventions, and best practices established from code review sessions to help developers write consistent, secure, and maintainable code.

---

## Table of Contents

1. [Backend (Java/Quarkus)](#backend-javaquarkus)
2. [Common Mistakes to Avoid](#common-mistakes-to-avoid)

---

## Backend (Java/Quarkus)

### Exception Handling

#### DO: Use custom domain exceptions
```java
// Create specific exception
public class BadRequestException extends RuntimeException {
    public BadRequestException(String message) {
        super(message);
    }
}

// Map only that exception
@Provider
public class BadRequestExceptionMapper implements ExceptionMapper<BadRequestException> {
    @Override
    public Response toResponse(BadRequestException exception) {
        String message = exception.getMessage() != null 
            ? exception.getMessage() 
            : "Bad request";
        return Response.status(Response.Status.BAD_REQUEST)
                .type(MediaType.APPLICATION_JSON)
                .entity(Map.of("error", message))
                .build();
    }
}
```

**Why**: Mapping generic exceptions (like `IllegalArgumentException`) globally can turn server bugs into misleading 400 responses.

#### DON'T: Map generic JDK exceptions globally
```java
// ❌ BAD - too broad, catches library/framework exceptions too
@Provider
public class IllegalArgumentExceptionMapper 
    implements ExceptionMapper<IllegalArgumentException> { }
```

#### DO: Make exception mappers null-safe
```java
// ❌ BAD - throws NPE if message is null
.entity(Map.of("error", exception.getMessage()))

// ✅ GOOD - null-safe default
String message = exception.getMessage() != null 
    ? exception.getMessage() 
    : "An error occurred";
.entity(Map.of("error", message))
```

### REST Resources

#### DO: Return typed responses, not generic `Response`
```java
// ❌ BAD
public Uni<Response> getMovie(@PathParam("id") UUID id) {
    return movieService.getMovie(id)
            .map(movie -> Response.ok(movie).build());
}

// ✅ GOOD
public Uni<MovieDTO> getMovie(@PathParam("id") UUID id) {
    return movieService.getMovie(id)
            .map(MovieDTO::from);
}
```

**Why**: Better type safety, clearer API contracts, automatic JSON serialization.

#### DO: Use DTOs for responses
```java
// ❌ BAD - returning entity directly
return Response.ok(movieEntity).build();

// ✅ GOOD - use DTO
return Response.ok(MovieDTO.from(movieEntity)).build();
```

**Why**: Entities may contain lazy-loaded associations or sensitive fields.

#### DO: Use text blocks and named parameters for JPQL
```java
// ❌ BAD - hard to read, positional params
return list("select s from Showtime s left join fetch s.movie " +
            "where s.movie.id = ?1 and s.startTime >= ?2", 
            movieId, startOfDay);

// ✅ GOOD - text blocks with named parameters
return list("""
        select s
        from Showtime s
        left join fetch s.movie
        where s.movie.id = :movieId
          and s.startTime >= :startOfDay
        """,
        Parameters.with("movieId", movieId)
                .and("startOfDay", startOfDay)
);
```

### Validation

#### DO: Apply Hibernate Validator to entities and DTOs too with @Valid if that's request payload
```java
@Entity
public class Showtime extends PanacheEntityBase {
    
    @NotNull(message = "Movie is required")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "movie_id", nullable = false)
    public Movie movie;
    
    @NotBlank(message = "Theater name is required")
    @Column(name = "theater_name", nullable = false)
    public String theaterName;
    
    @NotNull(message = "Available seats is required")
    @PositiveOrZero(message = "Available seats must be non-negative")
    @Column(name = "available_seats", nullable = false)
    public Integer availableSeats;
}
```

**Use appropriate annotations:**
- `@NotNull` - for required non-null fields
- `@NotBlank` - for required non-empty strings
- `@NotEmpty` - for required collections
- `@PositiveOrZero` - for non-negative numbers (semantically clearer than `@Min(0)`)
- `@Positive` - for strictly positive numbers
- `@Min`/`@Max` - for numeric ranges

### JSON Serialization

#### DO: Use `Map.of()` or DTOs for JSON responses
```java
// ❌ BAD - JSON injection risk
.entity("{\"error\":\"" + exception.getMessage() + "\"}")

// ✅ GOOD - safe serialization
.entity(Map.of("error", exception.getMessage()))

// ✅ GOOD - use DTO
.entity(new ErrorResponse(exception.getMessage()))
```

#### DO: Set `Content-Type` header
```java
return Response.status(Response.Status.NOT_FOUND)
        .type(MediaType.APPLICATION_JSON)  // Always set content type
        .entity(Map.of("error", message))
        .build();
```

### Date/Time Handling

#### DO: Use consistent timezone (UTC)
```java
// ❌ BAD - system default timezone
if (date.equals(LocalDate.now())) {
    
// ✅ GOOD - explicit UTC
if (date.equals(LocalDate.now(ZoneOffset.UTC))) {
```

#### DO: Use `var` for local variables
```java
// ✅ GOOD - let compiler infer types
var startOfDay = date.atStartOfDay().toInstant(ZoneOffset.UTC);
var endOfDay = date.plusDays(1).atStartOfDay().toInstant(ZoneOffset.UTC);
var isToday = date.equals(LocalDate.now(ZoneOffset.UTC));
```

**When to use `var`:**
- Local variables with obvious types
- When type is clear from context
- In stream operations

**When NOT to use `var`:**
- Public API signatures
- When type isn't obvious
- For primitives where specific type matters (int vs long)

---

## Common Mistakes to Avoid

### Backend

1. **Don't map generic exceptions globally**
   - Creates misleading HTTP status codes
   - Can expose internal errors
   - Use custom domain exceptions

2. **Don't return entities directly in REST responses**
   - Use DTOs to control exposed data
   - Prevents lazy loading issues
   - Better separation of concerns

3. **Don't concatenate strings for JSON**
   - Use `Map.of()` or DTOs
   - Prevents JSON injection

4. **Don't forget `Content-Type` headers**
   - Always set for JSON responses

5. **Don't forget null-safety in exception mappers**
   - `getMessage()` can return null
   - Provide sensible defaults

6. **Don't use system default timezone for server logic**
   - Use UTC consistently
   - Be explicit about timezones

7. **Don't ignore Hibernate validation**
   - Add `@NotNull`, `@NotBlank`, etc. to entities
   - Use appropriate constraint annotations
   - Set `nullable = false` in `@Column` annotations

### General

1. **Don't leave dead code**
   - Remove unreachable null checks
   - Keep code clean and maintainable

2. **Don't ignore code review feedback**
   - Address all comments
   - Ask for clarification when needed

---

## Checklist Before Submitting PR

- [ ] Custom exceptions used instead of generic ones
- [ ] Exception mappers are null-safe
- [ ] DTOs used for REST responses
- [ ] Text blocks with named parameters for JPQL
- [ ] Hibernate validators applied to entities and DTOs with @Validated of need
- [ ] `Content-Type` headers set for JSON responses
- [ ] UTC used consistently for date/time
- [ ] All tests passing

---

## Resources

- [Quarkus REST Reference](https://quarkus.io/guides/rest)
- [Hibernate Validator](https://hibernate.org/validator/)
