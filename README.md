# Game Design Specification: 2D RPG (libGDX / Java 21)

## 1. Technical Baseline
* **Language:** Java 21
* **Framework:** libGDX
* **Build System:** Gradle
* **Architectural Pattern:** Model-View-Controller (MVC) / Screen-Based State Management

---

## 2. Core Screen Specifications

### 2.1 Overworld View (`OverworldScreen`)
* **Objective:** Provide free-form grid/top-down movement for the player character.
* **Current Scope:**
    * Render a single player sprite centered on a neutral background.
    * Basic 4-directional movement (`UP`, `DOWN`, `LEFT`, `RIGHT`) using keyboard inputs (`WASD` / Arrow Keys).
    * Basic orthographic camera following the player entity.

### 2.2 Battle View (`BattleScreen`)
* **Objective:** Turn-based combat interface.
* **Current Scope:**
    * Render a distinct battle scene/background.
    * Render a 4-option UI menu panel built using libGDX `Scene2D.ui`.
    * **Menu Choices:**
        1. **Fight:** Opens combat action submenu.
        2. **Bag:** Opens item usage/inventory overlay.
        3. **Bench:** Opens party management overlay.
        4. **Run:** Triggers escape logic and switches state back to `OverworldScreen`.

---

## 3. Application State & Screen Flow

```
+-------------------+             Trigger Encounter / Key Press
|                   | --------------------------------------> +-------------------+
|  OverworldScreen  |                                         |    BattleScreen   |
| (Movement Loop)   | <-------------------------------------- |  (4-Option Menu)  |
+-------------------+           Select "Run" / Win Battle     +-------------------+
```

---

## 4. Module Roadmap
Detailed specifications for gameplay mechanics, entity properties, and UI layouts are tracked in dedicated documentation files (will be added later):
* `docs/combat_system.md`
* `docs/entity_system.md`
* `docs/inventory_system.md`