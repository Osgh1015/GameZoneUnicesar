# GameZone Unicesar

Sistema de información para **GameZone Unicesar**, una tienda de videojuegos y consolas ubicada en el sector universitario de Valledupar. El sistema permite gestionar productos (videojuegos y consolas), accesorios, personas (clientes y vendedores), ventas, promociones, garantías y devoluciones, con persistencia de la información entre ejecuciones. Los cuatro módulos de ampliación (accesorios, promociones, garantías y devoluciones) operan de forma integrada sobre una misma venta (Requerimiento 5).

Proyecto desarrollado como Taller de Programación III — Universidad Popular del Cesar (Unicesar). Docente: Ing. Esp. Alfredo Bautista.

## Equipo

| Rol | Integrante | Módulo asignado |
|---|---|---|
| Líder Técnico | Omar gamez | Ventas e Integración |
| Desarrollador 1 | Santiago Herrera | Productos |
| Desarrollador 2 | Pablo Amaya | Personas |

Detalle completo de roles, distribución de clases y actividades comprometidas en [`TEAM.md`](./TEAM.md).

## Arquitectura

El sistema está organizado en **cuatro capas**, bajo el paquete raíz `com.gamezone`:

```
com.gamezone
├── model         # Clases del dominio del negocio
├── persistence   # Guardado y recuperación de información desde archivos
├── service       # Reglas de negocio
├── ui            # Interfaz de usuario (menú de consola)
└── Main.java     # Clase principal que arranca la aplicación
```

Dependencias permitidas entre capas: `ui → service → persistence → model`. El modelo no depende de ninguna otra capa.

Diagramas de diseño disponibles en la carpeta [`docs/`](./docs):
- [`docs/analysis.md`](./docs/analysis.md) — Preguntas orientadoras del análisis
- [`docs/hierarchy-diagram.md`](./docs/hierarchy-diagram.md) — Diagrama de jerarquías
- [`docs/class-diagram.md`](./docs/class-diagram.md) — Diagrama de clases
- [`docs/layers-diagram.md`](./docs/layers-diagram.md) — Diagrama de capas
- [`docs/accessory-analysis.md`](./docs/accessory-analysis.md) — Análisis del módulo de accesorios
- [`docs/accessory-class-diagram.md`](./docs/accessory-class-diagram.md) — Diagrama de clases del módulo de accesorios
- [`docs/promotion-analysis.md`](./docs/promotion-analysis.md) — Análisis del módulo de promociones
- [`docs/promotion-class-diagram.md`](./docs/promotion-class-diagram.md) — Diagrama de clases del módulo de promociones
- [`docs/warranty-analysis.md`](./docs/warranty-analysis.md) — Análisis del módulo de garantías
- [`docs/warranty-class-diagram.md`](./docs/warranty-class-diagram.md) — Diagrama de clases del módulo de garantías
- [`docs/return-analysis.md`](./docs/return-analysis.md) — Análisis del módulo de devoluciones
- [`docs/return-class-diagram.md`](./docs/return-class-diagram.md) — Diagrama de clases del módulo de devoluciones
- [`docs/integration-analysis.md`](./docs/integration-analysis.md) — Análisis de la integración (ajustes A1–A7 e incidente del PR #11)
- [`docs/integrated-class-diagram.md`](./docs/integrated-class-diagram.md) — Diagrama de clases único del sistema integrado

## Requisitos previos

- Java JDK 17 o superior
- Apache Maven 3.8+

## Compilación

```bash
mvn clean install
```

## Ejecución

```bash
mvn exec:java -Dexec.mainClass="com.gamezone.Main"
```

Alternativamente, tras compilar, ejecutar el jar generado:

```bash
java -jar target/gamezone-unicesar-1.0.jar
```

## Funcionalidades del sistema

El menú principal tiene siete opciones: productos, personas, ventas, accesorios, devoluciones, promociones y garantías.

**Gestión de productos**
- Registrar un nuevo videojuego
- Registrar una nueva consola
- Listar todos los productos disponibles en el inventario

**Gestión de personas**
- Registrar un nuevo cliente
- Listar todos los clientes registrados
- Listar todos los vendedores registrados

**Gestión de accesorios**
- Registrar controles, cables y memorias, con sus consolas compatibles
- Listar todos los accesorios o filtrarlos por tipo
- Consultar los accesorios compatibles con una consola

**Gestión de ventas**
- Registrar una nueva venta: se muestran los productos y accesorios disponibles, se eligen uno o más ítems y, por cada consola, se pregunta si el cliente desea garantía extendida
- Flujo unificado de registro: validación de ítems y stock, mejor promoción vigente calculada solo sobre los ítems, garantías de las consolas, total final (subtotal − descuento + garantías extendidas), actualización de inventario y persistencia
- Consultar el historial completo de ventas
- Consultar el historial de compras de un cliente específico
- Consultar el historial de ventas atendidas por un vendedor específico
- Ver el detalle (recibo) de una venta, con el tipo de cada ítem, subtotal, descuento con el nombre de la promoción, costo de garantías extendidas y total final

**Gestión de promociones**
- Registrar promociones por porcentaje, por categoría (`VIDEOGAME` / `CONSOLE` / `ACCESSORY`) y por volumen de compra
- Listar todas las promociones registradas y solo las vigentes en la fecha actual
- Aplicación automática al registrar una venta: se aplica la única promoción vigente que otorga el mayor descuento (no son acumulables)

**Gestión de garantías**
- Garantía básica automática (6 meses, sin costo) para cada consola vendida; los videojuegos y accesorios no generan garantía
- Garantía extendida opcional para consolas (12 meses), con un costo del 10% del precio de la consola que se suma al total de la venta
- Consultar la garantía de un producto dentro de una venta específica (certificado con vigencia)
- Listar todas las garantías registradas
- Listar las garantías vigentes en la fecha actual
- Listar las garantías próximas a vencer, indicando los días de anticipación
- Al devolver una consola, sus garantías se anulan y se reembolsa el costo de la garantía extendida

**Gestión de devoluciones**
- Registrar la devolución total o parcial de una venta dentro de los 30 días siguientes, incluyendo productos y accesorios
- El stock de cada ítem devuelto se restaura en el inventario que corresponde (productos o accesorios)
- El reembolso de cada ítem es proporcional al descuento de la venta original: precio × (1 − descuento / subtotal); el recibo muestra precio de lista, descuento proporcional, monto reembolsado y garantías anuladas
- Consultar todas las devoluciones, por cliente o por venta
- Balance mensual: total de ventas (con descuentos y garantías), total de devoluciones y balance neto

## Persistencia de datos

Toda la información se almacena en archivos dentro de la carpeta [`data/`](./data). Los datos se cargan automáticamente al iniciar la aplicación y se guardan automáticamente al finalizar cada operación.

| Archivo | Contenido |
|---|---|
| `products.txt` | Videojuegos y consolas |
| `clients.txt`, `vendors.txt` | Clientes y vendedores (al menos tres vendedores precargados) |
| `accessories.csv` | Controles, cables y memorias (al menos uno de cada tipo precargado) |
| `promotions.csv` | Promociones por porcentaje, por categoría (incluida una de accesorios) y por volumen |
| `sales.txt` | Ventas, con promoción aplicada, descuento y costo de garantías extendidas |
| `warranties.csv` | Garantías básicas y extendidas (se generan a partir de las ventas) |
| `returns.txt` | Devoluciones, con el reembolso y el valor de las garantías anuladas (se generan a partir de las ventas) |

## Control de versiones

El repositorio sigue el modelo **Git Flow simplificado**:

- `main` — versión estable del sistema (protegida)
- `develop` — rama de integración del equipo (protegida)
- `feature/*` — nuevas funcionalidades (commits `feat:`)
- `fix/*` — corrección de defectos (commits `fix:`)
- `refactor/*` — reorganización sin cambio de funcionalidad (commits `refactor:`)
- `docs/*` — documentación (commits `docs:`)

Todas las ramas salen de `develop` y se integran a `develop` mediante Pull Request aprobado por un integrante distinto al autor; después de fusionarse se eliminan del remoto. Está prohibido hacer commits directos a `main` o `develop` y usar `git push --force`.

Convención de commits: [Conventional Commits](https://www.conventionalcommits.org/) (`feat:`, `fix:`, `docs:`, `refactor:`, `chore:`), redactados en inglés.

## Estructura del repositorio

```
repositorio/
├── README.md
├── TEAM.md
├── pom.xml
├── .gitignore
├── src/
│   └── main/
│       └── java/
│           └── com/
│               └── gamezone/
│                   ├── model/
│                   ├── persistence/
│                   ├── service/
│                   ├── ui/
│                   └── Main.java
├── data/
│   └── (archivos de datos)
└── docs/
    ├── analysis.md
    ├── hierarchy-diagram.md
    ├── class-diagram.md
    ├── layers-diagram.md
    ├── accessory-analysis.md
    ├── accessory-class-diagram.md
    ├── promotion-analysis.md
    ├── promotion-class-diagram.md
    ├── warranty-analysis.md
    ├── warranty-class-diagram.md
    ├── return-analysis.md
    ├── return-class-diagram.md
    ├── integration-analysis.md
    ├── integrated-class-diagram.md
    └── ai-usage/
        ├── leader-ai-log.md
        ├── developer1-ai-log.md
        └── developer2-ai-log.md
```