# GRASP Class Diagram — Requerimiento 7

Class diagrams of the parts of the system affected by the GRASP corrections. Only the classes, attributes, methods and relations that take part in a violation of `docs/grasp-audit/consolidated-audit.md` are drawn; the rest of the system is unchanged and is described in the diagrams of the previous requirements.

## Before the correction

State of `develop` at the audited commit `4027852`. The notes name the violations.

```mermaid
classDiagram
    direction LR

    class Product {
        <<abstract>>
        -quantity: int
        +getQuantity() int
        +setQuantity(quantity: int) void
        +getDescription()* String
    }
    class VideoGame
    class Console
    class Accessory {
        <<abstract>>
    }
    class Controller
    class Cable
    class Memory
    class CategoryDiscount {
        -targetCategory: String
        +calculateDiscount(sale: Sale) double
        -belongsToCategory(product: Product) boolean
    }
    class Return {
        -originalSale: Sale
        +calculatePaidRatio() double
    }
    class Warranty {
        <<abstract>>
        -product: Product
        -sale: Sale
    }
    class ExtendedWarranty {
        +getAdditionalCost() double
    }
    class Sale {
        -client: Client
        -seller: Seller
        -products: List~Product~
        +getProducts() List~Product~
        +calculateTotal() double
        +generateReceipt() String
        -describeItemType(product: Product) String
    }

    class ProductRepository {
        -filePath: String
        +ProductRepository()
    }
    class PersonRepository {
        -CLIENTS_FILE$: String
        -VENDORS_FILE$: String
    }
    class ReturnRepository {
        -saleService: SaleService
        -productService: ProductService
        -accessoryService: AccessoryService
        +loadAll() List~Return~
    }
    class SalePersistence {
        -accessoryService: AccessoryService
        +loadAll(productService, personService) List~Sale~
    }

    class ProductService {
        +ProductService()
        +decreaseStock(id: String, amount: int) void
        +restoreStock(id: String, amount: int) void
    }
    class PersonService {
        +PersonService()
    }
    class AccessoryService {
        +listAccessoriesByType(type: String) List~Accessory~
        -matchesType(accessory, type) boolean
        +decreaseStock(id: String, amount: int) void
        +restoreStock(id: String, amount: int) void
    }
    class ReturnService {
        +registerReturn(saleId, productIds, reason) Return
        +viewReturnsByCustomer(customerId: String) List~Return~
        +viewReturnsBySale(saleId: String) List~Return~
        -countSoldUnits(sale, productId) int
    }
    class WarrantyService {
        -salePersistence: SalePersistence
        -personService: PersonService
        +findWarrantyByProduct(productId, saleId) Warranty
        +cancelWarranties(productId, saleId) double
    }
    class SaleService {
        +registerSale(clientId, sellerId, productIds, extendedIds) Sale
        +getSalesByClient(clientId: String) List~Sale~
        +getSalesBySeller(sellerId: String) List~Sale~
        -findSellableItem(itemId: String) Product
        -updateInventory(items) void
    }
    class ConsoleMenu {
        -scanner: Scanner
        +start() void
        -productsMenu() void
        -peopleMenu() void
        -salesMenu() void
        -accessoriesMenu() void
        -returnsMenu() void
        -promotionsMenu() void
        -warrantiesMenu() void
        -askForExtendedWarranty(productId, ids) void
        -readInt() int
        -readDouble() double
        -readDate(prompt: String) LocalDate
    }

    Product <|-- VideoGame
    Product <|-- Console
    Product <|-- Accessory
    Accessory <|-- Controller
    Accessory <|-- Cable
    Accessory <|-- Memory
    Warranty <|-- ExtendedWarranty

    CategoryDiscount ..> VideoGame : instanceof (V-03)
    CategoryDiscount ..> Console : instanceof (V-03)
    CategoryDiscount ..> Accessory : instanceof (V-03)
    Sale ..> Console : instanceof (V-17)
    Sale ..> VideoGame : instanceof (V-17)
    Sale ..> Accessory : instanceof (V-17)
    Return --> Sale : recalculates subtotal (V-04)

    ProductService ..> ProductRepository : new (V-02)
    PersonService ..> PersonRepository : new (V-05)
    ReturnRepository ..> SaleService : persistence to service (V-10)
    ReturnRepository ..> ProductService : persistence to service (V-10)
    ReturnRepository ..> AccessoryService : persistence to service (V-10)
    SalePersistence ..> AccessoryService : persistence to service (V-19)
    SalePersistence ..> ProductService : persistence to service (V-19)
    SalePersistence ..> PersonService : persistence to service (V-19)
    WarrantyService --> SalePersistence : loads its own sales (V-19)
    WarrantyService --> PersonService : only for SalePersistence (V-19)

    AccessoryService ..> Controller : instanceof (V-07)
    AccessoryService ..> Cable : instanceof (V-07)
    AccessoryService ..> Memory : instanceof (V-07)
    ReturnService ..> Console : instanceof (V-11)
    ReturnService ..> Accessory : instanceof (V-11)
    ReturnService ..> ProductService : stock dispatch (V-11)
    ReturnService ..> AccessoryService : stock dispatch (V-11)
    SaleService ..> Console : instanceof (V-16)
    SaleService ..> ProductService : lookup and stock dispatch (V-18)
    SaleService ..> AccessoryService : lookup and stock dispatch (V-18)
    ConsoleMenu ..> Console : instanceof (V-16)
    ConsoleMenu ..> ExtendedWarranty : repeats the 10 percent rule (V-15)
```

Violations that are not visible as a relation:

| ID | Where |
|---|---|
| V-01, V-06, V-14 | `ProductService.decreaseStock`, `AccessoryService.decreaseStock`/`restoreStock` and `ProductService.restoreStock` compute the stock with `getQuantity`/`setQuantity` |
| V-08 | `ReturnService.viewReturnsByCustomer`/`viewReturnsBySale` navigate `Return → Sale → Client` |
| V-09 | `ReturnService.registerReturn` and `countSoldUnits` search the products of the sale |
| V-12 | `WarrantyService` navigates `Warranty → Product` and `Warranty → Sale` |
| V-13 | `SaleService.getSalesByClient`/`getSalesBySeller` and `ConsoleMenu.showWarrantiesOfSale` navigate `Sale` and `Warranty` |
| V-20 | `ConsoleMenu` holds the seven submenus and the input readers (1026 lines) |