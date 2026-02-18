# Project Code Convention

## Table of Contents
1. [Dependency Injection](#dependency-injection)
2. [Lombok Usage](#lombok-usage)
3. [DTO Pattern](#dto-pattern)
4. [Mapping Strategy](#mapping-strategy)
5. [Transaction Management](#transaction-management)
6. [Validation](#validation)
7. [Logging](#logging)
8. [Secrets Management](#secrets-management)

---

## Dependency Injection

### Imperative (Blocking) Mode
Use Lombok's `@RequiredArgsConstructor` with `private final` fields. **DO NOT** use `@Inject`.

```java
// ✅ Correct
@RequiredArgsConstructor
public class MovieService {
    private final MovieRepository movieRepository;
    private final ShowtimeRepository showtimeRepository;
}

// ❌ Incorrect
@Inject
private MovieRepository movieRepository;
```

### Reactive (Non-Blocking) Mode
For Quarkus reactive resources, use `@Inject` for injected dependencies:

```java
// ✅ Correct for Reactive
@Inject
MovieService movieService;

@Inject
ObjectMapper objectMapper;
```

> **Note:** In reactive mode, `@RequiredArgsConstructor` may not work as expected because constructors are not called in the same way. Stick with `@Inject` for reactive classes.

---

## Lombok Usage

### Required Annotations
Always add `@RequiredArgsConstructor` to Service and Resource classes (imperative mode only):

```java
@RequiredArgsConstructor
public class ShowtimeService {
    private final ShowtimeRepository showtimeRepository;
}
```

### Logging
Use `@Slf4j` for logging in any class:

```java
@Slf4j
public class SeatWebSocket {
    public void onConnect() {
        log.info("Client connected");
    }
}
```

---

## DTO Pattern

### Use Java Records
Prefer Java records over classes with builders for DTOs:

```java
// ✅ Correct - Using Java Record
public record ShowtimeDTO(
    UUID id,
    String movieTitle,
    LocalDateTime startTime,
    Integer availableSeats
) {}

// ❌ Incorrect - Using Class with Builder
@Data
@Builder
public class ShowtimeDTO {
    private UUID id;
    private String movieTitle;
    private LocalDateTime startTime;
    private Integer availableSeats;
}
```

### When Builder is Needed
If you need a builder pattern, use `@Builder(toBuilder = true)` with records:

```java
public record SeatDTO(
    UUID id,
    String seatNumber,
    String seatType
) {
    // Optional: Custom builder usage
    public static SeatDTOBuilder builder() {
        return new SeatDTOBuilder();
    }
}
```

---

## Mapping Strategy

### Location
**Keep mapping logic in the Resource (Controller) layer**, not in the Service layer.

```java
// ✅ Correct - Service returns Entity
@GET
public Uni<List<Showtime>> getShowtimes(@PathParam("movieId") UUID movieId) {
    return showtimeService.findByMovieId(movieId);
}

// ✅ Correct - Resource performs mapping
@GET
public Uni<List<ShowtimeDTO>> getShowtimes(@PathParam("movieId") UUID movieId) {
    return showtimeService.findByMovieId(movieId)
        .map(showtimes -> showtimes.stream()
            .map(this::toDTO)
            .collect(Collectors.toList()));
}

private ShowtimeDTO toDTO(Showtime showtime) {
    return new ShowtimeDTO(
        showtime.getId(),
        showtime.getMovie().getTitle(),
        showtime.getStartTime(),
        showtime.getAvailableSeats()
    );
}
```

### MapStruct (Optional for Complex Mappings)
For complex entity ↔ DTO mappings, consider using **MapStruct**:

```java
@Mapper(componentModel = "cdi")
public interface ShowtimeMapper {
    ShowtimeDTO toDTO(Showtime showtime);
    List<ShowtimeDTO> toDTOList(List<Showtime> showtimes);
}
```

---

## Transaction Management

### Imperative (Blocking) Mode
Place `@Transactional` on **Service layer**, NOT on Resource/Controller layer:

```java
// ✅ Correct - Transaction on Service
@Service
@Transactional
public class MovieService {
    public Movie createMovie(Movie movie) {
        return movieRepository.save(movie);
    }
}

// ❌ Incorrect - Transaction on Resource
@Path("/movies")
@Transactional  // Don't do this!
public class MovieResource { }
```

### Reactive (Non-Blocking) Mode
In reactive/Quarkus Vert.x context, use **Mutiny** transaction APIs instead of `@Transactional`:

```java
// ✅ Correct - Using Mutiny Transaction
@Inject
QuarkusTransaction tx;

public Uni<Seat> reserveSeat(UUID showtimeId, String seatNumber) {
    return tx.call(() -> seatRepository.findByShowtimeId(showtimeId)
        .onItem().ifNotNull().transformToUni(seat -> {
            seat.setReserved(true);
            return seatRepository.persist(seat);
        }));
}
```

### Transaction Patterns Summary

| Mode | Approach | Annotation/API |
|------|----------|----------------|
| Imperative | Declarative | `@Transactional` on Service |
| Reactive | Programmatic | `QuarkusTransaction.call()` |

---

## Validation

### Use Hibernate Validator
Add validation annotations to path parameters and request bodies:

```java
@GET
@Path("/showtimes/{movieId}")
public Uni<List<Showtime>> getShowtimes(
    @PathParam("movieId") @NotNull UUID movieId,
    @QueryParam("date") @NotNull LocalDate date
) {
    // ...
}
```

### Common Validation Annotations

| Annotation | Usage |
|------------|-------|
| `@NotNull` | Field cannot be null |
| `@NotBlank` | String cannot be blank |
| `@NotEmpty` | Collection cannot be empty |
| `@Size` | String/Collection size constraints |
| `@Min` / `@Max` | Numeric constraints |
| `@Pattern` | Regex pattern matching |

---

## Logging

### Use Lombok @Slf4j
Never use `System.out.println()` - use SLF4J logging:

```java
@Slf4j
public class ReservationService {
    
    public Uni<Reservation> reserve(ReservationRequest request) {
        log.info("Processing reservation for movie: {}", request.getMovieId());
        
        return doReserve(request)
            .onItem().invoke(result -> 
                log.info("Reservation completed: {}", result.getId())
            )
            .onFailure().invoke(err ->
                log.error("Reservation failed: {}", err.getMessage())
            );
    }
}
```

### Log Levels
- **DEBUG**: Detailed information for debugging
- **INFO**: General operational events
- **WARN**: Warning messages
- **ERROR**: Error messages and exceptions

---

## Secrets Management

### NEVER Hardcode Secrets
**DO NOT** commit passwords, API keys, tokens, or any secrets to the repository.

```properties
# ❌ Incorrect - Hardcoded password
database.password=mysecretpassword

# ✅ Correct - Environment variable
database.password=${DB_PASSWORD}
```

### Best Practices
1. Use environment variables for all sensitive data
2. Use `.env` files for local development (add to `.gitignore`)
3. Use a secrets manager (e.g., AWS Secrets Manager, HashiCorp Vault) in production
4. Rotate secrets regularly

---

## Summary

| Category | Convention |
|----------|------------|
| **DI (Imperative)** | `@RequiredArgsConstructor` + `private final` |
| **DI (Reactive)** | Use `@Inject` |
| **DTOs** | Java records |
| **Mapping** | Keep in Resource layer |
| **Transaction (Imperative)** | `@Transactional` on Service |
| **Transaction (Reactive)** | `QuarkusTransaction.call()` |
| **Validation** | Hibernate Validator annotations |
| **Logging** | Lombok `@Slf4j` |
| **Secrets** | Environment variables only |
