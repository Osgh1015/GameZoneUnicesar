x|# GRASP Audit — Developer 2

| Item | Value |
|---|---|
| Role | Developer 2 (Pablo Andres Amaya Pabon) |
| Requirement | Requerimiento 7 — GRASP audit and correction, Phase 1 (audit) |
| Audited commit | `4027852` (`develop`) |
| Date | 2026-10-23 |
| Scope rule | Only the code under the responsibility of Developer 2, per the responsibility table of Requerimiento 7 (role-based distribution agreed by the team) |

Verdicts: **Violation** (the code breaks the pattern at one or more points), **Correct** (the pattern is applied correctly), **Not applicable** (the audited code has no point where the pattern applies).

## Scope

| Module (requirement) | Classes and methods audited |
|---|---|
| People (Taller 1) | `Person`, `Client` (the statement calls it `Customer`), `Seller`, `PersonRepository`, `PersonService` |

## Information Expert

**Verdict:** _pending — audit in progress._

- **People — Correct.** `Client.getDescription()` and `Seller.getDescription()` build the description from their own fields; `PersonService.findClientById` and `findVendorById` (`PersonService.java:88`, `:103`) search the lists the service owns.

## Creator

**Verdict:** _pending — audit in progress._

- **People — Correct.** `PersonRepository.loadClients` and `loadVendors` (`PersonRepository.java:81`, `:116`) create `Client` and `Seller` objects from the records they read. The `Client` registered through the menu is created by `ConsoleMenu` (Technical Lead), reported in the developer1 audit.

## Controller

**Verdict:** _pending — audit in progress._

## Low Coupling

**Verdict:** _pending — audit in progress._

- **People — Violation (V-01).** `Person`, `Client` and `Seller` import nothing outside the model, and `PersonRepository` depends only on the model and the exceptions, which is correct; `PersonService` is tied to the concrete `PersonRepository` and to its files because it creates it.

### V-01

| Field | Content |
|---|---|
| ID | V-01 |
| Pattern | Low Coupling (also recorded under Protected Variations) |
| Module | People (Taller 1) |
| Evidence | `PersonService()`, `PersonService.java:39`: `this.repository = new PersonRepository();` and `PersonRepository.java:32-33`: `private static final String CLIENTS_FILE = "data/clients.txt"; private static final String VENDORS_FILE = "data/vendors.txt";` |
| Explanation | `PersonService` decides by itself which concrete repository it uses, and the repository decides by itself which files it uses. `AccessoryService`, `PromotionService`, `ReturnService` and `WarrantyService` receive their repository from `Main` with the file path; the people module is the exception. |
| Consequence | The people module cannot be wired with other files (for example, a copy of `data/` for a test run) without editing two classes, and `Main` cannot decide the wiring of this module as it does for the others. A change of the storage location is not protected. |
| Proposed solution | `PersonRepository(String clientsFile, String vendorsFile)` and `PersonService(PersonRepository repository)`, injected from `Main` with the same two files. Patterns applied: Low Coupling and Protected Variations. |
| Other role's code involved | Yes — `Main` belongs to the Technical Lead and must create the repository and pass it to the service. |

## High Cohesion

**Verdict:** _pending — audit in progress._

- **People — Correct.** `PersonRepository` only reads and writes the two people files; `PersonService` only registers and looks up people. It keeps two pairs of equivalent methods (`findVendorById`/`findSellerById`, `PersonService.java:103`, `:112`; `listVendors`/`listSellers`, `:78`, `:121`) because other modules call both names; they do the same job, so the class is still focused.

## Polymorphism

**Verdict:** _pending — audit in progress._

- **People — Correct.** `Person.getDescription()` is abstract and `Client`/`Seller` override it; no code in scope asks a person for its concrete type.

## Pure Fabrication

**Verdict:** _pending — audit in progress._

- **People — Correct.** `PersonRepository` is the artificial class that keeps file access out of `Client` and `Seller`.

## Indirection

**Verdict:** _pending — audit in progress._

- **People — Correct.** `PersonService` sits between `ConsoleMenu` and `PersonRepository`; the menu never touches the repository.

## Protected Variations

**Verdict:** _pending — audit in progress._

- **People — Violation (V-01).** A change of the file format is confined to `PersonRepository`, which is correct, but the file locations are fixed inside the repository and the service creates its own repository, so a change of location is not protected.
