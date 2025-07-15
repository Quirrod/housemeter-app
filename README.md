# HouseMeter - Gestión de Apartamentos

Aplicación Android para la gestión de apartamentos con medidores individuales.

## Características

### Para Administradores:
- Asignar deudas a apartamentos
- Ver métricas y estadísticas de pagos
- Aprobar/rechazar comprobantes de pago
- Gestionar apartamentos y usuarios

### Para Usuarios:
- Ver deudas pendientes
- Subir comprobantes de pago
- Ver historial de pagos
- Notificaciones de vencimientos

## Arquitectura

- **Frontend**: Android con Jetpack Compose + MVVM
- **Backend**: Node.js + Express + SQLite
- **Autenticación**: JWT
- **Containerización**: Docker

## Instalación

### Backend (Docker)

```bash
cd backend
docker-compose up -d
```

El backend estará disponible en `http://localhost:3000`

### Android App

1. Abrir el proyecto en Android Studio
2. Sync gradle dependencies
3. Ejecutar en emulador o dispositivo

## API Endpoints

### Autenticación
- `POST /api/auth/login` - Login de usuario
- `POST /api/auth/register` - Registro (solo admin)

### Apartamentos
- `GET /api/apartments` - Lista de apartamentos
- `POST /api/apartments` - Crear apartamento (admin)

### Deudas
- `GET /api/debts` - Lista de deudas
- `POST /api/debts` - Crear deuda (admin)
- `PUT /api/debts/{id}` - Actualizar deuda (admin)

### Pagos
- `GET /api/payments` - Lista de pagos
- `POST /api/payments` - Crear pago con comprobante
- `PUT /api/payments/{id}/status` - Aprobar/rechazar pago (admin)

### Métricas
- `GET /api/metrics/payments` - Métricas de pagos
- `GET /api/metrics/history` - Historial de pagos

## Estructura del Proyecto

```
housemeter/
├── backend/                 # Backend Node.js
│   ├── routes/             # Rutas de la API
│   ├── middleware/         # Middleware de autenticación
│   ├── database.js         # Configuración SQLite
│   ├── server.js           # Servidor principal
│   └── docker-compose.yml  # Docker setup
├── app/                    # Android App
│   └── src/main/java/com/jarrod/house/
│       ├── data/           # Modelos y repositorios
│       ├── ui/             # Pantallas y ViewModels
│       └── MainActivity.kt # Actividad principal
└── README.md
```

## Usuarios de Prueba

La base de datos se inicializa automáticamente con:

- **Admin**: username: `admin`, password: `admin123`
- **Usuario**: username: `user101`, password: `user123` (apartamento 101)

### Datos de muestra incluidos:
- 3 pisos (1, 2, 3)
- 6 apartamentos (101, 102, 201, 202, 301, 302)
- Medidores asignados (M001-M006)

## Screenshots

[Agregar screenshots de la aplicación aquí]

## Licencia

MIT License