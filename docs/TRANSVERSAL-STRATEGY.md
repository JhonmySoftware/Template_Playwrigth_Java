# Estrategia Transversal Web, API y Mobile

## 1. Objetivo

Este documento define como escalar la maqueta actual hacia una plataforma de automatizacion mas transversal sin romper el modelo base.

---

## 2. Situacion Actual

Actualmente el repositorio implementa:

- Web UI testing
- evidencias por test
- configuracion centralizada
- Page Object Model
- soporte de ampliacion para nuevos canales

No implementa aun:

- API clients y API tests
- mobile web profiles
- native mobile module
- paralelismo distribuido
- CI/CD declarativo

---

## 3. Que Soporta Playwright Java Directamente

De acuerdo con la documentacion oficial:

- **Web UI**: si
- **API testing**: si, mediante `APIRequestContext`
- **Mobile web emulation**: si, mediante emulacion de device/browser behavior

Referencias:

- https://playwright.dev/java/docs/intro
- https://playwright.dev/java/docs/api-testing
- https://playwright.dev/java/docs/next/emulation

---

## 4. Que No Debe Asumirse Como Soporte Directo

La documentacion anterior describe navegadores, API HTTP y emulacion.  
A partir de eso, la siguiente conclusion es una **inferencia de arquitectura**:

- para **native mobile apps** Android/iOS, se debe integrar una tecnologia adicional y aislarla del framework web

Recomendacion:

- Appium si el objetivo es amplia compatibilidad nativa/hibrida
- Maestro si el objetivo es simplicidad operativa basada en flujos

---

## 5. Modelo Objetivo por Canal

## Web

Uso recomendado:

- journeys E2E
- smoke
- regression UI
- validaciones cross-browser

Artefactos:

- screenshot
- video
- trace

## API

Uso recomendado:

- validaciones de contratos funcionales
- setup/teardown de datos
- verificacion backend post-UI
- autenticacion tecnica

## Mobile web

Uso recomendado:

- validacion responsiva
- emulacion de viewport
- pruebas tactiles en navegacion web

## Native mobile

Uso recomendado:

- aplicaciones Android/iOS nativas o hibridas
- flujos offline
- biometria
- permisos nativos
- deep links

---

## 6. Estructura Recomendada de Evolucion

### Opcion A: un solo modulo, organizado por canal

Adecuado para equipos pequenos o medianos.

```text
src/main/java/com/example/project/
+-- core/
+-- web/
|   +-- pages/
|   \-- components/
+-- api/
|   +-- clients/
|   +-- models/
|   \-- builders/
\-- mobile/
    \-- web/
        \-- pages/

src/test/java/com/example/project/tests/
+-- web/
+-- api/
\-- mobileweb/
```

### Opcion B: multi-modulo

Adecuado cuando Web, API y Mobile tienen ciclos de vida distintos.

```text
qa-automation-parent/
+-- web-e2e/
+-- api-tests/
\-- mobile-tests/
```

Recomendacion:

- quedarse en **Opcion A** mientras el alcance sea contenido
- migrar a **Opcion B** cuando native mobile o API tengan necesidades muy distintas

---

## 7. Integracion Web + API

Una ventaja fuerte de Playwright es combinar UI y API en el mismo ecosistema.

Patrones recomendados:

1. preparar estado por API antes de abrir la UI
2. ejecutar acciones en browser
3. validar post-condicion por API
4. evitar setup costoso solo a traves de UI

Ejemplos:

- crear usuario por API y validar login web
- generar orden por API y validar reflejo UI
- borrar datos de prueba por API al finalizar

---

## 8. Integracion Mobile Web

Para mobile web no se necesitan otros motores.  
La extension natural es crear perfiles de dispositivo y contexts especializados.

Ejemplo de futuro:

- `MobileWebContextFactory`
- `MobileLoginPage`
- `tests/mobileweb/`

Casos tipicos:

- menu hamburguesa
- layout responsive
- scroll y sticky headers
- touch interactions

---

## 9. Integracion Native Mobile

Regla importante:

- **no mezclar Page Objects web con Screen Objects nativos**

Modelo recomendado:

- `mobile-tests/` como modulo separado
- pipeline propio
- stack propio
- convenciones propias

Elementos que justifican la separacion:

- manejo de drivers/emuladores
- tiempos de arranque distintos
- dependencias del sistema operativo
- estrategia de evidencias distinta

---

## 10. Estrategia de Datos y Configuracion Transversal

La configuracion debe crecer por capas:

- propiedades comunes
- propiedades por canal
- propiedades por ambiente

Ejemplo de futuro:

```text
config/
+-- common.properties
+-- web.properties
+-- api.properties
+-- mobile.properties
+-- qa.properties
+-- staging.properties
\-- prod-sandbox.properties
```

Datos de prueba:

- `CSV` para matrices simples
- `JSON` para payloads API
- builders/factories para objetos complejos

---

## 11. Pipeline Recomendado

### Etapa 1

- compile
- install browsers
- smoke web

### Etapa 2

- regression web
- api regression

### Etapa 3

- mobile web
- native mobile

---

## 12. Criterios para Escalar

Escalar a una solucion transversal solo tiene sentido si se mantiene disciplina de arquitectura.

Disparadores validos:

- mas de un canal de prueba
- mas de un equipo consumidor
- mas de un ambiente por pipeline
- necesidad de test data management
- crecimiento de la suite a cientos de casos

---

## 13. Recomendaciones Finales

- mantener `core` pequeno y estable
- aislar responsabilidades por canal
- no acoplar API tests a Page Objects
- separar native mobile si aparece la necesidad real
- usar la misma estrategia de evidencias en todos los canales, aunque no necesariamente el mismo mecanismo
