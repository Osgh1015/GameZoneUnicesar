# SOLID Audit — Developer 1

| Item | Value |
|---|---|
| Role | Developer 1 (Santiago Andres Herrera Acuña) |
| Requirement | Requerimiento 8 — SOLID audit and correction, Phase 1 (audit) |
| Audited commit | `<commit de develop>` (`develop`, with the GRASP correction of Requerimiento 7 merged) |
| Date | 2026-10-23 |
| Scope rule | Only the code under the responsibility of Developer 1, including what Developer 1 created or changed in the GRASP correction (`docs/grasp-refactor-log.md`) |

Verdicts: **Violation** (the code breaks the principle at one or more points), **Correct** (the code follows the principle), **Not applicable** (the audited code has no point where the principle applies).

## Scope

| Module (requirement) | Classes and methods audited |
|---|---|
| Products (Taller 1) | `Product`, `VideoGame`, `Console`, `ProductRepository`, `ProductService` (every method except `restoreStock`, which belongs to the Technical Lead) |
| Accessories (Requerimiento 1) | `Accessory`, `Controller`, `Cable`, `Memory` |
| Promotions (Requerimiento 2) | `Promotion`, `PercentageDiscount`, `CategoryDiscount`, `BulkPurchaseDiscount` |

## Single Responsibility Principle (SRP)

**Verdict:** _pending — audit in progress._

- **Products — Correct.** `Product` and its subclasses hold the product data and its description (`VideoGame.java:45`, `Console.java:45`), which the Taller defines as part of each product; `ProductRepository` only reads and writes `data/products.txt`; `ProductService` only applies the inventory rules and asks the repository to persist them.

- **Accessories — Correct.** Each subclass adds only its attributes and its description (`Controller.java:49`, `Cable.java:66`, `Memory.java:65`).

- **Promotions — Correct.** Each promotion holds its parameters and its discount calculation; `Promotion.isActive` (`Promotion.java:72`) only uses its own dates.

## Open/Closed Principle (OCP)

**Verdict:** _pending — audit in progress._

- **Products — Correct for the hierarchy, accepted for the repository.** A new product type is a new subclass of `Product` that implements `getDescription`, `belongsToCategory` and `getTypeLabel` (`Product.java:86`, `:95`, `:113`); no existing class changes. `ProductRepository.toLine`/`fromLine` (`ProductRepository.java:107-128`) branch on the type and would change for a new type. This is **not** counted: a new product type brings new fields in the file, so the format changes anyway, and this class is the only one that knows the format (criterion of Q1).

- **Accessories — Correct.** A new accessory is a new subclass that implements `getDescription` and `getAccessoryType` (`Accessory.java:123`).

- **Promotions — Correct.** A new promotion is a subclass of `Promotion` that implements `calculateDiscount` (`Promotion.java:84`). Since the GRASP correction, `CategoryDiscount.calculateDiscount` (`CategoryDiscount.java:68`) asks `product.belongsToCategory`, so it no longer changes for a new product type.

## Liskov Substitution Principle (LSP)

**Verdict:** _pending — audit in progress._

- **Products — Correct.** `VideoGame` and `Console` implement `getDescription`, `belongsToCategory` and `getTypeLabel` without stronger preconditions or new exceptions; `Console.isWarrantyEligible` (`Console.java:68`) only changes a value the base class declares as overridable (`Product.java:103`). `ProductRepository` and `ProductService` have no subclasses.

- **Accessories — Correct.** `Accessory.belongsToCategory` (`Accessory.java:113`) answers for the "ACCESSORY" category, a valid answer under the base contract; `Controller`, `Cable` and `Memory` keep every behavior of `Product`.

- **Promotions — Correct.** The three subclasses return a non-negative discount for any sale and never throw (`PercentageDiscount.java:42`, `CategoryDiscount.java:68`, `BulkPurchaseDiscount.java:55`), which is what `PromotionService.findBestPromotionFor` expects from any `Promotion`.

## Interface Segregation Principle (ISP)

**Verdict:** _pending — audit in progress._

- **Products — Correct.** `ProductService` uses every operation of `ProductRepository` (`save`, `load`). Developer 1's classes are mostly providers; how other roles' classes use `ProductService` is audited by them.

- **Promotions — Correct.** The promotions depend on `Sale`, the domain object they evaluate, and use its products and subtotal; they do not depend on any service.

## Dependency Inversion Principle (DIP)

**Verdict:** _pending — audit in progress._

- **Products — Violation (V-03).** `ProductService` depends on the concrete `ProductRepository`.

- **Accessories, promotions, returns and warranties — Correct.** The model classes depend only on other model classes, never on persistence or services.

### V-03

| Field | Content |
|---|---|
| ID | V-03 |
| Principle | DIP |
| Module | Products (Taller 1) and GRASP correction V-02 (Requerimiento 7) |
| Evidence | `ProductService` field `private final ProductRepository repository` (`ProductService.java:25`) and constructor `ProductService(ProductRepository repository)` (`ProductService.java:35`). |
| Explanation | The inventory rules (high-level policy) depend on the concrete file repository (low-level detail). The GRASP correction made `Main` inject the repository, but it injects the concrete class, so the policy still depends on the detail instead of on an abstraction of "where the products are stored". |
| Consequence | Changing the storage (another file format, a database, an in-memory store for a test run) requires editing `ProductService`. |
| Proposed solution | Introduce a `ProductStore` interface (`save`, `load`) in the persistence package, implemented by `ProductRepository`, and make `ProductService` depend on it. The interface lives in persistence so that the layer rule `service → persistence` still holds; `Main` keeps passing `new ProductRepository("data/products.txt")`, so it does not change. |
| Other role's code involved | No. |
