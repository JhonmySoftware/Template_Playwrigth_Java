# CI/CD, GitHub Actions y Sonar

## 1. Objetivo

Este documento describe el pipeline base del proyecto y como conectarlo con SonarQube Server o SonarQube Cloud.

---

## 2. Pipeline Incluido

Archivo:

- `.github/workflows/ci.yml`

El workflow hace lo siguiente:

1. clona el repositorio con historial completo
2. instala Java 17
3. usa cache Maven
4. compila el proyecto con Maven Wrapper
5. instala navegadores Playwright para CI
6. ejecuta pruebas en `chromium` y `headless=true`
7. genera:
   - reportes Maven/TestNG
   - reportes JaCoCo
   - artefactos Playwright
8. publica artefactos en GitHub Actions
9. ejecuta Sonar si el repositorio tiene las variables y secretos necesarios

---

## 3. Por que el pipeline corre en headless

Playwright ejecuta browsers en modo headless por defecto y es la modalidad recomendada en CI.  
La documentacion oficial indica que para CI se deben instalar dependencias de browser y luego correr las pruebas normalmente.  
Referencia oficial:

- https://playwright.dev/java/docs/ci
- https://playwright.dev/java/docs/intro

En este proyecto, CI fuerza:

- `-Dbrowser=chromium`
- `-Dheadless=true`
- `-Dslow.mo=0`

---

## 4. Artefactos que Publica GitHub Actions

### `reports`

Incluye:

- `target/reports/`
- `target/surefire-reports/`
- `target/site/jacoco/`

### `playwright-artifacts`

Incluye:

- `target/artifacts/`

Eso permite descargar:

- screenshots
- videos
- traces
- reportes HTML

---

## 5. SonarQube y SonarCloud

### Estrategia implementada

Se usa **SonarScanner for Maven**, que SonarSource recomienda como scanner por defecto para proyectos Maven.

Referencia oficial:

- https://docs.sonarsource.com/sonarqube-server/latest/analyzing-source-code/scanners/sonarscanner-for-maven/

El pipeline ejecuta Sonar **solo si encuentra configuracion suficiente**.  
Si no encuentra variables/secrets, la etapa se omite sin romper el build.

---

## 6. Variables y Secrets Requeridos

### Variables de repositorio

Configurar en GitHub:

`Settings > Secrets and variables > Actions > Variables`

Variables:

- `SONAR_HOST_URL`
- `SONAR_PROJECT_KEY`
- `SONAR_ORGANIZATION` opcional, solo si usas SonarQube Cloud

Ejemplos:

### SonarQube Server

- `SONAR_HOST_URL=https://sonar.miempresa.com`
- `SONAR_PROJECT_KEY=template_playwright_java`

### SonarQube Cloud

- `SONAR_HOST_URL=https://sonarcloud.io`
- `SONAR_PROJECT_KEY=JhonmySoftware_Template_Playwrigth_Java`
- `SONAR_ORGANIZATION=jhonmysoftware`

### Secret de repositorio

Configurar en:

`Settings > Secrets and variables > Actions > Secrets`

Secret:

- `SONAR_TOKEN`

---

## 7. Cobertura JaCoCo

El proyecto ahora genera reporte de cobertura usando:

- `jacoco-maven-plugin`

Salida:

- `target/site/jacoco/jacoco.xml`

Ese XML es consumido por Sonar mediante:

- `sonar.coverage.jacoco.xmlReportPaths=target/site/jacoco/jacoco.xml`

Importante:

La cobertura mide el **codigo del framework** versionado en este repositorio, no la aplicacion bajo prueba.

---

## 8. Branches y Eventos

El workflow corre en:

- `push` a `main`
- `pull_request` hacia `main`
- `workflow_dispatch`

Ademas usa `concurrency` para cancelar ejecuciones anteriores de la misma rama cuando entra un commit nuevo.

---

## 9. Flujo Recomendado para Pull Requests

1. crear rama feature
2. abrir PR hacia `main`
3. dejar que GitHub Actions:
   - compile
   - ejecute pruebas
   - publique artefactos
   - haga analisis Sonar si aplica
4. revisar:
   - logs
   - artefactos
   - issues de Sonar
   - quality gate

---

## 10. Como Ver Artefactos en GitHub Actions

1. abrir la pestaña `Actions`
2. abrir una ejecucion
3. bajar a la seccion `Artifacts`
4. descargar:
   - `reports`
   - `playwright-artifacts`

Los traces `.zip` pueden abrirse luego en:

- https://trace.playwright.dev/

---

## 11. Ajustes Futuros Recomendados

### Ambientes

Agregar matrices o variables por ambiente:

- `qa`
- `staging`
- `prod-sandbox`

### Navegadores

Agregar matrix:

- chromium
- firefox
- webkit

### Etapas separadas

Separar:

- `compile`
- `test`
- `sonar`
- `package`

### Quality gates

Actualmente el pipeline usa:

- `-Dsonar.qualitygate.wait=true`

Eso permite que el job espere el resultado del quality gate.

---

## 12. Troubleshooting

### Sonar no corre

Revise si existen:

- `SONAR_HOST_URL`
- `SONAR_PROJECT_KEY`
- `SONAR_TOKEN`

### Sonar corre pero falla

Revise:

- URL correcta
- token valido
- permisos del proyecto en Sonar
- `SONAR_ORGANIZATION` si usa SonarQube Cloud

### Playwright falla en CI

Revise:

- instalacion de browsers
- uso de `install --with-deps`
- artefactos generados en `target/artifacts`

### El job tarda mucho

Es normal en la primera corrida por descarga de dependencias y browsers.

---

## 13. Referencias Oficiales

- GitHub Actions Maven: https://docs.github.com/actions/guides/building-and-testing-java-with-maven
- Playwright Java CI intro: https://playwright.dev/java/docs/ci-intro
- Playwright Java CI: https://playwright.dev/java/docs/ci
- SonarScanner for Maven: https://docs.sonarsource.com/sonarqube-server/latest/analyzing-source-code/scanners/sonarscanner-for-maven/
- SonarQube Cloud GitHub Actions: https://docs.sonarsource.com/sonarqube-cloud/advanced-setup/ci-based-analysis/github-actions-for-sonarcloud
