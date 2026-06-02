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

This is the fastest iteration path and the one to use while developing. It is also the
**recommended way to run the plugin at all** — the production client launched by the
Jagex Launcher does not reliably load side-loaded plugins (its real distribution channel
is the Plugin Hub), so until the plugin is published there, `./gradlew run` is how you use it.

---

## Auto clean-build (`scripts/clean-build.sh`)

`scripts/clean-build.sh` does two things:

1. removes any leftover `pvp-enhancer*.jar` from `~/.runelite/sideloaded-plugins/` (so
   `./gradlew run` never loads the plugin twice — see the duplicate-plugin note below), then
2. runs `./gradlew build`, logging to `build/clean-build.log`.

Run it manually any time:

```
bash scripts/clean-build.sh
```

It is also wired as a **Stop hook** so it runs automatically after each change. The hook
lives in `.claude/settings.local.json` (personal, git-ignored — it references your local
`~/.runelite` path):

```json
{
  "hooks": {
    "Stop": [{ "hooks": [{
      "type": "command",
      "command": "bash <abs-path>/scripts/clean-build.sh",
      "async": true, "asyncRewake": true
    }]}]
  }
}
```

`async` means it never delays the turn; `asyncRewake` means a **failed** build pings the
agent to fix it (a green build is silent). Note: after first creating that file you must
open `/hooks` once (or restart) so Claude Code's settings watcher picks it up.

---

## Logging in with a Jagex account

`./gradlew run` launches RuneLite **outside** the Jagex Launcher. Without the Jagex
session it falls back to the legacy username/password login screen — which a migrated
Jagex account cannot use. Symptom: "the dev client asks for a legacy account".

Fix: have the Jagex Launcher write its session to a credentials file the dev client reads.

1. Update the RuneLite launcher to **2.6.3+** (check: it is shown in the client log; 2.7.7 is fine).
2. Open **"RuneLite (configure)"** (Start menu) — or run
   `C:\Users\<you>\AppData\Local\RuneLite\RuneLite.exe --configure`.
3. In **"Client arguments"** set:
   ```
   --insecure-write-credentials
   ```
   (Replace the earlier `--developer-mode` here — the production client does not need it;
   the `run` task already passes `--developer-mode` to the dev client.)
4. **Save**, then launch OSRS through the **Jagex Launcher** once and log in. This writes
   `~/.runelite/credentials.properties`.
5. Run `./gradlew run`. The dev client reads `credentials.properties` and logs into your
   Jagex account, with the plugin loaded.

> ⚠️ **Security:** `credentials.properties` grants access to your account **without a
> password**. Never share or commit it. Delete it when done, or click **"End sessions"**
> in RuneLite account settings to invalidate it. If the dev client later fails to log in,
> the session expired — relaunch via the Jagex Launcher (flag still set) to regenerate it.

---

## Side-loading (opt-in, advanced — usually not needed)

`./gradlew run` is the way to run the plugin. Side-loading is a separate, optional path
for loading the built jar into a standalone client started with `--developer-mode`.

```
./gradlew installPlugin
```

This copies `build/libs/pvp-enhancer-<version>.jar` into `~/.runelite/sideloaded-plugins/`
(created if missing). Side-loaded jars load only when the client runs in **developer
mode** (`PluginManager.loadSideLoadPlugins()` is gated on it).

> ⚠️ **Do not combine `installPlugin` with `./gradlew run`.** `run` already loads the
> plugin from the classpath; if the jar is also sitting in `sideloaded-plugins/`,
> developer mode loads it a **second time** and **the plugin appears twice**. For this
> reason `installPlugin` is opt-in and is **not** wired into `build`. If you ever see two
> "PvP Enhancer" entries, delete the jar from `~/.runelite/sideloaded-plugins/` and just
> use `./gradlew run`.

> ⚠️ Side-loading into the **Jagex Launcher** production client is unreliable and not its
> intended use — the supported channel for the everyday client is the Plugin Hub. Prefer
> `./gradlew run` (above) with the Jagex-account credentials file.

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
