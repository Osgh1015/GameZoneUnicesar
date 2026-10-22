# AI Usage Log - Developer 2 (Pablo Andres Amaya Pabon)

Personal log of the use of Artificial Intelligence tools during the development of
GameZone Unicesar, as required by the Workshop (items 15-19) and by Requirement 5
(section 11). Each entry records the date, the tool, the phase and branch, the goal, the
query, a summary of the answer, the decision taken and the related commit.

Role: Developer 2 - people module (`Person`, `Client`, `Seller`, `PersonRepository`,
`PersonService`) and the repositories and services of the extension modules.


## Requirements 1-5

### Entry 1

| Field | Content |
|---|---|
| Date | 2026-10-01 |
| Tool | Claude |
| Phase and branch | Requirement 4 - `feature/warranty-module` |
| Goal | Implement the queries and the cost calculation of `WarrantyService` |
| Query (summary) | Asked how to list the active warranties, the ones expiring in the next N days, and how to load warranties resolving the sale and the product |
| Answer | Filtering by `isActive(LocalDate.now())`, a date range for the expiring ones, the sum of additional costs per sale and resolving the references when loading the file |
| Decision | Implemented the extended warranty registration, the cost calculation, the listing and validity queries and the loading of the repository |
| Related commit | `17b21cb`, `61a4fc8`, `81e64fd`, `0eaebb9` |

### Entry 2

| Field | Content |
|---|---|
| Date | 2026-10-02 |
| Tool | Claude |
| Phase and branch | Requirement 5 (A2) - `fix/warranty-circular-dependency` |
| Goal | Remove the circular dependency SaleService -> WarrantyService -> WarrantyRepository -> SaleService |
| Query (summary) | Explained the cycle that prevented building the objects in `Main` and asked how to break it |
| Answer | Store only the sale and product ids in `WarrantyRepository`, resolve them in `WarrantyService`, and build `WarrantyService` before `SaleService` in `Main` |
| Decision | Applied the three changes and updated the warranty class diagram |
| Related commit | `952aa91`, `15f173f`, `a47377c` |

### Entry 3

| Field | Content |
|---|---|
| Date | 2026-10-02 |
| Tool | Claude |
| Phase and branch | Requirement 5 (A4) - `fix/return-accessory-stock` |
| Goal | Restore the stock of returned accessories |
| Query (summary) | Asked why the stock of an accessory did not increase after a return |
| Answer | The return only called `ProductService.restoreStock`; the accessory references had to be resolved when loading sales and returns and `AccessoryService` had to be injected |
| Decision | Resolved accessory references when loading sales and wired the accessory dependencies for the returns module |
| Related commit | `2a5135d`, `66058cf` |

### Entry 4

| Field | Content |
|---|---|
| Date | 2026-10-02 |
| Tool | Claude |
| Phase and branch | Requirement 5 (A6) - `fix/monthly-balance-report` |
| Goal | Show monthly sales, monthly returns and the net balance |
| Query (summary) | Asked how to separate the totals without changing the signature of `generateMonthlyBalance` |
| Answer | Add `calculateMonthlySales` and `calculateMonthlyReturns`, use the final total of each sale and keep `generateMonthlyBalance` as their difference |
| Decision | Implemented both methods and showed the three values in the menu |
| Related commit | `87658a1`, `e6a76e4` |

### Entry 5

| Field | Content |
|---|---|
| Date | 2026-10-02 |
| Tool | Claude |
| Phase and branch | Requirement 5 (A7) - `feature/return-warranty-cancellation` |
| Goal | Cancel the warranties of a returned console and refund the extended warranty cost |
| Query (summary) | Asked how to remove the warranties of a product in a sale and include the refundable cost in the return |
| Answer | A `cancelWarranties(productId, saleId)` method that returns the extended cost, a `warrantyRefund` field in `Return` stored in the file, and its use in `registerReturn` for each console |
| Decision | Implemented the four changes in separate commits and checked the receipt of the return |
| Related commit | `89c330c`, `f95a482`, `6446d65`, `1fe7389` |

## Requirement 6 - Validations and exceptions

### Entry 6

| Field | Content |
|---|---|
| Date | 2026-10-20 |
| Tool | Claude (guide generated in the team session and shared by Developer 1) |
| Phase and branch | Requirement 6 - analysis - `feature/exception-handling` |
| Goal | Answer the questions assigned to Developer 2 in `docs/exception-analysis.md` |
| Query | Requested the draft of the exception analysis, sections 3 (where the validators are called), 4 (preserving the external behavior) and 6 (project-specific decisions) |
| Answer | Draft in English explaining that validators are called from the services before any change of state, that they cannot access the persistence package, the regression table of the ten menu operations and the decisions about `SalePersistence`, `Client` and `findById` |
| Decision | Accepted the sections assigned to Developer 2 and uploaded them in a separate commit |
| Related commit | `45f1d6a` docs: answer validation layer and behavior preservation questions |

### Entry 7

| Field | Content |
|---|---|
| Date | 2026-10-21 |
| Tool | Claude (guide generated in the team session and shared by Developer 1) |
| Phase and branch | Requirement 6 - implementation - `feature/exception-handling` |
| Goal | Refactor the three repositories and the three services to use the custom exceptions and the validators |
| Query | Requested the detailed step-by-step of Developer 2 with all the code, commands and commits |
| Answer | Guide with the refactored `ProductRepository`, `PersonRepository` and `SalePersistence` (`IOException` wrapped into `PersistenceException` with file and operation) and `ProductService`, `PersonService` and `SaleService` (validations delegated to the validators, typed exceptions instead of `null`). The tool compiled the code and ran twelve manual test cases |
| Decision | Accepted, one commit per class. Kept `findById` returning `null` because other services depend on it, and kept an empty list when a data file does not exist to preserve the previous behavior |
| Related commit | `384650d`, `ef7285d`, `78d96a2`, `1d62653`, `dcc043a`, `a1e07af` (PR #30) |
