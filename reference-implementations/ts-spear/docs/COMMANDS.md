# Staff Commands

Base command: `/ts` (alias configurable in `plugin.yml`).

Requires `tsspear.use` unless noted.

## Inspection

| Command | Permission | Description |
|---------|------------|-------------|
| `/ts inspect [player]` | `tsspear.inspect` | Open Inspector GUI |
| `/ts inspect text [player]` | `tsspear.inspect` | Text-mode confidence breakdown |
| `/ts trust [player]` | `tsspear.trust` | Net trust confidence + trend |
| `/ts suspicion [player]` | `tsspear.suspicion` | Risk score, delta, trend |
| `/ts evidence [player]` | `tsspear.evidence` | Evidence GUI (last 10 persisted) |
| `/ts evidence text [player]` | `tsspear.evidence` | Text evidence list |
| `/ts debug [player]` | `tsspear.debug` | Raw state dump |
| `/ts debug profile` | `tsspear.debug` | Performance metrics snapshot |

## Visualization

| Command | Permission | Description |
|---------|------------|-------------|
| `/ts graph [player] [kind]` | `tsspear.graph` | Graph GUI (`tension`, `evidence`, `risk`, etc.) |
| `/ts timeline [player]` | `tsspear.timeline` | Receipt timeline GUI |
| `/ts replay [player]` | `tsspear.replay` | Replay viewer GUI |
| `/ts checks` | `tsspear.checks` | Check management GUI |
| `/ts checks toggle <id>` | `tsspear.checks` | Enable/disable a check |

## Enforcement Support

| Command | Permission | Description |
|---------|------------|-------------|
| `/ts freeze [player]` | `tsspear.freeze` | Enable inspection freeze + continuous replay |
| `/ts unfreeze [player]` | `tsspear.freeze` | Disable freeze |
| `/ts alerts [on\|off]` | `tsspear.alerts` | Toggle personal staff alerts |

## ML & Export (Phase 5)

| Command | Permission | Description |
|---------|------------|-------------|
| `/ts export [player]` | `tsspear.export` | Export schema v1 JSON to `exports/` |
| `/ts export jsonl [player]` | `tsspear.export` | Export JSONL for batch ML ingestion |
| `/ts annotate <player> <verdict> [notes]` | `tsspear.annotate` | Staff training label |
| `/ts infer [player]` | `tsspear.infer` | Run active inference provider |

### Annotation Verdicts

`LEGIT` · `SUSPICIOUS` · `CHEATING` · `INCONCLUSIVE`

Labels attach to the next export for that player.

## Graph Kinds

`EVIDENCE` · `CONSTRAINT` · `TENSION` · `RISK` · `HISTORY`

Example: `/ts graph Steve tension`

## Registered Checks

| ID | Category | Dimension |
|----|----------|-----------|
| `impossible-movement` | Movement | MOVEMENT |
| `lag-spike` | Movement | MOVEMENT |
| `fly-check` | Movement | MOVEMENT |
| `speed-check` | Movement | MOVEMENT |
| `reach-check` | Combat | COMBAT |
| `killaura-check` | Combat | COMBAT |
| `velocity-check` | Combat | COMBAT |
| `bad-packet-check` | Networking | NETWORKING |
| `blink-check` | Networking | NETWORKING |