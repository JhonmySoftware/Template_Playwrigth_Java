# Template Playwright Java

Framework base de automatizacion transversal para QA con **Playwright Java**, **Maven**, **TestNG** y **Page Object Model (POM)**.

El proyecto nace con foco en **Web E2E**, pero esta documentado y preparado para evolucionar a una plataforma de automatizacion mas amplia que cubra:

- **Web UI**
- **API / integration testing**
- **Mobile web emulation**
- **Native mobile automation** como extension separada

Estado actual del repositorio:

- Web E2E funcional sobre `https://www.saucedemo.com/`
- Reportes HTML
- Screenshots, videos y traces por prueba
- Data-driven testing con CSV
- Maven Wrapper para clonar y ejecutar en otros equipos sin instalar Maven manualmente

---

## 1. Documentacion Disponible

Este `README` es la entrada principal. El detalle tecnico y operativo se separa en `docs/`.

- [Arquitectura](docs/ARCHITECTURE.md)
- [CI/CD, GitHub Actions y Sonar](docs/CI-CD.md)
- [Instalacion y clonacion en otros equipos](docs/SETUP-NEW-MACHINE.md)
- [Estrategia transversal Web/API/Mobile](docs/TRANSVERSAL-STRATEGY.md)

---

## 2. Alcance Actual y Alcance Objetivo

### Alcance actual

- Framework **Web UI** con Playwright Java
- Pruebas sobre navegador `chromium`
- Soporte para `firefox` y `webkit` por configuracion
- Evidencias automaticas:
  - screenshot
  - video
  - trace zip
- Reporte HTML de evidencias
- Reporte Maven/TestNG

### Alcance objetivo

El diseno contempla escalar a una fabrica de pruebas mas transversal:

- Web UI
- API
- Mobile web
- Native mobile
- Integracion CI/CD
- Ejecucion por ambientes
- Etiquetado por smoke, regression, critical

Importante:

- **Playwright Java soporta Web UI, API testing y emulacion de dispositivos moviles en navegador** segun su documentacion oficial.
- **Automatizacion nativa mobile** no forma parte de la capacidad directa de este esqueleto. Para esa capa se recomienda una integracion adicional, por ejemplo con Appium o Maestro, en un modulo separado.  
  Esto es una **inferencia de arquitectura** basada en el alcance documentado de Playwright Java.

Referencias oficiales:

- Playwright Java intro: https://playwright.dev/java/docs/intro
- Playwright Java API testing: https://playwright.dev/java/docs/api-testing
- Playwright Java browsers: https://playwright.dev/java/docs/browsers
- Playwright Java emulation: https://playwright.dev/java/docs/next/emulation

---

## 3. Stack Tecnologico

- Java 17
- Maven 3.9.x
- Maven Wrapper
- Playwright Java
- TestNG
- Log4j2
- Git
- Node.js opcional para servir reportes por `http://`

---

## 4. Estructura del Proyecto

```text
.
+-- .mvn/
|   \-- wrapper/
|       \-- maven-wrapper.properties
+-- docs/
|   +-- ARCHITECTURE.md
|   +-- SETUP-NEW-MACHINE.md
|   \-- TRANSVERSAL-STRATEGY.md
+-- src/
|   +-- main/java/com/example/project/
|   |   +-- config/
|   |   |   +-- FrameworkConfig.java
|   |   |   \-- PlaywrightFactory.java
|   |   +-- pages/
|   |   |   +-- BasePage.java
|   |   |   +-- InventoryPage.java
|   |   |   \-- LoginPage.java
|   |   \-- utils/
|   |       +-- ArtifactManager.java
|   |       +-- ConfigManager.java
|   |       +-- CsvDataReader.java
|   |       +-- DatabaseUtils.java
|   |       \-- ExecutionReportManager.java
|   \-- test/
|       +-- java/com/example/project/tests/
|       |   +-- BaseTest.java
|       |   \-- LoginTest.java
|       \-- resources/
|           +-- config.properties
|           +-- log4j2.xml
|           \-- testdata/
|               \-- login-credentials.csv
+-- .gitignore
+-- mvnw
+-- mvnw.cmd
+-- pom.xml
\-- README.md
```

---

## 5. Inicio Rapido

### Windows PowerShell

```powershell
git clone https://github.com/JhonmySoftware/Template_Playwrigth_Java.git
cd Template_Playwrigth_Java
.\mvnw.cmd -q -DskipTests compile
.\mvnw.cmd exec:java -e -Dexec.mainClass=com.microsoft.playwright.CLI -Dexec.args="install"
.\mvnw.cmd test
```

### Linux / macOS / Git Bash

```bash
git clone https://github.com/JhonmySoftware/Template_Playwrigth_Java.git
cd Template_Playwrigth_Java
chmod +x mvnw
./mvnw -q -DskipTests compile
./mvnw exec:java -e -Dexec.mainClass=com.microsoft.playwright.CLI -Dexec.args="install"
./mvnw test
```

Notas:

- `mvnw` y `mvnw.cmd` permiten usar Maven Wrapper.
- El comando `install` de Playwright instala los navegadores requeridos por la version actual del framework, tal como indica la documentacion oficial de Playwright Java: https://playwright.dev/java/docs/browsers

---

## 6. Como Clonar y Usar en Otros Equipos

La guia detallada esta en:

- [docs/SETUP-NEW-MACHINE.md](docs/SETUP-NEW-MACHINE.md)

Resumen corto:

1. instalar JDK 17
2. clonar el repositorio
3. usar Maven Wrapper
4. instalar navegadores Playwright
5. ejecutar una prueba de smoke
6. abrir el reporte de evidencias

Smoke recomendado:

### Windows

```powershell
.\mvnw.cmd test -Dtest=LoginTest#shouldDisplayLockedOutErrorForRestrictedUser
```

### Linux / macOS

```bash
./mvnw test -Dtest=LoginTest#shouldDisplayLockedOutErrorForRestrictedUser
```

---

## 7. Configuracion

Archivo principal:

- [config.properties](src/test/resources/config.properties)

Configuracion actual:

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

Overrides comunes:

```bash
./mvnw test -Dbrowser=firefox
./mvnw test -Dheadless=true
./mvnw test -Dslow.mo=800
./mvnw test -Dbase.url=https://mi-app.ejemplo.com
```

---

## 8. Ejecucion de Pruebas

### Ejecutar toda la suite

```bash
./mvnw clean test
```

### Ejecutar una clase

```bash
./mvnw test -Dtest=LoginTest
```

### Ejecutar un metodo

```bash
./mvnw test -Dtest=LoginTest#shouldLoginSuccessfully
```

### Ejecutar en otro navegador

```bash
./mvnw test -Dbrowser=webkit
```

### Ejecutar en modo oculto

```bash
./mvnw test -Dheadless=true
```

### Ejecutar con navegador visible y mas lento

```bash
./mvnw test -Dheadless=false -Dslow.mo=900
```

---

## 9. Reportes y Evidencias

### Reporte Maven/TestNG

- `target/reports/automation-test-report.html`

### Reporte HTML de evidencias

- `target/reports/playwright-evidence-report.html`

### Artefactos por corrida

```text
target/artifacts/run_YYYYMMDD_HHMMSS/
```

Cada corrida guarda:

- `screenshots/`
- `videos/`
- `traces/`
- `execution-summary.html`

### Como abrir el reporte correctamente

Si el navegador bloquea recursos locales `file://`, sirvalo por HTTP:

```bash
npx serve target -l 8765
```

Luego abra:

```text
http://127.0.0.1:8765/reports/playwright-evidence-report.html
```

### Como abrir un trace

Opcion recomendada:

1. abrir `https://trace.playwright.dev/`
2. arrastrar el `.zip` generado

Opcion CLI:

```bash
./mvnw exec:java -e -Dexec.mainClass=com.microsoft.playwright.CLI -Dexec.args="show-trace target/artifacts/<run>/traces/<archivo>.zip"
```

---

## 10. Datos de Prueba

Archivo actual:

- [login-credentials.csv](src/test/resources/testdata/login-credentials.csv)

Contenido:

```csv
username,password,expectedTitle
standard_user,secret_sauce,Products
problem_user,secret_sauce,Products
```

La prueba `shouldLoginSuccessfully` usa `DataProvider`, por lo tanto ejecuta una iteracion por cada fila del CSV.

---

## 11. Casos de Prueba Actuales

### LoginTest

- login exitoso con `standard_user`
- login exitoso con `problem_user`
- validacion de usuario bloqueado con `locked_out_user`

Sistema bajo prueba:

- `https://www.saucedemo.com/`

---

## 12. Estrategia de Evolucion

Este repositorio **no debe quedarse solo como prueba de login**. La estrategia documentada es evolucionarlo en capas.

### Fase 1

- estabilizar Web UI
- ampliar Page Objects
- cubrir smoke y regression

### Fase 2

- agregar pruebas API
- usar API para setup y teardown de datos
- validar post-condiciones server-side despues de acciones UI

### Fase 3

- agregar mobile web emulation
- ejecutar suites responsivas y por device profile

### Fase 4

- separar un modulo para native mobile si el producto lo exige

Detalle tecnico:

- [docs/TRANSVERSAL-STRATEGY.md](docs/TRANSVERSAL-STRATEGY.md)

---

## 13. Uso Transversal Web, API y Mobile

### Web UI

Es la capacidad principal actual.

### API

Playwright Java expone `APIRequestContext`, por lo que este mismo ecosistema puede cubrir:

- pruebas puras de API
- preparacion de datos de prueba
- verificacion de estado backend
- login via API y reutilizacion de `storageState`

Referencia oficial:

- https://playwright.dev/java/docs/api-testing

### Mobile web

Playwright soporta emulacion de dispositivos moviles en navegador:

- viewport
- user agent
- touch
- screen size

Referencia oficial:

- https://playwright.dev/java/docs/next/emulation

### Native mobile

Para Android/iOS nativo, la recomendacion de arquitectura es **separar modulo o suite**.  
No mezclar automation nativa dentro de los mismos Page Objects web.

---

## 14. Documentacion Tecnica Detallada

### Arquitectura interna

- [docs/ARCHITECTURE.md](docs/ARCHITECTURE.md)

Incluye:

- flujo de ejecucion completo
- responsabilidades por paquete
- ciclo de vida de Playwright
- estrategia de artefactos
- convenciones de nombres
- guia para extender el framework

### Instalacion en maquinas nuevas

- [docs/SETUP-NEW-MACHINE.md](docs/SETUP-NEW-MACHINE.md)

Incluye:

- JDK
- Git
- Maven Wrapper
- Playwright browsers
- proxy corporativo
- troubleshooting por sistema

### Estrategia transversal

- [docs/TRANSVERSAL-STRATEGY.md](docs/TRANSVERSAL-STRATEGY.md)

Incluye:

- modelo objetivo web/api/mobile
- limites actuales del framework
- propuesta de carpetas y modulos futuros
- criterios para decidir entre monolito y multi-modulo

---

## 15. Git y Colaboracion

### Clonar

```bash
git clone https://github.com/JhonmySoftware/Template_Playwrigth_Java.git
cd Template_Playwrigth_Java
```

### Crear rama de trabajo

```bash
git checkout -b feature/nombre-cambio
```

### Flujo minimo

```bash
git add .
git commit -m "Describe el cambio"
git push -u origin feature/nombre-cambio
```

### Actualizar rama local

```bash
git pull --rebase origin main
```

---

## 16. CI/CD y Sonar

El repositorio ya incluye pipeline base en:

- `.github/workflows/ci.yml`

Capacidades incluidas:

- build en GitHub Actions
- pruebas Playwright en CI
- publicacion de artefactos
- reporte JaCoCo
- integracion condicional con SonarQube / SonarCloud

Documentacion detallada:

- [docs/CI-CD.md](docs/CI-CD.md)

---

## 17. Problemas Frecuentes

### No veo el navegador

Verifique:

```properties
headless=false
slow.mo=300
```

### No se reproducen videos desde el HTML

Use un servidor local:

```bash
npx serve target -l 8765
```

### El trace ZIP no se abre como video

Es correcto. Debe abrirse con Playwright Trace Viewer:

- https://trace.playwright.dev/

### La primera corrida tarda mucho

Es normal si Playwright esta descargando navegadores compatibles.

### Tengo proxy corporativo

Use la estrategia documentada en:

- [docs/SETUP-NEW-MACHINE.md](docs/SETUP-NEW-MACHINE.md)

Playwright documenta el uso de `HTTPS_PROXY` y `PLAYWRIGHT_DOWNLOAD_HOST` para instalaciones restringidas:

- https://playwright.dev/java/docs/browsers

---

## 18. Referencias Oficiales

- Playwright Java intro: https://playwright.dev/java/docs/intro
- Playwright Java writing tests: https://playwright.dev/java/docs/writing-tests
- Playwright Java running tests: https://playwright.dev/java/docs/running-tests
- Playwright Java locators: https://playwright.dev/java/docs/locators
- Playwright Java browsers: https://playwright.dev/java/docs/browsers
- Playwright Java API testing: https://playwright.dev/java/docs/api-testing
- Playwright Java emulation: https://playwright.dev/java/docs/next/emulation
- Playwright Java trace viewer: https://playwright.dev/java/docs/trace-viewer-intro
- Maven Surefire Report Plugin: https://maven.apache.org/surefire/maven-surefire-report-plugin/report-only-mojo.html
- TestNG: https://testng.org/
