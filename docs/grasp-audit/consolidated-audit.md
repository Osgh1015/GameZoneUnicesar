# Consolidated GRASP Audit — Requerimiento 7

| Item | Value |
|---|---|
| Consolidated by | Technical Lead (Omar Sebastian Gamez Heredia) |
| Audited commit | `4027852` (`develop`) |
| Date | 2026-10-23 |
| Sources | `leader-audit.md`, `developer1-audit.md`, `developer2-audit.md` |

This document is the mandatory reference for Phase 2 (correction): the team corrects exactly the violations listed here, with the identifiers assigned here.

## Consolidation work

1. **Coverage:** every production class was checked against the three scopes (table below). No class was left unaudited and none was audited twice. The methods that one role added to another role's class were audited by the role that wrote them (`Sale.canBeReturned` by Developer 1, `ProductService.restoreStock` by the Technical Lead, the validator and exception calls added to the Taller 1 services in Requerimiento 6 by Developer 2).
2. **Evidence:** every violation of the three reports cites class, method and code fragment or line range on `4027852`, states a consequence and a proposed solution, and says whether another role's code is involved. All the cited lines were checked against the audited commit. No finding had to be returned to its author.
3. **Definitive identifiers:** the reports number their findings locally (V-01, V-02, …). The definitive identifiers are assigned here in the order Developer 1 → Developer 2 → Technical Lead, which is also the order of the corrections (see Q5 in `docs/grasp-analysis.md`).
4. **Team verdicts:** a pattern is a team-level violation when at least one report holds a confirmed violation for it.

## Coverage

| Package | Class | Audited by |
|---|---|---|
| model | `Product`, `VideoGame`, `Console` | Developer 1 |
| model | `Accessory`, `Controller`, `Cable`, `Memory` | Developer 1 |
| model | `Promotion`, `PercentageDiscount`, `CategoryDiscount`, `BulkPurchaseDiscount` | Developer 1 |
| model | `Return` | Developer 1 |
| model | `Warranty`, `BasicWarranty`, `ExtendedWarranty` | Developer 1 |
| model | `Person`, `Client` (statement: `Customer`), `Seller` | Developer 2 |
| model | `Sale` (except `canBeReturned`) | Technical Lead |
| model | `Sale.canBeReturned` | Developer 1 |
| persistence | `ProductRepository` | Developer 1 |
| persistence | `PersonRepository`, `AccessoryRepository`, `PromotionRepository`, `ReturnRepository`, `WarrantyRepository` | Developer 2 |
| persistence | `SalePersistence` (statement: `SaleRepository`) | Technical Lead |
| service | `ProductService` (except `restoreStock`) | Developer 1 |
| service | `ProductService.restoreStock` | Technical Lead |
| service | `PersonService`, `AccessoryService`, `PromotionService`, `ReturnService`, `WarrantyService` | Developer 2 |
| service | `SaleService` | Technical Lead |
| ui | `ConsoleMenu` | Technical Lead |
| (root) | `Main` | Technical Lead |
| exceptions | `GameZoneException`, `ResourceNotFoundException`, `BusinessRuleException`, `InvalidDataException`, `PersistenceException` | Developer 1 |
| validation | `ProductValidator`, `PersonValidator`, `SaleValidator` | Developer 1 |
| (use of exceptions and validators in services and repositories) | — | Developer 2 |

Totals: 43 production classes — Developer 1: 25, Developer 2: 13, Technical Lead: 5 (plus `ProductService.restoreStock`).

## Team verdict per pattern

| Pattern | Technical Lead | Developer 1 | Developer 2 | Team verdict |
|---|---|---|---|---|
| Information Expert | Violation | Violation | Violation | **Violation** |
| Creator | Correct | Correct | Correct | **Correct** |
| Controller | Correct | Not applicable | Not applicable | **Correct** |
| Low Coupling | Violation | Violation | Violation | **Violation** |
| High Cohesion | Violation | Correct | Correct | **Violation** |
| Polymorphism | Violation | Violation | Violation | **Violation** |
| Pure Fabrication | Correct | Correct | Correct | **Correct** |
| Indirection | Violation | Correct | Violation | **Violation** |
| Protected Variations | Violation | Violation | Violation | **Violation** |

Creator and Pure Fabrication are applied correctly across the whole system: every entity is created by the class that holds its data or aggregates it, and the repositories and validators keep files and rule checks out of the model. Controller only applies to `ConsoleMenu`, which delegates every event to a service.

## Definitive violations

| ID | Source | Pattern | Evidence (summary) | Correction owner |
|---|---|---|---|---|
| V-01 | developer1-audit V-01 | Information Expert | `ProductService.decreaseStock` computes the stock with `getQuantity`/`setQuantity` (`ProductService.java:112`) | Developer 1 |
| V-02 | developer1-audit V-02 | Low Coupling | `ProductService` creates its own `ProductRepository`, which has a fixed file (`ProductService.java:34`, `ProductRepository.java:34`) | Developer 1 (+ Technical Lead in `Main`) |
| V-03 | developer1-audit V-03 | Polymorphism | `CategoryDiscount.belongsToCategory` with `instanceof VideoGame/Console/Accessory` (`CategoryDiscount.java:86-97`) | Developer 1 |
| V-04 | developer1-audit V-04 | Information Expert | `Return.calculatePaidRatio` recalculates the subtotal of the sale (`Return.java:100-108`) | Developer 1 |
| V-05 | developer2-audit V-01 | Low Coupling | `PersonService` creates its own `PersonRepository`, which has fixed files (`PersonService.java:39`, `PersonRepository.java:32-33`) | Developer 2 (+ Technical Lead in `Main`) |
| V-06 | developer2-audit V-02 | Information Expert | `AccessoryService.decreaseStock`/`restoreStock` compute the stock (`AccessoryService.java:153`, `:186`) | Developer 2 |
| V-07 | developer2-audit V-03 | Polymorphism | `AccessoryService.matchesType` with `instanceof Controller/Cable/Memory` (`AccessoryService.java:164-172`) | Developer 2 (+ Developer 1 in the accessory hierarchy) |
| V-08 | developer2-audit V-04 | Information Expert | `ReturnService` navigates `getOriginalSale().getClient().getId()` (`ReturnService.java:190`, `:204`) | Developer 2 (+ Developer 1 in `Return`) |
| V-09 | developer2-audit V-05 | Information Expert | `ReturnService` searches and counts the products of the sale (`ReturnService.java:77-84`, `:149-157`) | Developer 2 (+ Technical Lead in `Sale`) |
| V-10 | developer2-audit V-06 | Low Coupling | `ReturnRepository` depends on `SaleService`, `ProductService` and `AccessoryService` (`ReturnRepository.java:4-6`, `:27-37`, `:97`, `:120-126`) | Developer 2 (+ Technical Lead in `Main`) |
| V-11 | developer2-audit V-07 | Polymorphism | `ReturnService.registerReturn` decides warranties and stock with `instanceof Console`/`instanceof Accessory` (`ReturnService.java:101`, `:109-113`) | Developer 2 (+ Technical Lead in `Main`) |
| V-12 | developer2-audit V-08 | Information Expert | `WarrantyService` navigates `getProduct().getId()` and `getSale().getId()` (`WarrantyService.java:211-212`, `:270-271`, `:320`) | Developer 2 (+ Developer 1 in `Warranty`) |
| V-13 | leader-audit V-03 | Information Expert | `SaleService` history queries and `ConsoleMenu.showWarrantiesOfSale` navigate `Sale` and `Warranty` (`SaleService.java:312`, `:330`; `ConsoleMenu.java:462`) | Technical Lead |
| V-14 | leader-audit V-08 | Information Expert | `ProductService.restoreStock` computes the stock (`ProductService.java:158`) | Technical Lead |
| V-15 | leader-audit V-06 | Information Expert | `ConsoleMenu.askForExtendedWarranty` calculates the extended warranty price (`ConsoleMenu.java:440-441`) | Technical Lead (+ Developer 1 in `ExtendedWarranty`) |
| V-16 | leader-audit V-04 | Polymorphism | Warranty eligibility with `instanceof Console` (`SaleService.java:236`; `ConsoleMenu.java:398`, `:434`) | Technical Lead (+ Developer 1 in `Product`, `Console`) |
| V-17 | leader-audit V-01 | Polymorphism | `Sale.describeItemType` with `instanceof` (`Sale.java:203-214`) | Technical Lead (+ Developer 1 in the product hierarchy) |
| V-18 | leader-audit V-05 | Indirection | `SaleService` and `SalePersistence` repeat the item lookup and the stock dispatch (`SaleService.java:256-280`; `SalePersistence.java:192-198`) | Technical Lead (+ Developer 2 in `ItemCatalog`) |
| V-19 | leader-audit V-02 | Low Coupling | `SalePersistence` depends on three services (`SalePersistence.java:8-10`, `:39`, `:49-52`, `:83`, `:155-156`, `:192-198`) | Technical Lead (+ Developer 2 in `WarrantyService`) |
| V-20 | leader-audit V-07 | High Cohesion | `ConsoleMenu` holds the user interface of seven modules in 1026 lines | Technical Lead |

Distinct violations: 20 — Developer 1: 4, Developer 2: 8, Technical Lead: 8.

### Agreed solution per violation

| ID | Agreed solution | Pattern applied |
|---|---|---|
| V-01 | `Product.decreaseStock(int)` and `increaseStock(int)`; `ProductService.decreaseStock` delegates | Information Expert |
| V-02 | `ProductRepository(String)` and `ProductService(ProductRepository)`, injected from `Main` | Low Coupling, Protected Variations |
| V-03 | Abstract `Product.belongsToCategory(String)`, implemented by `VideoGame`, `Console` and `Accessory`; `CategoryDiscount` delegates | Polymorphism, Protected Variations |
| V-04 | `Return.calculatePaidRatio` asks `originalSale.calculateTotal()` | Information Expert |
| V-05 | `PersonRepository(String, String)` and `PersonService(PersonRepository)`, injected from `Main` | Low Coupling, Protected Variations |
| V-06 | `AccessoryService` asks the accessory to `decreaseStock`/`increaseStock` (methods of V-01) | Information Expert |
| V-07 | Abstract `Accessory.getAccessoryType()`, implemented by each subclass; `AccessoryService` compares it | Polymorphism, Protected Variations |
| V-08 | `Return.belongsToClient(String)` and `belongsToSale(String)`, used by `ReturnService` | Information Expert |
| V-09 | `Sale.findProduct(String)` and `countUnitsOf(String)`, used by `ReturnService` | Information Expert |
| V-10 | `ReturnRepository` loads `ReturnRecord` objects; `ReturnService` resolves them | Low Coupling, Pure Fabrication |
| V-11 | New `ItemCatalog` (service layer) for lookup and stock; `WarrantyService` is asked for every returned item | Polymorphism, Indirection, Pure Fabrication |
| V-12 | `Warranty.covers(String, String)` and `belongsToSale(String)`, used by `WarrantyService` | Information Expert |
| V-13 | `Sale.belongsToClient(String)` and `wasAttendedBy(String)`; the menu uses `Warranty.belongsToSale` (V-12) | Information Expert |
| V-14 | `restoreStock` asks `product.increaseStock(amount)` (method of V-01) | Information Expert |
| V-15 | `ExtendedWarranty.quoteFor(Product)`, used by `getAdditionalCost()` and by the menu | Information Expert, Protected Variations |
| V-16 | `Product.isWarrantyEligible()` (false), overridden by `Console` (true), asked by the service and the menu | Polymorphism, Protected Variations |
| V-17 | Abstract `Product.getTypeLabel()`, implemented by `VideoGame`, `Console` and `Accessory` | Polymorphism, Protected Variations |
| V-18 | `SaleService` uses `ItemCatalog` (V-11) for lookup and stock, with a new `ItemCatalog.decreaseStock` | Indirection |
| V-19 | `SalePersistence` loads `SaleRecord` objects; new `SaleAssembler` (service layer) rebuilds the sales for `SaleService` and `WarrantyService` | Low Coupling, Pure Fabrication |
| V-20 | `ConsoleMenu` keeps the main loop and the exception handling; one class per submenu plus a shared `ConsoleInput` | High Cohesion, Pure Fabrication |

### Order of the corrections

1. **Developer 1:** V-01, V-03, V-04, V-02 (with the Technical Lead's commit in `Main` inside the group).
2. **Developer 2:** first the boundary commits of the model (Developer 1: `Accessory.getAccessoryType`, `Return.belongsToClient`/`belongsToSale`, `Warranty.covers`/`belongsToSale`; Technical Lead: `Sale.findProduct`/`countUnitsOf`), then V-06, V-07, V-08, V-09, V-12, V-11, V-10, V-05 (with the Technical Lead's commit in `Main`).
3. **Technical Lead:** first the boundary commits (Developer 1: `Product.isWarrantyEligible`, `Product.getTypeLabel`, `ExtendedWarranty.quoteFor`; Developer 2: `ItemCatalog.decreaseStock`), then V-13, V-14, V-15, V-16, V-17, V-19 and V-18 (with Developer 2's commits in `WarrantyService`), and V-20.

## Boundary findings

A boundary finding is a violation whose correction needs code of more than one role. Each one is registered once, above, with its single identifier; this table says how its correction is split. The rule is the one of the statement: whoever is responsible for the code that changes makes that change, and the Technical Lead fixes the order of the commits.

| ID | How the correction is split |
|---|---|
| V-02 | Developer 1 adds the new constructors and keeps the old ones `@Deprecated`; the Technical Lead rewires `Main`; Developer 1 removes the deprecated constructors. |
| V-05, V-10, V-11 | Developer 2 adds the new constructors and keeps the old ones `@Deprecated`; the Technical Lead rewires `Main` in one commit for the three; Developer 2 removes the deprecated constructors. |
| V-07 | Developer 1 adds `Accessory.getAccessoryType()` and its three implementations; Developer 2 uses it in `AccessoryService`. |
| V-08 | Developer 1 adds `Return.belongsToClient`/`belongsToSale`; Developer 2 uses them in `ReturnService`. |
| V-09 | The Technical Lead adds `Sale.findProduct`/`countUnitsOf`; Developer 2 uses them in `ReturnService`. |
| V-12 | Developer 1 adds `Warranty.covers`/`belongsToSale`; Developer 2 uses them in `WarrantyService` (and the Technical Lead in `ConsoleMenu`, V-13). |
| V-15 | Developer 1 adds `ExtendedWarranty.quoteFor`; the Technical Lead uses it in `ConsoleMenu`. |
| V-16 | Developer 1 adds `Product.isWarrantyEligible` and its override in `Console`; the Technical Lead uses it in `SaleService` and `ConsoleMenu`. |
| V-17 | Developer 1 adds `Product.getTypeLabel` and its implementations; the Technical Lead uses it in `Sale` and `ConsoleMenu`. |
| V-18 | Developer 2 adds `ItemCatalog.decreaseStock` (the class is Developer 2's, created for V-11); the Technical Lead uses `ItemCatalog` in `SaleService` and `SaleAssembler`. |
| V-19 | The Technical Lead changes `SalePersistence`, creates `SaleAssembler` and changes `SaleService` keeping the old API `@Deprecated`; Developer 2 changes `WarrantyService` to use `SaleAssembler`; the Technical Lead rewires `Main`; Developer 2 and the Technical Lead remove their deprecated code. |

### Related findings kept as separate violations

- **V-01, V-06 and V-14** are the same read-compute-write stock sequence in three methods of three owners (`ProductService.decreaseStock`, `AccessoryService`, `ProductService.restoreStock`). They are kept as three violations because each fragment lives in a different method and is corrected completely by its owner; the shared part, `Product.decreaseStock`/`increaseStock`, is added once, by Developer 1, in V-01.
- **V-11 and V-18** repeat the lookup of an item in two catalogs and the stock dispatch, in the returns module and in the sales module. They share the solution: `ItemCatalog` is created once, by Developer 2, for V-11, and the Technical Lead reuses it for V-18.
- **V-10 and V-19** are the same dependency of a repository on services. `WarrantyRepository` was already correct (it returns `WarrantyRecord`), and it is the model followed by both corrections.

### Findings examined and discarded

| Finding | Reported by | Why it is not a violation |
|---|---|---|
| Type tags in `ProductRepository`, `AccessoryRepository`, `PromotionRepository` and `WarrantyRepository` | All | The repositories are Pure Fabrications whose purpose is to keep the file format out of the model; moving serialization into the hierarchy would put persistence knowledge in the domain classes. |
| `ConsoleMenu` creates `VideoGame`, `Console`, `Client` and accessories | Developer 1 | The menu holds the initializing data typed by the user (Creator); the services of the three roles receive complete objects since Taller 1, and changing it would change their public signatures without improving any responsibility. |
| `WarrantyService` distinguishes basic and extended warranties | Developer 2 | The rule itself is about the two kinds (prefer the extended coverage; cancel at most one of each per unit); nothing is asked that the warranty could answer polymorphically. |
| Equivalent method pairs (`reduceStock`/`decreaseStock`, `listAll`/`listProducts`, `findVendorById`/`findSellerById`, `listVendors`/`listSellers`) | Developers 1 and 2 | Other modules call both names; they do the same job, so the classes are still focused. Removing one name would touch the callers of three roles for no GRASP gain. |
| The 30-day window written in `Sale.canBeReturned` and in a message of `ReturnService` | Developer 1 | The rule lives in one place (`Sale.canBeReturned`); the other occurrence is the text of a user message, which this requirement must not change. |
| `WarrantyService` depends on `SalePersistence` and `PersonService` | Developer 2 | Not a separate violation: it is a consequence of V-19 and is removed by its correction. |

## Out-of-scope findings

Functional defects found during the audit. They are **not** corrected in Requerimiento 7, because correcting them would change what the system shows or stores; they are recorded so that a later requirement can address them.

| ID | Defect | Evidence | Reported by |
|---|---|---|---|
| OS-01 | The descriptions of video games and consoles are printed in English, while the rest of the menu is in Spanish. | `VideoGame.getDescription` (`VideoGame.java:45`), `Console.getDescription` (`Console.java:45`) | Developer 1 |
| OS-02 | The refund of a return and the cost of an extended warranty use the current price of the product, not the price it had on the day of the sale. | `Return.calculateItemRefund` (`Return.java:130`), `ExtendedWarranty.getAdditionalCost` (`ExtendedWarranty.java:58`) | Developer 1 |
| OS-03 | The repositories of accessories, promotions, returns and warranties print file errors to the console instead of throwing `PersistenceException`, and `AccessoryService`, `PromotionService` and `ReturnService` still throw generic exceptions (some with English messages). Requerimiento 6 left these modules out of its refactor on purpose. | `AccessoryRepository.java:51`, `:80`; `ReturnRepository.java:46`, `:67`; `WarrantyRepository.java:64`, `:108`; `AccessoryService.java:131` | Developer 2 |
| OS-04 | The extended warranty is offered for a console even when no unit is left in stock for it; the sale is rejected afterwards by the stock check. | `ConsoleMenu.askForExtendedWarranty` (`ConsoleMenu.java:430-449`) | Technical Lead |
