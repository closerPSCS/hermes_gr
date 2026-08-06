# Registro de Cambios

Log cronologico de cambios relevantes del proyecto.

---

| Fecha | Descripcion | Archivos afectados |
|---|---|---|
| 2026-08-05 | Creacion de carpeta BITACORA_IA con plantillas vacias | `BITACORA_IA/registro_cambios.md`, `BITACORA_IA/tutorial.md`, `BITACORA_IA/manual_usuario.md`, `BITACORA_IA/manual_tecnico.md` |
| 2026-08-05 | Seccion Bitacora IA agregada a AGENTS.md | `AGENTS.md` |
| 2026-08-05 | Creacion de repositorio git local y .gitignore | `.gitignore` |
| 2026-08-05 | Commit inicial del repositorio (`33b4985`) | 524 archivos (source material, skills, config) |
| 2026-08-05 | Configuracion de remote origin en GitHub | `git remote add origin https://github.com/closerPSCS/hermes_gr.git` |
| 2026-08-05 | Push inicial a GitHub (`master`) | `33b4985` a `origin/master` |
| 2026-08-06 | Configuracion de Slack via Socket Mode + Gateway | `.env` (miperfil), `gateway_state.json`, `channel_directory.json` |
| 2026-08-06 | Documentacion de configuracion Slack | `BITACORA_IA/hermes-slack.md` |
| 2026-08-06 | Busqueda por correo electronico en agente N0 (grncero): nuevo SQL `buscar-por-correo.sql`, flujo Paso 1b, actualizacion de identificadores, proteccion de datos y casos de prueba | `grncero/skills/gr-consulta-persona/references/buscar-por-correo.sql` (nuevo), `references/identificadores.md`, `SKILL.md`, `AGENTS.md`, `N0_contexto_funcional.md` |
| 2026-08-06 | Refinamiento: restriccion de dominio email a @esade.edu / @alumni.esade.edu y prioridad de deteccion sobre pedir aclaraciones | `grncero/references/identificadores.md`, `SKILL.md`, `AGENTS.md`, `N0_contexto_funcional.md` |
