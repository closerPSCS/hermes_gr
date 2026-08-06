# Hermes Slack — Guia de Configuracion

Guia paso a paso para conectar Hermes Agent con Slack mediante Socket Mode.

---

## Requisitos previos

- Cuenta de Slack en el workspace objetivo
- Contenedor Docker `hermes-oracle-local` funcionando
- Acceso a terminal (WSL, PowerShell, etc.)

---

## Paso 1 — Crear Slack App

1. Ir a https://api.slack.com/apps
2. Clic en **Create New App** → **From scratch**
3. Elegir opcion **Blank app** (no usar templates)
4. Nombre: `Hermes` (o el que prefieras)
5. Seleccionar el workspace de destino

---

## Paso 2 — Activar Socket Mode

Socket Mode permite que el bot se conecte a Slack sin exponer una URL publica.

1. En tu Slack App, ve a **Settings** → **Socket Mode**
2. Activar **Enable Socket Mode**
3. Dar nombre al token (ej: `hermes-socket`)
4. Agregar scope: `connections:write`
5. Clic en **Generate**
6. **Guardar el App Token** (`xapp-...`) — se usara en el wizard

---

## Paso 3 — Configurar OAuth & Permissions

1. Ve a **Features** → **OAuth & Permissions**
2. En **Scopes** → **Bot Token Scopes**, agrega:
   - `app_mentions:read`
   - `channels:history`
   - `channels:read`
   - `chat:write`
   - `commands`
   - `files:read`
   - `files:write`
   - `groups:history`
   - `groups:read`
   - `im:history`
   - `im:read`
   - `im:write`
   - `reactions:read`
   - `users:read`
3. Ve a **Install App** y haz clic en **Install to Workspace**
4. Si el workspace es de empresa y no eres admin, Slack pedira aprobacion — contacta a tu admin
5. **Guardar el Bot Token** (`xoxb-...`)

---

## Paso 4 — Generar el Signing Secret

1. Ve a **Settings** → **Basic Information**
2. En **App Credentials**, copia el **Signing Secret**

---

## Paso 5 — Ejecutar el wizard de Hermes

Desde tu terminal, ejecuta:

```powershell
docker exec -it hermes-oracle-local hermes -p miperfil gateway setup
```

En el wizard:
1. Selecciona **Slack** (icono maletin con S)
2. Ingresa el **Bot Token** (`xoxb-...`)
3. Ingresa el **Signing Secret**
4. Ingresa el **App Token** (`xapp-...`)
5. **Allowed user IDs:** dejar vacio si no hay restriccion, o poner tu Member ID
6. **Home channel ID:** poner el ID del canal de notificaciones o dejar vacio para configurarlo luego con `/set-home` en Slack

El wizard genera automaticamente el archivo `/opt/data/slack-manifest.json`.

---

## Paso 6 — Aplicar el Manifest en Slack

Despues del wizard, pegar el manifest generado en la Slack App:

1. Ve a https://api.slack.com/apps → tu app
2. **Features** → **App Manifest**
3. Pega el contenido de `/opt/data/slack-manifest.json`
4. Clic en **Save**
5. Slack pedira reinstalar — haz clic en **Reinstall to Workspace**

Si eres admin no necesita aprobacion. Si no, solicita aprobacion al admin del workspace.

---

## Paso 7 — Configurar variables de entorno en el perfil

El wizard guarda los tokens en `/opt/data/.env`. Hay que copiarlos al `.env` del perfil miperfil:

```powershell
docker exec hermes-oracle-local bash -c "grep SLACK_ /opt/data/.env >> /opt/data/profiles/miperfil/.env"
docker exec hermes-oracle-local bash -c "echo 'GATEWAY_ALLOW_ALL_USERS=true' >> /opt/data/profiles/miperfil/.env"
```

---

## Paso 8 — Verificar que solo hay una gateway con Slack

Si hay dos gateways (default + miperfil), pelearan por el token de Slack. Detener la default:

```powershell
docker exec hermes-oracle-local bash -c "hermes gateway stop; s6-svc -d /var/run/s6/services/gateway-default 2>/dev/null; s6-svc -d /var/run/s6/services/gateway-default/log 2>/dev/null"
```

Verificar que solo corre la de miperfil:

```powershell
docker exec hermes-oracle-local ps aux | Select-String "gateway run"
```

---

## Paso 9 — Reiniciar gateway de miperfil

```powershell
docker exec hermes-oracle-local pkill -f "miperfil gateway run"
```

s6-supervise reiniciara automaticamente el gateway. Verificar con:

```powershell
docker exec hermes-oracle-local tail -20 /opt/data/profiles/miperfil/logs/gateway.log
```

Debes ver lineas como:

```
Connecting to slack...
[Slack] Authenticated as @hermesn0 in workspace closer
[Slack] Socket Mode connected (1 workspace(s))
✓ slack connected
Gateway running with 1 platform(s)
```

---

## Paso 10 — Probar la conexion

En Slack, escribe en el canal o DM:

```
@hermes 00089108
```

El bot debe responder con el diagnostico de la persona consultada.

---

## Troubleshooting

### El bot no responde

Verificar logs de gateway:

```powershell
docker exec hermes-oracle-local tail -50 /opt/data/profiles/miperfil/logs/gateway.log
```

Posibles causas:

| Mensaje en log | Solucion |
|---|---|
| `Early reject of unauthorized user` | Agregar `GATEWAY_ALLOW_ALL_USERS=true` al `.env` del perfil |
| `No messaging platforms enabled` | Verificar que los tokens SLACK estan en el `.env` del perfil |
| `Slack app token was held by gateway PID X` | Hay dos gateways peleando por el token — detener la default |
| `Gateway shutting down` repetitivo | Mismo problema de gateways duplicadas |

### No puedo instalar la app (empresa)

Si tu workspace de empresa requiere aprobacion de admin para instalar apps:
1. Al intentar instalar, Slack mostrara un banner amarillo
2. Haz clic en **Re-request approval**
3. El admin del workspace recibira la solicitud y debera aprobarla
4. Una vez aprobada, la app se instala automaticamente

---

## Resumen de archivos clave

| Archivo | Ubicacion (dentro del contenedor) |
|---|---|
| Tokens Slack | `/opt/data/.env` y `/opt/data/profiles/miperfil/.env` |
| Manifest Slack | `/opt/data/slack-manifest.json` |
| Config del perfil | `/opt/data/profiles/miperfil/config.yaml` |
| Logs del gateway | `/opt/data/profiles/miperfil/logs/gateway.log` |
