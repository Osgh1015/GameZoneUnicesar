# GRASP Audit — Technical Lead

| Item | Value |
|---|---|
| Role | Technical Lead (Omar Sebastian Gamez Heredia) |
| Requirement | Requerimiento 7 — GRASP audit and correction, Phase 1 (audit) |
| Audited commit | `4027852` (`develop`) |
| Date | 2026-10-23 |
| Scope rule | Only the code under the responsibility of the Technical Lead, per the responsibility table of Requerimiento 7 (role-based distribution agreed by the team) |

Verdicts: **Violation** (the code breaks the pattern at one or more points), **Correct** (the pattern is applied correctly), **Not applicable** (the audited code has no point where the pattern applies).

## Scope

| Module (requirement) | Classes and methods audited |
|---|---|
| Sales (Taller 1) and promotions (Requerimiento 2) | `Sale`, including the applied discount, the extended warranty cost and the breakdown of `generateReceipt` (every method except `canBeReturned`, which belongs to Developer 1) |
| Sales (Taller 1) | `SalePersistence` (the statement calls it `SaleRepository`) |

## Information Expert

**Verdict:** _pending — audit in progress._

- **Sale — Correct.** `Sale.calculateTotal()` (`Sale.java:114`) and `calculateFinalTotal()` (`:158`) use the items, the discount and the warranty cost the sale owns; `generateReceipt()` (`:169`) prints the sale's own data; `isValid()` (`:104`) checks its own items.

- **SalePersistence — Correct.** It writes each sale from the data the sale exposes and does not compute anything about it.

## Creator

**Verdict:** _pending — audit in progress._

- **SalePersistence — Correct.** `fromLine` creates each `Sale` from the record it reads.

## Controller

**Verdict:** _pending — audit in progress._

## Low Coupling

**Verdict:** _pending — audit in progress._

- **Sale — Correct except for V-01.** `Sale` depends on `Client`, `Seller` and `Product`, which it needs; `describeItemType` additionally ties it to the concrete product classes (part of V-01).

- **SalePersistence — Violation (V-02).** It depends on three services.

### V-02

| Field | Content |
|---|---|
| ID | V-02 |
| Pattern | Low Coupling |
| Module | Sales (Taller 1) and integration adjustments A2 and A3 (Requerimiento 5) |
| Evidence | `SalePersistence` imports `AccessoryService`, `PersonService` and `ProductService` (`SalePersistence.java:8-10`), stores `AccessoryService` (`:39`, constructor `:49-52`), receives the other two in `loadAll(ProductService, PersonService)` (`:83`) and calls them while reading (`fromLine`, `:155-156`; `resolveItem`, `:192-198`). |
| Explanation | A persistence class depends on the service layer, the reverse of the allowed direction `service → persistence`. It reads the file and also resolves business references. Because `WarrantyService` (Developer 2) needs the stored sales too, it must receive `PersonService` only to pass it to `SalePersistence.loadAll`. |
| Consequence | The persistence of sales cannot be built without services; a change in a service lookup breaks the persistence layer; the dependency spreads to `WarrantyService`, which knows `PersonService` for no reason of its own. |
| Proposed solution | `SalePersistence` loads only references (a `SaleRecord` with the ids and the stored amounts) and a fabrication in the service layer (`SaleAssembler`) resolves them; `SaleService` and `WarrantyService` rebuild the sales through it. Patterns applied: Low Coupling and Pure Fabrication. |
| Other role's code involved | Yes — `WarrantyService` is Developer 2's code and must receive the assembler instead of `SalePersistence` and `PersonService`. |

## High Cohesion

**Verdict:** _pending — audit in progress._

- **Sale — Correct.** It holds the data of the sale, its totals and its receipt.

- **SalePersistence — Correct.** Its only purpose is the sales file (resolving references is recorded under Low Coupling, V-02).

## Polymorphism

**Verdict:** _pending — audit in progress._

- **Sale — Violation (V-01).** The receipt decides the label of each item with `instanceof`.

- **SalePersistence — Correct.** It writes only ids; no type tags are needed.

### V-01

| Field | Content |
|---|---|
| ID | V-01 |
| Pattern | Polymorphism (also recorded under Protected Variations) |
| Module | Sales (Taller 1) and integration adjustment A3 (Requerimiento 5) |
| Evidence | `Sale.describeItemType(Product)`, `Sale.java:203-214`: `if (product instanceof Console) { return "Consola"; } if (product instanceof VideoGame) { return "Videojuego"; } if (product instanceof Accessory) { return "Accesorio"; } return "Producto";`, called from `generateReceipt()`, `Sale.java:179` |
| Explanation | The receipt asks every item for its concrete class to decide the name of its type. The name of the type varies with the product type, so it is a responsibility of each product type. |
| Consequence | A new product type is printed as "Producto" until someone edits `Sale`; the sale is coupled to three concrete classes of the product hierarchy. |
| Proposed solution | Let each product give the label of its type with an abstract `getTypeLabel()` in `Product`, implemented by `VideoGame` ("Videojuego"), `Console` ("Consola") and `Accessory` ("Accesorio"), and print `product.getTypeLabel()`. Patterns applied: Polymorphism and Protected Variations. |
| Other role's code involved | Yes — the product and accessory hierarchies are Developer 1's code; Developer 1 must add `getTypeLabel()`. |

## Pure Fabrication

**Verdict:** _pending — audit in progress._

- **SalePersistence — Correct.** It keeps file access out of `Sale` (its dependency on services is recorded under Low Coupling, V-02).

## Indirection

**Verdict:** _pending — audit in progress._

## Protected Variations

**Verdict:** _pending — audit in progress._

- **Sale — Violation (V-01).** The list of product types is hard-coded in the receipt.

- **SalePersistence — Correct.** A change of the file format is confined to this class, and file errors are translated into `PersistenceException` (`SalePersistence.java:67`, `:102`, `:124`).