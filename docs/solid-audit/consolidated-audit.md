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