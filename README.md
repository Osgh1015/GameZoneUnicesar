# GameZone Unicesar

Sistema de información para **GameZone Unicesar**, una tienda de videojuegos y consolas ubicada en el sector universitario de Valledupar. El sistema permite gestionar productos (videojuegos y consolas), personas (clientes y vendedores), accesorios, ventas y **garantías**, con persistencia de la información entre ejecuciones.

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
- [`docs/warranty-analysis.md`](./docs/warranty-analysis.md) — Análisis del módulo de garantías
- [`docs/warranty-class-diagram.md`](./docs/warranty-class-diagram.md) — Diagrama de clases del módulo de garantías

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

El menú de consola permite ejecutar las siguientes operaciones:

**Gestión de productos**
- Registrar un nuevo videojuego
- Registrar una nueva consola
- Listar todos los productos disponibles en el inventario

**Gestión de personas**
- Registrar un nuevo cliente
- Listar todos los clientes registrados
- Listar todos los vendedores registrados

**Gestión de ventas**
- Registrar una nueva venta (cliente, vendedor y uno o más productos)
- Al registrar la venta, el sistema pregunta si desea agregar garantía extendida a cada consola incluida
- Consultar el historial completo de ventas
- Consultar el historial de compras de un cliente específico
- Consultar el historial de ventas atendidas por un vendedor específico

**Gestión de garantías**
- Consultar la garantía asociada a un producto dentro de una venta específica (certificado completo)
- Listar todas las garantías registradas
- Listar las garantías vigentes en la fecha actual
- Listar las garantías próximas a vencer, indicando los días de anticipación

### Reglas del módulo de garantías

| | Garantía básica | Garantía extendida |
|---|---|---|
| Cobertura | Defectos de fábrica | Defectos de fábrica y daños accidentales |
| Duración | 6 meses desde la fecha de venta | 12 meses desde la fecha de venta |
| Costo | Sin costo adicional | 10% del precio del producto |
| Asignación | Automática para cada consola vendida | Opcional, solicitada al registrar la venta |

Los videojuegos y los accesorios no generan garantía, por tratarse de productos consumibles sin defectos de fábrica típicos. El costo de las garantías extendidas se suma automáticamente al total de la venta.

## Persistencia de datos

Toda la información (productos, personas, accesorios, ventas y garantías) se almacena en archivos dentro de la carpeta [`data/`](./data). Las garantías se guardan en `data/warranties.csv`, que se crea automáticamente al registrar la primera venta con consolas. Los datos se cargan automáticamente al iniciar la aplicación y se guardan automáticamente al finalizar cada operación. El archivo de vendedores incluye al menos tres registros precargados.

## Control de versiones

El repositorio sigue el modelo **Git Flow simplificado**:

- `main` — versión estable del sistema (protegida)
- `develop` — rama de integración del equipo (protegida)
- `feature/*` — ramas de trabajo por funcionalidad, integradas a `develop` mediante Pull Request revisado

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
│   └── (archivos de datos precargados)
└── docs/
    ├── analysis.md
    ├── hierarchy-diagram.md
    ├── class-diagram.md
    ├── layers-diagram.md
    ├── warranty-analysis.md
    ├── warranty-class-diagram.md
    └── ai-usage/
        ├── leader-ai-log.md
        ├── developer1-ai-log.md
        └── developer2-ai-log.md
```