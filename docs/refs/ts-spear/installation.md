# Installation

TS-Spear targets **Paper 1.21+** servers. The plugin JAR is self-contained (shadowed) except for optional ProtocolLib integration.

## Requirements

| Component | Version |
|-----------|---------|
| Java | 21+ (server runtime) |
| Paper | 1.21+ |
| ProtocolLib | Optional — enables packet-level position validation |

## Build from Source

```bash
git clone https://github.com/BadgersMC/spear-plugin.git
cd spear-plugin/reference-implementations/ts-spear
./gradlew build
```

Output: `plugin/build/libs/TSSpear-0.1.0-SNAPSHOT.jar`

## Deploy

1. Copy `TSSpear-0.1.0-SNAPSHOT.jar` to your server's `plugins/` directory.
2. Start (or restart) the server once to generate `plugins/TSSpear/config.yml`.
3. Edit configuration as needed (see [Configuration](./configuration.md)).
4. Reload or restart.

### Optional: ProtocolLib

Install [ProtocolLib](https://www.spigotmc.org/resources/protocollib.1997/) for:

- `BadPacketCheck` — server vs client position divergence
- Higher-fidelity packet sampling for `BlinkCheck`

Without ProtocolLib, movement-based packet sampling still powers blink detection.

## Storage Backends

Default: **SQLite** (`plugins/TSSpear/tsspear.db`).

For MySQL or PostgreSQL, set `storage.backend` in config (see [Storage](./storage.md)).

## Permissions

Grant staff permissions as needed (see [Commands](./commands.md)). Base permission:

```yaml
tsspear.use: true  # default: op
```

## Verify Installation

```
/ts debug profile
/ts inspect <player>
```

You should see performance metrics and confidence breakdown output.
