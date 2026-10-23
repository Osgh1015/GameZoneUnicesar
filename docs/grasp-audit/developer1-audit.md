# GRASP Audit — Developer 1

| Item | Value |
|---|---|
| Role | Developer 1 (Santiago Andres Herrera Acuña) |
| Requirement | Requerimiento 7 — GRASP audit and correction, Phase 1 (audit) |
| Audited commit | `4027852` (`develop`) |
| Date | 2026-10-23 |
| Scope rule | Only the code under the responsibility of Developer 1, per the responsibility table of Requerimiento 7 (role-based distribution agreed by the team) |

Verdicts: **Violation** (the code breaks the pattern at one or more points), **Correct** (the pattern is applied correctly), **Not applicable** (the audited code has no point where the pattern applies).

## Scope

| Module (requirement) | Classes and methods audited |
|---|---|
| Products (Taller 1) | `Product`, `VideoGame`, `Console`, `ProductRepository`, `ProductService` (every method except `restoreStock`, which belongs to the Technical Lead; the calls to validators and `PersistenceException` added in Requerimiento 6 belong to Developer 2) |
| Accessories (Requerimiento 1) | `Accessory`, `Controller`, `Cable`, `Memory` |
| Promotions (Requerimiento 2) | `Promotion`, `PercentageDiscount`, `CategoryDiscount`, `BulkPurchaseDiscount` |
| Returns (Requerimiento 3) | `Return`, `Sale.canBeReturned` |
| Warranties (Requerimiento 4) | `Warranty`, `BasicWarranty`, `ExtendedWarranty` |
| Exceptions and validation (Requerimiento 6) | `GameZoneException`, `ResourceNotFoundException`, `BusinessRuleException`, `InvalidDataException`, `PersistenceException`, `ProductValidator`, `PersonValidator`, `SaleValidator` |

## Information Expert

**Verdict:** Violation — see V-01 and V-04.

- **Products — Violation (V-01).** `VideoGame.getDescription` and `Console.getDescription` (`VideoGame.java:45`, `Console.java:45`) build the description from their own fields, which is correct, but `ProductService.decreaseStock` computes the new stock itself instead of asking the product to update it.

- **Accessories — Correct.** `Accessory.isCompatibleWith(String)` (`Accessory.java:76`) answers with the list of compatible consoles it owns; each subclass describes itself from its own attributes.

- **Promotions — Correct.** `Promotion.isActive(LocalDate)` (`Promotion.java:72`) uses its own start and end dates; each subclass computes its own discount for the sale it receives (`PercentageDiscount.java:42`, `BulkPurchaseDiscount.java:55`).

- **Returns — Violation (V-04).** `Return.calculateRefundAmount()` (`Return.java:141`) and `generateReturnReceipt()` (`Return.java:159`) use the data of the return, and `Sale.canBeReturned()` (`Sale.java:221-223`) decides the 30-day rule from the sale's own date, which is correct; but `calculatePaidRatio()` recalculates the subtotal of the sale instead of asking the sale.

- **Warranties — Correct.** `Warranty` derives `endDate` from `getDurationInMonths()` in its constructor (`Warranty.java:42`); `isActive(LocalDate)` (`Warranty.java:113`) uses its own dates; `ExtendedWarranty.getAdditionalCost()` (`ExtendedWarranty.java:58`) uses the price of the product it covers.

- **Exceptions and validation — Correct.** Each exception builds its message from the data it receives (`ResourceNotFoundException.java:21`). Each validator checks only the data passed by the service; `ProductValidator.validateStockAvailability` (`ProductValidator.java:60`) reads the stock of the product it receives, which is acceptable for a fabrication whose only job is to check rules.

### V-01

| Field | Content |
|---|---|
| ID | V-01 |
| Pattern | Information Expert |
| Module | Products (Taller 1) |
| Evidence | `ProductService.decreaseStock(String, int)`, `ProductService.java:112`: `product.setQuantity(product.getQuantity() - amount);` |
| Explanation | The product owns its stock, but the service reads the quantity, computes the new value and writes it back through the setter. The rule "stock goes down by the amount sold" belongs to the object that holds the stock. |
| Consequence | Every class that changes a stock repeats the same read-compute-write sequence (the same fragment exists in `ProductService.restoreStock` and in `AccessoryService`); a new rule on stock (for example, never below zero) would have to be added in every copy. |
| Proposed solution | Add `decreaseStock(int)` and `increaseStock(int)` to `Product`, so the product updates its own stock, and make `ProductService.decreaseStock` delegate to it. Pattern applied: Information Expert. |
| Other role's code involved | No for the correction of Developer 1's method. The same fragment in `ProductService.restoreStock` (Technical Lead) and in `AccessoryService` (Developer 2) was reported to the Technical Lead as a possible boundary finding. |

### V-04

| Field | Content |
|---|---|
| ID | V-04 |
| Pattern | Information Expert |
| Module | Returns (Requerimiento 3) and integration adjustment A5 (Requerimiento 5) |
| Evidence | `Return.calculatePaidRatio()`, `Return.java:100-108`: `double itemsSubtotal = 0.0; for (Product product : originalSale.getProducts()) { itemsSubtotal += product.getPrice(); }` |
| Explanation | The subtotal of the items of a sale is information that `Sale` already owns and calculates in `Sale.calculateTotal()`. `Return` reaches into the products of the sale and recalculates it on its own. |
| Consequence | The subtotal rule exists in two places; if the way a sale computes its subtotal changes, the refund would silently use a different value from the one shown on the sale receipt. |
| Proposed solution | `calculatePaidRatio()` asks the sale for its subtotal with `originalSale.calculateTotal()` and keeps the rest of the formula. Pattern applied: Information Expert. |
| Other role's code involved | No — `Sale.calculateTotal()` already exists and is only called, not changed. |

## Creator

**Verdict:** Correct — objects are created by the class that holds their initializing data.

- **Products — Correct.** `ProductRepository.fromLine` (`ProductRepository.java:118-129`) creates `VideoGame` and `Console` objects from the records it reads, so it holds the initializing data. `ProductService.registerVideoGame`/`registerConsole` (`ProductService.java:46`, `:58`) receive the product already built by the menu; the creation of domain objects inside `ConsoleMenu` is Technical Lead code and was reported to the Technical Lead.

- **Accessories, promotions, returns and warranties — Correct.** The model classes in scope do not create other domain objects; their instances are created by the repositories and services that aggregate them (Developer 2 and Technical Lead code).

- **Exceptions and validation — Correct.** Each validator creates the exception it throws at the point where it has the field name, the identifier or the rule that the message needs (`ProductValidator.java:35-86`).

## Controller

**Verdict:** Not applicable — no class in scope receives system events.

- **All modules — Not applicable.** None of the classes in scope receives a system event from the user; menu events are received by `ConsoleMenu` (Technical Lead) and coordinated by the services.

## Low Coupling

**Verdict:** Violation — see V-02 (and V-03 for the coupling of `CategoryDiscount`).

- **Products — Violation (V-02).** `Product`, `VideoGame` and `Console` import nothing outside the model, and `ProductRepository` depends only on the model, which is correct; `ProductService` is tied to the concrete `ProductRepository` and to its file because it creates it.

- **Accessories — Correct.** The accessory hierarchy depends only on `Product`.

- **Promotions — Correct except for V-03.** `Promotion` and its subclasses depend on `Sale` and `Product`, which they need to compute a discount; `CategoryDiscount` additionally depends on the concrete classes `VideoGame`, `Console` and `Accessory` (part of V-03).

- **Returns — Correct.** `Return` depends only on `Sale` and `Product`, the objects it describes.

- **Warranties — Correct.** The hierarchy depends only on `Product` and `Sale`.

- **Exceptions and validation — Correct.** The exceptions depend on nothing in the system; the validators depend only on the model and on the exceptions, never on services or repositories, as the Requerimiento 6 layer rule requires.

### V-02

| Field | Content |
|---|---|
| ID | V-02 |
| Pattern | Low Coupling (also recorded under Protected Variations) |
| Module | Products (Taller 1) |
| Evidence | `ProductService()`, `ProductService.java:34`: `this.repository = new ProductRepository();` and `ProductRepository()`, `ProductRepository.java:34`: `this.filePath = "data/products.txt";` |
| Explanation | `ProductService` decides by itself which concrete repository it uses, and the repository decides by itself which file it uses. The other services of the system (`AccessoryService`, `PromotionService`, `ReturnService`, `WarrantyService`, `SaleService`) receive their repository from `Main`; the product service is tied to one implementation and one file. |
| Consequence | The product module cannot be wired with another file (for example, a copy of `data/` for a test run) without editing two classes, and `Main` cannot decide the wiring of this module as it does for the others. A change of the storage location is not protected. |
| Proposed solution | `ProductRepository(String filePath)` and `ProductService(ProductRepository repository)`, injected from `Main` like the rest of the services, with the same file `data/products.txt`. Patterns applied: Low Coupling and Protected Variations. |
| Other role's code involved | Yes — `Main` belongs to the Technical Lead and must create the repository and pass it to the service. |

## High Cohesion

**Verdict:** Correct — each class in scope has one focused purpose.

- **Products — Correct.** `ProductRepository` only reads and writes the products file; `ProductService` only applies inventory rules. It keeps two pairs of equivalent methods (`reduceStock`/`decreaseStock`, `ProductService.java:138`; `listAll`/`listProducts`, `ProductService.java:167`) because other modules call both names; they do the same job, so the class is still focused.

- **Accessories — Correct.** Each subclass adds only its own attributes and its description.

- **Promotions — Correct.** Each promotion class holds only its parameters and its calculation.

- **Returns — Correct.** `Return` holds only the data of the return, its refund calculation and its receipt.

- **Warranties — Correct.** Each subclass contributes only its duration, its type and its cost.

- **Exceptions and validation — Correct.** One exception per category of error and one validator per module (products, people, sales).

## Polymorphism

**Verdict:** Violation — see V-03.

- **Products — Correct.** `Product.getDescription()` (`Product.java:68`) is abstract and `VideoGame`/`Console` override it. `ProductRepository.toLine`/`fromLine` (`ProductRepository.java:105-129`) branch on the concrete type and on a type tag; this is **not** treated as a violation: the repository is a Pure Fabrication whose job is to keep the file format out of the model, and moving serialization into the hierarchy would put persistence knowledge inside the domain classes, which Taller 1 forbids.

- **Accessories — Correct.** `Controller`, `Cable` and `Memory` override `getDescription()` (`Controller.java:49`, `Cable.java:66`, `Memory.java:65`); no code in scope asks an accessory for its concrete type.

- **Promotions — Mixed.** `Promotion.calculateDiscount(Sale)` (`Promotion.java:84`) is abstract and overridden by the three subclasses, which is correct use of Polymorphism. Inside `CategoryDiscount`, however, the category of each product is resolved with conditionals on the concrete type (V-03).

- **Warranties — Correct.** `getDurationInMonths()`, `getWarrantyType()` and `getAdditionalCost()` (`Warranty.java:86-102`) are abstract and overridden by `BasicWarranty` and `ExtendedWarranty`; `generateWarrantyCertificate()` (`Warranty.java:123`) is written once on the base class and uses those polymorphic facts.

- **Exceptions — Correct.** The four exceptions extend `GameZoneException` (`GameZoneException.java:13`), so the menu can handle each category by its type without asking for codes or messages.

### V-03

| Field | Content |
|---|---|
| ID | V-03 |
| Pattern | Polymorphism (also recorded under Low Coupling and Protected Variations) |
| Module | Promotions (Requerimiento 2) and integration adjustment A1 (Requerimiento 5) |
| Evidence | `CategoryDiscount.belongsToCategory(Product)`, `CategoryDiscount.java:86-97`: `if (VIDEOGAME.equalsIgnoreCase(targetCategory)) { return product instanceof VideoGame; } if (CONSOLE.equalsIgnoreCase(targetCategory)) { return product instanceof Console; } if (ACCESSORY.equalsIgnoreCase(targetCategory)) { return product instanceof Accessory; }`, called from `calculateDiscount(Sale)`, `CategoryDiscount.java:68` |
| Explanation | The discount asks every product for its concrete class and maps a category string to that class itself. The category a product belongs to varies with the product type, so it is a responsibility of each product type, not a chain of conditionals inside the promotion. |
| Consequence | Adjustment A1 already had to edit `CategoryDiscount` to add accessories; any new product type gets no category discount until someone remembers to edit this class again. `CategoryDiscount` is coupled to three concrete classes of the product hierarchy. |
| Proposed solution | Add an abstract `belongsToCategory(String category)` to `Product`, implemented by `VideoGame` (`"VIDEOGAME"`), `Console` (`"CONSOLE"`) and `Accessory` (`"ACCESSORY"`), ignoring case as today, and make `CategoryDiscount` sum the products for which `product.belongsToCategory(targetCategory)` is true. Patterns applied: Polymorphism and Protected Variations. |
| Other role's code involved | No — the product hierarchy, the accessory hierarchy and `CategoryDiscount` are all Developer 1's code. |

## Pure Fabrication

**Verdict:** Correct — the fabrications in scope keep technical and rule-checking responsibilities out of the domain classes.

- **Products — Correct.** `ProductRepository` is an artificial class that owns file access so that `Product` stays free of I/O.

- **Validation — Correct.** `ProductValidator`, `PersonValidator` and `SaleValidator` are artificial classes, with static methods and a private constructor, that concentrate the checks of each module so that services keep only business logic.

## Indirection

**Verdict:** Correct — the intermediaries in scope decouple the classes that collaborate.

- **Products — Correct.** `ProductService` sits between `ConsoleMenu` and `ProductRepository`; the menu never touches the repository.

- **Validation — Correct.** Services reach the rules through the validators, so a rule changes in one place without touching the services that use it.

- **Model classes — Not applicable.** The model classes have no collaborators that need an intermediary.

## Protected Variations

**Verdict:** Violation — see V-02 and V-03 (recorded once under their main pattern).

- **Products — Violation (V-02).** A change of storage format is confined to `ProductRepository`, which is correct, but the file location is fixed inside the repository and the service creates its own repository, so a change of location is not protected.

- **Promotions — Violation (V-03).** The mapping between category names and product classes is hard-coded inside `CategoryDiscount`, so the discount is not protected from a new product type. The full analysis is recorded once, under Polymorphism.

- **Warranties — Correct.** A new warranty type is added as a subclass without touching `Warranty` or the existing subclasses.

- **Exceptions — Correct.** A new category of error is added as a new subclass of `GameZoneException` without changing the existing exceptions; the validators protect the services from changes in the rules.

## Summary of verdicts

| Pattern | Verdict | Violations |
|---|---|---|
| Information Expert | Violation | V-01, V-04 |
| Creator | Correct | — |
| Controller | Not applicable | — |
| Low Coupling | Violation | V-02, V-03 |
| High Cohesion | Correct | — |
| Polymorphism | Violation | V-03 |
| Pure Fabrication | Correct | — |
| Indirection | Correct | — |
| Protected Variations | Violation | V-02, V-03 |

Distinct violations: 4 (V-01 to V-04). V-02 and V-03 appear under more than one pattern but are recorded only once, under the pattern that best describes their root cause.

## Notes

- Fragments that were examined and judged **not** to be violations are recorded above with their justification (the type tags in `ProductRepository`, the equivalent method pairs of `ProductService`, the stock check inside `ProductValidator`), so that the consolidation can see why they were discarded.
- Reported to the Technical Lead (problems whose origin is in another role's code, not audited here):
    - `ConsoleMenu` creates `VideoGame`, `Console`, `Client` and accessory objects before calling the services (Creator).
    - `ProductService.restoreStock` (Technical Lead) and `AccessoryService` (Developer 2) repeat the read-compute-write stock sequence of V-01 (possible boundary finding).
    - `PersonService` creates its own `PersonRepository` with a fixed file, as in V-02 (Developer 2).
    - `Sale.describeItemType` decides the item label with `instanceof` (Technical Lead).
    - The 30-day window is written as a literal both in `Sale.canBeReturned` (`Sale.java:223`) and in the error message of `ReturnService` (Developer 2).
- Candidate out-of-scope findings reported to the Technical Lead (functional, not corrected in Requerimiento 7 because correcting them would change what the user sees): the descriptions of `VideoGame` and `Console` are printed in English while the accessory descriptions are in Spanish; the refund of a return and the cost of an extended warranty use the current price of the product, not the price it had on the day of the sale.
