# Promotion Module - Design Analysis

## 1. Class hierarchy and polymorphism
The three promotions (percentage, category and bulk purchase) share the
same attributes (id, name, start date, end date) and the same behaviors
(`isActive`, `calculateDiscount`), but each one computes the discount with
a different rule. This is reflected in an **inheritance hierarchy**: the
abstract class `Promotion` holds the common state and behavior, and
`PercentageDiscount`, `CategoryDiscount` and `BulkPurchaseDiscount` extend
it and add only their own attributes.

The mechanism that lets each type calculate its discount differently is
**polymorphism (dynamic dispatch)**. `PromotionService` works only with
the `Promotion` type and calls `promotion.calculateDiscount(sale)`; the JVM
runs the implementation of the concrete class at runtime. The rest of the
system never needs to know the concrete types, so a new promotion type can
be added without modifying `SaleService` or the menu (open/closed principle).

## 2. Abstract method in the base class
`Promotion` cannot implement the calculation because each type has a
different rule. The method is declared in the base class as
`public abstract double calculateDiscount(Sale sale);`, without a body.
The Java compiler guarantees that every concrete subclass implements it:
a class that extends `Promotion` and does not override
`calculateDiscount` does not compile unless it is also declared abstract.
Using `@Override` in each subclass adds a second check, because the compiler
verifies that the signature really matches the parent method.

## 3. Where the "best promotion" selection lives
The selection logic is in `PromotionService.findBestPromotionFor(Sale)`,
in the **service layer**, because choosing the promotion with the highest
discount is a business rule. The layered architecture is
`ui -> service -> persistence -> model`, where each layer has one job:
the model holds data and intrinsic calculations, the persistence layer
reads and writes files, the service layer enforces business rules, and the
UI only interacts with the user.

It must NOT be in `Sale` because the model layer must not know about the
catalog of promotions: a sale cannot compare promotions without depending
on a service or repository, and that would invert the dependency direction.
It must NOT be in the console menu because the UI would then contain
business rules that could not be reused or tested without the console, and
they would be duplicated if another interface was added.

## 4. Changes in Sale and the receipt
`Sale` gets two new private attributes, `appliedPromotionName` and
`discountAmount`, with getters and setters, plus `calculateFinalTotal()`
(subtotal minus discount) and `generateReceipt()`, which prints the
subtotal, the discount with the promotion name, and the final total. The
receipt method did not exist before, so it is added rather than modified.

The changes are additive and do not break existing behavior:
`calculateTotal()` still returns the subtotal, a new sale starts with
`discountAmount = 0` and no promotion name, and `SalePersistence` writes two
extra fields at the end of each line but still loads old lines with only
five fields. A sale with no applicable promotion prints a zero discount and
its final total equals the subtotal. The monthly balance in `ReturnService`
now sums `calculateFinalTotal()`, which gives the same value as before for
sales without discount.

## 5. Where the validity check is done
It is done in **both**, with different responsibilities.
`Promotion.isActive(LocalDate)` (model) contains the rule itself: a date is
valid when it is within the start and end dates, inclusive. It is an
intrinsic property of the promotion and needs no other object.
`PromotionService` (service) decides **which date to evaluate** and applies
the rule to the whole collection: the current date in
`listActivePromotions()` and the sale date in `findBestPromotionFor(sale)`.
This keeps the rule in one place (no duplication) while the service
orchestrates when and over which promotions it is used.