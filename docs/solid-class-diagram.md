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
