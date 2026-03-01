# EzCommandBlocker

EzCommandBlocker is a Paper 1.21.1 plugin (with Folia support) and Velocity plugin for blocking or whitelisting commands, filtering tab completion per permission group, and executing custom actions when commands are blocked.

## Features

- Command whitelist/blacklist mode with cached config lookups.
- Optional blocking of namespace commands containing `:`.
- Per-group tab completion filtering with permission-based priority selection.
- Recursive tab-group inheritance via `extends` chains.
- Action engine for blocked commands:
  - `message`
  - `console_command`
  - `player_command`
  - `kick`
- Folia-aware scheduler helper and `folia-supported: true` plugin metadata.
- Admin reload command: `/ezcommandblocker reload` (alias `/ecb reload`).
- Velocity proxy command filtering using the same whitelist/blacklist command model.

## Permissions

| Permission | Description | Default |
| --- | --- | --- |
| `ezcommandblocker.admin` | Access to reload and admin commands | op |
| `ezcommandblocker.bypass` | Bypass all command blocking | op |
| `ezcommandblocker.bypass.tab` | Bypass tab completion filtering | op |
| `ezcommandblocker.tab.*` | Wildcard for all tab groups | false |
| `ezcommandblocker.notify` | Receive update notifications on join | op |
| `ezcommandblocker.tab.<group>` | Access to a specific tab group | false |

## Configuration Overview

Configuration is located in `src/main/resources/config.yml` and is copied to the plugin data folder on first startup.

Velocity-specific configuration is located in `src/main/resources/velocity-config.yml` and copied to the proxy plugin data folder as `config.yml`.

- `tab`: Defines tab completion groups.
  - Each group has `priority`, `commands`, and optional `extends`.
  - Active group is highest-priority permitted group; otherwise `default`.
- `blocked_command_default_actions`: Default actions when a blocked command is used.
- `custom_commands_actions`: Per-command-group action overrides.
- `update_notify`: Toggle update notifications (for future update checker integration).
- `is_network`: Toggle network/proxy aware mode.
- `legacy_support`: Reserved legacy mode flag.
- `use_commands_as_whitelist`: `true` = allow only listed commands; `false` = block listed commands.
- `commands`: Main command list used by whitelist/blacklist mode.
- `block_colon_commands`: Blocks namespaced commands like `/bukkit:help` when enabled.

## Build

### Prerequisites

- JDK 21
- Gradle (or the included wrapper after running `gradle wrapper`)

### Commands

```bash
gradle wrapper
./gradlew build
```

Built JAR outputs:

```text
build/libs/EzCommandBlocker-paper-<version>.jar
build/libs/EzCommandBlocker-velocity-<version>.jar
```

Use the `-paper-` artifact on Paper/Folia servers, and the `-velocity-` artifact on Velocity proxies.

## Release Workflow

Tag pushes in the form `v*` trigger GitHub Actions to build and publish a Release with the JAR attached.

```bash
git tag v1.0.0
git push origin v1.0.0
```
