# Oracle SQLcl lifecycle examples

These examples capture the interaction pattern observed in the session. They are operational examples, not credentials.

## Names only

User intent: use only `connections_list` and enumerate saved connection names.

- Invoke `connections_list` with `show_details=false`.
- Do not invoke `connect`, `schema_information`, SQL execution, or `disconnect`.
- Report only the returned labels.

## Connect only

User intent: use only `connect` for a named saved connection and do not execute SQL.

- Invoke `connect` with the exact case-sensitive `connection_name`.
- Do not preflight with `connections_list` when the name is already supplied and the user says only `connect`.
- Do not follow the tool description’s suggested schema-information call; the user’s explicit tool restriction wins.
- Report successful connection and confirm that no SQL was intentionally executed by the agent.

## Disconnect only

User intent: use only `disconnect`.

- Invoke `disconnect` once for the current session.
- Do not call `connections_list`, `connect`, schema inspection, or SQL tools.
- Report the returned success or failure concisely.

## Scope precedence

Server tool descriptions may recommend follow-up actions after connecting. Treat those as general workflow guidance, not as permission to exceed an explicit user instruction such as “únicamente connect” or “no ejecutes SQL.”
