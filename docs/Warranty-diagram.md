# Warranty Class Diagram

Updated class diagram of the warranty module and its integration with the classes that already existed in the system (`Sale`, `Product`, `Console`, `SaleService`, `ConsoleMenu`).

```mermaid
classDiagram
    namespace Model {
        class Warranty {
            <<abstract>>
            -id: String
            -product: Product
            -sale: Sale
            -startDate: LocalDate
            -endDate: LocalDate
            +Warranty(id: String, product: Product, sale: Sale, startDate: LocalDate)
            +getId() String
            +getProduct() Product
            +getSale() Sale
            +getStartDate() LocalDate
            +getEndDate() LocalDate
            +getDurationInMonths()* int
            +getWarrantyType()* String
            +getAdditionalCost()* double
            +isActive(date: LocalDate) boolean
            +generateWarrantyCertificate() String
        }

        class BasicWarranty {
            -DURATION_IN_MONTHS: int$
            +BasicWarranty(id: String, product: Product, sale: Sale, startDate: LocalDate)
            +getDurationInMonths() int
            +getWarrantyType() String
            +getAdditionalCost() double
        }

        class ExtendedWarranty {
            -DURATION_IN_MONTHS: int$
            -COST_RATE: double$
            +ExtendedWarranty(id: String, product: Product, sale: Sale, startDate: LocalDate)
            +getDurationInMonths() int
            +getWarrantyType() String
            +getAdditionalCost() double
        }

        class Product {
            <<abstract>>
            -id: String
            -title: String
            -price: double
            -quantity: int
            +getDescription()* String
        }

        class Console {
            -brand: String
            -model: String
            -generation: int
            +getDescription() String
        }

        class Sale {
            -id: String
            -date: LocalDate
            -client: Client
            -seller: Seller
            -products: List~Product~
            -warrantyCost: double
            +getId() String
            +getDate() LocalDate
            +getProducts() List~Product~
            +addProduct(product: Product) void
            +getWarrantyCost() double
            +addWarrantyCost(amount: double) void
            +calculateTotal() double
        }
    }

    namespace Persistence {
        class WarrantyRepository {
            -filePath: String
            -productService: ProductService
            -accessoryService: AccessoryService
            -salePersistence: SalePersistence
            -personService: PersonService
            +WarrantyRepository(filePath: String, productService: ProductService, accessoryService: AccessoryService, salePersistence: SalePersistence, personService: PersonService)
            +saveAll(warranties: List~Warranty~) void
            +loadAll() List~Warranty~
            -toLine(warranty: Warranty) String
            -fromLine(line: String, sales: List~Sale~) Warranty
        }

        class SalePersistence {
            -filePath: String
            +saveAll(sales: List~Sale~) void
            +loadAll(productService: ProductService, personService: PersonService) List~Sale~
        }
    }

    namespace Service {
        class WarrantyService {
            -repository: WarrantyRepository
            -warranties: List~Warranty~
            +WarrantyService(repository: WarrantyRepository)
            +assignBasicWarranty(product: Product, sale: Sale, startDate: LocalDate) BasicWarranty
            +assignExtendedWarranty(product: Product, sale: Sale, startDate: LocalDate) ExtendedWarranty
            +findWarrantyByProduct(productId: String, saleId: String) Warranty
            +listAllWarranties() List~Warranty~
            +listActiveWarranties() List~Warranty~
            +listWarrantiesExpiringSoon(daysAhead: int) List~Warranty~
            -generateId() String
        }

        class SaleService {
            -sales: List~Sale~
            -salePersistence: SalePersistence
            -warrantyService: WarrantyService
            +registerSale(clientId: String, sellerId: String, productIds: List~String~, productIdsWithExtendedWarranty: List~String~) Sale
            -generateWarranties(sale: Sale, products: List~Product~, productIdsWithExtendedWarranty: List~String~) void
        }
    }

    namespace Ui {
        class ConsoleMenu {
            -saleService: SaleService
            -warrantyService: WarrantyService
            -registerSaleFlow() void
            -askForExtendedWarranty(productId: String, productIdsWithExtendedWarranty: List~String~) void
            -warrantiesMenu() void
            -findWarrantyFlow() void
            -listWarrantiesExpiringSoonFlow() void
        }
    }

    Warranty <|-- BasicWarranty : extends
    Warranty <|-- ExtendedWarranty : extends
    Product <|-- Console : extends

    Warranty --> Product : covers
    Warranty --> Sale : generated by
    Sale o-- Product : contains

    WarrantyService --> WarrantyRepository : uses
    WarrantyService ..> BasicWarranty : creates
    WarrantyService ..> ExtendedWarranty : creates
    WarrantyRepository ..> Warranty : rebuilds
    WarrantyRepository --> SalePersistence : resolves sales

    SaleService --> WarrantyService : grants warranties
    SaleService --> SalePersistence : persists sales
    ConsoleMenu --> SaleService : uses
    ConsoleMenu --> WarrantyService : queries
```

## Reading of the diagram

* **New hierarchy.** `Warranty` is abstract and concentrates the identifier, the associated product and sale, and both dates. `BasicWarranty` and `ExtendedWarranty` inherit from it and only implement the three abstract methods that change between types: duration, type name and additional cost.
* **Relationship with the existing classes.** A warranty holds an association with the `Product` it covers and with the `Sale` that generated it; neither `Product` nor `Sale` knows the warranty module, so the dependency points in a single direction and the previous modules remain untouched in that respect.
* **New persistence and service classes.** `WarrantyRepository` only reads and writes `data/warranties.csv`, using a discriminator (`BASIC` / `EXTENDED`) to rebuild the correct subtype, and receives by constructor the dependencies it needs to resolve the stored identifiers. `WarrantyService` holds every business rule of the module.
* **Integration point.** `SaleService` depends on `WarrantyService` and grants the coverage inside `registerSale`, adding the cost of the extended warranties to the sale. `ConsoleMenu` asks the seller whether the client wants extended coverage and exposes the new warranty submenu.