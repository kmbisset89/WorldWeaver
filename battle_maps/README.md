# Dynamic 20 × 20 Battle Map Collection

This finished package contains **30 distinct fantasy tabletop RPG encounter locations**. Each map represents a **100 × 100-foot** playable area designed for a **20 × 20 grid at 5 feet per square**. All **40 PNG files** are square **1920 × 1920 px** images, yielding **96 px per 5-foot square**.

The artwork is deliberately **gridless**, with no text, labels, characters, tokens, UI, compass, or borders. This lets the map sit cleanly under the native grid and lighting controls of a virtual tabletop. When importing, align the map to **20 columns × 20 rows** and set the grid scale to **5 feet per square**.

## Contents

| Group | Maps | Files | Intended use |
| --- | ---: | ---: | --- |
| Dynamic encounter sequences | 5 locations | 15 images | Swap stages during play as the environment changes. |
| Standalone encounter maps | 25 locations | 25 images | Use as complete one-scene battle maps. |
| Total | 30 locations | 40 images | A varied fantasy encounter collection. |

## Dynamic encounter sequences

The three images in each sequence use matching square framing and preserve key landmarks to support stage swaps. The dynamic changes create a visible tactical shift without replacing the whole encounter.

| ID | Stage files | Encounter progression |
| --- | --- | --- |
| 01 | `01_whispering_shrine_A_calm.png` → `01_whispering_shrine_B_falling_trees.png` → `01_whispering_shrine_C_wildfire.png` | A quiet forest shrine becomes storm-damaged, then threatened by a brush fire. |
| 02 | `02_riverside_ruins_A_low_water.png` → `02_riverside_ruins_B_rising_water.png` → `02_riverside_ruins_C_flooded.png` | River routes become shallows and then a battlefield of elevated islands. |
| 03 | `03_ember_granary_A_smoke.png` → `03_ember_granary_B_spreading_fire.png` → `03_ember_granary_C_inferno.png` | An early smoulder advances through hay, barn, and granary edges. |
| 04 | `04_thawing_ice_cavern_A_frozen.png` → `04_thawing_ice_cavern_B_thaw.png` → `04_thawing_ice_cavern_C_flooded_cavern.png` | Solid ice breaks into floes and then leaves a flooded basin. |
| 05 | `05_collapsing_mine_A_intact.png` → `05_collapsing_mine_B_cave_in.png` → `05_collapsing_mine_C_blocked_flooded.png` | Stable mine works develop rubble blockades and water intrusion. |

## Standalone encounter index

| ID | Filename | Scene | Principal tactical features |
| --- | --- | --- | --- |
| 06 | `06_greenwood_clearing.png` | Greenwood Clearing | Giant oak, dry creek, hunter’s blind, boulders, fallen trunk. |
| 07 | `07_sunken_temple_court.png` | Sunken Temple Court | Ruined plaza, dry reflecting pool, columns, breached walls, raised paths. |
| 08 | `08_jungle_ziggurat.png` | Jungle Ziggurat | Stepped pyramid, central platform, stairs, vines, rain channel. |
| 09 | `09_overgrown_keep.png` | Overgrown Keep | Breached walls, tower footings, dry fountain, courtyard cover. |
| 10 | `10_fey_mushroom_ring.png` | Fey Mushroom Ring | Luminous fungi, standing stones, pond, root bridge, open glade. |
| 11 | `11_cliffside_pass.png` | Cliffside Pass | Switchback trail, rope bridge, chasm, camp, stone spires. |
| 12 | `12_moonlit_graveyard.png` | Moonlit Graveyard | Mausoleums, headstones, broken fence, yew tree, open grave. |
| 13 | `13_bone_crypt.png` | Bone Crypt | Sarcophagi, ossuary alcoves, columns, central dais, side chamber. |
| 14 | `14_crossroads_tavern.png` | Crossroads Tavern | Bar, common room, hearth, tables, side rooms, stairs. |
| 15 | `15_sewer_junction.png` | Sewer Junction | Brick channels, raised paths, sluice gates, bridge, drains. |
| 16 | `16_prison_cellblock.png` | Prison Cellblock | Cells, barred gates, guard post, armory cage, exercise yard. |
| 17 | `17_alchemists_workshop.png` | Alchemist’s Workshop | Furnace, vats, worktables, shelves, spill hazards, storage. |
| 18 | `18_grand_arcane_library.png` | Grand Arcane Library | Bookcase aisles, reading dais, study tables, spiral stair. |
| 19 | `19_market_square.png` | Market Square | Fountain, stalls, wagon, awnings, guild steps, alleys. |
| 20 | `20_dockside_warehouse.png` | Dockside Warehouse | Cargo stacks, dock, water edge, crane, loading office. |
| 21 | `21_storm_tossed_ship_deck.png` | Storm-Tossed Ship Deck | Mast bases, wet deck, hatches, raised decks, railings, crates. |
| 22 | `22_smugglers_sea_cave.png` | Smuggler’s Sea Cave | Tide pool, dock, rock arches, crates, boat, dry ledges. |
| 23 | `23_desert_oasis.png` | Desert Oasis | Spring, palms, waystation, sandstone cover, dry creek. |
| 24 | `24_canyon_bridge.png` | Canyon Bridge | Stone bridge, ravine, ruined gatehouse, camp, rope lines. |
| 25 | `25_volcanic_forge.png` | Volcanic Forge | Basalt walkways, lava channels, anvil, forge hearths, hoists. |
| 26 | `26_glacier_fissure.png` | Glacier Fissure | Deep crevasse, ice bridge, snow shelves, rope rail, sled. |
| 27 | `27_frozen_lake_camp.png` | Frozen Lake Camp | Fractured lake ice, huts, camp, pressure ridge, shore. |
| 28 | `28_dwarven_hall.png` | Dwarven Hall | Great pillars, banquet table, forge niches, stone bridge, galleries. |
| 29 | `29_clockwork_foundry.png` | Clockwork Foundry | Brass gears, conveyor tracks, catwalks, pits, furnace core. |
| 30 | `30_astral_observatory.png` | Astral Observatory | Telescope dais, radial bridges, floating cover, void hazards. |

## Import and play

Use the files at their native size. Create a square scene, assign the scene grid to **20 squares wide** and **20 squares tall**, and configure the scale as **5 feet per square**. The resulting map covers 100 feet on each side. For a dynamic encounter, place the three stage maps on separate scenes with identical grid configuration, or replace the background image without changing the grid settings.

The environmental state is indicated only by the filename; there are **no embedded labels or visual UI elements** in the map artwork. The five sequences are intentionally designed with changing paths, cover, difficult terrain, and hazards so that the battlefield remains tactically interesting after each swap.

> The package uses a high-detail painted, orthographic fantasy style inspired by the environmental richness and tabletop clarity of the supplied reference image, while every map is a newly generated scene.

