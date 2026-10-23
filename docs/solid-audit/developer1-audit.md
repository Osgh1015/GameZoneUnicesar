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
| Returns (Requerimiento 3) | `Return`, `Sale.canBeReturned` |
| Warranties (Requerimiento 4) | `Warranty`, `BasicWarranty`, `ExtendedWarranty` |
| Exceptions and validation (Requerimiento 6) | `GameZoneException`, `ResourceNotFoundException`, `BusinessRuleException`, `InvalidDataException`, `PersistenceException`, `ProductValidator`, `PersonValidator`, `SaleValidator` |

## Single Responsibility Principle (SRP)

**Verdict:** _pending — audit in progress._

- **Products — Correct.** `Product` and its subclasses hold the product data and its description (`VideoGame.java:45`, `Console.java:45`), which the Taller defines as part of each product; `ProductRepository` only reads and writes `data/products.txt`; `ProductService` only applies the inventory rules and asks the repository to persist them.

- **Accessories — Correct.** Each subclass adds only its attributes and its description (`Controller.java:49`, `Cable.java:66`, `Memory.java:65`).

- **Promotions — Correct.** Each promotion holds its parameters and its discount calculation; `Promotion.isActive` (`Promotion.java:72`) only uses its own dates.

- **Returns — Violation (V-01).** `Return` calculates the refund and also lays out the Spanish receipt. `Sale.canBeReturned` (`Sale.java:253`) only applies the 30-day rule, which is correct.

- **Warranties — Violation (V-02).** `Warranty` holds the coverage rules and also lays out the Spanish certificate. The subclasses only declare duration, type and cost.

- **Exceptions and validation — Correct.** One exception per category of error; one validator per module, each with only the checks of that module (`ProductValidator.java:35-86`).

### V-01

| Field | Content |
|---|---|
| ID | V-01 |
| Principle | SRP |
| Module | Returns (Requerimiento 3) and integration adjustments A5 and A7 (Requerimiento 5) |
| Evidence | `Return.generateReturnReceipt()`, `Return.java:176-201`: builds the Spanish receipt line by line — title `--- Recibo de Devolución ---`, labels (`Precio de lista:`, `Descuento proporcional:`, `Motivo:`), the money format `$%.2f` and the column alignment. |
| Explanation | `Return` has two reasons to change: its business rules (the proportional refund of each item, the refund of cancelled warranties, the total) and the layout of the document the seller reads. Presentation is a different concern from the refund rule. |
| Consequence | A change in the receipt (a new label, another currency format, a different alignment) forces editing the domain class that calculates refunds, with the risk of touching the calculation. |
| Proposed solution | Move the layout into its own class `ReturnReceipt` (model package, it reads the return through its getters) and keep `generateReturnReceipt()` as a one-line delegation, because Requerimiento 3 defines it as part of the return and `ReturnMenu` calls it. |
| Other role's code involved | No. |

### V-02

| Field | Content |
|---|---|
| ID | V-02 |
| Principle | SRP |
| Module | Warranties (Requerimiento 4) |
| Evidence | `Warranty.generateWarrantyCertificate()`, `Warranty.java:144-165`: a `String.format` with the Spanish certificate — `CERTIFICADO DE GARANTÍA`, labels (`Fecha de vencimiento:`, `Costo adicional:`), the format `%.2f` and the words `VIGENTE`/`VENCIDA`. |
| Explanation | `Warranty` holds the coverage rules (end date, whether it is active, the cost of each kind) and also the layout of the certificate. They change for different reasons. |
| Consequence | A change in the certificate forces editing the base class of the whole warranty hierarchy; every subclass is recompiled and put at risk for a change that is only about text. |
| Proposed solution | Move the layout into `WarrantyCertificate` (model package, it reads the warranty through its getters) and keep `generateWarrantyCertificate()` as a one-line delegation, because Requerimiento 4 defines it and `WarrantyMenu` calls it. |
| Other role's code involved | No. |

## Open/Closed Principle (OCP)

**Verdict:** _pending — audit in progress._

- **Products — Correct for the hierarchy, accepted for the repository.** A new product type is a new subclass of `Product` that implements `getDescription`, `belongsToCategory` and `getTypeLabel` (`Product.java:86`, `:95`, `:113`); no existing class changes. `ProductRepository.toLine`/`fromLine` (`ProductRepository.java:107-128`) branch on the type and would change for a new type. This is **not** counted: a new product type brings new fields in the file, so the format changes anyway, and this class is the only one that knows the format (criterion of Q1).

- **Accessories — Correct.** A new accessory is a new subclass that implements `getDescription` and `getAccessoryType` (`Accessory.java:123`).

- **Promotions — Correct.** A new promotion is a subclass of `Promotion` that implements `calculateDiscount` (`Promotion.java:84`). Since the GRASP correction, `CategoryDiscount.calculateDiscount` (`CategoryDiscount.java:68`) asks `product.belongsToCategory`, so it no longer changes for a new product type.

- **Returns — Correct.** The refund rule does not depend on the kind of item returned.

- **Warranties — Correct for the hierarchy.** A new warranty is a subclass that implements duration, type and cost (`Warranty.java:107-123`). How each kind is stored and rebuilt is decided in `WarrantyRepository` and `WarrantyService` (Developer 2); it was reported to the Technical Lead.

- **Exceptions and validation — Correct.** A new category of error is a new subclass of `GameZoneException` (`GameZoneException.java:13`); the code that catches `GameZoneException` does not change.

## Liskov Substitution Principle (LSP)

**Verdict:** _pending — audit in progress._

- **Products — Correct.** `VideoGame` and `Console` implement `getDescription`, `belongsToCategory` and `getTypeLabel` without stronger preconditions or new exceptions; `Console.isWarrantyEligible` (`Console.java:68`) only changes a value the base class declares as overridable (`Product.java:103`). `ProductRepository` and `ProductService` have no subclasses.

- **Accessories — Correct.** `Accessory.belongsToCategory` (`Accessory.java:113`) answers for the "ACCESSORY" category, a valid answer under the base contract; `Controller`, `Cable` and `Memory` keep every behavior of `Product`.

- **Promotions — Correct.** The three subclasses return a non-negative discount for any sale and never throw (`PercentageDiscount.java:42`, `CategoryDiscount.java:68`, `BulkPurchaseDiscount.java:55`), which is what `PromotionService.findBestPromotionFor` expects from any `Promotion`.

- **Returns — Not applicable.** `Return` has no subclasses and no base class besides `Object`.

- **Warranties — Correct.** `BasicWarranty` and `ExtendedWarranty` return a positive duration, a type name and a non-negative cost; `isActive`, `covers` and the certificate behave the same for both.

- **Exceptions — Correct.** The four exceptions can be caught as `GameZoneException`; each keeps the message and the error code of the base class.

## Interface Segregation Principle (ISP)

**Verdict:** _pending — audit in progress._

- **Products — Correct.** `ProductService` uses every operation of `ProductRepository` (`save`, `load`). Developer 1's classes are mostly providers; how other roles' classes use `ProductService` is audited by them.

- **Promotions — Correct.** The promotions depend on `Sale`, the domain object they evaluate, and use its products and subtotal; they do not depend on any service.

- **Returns — Correct.** `Return` uses the products, the subtotal, the discount and the promotion of its original sale.

- **Validation — Not applicable.** The validators are static functions with no interface; each service calls only the checks of its module.

## Dependency Inversion Principle (DIP)

**Verdict:** _pending — audit in progress._

- **Products — Violation (V-03).** `ProductService` depends on the concrete `ProductRepository`.

- **Accessories, promotions, returns and warranties — Correct.** The model classes depend only on other model classes, never on persistence or services.

- **Validation — Correct, examined.** `ProductService` calls the static methods of `ProductValidator` (`ProductService.java:70`, `:112-113`). They are stateless rule checks with no alternative implementation in the system, so depending on them is not depending on a volatile detail; this was examined and not counted.

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
