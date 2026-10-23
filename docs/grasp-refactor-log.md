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

| `AccessoryService` | `decreaseStock` and `restoreStock` ask the accessory to update its own stock with `decreaseStock(amount)`/`increaseStock(amount)` instead of computing it with getter and setter | V-06 | Information Expert | `<hash A>` / PR #<n> | Developer 2 |
| `Accessory`, `Controller`, `Cable`, `Memory` | **Boundary coordination:** Developer 1 (owner of the accessory hierarchy) added the abstract `getAccessoryType()`, implemented by each subclass | V-07 | Polymorphism | `<hash S1>` / PR #<n> | Developer 1 |
| `AccessoryService` | `listAccessoriesByType` compares the requested type with `accessory.getAccessoryType()`; the private `matchesType` with `instanceof` was removed | V-07 | Polymorphism, Protected Variations | `<hash B>` / PR #<n> | Developer 2 |
| `Return` | **Boundary coordination:** Developer 1 added `belongsToClient(String)` and `belongsToSale(String)` | V-08 | Information Expert | `<hash S2>` / PR #<n> | Developer 1 |
| `ReturnService` | `viewReturnsByCustomer` and `viewReturnsBySale` ask each return instead of navigating `getOriginalSale().getClient().getId()` | V-08 | Information Expert | `<hash C>` / PR #<n> | Developer 2 |
| `Sale` | **Boundary coordination:** the Technical Lead (owner of `Sale`) added `findProduct(String)` and `countUnitsOf(String)` | V-09 | Information Expert | `<hash O1>` / PR #<n> | Technical Lead |
| `ReturnService` | `registerReturn` asks the sale for each product and for the units sold; the private `countSoldUnits` was removed | V-09 | Information Expert | `<hash D>` / PR #<n> | Developer 2 |
| `Warranty` | **Boundary coordination:** Developer 1 added `covers(String, String)` and `belongsToSale(String)` | V-12 | Information Expert | `<hash S3>` / PR #<n> | Developer 1 |
| `WarrantyService` | `findWarrantyByProduct`, `cancelWarranties` and `calculateWarrantyCost` ask each warranty instead of navigating its product and sale | V-12 | Information Expert | `<hash E>` / PR #<n> | Developer 2 |
| `ItemCatalog` (new) | Intermediary in the service layer that finds a sellable item in products or accessories and restores its stock in the catalog that owns it | V-11 | Indirection, Pure Fabrication | `<hash F>` / PR #<n> | Developer 2 |
| `ReturnService` | Restores the stock through `ItemCatalog` and asks `WarrantyService.cancelWarranties` for every returned item; no `instanceof` left in the use case. Old constructor kept `@Deprecated` for one step | V-11 | Polymorphism, Indirection | `<hash G>` / PR #<n> | Developer 2 |
| `ReturnRepository`, `ReturnService` | The repository loads `ReturnRecord` objects (ids only) and no longer depends on any service; `ReturnService` resolves the sale and the items. Old constructor kept `@Deprecated` for one step | V-10 | Low Coupling | `<hash H>` / PR #<n> | Developer 2 |
| `PersonRepository`, `PersonService` | New constructors `PersonRepository(String, String)` and `PersonService(PersonRepository)`; the old ones kept `@Deprecated` for one step | V-05 | Low Coupling, Protected Variations | `<hash I>` / PR #<n> | Developer 2 |
| `Main` | **Boundary coordination:** the Technical Lead (owner of `Main`) creates `PersonRepository` and `ItemCatalog`, injects them, and builds `ReturnRepository` with the file path only, inside Developer 2's task group | V-05, V-10, V-11 | Low Coupling, Indirection | `<hash O2>` / PR #<n> | Technical Lead |
| `PersonRepository`, `PersonService`, `ReturnRepository`, `ReturnService` | Removed the transitional `@Deprecated` constructors; no service creates its repository and no repository depends on a service | V-05, V-10, V-11 | Low Coupling, Protected Variations | `<hash J>` / PR #<n> | Developer 2 |
| `Product`, `Console` | **Boundary coordination:** Developer 1 added `isWarrantyEligible()` (false in `Product`, true in `Console`) | V-16 | Polymorphism | `<hash S4>` / PR #<n> | Developer 1 |
| `Product`, `VideoGame`, `Console`, `Accessory` | **Boundary coordination:** Developer 1 added the abstract `getTypeLabel()` and its three implementations | V-17 | Polymorphism | `<hash S5>` / PR #<n> | Developer 1 |
| `ExtendedWarranty` | **Boundary coordination:** Developer 1 added `quoteFor(Product)`, also used by `getAdditionalCost()` | V-15 | Information Expert | `<hash S6>` / PR #<n> | Developer 1 |
| `ItemCatalog` | **Boundary coordination:** Developer 2 added `decreaseStock(Product, int)` | V-18 | Indirection | `<hash P4>` / PR #<n> | Developer 2 |
| `Sale` | Added `belongsToClient(String)` and `wasAttendedBy(String)` | V-13 | Information Expert | `<hash A>` / PR #<n> | Technical Lead |
| `SaleService` | `getSalesByClient` and `getSalesBySeller` ask each sale instead of navigating its client and seller | V-13 | Information Expert | `<hash B>` / PR #<n> | Technical Lead |
| `ConsoleMenu` | `showWarrantiesOfSale` asks `warranty.belongsToSale(saleId)` | V-13 | Information Expert | `<hash C>` / PR #<n> | Technical Lead |
| `ProductService` | `restoreStock` asks `product.increaseStock(amount)` | V-14 | Information Expert | `<hash D>` / PR #<n> | Technical Lead |
| `ConsoleMenu` | The extended warranty price is asked to `ExtendedWarranty.quoteFor(product)` | V-15 | Information Expert, Protected Variations | `<hash E>` / PR #<n> | Technical Lead |
| `SaleService` | `generateWarranties` asks `item.isWarrantyEligible()` instead of `instanceof Console` | V-16 | Polymorphism | `<hash F>` / PR #<n> | Technical Lead |
| `ConsoleMenu` | `askForExtendedWarranty` asks `product.isWarrantyEligible()` | V-16 | Polymorphism | `<hash G>` / PR #<n> | Technical Lead |
| `Sale` | `generateReceipt` prints `product.getTypeLabel()`; `describeItemType` and its `instanceof` chain were removed | V-17 | Polymorphism, Protected Variations | `<hash H>` / PR #<n> | Technical Lead |
| `ConsoleMenu` | `printSellableItems` builds the label with `getTypeLabel()` and `isWarrantyEligible()` | V-16, V-17 | Polymorphism | `<hash I>` / PR #<n> | Technical Lead |
| `SalePersistence` | Loads `SaleRecord` objects (ids and stored amounts) with no service; the old service-based API kept `@Deprecated` for one step | V-19 | Low Coupling | `<hash J>` / PR #<n> | Technical Lead |
| `SaleAssembler` (new) | Fabrication in the service layer that rebuilds the stored sales from their records for `SaleService` and `WarrantyService` | V-19 | Pure Fabrication, Low Coupling | `<hash K>` / PR #<n> | Technical Lead |
| `SaleService` | Loads the sales through `SaleAssembler` and finds and updates the items sold through `ItemCatalog`; `findSellableItem` and `instanceof Accessory` removed; old constructor kept `@Deprecated` for one step | V-18, V-19 | Indirection, Low Coupling | `<hash L>` / PR #<n> | Technical Lead |
| `WarrantyService` | **Boundary coordination:** Developer 2 made the service rebuild the sales through `SaleAssembler` instead of `SalePersistence` and `PersonService`; old constructor kept `@Deprecated` for one step | V-19 | Low Coupling | `<hash P5>` / PR #<n> | Developer 2 |
| `Main` | Creates `SalePersistence` with the file path only, creates `SaleAssembler` and injects it with `ItemCatalog` into `WarrantyService` and `SaleService` | V-18, V-19 | Low Coupling | `<hash M>` / PR #<n> | Technical Lead |
| `WarrantyService` | **Boundary coordination:** Developer 2 removed the deprecated constructor | V-19 | Low Coupling | `<hash P6>` / PR #<n> | Developer 2 |
| `SalePersistence`, `SaleService` | Removed the deprecated service-based loading and constructor; the persistence layer imports no service | V-18, V-19 | Low Coupling | `<hash N>` / PR #<n> | Technical Lead |
| `ConsoleInput` (new) | Single scanner and the numeric and date readers, moved unchanged from `ConsoleMenu` | V-20 | Pure Fabrication, High Cohesion | `<hash O>` / PR #<n> | Technical Lead |
| `ProductMenu` (new) | Products submenu moved out of `ConsoleMenu` | V-20 | High Cohesion | `<hash P>` / PR #<n> | Technical Lead |
| `PersonMenu` (new) | People submenu moved out of `ConsoleMenu` | V-20 | High Cohesion | `<hash Q>` / PR #<n> | Technical Lead |
| `SaleMenu` (new) | Sales submenu moved out of `ConsoleMenu` | V-20 | High Cohesion | `<hash R>` / PR #<n> | Technical Lead |
| `AccessoryMenu` (new) | Accessories submenu moved out of `ConsoleMenu` | V-20 | High Cohesion | `<hash S>` / PR #<n> | Technical Lead |
| `ReturnMenu` (new) | Returns submenu (including the monthly balance) moved out of `ConsoleMenu` | V-20 | High Cohesion | `<hash T>` / PR #<n> | Technical Lead |
| `PromotionMenu` (new) | Promotions submenu moved out of `ConsoleMenu` | V-20 | High Cohesion | `<hash U>` / PR #<n> | Technical Lead |
| `WarrantyMenu` (new) | Warranties submenu moved out of `ConsoleMenu` | V-20 | High Cohesion | `<hash V>` / PR #<n> | Technical Lead |
| `ConsoleMenu` | Keeps only the main loop, the exception handling and the creation of the submenus; its constructor did not change, so `Main` did not change | V-20 | High Cohesion | `<hash W>` / PR #<n> | Technical Lead |

## Design decisions and trade-offs

- **`instanceof` inside `ItemCatalog` (V-11, V-18).** The products and the accessories are kept by two services and persisted in two files, so something has to decide which catalog owns an item. The decision was moved from four places (`ReturnService`, `ReturnRepository`, `SaleService`, `SalePersistence`) to one intermediary. No pattern was sacrificed: the use cases no longer check types, and the single remaining check is protected behind `ItemCatalog`.
- **`ConsoleMenu` creates its submenus (V-20).** The submenus are created in the constructor of `ConsoleMenu` with the services it already received, so `Main` and its wiring did not change. `ConsoleMenu` holds the initializing data of the submenus (Creator).
- **Records with raw amounts (V-19).** `SaleRecord` keeps the discount and the warranty cost as they were written, and `SaleAssembler` parses them after checking the client and the seller, as the old code did. This keeps the same behavior with damaged files: a damaged line is still reported with the same `PersistenceException`, and a line whose client no longer exists is still skipped.
- **Out-of-scope defects untouched.** OS-01 to OS-04 of the consolidated audit were not corrected, even where a correction touched the same code (for example, `ExtendedWarranty.getAdditionalCost` still uses the current price, OS-02).

## Final verification

| Item | Value |
|---|---|
| Commit | `<hash final de feature/grasp-refactor>` |
| Date | 2026-10-23 |
| Input script | the same 308 entries of the baseline |
| Normalized result | 1676 lines |
| Comparison with the baseline | **No differences** (`diff` empty) |
| Compilation | `mvn clean compile`: BUILD SUCCESS |

Intermediate runs: the script was also run after the Pull Request of each role (Developer 1, Developer 2) over the full menu, with no differences.
