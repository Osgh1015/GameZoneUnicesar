# AI Usage Log — Technical Lead


This log records AI-assisted decisions taken by the Technical Lead role during the development of GameZone Unicesar.

The Technical Lead (Omar Sebastian Gamez Heredia) was responsible for the sales module, `ConsoleMenu`, `Main`, the integration of the modules, the README and the review of the Pull Requests.

## Entry 1

**Date:** 2026-09-10 **Tool used:** Claude

**Reason for use:** Implement the Sale class, its persistence and the sale validation rules of the Workshop.

**Problem faced:** A sale stores ids of its client, seller and products, so loading it from the file requires resolving those ids back into objects, and registerSale must reject a sale without products or without enough stock before touching the inventory.

**Prompt used:** ayúdame a implementar Sale, el guardado de ventas y las validaciones de registerSale

**Solution obtained and decision taken:** Sale holds the client, the seller and the list of products and calculates its own total. The sales file stores one line per sale with the product ids joined by |. SaleService validates that the sale has products, that the client and seller exist and that the stock is enough before decreasing it.



## Entry 2

**Date:** 2026-09-10 **Tool used:** Claude

**Reason for use:** Build the console menu with the ten operations and wire the layers in Main.

**Problem faced:** ConsoleMenu had to cover products, people and sales without touching persistence directly, and Main had to create the services in the right order and load the data on startup.

**Prompt used:** ayúdame con los submenús de ConsoleMenu y con Main para conectar las capas

**Solution obtained and decision taken:** One submenu per module calling only the services, Spanish messages for the user, and Main creating ProductService, PersonService, the sale persistence and SaleService before starting the menu.


## Entry 3

**Date:** 2026-09-16 **Tool used:** Claude

**Reason for use:** Build the first version of the warranty module of Requirement 4.

**Problem faced:** Basic and extended warranties have different durations and costs, they must be generated when a sale with consoles is registered, and the extended cost must be stored with the sale.

**Prompt used:** ejecuta el requerimiento 4 de garantías

**Solution obtained and decision taken:** Abstract Warranty with BasicWarranty and ExtendedWarranty, a repository with type discriminator, a service with assignment and queries, the generation in registerSale, the cost stored in the sale record and the warranty submenu. This version was later reverted (PR #15) and rebuilt in PR #17, as documented in the integration analysis.


## Entry 4

**Date:** 2026-09-30 **Tool used:** Claude

**Reason for use:** Implement the promotion hierarchy, its persistence and the selection of the best promotion of Requirement 2.

**Problem faced:** Each promotion type calculates its discount differently, the rest of the system must not depend on the concrete types, and only the promotion with the highest discount is applied.

**Prompt used:** ejecuta el requerimiento 2 de promociones: jerarquía, repositorio y servicio con la mejor promoción

**Solution obtained and decision taken:** Abstract Promotion with calculateDiscount(Sale) and isActive(LocalDate), the subclasses PercentageDiscount, CategoryDiscount and BulkPurchaseDiscount, PromotionRepository with type discriminator, and PromotionService with findBestPromotionFor returning null when no promotion gives a discount.


## Entry 5

**Date:** 2026-09-30 **Tool used:** Claude

**Reason for use:** Apply the best promotion when a sale is registered and show it in the receipt.

**Problem faced:** The discount had to be stored in the sale, survive a restart and appear in the receipt as subtotal, discount and final total, and the monthly balance had to use the total with discount.

**Prompt used:** integra las promociones en registerSale, en el recibo y en el archivo de ventas

**Solution obtained and decision taken:** Added appliedPromotionName and discountAmount to Sale with a receipt that shows the breakdown, applied the best promotion in registerSale, stored both values as extra columns of the sales file, added the promotions submenu and used the final total in the monthly balance.


## Entry 6

**Date:** 2026-10-02 **Tool used:** Claude

**Reason for use:** Apply integration adjustment A3 of Requirement 5: unify the sale registration flow.

**Problem faced:** Requirements 1, 2 and 4 changed registerSale separately, and the order of the steps decided the result, for example whether the discount was calculated before or after the warranty cost.

**Prompt used:** aplica el ajuste A3 con el orden de los ocho pasos de registerSale

**Solution obtained and decision taken:** Reorganized registerSale in the order of the statement: validate items, resolve and check stock, create the sale, apply the discount only over the items, generate warranties, compute subtotal - discount + extended warranties, update the inventory and persist. Updated the structure of Sale and the sale menu.


## Entry 7

**Date:** 2026-10-02 **Tool used:** Claude

**Reason for use:** Apply integration adjustment A8 of Requirement 5: document the integrated system.

**Problem faced:** The integration analysis had to describe each adjustment A1-A7 and the incident of PR #11, and the README and diagrams had to reflect the four modules working together.

**Prompt used:** aplica el ajuste A8: análisis de integración, README, diagrama de capas y diagrama de clases integrado

**Solution obtained and decision taken:** Wrote the integration analysis with the phases, the incident and one section per adjustment, updated the README and the layers diagram, and added the integrated class diagram.


## Entry 8

**Date:** 2026-10-20 **Tool used:** Claude

**Reason for use:** Correct the defects found in the review of the integrated system.

**Problem faced:** A product could be returned more times than it was sold, a non-numeric value closed the application, the seller list showed six sellers, and Person had no abstract method.

**Prompt used:** cómo corregimos en una rama fix las devoluciones repetidas, el cierre con datos inválidos, los vendedores duplicados y el método abstracto de Person

**Solution obtained and decision taken:** Applied four atomic corrections in fix/integration-review-defects: abstract getDescription in Person, sellers loaded only from vendors.txt, safe numeric input and a safety catch in ConsoleMenu, and validation of returnable units in ReturnService. Merged with PR #28.


## Entry 9

**Date:** 2026-10-20 **Tool used:** Claude

**Reason for use:** Write the sections assigned to the Technical Leader in the exception analysis of Requirement 6.

**Problem faced:** The analysis had to justify an unchecked root exception, the use of a custom hierarchy and the way ConsoleMenu catches the exceptions.

**Prompt used:** por qué conviene una excepción raíz unchecked y cómo debería capturar el menú cada tipo de excepción

**Solution obtained and decision taken:** Justified RuntimeException as root because it keeps the method signatures, the design principles of the hierarchy, and a single try with one catch per exception type in start().

## Entry 10

**Date:** 2026-10-21 **Tool used:** Claude

**Reason for use:** Refactor ConsoleMenu to catch each custom exception with a different message.

**Problem faced:** The menu had a generic catch, the client and sale flows still caught IllegalArgumentException or checked for null, and the sale detail used findById with a null check.

**Prompt used:** qué cambios necesita el líder técnico en ConsoleMenu para mostrar un mensaje distinto según el tipo de excepción

**Solution obtained and decision taken:** One try in start() with four catch blocks and the messages of the statement plus a safety catch; removed the local catch of the client flow and the null check of the sale flow; the sale detail uses getSaleDetail. Kept the catch of returns and promotions because those services are outside the scope of Requirement 6.



## Entry 11

**Date:** 2026-10-21 **Tool used:** Claude

**Reason for use:** Handle data errors at startup and document the new layers.

**Problem faced:** The services load the data files in their constructors, before the menu exists, so a damaged file closed the application with a stack trace.

**Prompt used:** cómo evitamos que el programa muestre la traza de error si un archivo de datos está dañado al arrancar, y qué agregamos al README sobre las nuevas capas

**Solution obtained and decision taken:** Moved the creation of the objects to buildApplication() and wrapped the startup in a try/catch for PersistenceException; added the README section of the exceptions and validation layers and the before/after table of the analysis.



## Entry 12

**Date:** 2026-10-21 **Tool used:** Claude

**Reason for use:** Fix the format of the exception analysis and the repository structure of the README.

**Problem faced:** The analysis had lost its markdown format, IntelliJ reported errors in the code block of section 5, and the README did not list the new packages and documents.

**Prompt used:** por qué IntelliJ marca error en el bloque del try del análisis de excepciones y qué falta en la estructura del README

**Solution obtained and decision taken:** Restored the headings and lists of the analysis, marked the code block as text so IntelliJ does not check it as Java, and added the new packages and documents to the repository structure of the README. Merged with PR #32.

## Requirement 7 — Phase 1: Audit

## Entry 13

**Date:** 2026-10-23 **Tool used:** Claude

**Reason for use:** Audit the code under the responsibility of the Technical Lead against the nine GRASP patterns and consolidate the three reports.

**Problem faced:** The Technical Lead's code integrates every module, so most of its violations need methods in classes of other roles, and several findings of the three reports were the same problem seen from different sides (stock arithmetic in three services, the item lookup in the sales and returns modules, repositories that depend on services). The consolidation also had to keep one identifier per violation and an order of correction in which every commit compiles.

**Prompt used:** cómo consolidar tres auditorías GRASP: qué hallazgos son de frontera, cuáles se unifican, cómo asignar identificadores y responsables, y en qué orden corregir

**Solution obtained and decision taken:** Audited `Sale`, `SalePersistence`, `SaleService`, `ConsoleMenu`, `Main` and `ProductService.restoreStock` with the tool and recorded eight violations with their evidence on the audited commit. The creation of domain objects in `ConsoleMenu`, reported by Developer 1, was examined and kept as correct (Creator). In the consolidation I checked the coverage of the 43 classes, verified every cited line, assigned the definitive identifiers V-01 to V-20 in the order Developer 1 → Developer 2 → Technical Lead, split each boundary finding by owner and recorded four out-of-scope defects. Related findings that each owner can correct completely were kept as separate violations with a shared solution. Also wrote the answers to questions 2 and 5.

## Requirement 7 — Phase 2: Correction

## Entry 14

**Date:** 2026-10-23 **Tool used:** Claude

**Reason for use:** Correct the Information Expert and Polymorphism violations of the Technical Lead (V-13 to V-17) in `Sale`, `SaleService`, `ProductService.restoreStock` and `ConsoleMenu`, using the methods that Developer 1 added to the products and to `ExtendedWarranty` inside my task group.

**Problem faced:** The history queries, the warranty listing and the sale flow navigated `Sale → Client`, `Sale → Seller` and `Warranty → Sale`, calculated the stock with `getQuantity`/`setQuantity` and decided with `instanceof Console` which products admit a warranty. The receipt also had a private `describeItemType` with one branch per product type. I had to change all of this without changing a single message of the menu, and some of the methods I needed (`isWarrantyEligible`, `getTypeLabel`, `quoteFor`) belonged to Developer 1's classes, so they had to exist before my commits.

**Prompt used:** cómo aplicar Information Expert y Polymorphism en Sale, SaleService y el menú sin cambiar ningún mensaje, y en qué orden coordinar los métodos que tiene que agregar el Desarrollador 1

**Solution obtained and decision taken:** Added `Sale.belongsToClient` and `Sale.wasAttendedBy` and used them in the history queries and in the listing of the warranties of a sale. `restoreStock` now asks `product.increaseStock`. The menu asks `ExtendedWarranty.quoteFor` for the price it offers. `SaleService`, the sale flow and the listing of sellable items ask `isWarrantyEligible()` and `getTypeLabel()` instead of checking classes, and `describeItemType` was removed. Developer 1 made S4, S5 and S6 first, so every commit compiled. I compared the output of the scripted run after each commit: no differences. Every suggested change was reviewed against the original code before committing it.



## Entry 15

**Date:** 2026-10-23 **Tool used:** Claude

**Reason for use:** Correct V-18 (Indirection) and V-19 (Low Coupling): `SalePersistence` depended on `PersonService`, `ProductService` and `AccessoryService` to rebuild the sales, and `SaleService` looked up items and stock in two services with `instanceof`.

**Problem faced:** `SalePersistence` was used by `SaleService` and by `WarrantyService` (Developer 2), and `WarrantyService` has to be built before `SaleService`, so making `SaleService` rebuild the sales would create a cycle. A damaged line of `sales.txt` also had to produce exactly the same `PersistenceException` at the same point as before, and a line whose client no longer exists had to be skipped as before. The change touched Developer 2's `WarrantyService` and the wiring in `Main`, and every commit had to compile.

**Prompt used:** cómo quitar la dependencia de SalePersistence hacia los servicios sin crear un ciclo entre SaleService y WarrantyService, manteniendo el mismo comportamiento con archivos dañados

**Solution obtained and decision taken:** `SalePersistence` now returns `SaleRecord` objects with the identifiers and the amounts as they were written. I created `SaleAssembler` in the service layer to rebuild the sales from those records, shared by `SaleService` and `WarrantyService`, so there is no cycle. `SaleService` uses `ItemCatalog` (Developer 2) for the lookup and the stock of the sold items. The order was additive: new code first with the old constructor kept `@Deprecated`, then Developer 2 moved `WarrantyService` (P5), then `Main` (M), then Developer 2 and I removed the deprecated code (P6, N). The damaged-file case was checked with a broken line of `sales.txt` (same message before and after), and the scripted run gave the same output after every commit.


## Entry 16

**Date:** 2026-10-23 **Tool used:** Claude

**Reason for use:** Correct V-20 (High Cohesion): `ConsoleMenu` had 1026 lines with the seven submenus, the input readers and the main loop in one class.

**Problem faced:** All the options share one `Scanner` over `System.in`, so the submenus could not create their own reader without losing input. The extraction had to be done in small commits that compile, one submenu at a time, with the same prompts, the same validation loops and the same handling of exceptions, and without changing `Main`.

**Prompt used:** cómo dividir ConsoleMenu en un submenú por módulo compartiendo el mismo Scanner, en commits pequeños y sin cambiar la salida

**Solution obtained and decision taken:** First moved the input readers to `ConsoleInput`, which wraps the single scanner (O). Then extracted one submenu per commit — `ProductMenu`, `PersonMenu`, `SaleMenu`, `AccessoryMenu`, `ReturnMenu`, `PromotionMenu`, `WarrantyMenu` — each receiving the shared `ConsoleInput` and only the services of its module (P to V). Finally `ConsoleMenu` kept only the main loop and the differentiated `catch` blocks, delegating to the submenus that it creates in its constructor with the services it already received, so `Main` did not change (W). After every extraction I compiled and ran the full script; the output was identical.


## Entry 17

**Date:** 2026-10-23 **Tool used:** Claude

**Reason for use:** Close Phase 2: final non-regression verification, change log of the Technical Lead, after class diagram and README.

**Problem faced:** The evidence had to show that the behavior of the whole system did not change after the 52 code commits of the three roles, including the returns that depend on the date of the run, and the documentation had to describe the new classes without modifying the documents of the previous requirements.

**Prompt used:** cómo registrar la verificación final de no regresión y qué debe mostrar el diagrama de clases después de la corrección GRASP

**Solution obtained and decision taken:** Ran the same script of 308 entries on the final branch on the same day as the baseline and compared the normalized output and data files with `diff`: 1676 identical lines. Recorded the result, my rows of the change log and the design decisions (the single `instanceof` left in `ItemCatalog`, `ConsoleMenu` creating its submenus, records with raw amounts) in `docs/grasp-refactor-log.md`. Added the after class diagram to `docs/grasp-class-diagram.md` and a GRASP section to the README. The Mermaid diagram was checked to render before committing.

## Requirement 8 — Phase 1: Audit

## Entry 15

**Date:** 2026-10-23 **Tool used:** Claude

**Reason for use:** Audit the code under the responsibility of the Technical Lead against the five SOLID principles and consolidate the three reports.

**Problem faced:** After the GRASP correction most of my code already looked well divided (`ConsoleMenu` split into submenus, `SaleAssembler`, constructor injection), so I had to tell apart what GRASP had really solved and what was still a SOLID problem. The sale registration depends on three services of Developer 2, and the returns module depends on my `SaleService`, so several findings crossed roles and had to be registered once, with the commits of each owner.

**Prompt used:** cómo revisar SaleService, SaleAssembler y el menú principal con SOLID después de la corrección GRASP, y cómo consolidar tres auditorías con hallazgos de frontera

**Solution obtained and decision taken:** Recorded four violations with evidence on the audited commit: V-01 (SRP, `Sale.generateReceipt`), V-02 (DIP, `SalePersistence` concrete), V-03 (ISP, the whole services of Developer 2 in the sale registration) and V-04 (OCP, the main menu lists every module in three places). LSP does not apply to my classes. The dependency on `ItemCatalog`, the submenus on their services and `Main` as composition root were examined and accepted. In the consolidation I checked the coverage of the 53 classes, verified every cited line, assigned V-01 to V-12 in the order Developer 1 → Developer 2 → Technical Lead, split the five boundary findings by owner and kept the out-of-scope defects of Requerimiento 7 untouched. Also wrote the answers to questions 2 and 5.

## Requirement 8 — Phase 2: Correction

## Entry 19

**Date:** 2026-10-23 **Tool used:** Claude

**Reason for use:** Correct the SOLID violations assigned to the Technical Lead and verify that the behavior of the system did not change.

**Problem faced:** `SaleService` and `SaleAssembler` had to stop depending on three services of Developer 2 without breaking any commit, so the interfaces, their implementations and the change of the dependencies had to go in that order. The main menu had to keep exactly the same numbers, titles and messages (including the invalid options and the exit) after replacing the `switch`, and `ConsoleMenu` could no longer create the submenus if a new module was to be added without editing it.

**Prompt used:** cómo crear interfaces pequeñas para que SaleService no dependa de los servicios completos de otro rol, y cómo reemplazar el switch del menú principal sin cambiar la salida

**Solution obtained and decision taken:** Moved the receipt layout to `SaleReceipt` and kept `generateReceipt()` as a delegation. Created `SaleStore`, implemented by `SalePersistence`. Created `PersonLookup`, `PromotionSelector` and `WarrantyAssigner`, made `PersonService`, `PromotionService` and `WarrantyService` implement them (agreed with Developer 2) and then made `SaleService` and `SaleAssembler` depend on them, with `Main` unchanged. Created `MenuOption`, implemented by the seven submenus with their old titles; `ConsoleMenu` prints and runs the list that `Main` composes, and keeps option 0 and the exception handling. Every commit compiles; the scripted run of 316 inputs gave identical output and data files after every commit of the three roles and in the final verification.

