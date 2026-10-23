# Class Diagram

```mermaid
classDiagram
    namespace Model {
        class Person {
            <<abstract>>
            -id: String
            -name: String
            -phone: String
            +getId() String
            +getName() String
            +getPhone() String
            +setName(name: String) void
            +setPhone(phone: String) void
        }

        class Client {
            -email: String
            +getEmail() String
            +setEmail(email: String) void
        }

        class Seller {
            -employeeCode: String
            -shift: String
            +getEmployeeCode() String
            +getShift() String
            +setShift(shift: String) void
        }

        class Product {
            <<abstract>>
            -id: String
            -title: String
            -price: double
            -quantity: int
            +getId() String
            +getTitle() String
            +getPrice() double
            +getQuantity() int
            +setQuantity(quantity: int) void
            +getDescription() String*
        }

        class VideoGame {
            -platform: String
            -genre: String
            -ageRating: String
            +getDescription() String
        }

        class Console {
            -brand: String
            -model: String
            -generation: String
            +getDescription() String
        }

        class Sale {
            -id: String
            -date: Date
            -client: Client
            -seller: Seller
            -products: List~Product~
            +calculateTotal() double
            +addProduct(product: Product) void
            +getProducts() List~Product~
        }
    }

    namespace Persistence {
        class ProductRepository {
            -filePath: String
            +saveProducts(products: List~Product~) void
            +loadProducts() List~Product~
        }

        class PersonRepository {
            -clientsFilePath: String
            -sellersFilePath: String
            +saveClients(clients: List~Client~) void
            +loadClients() List~Client~
            +saveSellers(sellers: List~Seller~) void
            +loadSellers() List~Seller~
        }

        class SaleRepository {
            -filePath: String
            +saveSales(sales: List~Sale~) void
            +loadSales() List~Sale~
        }
    }

    namespace Service {
        class ProductService {
            -productRepository: ProductRepository
            +registerVideoGame(...) VideoGame
            +registerConsole(...) Console
            +listProducts() List~Product~
            +updateStock(productId: String, amount: int) void
        }

        class PersonService {
            -personRepository: PersonRepository
            +registerClient(...) Client
            +listClients() List~Client~
            +listSellers() List~Seller~
        }

        class SaleService {
            -saleRepository: SaleRepository
            -productService: ProductService
            -personService: PersonService
            +registerSale(client: Client, seller: Seller, products: List~Product~) Sale
            +getSalesHistory() List~Sale~
            +getPurchaseHistoryByClient(clientId: String) List~Sale~
            +getSalesBySeller(sellerId: String) List~Sale~
        }
    }

    namespace UI {
        class ConsoleUI {
            -productService: ProductService
            -personService: PersonService
            -saleService: SaleService
            +showMainMenu() void
            +showProductMenu() void
            +showPersonMenu() void
            +showSaleMenu() void
        }
    }

    class Main {
        +main(args: String[]) void
    }

    Person <|-- Client
    Person <|-- Seller
    Product <|-- VideoGame
    Product <|-- Console

    Sale "1" --> "1" Client : buyer
    Sale "1" --> "1" Seller : attendedBy
    Sale "1" --> "1..*" Product : includes

    ProductService --> ProductRepository : depends on
    PersonService --> PersonRepository : depends on
    SaleService --> SaleRepository : depends on
    SaleService --> ProductService : depends on
    SaleService --> PersonService : depends on

    ConsoleUI --> ProductService : uses
    ConsoleUI --> PersonService : uses
    ConsoleUI --> SaleService : uses
    Main --> ConsoleUI : creates