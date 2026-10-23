# Accessory Module — Updated Class Diagram

```mermaid
classDiagram
    class Product {
        <<abstract>>
        -String id
        -String title
        -double price
        -int quantity
        +getId() String
        +getTitle() String
        +getPrice() double
        +getQuantity() int
        +setQuantity(int)
        +getDescription()* String
    }

    class VideoGame {
        -String platform
        -String genre
        -String ageRating
        +getDescription() String
    }

    class Console {
        -String brand
        -String model
        -int generation
        +getDescription() String
    }

    class Accessory {
        <<abstract>>
        -List~String~ compatibleConsoleIds
        +getCompatibleConsoleIds() List~String~
        +addCompatibleConsole(String)
        +removeCompatibleConsole(String)
        +isCompatibleWith(String) boolean
        +getDescription() String
    }

    class Controller {
        -String connectionType
        +getConnectionType() String
        +setConnectionType(String)
        +getDescription() String
    }

    class Cable {
        -double lengthInMeters
        -String connectorType
        +getLengthInMeters() double
        +getConnectorType() String
        +getDescription() String
    }

    class Memory {
        -int capacityInGb
        -String memoryType
        +getCapacityInGb() int
        +getMemoryType() String
        +getDescription() String
    }

    class AccessoryRepository {
        -String filePath
        +saveAll(List~Accessory~)
        +loadAll() List~Accessory~
    }

    class AccessoryService {
        -AccessoryRepository repository
        -List~Accessory~ accessories
        +registerController(Controller)
        +registerCable(Cable)
        +registerMemory(Memory)
        +listAllAccessories() List~Accessory~
        +listAccessoriesByType(String) List~Accessory~
        +findAccessoriesCompatibleWith(String) List~Accessory~
        +findById(String) Accessory
        +updateStock(String, int)
    }

    class Sale {
        -List~Product~ products
        +calculateTotal() double
    }

    class SaleService {
        -ProductService productService
        -AccessoryService accessoryService
        +registerSale(...) Sale
    }

    class ConsoleMenu {
        +accessoriesMenu()
    }

    Product <|-- VideoGame
    Product <|-- Console
    Product <|-- Accessory
    Accessory <|-- Controller
    Accessory <|-- Cable
    Accessory <|-- Memory

    Accessory ..> Console : compatible with (by id)
    AccessoryRepository --> Accessory : persists
    AccessoryService --> AccessoryRepository : uses
    Sale o-- Product : contains
    SaleService --> AccessoryService : resolves and updates
    ConsoleMenu --> AccessoryService : uses
```

## Notes

- `Accessory` sits between `Product` and the three concrete accessory types, so every
  accessory is a `Product` and can be placed inside a `Sale` alongside video games and
  consoles.
- Compatibility is drawn as a dependency from `Accessory` to `Console` because the accessory
  stores console **ids**, not `Console` object references — this keeps the model layer free
  of circular references and keeps persistence flat.
