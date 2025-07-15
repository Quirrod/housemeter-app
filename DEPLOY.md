# Despliegue en Railway

## Pasos para desplegar el backend en Railway:

### 1. Preparar el repositorio
```bash
cd backend
git init
git add .
git commit -m "Initial commit"
```

### 2. Subir a GitHub
1. Crear repositorio en GitHub
2. Conectar y subir:
```bash
git remote add origin https://github.com/tu-usuario/housemeter-backend.git
git branch -M main
git push -u origin main
```

### 3. Desplegar en Railway
1. Ve a [railway.app](https://railway.app) e inicia sesión
2. Click en "New Project"
3. Selecciona "Deploy from GitHub repo"
4. Selecciona tu repositorio `housemeter-backend`
5. Railway detectará automáticamente el Dockerfile

### 4. Configurar variables de entorno
En el dashboard de Railway, añade estas variables:
- `JWT_SECRET`: un string seguro aleatorio (ej: `your-super-secret-jwt-key-change-this`)
- `NODE_ENV`: `production`
- `PORT`: (Railway lo configura automáticamente)

### 5. Obtener la URL del deploy
Railway te dará una URL como: `https://tu-proyecto.railway.app`

### 6. Actualizar la app Android
Cambia la URL base en `RetrofitClient.kt`:
```kotlin
private const val BASE_URL = "https://tu-proyecto.railway.app/api/"
```

## Comandos útiles

### Ver logs en Railway:
```bash
railway logs
```

### Desplegar cambios:
```bash
git add .
git commit -m "Update"
git push
```

## Notas importantes:
- Railway ofrece $5 gratis al mes
- La base de datos SQLite se almacenará en el contenedor (se resetea con cada deploy)
- Para persistencia de datos en producción, considera usar Railway PostgreSQL
- Los archivos subidos (comprobantes) se almacenan en el contenedor

## Migrar a PostgreSQL (opcional):
1. En Railway dashboard, añade PostgreSQL service
2. Instala `pg` en lugar de `sqlite3`
3. Actualiza `database.js` para usar PostgreSQL
4. Configura variable `DATABASE_URL`