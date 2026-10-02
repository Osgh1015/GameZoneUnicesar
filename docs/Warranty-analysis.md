# Warranty Analysis — Final Exam: GameZone Unicesar

Analysis of the warranty module requested by the owner of GameZone Unicesar, before writing the code. The module is integrated into the existing four-layer architecture (`ui → service → persistence → model`).

## About the warranty hierarchy

**1. Both warranty types share attributes (dates, associated product) but also have different attributes and behaviors (duration, coverage, cost). How is this situation reflected in the design of the class hierarchy? What object-oriented mechanism allows each warranty type to have its own duration without duplicating code?**

The shared part is modeled once in an **abstract base class** `Warranty`, which owns the identifier, the associated `Product`, the associated `Sale`, the start date and the end date, together with the behavior that is identical for every warranty: `isActive(LocalDate)` and `generateWarrantyCertificate()`. What changes from one type to another is declared as **abstract methods**: `getDurationInMonths()`, `getWarrantyType()` and `getAdditionalCost()`. `BasicWarranty` and `ExtendedWarranty` extend `Warranty` and only implement those three methods.

The mechanism that allows each subtype to have its own duration without duplicating code is **runtime polymorphism (dynamic method dispatch)**. `Warranty` never asks what type it is; it simply calls `getDurationInMonths()`, and the JVM executes the implementation of the real object. That is why the calculation of the end date can be written a single time in the base class and still produce six months for a basic warranty and twelve for an extended one.

The coverage description (manufacturing defects only, versus manufacturing defects plus accidental damage) is documented in the JavaDoc of each subclass and expressed through the warranty type name, since the system does not need to evaluate the covered failure programmatically.

**2. The business rule establishes that only consoles generate an automatic basic warranty, not video games. In which layer of the system is this decision located, and what Java mechanism is used to verify the real type of a product? Justify.**

The decision belongs to the **service layer**, specifically to `SaleService.registerSale`, which delegates to the private method `generateWarranties`. The reason is that "which products generate a warranty" is a **business rule**, not a property of the product itself and not a presentation detail:

* It cannot live in the `model` layer, because `Product` and its subclasses only describe what a product *is*, and a product does not know in which sales it participates.
* It cannot live in the `persistence` layer, whose only responsibility is reading and writing files.
* It cannot live in the `ui` layer, because then the rule would apply only when a sale is registered through the console menu, and would disappear if another interface were added. The user interface only *asks* whether the client wants extended warranty; it never decides who deserves a basic one.

The Java mechanism used to verify the real type is the **`instanceof` operator** (`if (!(product instanceof Console)) continue;`), which inspects the actual runtime type of the object behind a `Product` reference. This is appropriate here because the rule is not a behavior of the product but an external decision about it: adding a method such as `generatesWarranty()` to `Product` would force the model to know about a concept (warranties) that belongs to a different module, coupling the product hierarchy to the sales process.

**3. The duration of each warranty type is different (6 or 12 months). How is the expiration date calculated in each subclass? Should this calculation be done in the warranty constructor or in a separate method? Justify.**

Each subclass only declares **how long** the coverage lasts, by returning a constant from `getDurationInMonths()` (`6` for `BasicWarranty`, `12` for `ExtendedWarranty`). The actual calculation is performed once in the constructor of `Warranty`:



The calculation belongs in the **constructor**, for three reasons:

1. **Invariant of the object.** A warranty without an expiration date is an incomplete domain concept. Computing the date at construction time guarantees that every `Warranty` object is valid from the moment it exists, and the field can therefore be declared `final`.
2. **No inconsistent state is possible.** If the date were calculated in a separate method called later, there would be a window in which the object exists with a null end date, and any caller who forgot to invoke that method would leave the object permanently broken.
3. **The end date is derived data, not input data.** The constructor never receives it from outside, so no caller can create a warranty whose expiration contradicts the rules of its type. This is also why the end date is not stored in the CSV file: it is recalculated when the object is rebuilt.

One technical detail deserves mention: calling an abstract method from the constructor of the base class is safe here **only because the subclasses return constants** and do not depend on their own fields, which are not yet initialized at that point. The compiler emits a `this-escape` warning for this pattern, and the design is only acceptable because the returned value comes from a `static final` constant. If a subclass ever needed to compute its duration from an instance field, the calculation would have to be moved to a factory method in `WarrantyService`.

**4. The extended warranty adds a cost of 10% of the product price to the total of the sale. At what point of the sale registration flow is this cost calculated and applied? What modifications are needed in the `SaleService.registerSale` method?**

The cost is calculated **after the sale has been created and the stock validated**, at the moment the extended warranty object is granted. The order of the flow is deliberate: the warranty needs a `Sale` object to point to, so the sale must already exist and be valid before any coverage is granted; and the total of the sale must be known only after every warranty has been added.

The amount itself is never calculated by `SaleService`. It is asked to the warranty through `getAdditionalCost()`, which returns `product.getPrice() * 0.10`. The service only accumulates it into the sale with `sale.addWarrantyCost(...)`, and `Sale.calculateTotal()` adds that accumulated value to the sum of the product prices. Keeping the percentage inside `ExtendedWarranty` means that if the store ever changes the rate, exactly one line of the system changes.

Modifications made to `SaleService`:

* The constructor receives an additional dependency, `WarrantyService`.
* The signature of `registerSale` receives a new parameter, `List<String> productIdsWithExtendedWarranty`, with the ids of the products for which the client accepted extended coverage. An overload with the original signature is kept, delegating with an empty list.
* After adding the sale to the in-memory list and before persisting it, the private method `generateWarranties(sale, products, productIdsWithExtendedWarranty)` is invoked. It grants a basic warranty to every console, grants an extended warranty when it was requested, and accumulates its cost in the sale.
* The persistence of the sale now happens after the warranties are generated, so the stored total already includes the coverage.

Modifications made to `Sale` (model layer): a private field `warrantyCost`, its getter, and the method `addWarrantyCost(double)`; `calculateTotal()` adds that field to the sum of the product prices. `Sale` does not know what a warranty is — it only knows that a sale can carry an extra charge, which keeps the model free of any dependency on the warranty module.

**5. The query for "warranties about to expire" requires iterating over all the warranties and filtering those whose end date falls within the next 30 days. In which class is this method located and what dependencies does it need? Why is this location coherent with the layered architecture?**

The method `listWarrantiesExpiringSoon(int daysAhead)` is located in **`WarrantyService`**, in the service layer. Its only dependency is the list of warranties the service already holds in memory, which was loaded through `WarrantyRepository`; it also uses `LocalDate.now()` to establish the current date and `Warranty.getEndDate()` to evaluate each element. It does not touch files and it does not read the console.

The location is coherent with the layered architecture for three reasons:

1. **It is a business query, not a data access operation.** "Which warranties expire in the next 30 days" is a commercial criterion of the store. The repository only knows how to read and write lines in `data/warranties.csv`; giving it the filter would place a business rule in the persistence layer and break the rule that forbids business logic there.
2. **It must be reusable by any interface.** Placing it in `ConsoleMenu` would tie the query to the console menu and would force it to be rewritten for any other interface. The `ui` layer only asks for the number of days and prints the result.
3. **It respects the direction of dependencies (`ui → service → persistence → model`).** `WarrantyService` depends downward on `WarrantyRepository` and on the model classes, and nothing in the lower layers depends on it. The `model` classes remain unaware of the query: `Warranty.isActive(LocalDate)` answers a question about a single warranty, which is intrinsic behavior of the object, while filtering a whole collection is a coordination task and therefore belongs to the service.