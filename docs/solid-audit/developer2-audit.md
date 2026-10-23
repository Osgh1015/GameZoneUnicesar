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
| Returns (Requerimiento 3) | `ReturnRepository` (with `ReturnRecord`), `ReturnService` (including the monthly balance) |
| Warranties (Requerimiento 4) | `WarrantyRepository` (with `WarrantyRecord`), `WarrantyService` |

## Single Responsibility Principle (SRP)

**Verdict:** _pending — audit in progress._

- **People — Correct.** `PersonRepository` only reads and writes the two people files; `PersonService` only registers and looks up people; `Client` and `Seller` hold their data and description.

- **Accessories — Correct.** `AccessoryRepository` only handles `data/accessories.csv`; `AccessoryService` only handles the accessory catalogue and its stock.

- **Promotions — Correct.** `PromotionService` registers, validates and selects promotions — all promotion rules; `PromotionRepository` only handles its file.

- **Returns — Violation (V-03).** `ReturnService` also produces the monthly balance. `ReturnRepository` only handles `data/returns.txt` and returns `ReturnRecord` objects.

- **Warranties — Correct.** `WarrantyService` assigns, queries and cancels warranties and rebuilds them from their records; `WarrantyRepository` only handles its file.

### V-03

| Field | Content |
|---|---|
| ID | V-03 |
| Principle | SRP |
| Module | Returns (Requerimiento 3) and integration adjustment A6 (Requerimiento 5) |
| Evidence | `ReturnService.calculateMonthlySales` (`ReturnService.java:222`), `calculateMonthlyReturns` (`:240`), `generateMonthlyBalance` (`:258`) and the helpers `isInPeriod` (`:270`) and `round`. |
| Explanation | `ReturnService` registers and queries returns, and it also produces an accounting report of sales and refunds. They change for different reasons: the return rules on one side, the content of the monthly balance on the other. |
| Consequence | A change in the balance (another period, a breakdown by seller) forces editing the class that applies the 30-day rule and restores stock, with the risk of touching it; the returns service also needs the whole history of sales only because of the report. |
| Proposed solution | Move the balance into `MonthlyBalanceService` (service layer), which reads the sales through `SaleLookup` and the returns through `ReturnService.viewAllReturns()`, and remove it from `ReturnService`. |
| Other role's code involved | Yes — `ReturnMenu` and `ConsoleMenu` (Technical Lead) must ask `MonthlyBalanceService` for the balance. |

## Open/Closed Principle (OCP)

**Verdict:** _pending — audit in progress._

- **People — Correct.** A new kind of person would be a new subclass of `Person` implementing `getDescription`; the services work with the concrete lists they own.

- **Accessories — Correct, examined.** A new accessory type is a new subclass that reports its type (`getAccessoryType`, GRASP V-07), so `AccessoryService.listAccessoriesByType` does not change. The type tags of `AccessoryRepository` are accepted for the reason of Q1: it is the only class that knows the file format.

- **Promotions — Correct, examined.** A new promotion is a subclass of `Promotion`; `findBestPromotionFor` only calls `calculateDiscount`. `validateCategory` (`PromotionService.java:82-90`) lists the three categories through the constants of `CategoryDiscount`, the one place that defines them; it is not counted.

- **Returns — Correct.** The return rules do not depend on the kind of item (since GRASP V-11).

- **Warranties — Violation (V-04).** The kinds of warranty are decided with tags and `instanceof` in the repository and in three methods of the service.

### V-04

| Field | Content |
|---|---|
| ID | V-04 |
| Principle | OCP |
| Module | Warranties (Requerimiento 4) and integration adjustments A2 and A7 (Requerimiento 5) |
| Evidence | `WarrantyRepository.toLine`, `WarrantyRepository.java:126-128`: `warranty instanceof ExtendedWarranty ? EXTENDED_TYPE : BASIC_TYPE`; `WarrantyService.loadWarranties`, `WarrantyService.java:86-87`: `case "BASIC" -> new BasicWarranty(...)`, `case "EXTENDED" -> new ExtendedWarranty(...)`; `WarrantyService.findWarrantyByProduct` (`:217`) and `cancelWarranties` (`:276`): `instanceof ExtendedWarranty`. |
| Explanation | The kinds of warranty are known in four places of two classes: the type tag, the construction from the tag and two business rules (the preferred coverage, one cancellation per kind). Unlike the other repositories, the decision is not only about the file format: the service also takes business decisions by asking the concrete class. |
| Consequence | A new kind of warranty (for example, a premium coverage) forces editing the repository and three methods of the service, and missing one of them gives a silent error (the new kind is never cancelled or never chosen). |
| Proposed solution | A `WarrantyKind` enum in the model (`BASIC`, `EXTENDED`) that knows how to build its warranty and whether it is the preferred coverage, and each warranty reporting its kind (`getKind()`); the repository writes `getKind().name()` and the service builds and chooses through the kind. |
| Other role's code involved | Yes — the warranty hierarchy is Developer 1's code; Developer 1 must add `getKind()` to `Warranty`, `BasicWarranty` and `ExtendedWarranty`. |

## Liskov Substitution Principle (LSP)

**Verdict:** _pending — audit in progress._

- **People — Correct.** `Client` and `Seller` implement `getDescription` without new preconditions or exceptions and keep the identity data of `Person`; any of them can be printed or validated as a `Person` (`PersonValidator.validateUniquePerson` receives `List<? extends Person>`).

- **Accessories, promotions, returns and warranties — Not applicable.** The repositories and services in scope have no subclasses and do not extend any class.

## Interface Segregation Principle (ISP)

**Verdict:** _pending — audit in progress._

- **People — Correct.** `PersonService` uses all the operations of `PersonRepository`.

- **Returns — Violation (V-02).** `ReturnService` depends on the whole `SaleService` to use two read operations. Its dependency on `WarrantyService` (only `cancelWarranties`) was examined: both services are Developer 2's code in the same group of modules, they change together, and `WarrantyService` has no other role's operations behind it; it is not counted.

- **Warranties — Correct.** `WarrantyService` depends on `SaleAssembler` (only `loadSales`, its single operation) and on `ProductService` for `findById`, the product lookup it needs.

### V-02

| Field | Content |
|---|---|
| ID | V-02 |
| Principle | ISP (also recorded under DIP) |
| Module | Returns (Requerimiento 3) |
| Evidence | `ReturnService` field `private final SaleService saleService` (`ReturnService.java:20`), used only for `findById` (`:55`, `:93`) and `getSalesHistory` (`:224`). |
| Explanation | The returns module depends on the whole sales service — including the registration of sales and its six collaborators — to use two read operations. It depends on operations it does not use, and on a concrete service of another role. |
| Consequence | Any change in `SaleService` (its constructor, its registration flow) reaches `ReturnService`; the returns cannot be built or reused without the full sales module. |
| Proposed solution | A read-only interface `SaleLookup` (`findById`, `getSalesHistory`) in the service layer, implemented by `SaleService`; `ReturnService` depends on it. `Main` keeps passing `saleService`. |
| Other role's code involved | Yes — `SaleService` belongs to the Technical Lead, who must declare `implements SaleLookup`. |

## Dependency Inversion Principle (DIP)

**Verdict:** _pending — audit in progress._

- **People — Violation (V-01).** `PersonService` depends on the concrete `PersonRepository`.

- **Accessories — Violation (V-01).** Same for `AccessoryService`.

- **Promotions — Violation (V-01).** Same for `PromotionService`.

- **Returns — Violation (V-01, V-02).** `ReturnService` depends on the concrete `ReturnRepository` and on the concrete `SaleService`.

- **Warranties — Violation (V-01).** `WarrantyService` depends on the concrete `WarrantyRepository`.

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
