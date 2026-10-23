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

## Information Expert

**Verdict:** _pending — audit in progress._

- **People — Correct.** `Client.getDescription()` and `Seller.getDescription()` build the description from their own fields; `PersonService.findClientById` and `findVendorById` (`PersonService.java:88`, `:103`) search the lists the service owns.

- **Accessories — Mixed.** `AccessoryService.findAccessoriesCompatibleWith` (`AccessoryService.java:99`) asks each accessory `isCompatibleWith(consoleId)` instead of reading its list, which is correct; the stock update is a violation (V-02).

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

## Creator

**Verdict:** _pending — audit in progress._

- **People — Correct.** `PersonRepository.loadClients` and `loadVendors` (`PersonRepository.java:81`, `:116`) create `Client` and `Seller` objects from the records they read. The `Client` registered through the menu is created by `ConsoleMenu` (Technical Lead), reported in the developer1 audit.

- **Accessories — Correct.** `AccessoryRepository.fromLine` (`AccessoryRepository.java:131-135`) creates each accessory type from the record it reads.

## Controller

**Verdict:** _pending — audit in progress._

## Low Coupling

**Verdict:** _pending — audit in progress._

- **People — Violation (V-01).** `Person`, `Client` and `Seller` import nothing outside the model, and `PersonRepository` depends only on the model and the exceptions, which is correct; `PersonService` is tied to the concrete `PersonRepository` and to its files because it creates it.

- **Accessories — Correct except for V-03.** `AccessoryService` receives its repository by constructor; `matchesType` additionally ties it to `Controller`, `Cable` and `Memory` (part of V-03).

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

## High Cohesion

**Verdict:** _pending — audit in progress._

- **People — Correct.** `PersonRepository` only reads and writes the two people files; `PersonService` only registers and looks up people. It keeps two pairs of equivalent methods (`findVendorById`/`findSellerById`, `PersonService.java:103`, `:112`; `listVendors`/`listSellers`, `:78`, `:121`) because other modules call both names; they do the same job, so the class is still focused.

- **Accessories — Correct.** `AccessoryRepository` only handles `data/accessories.csv`; `AccessoryService` only handles the accessory rules.

## Polymorphism

**Verdict:** _pending — audit in progress._

- **People — Correct.** `Person.getDescription()` is abstract and `Client`/`Seller` override it; no code in scope asks a person for its concrete type.

- **Accessories — Mixed.** `AccessoryRepository.toLine`/`fromLine` (`AccessoryRepository.java:100-110`, `:131-135`) branch on the concrete type and on a type tag; this is **not** treated as a violation: the repository is a Pure Fabrication whose job is to keep the file format out of the model. `AccessoryService.matchesType` is a violation (V-03).

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

## Pure Fabrication

**Verdict:** _pending — audit in progress._

- **People — Correct.** `PersonRepository` is the artificial class that keeps file access out of `Client` and `Seller`.

- **Accessories — Correct.** `AccessoryRepository` keeps the CSV format out of the accessory hierarchy.

## Indirection

**Verdict:** _pending — audit in progress._

- **People — Correct.** `PersonService` sits between `ConsoleMenu` and `PersonRepository`; the menu never touches the repository.

- **Accessories — Correct.** `AccessoryService` mediates between the menu and `AccessoryRepository`.

## Protected Variations

**Verdict:** _pending — audit in progress._

- **People — Violation (V-01).** A change of the file format is confined to `PersonRepository`, which is correct, but the file locations are fixed inside the repository and the service creates its own repository, so a change of location is not protected.

- **Accessories — Violation (V-03).** The vocabulary of accessory types and its mapping to classes is hard-coded in the service. The full analysis is recorded once, under Polymorphism.
