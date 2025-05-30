# Documentación del Proyecto eCommerce Microservices

## 1. Introducción

### 1.1 Objetivo del Taller

Este taller tiene como propósito guiar el desarrollo de una arquitectura de microservicios robusta y escalable para un sistema de comercio electrónico, aplicando las mejores prácticas de ingeniería de software. El proyecto busca no solo implementar la solución técnica, sino también fomentar la comprensión profunda de los principios de microservicios, DevOps, automatización y calidad de software. Los objetivos principales son:

- **Diseñar una arquitectura desacoplada y escalable**, donde cada microservicio pueda evolucionar y desplegarse de forma independiente, facilitando la innovación y la resiliencia.
- **Automatizar el ciclo de vida del software** mediante pipelines CI/CD con Jenkins, asegurando entregas rápidas, seguras y repetibles.
- **Garantizar portabilidad y consistencia** usando Docker para la containerización de servicios, permitiendo que el entorno de desarrollo, pruebas y producción sean equivalentes.
- **Orquestar y gestionar servicios en producción** con Kubernetes, asegurando alta disponibilidad, autoescalado y despliegues controlados.
- **Implementar un enfoque integral de testing**, cubriendo desde pruebas unitarias hasta pruebas de rendimiento, para asegurar la calidad y confiabilidad del sistema.
- **Asegurar observabilidad y trazabilidad** con herramientas de monitoreo distribuido, facilitando la detección y resolución de problemas en entornos complejos.
- **Centralizar la gestión de configuración** para mantener la coherencia y flexibilidad en todos los entornos.

### 1.2 Microservicios Seleccionados y Justificación

La arquitectura está compuesta por **10 microservicios principales** y **3 servicios de infraestructura**, cada uno diseñado para cumplir una responsabilidad específica dentro del ecosistema de comercio electrónico:

#### Microservicios de Negocio

| Microservicio | Puerto | Justificación |
|---------------|--------|---------------|
| **user-service** | 8700 | Gestiona toda la lógica relacionada con usuarios (registro, autenticación, perfiles). Separado para permitir escalabilidad independiente y seguridad especializada. |
| **product-service** | 8500 | Maneja el catálogo de productos, inventario y búsquedas. Crucial para el rendimiento ya que es uno de los servicios más consultados. |
| **order-service** | 8300 | Procesa y gestiona pedidos. Requiere alta disponibilidad y consistencia de datos, por lo que se beneficia de estar aislado. |
| **payment-service** | 8400 | Procesa transacciones financieras. Separado por seguridad, cumplimiento PCI-DSS y para permitir integración con múltiples gateways de pago. |
| **shipping-service** | 8600 | Gestiona envíos y logística. Permite integración independiente con diferentes proveedores de transporte. |
| **favourite-service** | 8800 | Maneja listas de favoritos de usuarios. Microservicio ligero que puede escalar independientemente según el uso. |

#### Servicios de Infraestructura

| Servicio | Puerto | Justificación |
|----------|--------|---------------|
| **service-discovery** (Eureka) | 8761 | Registro y descubrimiento automático de servicios. Fundamental para la comunicación dinámica entre microservicios. |
| **cloud-config** | 9296 | Configuración centralizada externa. Permite cambios de configuración sin redespliegue y mantiene consistencia entre entornos. |
| **api-gateway** | - | Punto de entrada único para clientes. Proporciona routing, load balancing, autenticación y rate limiting. |

#### Servicios de Soporte

| Servicio | Puerto | Justificación |
|----------|--------|---------------|
| **proxy-client** | - | Cliente proxy para comunicación entre servicios. Abstrae la comunicación HTTP y maneja circuit breakers. |
| **zipkin** | 9411 | Trazabilidad distribuida. Esencial para debugging y monitoring de requests que atraviesan múltiples servicios. |

### 1.3 Herramientas y Tecnologías Utilizadas

#### 🔧 Desarrollo y Framework
- **Spring Boot**: Framework principal para el desarrollo de microservicios Java

- **Maven**: Gestión de dependencias y construcción de proyectos
- **Java 11**: Versión LTS para estabilidad y soporte a largo plazo

#### DevOps y CI/CD
- **Jenkins**: Orquestación de pipelines de CI/CD con stages automatizados
  - Construcción y empaquetado con Maven
  - Ejecución de pruebas automatizadas (unitarias, integración, E2E)
  - Construcción y push de imágenes Docker
  - Despliegue automatizado en Kubernetes
- **Git**: Control de versiones con branching strategy (dev, release, master)

####  Containerización y Orquestación
- **Docker**: Containerización de todos los servicios para garantizar portabilidad
- **Docker Hub**: Registro de imágenes para distribución
- **Kubernetes**: Orquestación de contenedores con manifests YAML
  - Deployments, Services, ConfigMaps
  - Health checks y rolling updates
  - Namespace isolation (`ecommerce`)

####  Testing y Calidad
- **JUnit**: Pruebas unitarias para lógica de negocio
- **Spring Boot Test**: Pruebas de integración con contexto de aplicación
- **Locust**: Herramienta de testing de performance y estrés
  - Pruebas de carga con 10 usuarios concurrentes
  - Pruebas de estrés con 50 usuarios concurrentes
  - Generación de reportes CSV con métricas detalladas
- **Testcontainers**: Testing de integración con contenedores reales



Esta arquitectura robusta permite escalabilidad horizontal, fault tolerance, y deployment independiente de cada servicio, cumpliendo con los principios fundamentales de microservicios y las mejores prácticas de la industria.

## 2. Arquitectura General

### 2.1 Diagrama de Arquitectura de Microservicios

El siguiente diagrama ilustra la arquitectura lógica del sistema, mostrando la interacción entre microservicios, servicios de infraestructura y componentes de soporte. Esta visión global facilita la comprensión de los flujos de datos, la resiliencia y la escalabilidad del sistema.

![Diagrama de Arquitectura](app-architecture.drawio.png)

### 2.2 Interacciones entre los Servicios

- **API Gateway:** Punto de entrada único, gestiona autenticación, balanceo de carga y enrutamiento inteligente.
- **Service Discovery (Eureka):** Permite el registro y descubrimiento dinámico de servicios, facilitando la escalabilidad y tolerancia a fallos.
- **Cloud Config:** Centraliza la configuración, permitiendo cambios sin redespliegue y manteniendo la coherencia entre entornos.
- **Proxy Client:** Abstrae la comunicación HTTP y gestiona circuit breakers, mejorando la resiliencia.
- **Zipkin:** Proporciona trazabilidad distribuida, esencial para el monitoreo y la depuración de flujos complejos.
- **Microservicios de negocio:** Interactúan mediante llamadas HTTP internas, gestionadas por el API Gateway y el Service Discovery, asegurando independencia y escalabilidad.

### 2.3 Ambientes Definidos

El ciclo de vida del software se gestiona a través de tres ambientes principales:

- **dev:** Espacio seguro para experimentación y desarrollo individual.
- **stage:** Entorno de integración y pruebas end-to-end, simula condiciones de producción.
- **master (producción):** Entorno estable y seguro, donde se ejecuta la versión aprobada del sistema.

Cada ambiente cuenta con configuración propia y pipelines de CI/CD dedicados, permitiendo despliegues independientes y controlados.

## 3. Configuración del Entorno

### 3.1 Jenkins (Ejecución Local en Windows con UI)

Jenkins es el motor de automatización que orquesta la construcción, pruebas y despliegue del sistema. Su instalación y configuración en Windows es sencilla y permite gestionar pipelines visualmente desde la interfaz web.

- **Instalación:** Descarga desde [jenkins.io/download](https://www.jenkins.io/download/), instalación asistida y acceso vía navegador.
- **Desbloqueo inicial:** Uso de la clave generada automáticamente para el primer acceso.
- **Plugins recomendados:** Docker Pipeline, Git, Blue Ocean (UI avanzada).
- **Gestión de pipelines:** Creación, monitoreo y parametrización de jobs desde la UI.
- **Pipeline declarativa:** El `Jenkinsfile` define el flujo CI/CD, asegurando reproducibilidad y control de versiones.
- **Despliegue automatizado:** Desde la compilación hasta el despliegue en Kubernetes, todo el ciclo está automatizado y versionado.

**Buenas prácticas:**
- Configurar credenciales y variables sensibles desde la UI de Jenkins.
- Asegurar permisos adecuados para la ejecución de Docker y comandos de despliegue.

### 3.2 Docker y los Dockerfiles de los Microservicios

Docker permite encapsular cada microservicio en una imagen ligera y portable, asegurando que el entorno de ejecución sea idéntico en desarrollo, pruebas y producción.

- **Dockerfile por microservicio:** Define la base, dependencias, variables de entorno y comando de arranque.
- **Imágenes optimizadas:** Uso de imágenes base ligeras y build eficientes.
- **Orquestación local:** Docker Compose permite levantar el sistema completo para pruebas integradas.
- **Automatización en pipeline:** Jenkins construye y publica las imágenes en cada ciclo, garantizando trazabilidad y control de versiones.

### 3.3 Kubernetes

Kubernetes es la plataforma de orquestación que gestiona el ciclo de vida de los contenedores en producción y pruebas.

- **Manifiestos YAML:** Definen deployments, servicios, ConfigMaps y otros recursos.
- **Namespace dedicado:** Aislamiento lógico para el sistema eCommerce.
- **Comandos clave:**
  - `kubectl apply -f k8s/` para desplegar todos los recursos.
  - `kubectl get pods -n ecommerce` y `kubectl get svc -n ecommerce` para monitoreo.
  - `kubectl logs <nombre-del-pod> -n ecommerce` para depuración.
- **Despliegue automatizado:** El pipeline aplica los manifiestos tras la construcción y publicación de imágenes.

### 3.4 Repositorio y Estructura de Carpetas

El repositorio está cuidadosamente organizado para reflejar la separación de responsabilidades y facilitar la navegación y el mantenimiento:

```
ecommerce-microservice-backend-app/
│
├── api-gateway/           # Microservicio API Gateway (con su Dockerfile)
├── cloud-config/          # Servicio de configuración centralizada (con su Dockerfile)
├── favourite-service/     # Microservicio de favoritos (con su Dockerfile)
├── order-service/         # Microservicio de pedidos (con su Dockerfile)
├── payment-service/       # Microservicio de pagos (con su Dockerfile)
├── product-service/       # Microservicio de productos (con su Dockerfile)
├── proxy-client/          # Cliente proxy para comunicación entre servicios (con su Dockerfile)
├── service-discovery/     # Servicio Eureka para descubrimiento (con su Dockerfile)
├── shipping-service/      # Microservicio de envíos (con su Dockerfile)
├── user-service/          # Microservicio de usuarios (con su Dockerfile)
├── zipkin/                # Servicio de trazabilidad distribuida (con su Dockerfile)
│
├── k8s/                   # Manifiestos de Kubernetes
├── locust/                # Pruebas de carga y rendimiento con Locust
├── Jenkinsfile            # Pipeline de CI/CD
├── compose.yml            # Orquestación local con Docker Compose
├── DOCUMENTATION.md       # Documentación del proyecto
├── README.md              # Instrucciones rápidas
└── ...otros archivos
```

Esta estructura promueve la modularidad, la escalabilidad y la facilidad de mantenimiento, permitiendo que cada equipo o desarrollador trabaje de forma independiente en su microservicio, sin interferir con el resto del sistema.

---

## 4. Pipelines de Integración y Entrega Continua

La automatización del ciclo de vida del software es clave para la agilidad y la calidad. En este proyecto, una pipeline declarativa en Jenkins gestiona de forma inteligente los flujos de integración y despliegue para las ramas principales (`dev`, `stage`, `master`). Esto asegura que cada entorno reciba el nivel de validación y control adecuado, minimizando riesgos y acelerando la entrega de valor.

### 4.1 Pipeline para entorno de desarrollo (`dev`)

El entorno de desarrollo está orientado a la experimentación y validación rápida de cambios. Cada push o pull request a la rama `dev` dispara la pipeline, que ejecuta:

1. Compilación y análisis estático del código para detectar errores tempranos.
2. Pruebas unitarias y de integración para validar la lógica y la interacción interna.
3. Construcción de imágenes Docker etiquetadas como `dev`.
4. Publicación de imágenes en el registro.
5. Despliegue automático en el entorno de desarrollo de Kubernetes.

Esto permite a los desarrolladores recibir feedback inmediato y trabajar en un entorno lo más parecido posible a producción.

**Fragmento relevante del Jenkinsfile:**
```groovy
pipeline {
    agent any
    stages {
        stage('Build & Test') {
            steps {
                sh './mvnw clean package'
            }
        }
        stage('Docker Build & Push') {
            steps {
                sh 'docker build -t usuario/user-service:dev ./user-service'
                sh 'docker push usuario/user-service:dev'
                // ...repetir para cada microservicio
            }
        }
        stage('Deploy to Dev') {
            when { branch 'dev' }
            steps {
                sh 'kubectl apply -f k8s/'
            }
        }
    }
}
```
**Diferenciación:**
- El stage `Deploy to Dev` solo se ejecuta si la rama activa es `dev`.
- Las imágenes se etiquetan como `dev` y se despliegan en el namespace de desarrollo.

---

### 4.2 Pipeline para entorno de staging (`stage`)

El entorno de staging simula condiciones de producción y es el espacio para validaciones exhaustivas antes del despliegue final. Cada push a la rama `stage` ejecuta:

1. Compilación y pruebas (unitarias, integración y, opcionalmente, end-to-end).
2. Construcción y push de imágenes Docker etiquetadas como `stage`.
3. Despliegue en el entorno de staging de Kubernetes.
4. Ejecución de pruebas automatizadas post-despliegue (carga, E2E, validación de endpoints).

Esto asegura que solo versiones estables y validadas lleguen a producción.

**Fragmento relevante del Jenkinsfile:**
```groovy
stage('Deploy to Staging') {
    when { branch 'stage' }
    steps {
        sh 'kubectl apply -f k8s/'
        // Aquí se pueden agregar scripts para pruebas de carga o E2E
    }
}
```
**Diferenciación:**
- El stage `Deploy to Staging` solo se ejecuta si la rama activa es `stage`.
- Las imágenes se etiquetan como `stage` y se despliegan en el namespace de staging.
- Se recomienda agregar stages para pruebas de carga y validación de endpoints.

---

### 4.3 Pipeline de despliegue (`master` / Producción)

El entorno de producción es el más crítico y requiere validaciones adicionales. Cada push o merge a la rama `master` ejecuta:

1. Compilación y pruebas exhaustivas.
2. Construcción y push de imágenes Docker etiquetadas como `latest` o con número de versión.
3. Despliegue en el entorno de producción de Kubernetes.
4. Generación y publicación automática de release notes.
5. Notificación de despliegue exitoso.

Esto garantiza trazabilidad, control y calidad en cada entrega al usuario final.

**Fragmento relevante del Jenkinsfile:**
```groovy
stage('Deploy to Production') {
    when { branch 'master' }
    steps {
        sh 'kubectl apply -f k8s/'
        // Aquí se puede agregar la generación automática de release notes
    }
}
```
**Diferenciación:**
- El stage `Deploy to Production` solo se ejecuta si la rama activa es `master`.
- Las imágenes se etiquetan como `latest` o con el número de versión y se despliegan en el namespace de producción.
- Se recomienda automatizar la generación de release notes y notificaciones.

---

### 4.4 Resumen del manejo de ramas y pipeline

La pipeline única, combinada con la directiva `when { branch 'nombre' }`, permite adaptar el flujo a cada entorno, asegurando que cada rama reciba el tratamiento adecuado y que los despliegues sean seguros, trazables y eficientes.

---

## 5. Diseño y Ejecución de Pruebas

La calidad del software es un pilar fundamental en sistemas distribuidos. Este proyecto implementa una estrategia de testing integral, cubriendo desde la lógica interna de cada microservicio hasta la experiencia real del usuario y el rendimiento bajo carga. Cada tipo de prueba aporta una capa de confianza y permite detectar errores en diferentes etapas del ciclo de vida.

### 5.1 Pruebas Unitarias

Las pruebas unitarias validan la lógica de negocio de cada componente de forma aislada, usando JUnit y Mockito. Permiten detectar errores en etapas tempranas y facilitan el refactorizado seguro. Cada microservicio cuenta con pruebas enfocadas en sus métodos críticos, asegurando que la funcionalidad básica sea confiable y mantenible.

Cada microservicio cuenta con su propio conjunto de pruebas unitarias, enfocadas en los métodos más críticos de su lógica interna. Por ejemplo, en `user-service` se prueban métodos como `findById`, `save` y `update`, asegurando que el servicio gestione correctamente los datos de usuario. En `product-service` se valida la obtención y manipulación de productos y categorías, mientras que en `payment-service` se comprueba la gestión de pagos.

| Microservicio      | Clase de prueba                       | Ejemplo de método probado                |
|--------------------|---------------------------------------|------------------------------------------|
| user-service       | UserServiceImplTest                   | testFindById, testSave, testUpdate       |
| product-service    | ProductServiceImplTest, CategoryServiceImplTest | testFindById_ShouldReturnProductDto      |
| payment-service    | TestPaymentServiceImplTest            | testSave, testFindById, testDeleteById   |

**Ejemplo real de prueba unitaria (user-service):**
Esta prueba verifica que el método `findById` del servicio de usuarios retorna correctamente un usuario existente. Se simula el repositorio con Mockito para devolver un usuario de prueba y se comprueba que los datos retornados coinciden con los esperados.
```java
@Test
void testFindById() {
    Integer userId = 1;
    when(userRepository.findById(userId)).thenReturn(Optional.of(user));
    UserDto result = userServiceImpl.findById(userId);
    assertNotNull(result);
    assertEquals(userDto.getFirstName(), result.getFirstName());
    assertEquals(userDto.getEmail(), result.getEmail());
    verify(userRepository, times(1)).findById(userId);
}
```

**Ejemplo real de prueba unitaria (product-service):**
Aquí se valida que el método `findById` del servicio de productos retorna correctamente un DTO de producto cuando existe en el repositorio simulado. Se comprueban los campos clave del producto.
```java
@Test
void testFindById_ShouldReturnProductDto() {
    when(productRepository.findById(1)).thenReturn(Optional.of(ProductUtil.getSampleProduct()));
    ProductDto result = productService.findById(product.getProductId());
    assertNotNull(result);
    assertEquals(product.getProductId(), result.getProductId());
    assertEquals(product.getProductTitle(), result.getProductTitle());
    assertEquals(product.getImageUrl(), result.getImageUrl());
}
```

**Ejemplo real de prueba unitaria (payment-service):**
Esta prueba comprueba que el método `deleteById` del servicio de pagos invoca correctamente la eliminación en el repositorio simulado.
```java
@Test
void testDeleteById() {
    Integer paymentId = 3;
    paymentService.deleteById(paymentId);
    verify(paymentRepository, times(1)).deleteById(paymentId);
}
```

---

### 5.2 Pruebas de Integración

Las pruebas de integración validan la interacción entre componentes dentro de un microservicio, asegurando que los endpoints REST y la integración entre capas funcionen correctamente. Usan bases embebidas o contenedores para simular el entorno real y detectar problemas de configuración o comunicación.

Por ejemplo, en el `user-service` se prueba la creación de un usuario a través del endpoint REST, verificando que la respuesta sea la esperada y que los datos se almacenen correctamente. En `product-service` se valida la obtención de todos los productos mediante una petición HTTP real al endpoint correspondiente.

**Ejemplo real de prueba de integración (user-service):**
Esta prueba simula una petición HTTP POST para crear un usuario, usando `TestRestTemplate`. Se verifica que la respuesta sea exitosa y que los datos retornados coincidan con los enviados.
```java
@Test
public void testCreateUser() {
    String url = "http://localhost:" + port + "/user-service/api/users";
    UserDto userDto = UserUtil.getSampleUserDto();
    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_JSON);
    HttpEntity<UserDto> entity = new HttpEntity<>(userDto, headers);
    ResponseEntity<UserDto> response = restTemplate.exchange(
            url,
            HttpMethod.POST,
            entity,
            UserDto.class
    );
    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertNotNull(response.getBody());
    assertEquals(userDto.getFirstName(), response.getBody().getFirstName());
}
```

**Ejemplo real de prueba de integración (product-service):**
Esta prueba realiza una petición GET al endpoint de productos y valida que la respuesta contenga una colección de productos válida.
```java
@Test
public void testFindAllProducts() {
    ResponseEntity<DtoCollectionResponse> response = restTemplate.exchange(
            baseUrl,
            HttpMethod.GET,
            null,
            DtoCollectionResponse.class
    );
    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertNotNull(response.getBody());
    assertNotNull(response.getBody().getCollection());
}
```

---

### 5.3 Pruebas E2E (End-to-End)

Las pruebas E2E simulan flujos completos de usuario a través de varios microservicios, validando la integración real del sistema en condiciones similares a producción. Son clave para asegurar que los procesos críticos del negocio funcionen de extremo a extremo y que la experiencia del usuario sea consistente.

**Ejemplo real de prueba E2E (user-service):**
Esta prueba simula el registro completo de un usuario, enviando un payload complejo que incluye credenciales y dirección. Se valida que el endpoint acepte la petición y retorne un código de éxito.
```java
@Test
void shouldSaveUser(){
    Map<String, Object> credentialPayload = Map.of(
            "username", "jacotaco",
            "password", "12345678",
            "roleBasedAuthority", "ROLE_USER",
            "isEnabled", true,
            "isAccountNonExpired", true,
            "isAccountNonLocked", true,
            "isCredentialsNonExpired", true
    );
    Map<String, Object> addressPayload = Map.of(
            "fullAddress", "Calle 35 norte #6a abis 35",
            "postalCode", "760001",
            "city", "Cali"
    );
    Map<String, Object> userPayload = Map.of(
            "firstName", "Darwin",
            "lastName", "Lenis",
            "imageUrl", "http://placeholder:200",
            "email", "darpa@gmial.com",
            "phone", "3218770876",
            "addressDtos", List.of(addressPayload),
            "credential", credentialPayload
    );
    ResponseEntity<String> response = restFacade.post(userServiceUrl + "/user-service/api/users",
            userPayload,
            String.class);
    assertTrue(response.getStatusCode().is2xxSuccessful(), "Unexpected status code: " + response.getStatusCode());
}
```

**Ejemplo real de prueba E2E (order-service):**
Esta prueba realiza una consulta a un pedido específico, validando que el endpoint de órdenes responde correctamente y que el pedido existe.
```java
@Test
void shouldGetOrderById() {
    int orderId = 2;
    ResponseEntity<String> response = restFacade.get(
            productServiceUrl + "/order-service/api/orders/" + orderId,
            String.class);
    assertTrue(response.getStatusCode().is2xxSuccessful(), "Unexpected status code: " + response.getStatusCode());
}
```

**Ejemplo real de prueba E2E (favourite-service):**
Esta prueba comprueba que el endpoint de favoritos retorna correctamente la lista de favoritos del usuario autenticado, validando la integración entre autenticación, lógica de negocio y persistencia.
```java
@Test
void shouldGetAllFavourites() {
    ResponseEntity<String> response = restFacade.get(
            favouriteServiceUrl + "/favourite-service/api/favourites",
            String.class);
    assertTrue(response.getStatusCode().is2xxSuccessful(), "Unexpected status code: " + response.getStatusCode());
}
```

---

### 5.4 Pruebas de Rendimiento con Locust

Las pruebas de rendimiento, realizadas con Locust, permiten simular usuarios concurrentes y medir la capacidad, estabilidad y tiempos de respuesta del sistema bajo carga. Son esenciales para identificar cuellos de botella y validar la escalabilidad antes de exponer el sistema a usuarios reales.

Los scripts de Locust, ubicados en la carpeta `locust/`, definen escenarios de uso típicos, como navegación de productos y creación de órdenes. Se pueden ajustar el número de usuarios concurrentes y el tiempo de espera entre acciones para simular diferentes condiciones de carga.

**Ejemplo real de script Locust (locust/locustfile.py):**
Este script define dos tareas principales: consultar la lista de productos y crear una orden. Cada usuario virtual ejecuta estas tareas de forma aleatoria, permitiendo medir el rendimiento de los endpoints más críticos del sistema.
```python
from locust import HttpUser, task, between

class EcommerceUser(HttpUser):
    wait_time = between(1, 5)

    @task
    def view_products(self):
        self.client.get("/products")

    @task
    def create_order(self):
        self.client.post("/orders", json={"productId": 1, "quantity": 1})
```

Al ejecutar estas pruebas, Locust genera reportes detallados con métricas como tiempos de respuesta, throughput y tasa de errores, facilitando la toma de decisiones para optimización y dimensionamiento del sistema.

### 6. Ejecucion de la Pipeline

