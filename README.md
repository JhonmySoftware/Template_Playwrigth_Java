# Playwright Java E2E Skeleton

Framework profesional de automatizacion E2E con **Playwright Java**, **Maven**, **TestNG** y **Page Object Model (POM)**.

La maqueta esta preparada para ejecutar pruebas reales sobre **SauceDemo** (`https://www.saucedemo.com/`) y ya incorpora:

- ejecucion `headed` para ver el navegador en local
- captura de **screenshots**
- generacion de **videos**
- trazas **Playwright Trace**
- reporte HTML clasico de Maven/TestNG
- reporte HTML de evidencias con enlaces directos a artefactos

---

## 1. Objetivo

Esta base sirve para arrancar un proyecto de automatizacion E2E con una estructura mantenible y escalable:

- separacion clara entre pruebas, paginas, configuracion y utilidades
- soporte para evidencias por prueba
- configuracion por propiedades y sobreescritura por `-D`
- compatibilidad con ejecucion local e integracion futura en CI/CD

---

## 2. Stack Tecnologico

- **Java 17**
- **Maven**
- **Playwright Java**
- **TestNG**
- **Log4j2**

---

## 3. Requisitos Previos

Antes de ejecutar el proyecto asegurese de tener instalado:

1. **JDK 17**
2. **Maven 3.9+**
3. **Git**
4. **Node.js** opcional, solo si quiere servir reportes por `http://`

Verificaciones rapidas:

```bash
java -version
mvn -version
git --version
node -v
```

---

## 4. Estructura del Proyecto

```text
src/
+-- main/java/com/example/project/
|   +-- config/
|   |   +-- FrameworkConfig.java
|   |   \-- PlaywrightFactory.java
|   +-- pages/
|   |   +-- BasePage.java
|   |   +-- InventoryPage.java
|   |   \-- LoginPage.java
|   \-- utils/
|       +-- ArtifactManager.java
|       +-- ConfigManager.java
|       +-- CsvDataReader.java
|       +-- DatabaseUtils.java
|       \-- ExecutionReportManager.java
\-- test/
    +-- java/com/example/project/tests/
    |   +-- BaseTest.java
    |   \-- LoginTest.java
    \-- resources/
        +-- config.properties
        +-- log4j2.xml
        \-- testdata/
            \-- login-credentials.csv
```

### Responsabilidad por capa

- `config`: inicializacion de Playwright, lectura de propiedades y parametros base
- `pages`: Page Objects bajo POM
- `utils`: utilidades transversales y administracion de artefactos/reportes
- `tests`: casos de prueba y ciclo de vida de ejecucion
- `resources`: configuracion y data-driven testing

---

## 5. Patrones y Convenciones Aplicadas

### Page Object Model

Cada pagina encapsula:

- locators
- acciones
- validaciones basicas de estado

Las pruebas no interactuan directamente con locators de Playwright. Consumen metodos de negocio de cada pagina.

### BaseTest

`BaseTest` centraliza:

- inicializacion de `Playwright`
- apertura y cierre de `Browser`
- creacion aislada de `BrowserContext` y `Page` por test
- screenshots
- videos
- traces
- escritura de reporte de evidencias

### Locators estables

El proyecto usa `data-test` y `getByTestId(...)` cuando el sitio lo soporta.  
En SauceDemo eso es mas estable que selectores CSS acoplados al layout.

---

## 6. Configuracion

Archivo base:

- `src/test/resources/config.properties`

Contenido actual:

```properties
browser=chromium
headless=false
slow.mo=300
default.timeout.ms=10000
navigation.timeout.ms=15000
viewport.width=1440
viewport.height=900
base.url=https://www.saucedemo.com/
test.id.attribute=data-test
trace.enabled=true
video.enabled=true
screenshot.on.failure=true
screenshot.on.success=true
artifacts.base.dir=target/artifacts
reports.base.dir=target/reports
video.width=1280
video.height=720
inventory.url.fragment=inventory.html
```

### Significado de propiedades

| Propiedad | Descripcion |
|---|---|
| `browser` | Navegador a usar: `chromium`, `firefox`, `webkit` |
| `headless` | `false` para ver el navegador, `true` para ejecucion oculta |
| `slow.mo` | Retardo en milisegundos entre acciones Playwright |
| `default.timeout.ms` | Timeout por defecto para operaciones |
| `navigation.timeout.ms` | Timeout de navegacion |
| `base.url` | URL base del AUT |
| `test.id.attribute` | Atributo usado por `getByTestId` |
| `trace.enabled` | Habilita traza Playwright |
| `video.enabled` | Habilita grabacion de video |
| `screenshot.on.failure` | Screenshot cuando la prueba falla |
| `screenshot.on.success` | Screenshot cuando la prueba pasa |
| `artifacts.base.dir` | Ruta base para evidencias |
| `reports.base.dir` | Ruta base para reportes |
| `inventory.url.fragment` | Fragmento esperado de URL tras login exitoso |

### Override por linea de comandos

Ejemplos:

```bash
mvn test -Dheadless=true
mvn test -Dbrowser=firefox
mvn test -Dslow.mo=800
mvn test -Dbase.url=https://www.saucedemo.com/
```

---

## 7. Datos de Prueba

Archivo actual:

- `src/test/resources/testdata/login-credentials.csv`

Formato:

```csv
username,password,expectedTitle
standard_user,secret_sauce,Products
problem_user,secret_sauce,Products
```

### Nota importante

`shouldLoginSuccessfully` usa `DataProvider`, por eso una sola prueba puede ejecutarse varias veces si existen multiples filas en el CSV.

---

## 8. Casos de Prueba Incluidos

### `LoginTest`

Escenarios actuales:

1. login exitoso con `standard_user`
2. login exitoso con `problem_user`
3. validacion de error con `locked_out_user`

Credenciales conocidas de SauceDemo:

- usuario valido: `standard_user`
- usuario bloqueado: `locked_out_user`
- password comun: `secret_sauce`

---

## 9. Ejecucion

### Ejecutar toda la suite

```bash
mvn clean test
```

### Ejecutar una clase

```bash
mvn test -Dtest=LoginTest
```

### Ejecutar un metodo

```bash
mvn test -Dtest=LoginTest#shouldDisplayLockedOutErrorForRestrictedUser
```

### Ejecutar en Firefox

```bash
mvn test -Dbrowser=firefox
```

### Ejecutar en modo headless

```bash
mvn test -Dheadless=true
```

---

## 10. Reportes y Evidencias

### Reporte Maven/TestNG

Salida:

- `target/reports/automation-test-report.html`

Este reporte resume ejecuciones, pero no esta pensado como visor principal de evidencias.

### Reporte de evidencias Playwright

Salida:

- `target/reports/playwright-evidence-report.html`

Incluye por cada test:

- estado
- duracion
- link a screenshot
- link a trace ZIP
- link a video
- preview inline de imagen
- reproductor de video embebido

### Artefactos por corrida

Cada corrida genera una carpeta unica:

```text
target/artifacts/run_YYYYMMDD_HHMMSS/
```

Ejemplo de contenido:

```text
screenshots/
traces/
videos/
execution-summary.html
```

---

## 11. Como Ver las Evidencias

### Opcion 1: abrir HTML local

Abra:

- `target/reports/playwright-evidence-report.html`

### Opcion 2: servir por HTTP local

Algunos navegadores limitan la reproduccion de videos desde `file://`.  
En ese caso use un servidor local:

```bash
npx serve target -l 8765
```

Luego abra:

```text
http://127.0.0.1:8765/reports/playwright-evidence-report.html
```

### Trazas Playwright

Un archivo `.zip` de traza no se abre como video ni como HTML.  
Debe abrirse con **Playwright Trace Viewer**.

Opcion recomendada:

1. abra `https://trace.playwright.dev/`
2. arrastre el archivo `.zip` de `target/artifacts/.../traces/`

Referencia oficial:

- https://playwright.dev/java/docs/trace-viewer-intro

---

## 12. Flujo de Ejecucion del Framework

1. `BaseTest` lee `config.properties`
2. `PlaywrightFactory` crea `Playwright`, `Browser` y `BrowserContext`
3. `BaseTest` crea un `Page` aislado por prueba
4. el test interactua con `LoginPage` e `InventoryPage`
5. al terminar cada prueba:
   - se guarda screenshot
   - se guarda trace ZIP
   - se guarda video
   - se actualiza el HTML de evidencias

---

## 13. Ejemplo de Extension del Framework

Para agregar una nueva pagina:

1. crear Page Object en `src/main/java/.../pages`
2. encapsular locators y acciones
3. crear clase de prueba en `src/test/java/.../tests`
4. reutilizar `BaseTest`
5. si aplica, agregar CSV/JSON en `src/test/resources`

Ejemplo de nuevas paginas candidatas para SauceDemo:

- `CartPage`
- `CheckoutPage`
- `MenuPage`
- `ProductDetailPage`

---

## 14. Buenas Practicas Recomendadas

- no poner assertions complejas dentro de los Page Objects
- no usar CSS frágil basado en estructura visual si existe `data-test`
- aislar `BrowserContext` por prueba
- mantener los datos de prueba fuera de la clase cuando el escenario sea data-driven
- centralizar timeouts y configuracion
- usar videos y traces para diagnostico, no solo screenshots

---

## 15. Troubleshooting

### No veo el navegador

Verifique:

```properties
headless=false
slow.mo=300
```

Tambien confirme que no este sobreescribiendo por CLI:

```bash
mvn test -Dheadless=false
```

### El video no abre desde el HTML

Use servidor local:

```bash
npx serve target -l 8765
```

### El trace ZIP no "se reproduce"

Es correcto.  
Abra la traza en:

- `https://trace.playwright.dev/`

### IntelliJ indica error de JVM target 17

Configure el proyecto con JDK 17 en IntelliJ y recargue Maven.

### Primera corrida tarda mucho

Es normal. Playwright puede descargar dependencias/binarios en la primera ejecucion.

---

## 16. Git y Publicacion

### Inicializar repositorio local

```bash
git init -b main
git add .
git commit -m "Initial Playwright Java E2E framework"
```

### Conectar remoto y publicar

```bash
git remote add origin <URL_DEL_REPOSITORIO>
git push -u origin main
```

Ejemplo:

```bash
git remote add origin https://github.com/usuario/repositorio.git
git push -u origin main
```

### Recomendacion

No versionar:

- `target/`
- `.idea/`
- artefactos locales
- reportes generados

Eso ya queda cubierto por `.gitignore`.

---

## 17. Estado Actual de la Maqueta

La base ya esta funcional y validada contra SauceDemo con:

- login exitoso
- usuario bloqueado
- screenshots
- videos
- traces
- reporte HTML de evidencias

---

## 18. Siguientes Pasos Recomendados

1. modelar `CartPage`
2. modelar `CheckoutPage`
3. agregar pruebas de compra end-to-end
4. integrar ejecucion en Jenkins/GitHub Actions/Azure DevOps
5. agregar lectura JSON y capas de test data mas robustas
6. agregar tags/grupos TestNG por smoke, regression y critical

---

## 19. Referencias Oficiales

- Playwright Java: https://playwright.dev/java/docs/intro
- Playwright Locators: https://playwright.dev/java/docs/locators
- Playwright Videos: https://playwright.dev/java/docs/videos
- Playwright Trace Viewer: https://playwright.dev/java/docs/trace-viewer-intro
- Maven Surefire Report Plugin: https://maven.apache.org/surefire/maven-surefire-report-plugin/report-only-mojo.html
- TestNG: https://testng.org/
