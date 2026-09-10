# Analysis — Taller 2: GameZoneUnicesar

## About the people in the system

**1.What attributes are common to all the people who interact with the store, and which ones are specific to each type of person? How is this distinction reflected in a class hierarchy?**
All people who interact with the store share basic identity data: `name`, `identification`, and `contactPhone`. Clients additionally have an `email` and a `purchaseHistory`; sellers additionally have an `employeeCode` and an `assignedShift`. This is reflected by creating an abstract base class `Person` that holds the three shared attributes and their shared behavior (e.g., getters, a `toString()`), and two concrete subclasses, `Client` and `Seller`, that extend `Person` and add their own specific fields. This avoids duplicating the shared attributes and centralizes common logic in one place.

**2. Should there be a class that represents a "generic person" without specifying their role? Why or why not? What implication does this decision have for the possibility of instantiating that class?**
No. In this domain, every real person who exists in the system is always either a `Client` or a `Seller` — there is no business scenario that requires a person with no role. Therefore `Person` should be declared **abstract**, so it cannot be instantiated directly. Implication: `new Person(...)` becomes a compile-time error; the class only exists to be extended, guaranteeing that every object created through it is always a complete, valid domain concept (a client or a seller), never an incomplete "person with no purpose."

## About the products in the system

**3. What characteristics do all the products sold by the store have in common, regardless of their type? What characteristics are specific to each type of product?**
Every product shares: `id`, `title`, `price`, and `availableQuantity`. `VideoGame` additionally has `platform`, `genre`, and `ageRating`. `Console` additionally has `brand`, `model`, and `generation`. This mirrors the people hierarchy: an abstract base class `Product` with the shared attributes, and two subclasses, `VideoGame` and `Console`, that extend it.

**4. Each type of product must be able to provide a description that integrates its particular characteristics. How should this behavior be declared in the base class to ensure that all subclasses implement it in their own way? What object-oriented programming mechanism allows this?**
`Product` should declare an **abstract method**, e.g. `getDescription()`, with no implementation. Each subclass (`VideoGame`, `Console`) is then forced to provide its own implementation, combining the shared fields with its specific ones. At runtime, calling `product.getDescription()` on a `Product` reference automatically executes the correct subclass version — this is **runtime polymorphism (dynamic method dispatch)**, the OOP mechanism that lets the same method call behave differently depending on the real object type, without the caller needing to know which subtype it is.

## About sales and relationships between entities

**5. A sale involves a client, a seller, and one or more products. What types of relationships exist between the class representing the sale and the other classes in the system? Are these relationships based on inheritance, association, composition, or another type? Justify.**
A `Sale` is linked to exactly one `Client`, one `Seller`, and one or more products. These are **association** relationships, not inheritance:

* `Sale` → `Client` / `Seller`: **aggregation**. The sale references existing people, but a `Client` or `Seller` exists independently of any particular sale and is not destroyed if the sale is removed.
* `Sale` → items sold: typically modeled through a small `SaleItem` class holding the product reference, quantity, and unit price at the time of sale. The relationship `Sale` → `SaleItem` is **composition**, since a line item has no meaning or lifecycle outside of the sale that owns it; `SaleItem` → `Product` remains a simple association/reference.

**6. Should the sale be responsible for calculating its own total, or should this responsibility fall to another class? Justify your decision**
`Sale` should be responsible for calculating its own total (e.g. a `calculateTotal()` method that sums `quantity × unitPrice` across its line items). This is domain behavior intrinsic to what a sale *is*, and it only needs data the `Sale` object already owns — keeping it there respects encapsulation and the single-responsibility principle. This is different from persistence logic (see Q10 below): computing a total is a business rule about the sale itself, not about reading/writing data to storage, so it correctly belongs in the model layer.

## About business rules/constraints

**7. How is the design ensured that a sale cannot be registered without at least one product? At what point in the system should this rule be validated?**
The validation should happen at the point where a sale is created/registered — inside the service layer (e.g. `SaleService.registerSale(...)`) — by checking that the line-item list is not empty **before** persisting or confirming the sale, and throwing an exception (or returning an error) otherwise. Relying only on the UI to prevent this is not enough, since business rules must hold regardless of which interface triggers them; the service layer is what actually enforces the constraint.

**8. How is the automatic inventory update reflected in the design when a sale is registered? Which classes are involved in this operation?**
When a sale is registered, `SaleService` orchestrates the process: it checks the available stock through `Product`/its repository, and for each line item calls a method such as `Product.reduceStock(quantity)`. The updated `Product` objects are then saved through the persistence layer. Classes involved: `Sale` and `SaleItem` (what was sold), `Product` and its subclasses (whose stock changes), `SaleService`/`ProductService` (the orchestration and validation), and the persistence classes (which write the updated stock to storage).

## About the layer organization

**9. The system must be organized into four layers: model, persistence, services, and user interface. What types of classes belong to each layer? What criterion determines which layer a class should be placed in?**

* **Model**: domain entities that represent business concepts and their inherent state/behavior — `Person`, `Client`, `Seller`, `Product`, `VideoGame`, `Console`, `Sale`, `SaleItem`.
* **Persistence**: classes whose only job is reading and writing that data to files (repositories/DAOs) — `ClientRepository`, `ProductRepository`, `SaleRepository`, etc.
* **Services**: classes implementing business rules, validations, and orchestration between model and persistence — `ClientService`, `ProductService`, `SaleService`.
* **User interface**: classes handling input/output with the user (menus, screens), which call the service layer.

The deciding criterion: ask what the class's single responsibility is. If it represents domain data/state → model. If it only moves that data to/from storage → persistence. If it enforces rules or coordinates operations → service. If it interacts with the user → UI.

**10. Why should the logic for saving and retrieving data from files not be placed inside the domain classes? What problems arise when these responsibilities are mixed?**
Because it would violate the single-responsibility principle: a model class should only represent a business concept and its natural behavior, not know *how* it gets stored. Mixing them creates several problems: the domain classes become tightly coupled to a specific storage technology (files, in this case), they become harder to test in isolation, and changing the storage mechanism later (e.g., moving from files to a database) would require modifying the model classes themselves instead of just the persistence layer — defeating the entire purpose of separating the system into layers.

**11. What dependencies are allowed between the layers, and which ones are prohibited? Justify the purpose of the allowed dependencies**
Allowed direction: **UI → Services → Persistence → Model**, where each layer only depends on the layer(s) below it. `Model` should not depend on any other layer — it is the most stable, foundational layer. Prohibited: any dependency going the other way (e.g., `Model` depending on `Persistence` or `Services`), a layer skipping past the one directly below it (e.g., `UI` calling `Persistence` directly instead of going through `Services`), and circular dependencies between layers. This direction is justified because the model represents the core, stable business concepts that should not change based on how they are displayed or stored; keeping dependencies pointing inward toward the model keeps the domain reusable and independent of UI or storage decisions.
