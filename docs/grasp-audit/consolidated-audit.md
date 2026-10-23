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
