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
