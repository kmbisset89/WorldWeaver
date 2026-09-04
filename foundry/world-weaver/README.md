# World Weaver for Foundry VTT

Companion module for [World Weaver](https://github.com/kmbisset89/WorldWeaver). It reads a `.wwbundle` from disk and creates journals and scenes. There is no World Weaver account, no cloud API, and no live combat or fog sync.

World Weaver stays the GM laptop. Foundry stays the remote table. Re-import the same bundle to refresh content.

## Install

1. Copy this `world-weaver` folder into Foundry’s `Data/modules/` directory so the path is `Data/modules/world-weaver/module.json`.
2. Restart Foundry (or reload the setup screen) and enable **World Weaver** in the world.
3. In World Weaver, export the world from **Worlds** to a `.wwbundle`.
4. In Foundry, open the **Journal** directory and click **Import World Weaver bundle**. Choose the `.wwbundle`.

Requires Foundry VTT v12 or later.

## What is imported

- **Lore** → journal entries, grouped by category. Secret pages use GM-only ownership. Players never see those pages.
- **Locations** → journals in type folders (continent, area, city, place).
- **Battle maps** → scenes with the background PNG and square grid (columns, rows, feet per square). Walls and lights are not created.

Character sheets, encounters, and fog stay in World Weaver. This module does not create `dnd5e` or `pf2e` actors.

Documents are tagged with a World Weaver source id. Importing the same bundle again updates those journals and scenes instead of duplicating them.

## What this is not

- Not a live bridge for HP, tokens, or fog of war.
- Not a replacement for [Universal Battlemap Importer](https://foundryvtt.com/packages/dd-import/). For a single map with grid alignment, export `.uvtt` from World Weaver **Maps → Export VTT** and import that file instead.

See [docs/FOUNDRY.md](../../docs/FOUNDRY.md) in the World Weaver repository.
