# SOLID Analysis — Requerimiento 8

Answers to the five orienting questions of the SOLID audit and correction. Each answer was discussed by the team and written by the role indicated.

## Q1. Criteria for a verdict and minimum evidence

*Written by Developer 1.*

For every principle we asked its own question about a concrete class or method, and we accepted a **violation** only when we could name a change that is likely in this system and show that the current code makes that change expensive or risky:

- **SRP:** the class has more than one reason to change, and the reasons come from different actors or concerns (for example, the data and rules of a return, and the text layout of its receipt).
- **OCP:** adding a new variant that the system already anticipates (a new product type, accessory type, warranty kind, catalog or menu module) forces editing a class that already works, usually because of a `switch`, an `instanceof` chain or a type tag.
- **LSP:** a subclass cannot be used where its base class is expected without a different result, a stronger precondition, a weaker postcondition or an exception the base class does not declare.
- **ISP:** a client depends on a class or interface with operations it never uses, so a change in those operations reaches the client.
- **DIP:** a high-level class (a service or use case) depends on a concrete low-level class (a repository, a file, another service it only queries) instead of an abstraction it owns.

A **correct** verdict needs one class and method where the principle holds and the reason. A **not applicable** verdict needs a sentence explaining why the code has no point where the question arises (for example, LSP for classes without subclasses).

A warning sign alone was not enough. `instanceof` and type tags inside the repositories (`ProductRepository.toLine`/`fromLine`) were examined for OCP: the repository is the only place that knows the file format, and a new product type must be written to the file somewhere, so this is the place where the change is expected, not a rule scattered across the system. The same check inside a service or a use case was treated as a violation.

Minimum evidence accepted in the consolidation for each verdict:

- **Violation:** class, method and the exact code fragment or line range on the audited commit; why it breaks the principle; a concrete consequence; a proposed correction; and whether it touches another role's code.
- **Correct:** at least one class and method where the principle holds, with one sentence explaining why.
- **Not applicable:** one sentence explaining why the audited scope has no point of application.

## Q2. Boundary findings

*Written by the Technical Lead.*

A boundary finding is a violation whose correction needs code of more than one role. We looked for them in two ways: every report fills the field "Other role's code involved" for each violation, and in the consolidation we checked every dependency that crosses from one role's class to another's (constructor parameters, fields and method calls between services, and the wiring in `Main`).

The concrete cases found are dependencies of a service of one role on a concrete service of another role, of which the client uses only one or two operations:

- **`SaleService` and `SaleAssembler` (Technical Lead) → `PersonService`, `PromotionService` and `WarrantyService` (Developer 2).** The sale registration uses only `findClientById`/`findSellerById`, `findBestPromotionFor` and `assignBasicWarranty`/`assignExtendedWarranty`, but it depends on the three complete concrete services (ISP and DIP). The client defines what it needs, so the Technical Lead creates the small interfaces in the service layer (`PersonLookup`, `PromotionSelector`, `WarrantyAssigner`), Developer 2 makes the three services implement them, and the Technical Lead changes `SaleService` and `SaleAssembler` to depend on them (V-11).
- **`ReturnService` (Developer 2) → `SaleService` (Technical Lead).** The returns module uses only `findById` and `getSalesHistory` of `SaleService` (ISP and DIP). Developer 2 creates the read-only interface (`SaleLookup`), the Technical Lead makes `SaleService` implement it, and Developer 2 changes `ReturnService` to depend on it (V-07). In the same module the monthly balance moves to `MonthlyBalanceService` (SRP, V-08), so the Technical Lead also makes the returns menu ask the new service.
- **`ItemCatalog` (Developer 2) → `ProductService` (Developer 1) and `AccessoryService` (Developer 2).** `ItemCatalog` decides with `instanceof` which catalog owns an item (OCP) and depends on the concrete services (DIP). Developer 2 creates the abstraction of an item source (`ItemSource`) and implements it in `AccessoryService`, Developer 1 implements it in `ProductService`, and Developer 2 makes `ItemCatalog` delegate to the sources (V-05).
- **`WarrantyRepository` and `WarrantyService` (Developer 2) → `Warranty` and its subclasses (Developer 1).** The kinds of warranty are decided with tags and `instanceof ExtendedWarranty` (OCP). Developer 2 creates `WarrantyKind`, Developer 1 makes each warranty report its kind, and Developer 2 uses it in the repository and the service (V-06).

Each one is registered **once** in `consolidated-audit.md`, in the report of the role whose class holds the violation, with the part that touches another role's class written in its "Other role's code involved" field. The rules of the statement decide the split: the new abstraction is created by the owner of the violation, and every class is changed only by its owner.

Two cases looked like boundary findings and were not: the services that depend on their concrete repository (DIP) are corrected completely by the owner of each service, because the repository is passed by `Main` as the same object and `Main` does not need to change; and the type tags of the repositories were judged correct for the reason given in Q1.

## Q3. Relation with the GRASP audit of Requerimiento 7

*Written by Developer 1.*

The two audits look at the same code from different angles. GRASP asks *who* should own a responsibility; SOLID asks whether the design will *resist change*. We found both situations the question describes.

**GRASP corrections that also solved a SOLID problem:**

- **OCP in `CategoryDiscount`.** Before Requerimiento 7, `CategoryDiscount` decided the category of each product with `instanceof VideoGame/Console/Accessory`, so a new product type forced editing the promotion. The GRASP correction (Polymorphism, `Product.belongsToCategory`) moved that answer into each product, and the promotion is now closed for modification and open for new product types.
- **SRP in `ConsoleMenu`.** The GRASP High Cohesion correction split the 1026-line `ConsoleMenu` into one submenu per module and a shared `ConsoleInput`. That was also an SRP violation (seven reasons to change in one class), and it is no longer one.
- **DIP direction between layers.** `ReturnRepository` and `SalePersistence` depended on services, a dependency in the wrong direction. Since the GRASP correction they return records (`ReturnRecord`, `SaleRecord`) and no persistence class imports a service.

**SOLID violations in code that GRASP had already corrected:**

- **`ItemCatalog`** was created by the GRASP correction (Indirection) as the single intermediary between the use cases and the two catalogs. GRASP judged it correct because the type check was moved to one place, but from the SOLID point of view the check is still there: `ItemCatalog` decides with `instanceof Accessory` which service owns an item, so a third catalog would force editing it (OCP), and it depends on the concrete `ProductService` and `AccessoryService` (DIP).
- **Constructor injection.** GRASP made `ProductService`, `PersonService` and the other services receive their repository from `Main` (Low Coupling), but they receive the **concrete** repository class. The coupling to the file location was removed; the dependency on a concrete low-level class remains, which is what DIP asks to invert.

The lesson for the correction is that a GRASP fix is not undone by the SOLID fix: the SOLID corrections build on it (for example, `ItemCatalog` stays as the intermediary, but it depends on an abstraction implemented by each catalog), and no GRASP violation corrected in Requerimiento 7 may come back.

## Q4. Non-regression strategy

*Written by Developer 2.*

The SOLID correction is a refactor, so "nothing broke" has the same meaning as in Requerimiento 7: for the same inputs, every menu operation prints the same text and leaves the same content in the files of `data/`. The team reuses the verification built for Requerimiento 7, which already covers every operation of the menu:

1. **Compilation.** `mvn clean compile` (or *Build → Rebuild Project*) before every commit of code and before every Pull Request, so the shared branch always compiles.
2. **Scripted run of the whole menu.** The application runs from a scratch copy of the data files, never from the repository's own `data/`, with the fixed input script of 308 entries: products, people, accessories, promotions, sales (including every error case), returns, the monthly balance, warranties, an invalid option and exit. The data are the files of `data/` plus three seeded records (a sale of today with warranties and a previous return, and a sale of 45 days ago), so that the returns and warranty operations can be exercised on any day.
3. **Comparison.** The standard output and the final content of every data file are captured; the random identifiers become `<uuid>` and the dates of the run become `<TODAY>` and `<OLD>`, and the normalized result is compared with the baseline line by line with `diff`.

When it runs: on `develop` before the first correction (baseline), on the branch before every Pull Request of Phase 2, and at the end of the phase over all the operations.

Because Requerimiento 7 ended with the same normalized result as its own baseline, the baseline of Requerimiento 8 also has to match it. If it does not, the difference is investigated before any SOLID correction starts.

The evidence shown at the end is recorded in `docs/solid-refactor-log.md`: the commit of the baseline, the number of entries of the script and of normalized lines, and the result of the comparison, which must be **no differences**. Defects recorded as out of scope (in Requerimiento 7 or in this one) are part of the expected output, so a refactor that changes one of them by accident also shows up in the comparison.

## Q5. Order of the corrections and coordination

*Written by the Technical Lead.*

The order follows the dependencies between corrections, from the abstractions other corrections need to the classes that use them, and it keeps the same role order as Requerimiento 7:

1. **Developer 1 — model and products first.** Moving the receipt and certificate layouts out of `Return` and `Warranty` and the abstraction of the product repository do not depend on anyone else's correction, and the other roles build on them.
2. **Developer 2 — repositories and services of the extension modules.** It introduces the abstractions of its repositories, the item source used by `ItemCatalog`, the kinds of warranty and the read-only view of sales used by `ReturnService`. The parts that touch other roles' classes (`ProductService` implementing the item source and each warranty reporting its kind, by Developer 1; `SaleService` implementing the read-only view, by the Technical Lead) are done by their owners inside Developer 2's group.
3. **Technical Lead — sales, user interface and `Main` last.** `SaleService`, `SaleAssembler`, `ConsoleMenu` and `Main` use the services of the other roles, so they are corrected when those services already have their final API. The interfaces that `SaleService` and `SaleAssembler` need from `PersonService`, `PromotionService` and `WarrantyService` are created by the Technical Lead and implemented by Developer 2 inside this group. The main menu (`MenuOption`) is the last correction, because `Main` composes the final services and menus.

Coordination rules on the shared branch `feature/solid-refactor`:

- Only one task group works on the branch at a time. Each group starts with `git pull`, and it ends with the project compiling, its non-regression run and its Pull Request merged; only then does the next group start.
- New interfaces are introduced as purely additive commits (nobody uses them yet), then the implementing class declares `implements`, and only then the client changes the type of its dependency. Every commit compiles.
- When a change in one role's class would break another role's class, it is done in three steps: the owner adds the new code and keeps the old one `@Deprecated`; the owner of the other class adapts it inside the same task group; the owner removes the deprecated code.
- Commits that correct a violation mention its definitive identifier, for example `refactor(service): ... (V-07)`, and each correction is logged in `docs/solid-refactor-log.md` by its author, including the boundary commits made by another role.
