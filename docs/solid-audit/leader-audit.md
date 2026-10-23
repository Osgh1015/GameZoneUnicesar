# SOLID Audit — Technical Lead

| Item | Value |
|---|---|
| Role | Technical Lead (Omar Sebastian Gamez Heredia) |
| Requirement | Requerimiento 8 — SOLID audit and correction, Phase 1 (audit) |
| Audited commit | `<commit de develop>` (`develop`, with the GRASP correction of Requerimiento 7 merged) |
| Date | 2026-10-23 |
| Scope rule | Only the code under the responsibility of the Technical Lead, including what the Technical Lead created or changed in the GRASP correction (`docs/grasp-refactor-log.md`) |

Verdicts: **Violation** (the code breaks the principle at one or more points), **Correct** (the code follows the principle), **Not applicable** (the audited code has no point where the principle applies).

## Scope

| Module (requirement) | Classes and methods audited |
|---|---|
| Sales (Taller 1), promotions (Requerimiento 2), warranties (Requerimiento 4) | `Sale` (every method except `canBeReturned`, Developer 1), including the discount, the warranty cost and `generateReceipt`, and the GRASP additions `findProduct`, `countUnitsOf`, `belongsToClient`, `wasAttendedBy` |
| Sales (Taller 1) | `SalePersistence` (statement: `SaleRepository`) and its `SaleRecord` (GRASP V-19) |
| Sales and integrations (Taller 1, Requerimientos 1, 2, 4 and 5) | `SaleService`, including the accessories, promotions and warranties in `registerSale`; `SaleAssembler` (GRASP V-19) |
| User interface (Taller 1 to Requerimiento 6) | `ConsoleMenu`, including the differentiated handling of exceptions; `ConsoleInput` and the submenus `ProductMenu`, `PersonMenu`, `SaleMenu`, `AccessoryMenu`, `ReturnMenu`, `PromotionMenu`, `WarrantyMenu` (GRASP V-20) |
| Start-up and returns (Taller 1, Requerimiento 3) | `Main`, `ProductService.restoreStock` |

## Single Responsibility Principle (SRP)

**Verdict:** Violation — see V-01.

- **Sale — Violation (V-01).** `Sale` also lays out its receipt. The GRASP additions (`findProduct`, `countUnitsOf`, `belongsToClient`, `wasAttendedBy`) answer questions with the sale's own data, which is correct.

- **SalePersistence — Correct.** It only reads and writes `data/sales.txt` and returns `SaleRecord` objects.

- **SaleService — Correct.** `registerSale` is split into private steps with one purpose each, and the queries are all about sales. **SaleAssembler — Correct.** It only rebuilds the stored sales.

- **User interface — Correct.** Since the GRASP correction each submenu handles one module and `ConsoleInput` only reads the input; `ConsoleMenu` keeps the main loop and the exception handling.

- **Main — Correct.** It only composes the application. **ProductService.restoreStock — Correct.** It only returns units to the stock and persists the change.

### V-01

| Field | Content |
|---|---|
| ID | V-01 |
| Principle | SRP |
| Module | Sales (Taller 1), promotions (Requerimiento 2) and warranties (Requerimiento 4) |
| Evidence | `Sale.generateReceipt()`, `Sale.java:221-245`: builds the Spanish receipt — `--- Recibo de Venta ---`, the labels `Subtotal:`, `Descuento (...)`, `Garantías extendidas:`, `Total final:`, the texts for no promotion and no warranty, and the format `$%.2f`. |
| Explanation | `Sale` holds the data and the totals of a sale and also the layout of the receipt. They change for different reasons. |
| Consequence | A change in the receipt (a new line, another format) forces editing the domain class that calculates the totals, which every module of the system uses. |
| Proposed solution | Move the layout into `SaleReceipt` (model package, it reads the sale through its getters) and keep `generateReceipt()` as a one-line delegation, because the sales module defines it and `SaleMenu` calls it. |
| Other role's code involved | No. |

## Open/Closed Principle (OCP)

**Verdict:** Violation — see V-04.

- **Sale — Correct.** The receipt asks each product for its label (`getTypeLabel`, GRASP V-17); a new product type does not change `Sale`.

- **SaleService — Correct.** Warranty eligibility and the stock dispatch are asked polymorphically since the GRASP correction (`isWarrantyEligible`, `ItemCatalog`).

- **ConsoleMenu — Violation (V-04).** A new module forces editing the main menu in three places.

### V-04

| Field | Content |
|---|---|
| ID | V-04 |
| Principle | OCP |
| Module | User interface (Taller 1 to Requerimiento 6) and GRASP correction V-20 (Requerimiento 7) |
| Evidence | `ConsoleMenu` constructor, `ConsoleMenu.java:51-61`, creates the seven concrete submenus; `start()` prints the seven options one by one (`:77-83`) and dispatches them with a `switch` with one `case` per module (`:90-117`). |
| Explanation | The GRASP correction moved each submenu to its own class, but the main menu still lists every module three times: in the constructor, in the printed menu and in the `switch`. |
| Consequence | Adding a module (for example, a reports menu) forces editing `ConsoleMenu` in three places; forgetting one of them gives an option that is printed but does nothing, or the reverse. |
| Proposed solution | A `MenuOption` interface (`getTitle`, `show`) implemented by every submenu; `ConsoleMenu` receives the list of options from `Main`, prints `number. title` for each and runs the chosen one. A new module is added in `Main` only. |
| Other role's code involved | No. |

## Liskov Substitution Principle (LSP)

**Verdict:** Not applicable — the classes in scope have no subclasses and extend no class of the system.

- **Sale, SalePersistence, SaleService, SaleAssembler, the user interface and Main — Not applicable.** None of them takes part in an inheritance hierarchy of the system; the hierarchies they use (`Product`, `Promotion`, `Warranty`) are audited by Developer 1.

## Interface Segregation Principle (ISP)

**Verdict:** Violation — see V-03.

- **SaleService and SaleAssembler — Violation (V-03).** They depend on three complete services of Developer 2 to use one or two operations of each. Their dependency on `ItemCatalog` was examined: it is the intermediary created for the sales and returns modules and every operation it has is used by them; it is not counted.

- **Submenus — Correct, examined.** Each submenu depends on the services of its own module, whose operations are exactly the options it offers.

### V-03

| Field | Content |
|---|---|
| ID | V-03 |
| Principle | ISP (also recorded under DIP) |
| Module | Sales and integrations (Requerimientos 2, 4 and 5) and GRASP correction V-19 (Requerimiento 7) |
| Evidence | `SaleService` fields `PersonService personService`, `PromotionService promotionService`, `WarrantyService warrantyService` (`SaleService.java:42`, `:44`, `:45`), used only for `findClientById`/`findSellerById` (`:111`, `:113`), `findBestPromotionFor` (`:205`) and `assignBasicWarranty`/`assignExtendedWarranty` (`:237`, `:241`); `SaleAssembler` field `PersonService personService` (`SaleAssembler.java:28`), used for the same two lookups (`:75-76`). |
| Explanation | The sale registration depends on three complete concrete services of another role to use one or two operations of each; it depends on registrations, listings and cancellations it never calls. |
| Consequence | Any change in those services (their constructors, their other operations) reaches the sales module; the sale registration cannot be built or reused without the full people, promotions and warranties modules. |
| Proposed solution | Three narrow interfaces in the service layer, defined by the client: `PersonLookup` (`findClientById`, `findSellerById`), `PromotionSelector` (`findBestPromotionFor`) and `WarrantyAssigner` (`assignBasicWarranty`, `assignExtendedWarranty`), implemented by the services of Developer 2; `SaleService` and `SaleAssembler` depend on them. `Main` keeps passing the same services. |
| Other role's code involved | Yes — `PersonService`, `PromotionService` and `WarrantyService` are Developer 2's code and must declare `implements`. |

## Dependency Inversion Principle (DIP)

**Verdict:** Violation — see V-02 (and V-03).

- **SaleService and SaleAssembler — Violation (V-02, V-03).** They depend on the concrete `SalePersistence` and on concrete services of another role.

- **User interface — Correct, examined.** The submenus depend on the services of the application, which are the entry points of the use cases; the user interface is the outermost layer and no abstraction is gained by hiding them. `ConsoleMenu` depending on concrete submenus is recorded under OCP (V-04).

- **Main — Correct.** It is the composition root; creating every concrete class is its purpose.

### V-02

| Field | Content |
|---|---|
| ID | V-02 |
| Principle | DIP |
| Module | Sales (Taller 1) and GRASP correction V-19 (Requerimiento 7) |
| Evidence | `SaleService` field `private SalePersistence salePersistence` (`SaleService.java:41`) and constructor (`:59`); `SaleAssembler` field `private final SalePersistence salePersistence` (`SaleAssembler.java:27`) and constructor (`:38`). |
| Explanation | The sales rules and the rebuilding of the stored sales (high-level) depend on the concrete file class (low-level detail). |
| Consequence | Changing how sales are stored requires editing `SaleService` and `SaleAssembler`. |
| Proposed solution | A `SaleStore` interface in the persistence package (`saveAll`, `loadAll`, `damagedLine`) implemented by `SalePersistence`; both classes depend on it. `Main` keeps passing the same `SalePersistence`. |
| Other role's code involved | No. |

## GRASP additions (Requerimiento 7)

- **`SaleAssembler`** — recorded under V-02 and V-03 (it depends on the concrete `SalePersistence` and on the whole `PersonService`).
- **`SaleRecord`, `Sale.findProduct`/`countUnitsOf`/`belongsToClient`/`wasAttendedBy`** — correct under the five principles.
- **`ConsoleInput` and the seven submenus** — correct for SRP (the GRASP High Cohesion correction also solved the SRP problem of `ConsoleMenu`); the way `ConsoleMenu` creates and dispatches them is V-04.

## Summary of verdicts

| Principle | Verdict | Violations |
|---|---|---|
| Single Responsibility Principle (SRP) | Violation | V-01 |
| Open/Closed Principle (OCP) | Violation | V-04 |
| Liskov Substitution Principle (LSP) | Not applicable | — |
| Interface Segregation Principle (ISP) | Violation | V-03 |
| Dependency Inversion Principle (DIP) | Violation | V-02, V-03 |

Distinct violations: 4 (V-01 to V-04). V-03 appears under two principles but is recorded once.

## Notes

- **Examined and accepted (not counted):** the dependency of the sales module on `ItemCatalog` (ISP); the dependency of the submenus on the services of their module (DIP); `Main` as the composition root.
- **Relation with GRASP:** the GRASP split of `ConsoleMenu` solved its SRP problem but not its OCP problem (V-04); `SaleAssembler`, created by GRASP, still depends on concrete classes (V-02, V-03).
- **Corrections that need code of another role:** V-03 (`PersonService`, `PromotionService` and `WarrantyService` implement the new interfaces, Developer 2).
