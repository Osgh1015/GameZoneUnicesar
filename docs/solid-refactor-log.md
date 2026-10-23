# SOLID Refactor Log — Requerimiento 8

This log records every correction of Phase 2 and the evidence that the external behavior of the system did not change. The violations and their identifiers are those of `docs/solid-audit/consolidated-audit.md`.

## Non-regression strategy

The SOLID correction is a refactor: for the same inputs, every menu operation must print the same text and leave the same content in the files of `data/` (Q4 in `docs/solid-analysis.md`). The team reuses the verification of Requerimiento 7:

1. **Compilation.** `mvn clean compile` (or *Build → Rebuild Project* in IntelliJ) before every commit of code and before every Pull Request, so the shared branch always compiles.
2. **Scripted run of the whole menu.** The application runs from a scratch copy of the data files, never from the repository's own `data/`, with the fixed input script of Requerimiento 7 (every operation of the menu, including every error case; see `docs/grasp-refactor-log.md`). Two queries of the warranty certificate of an existing sale were added at the beginning (8 entries), because V-02 moves the layout of the certificate and the script of Requerimiento 7 did not print it before the warranties were cancelled.
3. **Comparison.** The standard output and the final content of every data file are captured; the random identifiers become `<uuid>` and the dates of the run become `<TODAY>` and `<OLD>`, and the normalized result is compared with the baseline line by line with `diff`.

The receipts and the certificate are the most exposed outputs of this correction (V-01, V-02, V-09); the script prints the sale receipt (registration and query), the return receipts and the warranty certificate, so any change in their text appears in the comparison. The main menu (V-12) is printed after every operation.

When it runs: on `develop` before the first correction (baseline), on the branch before every Pull Request of Phase 2, and at the end of the phase (final verification, recorded below).

## Baseline

| Item | Value |
|---|---|
| Commit | `<hash de develop>` (`develop`, after the merge of the consolidated audit) |
| Date | 2026-10-23 |
| Input script | 316 entries (the 308 of Requerimiento 7 plus 8 for the two certificate queries) |
| Normalized result | 1730 lines (output of the menu and content of the 8 data files) |
| Errors or exceptions not handled | None: every error case shows its expected message |

## Change log

| Class | Change | Violation ID | Principle applied | Commit / PR | Owner |
|---|---|---|---|---|---|
| `ReturnReceipt` (new) | Layout of the return receipt moved out of `Return`, unchanged text | V-01 | SRP | `<hash A>` / PR #<n> | Developer 1 |
| `Return` | `generateReturnReceipt()` delegates to `ReturnReceipt`; kept because Requerimiento 3 defines it | V-01 | SRP | `<hash B>` / PR #<n> | Developer 1 |
| `WarrantyCertificate` (new) | Layout of the warranty certificate moved out of `Warranty`, unchanged text | V-02 | SRP | `<hash C>` / PR #<n> | Developer 1 |
| `Warranty` | `generateWarrantyCertificate()` delegates to `WarrantyCertificate`; kept because Requerimiento 4 defines it | V-02 | SRP | `<hash D>` / PR #<n> | Developer 1 |
| `ProductStore` (new), `ProductRepository` | New interface `ProductStore` (`save`, `load`) in the persistence package, implemented by `ProductRepository` | V-03 | DIP | `<hash E>` / PR #<n> | Developer 1 |
| `ProductService` | Depends on `ProductStore` instead of the concrete repository; `Main` unchanged | V-03 | DIP | `<hash F>` / PR #<n> | Developer 1 |

| `PersonStore` (new), `PersonRepository`, `PersonService` | New interface implemented by the repository; the service depends on it; `Main` unchanged | V-04 | DIP | `<hash A>` / PR #<n> | Developer 2 |
| `AccessoryStore` (new), `AccessoryRepository`, `AccessoryService` | Same for accessories | V-04 | DIP | `<hash B>` / PR #<n> | Developer 2 |
| `PromotionStore` (new), `PromotionRepository`, `PromotionService` | Same for promotions | V-04 | DIP | `<hash C>` / PR #<n> | Developer 2 |
| `ReturnStore` (new), `ReturnRepository`, `ReturnService` | Same for returns | V-04 | DIP | `<hash D>` / PR #<n> | Developer 2 |
| `WarrantyStore` (new), `WarrantyRepository`, `WarrantyService` | Same for warranties | V-04 | DIP | `<hash E>` / PR #<n> | Developer 2 |
| `ItemSource` (new) | Interface of a catalog of sellable items (`findItem`, `owns`, `takeStock`, `returnStock`) | V-05 | OCP, DIP | `<hash F>` / PR #<n> | Developer 2 |
| `ProductService` | Implements `ItemSource` (`findItem`, `owns`, `takeStock`, `returnStock`) | V-05 | OCP, DIP | `<hash S1>` / PR #<n> | Developer 2 |
| `AccessoryService` | Implements `ItemSource` | V-05 | OCP, DIP | `<hash G>` / PR #<n> | Developer 2 |
| `ItemCatalog` | Receives its `ItemSource`s and asks them in order; no `instanceof`; `Main` unchanged | V-05 | OCP, DIP | `<hash H>` / PR #<n> | Developer 2 |
| `WarrantyKind` (new) | Enum of the kinds of warranty: builds each warranty and tells the preferred coverage | V-06 | OCP | `<hash I>` / PR #<n> | Developer 2 |
| `Warranty`, `BasicWarranty`, `ExtendedWarranty` | Added `getKind()` to the warranty hierarchy | V-06 | OCP | `<hash S2>` / PR #<n> | Developer 2 |
| `WarrantyRepository` | Writes `getKind().name()` (same text) instead of `instanceof ExtendedWarranty` | V-06 | OCP | `<hash J>` / PR #<n> | Developer 2 |
| `WarrantyService` | Builds warranties through `WarrantyKind`, chooses with `isPreferredCoverage()`, cancels one per kind; no `instanceof` | V-06 | OCP | `<hash K>` / PR #<n> | Developer 2 |
| `SaleLookup` (new) | Read-only view of the sales (`findById`, `getSalesHistory`) | V-07 | ISP, DIP | `<hash L>` / PR #<n> | Developer 2 |
| `SaleService` | Declares `implements SaleLookup` | V-07 | ISP | `<hash O1>` / PR #<n> | Developer 2 |
| `ReturnService` | Depends on `SaleLookup` instead of `SaleService`; `Main` unchanged | V-07 | ISP, DIP | `<hash M>` / PR #<n> | Developer 2 |
| `MonthlyBalanceService` (new) | Monthly balance moved out of `ReturnService`, same calculation | V-08 | SRP | `<hash N>` / PR #<n> | Developer 2 |
| `ReturnMenu`, `ConsoleMenu` | The returns menu asks `MonthlyBalanceService` for the balance | V-08 | SRP | `<hash O2>` / PR #<n> | Developer 2 |
| `ReturnService` | Removed the monthly balance; the service only handles returns | V-08 | SRP | `<hash Q>` / PR #<n> | Developer 2 |
    