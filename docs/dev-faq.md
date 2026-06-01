# Developer FAQ

Hard-won answers to the questions that come up while developing this plugin. If you hit
something not here, add it.

---

## Running & loading

### Q: I built the plugin and started RuneLite from the Jagex Launcher (or `Desktop/RuneLite.jar`), but the plugin doesn't appear. Is that wrong?

No, that is expected. The Jagex Launcher (and the desktop `RuneLite.jar`) start the
**production** client. The production client only loads external plugins from the
**Plugin Hub** — it does **not** reliably load locally side-loaded jars. So a jar you
built and dropped into `~/.runelite/sideloaded-plugins/` is invisible to it.

**Use `./gradlew run` instead** — that is the supported development path. The only way to
get the plugin into the everyday Jagex-launched client is to publish it to the Plugin Hub.

### Q: How do I actually run the plugin during development?

```
./gradlew run
```

This launches a full RuneLite client in developer mode with the plugin loaded from the
classpath (`ExternalPluginManager.loadBuiltin` in `PvpEnhancerTest`). Edit code, stop the
client, run again — changes take effect with no jar build or copy.

### Q: `./gradlew run` opens RuneLite but it shows the OLD login screen and asks for a legacy account. I only have a Jagex account.

This is the big one we tripped over. The dev client runs **outside** the Jagex Launcher,
so it has no Jagex session and falls back to the legacy username/password login — which a
migrated Jagex account cannot use.

**Fix** (full steps in `building-and-testing.md` → "Logging in with a Jagex account"):

1. Open **"RuneLite (configure)"** (Start menu).
2. In **"Client arguments"** set `--insecure-write-credentials`. Save.
3. Launch OSRS through the **Jagex Launcher** once and log in. This writes
   `~/.runelite/credentials.properties`.
4. Run `./gradlew run` — the dev client reads that file and logs into your Jagex account.

⚠️ `credentials.properties` grants account access **without a password**. Never share or
commit it; delete it or use "End sessions" in RuneLite account settings when done. If the
dev client later can't log in, the session expired — relaunch via the Jagex Launcher to
regenerate it.

### Q: "PvP Enhancer" appears TWICE in the plugin list.

The plugin is being loaded by two paths at once:

1. `./gradlew run` loads it from the classpath (`loadBuiltin`), **and**
2. developer mode also side-loads the jar if one is sitting in
   `~/.runelite/sideloaded-plugins/` (e.g. left there by `./gradlew installPlugin`).

**Fix:** delete the jar from `~/.runelite/sideloaded-plugins/` and just use `./gradlew run`.
The `installPlugin` task is opt-in and is deliberately **not** wired into `build` for
exactly this reason. Never combine `installPlugin` with `run`.

> If the file won't delete ("being used by another process"), the RuneLite client is
> still open — close it first.

### Q: There are `LambdaConversionException` / `InaccessibleObjectException` warnings in the log.

- `LambdaConversionException: Invalid caller` only happens for **side-loaded** plugins
  (they live in a child classloader, so the EventBus can't build a fast lambda and falls
  back to reflection). With `./gradlew run` (classpath / `loadBuiltin`) it does not occur.
- `InaccessibleObjectException: ... java.lang.reflect ...` is a JDK 17+ module-access
  issue. The `run` task passes `--add-opens=java.base/java.lang.reflect=ALL-UNNAMED`
  (and `java.lang`) to fix it. If you launch the dev client some other way, pass the same.

---

## Testing

### Q: How do I test combat detection without a second player?

Enable **"Track NPCs (testing)"** in the plugin config, then attack any NPC (a training
dummy, a chicken, etc.). You will see your own attacks (target = the NPC's name) and the
hitsplats landing on it. NPCs cannot report some fields — overhead prayer in particular —
so those are left blank by design. NPC tracking is off by default and is a testing aid
only.

### Q: An attack shows style `?` (or nothing shows for an attack).

The attack animation id is not in `AnimationStyleMap` yet. The plugin logs unmapped
attack-like animations at debug level:

```
Unmapped animation <id> by <attacker> -> <target>
```

Grab the id from the log and add it to `model/AnimationStyleMap.java` with a wiki citation
(see `.ai/game/pvp/pvp-combat-events.md`). This is backlog item **B008**.

### Q: How are unit tests structured? Why an extra `Combatant` interface?

Detection logic is written against the `com.knz.pvpenhancer.combatant.Combatant`
interface, not RuneLite's `Player`/`NPC` directly. `PlayerCombatant` and `NpcCombatant`
adapt those two RuneLite types to the one interface. This keeps the recording logic
type-agnostic **and** trivially mockable: `CombatEventFactoryTest` builds attacks from
`mock(Combatant.class)` with no live client. Run all tests with `./gradlew test`.

---

## Logs & debugging

### Q: Where are the logs, and how do I read them?

`~/.runelite/logs/` — `client.log` (the client) and `launcher.log` (the launcher).
Useful greps:

- `starting up, args:` — confirms developer mode is on and shows the launch arguments.
- `Side-loading plugin` — a jar was loaded from `sideloaded-plugins/`.
- `Loaded plugin PvpEnhancerPlugin` / `is now running` — our plugin loaded/started.

Hand the path to the AI agent and it will read it with the dedicated tools.
