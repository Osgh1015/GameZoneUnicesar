# AI Usage Log - Technical Leader (Omar Sebastian Gamez Heredia)

Personal log of the use of Artificial Intelligence tools during the development of
GameZone Unicesar, as required by the Workshop (items 15-19) and by Requirement 5
(section 11). Each entry records the date, the tool, the phase and branch, the goal, the
query, a summary of the answer, the decision taken and the related commit.

Role: Technical Leader - sales module (`Sale`, `SalePersistence`, `SaleService`),
`ConsoleMenu`, `Main`, integration of the modules, README and review of Pull Requests.

## Workshop

### Entry 1

| Field | Content |
|---|---|
| Date | 2026-09-10 |
| Tool | Claude |
| Phase and branch | Workshop - `feature/sale-module` |
| Goal | Implement the sale class, its validation and persistence, and the console menu |
| Query (summary) | Asked how to validate a sale with at least one product and enough stock, how to save the sale with the ids of its client, seller and products, and how to organize the submenus |
| Answer | Validation in the service before updating the inventory, a `;` separated line with the product ids joined by `|`, and one method per submenu in the console menu |
| Decision | Implemented the attributes of `Sale`, the sale validation, the sale persistence and the menus of the three modules |
| Related commit | `2a635fc`, `fa91e0b`, `94da065`, `812c243` |

## Requirements 1-5

### Entry 2

| Field | Content |
|---|---|
| Date | 2026-09-16 |
| Tool | Claude |
| Phase and branch | Requirement 4 - `feature/warranty-module` (first version) |
| Goal | Build the first version of the warranty module |
| Query (summary) | Asked how to model basic and extended warranties with different durations and costs, and how to generate them when a sale is registered |
| Answer | Abstract `Warranty` with subclasses for 6 and 12 months, a repository with a type discriminator, a service with assignment and queries, and the extended warranty question in the sale menu |
| Decision | Implemented the module in ten commits. This version was later reverted and replaced in PR #17 (see the integration analysis) |
| Related commit | `9c455be`, `af35671`, `36d7b05`, `b3a91a0`, `1267f96`, `cd6fa9c`, `05b5e24`, `8f62058`, `5939f4c`, `0736371` |

### Entry 3

| Field | Content |
|---|---|
| Date | 2026-09-30 |
| Tool | Claude |
| Phase and branch | Requirement 2 - `feature/promotion-module` |
| Goal | Implement the promotions module and apply the best promotion to each sale |
| Query (summary) | Asked how to calculate a different discount for each promotion type without the rest of the system knowing the concrete types, and where to select the best promotion |
| Answer | Abstract `calculateDiscount(Sale)` in `Promotion`, three subclasses, a repository with a type discriminator, `findBestPromotionFor` in `PromotionService`, and the discount stored in the sale and shown in the receipt |
| Decision | Implemented the hierarchy, the repository, the service, the discount fields and receipt of `Sale`, the application in `registerSale`, its persistence and the submenu |
| Related commit | `5b853dd`, `a2bde08`, `251835c`, `ba9b4aa`, `8689812`, `9a228cf`, `eae0659`, `2ffc1c2`, `13ecea6` |

### Entry 4

| Field | Content |
|---|---|
| Date | 2026-10-02 |
| Tool | Claude |
| Phase and branch | Requirement 5 (A3) - `refactor/unified-sale-registration` |
| Goal | Unify the sale registration flow of Requirements 1, 2 and 4 |
| Query (summary) | Asked in which order to apply discount, warranties and inventory update so the total is subtotal - discount + extended warranties |
| Answer | The eight-step order of the statement, with the discount calculated only over the items and the extended warranty cost added after it |
| Decision | Reorganized `registerSale`, the structure of `Sale` and the sale menu in three commits |
| Related commit | `90935ff`, `7bdd39c`, `0760ae9` |

### Entry 5

| Field | Content |
|---|---|
| Date | 2026-10-02 |
| Tool | Claude |
| Phase and branch | Requirement 5 (A8) - `docs/integration-documentation` |
| Goal | Document the integration adjustments A1-A7 and the incident of PR #11 |
| Query (summary) | Asked for help structuring the integration analysis with the cause and the solution of each adjustment |
| Answer | A structure with the phases, the incident and its resolution, and one section per adjustment |
| Decision | Wrote the integration analysis following that structure |
| Related commit | `fe2d7df` |

## Review of the integrated system (after Requirement 5)

### Entry 6

| Field | Content |
|---|---|
| Date | 2026-10-20 |
| Tool | Claude (guide generated in the team session and shared by Developer 1) |
| Phase and branch | Correction of defects - `fix/integration-review-defects` |
| Goal | Correct the defects found in the review of the integrated system |
| Query | Requested the step-by-step guide with the code of the four corrections |
| Answer | Code for the abstract `getDescription` of `Person`, the removal of the hardcoded sellers in `Main`, the safe numeric input and error handling in `ConsoleMenu`, and the validation of returnable units in `ReturnService`, compiled and tested |
| Decision | Accepted and applied in four atomic commits, merged with PR #28 |
| Related commit | `52ac776`, `689d267`, `ee79e9d`, `0898dd3` (PR #28) |

## Requirement 6 - Validations and exceptions

### Entry 7

| Field | Content |
|---|---|
| Date | 2026-10-20 |
| Tool | Claude (guide generated in the team session and shared by Developer 1) |
| Phase and branch | Requirement 6 - analysis - `feature/exception-handling` |
| Goal | Answer the questions assigned to the Technical Leader in `docs/exception-analysis.md` |
| Query | Requested the draft of the exception analysis: introduction and sections 1 (checked or unchecked), 2 (why a custom hierarchy) and 5 (how `ConsoleMenu` catches the exceptions) |
| Answer | Draft in English justifying `RuntimeException` as the root, the design principles of the hierarchy and a single `try` with one `catch` per exception type in `start()` |
| Decision | Accepted the introduction and the three sections and uploaded them in one commit |
| Related commit | `20576c3` docs: answer exception hierarchy and error handling questions |

### Entry 8

| Field | Content |
|---|---|
| Date | 2026-10-21 |
| Tool | Claude (guide generated in the team session and shared by Developer 1) |
| Phase and branch | Requirement 6 - implementation and integration - `feature/exception-handling` |
| Goal | Refactor `ConsoleMenu` and `Main`, update the README, add the before/after evidence and review the Pull Requests of the developers |
| Query | Requested the detailed step-by-step of the Technical Leader with all the code, commands and commits |
| Answer | Checklist to review PR #29 and PR #30; four `catch` blocks with the messages of the statement plus a safety `catch` in `start()`; client and sale flows without local `catch` or `null` checks; `getSaleDetail` in the receipt option; a `try/catch` in `Main` for data errors at startup; README section and the filled regression table |
| Decision | Accepted, one commit per logical change, merged with PR #31 |
| Related commit | `aaa3f77`, `0fec53f`, `4f9727a`, `94164f5`, `1bb4f6a`, `510ec5d`, `f103b14` (PR #31) |

### Entry 9

| Field | Content |
|---|---|
| Date | 2026-10-21 |
| Tool | Claude (files generated in the team session and shared by Developer 1) |
| Phase and branch | Requirement 6 - documentation fix - `docs/fix-exception-docs` |
| Goal | Restore the markdown format of the exception analysis and complete the repository structure of the README |
| Query | Requested the corrected versions of `docs/exception-analysis.md` and `README.md` |
| Answer | Both files corrected, without the reference to a screenshots folder, and with the code block of section 5 marked as text so IntelliJ does not report false errors |
| Decision | Accepted and uploaded in two commits, merged with PR #32 |
| Related commit | `d4d474d`, `b7dc8a7` (PR #32) |