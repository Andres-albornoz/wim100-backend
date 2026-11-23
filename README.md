# ⚙️ wim100-backend

Microservicio REST del proyecto **WIM 100** (DYM Cyber). Gestiona las candidaturas del programa y el inicio de sesión de las evaluadoras y de la Dirección. Lo consume la app Android [APLICACION_DESARROLLO_MOVIL](https://github.com/Angellssj/APLICACION_DESARROLLO_MOVIL).

Proyecto académico de **Desarrollo de Aplicaciones Móviles (DSY1105)**, Duoc UC, 2026. Evaluación Parcial 4 (encargo) y 5 (defensa).

> Todos los datos de prueba son **ficticios**, como exige el caso.

## 👥 Integrantes

| Integrante | GitHub |
|---|---|
| Andrés Albornoz | [@Andres-albornoz](https://github.com/Andres-albornoz) |
| Ángel González | [@Angellssj](https://github.com/Angellssj) |
| Roselín Verdu | [@Roselin2831](https://github.com/Roselin2831) |

## 🛠️ Tecnologías

| Tecnología | Uso |
|---|---|
| Java 21 | Lenguaje |
| Spring Boot 4.1 (Web MVC, Data JPA, Validation) | API REST, acceso a datos y validaciones |
| MySQL 8 + MySQL Connector/J | Base de datos |
| Spring Security Crypto (BCrypt) | Cifrado de contraseñas. Solo el encriptador: **no** se usa `spring-boot-starter-security`, que bloquearía todos los endpoints con un 401 |
| Lombok | Getters, setters y constructores de las entidades |
| Maven (wrapper incluido) | Build y dependencias |

## 🏗️ Arquitectura

```
App Android ── HTTP/JSON ──> Controller ──> Service ──> Repository (JPA) ──> MySQL
```

```
src/main/java/cl/duoc/wim100
├── Wim100BackendApplication.java   Punto de entrada
├── controller/   Endpoints REST: CandidataController, AuthController
├── Service/      Reglas de negocio: CandidataService, AuthService
├── Repository/   Interfaces JpaRepository: CandidataRepository, UsuarioRepository
├── Model/        Entidades JPA (Candidata, Usuario) y enums (EstadoCandidata, Rol)
├── dto/          Entrada y salida de la API, con validaciones (records)
├── Exception/    Errores propios y GlobalExceptionHandler
└── Config/       Bean de BCrypt (PasswordConfig) y datos de prueba (DataLoader)
```

| Capa | Qué hace | Ejemplo |
|---|---|---|
| Controller | Recibe la petición, valida el JSON con `@Valid` y responde | `CandidataController` |
| Service | Aplica las reglas de negocio | Correo único, cambio de estado, login |
| Repository | Habla con la base de datos; Spring genera las consultas desde el nombre del método | `existsByCorreoIgnoreCase` |
| Model | Representa las tablas | `postulantes`, `usuarios` |

### Reglas de negocio
- No se puede registrar dos veces el mismo correo (409), ni al crear ni al editar.
- Una candidatura nueva siempre parte en estado `POSTULADA`, con la fecha del servidor.
- Editar no cambia el estado, la fecha ni quién registró la candidatura.
- Las contraseñas se guardan cifradas con BCrypt, nunca en texto plano.
- El login responde el mismo mensaje si el usuario no existe o si la contraseña está mal, para no revelar qué usuarios existen. La respuesta nunca incluye la contraseña.

### Estados de una candidatura
`POSTULADA` → `EN_EVALUACION` → `EVALUADA` → `SELECCIONADA` / `NO_SELECCIONADA`

## 🔌 Endpoints

URL base: `http://localhost:8080/api`

| Método | Ruta | Uso | Respuestas |
|---|---|---|---|
| GET | `/postulantes` | Listar (más recientes primero) | 200 |
| GET | `/postulantes/{id}` | Ver una candidatura | 200 · 404 |
| POST | `/postulantes` | Registrar | 201 · 400 · 409 |
| PUT | `/postulantes/{id}` | Editar | 200 · 400 · 404 · 409 |
| PATCH | `/postulantes/{id}/estado` | Cambiar estado (evaluación o resolución) | 200 · 400 · 404 |
| DELETE | `/postulantes/{id}` | Eliminar | 204 · 404 |
| POST | `/auth/login` | Iniciar sesión | 200 · 400 · 401 |

### Ejemplos

**Login** — `POST /api/auth/login`
```json
{ "usuario": "evaluadora1", "password": "wim2026" }
```
Respuesta 200:
```json
{ "id": 1, "usuario": "evaluadora1", "nombre": "Camila Rojas Demo", "rol": "EVALUADORA" }
```

**Registrar** — `POST /api/postulantes`
```json
{
  "nombre": "Antonia Pérez Lillo",
  "correo": "antonia.perez@ejemplo.cl",
  "telefono": "+56 9 1111 2299",
  "area": "Forense y respuesta a incidentes",
  "aniosExperiencia": 6,
  "resumenExperiencia": "Analista forense en un equipo ficticio de respuesta a incidentes.",
  "linkedinUrl": "https://www.linkedin.com/in/antonia-perez-demo",
  "cvNombre": "CV_Antonia.pdf",
  "registradaPor": "Camila Rojas Demo"
}
```

**Cambiar estado** — `PATCH /api/postulantes/{id}/estado`
```json
{ "estado": "EN_EVALUACION" }
```

**Error de validación (400)**
```json
{
  "status": 400,
  "mensaje": "Datos inválidos",
  "errores": { "aniosExperiencia": "No puede ser negativo" }
}
```

### Validaciones del registro y la edición
| Campo | Regla |
|---|---|
| `nombre` | Obligatorio, de 3 a 60 caracteres |
| `correo` | Obligatorio, formato de correo válido, único |
| `telefono` | Obligatorio, máximo 20 caracteres |
| `area` | Obligatoria. Debe ser una de las áreas de la app (ver abajo) |
| `aniosExperiencia` | Obligatorio, de 0 a 50 |
| `resumenExperiencia` | Obligatorio, de 30 a 500 caracteres |
| `linkedinUrl` | Obligatorio, debe comenzar con `https://` |
| `cvNombre` | Opcional. Solo el nombre del archivo: el PDF queda en el teléfono |
| `registradaPor` | Obligatorio |

Áreas: Seguridad ofensiva (Red Team) · Seguridad defensiva (Blue Team) · Gobierno, riesgo y cumplimiento · Forense y respuesta a incidentes · Seguridad en la nube · Inteligencia de amenazas.

## 💻 Cómo ejecutar

### Requisitos
- **JDK 21** (sirve el que trae Android Studio).
- **MySQL 8** encendido (en Windows, el servicio `MySQL80`). MySQL Workbench es opcional.
- Postman o Insomnia para probar los endpoints (opcional).

### 1. Base de datos
No hay que crear nada: la URL de conexión incluye `createDatabaseIfNotExist=true` y JPA (`ddl-auto=update`) crea las tablas `postulantes` y `usuarios` al arrancar. Si prefieres crearla a mano:
```sql
CREATE DATABASE IF NOT EXISTS wim100 CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

### 2. Usuario y contraseña de MySQL
Por defecto se usa `root` / `root` (en `src/main/resources/application.properties`). Si tu MySQL tiene otra contraseña, no edites el archivo: pásala como variable de entorno al ejecutar.

### 3. Ejecutar
**Windows (PowerShell):**
```powershell
git clone https://github.com/Andres-albornoz/wim100-backend.git
cd wim100-backend
$env:SPRING_DATASOURCE_PASSWORD="tu_clave_de_mysql"
.\mvnw.cmd spring-boot:run
```

**macOS / Linux:**
```bash
SPRING_DATASOURCE_PASSWORD=tu_clave_de_mysql ./mvnw spring-boot:run
```

También se puede ejecutar `Wim100BackendApplication` desde IntelliJ. En ese caso, activa *Settings › Build › Compiler › Annotation Processors › Enable annotation processing* para que funcione Lombok.

Está listo cuando la consola muestra `Tomcat started on port 8080`. Para comprobarlo, abre http://localhost:8080/api/postulantes en el navegador: deben aparecer 3 candidatas en JSON.

### 4. Conectar la app
- **Emulador:** la app usa `http://10.0.2.2:8080/api/`, que apunta a este computador. No hay que cambiar nada.
- **Teléfono físico:** misma red Wi-Fi, la IP del computador (`ipconfig`) en `BASE_URL` y el puerto 8080 permitido en el firewall.

## 👤 Datos de prueba

Al arrancar, `DataLoader` carga estos datos solo si las tablas están vacías.

| Usuario | Contraseña | Nombre | Rol |
|---|---|---|---|
| `evaluadora1` | `wim2026` | Camila Rojas Demo | EVALUADORA |
| `evaluadora2` | `wim2026` | Valentina Muñoz Demo | EVALUADORA |
| `direccion` | `wim2026` | Dirección WIM Demo | DIRECCION |

Candidatas: Ignacia Valdés Soto (Red Team), Martina Herrera Lagos (Blue Team) y Fernanda Castillo Rivas (Gobierno, riesgo y cumplimiento), todas en estado `POSTULADA`.

## 🧪 Pruebas con Postman

La colección está en [`postman/WIM100.postman_collection.json`](postman/WIM100.postman_collection.json). En Postman: **Import** → elige el archivo → clic derecho en la colección → **Run collection**. Las 11 peticiones traen pruebas automáticas que revisan el código de respuesta. Se pueden ejecutar varias veces, porque la candidata de prueba se elimina al final.

Resumen de lo que se prueba:

| # | Petición | Resultado esperado |
|---|---|---|
| 1 | `GET /api/postulantes` | 200 y la lista de 3 candidatas |
| 2 | `POST /api/auth/login` con `evaluadora1` / `wim2026` | 200 con id, nombre y rol |
| 3 | `POST /api/auth/login` con una contraseña incorrecta | 401 |
| 4 | `POST /api/postulantes` con el JSON de ejemplo | 201 con el id asignado |
| 5 | Repetir el POST anterior | 409 (correo repetido) |
| 6 | POST con `"aniosExperiencia": -1` | 400 con el error del campo |
| 7 | `PUT /api/postulantes/{id}` con datos cambiados | 200 |
| 8 | `PATCH /api/postulantes/{id}/estado` con `{"estado":"EN_EVALUACION"}` | 200 |
| 9 | `DELETE /api/postulantes/{id}` | 204; un GET posterior da 404 |

## ❗ Errores frecuentes

| Síntoma | Causa | Solución |
|---|---|---|
| `Communications link failure` al arrancar | MySQL apagado o credenciales malas | Iniciar el servicio `MySQL80`; revisar la contraseña |
| `Access denied for user 'root'` | Contraseña de MySQL distinta de `root` | Usar `SPRING_DATASOURCE_PASSWORD` |
| `release version 21 not supported` | JDK antiguo | Instalar JDK 21 o usar el de Android Studio |
| `cannot find symbol` en getters | Lombok sin procesamiento de anotaciones | Activarlo en IntelliJ |
| Port 8080 already in use | Otro proceso usa el puerto | Cerrarlo o cambiar `server.port` |
| La app recibe 404 | `BASE_URL` sin `/api/` o sin `/` final | La URL debe terminar en `/api/` |

---

Proyecto académico con fines educativos · Duoc UC · 2026
