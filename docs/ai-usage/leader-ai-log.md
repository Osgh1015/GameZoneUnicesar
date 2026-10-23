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

