# AI Usage Log — Developer 1

This log records AI-assisted decisions taken by the Developer 1 role during the development of GameZone Unicesar.

Developer 1 (Santiago Andres Herrera Acuña) was responsible for the products module, the accessory, promotion and warranty hierarchies, the `Return` class and `Sale.canBeReturned`, and the exceptions and validation packages.

## Entry 1

**Date:** 2026-09-10 **Tool used:** Claude

**Reason for use:** Implement the file persistence and the business rules of the products module after the Product hierarchy was in place.

**Problem faced:** ProductRepository had to save and load two different product types (VideoGame and Console) from a single text file and rebuild the right subclass when reading it back, and ProductService needed stock validation without letting any file access leak out of the persistence layer.

**Prompt used:** ayúdame a implementar el guardado y la carga de productos en archivo y las validaciones de stock en ProductService

**Solution obtained and decision taken:** Used a semicolon-separated line with VIDEOGAME or CONSOLE as the first field acting as a type discriminator, with toLine/fromLine helpers in the repository and an empty list returned when data/products.txt does not exist yet. ProductService loads the list once in its constructor, registers products, lists them and validates and decreases stock, calling the repository after every change. Kept all file handling inside ProductRepository so the model classes stay free of persistence logic, as the Workshop requires.

## Entry 2

**Date:** 2026-09-16 **Tool used:** Claude

**Reason for use:** Design and implement the accessory module of Requirement 1 and let a sale include accessories together with video games and consoles.

**Problem faced:** Accessories share id, title, price and stock with products but add their own attributes and a list of compatible consoles. The sale flow only searched the product catalogue, so an accessory id could not be sold, and the compatibility relation had to be stored in the CSV file in a way that could be read back.

**Prompt used:** ejecuta el requerimiento 1 de accesorios: jerarquía, repositorio, servicio, integración con ventas y submenú

**Solution obtained and decision taken:** Made Accessory an abstract class that extends Product, so an accessory fits directly into the existing List<Product> of a sale, with Controller, Cable and Memory as concrete subclasses. Stored the compatible consoles as ids joined with | in data/accessories.csv and used CONTROLLER/CABLE/MEMORY as discriminator in AccessoryRepository. AccessoryService offers the type filter and the compatibility query, and the sale flow now resolves each item in the product catalogue first and falls back to the accessory catalogue. Added the accessories submenu and wired the module in Main.

## Entry 3

**Date:** 2026-09-30 **Tool used:** Claude

**Reason for use:** Implement the returns module of Requirement 3 with the 30-day rule, the ownership rule and the automatic stock restoration.

**Problem faced:** A return references an existing sale and only some of its products, must be rejected after 30 days or when a product does not belong to the sale, and must give the stock back to the right catalogue. ReturnService also needed to find a sale by id, and SaleService had no such method yet.

**Prompt used:** ejecuta el requerimiento 3 de devoluciones con el plazo de 30 días, la regla de pertenencia y la restauración de stock

**Solution obtained and decision taken:** Added Sale.canBeReturned using ChronoUnit.DAYS.between, and Return with the refund calculation and a receipt in Spanish. ReturnService validates the sale, the deadline and that every product id belongs to the sale before creating the return, and restores the stock through the new restoreStock methods of ProductService and AccessoryService instead of duplicating the stock logic. Added SaleService.findById as a preparatory method, ReturnRepository to persist the returns, the returns submenu and the wiring in Main.

## Entry 4

**Date:** 2026-10-01 **Tool used:** Claude

**Reason for use:** Apply integration adjustment A1 of Requirement 5: allow category promotions over accessories.

**Problem faced:** CategoryDiscount only recognized VIDEOGAME and CONSOLE, and PromotionService accepted any text as category, so a promotion over accessories could not be created and an invalid category could be saved.

**Prompt used:** aplica el ajuste A1: que CategoryDiscount acepte ACCESSORY y que se validen las tres categorías

**Solution obtained and decision taken:** Added ACCESSORY as a target category that recognizes every Accessory instance in calculateDiscount, validated the three allowed categories in registerCategoryDiscount and added the accessories option to the category menu. Each change went in its own commit in the feature/accessory-category-discount branch.

## Entry 5

**Date:** 2026-10-02 **Tool used:** Claude

**Reason for use:** Recover develop after the merge of PR #11 left the project without compiling.

**Problem faced:** The old warranty branch had been merged with outdated versions of Sale, SaleService, SalePersistence, ConsoleMenu and Main, so develop had compilation errors and had lost the promotion and return integration. The Workshop forbids git push --force, so the history could not be rewritten.

**Prompt used:** el merge del PR #11 rompió develop, ¿cómo lo deshago sin usar push --force?

**Solution obtained and decision taken:** Reverted the merge commit with git revert -m 1 in its own branch (fix/revert-broken-warranty-merge) and integrated it through PR #15, then checked with git diff that develop was exactly as after PR #14. The warranty module was then rebuilt on top of the current develop, and the incident was documented in the integration analysis.

## Entry 6

**Date:** 2026-10-02 **Tool used:** Claude

**Reason for use:** Rebuild the warranty module of Requirement 4 on top of the integrated develop branch.

**Problem faced:** The end date of a warranty depends on its type, so it had to be computed in the constructor from an abstract method; the file had to tell basic and extended warranties apart; and only consoles must receive the automatic basic warranty. After testing, the final total of a sale with extended warranty was higher than expected.

**Prompt used:** ejecuta el requerimiento 4 de garantías sobre develop; después: el total sale más alto, revisa por qué se cobra dos veces la garantía

**Solution obtained and decision taken:** Implemented Warranty with getDurationInMonths called from the constructor, BasicWarranty (6 months, no cost) and ExtendedWarranty (12 months, 10% of the price), BASIC/EXTENDED as discriminator in WarrantyRepository, and WarrantyService with assignment and queries. registerSale grants the basic warranty to every Console and the requested extended ones. The duplicated cost came from adding it both when assigning the warranty and when computing the final total: removed the duplicated sum and rounded the extended cost to two decimals.

## Entry 7

**Date:** 2026-10-02 **Tool used:** Claude

**Reason for use:** Connect the warranty module to the sale record, the menu and the application startup.

**Problem faced:** The extended warranty cost had to be kept in the sale and survive a restart, the seller had to be asked about extended warranty for each console during the sale, and the warranty service had to be created in Main before it was used by SaleService.

**Prompt used:** integra las garantías en la venta, en el archivo de ventas y en el menú

**Solution obtained and decision taken:** Sale accumulates the warranty cost and SalePersistence stores it as an extra column; the sale menu asks, for each console, whether the client wants extended warranty; a warranty submenu shows the queries; and Main builds the warranty module and injects it into SaleService.

## Entry 8

**Date:** 2026-10-02 **Tool used:** Claude

**Reason for use:** Apply integration adjustment A5 of Requirement 5: refund returned items proportionally to the discount of the original sale.

**Problem faced:** Return.calculateRefundAmount added the list prices of the returned items, so when the sale had a promotion the client received more money than he paid.

**Prompt used:** aplica el ajuste A5: reembolso proporcional con precio x (1 - descuento / subtotal) y muéstralo en el recibo

**Solution obtained and decision taken:** Added calculatePaidRatio, the proportional discount and the refund per item, all rounded to two decimals, and changed the return receipt to show the list price, the proportional discount and the refund of each item. Updated the return analysis and its class diagram.

## Entry 9

**Date:** 2026-10-20 **Tool used:** Claude

**Reason for use:** Prepare the class diagram of Requirement 6 before writing any code.

**Problem faced:** The statement asks for the exception hierarchy, the validators and their relation with services, repositories and ConsoleMenu, but uses names that do not exist in our project (Customer, SaleRepository).

**Prompt used:** cómo debería quedar el diagrama de clases del requerimiento 6 con la jerarquía de excepciones, los validadores y su relación con los servicios y el menú

**Solution obtained and decision taken:** Mermaid diagram with GameZoneException as abstract root of the four exceptions, the three validators with static methods and the relations invokes/throws/catches. Kept the real names of the project (Client, SalePersistence) and explained the difference in a note. The tool rendered the diagram to check that it had no syntax errors.

## Entry 10

**Date:** 2026-10-20 **Tool used:** Claude

**Reason for use:** Implement the exception hierarchy and the three validators of Requirement 6.

**Problem faced:** GameZoneException could only have two constructors according to the statement, but PersistenceException must keep the original cause; and validateUniquePerson had to accept the list of clients, which in Java is not a List<Person>.

**Prompt used:** qué cambios le tocan al desarrollador 1 en el requerimiento 6 para crear las excepciones personalizadas y los validadores de productos, personas y ventas

**Solution obtained and decision taken:** GameZoneException abstract and unchecked with an error code; ResourceNotFoundException, BusinessRuleException, InvalidDataException with descriptive Spanish messages; PersistenceException keeping the cause through initCause; ProductValidator, PersonValidator and SaleValidator with static methods and a private constructor. Used List<? extends Person> in validateUniquePerson. One commit per class; the code was compiled and tested with thirteen cases before pushing.


## Requirement 7 — Phase 1: Audit

## Entry 11

**Date:** 2026-10-23 **Tool used:** Claude

**Reason for use:** Audit the code under the responsibility of Developer 1 against the nine GRASP patterns and record each verdict with evidence from the repository.

**Problem faced:** The scope covers six modules (products, accessories, promotions, returns, warranties, exceptions and validation). Some fragments show the warning sign of a pattern without being a real violation: both `CategoryDiscount` and `ProductRepository` branch on the concrete product type, but treating both the same way would have forced persistence knowledge into the model.

**Prompt used:** cómo se aplican los nueve patrones GRASP al código del desarrollador 1 y qué evidencia hay que registrar para cada veredicto

**Solution obtained and decision taken:** Reviewed each class and method in scope with the tool and recorded four violations with class, method and line range on the audited commit: V-01 (Information Expert, `ProductService` computes the stock instead of the product), V-02 (Low Coupling, `ProductService` creates its own repository with a fixed file), V-03 (Polymorphism, `CategoryDiscount` resolves the category with `instanceof`) and V-04 (Information Expert, `Return` recalculates the subtotal of the sale). The type checks of `ProductRepository` were recorded as correct, because the repository is a Pure Fabrication that isolates the file format. Problems whose origin is in other roles' code (`ConsoleMenu` creating domain objects, the same stock arithmetic in `restoreStock` and `AccessoryService`) were reported to the Technical Lead instead of being audited here. Also wrote the answers to questions 1 and 3 of the GRASP analysis.

**Related commits:** `<hash commit 1>` … `<hash commit 8>` (PR #<n>)

## Requirement 7 — Phase 2: Correction

## Entry 12

**Date:** 2026-10-23 **Tool used:** Claude

**Reason for use:** Correct the violations assigned to Developer 1 in the consolidated audit without changing what the user sees.

**Problem faced:** The corrections had to keep exactly the same results: the same products must receive a category discount for every target string, the refund of a return must keep the same value, and `Main` (Technical Lead) still created `ProductService` with its old constructor, so removing it at once would have left the project without compiling.

**Prompt used:** cómo corregir las violaciones del desarrollador 1 sin cambiar el comportamiento del menú y en qué orden hacer los commits para que siempre compile

**Solution obtained and decision taken:** `Product` received `decreaseStock`/`increaseStock` and the abstract `belongsToCategory(String)`, implemented by `VideoGame`, `Console` and `Accessory` ignoring case as before; `CategoryDiscount` delegates to it and lost its `instanceof` chain; `Return.calculatePaidRatio` asks the sale for `calculateTotal()`; `ProductRepository` and `ProductService` received constructors with the file path and the repository. The old constructors were kept as `@Deprecated` until the Technical Lead rewired `Main` inside this task group, and then removed, so every commit compiles. The same scripted run of the menu was executed before and after the corrections and the outputs, including the data files, were identical.

**Related commits:** `<hash A>` … `<hash H>` (PR #<n>)


