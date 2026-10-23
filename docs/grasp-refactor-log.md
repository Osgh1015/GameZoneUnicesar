git # GRASP Refactor Log — Requerimiento 7

This log records every correction of Phase 2 and the evidence that the external behavior of the system did not change. The violations and their identifiers are those of `docs/grasp-audit/consolidated-audit.md`.

## Non-regression strategy

The correction is a refactor: for the same inputs, every menu operation must print the same text and leave the same content in the files of `data/`. The project has no automated test suite, so the team uses two checks (see Q4 in `docs/grasp-analysis.md`):

1. **Compilation.** `mvn clean compile` (or *Build → Rebuild Project* in IntelliJ) before every commit of code and before every Pull Request.
2. **Scripted run of the whole menu.** The application is run from a scratch copy of the data files, never from the repository's own `data/`, with a fixed input script of 308 entries that covers every operation of the menu:

| Module | Operations and inputs | Expected result |
|---|---|---|
| Products | Register a video game and a console; a duplicated id; a price of zero; a non-numeric price; list | Registered / "Ya existe un producto…" / "El campo 'precio' debe ser mayor que cero." / the question is repeated / list with the new products |
| People | Register a client; a duplicated id; an invalid email; list clients; list sellers | Registered / "Ya existe una persona…" / "debe tener un formato válido…" / lists |
| Accessories | Register a controller, a cable and a memory; list all; list by type CONTROLLER, `cable`, ` memory `, XYZ; compatible with P002 | Registered / filtered lists / "No hay accesorios de ese tipo." |
| Promotions | Register percentage, category and bulk promotions; an invalid category; reversed dates; an invalid date; list all; list active | Registered / "Opción de categoría inválida." / "La fecha de fin no puede ser anterior…" / "Error: Text '2026-13-01'…" / lists |
| Sales | A sale with a console with extended warranty, an accessory and a video game; an unknown client; an unknown item; a sale without items; insufficient stock; the full history; by client; by seller; the receipt of an existing and of a missing sale | Receipt with discount and warranties / the four error messages / histories / receipt / "No se encontró el recurso solicitado…" |
| Returns | A console with warranties and an accessory of a recent sale; more units than sold; a video game; a second console; a product not in the sale; a sale older than 30 days; a missing sale; no products; list all; by client; by sale; monthly balance | Return receipts with refunds and cancelled warranties / the five error messages / lists / balance |
| Warranties | The warranty of a product in a sale (existing, cancelled and missing); list all; active; expiring in 30 days | Warranty or "no tiene garantía" / lists |
| Main menu | An invalid option and a non-numeric option; exit | "Opción inválida." / "¡Hasta la próxima!" |

The data for the run are the files of `data/` at the audited commit plus three seeded records: a sale of today with a console, an accessory, two units of a video game and a second console (with its basic and extended warranties and one unit already returned), and a sale of 45 days ago. The standard output and the final content of every data file are captured. The random identifiers (UUID) become `<uuid>` and the dates of the run become `<TODAY>` and `<OLD>`; the normalized result is compared with the baseline line by line with `diff`.

When it runs: on `develop` before the first correction (baseline), on every Pull Request of Phase 2 over the operations it affects, and at the end of the phase over all the operations (final verification, recorded below).

## Baseline

| Item | Value |
|---|---|
| Commit | `<hash de develop>` (`develop`, after the merge of the consolidated audit) |
| Date | 2026-10-23 |
| Input script | 308 entries |
| Normalized result | 1676 lines (output of the menu and content of the 8 data files) |
| Errors or exceptions not handled | None: every error case shows its expected message |

## Change log

| Class | Change | Violation ID | Pattern applied | Commit / PR | Owner |
|---|---|---|---|---|---|
| `Product` | Added `decreaseStock(int)` and `increaseStock(int)`; the product updates its own stock | V-01 | Information Expert | `<hash A>` / PR #<n> | Developer 1 |
| `ProductService` | `decreaseStock` delegates to `product.decreaseStock(amount)` instead of computing the stock with getter and setter | V-01 | Information Expert | `<hash B>` / PR #<n> | Developer 1 |
| `Product`, `VideoGame`, `Console`, `Accessory` | Added abstract `belongsToCategory(String)`; video games answer `"VIDEOGAME"`, consoles `"CONSOLE"`, every accessory `"ACCESSORY"`, ignoring case (same results as before for every target string) | V-03 | Polymorphism, Protected Variations | `<hash C>` / PR #<n> | Developer 1 |
| `CategoryDiscount` | `calculateDiscount` sums the products for which `product.belongsToCategory(targetCategory)`; the private `instanceof` chain was removed | V-03 | Polymorphism, Low Coupling, Protected Variations | `<hash D>` / PR #<n> | Developer 1 |
| `Return` | `calculatePaidRatio` asks the sale for its subtotal with `originalSale.calculateTotal()` | V-04 | Information Expert | `<hash E>` / PR #<n> | Developer 1 |
| `ProductRepository`, `ProductService` | New constructors `ProductRepository(String filePath)` and `ProductService(ProductRepository)`; the old ones kept `@Deprecated` for one step | V-02 | Low Coupling, Protected Variations | `<hash F>` / PR #<n> | Developer 1 |
| `Main` | **Boundary coordination:** the Technical Lead (owner of `Main`) creates `ProductRepository("data/products.txt")` and injects it into `ProductService` inside Developer 1's task group | V-02 | Low Coupling | `<hash G>` / PR #<n> | Technical Lead |
| `ProductRepository`, `ProductService` | Removed the transitional `@Deprecated` constructors; `ProductService` no longer creates its repository | V-02 | Low Coupling, Protected Variations | `<hash H>` / PR #<n> | Developer 1 |
