# GRASP Analysis — Requerimiento 7

Answers to the five orienting questions of the GRASP audit and correction. Each answer was discussed by the team and written by the role indicated.

## Q1. Criteria for a verdict and minimum evidence

*Written by Developer 1.*

A fragment is a **violation** when a responsibility is placed where the pattern says it should not be, and the placement has a concrete cost that we can point to in our own code: a rule that is repeated in several classes, a class that must be edited when an unrelated variant is added, a dependency that ties a class to one concrete implementation, or a class that recalculates information another object already owns. A pattern is **correct** when we can name the class and method where it is applied and explain why that class is the right owner. A pattern is **not applicable** when the audited code has no point where the question of the pattern even arises (for example, Controller for model classes that never receive a system event).

We used the warning sign of each pattern as a first filter (`instanceof` or type strings for Polymorphism, getter-and-setter arithmetic for Information Expert, `new` of a concrete collaborator inside a service for Low Coupling, and so on), but a warning sign alone was not enough. For example, the type checks inside `ProductRepository.toLine`/`fromLine` were judged correct, because the repository is a Pure Fabrication whose purpose is to keep the file format out of the model, while the type checks inside `CategoryDiscount` were judged a violation, because they are a business rule that belongs to each product type.

Minimum evidence accepted in the consolidation for each verdict:

- **Violation:** class, method and the exact code fragment or line range on the audited commit; why it breaks the pattern; a concrete consequence; a proposed correction that names the pattern applied; and whether the correction touches another role's code.
- **Correct:** at least one class and method where the pattern is applied, with one sentence explaining why.
- **Not applicable:** one sentence explaining why the audited scope has no point of application.

## Q2. Boundary findings

*Written by the Technical Lead.*

Boundary findings appeared in three ways, and all of them are listed with their split in the "Boundary findings" table of `consolidated-audit.md`:

- **A violation in one role's code whose correction needs a class of another role.** The most frequent case is the wiring in `Main`: `ProductService` (V-02, Developer 1) and `PersonService` (V-05, Developer 2) create their own repositories, and `ReturnRepository` (V-10) receives services; the violations belong to the developers, but `Main`, which must create and inject the objects, belongs to the Technical Lead. The same happens with model methods: Developer 2's services must ask `Return`, `Warranty` and the accessories (V-07, V-08, V-12), which are Developer 1's classes, and `Sale` (V-09), which is the Technical Lead's; the Technical Lead's code must ask `Product`, `Console` and `ExtendedWarranty` (V-15, V-16, V-17), which are Developer 1's.
- **A violation whose correction crosses a layer of another role.** `SalePersistence` depends on services (V-19, Technical Lead); its correction removes `PersonService` and `SalePersistence` from `WarrantyService`, which is Developer 2's.
- **The same problem seen from several sides.** The read-compute-write stock sequence appears in three methods of three owners (V-01, V-06, V-14), and the lookup of an item in two catalogs appears in the returns and in the sales modules (V-11, V-18).

Each boundary finding was registered once, with a single identifier, in the report of the role whose code holds the violation, and the part that touches another role's class is written in its "Other role's code involved" field. The correction is split by owner: whoever is responsible for the code that changes makes that change, as the statement requires. Model methods that another role needs are added first by their owner, as purely additive commits; when a constructor changes, the owner keeps the old one `@Deprecated` until the Technical Lead rewires `Main`, and then removes it.

The related findings that look alike were **not** merged when each fragment can be corrected completely by its owner (V-01, V-06 and V-14 are three methods in three classes); they share the solution instead, which is written once (`Product.decreaseStock`/`increaseStock` in V-01, `ItemCatalog` in V-11).

## Q3. Fragments that break more than one pattern

*Written by Developer 1.*

Yes. The clearest case in Developer 1's scope is `CategoryDiscount.belongsToCategory` (developer1-audit V-03). The same fragment breaks **Polymorphism** — it asks each product for its concrete class with `instanceof` instead of letting each product answer —, **Low Coupling** — the promotion depends on three concrete classes of the product hierarchy — and **Protected Variations** — the mapping between category names and product classes is hard-coded in the promotion, so a new product type is an unprotected variation point. A second case is `ProductService` creating its own `ProductRepository` with a fixed file (developer1-audit V-02), which breaks Low Coupling and Protected Variations at the same time.

We recorded each fragment **once**, with a single ID, under the pattern that best describes its root cause (Polymorphism for V-03, Low Coupling for V-02), and added a short cross-reference in the sections of the other patterns that points to the same ID. The summary table lists the ID under every pattern it affects, so the number of distinct violations is not inflated.

The pattern that guides the correction is the one whose solution removes the root cause. For V-03 that is Polymorphism: once each product answers `belongsToCategory`, the promotion no longer depends on the concrete classes and the variation point is protected, so one change fixes the three patterns. For V-02 it is Low Coupling: once the repository is injected, the file location is also decided outside the service.

## Q4. Non-regression strategy

*Written by Developer 2.*

The correction is a refactor, so "nothing broke" means: for the same inputs, every menu operation prints the same text and leaves the same content in the files of `data/`. The project has no automated test suite, so the team uses two complementary checks, run on `develop` **before** the first correction (baseline), on every Pull Request of the correction (affected operations) and at the end of the phase (all operations):

1. **Compilation.** `mvn clean compile` (or *Build → Rebuild Project* in IntelliJ) must succeed before every commit of code, not only before each Pull Request, so that the shared branch always compiles.
2. **Scripted run of the whole menu.** The application is run from a scratch copy of `data/`, never from the repository's own files, with a fixed input script that exercises every menu operation:
    - Products: register a video game and a console, a duplicated id, a price of zero, list.
    - People: register a client, a duplicated id, an invalid email, list clients and sellers.
    - Sales: a sale with a console with extended warranty, a video game and accessories; a sale that activates each category promotion (consoles, video games, accessories); an unknown client, an unknown item, insufficient stock, a sale without items; the full history, by client and by seller; the receipt of an existing and of a missing sale.
    - Accessories: register a controller, a cable and a memory; list all; list by type; compatible accessories of a console.
    - Promotions: register the three types, an invalid category, reversed dates; list all and active.
    - Returns: a partial return of a console and an accessory, the same product returned again, a product that is not in the sale; list all, by client and by sale; the monthly balance.
    - Warranties: query an existing and a missing warranty; list all, active and expiring soon.
    - An invalid option of the main menu, non-numeric input and exit.

   The standard output and the final content of every file in `data/` are captured. Values that legitimately change between runs are normalized before comparing: the random sale, return and warranty identifiers (UUIDs) become `<uuid>` and today's date becomes `<TODAY>`. The normalized output is compared with the baseline line by line.

The evidence shown at the end is recorded in `docs/grasp-refactor-log.md`: the commit of the baseline, the number of normalized lines of the baseline and of the final run, and the result of the comparison, which must be **no differences**. Defects recorded as out of scope are part of the expected output, so a refactor that changes one of them by accident also shows up in the comparison.

## Q5. Order of the corrections and coordination

*Written by the Technical Lead.*

The order follows the dependencies between corrections, from the classes other corrections need to the classes that use them:

1. **Developer 1 — model first (V-01 to V-04).** The new methods of the model (`Product.decreaseStock`/`increaseStock`, `Product.belongsToCategory`) are purely additive, so they can go first and the other roles build on them. V-02 needs `Main`, so the Technical Lead's commit in `Main` goes inside Developer 1's group.
2. **Developer 2 — repositories and services of the extension modules (V-05 to V-12).** The group starts with the boundary commits that add the model methods it needs (Developer 1: `Accessory.getAccessoryType`, `Return.belongsToClient`/`belongsToSale`, `Warranty.covers`/`belongsToSale`; Technical Lead: `Sale.findProduct`/`countUnitsOf`). Then Developer 2 corrects the services, creates `ItemCatalog` (V-11) and changes the repositories and constructors; the Technical Lead rewires `Main` once for V-05, V-10 and V-11.
3. **Technical Lead — sales and user interface last (V-13 to V-20).** `Sale`, `SaleService`, `SalePersistence`, `ConsoleMenu` and `Main` use the services of the other roles, so they are corrected when those services already have their final API. The group starts with the boundary commits of Developer 1 (`Product.isWarrantyEligible`, `Product.getTypeLabel`, `ExtendedWarranty.quoteFor`) and of Developer 2 (`ItemCatalog.decreaseStock`); `WarrantyService` changes inside this group for V-19. The extraction of the submenus of `ConsoleMenu` (V-20) goes at the very end, so that it moves code that is already corrected.

Coordination rules on the shared branch `feature/grasp-refactor`:

- Only one task group works on the branch at a time. Each group starts with `git pull`, and it ends with the project compiling, its non-regression run and its Pull Request merged; only then does the next group start.
- Every commit must compile. When a change in one role's class would break another role's class (for example, a new constructor of `ProductService` that `Main` must use), it is done in three steps: the owner adds the new code and marks the old one `@Deprecated`; the owner of the other class adapts it in a commit inside the same task group; the owner removes the deprecated code.
- Commits that correct a violation mention its definitive identifier, for example `refactor(model): ... (V-03)`, and each correction is logged in `docs/grasp-refactor-log.md` by its author, including the boundary commits made by another role.
