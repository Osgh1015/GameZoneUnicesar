# Return Module - Design Analysis

## 1. Relationship between Return and Sale
The relationship is an **association**: `Return` holds a reference to `Sale`,
but `Return` is not a specialization of `Sale` (no inheritance), it is not
part of `Sale`'s internal structure (no composition/aggregation from
`Sale`'s side), and `Sale` has no knowledge of `Return` at all. The
dependency is one-directional: only `Return` references `Sale`.

## 2. Representation of partially returned products
The `returnedProducts` attribute in `Return` is a `List<Product>` containing
only the subset of products the customer is actually returning, taken from
`sale.getProducts()` — not the full list of products in the original sale.
This allows the returned list to be smaller than the original sale's list.

## 3. Layer for the 30-day validation
The check is split across two layers deliberately: `Sale.canBeReturned()`
(model layer) computes the raw day difference using
`ChronoUnit.DAYS.between(date, LocalDate.now())`, because that calculation
is an intrinsic property of the sale itself. The actual business decision
of *rejecting* a return based on that result lives in
`ReturnService.registerReturn` (service layer), since deciding whether an
operation is allowed to proceed is a business rule, not something the model
layer should enforce on its own.

## 4. Reused stock update method
`ReturnService` invokes `ProductService.restoreStock` (for regular products)
or `AccessoryService.restoreStock` (for accessories), two additive methods
placed in the existing service classes rather than duplicated logic inside
`ReturnService`. This mirrors how `SaleService.registerSale` already
delegates stock changes to the same two services. Reusing them keeps stock
management consistent across both modules and avoids having two different
places where "how stock changes" could diverge.

## 5. Monthly balance report location
`generateMonthlyBalance` lives in `ReturnService`, because it needs data
from both sales and returns, and `ReturnService` already depends on
`SaleService` (through constructor injection) to validate the original sale
during registration. Placing the report here avoids creating a new,
unnecessary service just for one report, and keeps the dependency direction
consistent with the layered architecture.
