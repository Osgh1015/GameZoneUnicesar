# Consolidated SOLID Audit — Requerimiento 8

| Item | Value |
|---|---|
| Consolidated by | Technical Lead (Omar Sebastian Gamez Heredia) |
| Audited commit | `<commit de develop>` (`develop`, with the GRASP correction of Requerimiento 7 merged) |
| Date | 2026-10-23 |
| Sources | `leader-audit.md`, `developer1-audit.md`, `developer2-audit.md` |

This document is the mandatory reference for Phase 2 (correction): the team corrects exactly the violations listed here, with the identifiers assigned here. The GRASP documents of Requerimiento 7 (`docs/grasp-*`) are not modified, and no GRASP correction may be undone by a SOLID correction.

## Consolidation work

1. **Coverage:** every production class was checked against the three scopes (table below). No class was left unaudited and none was audited twice. The methods that one role added to another role's class were audited by the role that wrote them (`Sale.canBeReturned` by Developer 1, `ProductService.restoreStock` by the Technical Lead), and the classes created in the GRASP correction were audited by the role that created them (`ItemCatalog` by Developer 2; `SaleAssembler`, `ConsoleInput` and the seven submenus by the Technical Lead).
2. **Evidence:** every violation of the three reports cites class, method and code fragment or line range on the audited commit, states a consequence and a proposed solution, and says whether another role's code is involved. All the cited lines were checked against the audited commit. No finding had to be returned to its author.
3. **Definitive identifiers:** the reports number their findings locally (V-01, V-02, …). The definitive identifiers are assigned here in the order Developer 1 → Developer 2 → Technical Lead, which is also the order of the corrections (see Q5 in `docs/solid-analysis.md`).
4. **Team verdicts:** a principle is a team-level violation when at least one report holds a confirmed violation for it.
5. **Criterion:** the same criterion for every report (Q1 in `docs/solid-analysis.md`): a violation needs a likely change of this system that the current code makes expensive or risky. Warning signs that do not meet it were examined and accepted with their reason.

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
| persistence | `PersonRepository`, `AccessoryRepository`, `PromotionRepository`, `ReturnRepository` (with `ReturnRecord`), `WarrantyRepository` (with `WarrantyRecord`) | Developer 2 |
| persistence | `SalePersistence` (statement: `SaleRepository`, with `SaleRecord`) | Technical Lead |
| service | `ProductService` (except `restoreStock`) | Developer 1 |
| service | `ProductService.restoreStock` | Technical Lead |
| service | `PersonService`, `AccessoryService`, `PromotionService`, `ReturnService`, `WarrantyService`, `ItemCatalog` | Developer 2 |
| service | `SaleService`, `SaleAssembler` | Technical Lead |
| ui | `ConsoleMenu`, `ConsoleInput`, `ProductMenu`, `PersonMenu`, `SaleMenu`, `AccessoryMenu`, `ReturnMenu`, `PromotionMenu`, `WarrantyMenu` | Technical Lead |
| (root) | `Main` | Technical Lead |
| exceptions | `GameZoneException`, `ResourceNotFoundException`, `BusinessRuleException`, `InvalidDataException`, `PersistenceException` | Developer 1 |
| validation | `ProductValidator`, `PersonValidator`, `SaleValidator` | Developer 1 |
| (use of exceptions and validators in services and repositories) | — | Developer 2 |

Totals: 53 production classes — Developer 1: 25, Developer 2: 14, Technical Lead: 14 (plus `ProductService.restoreStock`).

## Team verdict per principle

| Principle | Technical Lead | Developer 1 | Developer 2 | Team verdict |
|---|---|---|---|---|
| Single Responsibility (SRP) | Violation | Violation | Violation | **Violation** |
| Open/Closed (OCP) | Violation | Correct | Violation | **Violation** |
| Liskov Substitution (LSP) | Not applicable | Correct | Correct | **Correct** |
| Interface Segregation (ISP) | Violation | Correct | Violation | **Violation** |
| Dependency Inversion (DIP) | Violation | Violation | Violation | **Violation** |

LSP holds across the system: the subclasses of `Product`, `Accessory`, `Promotion`, `Warranty`, `Person` and `GameZoneException` can be used wherever their base class is expected; none strengthens a precondition, weakens a result or throws an exception the base class does not declare. The Technical Lead's classes take part in no hierarchy, so the principle does not apply to them.

## Definitive violations

| ID | Principle | Class / method | Evidence (audited commit) | Owner | Source |
|---|---|---|---|---|---|
| V-01 | SRP | `Return.generateReturnReceipt` | `Return.java:176-201`: the layout of the return receipt inside the domain class | Developer 1 | developer1-audit V-01 |
| V-02 | SRP | `Warranty.generateWarrantyCertificate` | `Warranty.java:144-165`: the layout of the warranty certificate inside the domain class | Developer 1 | developer1-audit V-02 |
| V-03 | DIP | `ProductService` | Field and constructor of the concrete `ProductRepository` | Developer 1 | developer1-audit V-03 |
| V-04 | DIP | `PersonService`, `AccessoryService`, `PromotionService`, `ReturnService`, `WarrantyService` | Fields and constructors of the concrete repositories | Developer 2 | developer2-audit V-01 |
| V-05 | OCP (also DIP) | `ItemCatalog` | `ItemCatalog.java:19-20`, `:56`, `:71`: concrete `ProductService`/`AccessoryService` and `instanceof Accessory` | Developer 2 | developer2-audit V-05 |
| V-06 | OCP | `WarrantyRepository`, `WarrantyService` | `WarrantyRepository.java:126-128`, `WarrantyService.java:86-87`, `:217`, `:276`: the kinds of warranty decided with tags and `instanceof` | Developer 2 | developer2-audit V-04 |
| V-07 | ISP (also DIP) | `ReturnService` | `ReturnService.java:20`: the whole `SaleService` for `findById` and `getSalesHistory` | Developer 2 | developer2-audit V-02 |
| V-08 | SRP | `ReturnService` (monthly balance) | `ReturnService.java:222-270`: the accounting report inside the returns service | Developer 2 | developer2-audit V-03 |
| V-09 | SRP | `Sale.generateReceipt` | `Sale.java:221-245`: the layout of the sale receipt inside the domain class | Technical Lead | leader-audit V-01 |
| V-10 | DIP | `SaleService`, `SaleAssembler` | `SaleService.java:41`, `:59`; `SaleAssembler.java:27`, `:38`: the concrete `SalePersistence` | Technical Lead | leader-audit V-02 |
| V-11 | ISP (also DIP) | `SaleService`, `SaleAssembler` | `SaleService.java:42`, `:44`, `:45`; `SaleAssembler.java:28`: the whole `PersonService`, `PromotionService` and `WarrantyService` for one or two operations | Technical Lead | leader-audit V-03 |
| V-12 | OCP | `ConsoleMenu` | `ConsoleMenu.java:51-61`, `:77-83`, `:90-117`: every module listed in the constructor, the printed menu and the `switch` | Technical Lead | leader-audit V-04 |

Total: 12 violations — Developer 1: 3, Developer 2: 5, Technical Lead: 4.

### Agreed solution per violation

| ID | Agreed solution | Boundary commit (owner of the other class) |
|---|---|---|
| V-01 | `ReturnReceipt` (model) lays out the receipt; `generateReturnReceipt()` delegates to it | — |
| V-02 | `WarrantyCertificate` (model) lays out the certificate; `generateWarrantyCertificate()` delegates to it | — |
| V-03 | `ProductStore` (persistence) implemented by `ProductRepository`; `ProductService` depends on it | — |
| V-04 | `PersonStore`, `AccessoryStore`, `PromotionStore`, `ReturnStore`, `WarrantyStore` implemented by the repositories; the services depend on them | — |
| V-05 | `ItemSource` (service) implemented by `ProductService` and `AccessoryService`; `ItemCatalog` asks its sources in order | `ProductService implements ItemSource` (Developer 1) |
| V-06 | `WarrantyKind` (model) builds each kind and tells the preferred coverage; the repository and the service use it | `Warranty.getKind()` and its overrides (Developer 1) |
| V-07 | `SaleLookup` (service) with `findById` and `getSalesHistory`; `ReturnService` depends on it | `SaleService implements SaleLookup` (Technical Lead) |
| V-08 | `MonthlyBalanceService` with the same calculation; the balance is removed from `ReturnService` | `ReturnMenu`/`ConsoleMenu` ask the new service (Technical Lead) |
| V-09 | `SaleReceipt` (model) lays out the receipt; `generateReceipt()` delegates to it | — |
| V-10 | `SaleStore` (persistence) implemented by `SalePersistence`; `SaleService` and `SaleAssembler` depend on it | — |
| V-11 | `PersonLookup`, `PromotionSelector`, `WarrantyAssigner` (service), defined by the client; `SaleService` and `SaleAssembler` depend on them | `PersonService`, `PromotionService`, `WarrantyService` implement them (Developer 2) |
| V-12 | `MenuOption` (ui) implemented by every submenu; `ConsoleMenu` prints and runs a list of options composed by `Main` | — |

Rules for every solution: the delegating methods keep their name and their text, so the user interface sees the same receipts; the new interfaces accept the same objects that `Main` already creates, so `Main` only changes for V-12; every commit compiles.

### Order of the corrections

1. **Developer 1 — V-01, V-02, V-03.** No dependency on other corrections.
2. **Developer 2 — V-04, V-05, V-06, V-07, V-08.** Inside this group: Developer 1 implements `ItemSource` in `ProductService` (after `ItemSource` exists) and adds `getKind()` to the warranties (after `WarrantyKind` exists); the Technical Lead implements `SaleLookup` in `SaleService` and makes the returns menu ask `MonthlyBalanceService` (before the balance is removed from `ReturnService`).
3. **Technical Lead — V-09, V-10, V-11, V-12.** Inside this group: Developer 2 implements `PersonLookup`, `PromotionSelector` and `WarrantyAssigner` (after the Technical Lead creates them, before `SaleService` depends on them). V-12 is the last correction because `Main` composes the final services and menus.