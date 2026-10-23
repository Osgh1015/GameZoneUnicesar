# AI Usage Log — Developer 2

This log records AI-assisted decisions taken by the Developer 2 role during the development of GameZone Unicesar.

Developer 2 (Pablo Andres Amaya Pabon) was responsible for the people module and for the repositories and services of the extension modules, including the integration adjustments A2, A4, A6 and A7.

## Entry 1

**Date:** 2026-09-10 **Tool used:** Claude

**Reason for use:** Implement the service and the repository of the people module and make the first run work without data files.

**Problem faced:** PersonService had to keep clients and vendors in memory and save them after every change, registering a client with an empty name had to be rejected, and the first execution failed because data/clients.txt did not exist yet.

**Prompt used:** ayúdame con PersonService y PersonRepository: registro, listados, validación del nombre y qué pasa si no existen los archivos

**Solution obtained and decision taken:** PersonService loads clients and vendors in its constructor, registers clients and lists both groups, and rejects a client without name. PersonRepository returns an empty list when a file does not exist instead of failing, and the writing of the clients file was reorganized in a single method.


## Entry 2

**Date:** 2026-10-01 **Tool used:** Claude

**Reason for use:** Implement the queries and the cost calculation of WarrantyService.

**Problem faced:** The warranty submenu needs the active warranties, the ones that expire in the next N days and the warranty of a product in a sale, and the repository had to rebuild each warranty with its sale and product when loading the file.

**Prompt used:** cómo listo las garantías vigentes y las que vencen en los próximos días, y cómo cargo las garantías resolviendo la venta y el producto

**Solution obtained and decision taken:** Added the extended warranty registration, the sum of additional costs per sale, the active and expiring-soon queries using isActive and a date range, and the loading of WarrantyRepository resolving the sale and the product of each line.


## Entry 3

**Date:** 2026-10-02 **Tool used:** Claude

**Reason for use:** Apply integration adjustment A2 of Requirement 5: remove the circular dependency of the warranty module.

**Problem faced:** With the specification of Requirement 4, SaleService needed WarrantyService, WarrantyService needed WarrantyRepository and WarrantyRepository needed SaleService to resolve sales, so there was no valid order to build the objects in Main.

**Prompt used:** aplica el ajuste A2: hay un ciclo SaleService -> WarrantyService -> WarrantyRepository -> SaleService

**Solution obtained and decision taken:** WarrantyRepository now stores and loads only the sale and product ids, WarrantyService resolves those ids itself, and Main builds WarrantyService before SaleService. Updated the warranty class diagram with the new dependencies.


## Entry 4

**Date:** 2026-10-02 **Tool used:** Claude

**Reason for use:** Apply integration adjustment A4 of Requirement 5: restore the stock of returned accessories.

**Problem faced:** Returning an accessory did not increase its stock, and a sale that included an accessory could not be rebuilt when loading the sales file.

**Prompt used:** aplica el ajuste A4: al devolver un accesorio no se restaura su stock

**Solution obtained and decision taken:** The sales and returns loading now resolves item ids in the accessory catalogue when they are not products, and AccessoryService was wired into the return module so the stock is restored in the right catalogue.

## Entry 5

**Date:** 2026-10-02 **Tool used:** Claude

**Reason for use:** Apply integration adjustment A6 of Requirement 5: complete the monthly balance report.

**Problem faced:** generateMonthlyBalance only returned the net balance, the report must show total sales, total returns and the balance, and with promotions and warranties the sales total had to use the final total of each sale.

**Prompt used:** aplica el ajuste A6 sin cambiar la firma de generateMonthlyBalance

**Solution obtained and decision taken:** Added calculateMonthlySales and calculateMonthlyReturns, kept generateMonthlyBalance as their difference, used the final total of each sale, and showed the three values in the menu.

## Entry 6

**Date:** 2026-10-02 **Tool used:** Claude

**Reason for use:** Apply integration adjustment A7 of Requirement 5: cancel the warranties of a returned console.

**Problem faced:** A returned console kept its warranties active, and the cost of an extended warranty was not given back to the client.

**Prompt used:** aplica el ajuste A7: anular las garantías de una consola devuelta y reembolsar el costo de la extendida

**Solution obtained and decision taken:** Added cancelWarranties(productId, saleId) to WarrantyService returning the refundable cost, a warranty refund value in Return included in the amount and the receipt, its persistence in the returns file, and the call from registerReturn for every returned console.


## Entry 7

**Date:** 2026-10-20 **Tool used:** Claude

**Reason for use:** Write the sections assigned to Developer 2 in the exception analysis of Requirement 6.

**Problem faced:** The analysis had to explain where the validators are called, how the external behavior is preserved, and the decisions that differ from the statement.

**Prompt used:** en qué punto se llaman los validadores y cómo demostramos que el refactor no cambia el comportamiento del menú

**Solution obtained and decision taken:** Wrote that validators are called from the services before any change of state and never access persistence, the regression strategy with the ten menu operations, and the decisions about SalePersistence, Client and findById.


## Entry 8

**Date:** 2026-10-21 **Tool used:** Claude

**Reason for use:** Refactor the three repositories of Requirement 6 to report file errors with PersistenceException.

**Problem faced:** The repositories printed IOException messages and continued, and a damaged line closed the application with a NumberFormatException; a missing file still had to start with an empty list.

**Prompt used:** qué cambios hay que hacer en los repositorios para que los errores de archivo se reporten con PersistenceException sin perder qué archivo falló

**Solution obtained and decision taken:** ProductRepository, PersonRepository and SalePersistence wrap IOException into PersistenceException with the file and the operation, report corrupted lines with their line number, and keep returning an empty list when the file does not exist. One commit per repository.


## Entry 9

**Date:** 2026-10-21 **Tool used:** Claude

**Reason for use:** Refactor ProductService, PersonService and SaleService of Requirement 6 to use the validators.

**Problem faced:** The services had inline validations with IllegalArgumentException, and registerSale printed messages and returned null.

**Prompt used:** cómo cambiamos ProductService, PersonService y SaleService para que usen los validadores y lancen excepciones en vez de devolver null

**Solution obtained and decision taken:** The services call ProductValidator, PersonValidator and SaleValidator and throw typed exceptions; registerSale no longer returns null and getSaleDetail was added. Kept findById returning null because other services depend on it. One commit per service; the twelve manual tests gave the expected results.

## Requirement 7 — Phase 1: Audit

## Entry 10

**Date:** 2026-10-23 **Tool used:** Claude

**Reason for use:** Audit the repositories and services under the responsibility of Developer 2 against the nine GRASP patterns and record each verdict with evidence from the repository.

**Problem faced:** The scope covers the people module and the repository and service of accessories, promotions, returns and warranties, plus the use of exceptions and validators. Several fragments look alike but are not the same problem: the repositories branch on type tags to write their files, while `AccessoryService` and `ReturnService` branch on the concrete type to take business decisions; and `WarrantyRepository` already loads only identifiers while `ReturnRepository` still calls three services.

**Prompt used:** cómo revisar con los patrones GRASP los repositorios y servicios de personas, accesorios, promociones, devoluciones y garantías, y cómo distinguir un instanceof de serialización de uno que es regla de negocio

**Solution obtained and decision taken:** Reviewed each class and method in scope with the tool and recorded eight violations with class, method and line range on the audited commit: V-01 (Low Coupling, `PersonService` creates its own repository with fixed files), V-02 (Information Expert, stock arithmetic in `AccessoryService`), V-03 (Polymorphism, `AccessoryService.matchesType`), V-04 and V-05 (Information Expert, `ReturnService` navigates `Return` and searches the products of the sale), V-06 (Low Coupling, `ReturnRepository` depends on services), V-07 (Polymorphism, type dispatch in `registerReturn`) and V-08 (Information Expert, `WarrantyService` navigates the warranty associations). The type tags of the repositories were recorded as correct. The same dispatch in `SaleService` and the dependency of `WarrantyService` on `SalePersistence` were reported to the Technical Lead. Also wrote the answer to question 4 of the GRASP analysis.

**Related commits:** `<hash commit 1>` … `<hash commit 8>` (PR #<n>)

## Requirement 7 — Phase 2: Correction

## Entry 11

**Date:** 2026-10-23 **Tool used:** Claude

**Reason for use:** Correct the violations assigned to Developer 2 in the consolidated audit without changing what the user sees.

**Problem faced:** Five of the eight corrections needed code of other roles (methods in `Accessory`, `Return` and `Warranty` from Developer 1, two methods in `Sale` and the wiring in `Main` from the Technical Lead), and `Main` still created `PersonService`, `ReturnRepository` and `ReturnService` with their old constructors, so removing them at once would have left the project without compiling. The returns also had to keep exactly the same refunds, stock and warranty cancellations.

**Prompt used:** en qué orden hacer las correcciones del desarrollador 2 que dependen de métodos del modelo y de Main para que cada commit compile y el menú no cambie

**Solution obtained and decision taken:** The model methods of the other roles were added first by their owners (`getAccessoryType`, `belongsToClient`/`belongsToSale`, `covers`/`belongsToSale`, `findProduct`/`countUnitsOf`), then each service was changed to ask them: `AccessoryService` uses the stock methods of `Product` and the accessory type, `ReturnService` and `WarrantyService` ask the return, the sale and the warranty. Created `ItemCatalog` as the single intermediary between the returns and the two catalogs, and let `WarrantyService` decide which items have warranties. `ReturnRepository` now loads `ReturnRecord` objects like `WarrantyRepository`, and `PersonRepository`/`PersonService` receive their files and repository by constructor. The old constructors were kept as `@Deprecated` until the Technical Lead rewired `Main`, and then removed, so every commit compiles. The same scripted run of the menu was executed after every commit and the output and the data files were identical to the baseline.

**Related commits:** `<hash A>` … `<hash J>` (PR #<n>)

