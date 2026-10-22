# AI Usage Log - Developer 1 (Santiago Andres Herrera Acuña)

Personal log of the use of Artificial Intelligence tools during the development of
GameZone Unicesar, as required by the Workshop (items 15-19) and by Requirement 5
(section 11). Each entry records the date, the tool, the phase and branch, the goal, the
query, a summary of the answer, the decision taken and the related commit.

Role: Developer 1 - products module, accessory, promotion and warranty hierarchies,
`Return` and `Sale.canBeReturned`, exceptions and validation packages.

## Workshop

### Entry 1

| Field | Content |
|---|---|
| Date | 2026-09-10 |
| Tool | Claude |
| Phase and branch | Workshop - `feature/product-module` |
| Goal | Implement file persistence and the business rules of the products module |
| Query (summary) | Asked how to save and load the products in a text file with a type discriminator, and how to validate and update stock in `ProductService` |
| Answer | Explanation of a `;` separated format with `VIDEOGAME`/`CONSOLE` as first field, `toLine`/`fromLine` methods, returning an empty list when the file does not exist, and stock methods in the service |
| Decision | Implemented `save` and `load` in `ProductRepository` and the registration, listing and stock methods in `ProductService`, keeping file access only in the persistence layer |
| Related commit | `c786c3f`, `4bf24c6`, `3bf6c5a`, `d4cb839` |

## Requirements 1-5

### Entry 2

| Field | Content |
|---|---|
| Date | 2026-09-16 |
| Tool | Claude |
| Phase and branch | Requirement 1 - `feature/accessory-module` |
| Goal | Design and implement the accessory hierarchy and its integration with sales |
| Query (summary) | Asked whether `Accessory` should extend `Product`, how to store the list of compatible consoles, and how a sale can include products and accessories in the same transaction |
| Answer | Recommendation to extend `Product` to reuse id, title, price and stock; a list of console ids as the compatibility attribute; a repository with a type discriminator; resolving each sale item in the product catalogue or in the accessory catalogue |
| Decision | Accepted the hierarchy `Accessory` -> `Controller`, `Cable`, `Memory`. Stored compatibility as console ids separated by `|` in `accessories.csv`. Integrated the accessories in the sale flow and in the menu |
| Related commit | `fa61406`, `e34e3b1`, `9a8a1a3`, `a9a02cd`, `ff1a96e`, `aa29bcd`, `73536aa` |

### Entry 3

| Field | Content |
|---|---|
| Date | 2026-09-30 |
| Tool | Claude |
| Phase and branch | Requirement 3 - `feature/return-module` |
| Goal | Implement the returns module with the 30-day rule, the ownership rule and the stock restoration |
| Query (summary) | Asked how to compute the days between two dates in Java, how to validate that the returned products belong to the sale, and how to restore the stock reusing existing methods |
| Answer | Use of `ChronoUnit.DAYS.between` in `Sale.canBeReturned`, validation of each product id against the products of the sale, `restoreStock` in the services, and a `ReturnRepository` that resolves sales and products when loading |
| Decision | Implemented `Return` with the refund and the receipt, `canBeReturned`, `ReturnRepository`, `ReturnService`, `restoreStock` in both catalogues and the returns submenu |
| Related commit | `5d2a0c4`, `06a6701`, `c047b33`, `1fd127b`, `775f3b1`, `9ee1e44`, `bc1e0eb`, `bacc16d`, `074d546` |

### Entry 4

| Field | Content |
|---|---|
| Date | 2026-10-01 |
| Tool | Claude |
| Phase and branch | Requirement 5 (A1) - `feature/accessory-category-discount` |
| Goal | Allow category promotions over accessories |
| Query (summary) | Asked how to add `ACCESSORY` as a target category without breaking the existing promotions |
| Answer | Recognize every `Accessory` instance as the new category in `CategoryDiscount`, validate the three allowed categories in `PromotionService` and add the option to the menu |
| Decision | Accepted. Added the category, the validation and the menu option in separate commits |
| Related commit | `fc46dfa`, `0c05f73`, `1221148` |

### Entry 5

| Field | Content |
|---|---|
| Date | 2026-10-02 |
| Tool | Claude |
| Phase and branch | Requirement 5 (phase 3) - `fix/revert-broken-warranty-merge` |
| Goal | Recover `develop` after the merge of PR #11 broke the compilation |
| Query (summary) | Asked how to undo a merge commit that was already pushed without using `git push --force` |
| Answer | Use `git revert -m 1 <merge commit>` in a new branch and integrate it with a Pull Request, then compare with the previous state using `git diff` |
| Decision | Applied the revert in its own branch (PR #15) and documented the incident in the integration analysis |
| Related commit | `bb072ab` |

### Entry 6

| Field | Content |
|---|---|
| Date | 2026-10-02 |
| Tool | Claude |
| Phase and branch | Requirement 4 - `feature/warranty-module` |
| Goal | Implement the warranty hierarchy, its persistence and the automatic warranties of each sale |
| Query (summary) | Asked how to calculate the end date inside the constructor using an abstract method, how to tell the two warranty types apart in the file, and how to generate the basic warranty only for consoles |
| Answer | Abstract `getDurationInMonths` called from the `Warranty` constructor, `BASIC`/`EXTENDED` as discriminator in `warranties.csv`, and `instanceof Console` in the sale flow to grant the basic warranty and the requested extended ones |
| Decision | Implemented `Warranty`, `BasicWarranty`, `ExtendedWarranty`, `WarrantyRepository`, `WarrantyService`, the generation in `registerSale` and the cost in the final total |
| Related commit | `c76d839`, `b967767`, `fee6cfd`, `e9ddc83`, `246c6e4`, `028fc94`, `5e81a6b` |

### Entry 7

| Field | Content |
|---|---|
| Date | 2026-10-02 |
| Tool | Claude |
| Phase and branch | Requirement 4 - `fix/warranty-cost-double-count` |
| Goal | Find why the final total of a sale with extended warranty was higher than expected |
| Query (summary) | Shared the receipt of a sale where the extended warranty cost appeared twice and asked for the cause |
| Answer | The cost was added both when assigning the warranty and when computing the final total; the 10% cost also needed rounding to two decimals |
| Decision | Removed the duplicated sum and rounded the cost in `ExtendedWarranty` |
| Related commit | `87e357a`, `2043097` |

### Entry 8

| Field | Content |
|---|---|
| Date | 2026-10-02 |
| Tool | Claude |
| Phase and branch | Requirement 5 (A5) - `fix/return-discounted-refund` |
| Goal | Refund returned items proportionally to the discount of the original sale |
| Query (summary) | Asked how to apply the formula price x (1 - discount / subtotal) per item and show it in the return receipt |
| Answer | A `calculatePaidRatio` method, the refund and the proportional discount per item, rounded to two decimals, and a receipt with list price, discount and refund of each item |
| Decision | Implemented the three methods in `Return` and updated the receipt, the analysis and the diagram |
| Related commit | `8ed966c`, `b211207`, `34445ca`, `27f6b3d` |

## Review of the integrated system (after Requirement 5)

### Entry 9

| Field | Content |
|---|---|
| Date | 2026-10-20 |
| Tool | Claude |
| Phase and branch | Review of the integrated system - `develop` (commit `06cff14`, PR #27) |
| Goal | Check the repository against the Workshop and Requirements 1-5 before continuing with the next requirements |
| Query | Asked the tool to review the project (zip and GitHub repository) taking into account that the Workshop was done first and Requirements 1 to 5 were added in order |
| Answer | Report of compliance and defects: `main` without the integrated system, a sale item could be returned more times than it was sold, the application closed with non-numeric input, sellers were duplicated (hardcoded in `Main` and loaded from `vendors.txt`), `Person` had no abstract method, missing AI logs and remote branches not deleted |
| Decision | Accepted the four code defects as the scope of a correction branch. The process observations (commit dates, phase order) were kept as information for the oral presentation because the history cannot be rewritten |
| Related commit | - (analysis only) |

### Entry 10

| Field | Content |
|---|---|
| Date | 2026-10-20 |
| Tool | Claude |
| Phase and branch | Correction of defects - `fix/integration-review-defects` |
| Goal | Get a step-by-step plan with the code to correct the four defects found in Entry 1 |
| Query | Asked for the step-by-step guide of the correction branch, with commands, code and commits |
| Answer | Guide with four atomic corrections: abstract `getDescription` in `Person`, sellers loaded only from `vendors.txt`, safe numeric input in `ConsoleMenu` and validation of returnable units in `ReturnService`. The tool compiled and tested the code before giving it |
| Decision | Accepted. The four commits were pushed by the Technical Leader in the correction branch and merged with PR #28 |
| Related commit | `52ac776`, `689d267`, `ee79e9d`, `0898dd3` (PR #28) |

## Requirement 6 - Validations and exceptions

### Entry 11

| Field | Content |
|---|---|
| Date | 2026-10-20 |
| Tool | Claude |
| Phase and branch | Requirement 6 - analysis and design - `feature/exception-handling` |
| Goal | Understand the distribution of work and prepare the updated class diagram |
| Query | Asked what each member had to do and in which order, and asked for the class diagram of the exception hierarchy and validators following the specification of Requirement 6 |
| Answer | Work order (Developer 1, Developer 2, Technical Leader) and a Mermaid diagram with `GameZoneException`, its four subclasses, the three validators and their relations with services, repositories and `ConsoleMenu`. The diagram was rendered by the tool to check that it had no syntax errors |
| Decision | Accepted the diagram. Kept the real class names of the project (`Client` instead of `Customer`, `SalePersistence` instead of `SaleRepository`) and documented the difference in a note |
| Related commit | `a6ffbab` docs: add exception handling class diagram |

### Entry 12

| Field | Content |
|---|---|
| Date | 2026-10-20 |
| Tool | Claude |
| Phase and branch | Requirement 6 - implementation - `feature/exception-handling` |
| Goal | Implement the exception hierarchy and the three validators |
| Query | Asked for the detailed step-by-step of Developer 1 with all the code and commands |
| Answer | Code of `GameZoneException` (abstract, unchecked, with error code), `ResourceNotFoundException`, `BusinessRuleException`, `InvalidDataException`, `PersistenceException`, `ProductValidator`, `PersonValidator` and `SaleValidator`, compiled and tested by the tool |
| Decision | Accepted, one commit per class. Accepted `List<? extends Person>` in `validateUniquePerson` because a `List<Client>` is not a `List<Person>`, and `initCause` in `PersistenceException` because the statement allows only two constructors in `GameZoneException` |
| Related commit | `588c96a`, `325aaa9`, `7e035e5`, `231e3f2`, `6276332`, `7c83f7c`, `a839983`, `0ae0cc5` (PR #29) |

### Entry 13

| Field | Content |
|---|---|
| Date | 2026-10-21 |
| Tool | Claude |
| Phase and branch | Requirement 6 - review - `feature/exception-handling` |
| Goal | Check the Pull Request of Developer 2 before its approval |
| Query | Asked the tool to verify in the repository whether the work of Developer 2 was correct |
| Answer | The six commits compiled one by one, the code matched the tested version except for blank lines, and the manual tests of the menu gave the expected results. Pending items: AI log of Developer 2 and the "before" column of the analysis table |
| Decision | Recommended the approval of the Pull Request and reported the pending items to the team |
| Related commit | - (review of PR #30) |

### Entry 14

| Field | Content |
|---|---|
| Date | 2026-10-21 |
| Tool | Claude |
| Phase and branch | Requirement 6 - final review - `develop` |
| Goal | Check the repository after the three Pull Requests of Requirement 6 |
| Query | Asked the tool to review the repository looking for errors |
| Answer | The code compiled and all the menu operations worked. Problems found in documentation: lost markdown format in `docs/exception-analysis.md`, missing paths in the repository structure of the README, missing AI logs, `main` without the system and merged branches not deleted |
| Decision | Accepted the corrected documentation files. Removed the reference to a screenshots folder because the requirement does not ask for it, and changed the code block of section 5 from `java` to `text` so IntelliJ does not report false errors |
| Related commit | `d4d474d`, `b7dc8a7` (PR #32) |
