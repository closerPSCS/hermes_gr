---
name: oracle-sqlcl
description: Use Oracle SQLcl MCP tools for safe connection lifecycle.
---

# Oracle SQLcl MCP workflow

Use this skill when the user asks to inspect saved Oracle connections or manage an Oracle SQLcl connection through the MCP server.

## Core rules

1. Identify the exact requested operation before invoking a tool: list saved connections, connect, disconnect, or another database operation.
2. Honor tool-scope constraints literally. If the user says “use only” a specific Oracle SQLcl tool, invoke only that server operation; do not add schema inspection, SQL execution, connection discovery, or follow-up database tools. If the user supplies the exact saved connection name, do not call `connections_list`; invoke `connect` directly. Tool-loading calls (`tool_search`/`tool_describe`) are permissible only to obtain the requested tool's schema and are not database operations.
3. For saved-connection discovery, use `connections_list` and report connection names only unless the user explicitly requests details. Keep `show_details=false` by default.
4. For connection establishment, use `connect` with the exact saved connection name. Never infer or invent a name. A successful connection tool response is sufficient to report the lifecycle result; do not automatically call schema-information tools when the user asked only to connect.
5. For closing a session, use `disconnect` and report whether the disconnect succeeded. Do not reconnect or run validation SQL unless requested.
6. Do not expose credentials, connect strings, usernames, or other connection details unless explicitly requested and authorized.

## Tool invocation guidance

- `connections_list`: pass an empty name and username filter for all saved connections; use `show_details=false` for names-only requests.
- `connect`: pass the case-sensitive saved connection label as `connection_name`.
- `disconnect`: close the currently active session.
- Supply the actual model name/version in the tool’s `model` parameter when required by the server schema.

## Reporting

Be concise for lifecycle operations. State the exact connection label and the outcome. If the user prohibited SQL, explicitly confirm that no SQL was executed, while avoiding claims about what internal setup the server may perform.

## Pitfalls

- Do not call `schema_information` after a successful `connect` merely because a tool description recommends it; user-imposed scope takes precedence.
- Do not use `connections_list` to satisfy a request that explicitly says to use only `connect` or `disconnect`.
- Tool-loading calls may be necessary to discover a schema, but the database action itself must remain limited to the user-requested Oracle SQLcl tool.

Session-specific notes and examples are in `references/lifecycle-examples.md`.
