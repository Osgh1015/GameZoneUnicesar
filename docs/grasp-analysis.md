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

*To be written by the Technical Lead.*

## Q3. Fragments that break more than one pattern

*Written by Developer 1.*

Yes. The clearest case in Developer 1's scope is `CategoryDiscount.belongsToCategory` (developer1-audit V-03). The same fragment breaks **Polymorphism** — it asks each product for its concrete class with `instanceof` instead of letting each product answer —, **Low Coupling** — the promotion depends on three concrete classes of the product hierarchy — and **Protected Variations** — the mapping between category names and product classes is hard-coded in the promotion, so a new product type is an unprotected variation point. A second case is `ProductService` creating its own `ProductRepository` with a fixed file (developer1-audit V-02), which breaks Low Coupling and Protected Variations at the same time.

We recorded each fragment **once**, with a single ID, under the pattern that best describes its root cause (Polymorphism for V-03, Low Coupling for V-02), and added a short cross-reference in the sections of the other patterns that points to the same ID. The summary table lists the ID under every pattern it affects, so the number of distinct violations is not inflated.

The pattern that guides the correction is the one whose solution removes the root cause. For V-03 that is Polymorphism: once each product answers `belongsToCategory`, the promotion no longer depends on the concrete classes and the variation point is protected, so one change fixes the three patterns. For V-02 it is Low Coupling: once the repository is injected, the file location is also decided outside the service.

## Q4. Non-regression strategy

*To be written by Developer 2.*

## Q5. Order of the corrections and coordination

*To be written by the Technical Lead.*
