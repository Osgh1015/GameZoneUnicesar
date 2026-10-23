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
| Sales (Taller 1) | `SalePersistence` (the statement calls it `SaleRepository`) |
| Sales and integrations (Taller 1, Requerimientos 1, 2, 4 and 5) | `SaleService`, including the accessory integration, the application of the best promotion in `registerSale` and the assignment of warranties and extended warranty cost |
| User interface (Taller 1 to Requerimiento 6) | `ConsoleMenu`, including the submenus of every module and the differentiated handling of exceptions (Requerimiento 6) |

## Information Expert

**Verdict:** _pending — audit in progress._

- **Sale — Correct.** `Sale.calculateTotal()` (`Sale.java:114`) and `calculateFinalTotal()` (`:158`) use the items, the discount and the warranty cost the sale owns; `generateReceipt()` (`:169`) prints the sale's own data; `isValid()` (`:104`) checks its own items.

- **SalePersistence — Correct.** It writes each sale from the data the sale exposes and does not compute anything about it.

- **SaleService — Mixed.** The best promotion is asked to `PromotionService` (`SaleService.java:208`), the stock check to `ProductValidator` (`:293`) and the warranty cost to the warranty (`:245`), which is correct; the history queries navigate the sale (V-03).

- **ConsoleMenu — Violation (V-03, V-06).** `showWarrantiesOfSale` navigates the warranty to find its sale (part of V-03), and `askForExtendedWarranty` calculates the extended warranty price by itself (V-06).

### V-03

| Field | Content |
|---|---|
| ID | V-03 |
| Pattern | Information Expert |
| Module | Sales (Taller 1) and integration adjustment A3 (Requerimiento 5) |
| Evidence | `SaleService.getSalesByClient(String)`, `SaleService.java:312`: `sale.getClient().getId().equals(clientId)`; `SaleService.getSalesBySeller(String)`, `:330`: `sale.getSeller().getId().equals(sellerId)`; `ConsoleMenu.showWarrantiesOfSale(Sale)`, `ConsoleMenu.java:462`: `warranty.getSale().getId().equals(sale.getId())` |
| Explanation | The service and the menu navigate getter chains to answer questions about a sale and a warranty. The sale holds its client and its seller, and the warranty holds its sale; they are the experts. |
| Consequence | The service and the menu know the internal structure of `Sale` and `Warranty`; a change in those associations breaks them. |
| Proposed solution | Ask the sale with `Sale.belongsToClient(clientId)` and `Sale.wasAttendedBy(sellerId)`, and ask the warranty with `Warranty.belongsToSale(saleId)`. Pattern applied: Information Expert. |
| Other role's code involved | Yes for the menu: `Warranty` is Developer 1's code; `belongsToSale` is the method Developer 1 adds for the same problem in `WarrantyService` (developer2-audit V-08). `Sale` is the Technical Lead's own code. |

### V-06

| Field | Content |
|---|---|
| ID | V-06 |
| Pattern | Information Expert (also recorded under Protected Variations) |
| Module | Warranties (Requerimiento 4) and integration adjustment A3 (Requerimiento 5) |
| Evidence | `ConsoleMenu.askForExtendedWarranty`, `ConsoleMenu.java:440-441`: `System.out.printf("¿Desea agregar garantía extendida de 12 meses por $%.2f? (s/n): ", product.getPrice() * 0.10);` |
| Explanation | The menu calculates the price of the extended warranty by itself. The rule "ten percent of the price" belongs to `ExtendedWarranty`, which already has it as `COST_RATE` and uses it in `getAdditionalCost()`. |
| Consequence | The rule exists in two places of two layers; if the rate changes in `ExtendedWarranty`, the menu keeps offering the old price while the receipt charges the new one. |
| Proposed solution | Let `ExtendedWarranty` quote its cost for a product (`ExtendedWarranty.quoteFor(product)`, used also by `getAdditionalCost()`) and print that value. Patterns applied: Information Expert and Protected Variations. |
| Other role's code involved | Yes — `ExtendedWarranty` is Developer 1's code; Developer 1 must add `quoteFor`. |

## Creator

**Verdict:** _pending — audit in progress._

- **SalePersistence — Correct.** `fromLine` creates each `Sale` from the record it reads.

- **SaleService — Correct.** `createSale` (`SaleService.java:192`) creates the `Sale` it registers and aggregates, with the client, seller and items it has just validated.

- **ConsoleMenu — Correct, examined.** The menu creates `VideoGame`, `Console`, `Client` and the accessories (`ConsoleMenu.java:215`, `:236`, `:288`, `:560`, `:581`, `:602`) from the data the user has just typed, so it holds the initializing data. This was reported by Developer 1 and is **not** recorded as a violation: the services of the three roles receive complete objects since Taller 1, and moving the creation would change their public signatures without improving any responsibility.

## Controller

**Verdict:** _pending — audit in progress._

- **ConsoleMenu — Correct.** Every menu option reads the input and calls one operation of a service; the menu does not apply business rules except for the decisions recorded in V-04 and V-06, which are corrected by asking the model.

## Low Coupling

**Verdict:** _pending — audit in progress._

- **Sale — Correct except for V-01.** `Sale` depends on `Client`, `Seller` and `Product`, which it needs; `describeItemType` additionally ties it to the concrete product classes (part of V-01).

- **SalePersistence — Violation (V-02).** It depends on three services.

- **SaleService — Correct.** It receives its six collaborators by constructor from `Main` (`SaleService.java:61-74`).

- **ConsoleMenu — Correct.** It depends only on services and model classes, never on persistence; its seven service dependencies come from holding seven modules (recorded under High Cohesion, V-07).

### V-02

| Field | Content |
|---|---|
| ID | V-02 |
| Pattern | Low Coupling |
| Module | Sales (Taller 1) and integration adjustments A2 and A3 (Requerimiento 5) |
| Evidence | `SalePersistence` imports `AccessoryService`, `PersonService` and `ProductService` (`SalePersistence.java:8-10`), stores `AccessoryService` (`:39`, constructor `:49-52`), receives the other two in `loadAll(ProductService, PersonService)` (`:83`) and calls them while reading (`fromLine`, `:155-156`; `resolveItem`, `:192-198`). |
| Explanation | A persistence class depends on the service layer, the reverse of the allowed direction `service → persistence`. It reads the file and also resolves business references. Because `WarrantyService` (Developer 2) needs the stored sales too, it must receive `PersonService` only to pass it to `SalePersistence.loadAll`. |
| Consequence | The persistence of sales cannot be built without services; a change in a service lookup breaks the persistence layer; the dependency spreads to `WarrantyService`, which knows `PersonService` for no reason of its own. |
| Proposed solution | `SalePersistence` loads only references (a `SaleRecord` with the ids and the stored amounts) and a fabrication in the service layer (`SaleAssembler`) resolves them; `SaleService` and `WarrantyService` rebuild the sales through it. Patterns applied: Low Coupling and Pure Fabrication. |
| Other role's code involved | Yes — `WarrantyService` is Developer 2's code and must receive the assembler instead of `SalePersistence` and `PersonService`. |

## High Cohesion

**Verdict:** _pending — audit in progress._

- **Sale — Correct.** It holds the data of the sale, its totals and its receipt.

- **SalePersistence — Correct.** Its only purpose is the sales file (resolving references is recorded under Low Coupling, V-02).

- **SaleService — Correct.** `registerSale` is split into private steps (`resolveItems`, `createSale`, `applyBestPromotion`, `generateWarranties`, `updateInventory`), each with one purpose; the queries are all about sales.

- **ConsoleMenu — Violation (V-07).** One class holds the user interface of seven modules.

### V-07

| Field | Content |
|---|---|
| ID | V-07 |
| Pattern | High Cohesion |
| Module | User interface (Taller 1 to Requerimiento 6) |
| Evidence | `ConsoleMenu.java`, 1026 lines: the main loop with the exception handling (`start()`, `:86-155`) plus the seven submenus and all their operations: `productsMenu` (`:158`), `peopleMenu` (`:241`), `salesMenu` (`:293`), `accessoriesMenu` (`:478`), `returnsMenu` (`:624`), `promotionsMenu` (`:735`), `warrantiesMenu` (`:926`), and the input readers `readDate`, `readDouble`, `readInt` (`:875-924`). |
| Explanation | One class holds the user interface of the seven modules. Each submenu is an unrelated responsibility (it uses different services and different model classes), so the class is not focused. |
| Consequence | Every requirement of the course has edited this same file; changes to one module touch a class that contains all the others, and the class depends on seven services and on most of the model. |
| Proposed solution | Keep in `ConsoleMenu` only the main loop and the exception handling, and move each submenu to its own class in the `ui` package (`ProductMenu`, `PersonMenu`, `SaleMenu`, `AccessoryMenu`, `ReturnMenu`, `PromotionMenu`, `WarrantyMenu`), sharing a `ConsoleInput` with the scanner and the numeric readers. `ConsoleMenu` creates them with the services it already receives, so `Main` does not change. Patterns applied: High Cohesion and Pure Fabrication. |
| Other role's code involved | No — the user interface is the Technical Lead's code. |

## Polymorphism

**Verdict:** _pending — audit in progress._

- **Sale — Violation (V-01).** The receipt decides the label of each item with `instanceof`.

- **SalePersistence — Correct.** It writes only ids; no type tags are needed.

- **SaleService — Violation (V-04, and V-05 for the stock dispatch).** The promotions are asked polymorphically (`calculateDiscount`), but warranty eligibility and the catalog of each item are decided with `instanceof`.

- **ConsoleMenu — Violation (V-04).** The sale flow decides with `instanceof Console` whether to offer the extended warranty and how to label each product.

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

### V-04

| Field | Content |
|---|---|
| ID | V-04 |
| Pattern | Polymorphism (also recorded under Protected Variations) |
| Module | Warranties (Requerimiento 4) and integration adjustment A3 (Requerimiento 5) |
| Evidence | `SaleService.generateWarranties`, `SaleService.java:236`: `if (!(item instanceof Console)) { continue; }`; `ConsoleMenu.askForExtendedWarranty`, `ConsoleMenu.java:434`: `if (!(product instanceof Console)) { return; }`; `ConsoleMenu.printSellableItems`, `ConsoleMenu.java:398`: `product instanceof Console ? "Consola - admite garantía extendida" : "Videojuego"` |
| Explanation | Whether a product admits a warranty is decided with a type check in three places of two layers. It is a property of each product type. |
| Consequence | If another product type starts admitting warranties, the three places must be found and edited; the service and the menu are coupled to `Console`. |
| Proposed solution | Let each product answer `isWarrantyEligible()` (false in `Product`, true in `Console`) and ask it in the three places; the listing builds its label with `getTypeLabel()` (V-01). Patterns applied: Polymorphism and Protected Variations. |
| Other role's code involved | Yes — `Product` and `Console` are Developer 1's code; Developer 1 must add `isWarrantyEligible()`. |

## Pure Fabrication

**Verdict:** _pending — audit in progress._

- **SalePersistence — Correct.** It keeps file access out of `Sale` (its dependency on services is recorded under Low Coupling, V-02).

- **SaleService — Correct.** The checks are delegated to `SaleValidator`, `PersonValidator` and `ProductValidator` (`SaleService.java:115`, `:124`, `:176`, `:293`); identifiers are generated with the JDK fabrication `UUID` (`:192`).

- **ConsoleMenu — Correct.** The input readers are private helpers of the user interface; they move to `ConsoleInput` as part of the correction of V-07.

## Indirection

**Verdict:** _pending — audit in progress._

- **SaleService — Violation (V-05).** There is no intermediary between the sales module and the two catalogs of sellable items.

- **ConsoleMenu — Correct.** The menu reaches persistence only through the services.

### V-05

| Field | Content |
|---|---|
| ID | V-05 |
| Pattern | Indirection (also recorded under Polymorphism) |
| Module | Accessories integration (Requerimiento 1) and integration adjustment A3 (Requerimiento 5) |
| Evidence | `SaleService.findSellableItem(String)`, `SaleService.java:274-280` (`productService.findById`, then `accessoryService.findById`), `SaleService.updateInventory(List)`, `:256-264` (`if (item instanceof Accessory) { accessoryService.decreaseStock(...) } else { productService.reduceStock(...) }`) and `SalePersistence.resolveItem`, `SalePersistence.java:192-198` (same lookup order). |
| Explanation | Two catalogs of sellable items (products and accessories) must collaborate with every class that handles an item, but there is no intermediary, so the sales module talks to both, repeats the lookup order and decides with a type check which catalog owns the stock. |
| Consequence | The same lookup and dispatch exist in the returns module (developer2-audit V-07); a third catalog would require editing every copy. |
| Proposed solution | Use the intermediary proposed for the returns module (`ItemCatalog`, developer2-audit V-07) with `findItem(id)` and a new `decreaseStock(item, amount)`, from `SaleService` and from the sale loading. Patterns applied: Indirection and Pure Fabrication. |
| Other role's code involved | Yes — `ItemCatalog` is created by Developer 2 for developer2-audit V-07; Developer 2 must add `decreaseStock`. `Main` (Technical Lead) injects it. |

## Protected Variations

**Verdict:** _pending — audit in progress._

- **Sale — Violation (V-01).** The list of product types is hard-coded in the receipt.

- **SalePersistence — Correct.** A change of the file format is confined to this class, and file errors are translated into `PersistenceException` (`SalePersistence.java:67`, `:102`, `:124`).

- **SaleService — Violation (V-04).** Warranty eligibility is not protected from a new product type.

- **ConsoleMenu — Violation (V-04, V-06).** Warranty eligibility and the warranty rate are hard-coded in the menu. The handling of exceptions is correct: one `catch` per category in a single place (`ConsoleMenu.java:141-153`), so the services are free to change which exception they throw inside each category.
