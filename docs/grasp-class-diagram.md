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

## After the correction

State of `develop` after the Pull Requests of Phase 2. New classes: `ItemCatalog`, `SaleAssembler`, `ConsoleInput` and the seven submenus; new records: `ReturnRecord`, `SaleRecord`. The notes name the violation each element corrects.

```mermaid
classDiagram
    direction LR

    class Product {
        <<abstract>>
        +decreaseStock(amount: int) void
        +increaseStock(amount: int) void
        +belongsToCategory(category: String)* boolean
        +isWarrantyEligible() boolean
        +getTypeLabel()* String
    }
    class VideoGame {
        +belongsToCategory(category: String) boolean
        +getTypeLabel() String
    }
    class Console {
        +belongsToCategory(category: String) boolean
        +isWarrantyEligible() boolean
        +getTypeLabel() String
    }
    class Accessory {
        <<abstract>>
        +belongsToCategory(category: String) boolean
        +getAccessoryType()* String
        +getTypeLabel() String
    }
    class Controller {
        +getAccessoryType() String
    }
    class Cable {
        +getAccessoryType() String
    }
    class Memory {
        +getAccessoryType() String
    }
    class CategoryDiscount {
        +calculateDiscount(sale: Sale) double
    }
    class Return {
        +calculatePaidRatio() double
        +belongsToClient(clientId: String) boolean
        +belongsToSale(saleId: String) boolean
    }
    class Warranty {
        <<abstract>>
        +covers(productId: String, saleId: String) boolean
        +belongsToSale(saleId: String) boolean
    }
    class ExtendedWarranty {
        +getAdditionalCost() double
        +quoteFor(product: Product)$ double
    }
    class Sale {
        +belongsToClient(clientId: String) boolean
        +wasAttendedBy(sellerId: String) boolean
        +findProduct(productId: String) Product
        +countUnitsOf(productId: String) int
        +generateReceipt() String
    }

    class ProductRepository {
        +ProductRepository(filePath: String)
    }
    class PersonRepository {
        +PersonRepository(clientsFile: String, vendorsFile: String)
    }
    class ReturnRepository {
        +ReturnRepository(filePath: String)
        +loadAll() List~ReturnRecord~
    }
    class ReturnRecord {
        <<record>>
    }
    class SalePersistence {
        +SalePersistence(filePath: String)
        +loadAll() List~SaleRecord~
        +saveAll(sales: List~Sale~) void
    }
    class SaleRecord {
        <<record>>
    }

    class ItemCatalog {
        +findItem(id: String) Product
        +decreaseStock(item: Product, amount: int) void
        +restoreStock(item: Product, amount: int) void
    }
    class SaleAssembler {
        +loadSales() List~Sale~
    }
    class ProductService {
        +ProductService(repository: ProductRepository)
    }
    class PersonService {
        +PersonService(repository: PersonRepository)
    }
    class AccessoryService
    class ReturnService
    class WarrantyService
    class SaleService

    class ConsoleMenu {
        +start() void
    }
    class ConsoleInput {
        +nextLine() String
        +readInt() int
        +readDouble() double
        +readDate(prompt: String) LocalDate
    }
    class ProductMenu {
        +show() void
    }
    class PersonMenu {
        +show() void
    }
    class SaleMenu {
        +show() void
    }
    class AccessoryMenu {
        +show() void
    }
    class ReturnMenu {
        +show() void
    }
    class PromotionMenu {
        +show() void
    }
    class WarrantyMenu {
        +show() void
    }

    Product <|-- VideoGame
    Product <|-- Console
    Product <|-- Accessory
    Accessory <|-- Controller
    Accessory <|-- Cable
    Accessory <|-- Memory
    Warranty <|-- ExtendedWarranty

    CategoryDiscount ..> Product : belongsToCategory (V-03)
    Return --> Sale : calculateTotal (V-04)
    ReturnRepository ..> ReturnRecord : creates (V-10)
    SalePersistence ..> SaleRecord : creates (V-19)

    ProductService --> ProductRepository : injected (V-02)
    PersonService --> PersonRepository : injected (V-05)
    AccessoryService ..> Accessory : getAccessoryType, stock methods (V-06, V-07)
    ReturnService --> ReturnRepository
    ReturnService --> ItemCatalog : lookup and stock (V-10, V-11)
    ReturnService ..> Return : belongsToClient, belongsToSale (V-08)
    ReturnService ..> Sale : findProduct, countUnitsOf (V-09)
    WarrantyService ..> Warranty : covers, belongsToSale (V-12)
    WarrantyService --> SaleAssembler : stored sales (V-19)
    SaleService --> SaleAssembler : stored sales (V-19)
    SaleService --> ItemCatalog : lookup and stock (V-18)
    SaleService ..> Sale : belongsToClient, wasAttendedBy (V-13)
    SaleService ..> Product : isWarrantyEligible (V-16)
    SaleAssembler --> SalePersistence
    SaleAssembler --> PersonService
    SaleAssembler --> ItemCatalog
    ItemCatalog --> ProductService
    ItemCatalog --> AccessoryService

    ConsoleMenu --> ConsoleInput
    ConsoleMenu --> ProductMenu : V-20
    ConsoleMenu --> PersonMenu : V-20
    ConsoleMenu --> SaleMenu : V-20
    ConsoleMenu --> AccessoryMenu : V-20
    ConsoleMenu --> ReturnMenu : V-20
    ConsoleMenu --> PromotionMenu : V-20
    ConsoleMenu --> WarrantyMenu : V-20
    SaleMenu ..> ExtendedWarranty : quoteFor (V-15)
    SaleMenu ..> Product : isWarrantyEligible, getTypeLabel (V-16, V-17)
```

Changes that are not visible as a relation:

| ID | Change |
|---|---|
| V-01, V-06, V-14 | The three services ask the product to `decreaseStock`/`increaseStock` |
| V-13 | `SaleMenu.showWarrantiesOfSale` asks `warranty.belongsToSale(saleId)` |
| V-17 | `Sale.generateReceipt` prints `product.getTypeLabel()` |
| V-20 | `ConsoleMenu` keeps only the main loop and the exception handling (145 lines); each submenu has between 86 and 242 lines |

Dependency direction after the correction: `ui → service → persistence → model`, with no persistence class importing a service.
