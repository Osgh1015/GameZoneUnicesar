# Integration Analysis - GameZone Unicesar

Requirement 5 integrates the four extensions of the system (accessories, promotions,
warranties and returns) into a single application built on the layered architecture of the
workshop (`ui → service → persistence → model`). This document describes the integration
adjustments A1-A7, the cause of each one and the solution applied, together with the
incident that happened during phase 3 and how it was resolved.

## 1. Integration phases

| Phase | Module | Module branch | Adjustments |
|---|---|---|---|
| 1 | Accessories | `feature/accessory-module` | - |
| 2 | Promotions | `feature/promotion-module` | A1 |
| 3 | Warranties | `feature/warranty-module` | A2, A3 |
| 4 | Returns | `feature/return-module` | A4, A5, A6, A7 |
| 5 | Closing | - | A8, A9 |

Every branch was created from `develop` and integrated back into `develop` through a Pull
Request reviewed by a member other than the author.

## 2. Incident during phase 3: broken warranty merge (PR #11)

### What happened
The first warranty branch (`feature/warranty-module`) had been created weeks before, when
`develop` did not contain the promotions and returns modules yet. Before opening the Pull
Request, `origin/develop` was merged into that old branch (commit `d3a9807`), and the
conflicts in `Sale`, `SaleService`, `SalePersistence`, `ConsoleMenu`, `Main` and `README.md`
were resolved by **keeping the outdated versions of the branch**. When PR #11 was merged:

- `develop` stopped compiling (11 errors: `ConsoleMenu` called methods that the new
  `WarrantyService` no longer had).
- The promotion integration was lost from `Sale` (discount fields, `calculateFinalTotal`,
  receipt with discount) and `Sale.canBeReturned` disappeared.
- The returns and promotions submenus and their wiring in `Main` were removed.

### How it was resolved
1. **Revert instead of rewriting history.** PR #15 (`fix/revert-broken-warranty-merge`)
   reverted the merge commit with `git revert -m 1 18b7433`. `develop` returned exactly to
   the state after PR #14 (`git diff 646632f` showed no differences) without using
   `push --force`, so the history keeps both the mistake and its correction.
2. **Work that had to be redone.** A reverted merge cannot simply be merged again, because
   Git considers those commits already integrated. The warranty module (model by the
   technical leader, repository and service by developer 2) was re-applied on top of the
   current `develop` in a new `feature/warranty-module` branch (PR #17). The first A2 branch,
   built over the broken `develop`, had to be deleted and redone (PR #20): merging it as it
   was produced three conflicts (`Main`, `WarrantyService` and the warranty diagram) and
   would have removed promotions and returns from `Main` again.
3. **Follow-up defect.** PR #17 carried an old commit that added `warrantyCost` inside
   `Sale.calculateTotal()`, while `calculateFinalTotal()` also added it, so the extended
   warranty was charged twice (final total 643.97 instead of 593.97). PR #18
   (`fix/warranty-cost-double-count`) kept `calculateTotal()` as the subtotal of the items
   and rounded the extended warranty cost to two decimals.

### Lessons applied in the rest of the integration
- Create every branch from an updated `develop` (`git checkout develop`, `git pull`)
  immediately before working on it; do not reuse old branches.
- Never resolve a conflict by keeping one side blindly: keep both the existing code and
  the new code, and compile before committing the merge.
- Before approving a Pull Request, the reviewer compiles the branch and runs the main
  scenario; a Pull Request that deletes hundreds of lines in unrelated files is a warning.
- When a merged change breaks `develop`, revert it with a reviewed Pull Request; never use
  `push --force`.
- Commits copied from an old branch are checked line by line against the current design.

## 3. Integration adjustments

### A1 - Category discount for accessories
- **Type / branch:** feature - `feature/accessory-category-discount` (PR #16), developer 1.
- **Problem:** `CategoryDiscount` only accepted `"VIDEOGAME"` and `"CONSOLE"`, so the store
  could not launch promotions on accessories once the accessory module was integrated.
- **Cause:** the promotions requirement was designed before accessories existed.
- **Solution:** `CategoryDiscount` accepts `"ACCESSORY"` and recognizes every instance of
  `Accessory` (`Controller`, `Cable`, `Memory`) with `instanceof`;
  `PromotionService.registerCategoryDiscount` validates the three allowed categories;
  `ConsoleMenu` offers *Videojuegos / Consolas / Accesorios*; `data/promotions.csv` includes
  a 25% accessory promotion. While testing, the data file was found to be named
  `data/promotions` while `Main` loaded `data/promotions.csv`, so no preloaded promotion was
  ever loaded; the file was renamed in the same branch.

### A2 - Circular dependency in the warranty module
- **Type / branch:** fix - `fix/warranty-circular-dependency` (PR #20), developer 2.
- **Problem:** with the Requirement 4 design the repository needed other components to
  rebuild the `Sale` and `Product` of each warranty while loading, creating the cycle
  `SaleService -> WarrantyService -> WarrantyRepository -> SaleService` and a dependency
  from the persistence layer to the service layer, which prevents constructor injection in
  `Main`.
- **Cause:** `WarrantyRepository` received `ProductService`, `AccessoryService`,
  `SalePersistence` and `PersonService` to resolve references.
- **Solution:** `WarrantyRepository` only receives the file path and stores/loads ids
  (`type;id;productId;saleId;startDate`) as `WarrantyRecord`. `WarrantyService` receives
  `WarrantyRepository`, `SalePersistence`, `ProductService` and `PersonService` and resolves
  the references in `loadWarranties()`. `Main` builds the warranty module before
  `SaleService` and injects it. The file format did not change.

### A3 - Unified sale registration flow
- **Type / branch:** refactor - `refactor/unified-sale-registration` (PR #22), technical leader.
- **Problem:** Requirements 1, 2 and 4 modified `SaleService.registerSale` independently;
  once integrated, the order of the operations determines the result (for example, whether
  the discount is applied before or after the extended warranty cost).
- **Cause:** each requirement added its logic inside the same long method without a defined
  sequence.
- **Solution:** `registerSale` follows eight explicit steps, each delegated to a private
  method: (1) at least one item, (2) resolve items as products or accessories and validate
  stock (`resolveItems`), (3) create the sale and its subtotal (`createSale`), (4) best
  promotion over the items only (`applyBestPromotion`), (5) basic warranty for every console
  and requested extended warranties (`generateWarranties`), (6) final total =
  subtotal - discount + extended warranty cost (`Sale.calculateFinalTotal`), (7) inventory
  through `ProductService` or `AccessoryService` (`updateInventory`), (8) persistence. The
  warranties are stored by `WarrantyService` when they are granted in step 5, so
  `WarrantyService` was not modified while A2 was in progress. `Sale.generateReceipt` shows
  the type of each item, subtotal, discount with the promotion name, extended warranty cost
  and final total; the sales submenu lists the products and accessories with stock and asks
  for extended warranty for each console.

### A4 - Return of accessories
- **Type / branch:** fix - `fix/return-accessory-stock` (PR #23), developer 2.
- **Problem:** the returns requirement restored stock only through
  `ProductService.restoreStock`, so the stock of a returned accessory was not restored.
- **Cause / state found:** `ReturnService` already delegated to
  `AccessoryService.restoreStock` according to the item type, `AccessoryService.restoreStock`
  already existed and `ReturnRepository` already resolved accessories. The remaining defect
  was in `SalePersistence`, which resolved stored items only through `ProductService`: after
  restarting the application the accessories disappeared from the sales, and returning one
  failed with "El producto A001 no pertenece a la venta indicada".
- **Solution:** `SalePersistence` receives `AccessoryService` and resolves each stored item
  as a product or an accessory; `Main` injects it. After a restart, the console and the
  accessory can be returned and both stocks are restored.

### A5 - Refund of sales with a discount
- **Type / branch:** fix - `fix/return-discounted-refund` (PR #24), developer 1.
- **Problem:** `Return.calculateRefundAmount` added the list prices of the returned items, so
  a client could receive more than what was paid when the sale had a promotion.
- **Cause:** the refund ignored `Sale.discountAmount`, added by the promotions module after
  the returns module was designed.
- **Solution:** each item is refunded as `price × (1 − discount / subtotal)`, where the
  subtotal is the sum of the item prices (extended warranties excluded, since the discount
  is calculated only over the items). New methods `calculatePaidRatio`,
  `calculateItemDiscount` and `calculateItemRefund`; the receipt shows list price,
  proportional discount and refund per item. Example: returning the console and the
  controller of a 639.97 sale with 15% off refunds 424.99 + 59.49 = 484.48 instead of 569.98.

### A6 - Monthly balance report
- **Type / branch:** fix - `fix/monthly-balance-report` (PR #25), developer 2.
- **Problem:** the returns requirement asked for total sales, total returns and net balance,
  but `generateMonthlyBalance` only returned the net balance.
- **Cause:** the three values were calculated inside one method and only the difference was
  exposed.
- **Solution:** `ReturnService.calculateMonthlySales` (final total of each sale, with
  discounts and extended warranties) and `calculateMonthlyReturns` (refund of each return);
  `generateMonthlyBalance` keeps its signature and returns their difference. The menu option
  shows the three values and validates month and year.

### A7 - Warranty cancellation when a console is returned
- **Type / branch:** feature - `feature/return-warranty-cancellation`, developer 2. It started
  after A5 was merged, since both modify the refund calculation.
- **Problem:** no requirement defined what happens to the warranty of a returned console; a
  returned console could keep a valid warranty.
- **Solution:** `WarrantyService.cancelWarranties(productId, saleId)` removes the warranties
  of the returned console in that sale and returns the refundable cost (zero for the basic
  warranty, the additional cost for the extended one). `ReturnService.registerReturn`
  invokes it for every returned console and passes the value to `Return`, which adds it to
  the refund and shows it in the receipt. `ReturnRepository` stores that value so the refund
  is the same after restarting.

## 4. Verification of the integrated system

Scenario of the defense, restarting the application between steps:

| Step | Result |
|---|---|
| Sale: PlayStation 5 (extended warranty) + The Legend of Zelda + DualSense | Subtotal 639.97, discount "Descuento general 15%" -96.00, extended warranties +50.00, final total 593.97 |
| Warranties of the sale | Basic (6 months, no cost) and extended (12 months, 50.00) for the console; none for the game and the accessory |
| Partial return of the console and the accessory | 424.99 + 59.49 + cancelled extended warranty 50.00 = 534.48 |
| After the return | Stock of the console and the accessory restored; the console has no warranties |
| Monthly balance | Sales 593.97, returns 534.48, net 59.49 (the price paid for the game that was kept) |