---
purpose: RuneLite plugin development reference — structure, annotations, lifecycle, overlays, config, events.
scope: AI reference for implementing any RuneLite plugin feature
load_when: implementing plugin classes, overlays, config, event subscribers, or build/run setup
sources:
  - https://github.com/runelite/runelite/wiki/Developer-Guide
  - https://github.com/runelite/runelite/wiki/Creating-plugin-config-panels
  - https://github.com/runelite/runelite/wiki/Building-with-IntelliJ-IDEA
  - https://static.runelite.net/runelite-api/apidocs/
  - https://static.runelite.net/runelite-client/apidocs/
---

# RuneLite Plugin Development Reference

## Plugin Anatomy

A plugin is typically 3–5 files:

```
MyPlugin.java          extends Plugin, annotated @PluginDescriptor
MyConfig.java          extends Config, annotated @ConfigGroup
MyOverlay.java         extends Overlay (or OverlayPanel)
MyService.java         stateful helper, @Singleton if needed
```

## Plugin Class

```java
@PluginDescriptor(
    name = "My Plugin",
    description = "What it does",
    tags = {"pvp", "combat"}
)
public class MyPlugin extends Plugin {

    @Inject private Client client;
    @Inject private OverlayManager overlayManager;
    @Inject private MyOverlay overlay;
    @Inject private MyConfig config;

    @Provides
    MyConfig provideConfig(ConfigManager configManager) {
        return configManager.getConfig(MyConfig.class);
    }

    @Override
    protected void startUp() {
        overlayManager.add(overlay);
    }

    @Override
    protected void shutDown() {
        overlayManager.remove(overlay);
    }

    @Subscribe
    public void onGameTick(GameTick event) { ... }
}
```

Key rules:
- `@Provides` method is mandatory for config injection.
- `startUp` / `shutDown` are the only safe places to register/deregister overlays.
- All `@Subscribe` methods must be `public void on<EventName>(<EventType> event)`.
- RuneLite auto-registers/deregisters `@Subscribe` methods when the plugin starts/stops — no manual eventBus calls needed.

## Config Interface

```java
@ConfigGroup("myplugin")
public interface MyConfig extends Config {

    @ConfigItem(keyName = "showPanel", name = "Show panel", description = "Enables the overlay panel")
    default boolean showPanel() { return true; }

    @Range(min = 5, max = 100)
    @ConfigItem(keyName = "maxTicks", name = "Max ticks", description = "History depth")
    default int maxTicks() { return 20; }
}
```

Supported return types → UI widget:
- `boolean` → checkbox
- `int` (with optional `@Range`) → spinner or slider
- `String` → text field
- `Color` → colour picker
- `enum` → dropdown
- `Keybind` → keybind input

RuneLite persists config automatically. No manual save/load required.

## Overlays

Two base classes:
- `Overlay` — raw `Graphics2D` canvas. Full control. Use for custom rendering.
- `OverlayPanel` — pre-built panel with `panelComponent`. Renders `LineComponent`, `TitleComponent`, `TableComponent`, etc. Use for text-based info panels.

```java
public class MyOverlay extends OverlayPanel {

    @Inject
    public MyOverlay(MyPlugin plugin, MyConfig config) {
        super(plugin);
        setPosition(OverlayPosition.TOP_LEFT);
        setPriority(OverlayPriority.LOW);
    }

    @Override
    public Dimension render(Graphics2D graphics) {
        if (!config.showPanel()) return null;

        panelComponent.getChildren().add(TitleComponent.builder().text("PvP History").build());
        panelComponent.getChildren().add(LineComponent.builder()
            .left("Tick 42")
            .right("hit 15")
            .build());

        return super.render(graphics);
    }
}
```

Overlay positions: `TOP_LEFT`, `TOP_CENTER`, `TOP_RIGHT`, `BOTTOM_LEFT`, `BOTTOM_RIGHT`, `CANVAS_TOP_RIGHT`, `ABOVE_CHATBOX_RIGHT`, `DYNAMIC`.
Always call `panelComponent.getChildren().clear()` at the start of `render()` to avoid stale entries.

## Key Events

| Event | Trigger | Key fields |
|---|---|---|
| `GameTick` | Every 600ms server tick, after all packets | use `client.getTickCount()` for current tick number |
| `ClientTick` | Every ~20ms client frame | use sparingly — fires ~30x per game tick |
| `HitsplatApplied` | A hitsplat is applied to any actor | `.getActor()`, `.getHitsplat()` → `.getAmount()`, `.getHitsplatType()` |
| `AnimationChanged` | Any actor's animation changes | `.getActor()` — then call `actor.getAnimation()` |
| `InteractingChanged` | Any actor's interaction target changes | `.getActor()`, `.getTarget()` (may be null) |
| `MenuOptionClicked` | Player right-clicks and selects an option | `.getMenuOption()`, `.getMenuTarget()`, `.getItemId()` |
| `PlayerSpawned` / `PlayerDespawned` | Player enters/leaves render distance | `.getPlayer()` |
| `StatChanged` | Skill XP or boosted level changes | `.getSkill()`, `.getLevel()`, `.getBoostedLevel()` |
| `VarbitChanged` | Any varbit or varplayer changes | `.getVarbitId()`, `.getValue()` |

## Client API (key methods)

```java
client.getTickCount()           // current server tick (int)
client.getLocalPlayer()         // Player — the logged-in player
client.getPlayers()             // List<Player> — all visible players
client.getNpcs()                // List<NPC> — all visible NPCs
client.getItemDefinition(id)    // ItemComposition — name, noted, etc.
```

## Actor API

```java
actor.getName()                 // String
actor.getAnimation()            // int — current animation ID (-1 = idle)
actor.getInteracting()          // Actor — who this actor is targeting (null if none)
actor.getHealthRatio()          // int — current HP ratio (0–healthScale)
actor.getHealthScale()          // int — max health scale value
actor.getWorldLocation()        // WorldPoint
actor.hasSpotAnim(int id)       // boolean — check for a specific spot animation (replaces deprecated getGraphic)
actor.getSpotAnims()            // set of active spot animation IDs
```

## Player API (extends Actor)

```java
player.getCombatLevel()         // int
player.getOverheadIcon()        // HeadIcon enum (null if no overhead prayer)
player.getSkullIcon()           // int — skull icon ID, -1 if unskulled
player.getPlayerComposition()   // PlayerComposition
player.isFriend()
player.isClanMember()
player.getTeam()                // int — team cape number (0 = none)
```

## PlayerComposition API

```java
composition.getEquipmentIds()           // int[] — one entry per KitType slot
composition.getEquipmentId(KitType)     // int — ID for specific slot
```

Equipment ID formula: if `id >= KIT_OFFSET && id < ITEM_OFFSET` → kit (cosmetic). If `id >= ITEM_OFFSET` → real item. Real item ID = `id - ITEM_OFFSET`.

`KitType` slots: `HEAD`, `CAPE`, `AMULET`, `WEAPON`, `TORSO`, `SHIELD`, `LEGS`, `GLOVES`, `BOOTS`, `RING`, `AMMO`.

## HeadIcon Enum

Values relevant to OSRS PvP:

| HeadIcon | Meaning |
|---|---|
| `MELEE` | Protect from Melee |
| `RANGED` | Protect from Ranged |
| `MAGIC` | Protect from Magic |
| `SMITE` | Smite |
| `RETRIBUTION` | Retribution |
| `REDEMPTION` | Redemption |
| `SOUL_SPLIT` | Soul Split (ancient curses) |
| `DEFLECT_MELEE` / `DEFLECT_RANGE` / `DEFLECT_MAGE` | Ancient curse deflects |

`player.getOverheadIcon()` returns `null` if the player has no overhead prayer active.

## Build & IntelliJ Setup

Prerequisites: JDK 11 (Eclipse Temurin / AdoptOpenJDK HotSpot), IntelliJ IDEA Community.

Setup:
1. Clone `https://github.com/runelite/runelite` (or a plugin-template fork).
2. File → Project Structure → Project SDK → Download JDK → version 11, vendor Eclipse Temurin → Language level 11.
3. Locate `runelite-client/src/main/java/net/runelite/client/RuneLite.java` → right-click → Run.

Troubleshooting:
- Failing tests → delete `%TEMP%/cache-165` or skip tests in run configuration.
- Client won't start → run Gradle target `:cleanAll`.
- Gradle changes not detected → "Sync All Gradle Projects" (Ctrl+Shift+A).
- Hot reload → see JetBrains documentation on DCEVM / HotSwap.

Plugin Hub distribution:
- External plugins distributed via Plugin Hub are the standard path for community plugins.
- Requires a separate fork of the `runelite/plugin-hub` repository.
- Core RuneLite contributions are reserved for bug fixes; new features go to Plugin Hub.
