# SOLID Audit — Developer 2

| Item | Value |
|---|---|
| Role | Developer 2 (Pablo Andres Amaya Pabon) |
| Requirement | Requerimiento 8 — SOLID audit and correction, Phase 1 (audit) |
| Audited commit | `<commit de develop>` (`develop`, with the GRASP correction of Requerimiento 7 merged) |
| Date | 2026-10-23 |
| Scope rule | Only the code under the responsibility of Developer 2, including what Developer 2 created or changed in the GRASP correction (`docs/grasp-refactor-log.md`) |

Verdicts: **Violation** (the code breaks the principle at one or more points), **Correct** (the code follows the principle), **Not applicable** (the audited code has no point where the principle applies).

## Scope

| Module (requirement) | Classes and methods audited |
|---|---|
| People (Taller 1) | `Person`, `Client` (statement: `Customer`), `Seller`, `PersonRepository`, `PersonService` |
| Accessories (Requerimiento 1) | `AccessoryRepository`, `AccessoryService` |
| Promotions (Requerimiento 2) | `PromotionRepository`, `PromotionService` |

## Single Responsibility Principle (SRP)

**Verdict:** _pending — audit in progress._

- **People — Correct.** `PersonRepository` only reads and writes the two people files; `PersonService` only registers and looks up people; `Client` and `Seller` hold their data and description.

- **Accessories — Correct.** `AccessoryRepository` only handles `data/accessories.csv`; `AccessoryService` only handles the accessory catalogue and its stock.

- **Promotions — Correct.** `PromotionService` registers, validates and selects promotions — all promotion rules; `PromotionRepository` only handles its file.

## Open/Closed Principle (OCP)

**Verdict:** _pending — audit in progress._

- **People — Correct.** A new kind of person would be a new subclass of `Person` implementing `getDescription`; the services work with the concrete lists they own.

- **Accessories — Correct, examined.** A new accessory type is a new subclass that reports its type (`getAccessoryType`, GRASP V-07), so `AccessoryService.listAccessoriesByType` does not change. The type tags of `AccessoryRepository` are accepted for the reason of Q1: it is the only class that knows the file format.

- **Promotions — Correct, examined.** A new promotion is a subclass of `Promotion`; `findBestPromotionFor` only calls `calculateDiscount`. `validateCategory` (`PromotionService.java:82-90`) lists the three categories through the constants of `CategoryDiscount`, the one place that defines them; it is not counted.

## Liskov Substitution Principle (LSP)

**Verdict:** _pending — audit in progress._

- **People — Correct.** `Client` and `Seller` implement `getDescription` without new preconditions or exceptions and keep the identity data of `Person`; any of them can be printed or validated as a `Person` (`PersonValidator.validateUniquePerson` receives `List<? extends Person>`).

- **Accessories, promotions, returns and warranties — Not applicable.** The repositories and services in scope have no subclasses and do not extend any class.

## Interface Segregation Principle (ISP)

**Verdict:** _pending — audit in progress._

- **People — Correct.** `PersonService` uses all the operations of `PersonRepository`.

## Dependency Inversion Principle (DIP)

**Verdict:** _pending — audit in progress._

- **People — Violation (V-01).** `PersonService` depends on the concrete `PersonRepository`.

- **Accessories — Violation (V-01).** Same for `AccessoryService`.

- **Promotions — Violation (V-01).** Same for `PromotionService`.

### V-01

| Field | Content |
|---|---|
| ID | V-01 |
| Principle | DIP |
| Module | People (Taller 1), accessories (Requerimiento 1), promotions (Requerimiento 2), returns (Requerimiento 3), warranties (Requerimiento 4) and GRASP correction V-05 (Requerimiento 7) |
| Evidence | Each service keeps and receives the concrete repository: `PersonService` (`PersonService.java:28`, `:39`), `AccessoryService` (`AccessoryService.java:21`, `:30`), `PromotionService` (`PromotionService.java:22`, `:28`), `ReturnService` (`ReturnService.java:19`, `:36`), `WarrantyService` (`WarrantyService.java:29`, `:42`, `:56`). |
| Explanation | The rules of each module (high-level policy) depend on a concrete file repository (low-level detail). Since the GRASP correction the repositories are injected from `Main`, but as concrete classes, so the policy still depends on the detail. |
| Consequence | Changing how one module stores its data (another format, a database, an in-memory store for a test run) requires editing its service. |
| Proposed solution | One interface per repository in the persistence package (`PersonStore`, `AccessoryStore`, `PromotionStore`, `ReturnStore`, `WarrantyStore`) with the operations the service uses, implemented by each repository; each service depends on its interface. `Main` keeps passing the same repositories, so it does not change. |
| Other role's code involved | No. |
