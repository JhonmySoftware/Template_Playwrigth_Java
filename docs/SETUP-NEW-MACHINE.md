# Instalacion y Clonacion en Otros Equipos

## 1. Objetivo

Esta guia describe como dejar operativo el framework en una maquina nueva, ya sea de un QA, un desarrollador, un lider tecnico o un agente de CI.

---

## 2. Requisitos Minimos

- Git
- JDK 17
- acceso a internet o repositorio interno para dependencias Maven
- acceso a descarga de navegadores Playwright o mirror corporativo

No es obligatorio instalar Maven manualmente porque el proyecto ya incluye:

- `mvnw`
- `mvnw.cmd`

---

## 3. Clonar el Repositorio

### HTTPS

```bash
git clone https://github.com/JhonmySoftware/Template_Playwrigth_Java.git
cd Template_Playwrigth_Java
```

### SSH

```bash
git clone git@github.com:JhonmySoftware/Template_Playwrigth_Java.git
cd Template_Playwrigth_Java
```

---

## 4. Validar Java

```bash
java -version
```

Debe apuntar a Java 17.  
Si el equipo tiene multiples JDK, confirme tambien:

```bash
echo %JAVA_HOME%
```

o en bash:

```bash
echo $JAVA_HOME
```

---

## 5. Preparar el Proyecto

### Windows PowerShell

```powershell
.\mvnw.cmd -q -DskipTests compile
```

### Linux / macOS / Git Bash

```bash
chmod +x mvnw
./mvnw -q -DskipTests compile
```

Esto valida:

- descarga de dependencias Maven
- compilacion del framework
- lectura correcta del `pom.xml`

---

## 6. Instalar Navegadores Playwright

Playwright Java requiere instalar binarios de navegador compatibles con la version usada por el proyecto.

Segun la documentacion oficial:

- https://playwright.dev/java/docs/browsers

### Instalar navegadores por defecto

#### Windows

```powershell
.\mvnw.cmd exec:java -e -Dexec.mainClass=com.microsoft.playwright.CLI -Dexec.args="install"
```

#### Linux / macOS

```bash
./mvnw exec:java -e -Dexec.mainClass=com.microsoft.playwright.CLI -Dexec.args="install"
```

### Instalar un navegador especifico

```bash
./mvnw exec:java -e -Dexec.mainClass=com.microsoft.playwright.CLI -Dexec.args="install chromium"
```

---

## 7. Primera Ejecucion Recomendada

Prueba corta:

### Windows

```powershell
.\mvnw.cmd test -Dtest=LoginTest#shouldDisplayLockedOutErrorForRestrictedUser
```

### Linux / macOS

```bash
./mvnw test -Dtest=LoginTest#shouldDisplayLockedOutErrorForRestrictedUser
```

Esperado:

- compila
- abre navegador
- ejecuta el login sobre SauceDemo
- genera evidencias en `target/`

---

## 8. Ver Evidencias

### HTML local

Abra:

- `target/reports/playwright-evidence-report.html`

### Servidor HTTP local

Si el navegador bloquea recursos locales:

```bash
npx serve target -l 8765
```

Y abra:

```text
http://127.0.0.1:8765/reports/playwright-evidence-report.html
```

---

## 9. Configuracion de IntelliJ IDEA

1. abrir el proyecto como Maven
2. configurar SDK del proyecto en Java 17
3. recargar Maven
4. ejecutar `LoginTest`

Si IntelliJ marca un error de target JVM 17:

- revise `Project SDK`
- revise `Project language level`
- confirme que el SDK seleccionado no sea Java 8

---

## 10. Proxy Corporativo / Firewall

Playwright documenta soporte para proxy y host de descarga custom:

- `HTTPS_PROXY`
- `PLAYWRIGHT_DOWNLOAD_HOST`
- `PLAYWRIGHT_BROWSERS_PATH`

Referencia oficial:

- https://playwright.dev/java/docs/browsers

### Ejemplo PowerShell

```powershell
$Env:HTTPS_PROXY="https://proxy.miempresa.local:8080"
.\mvnw.cmd exec:java -e -Dexec.mainClass=com.microsoft.playwright.CLI -Dexec.args="install"
```

### Ejemplo con cache compartida de navegadores

```powershell
$Env:PLAYWRIGHT_BROWSERS_PATH="$Env:USERPROFILE\\pw-browsers"
.\mvnw.cmd exec:java -e -Dexec.mainClass=com.microsoft.playwright.CLI -Dexec.args="install"
.\mvnw.cmd test
```

---

## 11. Actualizar el Proyecto en una Maquina Ya Configurada

```bash
git pull --rebase origin main
./mvnw -q -DskipTests compile
./mvnw test
```

Si se actualiza la version de Playwright, puede ser necesario reinstalar navegadores:

```bash
./mvnw exec:java -e -Dexec.mainClass=com.microsoft.playwright.CLI -Dexec.args="install"
```

---

## 12. Checklist de Onboarding

- [ ] Git instalado
- [ ] Java 17 instalado
- [ ] repositorio clonado
- [ ] `mvnw` ejecuta compilacion
- [ ] navegadores Playwright instalados
- [ ] prueba smoke ejecutada
- [ ] reporte HTML abierto correctamente
- [ ] videos / screenshots / traces visibles
