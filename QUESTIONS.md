# Questions

Here we have 3 questions related to the code base for you to answer. It is not about right or wrong, but more about what's the reasoning behind your decisions.

1. In this code base, we have some different implementation strategies when it comes to database access layer and manipulation. If you would maintain this code base, would you refactor any of those? Why?

**Answer:**
```txt
I would aim for a more consistent separation between domain logic and persistence concerns.

For simple CRUD-style resources (like Store and Product), Panache entities exposed directly from
the REST layer are acceptable for a small codebase, but they couple HTTP, persistence and domain
quite tightly. For the Warehouse area we already have a cleaner architecture: a domain model,
ports (WarehouseStore, LocationResolver, *UseCaseOperation interfaces) and adapters
(WarehouseRepository, WarehouseResourceImpl). This makes it much easier to enforce business rules,
swap storage, and test use cases in isolation.

If I were maintaining this long‑term, I would:
1) Gradually move Store/Product towards the same ports/use-case/repository style as Warehouse,
   so that business rules live in use cases instead of REST resources or entities.
2) Keep Panache entities as persistence-only classes (DbXxx), and use plain domain models for
   business logic.
3) Centralize transactional boundaries in services/use cases instead of controllers.

The benefits would be clearer responsibilities, stronger encapsulation of business rules,
and easier evolution/testing of the domain as complexity grows.
```
----
2. When it comes to API spec and endpoints handlers, we have an Open API yaml file for the `Warehouse` API from which we generate code, but for the other endpoints - `Product` and `Store` - we just coded directly everything. What would be your thoughts about what are the pros and cons of each approach and what would be your choice?

**Answer:**
```txt
OpenAPI-first (as used for Warehouse) is very powerful when:
- You need a clear contract between teams/clients and backend.
- You care about consistent request/response shapes, error codes and documentation.
- You want generated clients/servers to reduce boilerplate and keep spec and code in sync.

Pros of OpenAPI-first:
- Single source of truth for the contract (documentation, validation, clients).
- Easier collaboration with other teams and external integrators.
- Less manual boilerplate in controllers and DTOs.

Cons:
- More tooling and build complexity.
- Can feel heavier for very small or fast-changing internal endpoints.
- Refactoring the spec requires some discipline so it doesn’t lag behind code.

Hand-written endpoints (like Product/Store) are:
- Faster to prototype, with fewer moving parts.
- More flexible when experimenting with API shapes.

But they can drift in style and documentation and make it harder to share a stable contract.

Given this project, I would:
- Keep Warehouse OpenAPI-first (good domain boundary and external‑facing semantics).
- For Product and Store, either:
  * Also move them to OpenAPI-first if they are real public APIs, or
  * Keep them hand-written but add a spec later once they stabilize.

In a production environment, having OpenAPI specs for all public-facing APIs is my preferred
choice because it improves interoperability, testing, and long-term maintainability.
```
----
3. Given the need to balance thorough testing with time and resource constraints, how would you prioritize and implement tests for this project? Which types of tests would you focus on, and how would you ensure test coverage remains effective over time?

**Answer:**
```txt
I would prioritize tests around the most business-critical and rule-heavy areas, then add
lightweight coverage elsewhere.

Priorities for this codebase:
1) Domain/use-case tests for Warehouse:
   - Create/Replace/ArchiveUseCase: cover all validation rules (BU uniqueness, location,
     max warehouses per location, capacity and stock constraints, replacement-specific rules).
   - These can be fast unit tests using in-memory fakes for WarehouseStore/LocationResolver.
2) Integration tests for key REST flows:
   - Warehouse endpoints (as in WarehouseEndpointIT), plus happy/validation-error cases.
   - Smoke tests for Product and Store to ensure wiring, transactions and error mapping work.
3) Unit tests for smaller components:
   - LocationGateway, FulfilmentAssignmentService (product/store/warehouse constraints),
     and any non-trivial helpers.

To keep coverage effective over time:
- Treat tests as part of the design: when adding a new business rule, first express it in a test.
- Keep unit tests fast so they can run on every commit, and run integration tests in CI.
- Use code coverage as a guide, not a target: focus on covering important decision branches
  in the domain, not every line of boilerplate.
- Refactor tests alongside production code, avoiding brittle tests that over-couple to implementation
  details (e.g. asserting on public behavior/contracts instead of internals).
```