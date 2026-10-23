# GRASP Audit — Technical Lead

| Item | Value |
|---|---|
| Role | Technical Lead (Omar Sebastian Gamez Heredia) |
| Requirement | Requerimiento 7 — GRASP audit and correction, Phase 1 (audit) |
| Audited commit | `4027852` (`develop`) |
| Date | 2026-10-23 |
| Scope rule | Only the code under the responsibility of the Technical Lead, per the responsibility table of Requerimiento 7 (role-based distribution agreed by the team) |

Verdicts: **Violation** (the code breaks the pattern at one or more points), **Correct** (the pattern is applied correctly), **Not applicable** (the audited code has no point where the pattern applies).

## Scope

| Module (requirement) | Classes and methods audited |
|---|---|
| Sales (Taller 1) and promotions (Requerimiento 2) | `Sale`, including the applied discount, the extended warranty cost and the breakdown of `generateReceipt` (every method except `canBeReturned`, which belongs to Developer 1) |

## Information Expert

**Verdict:** _pending — audit in progress._

- **Sale — Correct.** `Sale.calculateTotal()` (`Sale.java:114`) and `calculateFinalTotal()` (`:158`) use the items, the discount and the warranty cost the sale owns; `generateReceipt()` (`:169`) prints the sale's own data; `isValid()` (`:104`) checks its own items.

## Creator

**Verdict:** _pending — audit in progress._

## Controller

**Verdict:** _pending — audit in progress._

## Low Coupling

**Verdict:** _pending — audit in progress._

- **Sale — Correct except for V-01.** `Sale` depends on `Client`, `Seller` and `Product`, which it needs; `describeItemType` additionally ties it to the concrete product classes (part of V-01).

## High Cohesion

**Verdict:** _pending — audit in progress._

- **Sale — Correct.** It holds the data of the sale, its totals and its receipt.

## Polymorphism

**Verdict:** _pending — audit in progress._

- **Sale — Violation (V-01).** The receipt decides the label of each item with `instanceof`.

### V-01

| Field | Content |
|---|---|
| ID | V-01 |
| Pattern | Polymorphism (also recorded under Protected Variations) |
| Module | Sales (Taller 1) and integration adjustment A3 (Requerimiento 5) |
| Evidence | `Sale.describeItemType(Product)`, `Sale.java:203-214`: `if (product instanceof Console) { return "Consola"; } if (product instanceof VideoGame) { return "Videojuego"; } if (product instanceof Accessory) { return "Accesorio"; } return "Producto";`, called from `generateReceipt()`, `Sale.java:179` |
| Explanation | The receipt asks every item for its concrete class to decide the name of its type. The name of the type varies with the product type, so it is a responsibility of each product type. |
| Consequence | A new product type is printed as "Producto" until someone edits `Sale`; the sale is coupled to three concrete classes of the product hierarchy. |
| Proposed solution | Let each product give the label of its type with an abstract `getTypeLabel()` in `Product`, implemented by `VideoGame` ("Videojuego"), `Console` ("Consola") and `Accessory` ("Accesorio"), and print `product.getTypeLabel()`. Patterns applied: Polymorphism and Protected Variations. |
| Other role's code involved | Yes — the product and accessory hierarchies are Developer 1's code; Developer 1 must add `getTypeLabel()`. |

## Pure Fabrication

**Verdict:** _pending — audit in progress._

## Indirection

**Verdict:** _pending — audit in progress._

## Protected Variations

**Verdict:** _pending — audit in progress._

- **Sale — Violation (V-01).** The list of product types is hard-coded in the receipt.
