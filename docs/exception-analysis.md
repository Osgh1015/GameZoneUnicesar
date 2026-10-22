Exception Handling Analysis - GameZone Unicesar
Requirement 6 adds two new layers to the system: a hierarchy of custom exceptions
(`com.gamezone.exceptions`) and one validator per functional module
(`com.gamezone.validation`). Services delegate their checks to the validators, repositories
translate `IOException` into `PersistenceException`, and `ConsoleMenu` catches each kind of
error separately so the user receives a clear message according to its category. The
external behavior of the system does not change: the same conditions that caused an error
before still cause an error, but now with a more informative exception.
1. Checked or unchecked root exception
   `GameZoneException` extends `RuntimeException` (unchecked).
   Error handling happens only in the user interface. Repositories, validators and
   services cannot do anything useful to recover from a missing product or a corrupted
   file; they only need to report it. The only layer that decides what to do with an error
   is `ConsoleMenu`, which shows the message to the user. With a checked exception, every
   intermediate method would be forced to declare or catch it even though it does nothing
   with it.
   Method signatures stay the same. If the root exception were checked, every method of
   the repositories, the services and the menu would need a `throws` clause, and the clause
   would propagate through all the layers (`ProductRepository.save` → `ProductService` →
   `SaleService` → `ReturnService` → `ConsoleMenu`). Requirement 6 asks for a refactor that
   preserves the external behavior, and changing the signature of almost every public method
   goes against that.
   It is consistent with the existing code. The system already reported errors with
   `IllegalArgumentException`, `IllegalStateException` and `NoSuchElementException`, which
   are all unchecked. The new exceptions replace them without changing how they propagate.
   Consequence on the signatures: service and repository methods do not declare `throws`.
   The exceptions each method can throw are documented with the `@throws` tag of its JavaDoc.
   Since the compiler does not force anyone to catch an unchecked exception, `ConsoleMenu`
   catches them on purpose, and it keeps a final `catch (RuntimeException e)` as a safety net
   so that no error ever closes the application or shows a stack trace.
2. Why a custom exception hierarchy
   Throwing `IllegalArgumentException` in every case has two problems:
   It does not tell the categories apart. A product that does not exist, insufficient
   stock, a negative price and a file that cannot be read all looked the same to the menu.
   This is exactly the problem described in the context of the requirement: sellers could
   not tell whether the error was their fault, an inventory problem or a technical failure.
   It mixes our errors with Java's errors. `NumberFormatException` is a subclass of
   `IllegalArgumentException`, so a `catch (IllegalArgumentException e)` also catches
   number-parsing errors thrown by the JDK and treats them as if they were business errors.
   With a hierarchy rooted in `GameZoneException`:
   The menu can catch each category (`ResourceNotFoundException`, `BusinessRuleException`,
   `InvalidDataException`, `PersistenceException`) and show a different message for each
   one, or catch `GameZoneException` to handle every error of the system at once.
   Each exception builds its own descriptive message in Spanish from its parameters (the
   resource type and id, the invalid field, the file and operation), and
   `PersistenceException` keeps the original cause so the technical trace is not lost.
   The root class carries an error code (`errorCode`) that every exception inherits.
   Design principles applied:
   Inheritance and polymorphism: all exceptions are a `GameZoneException`, and the menu
   decides what to do according to the real type of the exception it receives.
   Single responsibility: each exception class represents one category of error, and
   each validator concentrates the checks of one module, so services only contain business
   logic.
   Open/closed principle: a new category of error can be added as a new subclass without
   modifying the existing exceptions or the code that catches `GameZoneException`.
   Abstraction: `GameZoneException` is abstract because a "generic" error with no
   category has no meaning in the system; only the four specialized exceptions are thrown.
3. Where the validators are called

The validators are called **from the services, at the beginning of each operation, before
any change of state or any write to a file**. For example:

- `ProductService.registerVideoGame` and `registerConsole` call
  `ProductValidator.validateProductData` before adding the product to the list.
- `PersonService.registerClient` calls `PersonValidator.validatePersonData` and
  `validateUniquePerson` before adding the client.
- `SaleService.registerSale` calls `SaleValidator.validateSaleData` and
  `ProductValidator.validateStockAvailability` before creating the sale, so the stock is
  never decreased for a sale that will later be rejected.

Validating before acting guarantees that a failed operation leaves the system exactly as it
was: no half-registered sales and no stock decreased by mistake.

This location is consistent with the layered architecture because the validation layer sits
logically **between `service` and `model`**: the services depend on the validators, and the
validators depend only on the model classes and on the exceptions layer, which is
transversal.

**Validators cannot access the persistence package.**

- It would create a dependency (`validation → persistence`) that breaks the direction of the
  architecture, and it would duplicate the data access that already belongs to the
  repositories.
- The validators would stop being simple, reusable classes with static methods and would
  start depending on files.

When a validator needs existing data, the service passes it as a parameter. That is why
`validateUniquePerson` receives the list of existing people instead of loading it: the
service, which owns the data, supplies it, and the validator only checks the rule.
4. Preserving the external behavior

The strategy of the team is to change only **the type of exception**, never the
**condition** that causes it. Every place that threw `IllegalArgumentException`,
`IllegalStateException` or `NoSuchElementException`, or printed a message and returned
`null`, now throws the equivalent custom exception under exactly the same condition.

To make sure nothing was broken:

1. **Baseline before the refactor.** Before writing any code, the ten operations of the
   menu were executed on `develop` and the results were recorded.
2. **Incremental integration.** The work was split into three Pull Requests (exceptions
   and validators, services and repositories, user interface). After each one, the project
   was compiled and the ten operations were executed again.
3. **Same data files.** The format of the files in `data/` did not change, so the data
   saved before the refactor still loads correctly. A missing data file still starts the
   system with an empty list; only a real read or write error throws
   `PersistenceException`.

**Evidence:** the following table compares the result of every operation before and after
the refactor, including the error cases.

| # | Operation | Input | Result before | Result after |
|---|---|---|---|---|
| 1 | Register video game | Valid data | "Videojuego registrado correctamente." | "Videojuego registrado correctamente." |
| 2 | Register console | Valid data | "Consola registrada correctamente." | "Consola registrada correctamente." |
| 3 | List products | - | Every product listed | Every product listed (same list) |
| 4 | Register client | Valid data | "Cliente registrado correctamente." | "Cliente registrado correctamente." |
| 5 | List clients | - | Every client listed | Every client listed (same list) |
| 6 | List sellers | - | 3 sellers listed | 3 sellers listed |
| 7 | Register sale | Client, seller and items | "Venta registrada correctamente." + receipt | "Venta registrada correctamente." + receipt |
| 8 | Sales history | - | Every sale listed | Every sale listed (same list) |
| 9 | Purchase history of a client | Client id `111` | Sales of the client listed | Sales of the client listed (same list) |
| 10 | Sales attended by a seller | Seller id `V001` | Sales of the seller listed | Sales of the seller listed (same list) |
| E1 | Register sale | No items | "Una venta debe contener al menos un producto." / "No se pudo registrar la venta." | "No se puede completar la operación: Una venta debe contener al menos un producto." |
| E2 | Register sale | Quantity above the stock | "Stock insuficiente para uno o más productos." / "No se pudo registrar la venta." | "No se puede completar la operación: Stock insuficiente para 'PlayStation 5' (P002): disponible 5, solicitado 6." |
| E3 | Register sale | Item id that does not exist | "Producto no encontrado: ZZZ" / "No se pudo registrar la venta." | "No se encontró el recurso solicitado: El producto con identificador ZZZ no existe en el sistema." |
| E4 | Register client | Id that already exists | Accepted (duplicated client) | "No se puede completar la operación: Ya existe una persona registrada con el identificador 111." |
| E5 | Register client | Empty name / invalid email | "Error: El nombre del cliente no puede estar vacío." / invalid email accepted | "Los datos ingresados no son válidos: El campo 'nombre' no puede estar vacío." / "...El campo 'correo electrónico' debe tener un formato válido..." |
| E6 | Register product | Price of zero | Accepted (product with price 0) | "Los datos ingresados no son válidos: El campo 'precio' debe ser mayor que cero." |

Operations 1 to 10 give exactly the same result before and after the refactor. In the
error cases E1, E2 and E3 the same condition still causes an error, but the message now
tells the category of the problem. Cases E4, E5 (invalid email) and E6 were accepted
before and are now rejected: they are the new validations required by Requirement 6
(`validateUniquePerson`, the email format of `validatePersonData` and the positive price
of `validateProductData`).
The "before" and "after" columns are filled with the output of the console, and
screenshots of both runs are added to this document.

**Screenshots after the refactor:** see the folder [`docs/evidence/after/`](./evidence/after/).

5. How ConsoleMenu catches the exceptions
   The team uses a single `try` block with multiple `catch` blocks, placed in `start()`
   around the main menu `switch`, ordered from the most specific exception to the most
   general one:
```
try {
    switch (option) { ... }
} catch (ResourceNotFoundException e) {
    System.out.println("No se encontró el recurso solicitado: " + e.getMessage());
} catch (BusinessRuleException e) {
    System.out.println("No se puede completar la operación: " + e.getMessage());
} catch (InvalidDataException e) {
    System.out.println("Los datos ingresados no son válidos: " + e.getMessage());
} catch (PersistenceException e) {
    System.out.println("Ocurrió un problema al acceder a los datos del sistema. Contacte al administrador.");
} catch (RuntimeException e) {
    System.out.println("Ocurrió un error inesperado. Intente de nuevo.");
}
```
Reasons for this choice:
Different messages for each category. Each `catch` block maps one type of exception to
its own message, which is the main goal of the requirement. The persistence message does
not show the technical detail, because it is meant for the administrator, not for the
seller.
Clarity. A single `catch (GameZoneException e)` would need `instanceof` checks inside
the block to choose the message, which hides the four categories inside one block. Nested
("cascading") `try` blocks make the flow hard to read and to maintain.
One place instead of seven. Because all the submenus are called from the `switch` of
`start()`, the four `catch` blocks are written only once instead of being repeated in every
submenu.
The order matters. The specific exceptions go first and the general `RuntimeException`
goes last. Java does not compile a `catch` of a subclass placed after the `catch` of its
superclass, so the compiler itself protects this order.
Safety net. The final `catch (RuntimeException e)` guarantees that no unexpected error
closes the application or shows a raw stack trace to the user.
6. Project-specific decisions

- **`SalePersistence` instead of `SaleRepository`.** The statement calls the sales
  repository `SaleRepository`. In this project the class has been called `SalePersistence`
  since the Workshop; the name was kept to avoid changing `Main`, `WarrantyService` and the
  previous diagrams. Its responsibility is the same.
- **`Client` instead of `Customer`.** The statement uses `Customer` in
  `SaleValidator.validateSaleData`; in this project the class is `Client`.
- **`validateUniquePerson` receives `List<? extends Person>`.** In Java a `List<Client>` is
  not a `List<Person>`, so the wildcard lets the same method validate clients and sellers.
- **`findById` methods still return `null`.** `SaleService`, `ReturnService`,
  `WarrantyService` and the repositories use `findById` to check whether an id belongs to a
  product or to an accessory, so they depend on a `null` result. Instead of changing that
  contract, the services call `validateProductExists`, `validatePersonExists` and
  `validateSaleExists`, which throw `ResourceNotFoundException` when the result is `null`.
- **Scope of the refactor.** Requirement 6 refactors `ProductService`, `PersonService`,
  `SaleService` and their repositories. The services of the extension modules
  (`ReturnService`, `PromotionService`, `AccessoryService`, `WarrantyService`) are outside
  that scope, so the menu keeps handling their `IllegalArgumentException` in the flows that
  use them.

