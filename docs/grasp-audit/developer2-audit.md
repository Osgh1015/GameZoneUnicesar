# GRASP Audit — Developer 2

| Item | Value |
|---|---|
| Role | Developer 2 (Pablo Andres Amaya Pabon) |
| Requirement | Requerimiento 7 — GRASP audit and correction, Phase 1 (audit) |
| Audited commit | `4027852` (`develop`) |
| Date | 2026-10-23 |
| Scope rule | Only the code under the responsibility of Developer 2, per the responsibility table of Requerimiento 7 (role-based distribution agreed by the team) |

Verdicts: **Violation** (the code breaks the pattern at one or more points), **Correct** (the pattern is applied correctly), **Not applicable** (the audited code has no point where the pattern applies).

## Scope

| Module (requirement) | Classes and methods audited |
|---|---|
| People (Taller 1) | `Person`, `Client` (the statement calls it `Customer`), `Seller`, `PersonRepository`, `PersonService` |
| Accessories (Requerimiento 1) | `AccessoryRepository`, `AccessoryService` |
| Promotions (Requerimiento 2) | `PromotionRepository`, `PromotionService` |
| Returns (Requerimiento 3) | `ReturnRepository`, `ReturnService` (including the monthly balance: `calculateMonthlySales`, `calculateMonthlyReturns`, `generateMonthlyBalance`) |

## Information Expert

**Verdict:** _pending — audit in progress._

- **People — Correct.** `Client.getDescription()` and `Seller.getDescription()` build the description from their own fields; `PersonService.findClientById` and `findVendorById` (`PersonService.java:88`, `:103`) search the lists the service owns.

- **Accessories — Mixed.** `AccessoryService.findAccessoriesCompatibleWith` (`AccessoryService.java:99`) asks each accessory `isCompatibleWith(consoleId)` instead of reading its list, which is correct; the stock update is a violation (V-02).

- **Promotions — Correct.** `PromotionService.listActivePromotions` (`PromotionService.java:141`) asks each promotion `isActive(date)`, and `findBestPromotionFor` (`PromotionService.java:160`) asks each promotion `calculateDiscount(sale)` and only compares the results.

- **Returns — Violation (V-04, V-05).** The monthly balance asks each sale `calculateFinalTotal()` (`ReturnService.java:224`) and each return `getRefundAmount()` (`:242`), and the 30-day rule is asked to the sale with `canBeReturned()` (`:71`), which is correct; but the queries navigate the associations of `Return` (V-04) and the registration searches the products of the sale by itself (V-05).

### V-02

| Field | Content |
|---|---|
| ID | V-02 |
| Pattern | Information Expert |
| Module | Accessories (Requerimiento 1) and integration adjustment A4 (Requerimiento 5) |
| Evidence | `AccessoryService.decreaseStock(String, int)`, `AccessoryService.java:153`: `accessory.setQuantity(accessory.getQuantity() - amount);` and `AccessoryService.restoreStock(String, int)`, `AccessoryService.java:186`: `accessory.setQuantity(accessory.getQuantity() + amount);` |
| Explanation | The accessory owns its stock, but the service reads the quantity, computes the new value and writes it back through the setter. The rule "stock goes down by the amount sold and up by the amount returned" belongs to the object that holds the stock. |
| Consequence | The same read-compute-write sequence is repeated in the product module; a new rule on stock (for example, never below zero) would have to be added in every copy. |
| Proposed solution | Ask the accessory to update its own stock with `decreaseStock(int)` and `increaseStock(int)`. These methods belong to `Product`, which `Accessory` extends; the developer1 audit proposes them for the same problem in `ProductService` (developer1-audit V-01), so once they exist `AccessoryService` only calls them. Pattern applied: Information Expert. |
| Other role's code involved | No for the change in `AccessoryService`. It depends on the methods that Developer 1 adds to `Product` for developer1-audit V-01, so it is corrected after that correction. |

### V-04

| Field | Content |
|---|---|
| ID | V-04 |
| Pattern | Information Expert |
| Module | Returns (Requerimiento 3) |
| Evidence | `ReturnService.viewReturnsByCustomer(String)`, `ReturnService.java:190`: `r.getOriginalSale().getClient().getId().equals(customerId)` and `ReturnService.viewReturnsBySale(String)`, `ReturnService.java:204`: `r.getOriginalSale().getId().equals(saleId)` |
| Explanation | The service navigates getter chains through `Return` and `Sale` to answer a question about the return. The return holds its original sale and is the expert to answer whether it belongs to a client or to a sale. |
| Consequence | The service knows the internal structure of `Return` and of `Sale`; a change in those associations breaks the service queries. |
| Proposed solution | Ask the return: `Return.belongsToClient(clientId)` and `Return.belongsToSale(saleId)`. Pattern applied: Information Expert. |
| Other role's code involved | Yes — `Return` is Developer 1's code; Developer 1 must add the two methods. |

### V-05

| Field | Content |
|---|---|
| ID | V-05 |
| Pattern | Information Expert |
| Module | Returns (Requerimiento 3) |
| Evidence | `ReturnService.registerReturn(String, List, String)`, `ReturnService.java:77-84`: `Product match = null; for (Product p : sale.getProducts()) { if (p.getId().equals(productId)) { match = p; break; } }` and `ReturnService.countSoldUnits(Sale, String)`, `ReturnService.java:149-157`: `for (Product product : sale.getProducts()) { if (product.getId().equals(productId)) { count++; } }` |
| Explanation | The service searches the products of the sale itself. `Sale` holds that list and is the expert to answer which of its products has a given id and how many units of it were sold. |
| Consequence | The search logic lives outside the class that owns the data and is written twice in the same service; any other class that needs it would copy it again. |
| Proposed solution | Ask the sale: `sale.findProduct(productId)` and `sale.countUnitsOf(productId)`, and remove `countSoldUnits`. Pattern applied: Information Expert. |
| Other role's code involved | Yes — `Sale` belongs to the Technical Lead; the Technical Lead must add the two methods. |

## Creator

**Verdict:** _pending — audit in progress._

- **People — Correct.** `PersonRepository.loadClients` and `loadVendors` (`PersonRepository.java:81`, `:116`) create `Client` and `Seller` objects from the records they read. The `Client` registered through the menu is created by `ConsoleMenu` (Technical Lead), reported in the developer1 audit.

- **Accessories — Correct.** `AccessoryRepository.fromLine` (`AccessoryRepository.java:131-135`) creates each accessory type from the record it reads.

- **Promotions — Correct.** `PromotionService.registerPercentageDiscount`, `registerCategoryDiscount` and `registerBulkPurchaseDiscount` (`PromotionService.java:48`, `:71`, `:117`) create the promotions they register and aggregate.

- **Returns — Correct.** `ReturnService.registerReturn` (`ReturnService.java:95`) creates the `Return` it records, with the sale and items it has just validated.

## Controller

**Verdict:** _pending — audit in progress._

## Low Coupling

**Verdict:** _pending — audit in progress._

- **People — Violation (V-01).** `Person`, `Client` and `Seller` import nothing outside the model, and `PersonRepository` depends only on the model and the exceptions, which is correct; `PersonService` is tied to the concrete `PersonRepository` and to its files because it creates it.

- **Accessories — Correct except for V-03.** `AccessoryService` receives its repository by constructor; `matchesType` additionally ties it to `Controller`, `Cable` and `Memory` (part of V-03).

- **Promotions — Correct.** `PromotionService` receives its repository by constructor and depends only on the model; `PromotionRepository` depends only on the model.

- **Returns — Violation (V-06).** `ReturnService` receives all its collaborators by constructor, but `ReturnRepository` depends on three services.

### V-01

| Field | Content |
|---|---|
| ID | V-01 |
| Pattern | Low Coupling (also recorded under Protected Variations) |
| Module | People (Taller 1) |
| Evidence | `PersonService()`, `PersonService.java:39`: `this.repository = new PersonRepository();` and `PersonRepository.java:32-33`: `private static final String CLIENTS_FILE = "data/clients.txt"; private static final String VENDORS_FILE = "data/vendors.txt";` |
| Explanation | `PersonService` decides by itself which concrete repository it uses, and the repository decides by itself which files it uses. `AccessoryService`, `PromotionService`, `ReturnService` and `WarrantyService` receive their repository from `Main` with the file path; the people module is the exception. |
| Consequence | The people module cannot be wired with other files (for example, a copy of `data/` for a test run) without editing two classes, and `Main` cannot decide the wiring of this module as it does for the others. A change of the storage location is not protected. |
| Proposed solution | `PersonRepository(String clientsFile, String vendorsFile)` and `PersonService(PersonRepository repository)`, injected from `Main` with the same two files. Patterns applied: Low Coupling and Protected Variations. |
| Other role's code involved | Yes — `Main` belongs to the Technical Lead and must create the repository and pass it to the service. |

### V-06

| Field | Content |
|---|---|
| ID | V-06 |
| Pattern | Low Coupling |
| Module | Returns (Requerimiento 3) and integration adjustment A4 (Requerimiento 5) |
| Evidence | `ReturnRepository` imports and stores `SaleService`, `ProductService` and `AccessoryService` (`ReturnRepository.java:4-6`, fields `:27-29`, constructor `:31-37`) and calls them while loading: `saleService.findById(parts[2])` (`:97`) and `resolveProduct(String)` (`:120-126`), which asks `productService.findById` and then `accessoryService.findById`. |
| Explanation | A persistence class depends on the service layer, the reverse of the allowed direction `service → persistence`. The repository both reads the file and resolves business references. `WarrantyRepository` already avoids this by returning `WarrantyRecord` objects that `WarrantyService` resolves; `ReturnRepository` does not. |
| Consequence | The repository cannot be built without three services, a change in a service constructor or lookup breaks the persistence layer, and `Main` must build the returns repository after the sales module only because of this dependency. |
| Proposed solution | The repository loads only references (a `ReturnRecord` with the ids, the reason and the warranty refund) and `ReturnService` resolves them into `Sale` and `Product` objects, as `WarrantyService` already does. Patterns applied: Low Coupling and Pure Fabrication. |
| Other role's code involved | Yes — `Main` belongs to the Technical Lead and must stop passing the services to the repository. Both classes are Developer 2's code. |

## High Cohesion

**Verdict:** _pending — audit in progress._

- **People — Correct.** `PersonRepository` only reads and writes the two people files; `PersonService` only registers and looks up people. It keeps two pairs of equivalent methods (`findVendorById`/`findSellerById`, `PersonService.java:103`, `:112`; `listVendors`/`listSellers`, `:78`, `:121`) because other modules call both names; they do the same job, so the class is still focused.

- **Accessories — Correct.** `AccessoryRepository` only handles `data/accessories.csv`; `AccessoryService` only handles the accessory rules.

- **Promotions — Correct.** `PromotionService` holds registration, validation of the promotion data and selection of the best promotion — all promotion rules.

- **Returns — Correct.** `ReturnService` groups return registration, return queries and the monthly balance (`ReturnService.java:220-258`), which the requirement defines as part of the returns module; the balance only adds the totals that each sale and each return already calculate.

## Polymorphism

**Verdict:** _pending — audit in progress._

- **People — Correct.** `Person.getDescription()` is abstract and `Client`/`Seller` override it; no code in scope asks a person for its concrete type.

- **Accessories — Mixed.** `AccessoryRepository.toLine`/`fromLine` (`AccessoryRepository.java:100-110`, `:131-135`) branch on the concrete type and on a type tag; this is **not** treated as a violation: the repository is a Pure Fabrication whose job is to keep the file format out of the model. `AccessoryService.matchesType` is a violation (V-03).

- **Promotions — Correct.** `findBestPromotionFor` relies on the polymorphic `calculateDiscount` and never asks for the type of a promotion; the type tags of `PromotionRepository` (`PromotionRepository.java:95-104`, `:120-125`) are judged correct for the same reason as the accessory repository.

- **Returns — Violation (V-07).** `registerReturn` decides with `instanceof` which items have warranties and which catalog restores their stock.

### V-03

| Field | Content |
|---|---|
| ID | V-03 |
| Pattern | Polymorphism (also recorded under Low Coupling and Protected Variations) |
| Module | Accessories (Requerimiento 1) |
| Evidence | `AccessoryService.matchesType(Accessory, String)`, `AccessoryService.java:164-172`: `return switch (normalized) { case "CONTROLLER" -> accessory instanceof Controller; case "CABLE" -> accessory instanceof Cable; case "MEMORY" -> accessory instanceof Memory; default -> false; };`, called from `listAccessoriesByType(String)`, `AccessoryService.java:83` |
| Explanation | The service asks each accessory for its concrete class and maps a type name to that class itself. The type of an accessory is something each accessory knows about itself. |
| Consequence | A new accessory subclass is never listed by type until someone edits this method, and the service is coupled to the three concrete subclasses. |
| Proposed solution | Let each accessory report its own type with an abstract `getAccessoryType()` in `Accessory`, implemented by `Controller` (`"CONTROLLER"`), `Cable` (`"CABLE"`) and `Memory` (`"MEMORY"`), and filter with `accessory.getAccessoryType().equals(normalized)`, normalizing the requested type as today. Patterns applied: Polymorphism and Protected Variations. |
| Other role's code involved | Yes — the accessory hierarchy is Developer 1's code; Developer 1 must add `getAccessoryType()`. |

### V-07

| Field | Content |
|---|---|
| ID | V-07 |
| Pattern | Polymorphism (also recorded under Indirection) |
| Module | Returns (Requerimiento 3) and integration adjustments A4 and A7 (Requerimiento 5) |
| Evidence | `ReturnService.registerReturn(String, List, String)`, `ReturnService.java:101`: `if (p instanceof Console) { warrantyRefund += warrantyService.cancelWarranties(p.getId(), sale.getId()); }` and `ReturnService.java:109-113`: `if (p instanceof Accessory) { accessoryService.restoreStock(p.getId(), 1); } else { productService.restoreStock(p.getId(), 1); }`; the same lookup order (products, then accessories) is written in `ReturnRepository.resolveProduct` (`:120-126`). |
| Explanation | The use case decides with type checks which items may have warranties and which catalog owns the stock of each item. Which items have warranties is known by `WarrantyService`; which catalog owns an item is a decision that every class handling items has to repeat, because no intermediary exists between them and the two catalogs. |
| Consequence | Every class that adjusts stock or resolves an item id must repeat the same check and the same lookup order; a new kind of sellable item, or warranties for another product type, means editing every such place. |
| Proposed solution | Ask `WarrantyService.cancelWarranties` for every returned item (it answers zero when the item has no warranty), and introduce an intermediary `ItemCatalog` in the service layer with `findItem(id)` and `restoreStock(item, amount)`, used by `ReturnService` for the lookup and the stock. The decision of which catalog owns an item is then written in one place only. Patterns applied: Polymorphism, Indirection and Pure Fabrication. |
| Other role's code involved | Yes — `Main` (Technical Lead) must create `ItemCatalog` and pass it to `ReturnService`. The same dispatch exists in `SaleService.registerSale` and `SalePersistence` (Technical Lead); it was reported to the Technical Lead, who can reuse `ItemCatalog`. |

## Pure Fabrication

**Verdict:** _pending — audit in progress._

- **People — Correct.** `PersonRepository` is the artificial class that keeps file access out of `Client` and `Seller`.

- **Accessories — Correct.** `AccessoryRepository` keeps the CSV format out of the accessory hierarchy.

- **Promotions — Correct.** `PromotionRepository` keeps the CSV format out of the promotion hierarchy.

- **Returns — Correct.** `ReturnRepository` keeps the file format out of `Return` (its dependency on services is recorded under Low Coupling, V-06).

## Indirection

**Verdict:** _pending — audit in progress._

- **People — Correct.** `PersonService` sits between `ConsoleMenu` and `PersonRepository`; the menu never touches the repository.

- **Accessories — Correct.** `AccessoryService` mediates between the menu and `AccessoryRepository`.

- **Promotions — Correct.** `PromotionService` mediates between the menu, the sale registration and `PromotionRepository`.

- **Returns — Violation (V-07).** No intermediary exists between the return module and the two catalogs of sellable items, so `ReturnService` and `ReturnRepository` talk to both and repeat the lookup order. The full analysis is recorded once, under Polymorphism.

## Protected Variations

**Verdict:** _pending — audit in progress._

- **People — Violation (V-01).** A change of the file format is confined to `PersonRepository`, which is correct, but the file locations are fixed inside the repository and the service creates its own repository, so a change of location is not protected.

- **Accessories — Violation (V-03).** The vocabulary of accessory types and its mapping to classes is hard-coded in the service. The full analysis is recorded once, under Polymorphism.

- **Promotions — Correct.** `validateCategory` (`PromotionService.java:82`) checks the category against the constants of `CategoryDiscount`, so the list of categories lives in one place.
