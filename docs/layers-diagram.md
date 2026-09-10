# Layers Diagram

```mermaid
flowchart TD
    subgraph UI["User Interface Layer"]
        ConsoleUI
        Main
    end

    subgraph Service["Service Layer"]
        ProductService
        PersonService
        SaleService
    end

    subgraph Persistence["Persistence Layer"]
        ProductRepository
        PersonRepository
        SaleRepository
    end

    subgraph Model["Model Layer"]
        Person
        Client
        Seller
        Product
        VideoGame
        Console
        Sale
    end

    UI --> Service
    Service --> Persistence
    Service --> Model
    Persistence --> Model