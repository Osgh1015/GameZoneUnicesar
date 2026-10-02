# Warranty Class Diagram

This diagram reflects the warranty module after resolving the circular
dependency identified during system integration.

The persistence layer stores only primitive warranty information and
identifiers. Object references to `Sale` and `Product` are resolved by
`WarrantyService`.

```mermaid
classDiagram

    class Warranty {
        <<abstract>>
        -String id
        -Product product
        -Sale sale
        -LocalDate startDate
        -LocalDate endDate
        +getId() String
        +getProduct() Product
        +getSale() Sale
        +getStartDate() LocalDate
        +getEndDate() LocalDate
        +getWarrantyType() String
        +getAdditionalCost() double
        +isActive(LocalDate) boolean
    }

    class BasicWarranty {
        +BasicWarranty(String id, Product product, Sale sale, LocalDate startDate)
        +getWarrantyType() String
        +getAdditionalCost() double
    }

    class ExtendedWarranty {
        +ExtendedWarranty(String id, Product product, Sale sale, LocalDate startDate)
        +getWarrantyType() String
        +getAdditionalCost() double
    }

    class WarrantyRecord {
        +String type
        +String id
        +String productId
        +String saleId
        +LocalDate startDate
    }

    class WarrantyRepository {
        -String filePath
        +WarrantyRepository(String filePath)
        +saveAll(List~Warranty~ warranties) void
        +loadAll() List~WarrantyRecord~
    }

    class WarrantyService {
        -WarrantyRepository repository
        -SalePersistence salePersistence
        -ProductService productService
        -PersonService personService
        -List~Warranty~ warranties
        +assignBasicWarranty(Product product, Sale sale, LocalDate startDate) BasicWarranty
        +assignExtendedWarranty(Product product, Sale sale, LocalDate startDate) ExtendedWarranty
        +findWarrantyByProduct(String productId, String saleId) Warranty
        +listAllWarranties() List~Warranty~
        +listActiveWarranties() List~Warranty~
        +listWarrantiesExpiringSoon(int daysAhead) List~Warranty~
        +calculateWarrantyCost(String saleId) double
    }

    class SalePersistence
    class ProductService
    class PersonService
    class Product
    class Sale

    Warranty <|-- BasicWarranty
    Warranty <|-- ExtendedWarranty

    Warranty --> Product
    Warranty --> Sale

    WarrantyRepository --> WarrantyRecord : loads
    WarrantyRepository --> Warranty : saves

    WarrantyService --> WarrantyRepository : uses
    WarrantyService --> SalePersistence : resolves sales
    WarrantyService --> ProductService : resolves products
    WarrantyService --> PersonService : resolves people

    WarrantyService --> Warranty : manages