# Building & Testing the Plugin

This plugin is a standalone RuneLite external plugin built with Gradle. There are two
ways to run it: a **development loop** that launches RuneLite with the plugin loaded
straight from the classpath, and an **install loop** that drops the built jar into your
normal RuneLite client.

---

## Prerequisites

| Requirement | Notes |
|---|---|
| JDK 11+ | Production target is Java 11 (`options.release = 11`). A newer JDK (e.g. 21) builds it fine. |
| Internet access | First build downloads Gradle 8.10 and the RuneLite client artifact from `repo.runelite.net`. |
| RuneLite client | Needed only for side-loading (install loop). The dev loop downloads what it needs. |

The Gradle wrapper is committed, so you do **not** need Gradle installed — `./gradlew`
bootstraps it.

---

## Development loop (recommended)

```
./gradlew run
```

This runs `PvpEnhancerTest.main()`, which calls
`ExternalPluginManager.loadBuiltin(PvpEnhancerPlugin.class)` and then `RuneLite.main()`
with `--developer-mode --debug`. A full RuneLite client launches with the plugin already
loaded. Edit code, stop the client, re-run. No jar is built or copied.

This is the fastest iteration path and the one to use while developing.

---

## Install loop (use the plugin in your real client)

To run the plugin inside the RuneLite client you normally play on:

```
./gradlew build
```

`build` compiles, runs the unit tests, produces `build/libs/pvp-enhancer-<version>.jar`,
and — because `build` is wired to finalize with `installPlugin` — copies that jar into:

```
~/.runelite/sideloaded-plugins/
```

(`~` is your home directory; on Windows that is `C:\Users\<you>`.) The directory is
created if it does not exist.

Then start RuneLite with the developer-mode flag so it loads side-loaded plugins:

```
runelite --developer-mode
```

The plugin appears in the plugin list as **PvP Enhancer**.

To build without installing:

```
./gradlew build -x installPlugin
```

To install without a full rebuild (after a prior build):

```
./gradlew installPlugin
```

> **Why `sideloaded-plugins` and not `plugins/`?** The `~/.runelite/plugins/` directory
> holds Plugin Hub jars managed (and overwritten) by the client's hub updater — manually
> placed jars there are fragile. `sideloaded-plugins/` is the sanctioned location for
> local developer side-loading.

---

## Running the tests

```
./gradlew test
```

Unit tests live in `src/test/java`. `TickHistoryServiceTest` covers the buffer's cap,
ordering, grouping, and trim behaviour. The test source set also holds the dev-harness
`PvpEnhancerTest` (a `main()`, not a JUnit test).

---

## What the build cannot verify

Overlay rendering, attack-style accuracy, prayer detection, and in-game feel require a
live OSRS client and a real fight. Those checks live in `.ai/human-testing.md` as
`HT-NNN` items. A green `./gradlew build` proves the code compiles and the service logic
is correct — it does **not** prove the in-game behaviour is right.

---

## Troubleshooting

| Symptom | Fix |
|---|---|
| `./gradlew` fails to find Java | Set `JAVA_HOME` to a JDK 11+ install, or put `java` on `PATH`. |
| First build hangs | It is downloading Gradle + the RuneLite client jar. Give it a few minutes. |
| `Could not resolve net.runelite:client` | Check internet access to `https://repo.runelite.net`. |
| Plugin not listed after install | Confirm RuneLite was started with `--developer-mode` and the jar is in `~/.runelite/sideloaded-plugins/`. |
| Want to stop auto-install on every build | `./gradlew build -x installPlugin`. |
