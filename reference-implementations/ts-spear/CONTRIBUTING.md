# Contributing to TS-Spear

Thank you for contributing to the BadgersMC / Thinking System anti-cheat intelligence engine.

## Principles

1. **Checks emit evidence, never punish.** No `ban()`, `kick()`, or silent enforcement in check code.
2. **Domain stays pure.** No Bukkit/Paper imports in `core/domain`.
3. **Prove before implement.** Add or extend tests with every behavior change.
4. **Receipts for mutations.** Confidence changes must produce `ConfidenceReceipt` records.
5. **Explainability first.** Staff-facing output must decompose scores into evidence.

## Development Setup

```bash
git clone https://github.com/BadgersMC/spear-plugin.git
cd spear-plugin/reference-implementations/ts-spear
./gradlew build
```

See [docs/DEVELOPMENT.md](docs/DEVELOPMENT.md) for module layout and commands.

## Pull Request Process

1. Fork and create a feature branch from `main`.
2. Run `./gradlew build` — all tests must pass.
3. Update documentation if you change config, commands, schemas, or public API.
4. Write a clear PR description: what, why, how to verify.
5. Reference related issues or design doc sections.

## Code Style

- Match existing naming and package structure.
- Prefer records and immutable snapshots where the domain already does.
- Keep changes focused — avoid drive-by refactors.
- Java 21 toolchain.

## Architecture References

- [Architecture Design](docs/architecture/TS-SPEAR-ARCHITECTURE.md)
- [Core Interfaces](docs/architecture/CORE-INTERFACES.md)
- [SPEAR Methodology](https://github.com/BadgersMC/spear-plugin)

## Reporting Issues

Include:

- Paper version and TS-Spear version
- Relevant `config.yml` sections
- Steps to reproduce with `/ts debug` output
- Expected vs actual confidence behavior