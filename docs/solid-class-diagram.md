# SOLID Class Diagram — Requerimiento 8

Class diagrams of the parts of the system affected by the SOLID corrections. Only the classes, methods and dependencies that take part in a violation of `docs/solid-audit/consolidated-audit.md` are drawn; the rest of the system is described in the diagrams of the previous requirements and in `docs/grasp-class-diagram.md`.

## Before the correction

State of `develop` at the audited commit. The notes name the violations.

```mermaid
classDiagram
    direction LR

    class Return {
        +generateReturnReceipt() String
    }
    class Warranty {
        <<abstract>>
        +generateWarrantyCertificate() String
    }
    class Sale {
        +generateReceipt() String
    }
    note for Return "V-01 SRP: lays out its receipt"
    note for Warranty "V-02 SRP: lays out its certificate"
    note for Sale "V-09 SRP: lays out its receipt"

    class ProductRepository
    class PersonRepository
    class AccessoryRepository
    class PromotionRepository
    class ReturnRepository
    class WarrantyRepository
    class SalePersistence

    class ProductService
    class PersonService
    class AccessoryService
    class PromotionService
    class ReturnService {
        +calculateMonthlySales(month: int, year: int) double
        +calculateMonthlyReturns(month: int, year: int) double
        +generateMonthlyBalance(month: int, year: int) double
    }
    class WarrantyService
    class ItemCatalog {
        -productService: ProductService
        -accessoryService: AccessoryService
    }
    class SaleService
    class SaleAssembler

    ProductService --> ProductRepository : V-03 DIP
    PersonService --> PersonRepository : V-04 DIP
    AccessoryService --> AccessoryRepository : V-04 DIP
    PromotionService --> PromotionRepository : V-04 DIP
    ReturnService --> ReturnRepository : V-04 DIP
    WarrantyService --> WarrantyRepository : V-04 DIP
    ItemCatalog --> ProductService : V-05
    ItemCatalog --> AccessoryService : V-05
    ReturnService --> SaleService : V-07 ISP
    SaleService --> SalePersistence : V-10 DIP
    SaleAssembler --> SalePersistence : V-10 DIP
    SaleService --> PersonService : V-11 ISP
    SaleService --> PromotionService : V-11 ISP
    SaleService --> WarrantyService : V-11 ISP
    SaleAssembler --> PersonService : V-11 ISP
    note for ItemCatalog "V-05 OCP: instanceof Accessory"
    note for ReturnService "V-08 SRP: monthly balance"

    class ConsoleMenu {
        +ConsoleMenu(seven services)
        +start() void
    }
    class ProductMenu
    class PersonMenu
    class SaleMenu
    class AccessoryMenu
    class ReturnMenu
    class PromotionMenu
    class WarrantyMenu
    ConsoleMenu *-- ProductMenu
    ConsoleMenu *-- PersonMenu
    ConsoleMenu *-- SaleMenu
    ConsoleMenu *-- AccessoryMenu
    ConsoleMenu *-- ReturnMenu
    ConsoleMenu *-- PromotionMenu
    ConsoleMenu *-- WarrantyMenu
    note for ConsoleMenu "V-12 OCP: creates, prints and switches on every module"
```

Violations that are not visible as a relation:

| ID | Where |
|---|---|
| V-06 | `WarrantyRepository` writes and reads the kind with tags and `instanceof ExtendedWarranty`; `WarrantyService` chooses and cancels by concrete class |
| V-12 | `ConsoleMenu.start()` prints the seven options one by one and dispatches them with a `switch` |

## After the correction

State of `develop` after the Pull Requests of Phase 2. New interfaces: `ProductStore`, `PersonStore`, `AccessoryStore`, `PromotionStore`, `ReturnStore`, `WarrantyStore`, `SaleStore`, `ItemSource`, `SaleLookup`, `PersonLookup`, `PromotionSelector`, `WarrantyAssigner`, `MenuOption`; new classes: `ReturnReceipt`, `WarrantyCertificate`, `SaleReceipt`, `MonthlyBalanceService`; new enum: `WarrantyKind`. The repositories implement their store with the same names (`ProductRepository` implements `ProductStore`, and so on).

### Model and persistence

```mermaid
classDiagram
    direction LR

    class Return {
        +generateReturnReceipt() String
    }
    class ReturnReceipt {
        +format() String
    }
    class Warranty {
        <<abstract>>
        +generateWarrantyCertificate() String
        +getKind()* WarrantyKind
    }
    class WarrantyCertificate {
        +format() String
    }
    class WarrantyKind {
        <<enumeration>>
        BASIC
        EXTENDED
        +create(id, product, sale, startDate) Warranty
        +isPreferredCoverage() boolean
    }
    class Sale {
        +generateReceipt() String
    }
    class SaleReceipt {
        +format() String
    }
    Return ..> ReturnReceipt : V-01
    Warranty ..> WarrantyCertificate : V-02
    Warranty ..> WarrantyKind : V-06
    Sale ..> SaleReceipt : V-09

    class ProductStore {
        <<interface>>
        +save(products) void
        +load() List~Product~
    }
    class SaleStore {
        <<interface>>
        +saveAll(sales) void
        +loadAll() List~SaleRecord~
        +damagedLine(lineNumber, cause) PersistenceException
    }
    class ProductRepository
    class SalePersistence
    ProductRepository ..|> ProductStore : V-03
    SalePersistence ..|> SaleStore : V-10
    note for ProductStore "PersonStore, AccessoryStore, PromotionStore, ReturnStore and WarrantyStore follow the same shape (V-04)"
```

### Services and user interface

```mermaid
classDiagram
    direction LR

    class ItemSource {
        <<interface>>
        +findItem(id) Product
        +owns(item) boolean
        +takeStock(item, amount) void
        +returnStock(item, amount) void
    }
    class SaleLookup {
        <<interface>>
        +findById(saleId) Sale
        +getSalesHistory() List~Sale~
    }
    class PersonLookup {
        <<interface>>
        +findClientById(id) Client
        +findSellerById(id) Seller
    }
    class PromotionSelector {
        <<interface>>
        +findBestPromotionFor(sale) Promotion
    }
    class WarrantyAssigner {
        <<interface>>
        +assignBasicWarranty(product, sale, startDate) BasicWarranty
        +assignExtendedWarranty(product, sale, startDate) ExtendedWarranty
    }
    class ProductService
    class AccessoryService
    class PersonService
    class PromotionService
    class WarrantyService
    class ItemCatalog {
        -sources: List~ItemSource~
    }
    class SaleService
    class SaleAssembler
    class ReturnService
    class MonthlyBalanceService {
        +calculateMonthlySales(month, year) double
        +calculateMonthlyReturns(month, year) double
        +generateMonthlyBalance(month, year) double
    }

    ProductService ..|> ItemSource : V-05
    AccessoryService ..|> ItemSource : V-05
    ItemCatalog --> ItemSource : V-05
    SaleService ..|> SaleLookup : V-07
    ReturnService --> SaleLookup : V-07
    MonthlyBalanceService --> SaleLookup : V-08
    MonthlyBalanceService --> ReturnService : V-08
    PersonService ..|> PersonLookup : V-11
    PromotionService ..|> PromotionSelector : V-11
    WarrantyService ..|> WarrantyAssigner : V-11
    SaleService --> PersonLookup : V-11
    SaleService --> PromotionSelector : V-11
    SaleService --> WarrantyAssigner : V-11
    SaleAssembler --> PersonLookup : V-11

    class MenuOption {
        <<interface>>
        +getTitle() String
        +show() void
    }
    class ConsoleMenu {
        -options: List~MenuOption~
        +ConsoleMenu(userInput, options)
        +start() void
    }
    class Main
    ConsoleMenu --> MenuOption : V-12
    Main ..> ConsoleMenu : composes the options
    note for MenuOption "Implemented by ProductMenu, PersonMenu, SaleMenu, AccessoryMenu, ReturnMenu, PromotionMenu and WarrantyMenu (V-12)"
```