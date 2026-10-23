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
