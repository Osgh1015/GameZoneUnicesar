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

*To be written by the Technical Lead.*

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

*To be written by the Technical Lead.*
