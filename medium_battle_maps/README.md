# Medium 30 × 30 Battle Map Collection

This package contains **30 medium-scale fantasy tabletop RPG locations**. Each location is built for a **30 × 30-square VTT grid**, representing a **150 × 150-foot** play area at **5 feet per square**. All **40 map images** are gridless, square **1920 × 1920 px PNGs**, providing **64 px per 5-foot square** when aligned to the intended grid.

The maps are designed for larger tactical scenes than the 20 × 20 collection. Their expanded layouts offer longer sightlines, broad maneuvering space, multiple routes, and layered terrain while retaining distinct obstacles, cover, walls, water, elevation changes, and landmarks. No image contains characters, tokens, text, labels, UI, compass marks, borders, or an embedded grid.

## Contents

| Group | Locations | Images | Purpose |
| --- | ---: | ---: | --- |
| Dynamic encounter sequences | 5 | 15 | Three battlefield states per location for encounter escalation. |
| Standalone encounter maps | 25 | 25 | Complete one-scene battle maps for medium-sized engagements. |
| **Total** | **30** | **40** | Gridless 30 × 30 tactical maps. |

## Dynamic encounter sequences

Each sequence retains the same overhead camera, core layout, and primary landmarks from stage to stage. Switch images when the environmental or structural event occurs, retaining a 30 × 30 grid on every scene.

| ID | Files | Tactical escalation |
| --- | --- | --- |
| 01 | `01_stormwatch_keep_A_holding.png` → `01_stormwatch_keep_B_gate_breached.png` → `01_stormwatch_keep_C_inner_bailey_burning.png` | A stronghold gate breaks, followed by discrete fires and smoke hazards in the outer ward. |
| 02 | `02_flooded_harbor_A_low_tide.png` → `02_flooded_harbor_B_storm_surge.png` → `02_flooded_harbor_C_flooded_harbor.png` | Exposed mud flats become waterlogged quays and then a network of inundated streets and elevated routes. |
| 03 | `03_sinking_jungle_city_A_dry_season.png` → `03_sinking_jungle_city_B_monsoon.png` → `03_sinking_jungle_city_C_sunken_plaza.png` | Empty canals fill during a monsoon and eventually split the plaza into raised ruin islands. |
| 04 | `04_great_river_bridge_A_intact.png` → `04_great_river_bridge_B_partial_collapse.png` → `04_great_river_bridge_C_burning_barricades.png` | The bridge becomes a rubble chokepoint before its approaches receive separate burning barricades. |
| 05 | `05_glacial_vale_A_frozen_vale.png` → `05_glacial_vale_B_avalanche.png` → `05_glacial_vale_C_spring_thaw.png` | A snowfall blocks terrain, then thawing turns the frozen river into water channels and ice floes. |

## Standalone map index

| ID | Filename | Location | Tactical features |
| --- | --- | --- | --- |
| 06 | `06_riverford_village.png` | Riverford Village | Broad ford, mill race, bridge, cottages, orchard, and farm lanes. |
| 07 | `07_castle_outer_ward.png` | Castle Outer Ward | Gatehouse, stables, barracks, wall walks, training green, and inner gate. |
| 08 | `08_desert_caravanserai.png` | Desert Caravanserai | Open courtyard, cistern, stables, arcades, roof stairs, and gates. |
| 09 | `09_port_town_square.png` | Port Town Square | Market, fountain, tavern, chapel, alleys, cargo, and waterfront stair. |
| 10 | `10_walled_hill_monastery.png` | Walled Hill Monastery | Cloister, chapel, terraces, gardens, bell tower, and hill approach. |
| 11 | `11_old_growth_forest_road.png` | Old-Growth Forest Road | Winding road, creek crossing, camp, massive trunks, shrine, and cover. |
| 12 | `12_crossroads_battlefield.png` | Crossroads Battlefield | Long roads, stone fences, drain ditch, oak copse, wagon, and hillock. |
| 13 | `13_grand_cathedral_nave.png` | Grand Cathedral Nave | Nave, aisles, pew lines, raised sanctuary, transept, and stairs. |
| 14 | `14_sandstone_arena.png` | Sandstone Arena | Central sand, terraces, gates, holding-cell stairs, and raised platforms. |
| 15 | `15_forgotten_battlefield.png` | Forgotten Battlefield | Trenches, earthworks, ruined siege engine, craters, and open ground. |
| 16 | `16_deep_mine_complex.png` | Deep Mine Complex | Rail junction, loading floor, lift, extraction pits, galleries, and sump. |
| 17 | `17_fishing_village.png` | Fishing Village | Docks, sheds, boats, net racks, tidal creek, market, and shore lanes. |
| 18 | `18_volcanic_ridge.png` | Volcanic Ridge | Basalt plateaus, narrow lava cracks, stone bridges, shrine, and steam vents. |
| 19 | `19_feywild_glade.png` | Feywild Glade | Giant trees, flowered meadow, pond, root bridge, arches, and stream. |
| 20 | `20_harvest_farmstead.png` | Harvest Farmstead | Barn, house, fields, silos, orchard, fences, ditch, and hay cover. |
| 21 | `21_troll_bridge_ravine.png` | Troll Bridge Ravine | Stone bridge, cliff paths, tollhouse, river below, cave, and rope bridge. |
| 22 | `22_valley_river_crossing.png` | Valley River Crossing | Braided river, pebble bars, ford, watchtower, willows, and bridge. |
| 23 | `23_hillfort_camp.png` | Hillfort Camp | Palisade, gates, longhouses, watch platforms, ditch, and outer slopes. |
| 24 | `24_dwarven_market_hall.png` | Dwarven Market Hall | Wide hall, arcades, stalls, fountain, tunnels, steps, carts, and galleries. |
| 25 | `25_clockwork_city_plaza.png` | Clockwork City Plaza | Gear fountain, tram tracks, arcades, raised platform, and tunnel access. |
| 26 | `26_obsidian_temple_basin.png` | Obsidian Temple Basin | Terraced basalt, central pool, obelisks, raised sanctum, and lava channels. |
| 27 | `27_giant_mushroom_maze.png` | Giant Mushroom Maze | Fungal corridors, pools, plank paths, root tunnels, and central clearing. |
| 28 | `28_crystal_meteor_crater.png` | Crystal Meteor Crater | Crystal formations, crater terraces, ridge routes, scaffolds, and pools. |
| 29 | `29_floating_sky_ruins.png` | Floating Sky Ruins | Central platform, satellite ruins, bridges, void, chains, and wind paths. |
| 30 | `30_high_mountain_pass.png` | High Mountain Pass | Switchback trails, snow bridge, shrine, cliff edges, and avalanche cover. |

## VTT import setup

Use every PNG at its native resolution. Configure a square scene to **30 grid squares wide by 30 grid squares high**, then set the map scale to **5 feet per square**. Each map will cover a 150-foot square. For dynamic sequences, import each stage into a separate scene using the same grid settings, or replace the background image while preserving the aligned grid.

> The filenames contain the stage information. The artwork itself remains clean and gridless so that it can be used beneath any compatible virtual tabletop interface.

