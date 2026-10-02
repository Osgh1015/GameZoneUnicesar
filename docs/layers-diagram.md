# Layers Diagram

Four-layer architecture of the integrated system (Requirement 5). Each layer only depends
on the layers below it: `ui → service → persistence → model`. `Main` is the entry point:
it creates every repository and service, injects the dependencies by constructor and
starts `ConsoleMenu`.

```mermaid
flowchart TD
    subgraph UI["User Interface Layer (com.gamezone.ui + Main)"]
        Main
        ConsoleMenu
    end

    subgraph Service["Service Layer (com.gamezone.service)"]
        ProductService
        PersonService
        AccessoryService
        PromotionService
        WarrantyService
        SaleService
        ReturnService
    end

    subgraph Persistence["Persistence Layer (com.gamezone.persistence)"]
        ProductRepository
        PersonRepository
        AccessoryRepository
        PromotionRepository
        WarrantyRepository
        SalePersistence
        ReturnRepository
    end

    subgraph Model["Model Layer (com.gamezone.model)"]
        Product
        VideoGame
        Console
        Accessory
        Controller
        Cable
        Memory
        Person
        Client
        Seller
        Sale
        Promotion
        PercentageDiscount
        CategoryDiscount
        BulkPurchaseDiscount
        Warranty
        BasicWarranty
        ExtendedWarranty
        Return
    end

    subgraph Data["Data files (data/)"]
        products["products.txt"]
        people["clients.txt / vendors.txt"]
        accessories["accessories.csv"]
        promotions["promotions.csv"]
        warranties["warranties.csv"]
        sales["sales.txt"]
        returns["returns.txt"]
    end

    UI --> Service
    Service --> Persistence
    Service --> Model
    Persistence --> Model
    Persistence --> Data
```

## Responsibilities per layer

| Layer | Responsibility | Classes |
|---|---|---|
| UI | Console menu (messages in Spanish) and application startup. It only calls services. | `Main`, `ConsoleMenu` |
| Service | Business rules: stock, best promotion, warranties for consoles, unified sale flow, returns, refunds and monthly balance. | `ProductService`, `PersonService`, `AccessoryService`, `PromotionService`, `WarrantyService`, `SaleService`, `ReturnService` |
| Persistence | Reading and writing the files in `data/`. No business rules. | `ProductRepository`, `PersonRepository`, `AccessoryRepository`, `PromotionRepository`, `WarrantyRepository`, `SalePersistence`, `ReturnRepository` |
| Model | Domain classes and their intrinsic behavior (totals, discounts, warranty duration, refund). No file access. | Products, accessories, people, sale, promotions, warranties and returns |

## Main service dependencies

```mermaid
flowchart LR
    SaleService --> PromotionService
    SaleService --> WarrantyService
    SaleService --> ProductService
    SaleService --> AccessoryService
    SaleService --> PersonService
    ReturnService --> SaleService
    ReturnService --> WarrantyService
    ReturnService --> ProductService
    ReturnService --> AccessoryService
    WarrantyService --> WarrantyRepository
    WarrantyService -. resolves sales .-> SalePersistence
```

There is no cycle between services: `WarrantyService` does not depend on `SaleService`
(integration adjustment A2), so `Main` can build `WarrantyService` first, then
`SaleService` and finally `ReturnService`.
