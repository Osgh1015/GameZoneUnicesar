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

## Information Expert

**Verdict:** _pending — audit in progress._

- **Products — Violation (V-01).** `VideoGame.getDescription` and `Console.getDescription` (`VideoGame.java:45`, `Console.java:45`) build the description from their own fields, which is correct, but `ProductService.decreaseStock` computes the new stock itself instead of asking the product to update it.

- **Accessories — Correct.** `Accessory.isCompatibleWith(String)` (`Accessory.java:76`) answers with the list of compatible consoles it owns; each subclass describes itself from its own attributes.

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

## Creator

**Verdict:** _pending — audit in progress._

- **Products — Correct.** `ProductRepository.fromLine` (`ProductRepository.java:118-129`) creates `VideoGame` and `Console` objects from the records it reads, so it holds the initializing data. `ProductService.registerVideoGame`/`registerConsole` (`ProductService.java:46`, `:58`) receive the product already built by the menu; the creation of domain objects inside `ConsoleMenu` is Technical Lead code and was reported to the Technical Lead.

- **Accessories, promotions, returns and warranties — Correct.** The model classes in scope do not create other domain objects; their instances are created by the repositories and services that aggregate them (Developer 2 and Technical Lead code).

## Controller

**Verdict:** _pending — audit in progress._

## Low Coupling

**Verdict:** _pending — audit in progress._

- **Products — Violation (V-02).** `Product`, `VideoGame` and `Console` import nothing outside the model, and `ProductRepository` depends only on the model, which is correct; `ProductService` is tied to the concrete `ProductRepository` and to its file because it creates it.

- **Accessories — Correct.** The accessory hierarchy depends only on `Product`.

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

**Verdict:** _pending — audit in progress._

- **Products — Correct.** `ProductRepository` only reads and writes the products file; `ProductService` only applies inventory rules. It keeps two pairs of equivalent methods (`reduceStock`/`decreaseStock`, `ProductService.java:138`; `listAll`/`listProducts`, `ProductService.java:167`) because other modules call both names; they do the same job, so the class is still focused.

- **Accessories — Correct.** Each subclass adds only its own attributes and its description.

## Polymorphism

**Verdict:** _pending — audit in progress._

- **Products — Correct.** `Product.getDescription()` (`Product.java:68`) is abstract and `VideoGame`/`Console` override it. `ProductRepository.toLine`/`fromLine` (`ProductRepository.java:105-129`) branch on the concrete type and on a type tag; this is **not** treated as a violation: the repository is a Pure Fabrication whose job is to keep the file format out of the model, and moving serialization into the hierarchy would put persistence knowledge inside the domain classes, which Taller 1 forbids.

- **Accessories — Correct.** `Controller`, `Cable` and `Memory` override `getDescription()` (`Controller.java:49`, `Cable.java:66`, `Memory.java:65`); no code in scope asks an accessory for its concrete type.

## Pure Fabrication

**Verdict:** _pending — audit in progress._

- **Products — Correct.** `ProductRepository` is an artificial class that owns file access so that `Product` stays free of I/O.

## Indirection

**Verdict:** _pending — audit in progress._

- **Products — Correct.** `ProductService` sits between `ConsoleMenu` and `ProductRepository`; the menu never touches the repository.

## Protected Variations

**Verdict:** _pending — audit in progress._

- **Products — Violation (V-02).** A change of storage format is confined to `ProductRepository`, which is correct, but the file location is fixed inside the repository and the service creates its own repository, so a change of location is not protected.

