# Accessory Module — Analysis

## Q1. Should accessories extend Product or form an independent hierarchy?

Accessories must extend the existing `Product` hierarchy. An accessory is, in business
terms, a sellable item with an identifier, a title, a price and an available quantity —
exactly the attributes `Product` already declares. Creating an independent hierarchy would
force us to duplicate those four attributes, their getters and setters, and the stock
handling logic, and it would also break the sales module: `Sale` holds a `List<Product>`,
so an accessory that is not a `Product` could not be sold in the same transaction as a
console or a video game without rewriting `Sale` and `SaleService`.

By declaring `Accessory extends Product`, every accessory is automatically accepted
anywhere a `Product` is expected, which is the requirement stated in the specification: a
sale may mix video games, consoles and accessories freely. This is code reuse through
inheritance, and it keeps the model coherent: the system has one concept of "something the
store sells", with specialisations below it.

## Q2. Common versus type-specific attributes

The attributes inherited from `Product` and shared by all three accessory types are `id`,
`title`, `price` and `quantity`. In addition, `Accessory` itself declares one attribute
common to all accessories but not to other products: `compatibleConsoleIds`, the list of
consoles the accessory works with.

The type-specific attributes are:

- `Controller`: `connectionType` (wireless or wired).
- `Cable`: `lengthInMeters` (double) and `connectorType` (String).
- `Memory`: `capacityInGb` (int) and `memoryType` (String).

This distinction is reflected in a three-level hierarchy. `Product` (abstract) holds what
every sellable item shares; `Accessory` (abstract) extends it and adds what every accessory
shares; and `Controller`, `Cable` and `Memory` are the concrete classes that add their own
particular attributes. Both `Product` and `Accessory` are abstract because neither "a
generic product" nor "a generic accessory" is something the store actually sells — only the
concrete types are.

## Q3. Representing compatibility between an accessory and a console

Compatibility is a **many-to-many association**: one accessory can be compatible with
several consoles, and one console can be compatible with many accessories. In the design we
model it as an attribute of the **accessory only**, not of both sides. `Accessory` holds a
`List<String>` of compatible console ids, and `Console` is left untouched.

Storing it on a single side avoids having to keep two lists synchronised — if the same fact
were written in both classes, an update on one side could leave the other stale, and there
would be no single source of truth. Since the queries the system actually needs run in one
direction ("which accessories are compatible with this console?"), one list on the accessory
side answers them by filtering, and no reverse list is required.

In persistence, the list is stored as a single field inside each accessory's row, with its
ids separated by an internal separator (`|`) so that one row still maps to one accessory.
This keeps the file format flat and avoids needing a second relationship file.

## Q4. Modifications needed in SaleService

The required changes are **additive**, not rewrites. Because `Accessory` extends `Product`,
`registerSale` can keep receiving a list of product ids and keep validating stock and
computing the total through the inherited `getQuantity()` and `getPrice()` — that logic
works unchanged for accessories, which is precisely the benefit of Q1's decision.

What must change is item lookup and inventory update. `SaleService` currently resolves ids
only through `ProductService`; it now needs an `AccessoryService` dependency as well, so
that when an id is not found among products it is resolved among accessories. Likewise, when
decrementing stock after a sale, `SaleService` must delegate to the service that owns the
item: `ProductService` for video games and consoles, `AccessoryService` for accessories.
Existing behaviour for video games and consoles is untouched, so nothing that already worked
breaks.

## Q5. Layer placement of the new classes

The new classes are distributed across three of the four layers, following each layer's
responsibility:

- `Accessory`, `Controller`, `Cable` and `Memory` go in **model** (`com.gamezone.model`).
  They hold data and derived descriptions only, and contain no file access whatsoever, as
  the workshop rules require.
- `AccessoryRepository` goes in **persistence** (`com.gamezone.persistence`). It is the only
  class allowed to read and write `data/accessories.csv`.
- `AccessoryService` goes in **service** (`com.gamezone.service`). It holds the business
  rules — registration, filtering by type, compatibility queries, stock updates — and it is
  the only entry point the UI is allowed to use.
- The menu extensions go in **ui** (`com.gamezone.ui.ConsoleMenu`), which never touches
  persistence directly.

This respects the established dependency direction `ui → service → persistence → model`: each
layer depends only on the ones below it, and never the other way around.
