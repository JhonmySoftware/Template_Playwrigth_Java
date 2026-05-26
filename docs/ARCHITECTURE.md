# Arquitectura del Framework

## 1. Objetivo

Este documento describe la arquitectura tecnica actual del proyecto y la forma correcta de extenderlo sin degradar mantenibilidad.

El framework esta construido con una idea central:

- **las pruebas expresan comportamiento**
- **los Page Objects encapsulan interacciones**
- **la configuracion vive fuera del test**
- **las evidencias se generan automaticamente**

---

## 2. Principios de Diseno

### Separacion de responsabilidades

- `tests`: orquestan escenarios y assertions
- `pages`: encapsulan locators y acciones de pagina
- `config`: encapsula bootstrap de Playwright y lectura de propiedades
- `utils`: concentra capacidades compartidas

### Independencia por prueba

Cada test usa un `BrowserContext` y un `Page` aislado.  
Eso evita fuga de cookies, local storage y estado accidental entre casos.

### Evidencia por defecto

La observabilidad no se deja para despues. Cada prueba puede producir:

- screenshot
- video
- trace
- registro en reporte HTML

### Configuracion parametrizable

Los valores del archivo `config.properties` pueden sobreescribirse por `System.getProperty`, lo que permite variar navegador, tiempo y URL por ambiente sin tocar codigo.

---

## 3. Paquetes y Responsabilidades

## `config`

### `FrameworkConfig`

Expone getters tipados para:

- navegador
- headless
- slowMo
- timeouts
- viewport
- URL base
- artefactos
- reportes
- video

### `PlaywrightFactory`

Se encarga de:

- crear `Playwright`
- configurar `test id attribute`
- crear `Browser`
- crear `BrowserContext`

La razon de tener esta fabrica es evitar que `BaseTest` conozca detalles de creacion de browser y contexto.

## `pages`

### `BasePage`

Contiene wrappers reutilizables:

- `navigate`
- `fill`
- `click`
- `textOf`
- `isVisible`
- `waitForUrlContains`

Estos wrappers reducen duplicacion y unifican manejo de excepciones.

### `LoginPage`

Modela:

- username
- password
- login button
- mensaje de error

### `InventoryPage`

Modela la primera pagina valida despues del login.

## `utils`

### `ConfigManager`

Carga propiedades desde recurso y permite override por `System properties`.

### `CsvDataReader`

Lee datasets simples desde `src/test/resources`.

### `DatabaseUtils`

Queda disponible para validaciones backend futuras por JDBC.

### `ArtifactManager`

Administra estructura de salida bajo `target/artifacts/run_<timestamp>/`.

### `ExecutionReportManager`

Construye el HTML de evidencias con:

- estado
- duracion
- links a screenshot
- links a trace
- links a video
- preview inline

## `tests`

### `BaseTest`

Gestiona el ciclo de vida:

1. crea `Playwright`
2. crea `Browser`
3. crea `BrowserContext`
4. crea `Page`
5. activa tracing
6. al finalizar:
   - captura screenshot
   - guarda trace
   - guarda video
   - actualiza reporte de evidencias
   - cierra recursos

### `LoginTest`

Contiene la logica del escenario y assertions de negocio.

---

## 4. Flujo de Ejecucion

1. TestNG invoca `@BeforeClass` en `BaseTest`
2. `FrameworkConfig` carga propiedades
3. `PlaywrightFactory` crea `Playwright` y `Browser`
4. antes de cada prueba:
   - se crea `BrowserContext`
   - se crea `Page`
   - se activan traces si aplica
5. la prueba usa Page Objects
6. al terminar:
   - screenshot
   - trace
   - video
   - reporte HTML
7. se cierran `Page`, `BrowserContext`, `Browser`, `Playwright`

---

## 5. Estrategia de Locators

Orden recomendado:

1. `getByTestId`
2. `getByRole`
3. `getByLabel`
4. `getByText`
5. CSS/XPath solo si no hay alternativa estable

Razon:

- menor acoplamiento con layout
- menor fragilidad ante cambios visuales
- mejor legibilidad

Playwright recomienda locators semanticos y estables:

- https://playwright.dev/java/docs/locators

---

## 6. Estrategia de Evidencias

### Screenshots

Se capturan en exito y fallo segun configuracion.

### Videos

Se guardan por test para diagnostico funcional.

### Traces

Se guardan como `.zip` para depuracion profunda en Trace Viewer.

### Reporte HTML propio

Se genera para consumo del equipo QA sin depender solo del reporte tecnico de Surefire.

---

## 7. Convenciones de Extension

### Nueva pagina web

1. crear clase en `pages`
2. encapsular locators
3. encapsular acciones
4. no meter assertions complejas de negocio dentro del Page Object

### Nueva prueba

1. extender `BaseTest`
2. usar Page Objects
3. dejar assertions en la prueba
4. externalizar data si el escenario crece

### Nueva utilidad

Una utilidad solo debe entrar en `utils` si:

- se usa en mas de una capa
- no pertenece claramente a `pages`, `tests` o `config`

---

## 8. Riesgos a Evitar

- meter logica de negocio dentro de `BasePage`
- usar locators CSS fragiles
- compartir `Page` o `BrowserContext` entre pruebas
- codificar URLs, credenciales o datasets dentro de las pruebas
- capturar evidencias solo cuando ya hay fallos masivos

---

## 9. Evolucion Recomendada

A medida que el proyecto crezca, se recomienda evolucionar a una estructura por canal:

```text
src/main/java/com/example/project/
+-- core/
+-- web/
+-- api/
\-- mobile/
```

La estrategia detallada esta en:

- [TRANSVERSAL-STRATEGY.md](TRANSVERSAL-STRATEGY.md)
