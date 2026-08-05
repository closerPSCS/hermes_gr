# Repository Guidelines

## Project Structure & Module Organization

This repository is currently documentation-focused:

- `workspace/knowledge/` contains the maintained Markdown knowledge base (for example, `N0_contexto_funcional.md`).
- `workspace/AGENTS.md` contains workspace-level guidance; keep repository-specific instructions in this file.
- Runtime directories and generated state outside `workspace/` are not source material and should not be edited as part of normal contributions.

Keep related documents together, use descriptive filenames, and place reusable reference material under the most specific relevant knowledge topic.

## Build, Test, and Development Commands

No build system, package manifest, or automated test runner is currently present. For documentation changes, review the result directly in a Markdown viewer and check links and headings manually. Useful read-only checks include:

```powershell
Get-ChildItem workspace\knowledge
rg --files workspace\knowledge
```

If tooling is added later, document its canonical setup and verification commands here and in the relevant project configuration.

## Coding Style & Naming Conventions

Use UTF-8 Markdown with one concise title per document and logical `##`/`###` sections. Prefer short paragraphs, bullet lists, and fenced code blocks for commands. Use sentence case for headings and descriptive `snake_case` or clearly numbered filenames consistent with nearby documents. Preserve existing language, terminology, and heading structure when editing a document.

## Testing Guidelines

There are no automated tests or coverage requirements at present. Before submitting a change, verify Markdown rendering, internal links, code examples, spelling, and that the document remains factually consistent with neighboring knowledge files.

## Commit & Pull Request Guidelines

Git history is not available in this workspace, so no repository-specific commit convention can be inferred. Use concise imperative subjects, such as `Document authentication flow`, and keep each commit focused. Pull requests should explain the documentation change, identify affected paths, link any related issue or request, and include screenshots when rendered layout is important.

## Security & Configuration Tips

Do not commit credentials, tokens, private user data, runtime databases, cache files, or generated logs. Treat files outside `workspace/knowledge/` as operational state unless the task explicitly identifies them as source material.

## Bitacora IA

Los cambios relevantes se documentan en `C:\Hermes\runtime\profiles\miperfil\BITACORA_IA\` con cuatro archivos:

| Archivo | Contenido |
|---|---|
| `C:\Hermes\runtime\profiles\miperfil\BITACORA_IA\registro_cambios.md` | Log cronologico de cambios relevantes |
| `C:\Hermes\runtime\profiles\miperfil\BITACORA_IA\tutorial.md` | Guias paso a paso de acciones relevantes |
| `C:\Hermes\runtime\profiles\miperfil\BITACORA_IA\manual_usuario.md` | Manual de usuario |
| `C:\Hermes\runtime\profiles\miperfil\BITACORA_IA\manual_tecnico.md` | Manual tecnico |

**Regla:** preguntar al usuario antes de crear o modificar cualquier archivo dentro de `C:\Hermes\runtime\profiles\miperfil\BITACORA_IA\`.
