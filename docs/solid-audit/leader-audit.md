# SOLID Audit — Technical Lead

| Item | Value |
|---|---|
| Role | Technical Lead (Omar Sebastian Gamez Heredia) |
| Requirement | Requerimiento 8 — SOLID audit and correction, Phase 1 (audit) |
| Audited commit | `<commit de develop>` (`develop`, with the GRASP correction of Requerimiento 7 merged) |
| Date | 2026-10-23 |
| Scope rule | Only the code under the responsibility of the Technical Lead, including what the Technical Lead created or changed in the GRASP correction (`docs/grasp-refactor-log.md`) |

Verdicts: **Violation** (the code breaks the principle at one or more points), **Correct** (the code follows the principle), **Not applicable** (the audited code has no point where the principle applies).

## Scope

| Module (requirement) | Classes and methods audited |
|---|---|
| Sales (Taller 1), promotions (Requerimiento 2), warranties (Requerimiento 4) | `Sale` (every method except `canBeReturned`, Developer 1), including the discount, the warranty cost and `generateReceipt`, and the GRASP additions `findProduct`, `countUnitsOf`, `belongsToClient`, `wasAttendedBy` |

## Single Responsibility Principle (SRP)

**Verdict:** _pending — audit in progress._

- **Sale — Violation (V-01).** `Sale` also lays out its receipt. The GRASP additions (`findProduct`, `countUnitsOf`, `belongsToClient`, `wasAttendedBy`) answer questions with the sale's own data, which is correct.

### V-01

| Field | Content |
|---|---|
| ID | V-01 |
| Principle | SRP |
| Module | Sales (Taller 1), promotions (Requerimiento 2) and warranties (Requerimiento 4) |
| Evidence | `Sale.generateReceipt()`, `Sale.java:221-245`: builds the Spanish receipt — `--- Recibo de Venta ---`, the labels `Subtotal:`, `Descuento (...)`, `Garantías extendidas:`, `Total final:`, the texts for no promotion and no warranty, and the format `$%.2f`. |
| Explanation | `Sale` holds the data and the totals of a sale and also the layout of the receipt. They change for different reasons. |
| Consequence | A change in the receipt (a new line, another format) forces editing the domain class that calculates the totals, which every module of the system uses. |
| Proposed solution | Move the layout into `SaleReceipt` (model package, it reads the sale through its getters) and keep `generateReceipt()` as a one-line delegation, because the sales module defines it and `SaleMenu` calls it. |
| Other role's code involved | No. |

## Open/Closed Principle (OCP)

**Verdict:** _pending — audit in progress._

- **Sale — Correct.** The receipt asks each product for its label (`getTypeLabel`, GRASP V-17); a new product type does not change `Sale`.

## Liskov Substitution Principle (LSP)

**Verdict:** _pending — audit in progress._

- **Sale, SalePersistence, SaleService, SaleAssembler, the user interface and Main — Not applicable.** None of them takes part in an inheritance hierarchy of the system; the hierarchies they use (`Product`, `Promotion`, `Warranty`) are audited by Developer 1.

## Interface Segregation Principle (ISP)

**Verdict:** _pending — audit in progress._

## Dependency Inversion Principle (DIP)

**Verdict:** _pending — audit in progress._
