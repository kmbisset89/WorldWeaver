package net.tactware.worldweaver.domain

/**
 * Starter battle maps shipped beside the app in `battle_maps` and `medium_battle_maps`.
 */
internal object BundledBattleMapCatalog {
    data class Entry(
        val id: String,
        val name: String,
        val fileName: String,
        val columns: Int = 20,
        val rows: Int = 20,
        val situations: List<Situation> = emptyList(),
    )

    data class Situation(
        val name: String,
        val fileName: String,
    )

    val entries: List<Entry> = listOf(
        Entry(
            id = "whispering-shrine",
            name = "Whispering Shrine",
            fileName = "01_whispering_shrine_A_calm.png",
            situations = listOf(
                Situation("Falling trees", "01_whispering_shrine_B_falling_trees.png"),
                Situation("Wildfire", "01_whispering_shrine_C_wildfire.png"),
            ),
        ),
        Entry(
            id = "riverside-ruins",
            name = "Riverside Ruins",
            fileName = "02_riverside_ruins_A_low_water.png",
            situations = listOf(
                Situation("Rising water", "02_riverside_ruins_B_rising_water.png"),
                Situation("Flooded", "02_riverside_ruins_C_flooded.png"),
            ),
        ),
        Entry(
            id = "ember-granary",
            name = "Ember Granary",
            fileName = "03_ember_granary_A_smoke.png",
            situations = listOf(
                Situation("Spreading fire", "03_ember_granary_B_spreading_fire.png"),
                Situation("Inferno", "03_ember_granary_C_inferno.png"),
            ),
        ),
        Entry(
            id = "thawing-ice-cavern",
            name = "Thawing Ice Cavern",
            fileName = "04_thawing_ice_cavern_A_frozen.png",
            situations = listOf(
                Situation("Thaw", "04_thawing_ice_cavern_B_thaw.png"),
                Situation("Flooded cavern", "04_thawing_ice_cavern_C_flooded_cavern.png"),
            ),
        ),
        Entry(
            id = "collapsing-mine",
            name = "Collapsing Mine",
            fileName = "05_collapsing_mine_A_intact.png",
            situations = listOf(
                Situation("Cave-in", "05_collapsing_mine_B_cave_in.png"),
                Situation("Blocked and flooded", "05_collapsing_mine_C_blocked_flooded.png"),
            ),
        ),
        standalone("greenwood-clearing", "Greenwood Clearing", "06_greenwood_clearing.png"),
        standalone("sunken-temple-court", "Sunken Temple Court", "07_sunken_temple_court.png"),
        standalone("jungle-ziggurat", "Jungle Ziggurat", "08_jungle_ziggurat.png"),
        standalone("overgrown-keep", "Overgrown Keep", "09_overgrown_keep.png"),
        standalone("fey-mushroom-ring", "Fey Mushroom Ring", "10_fey_mushroom_ring.png"),
        standalone("cliffside-pass", "Cliffside Pass", "11_cliffside_pass.png"),
        standalone("moonlit-graveyard", "Moonlit Graveyard", "12_moonlit_graveyard.png"),
        standalone("bone-crypt", "Bone Crypt", "13_bone_crypt.png"),
        standalone("crossroads-tavern", "Crossroads Tavern", "14_crossroads_tavern.png"),
        standalone("sewer-junction", "Sewer Junction", "15_sewer_junction.png"),
        standalone("prison-cellblock", "Prison Cellblock", "16_prison_cellblock.png"),
        standalone("alchemists-workshop", "Alchemist's Workshop", "17_alchemists_workshop.png"),
        standalone("grand-arcane-library", "Grand Arcane Library", "18_grand_arcane_library.png"),
        standalone("market-square", "Market Square", "19_market_square.png"),
        standalone("dockside-warehouse", "Dockside Warehouse", "20_dockside_warehouse.png"),
        standalone("storm-tossed-ship-deck", "Storm-Tossed Ship Deck", "21_storm_tossed_ship_deck.png"),
        standalone("smugglers-sea-cave", "Smuggler's Sea Cave", "22_smugglers_sea_cave.png"),
        standalone("desert-oasis", "Desert Oasis", "23_desert_oasis.png"),
        standalone("canyon-bridge", "Canyon Bridge", "24_canyon_bridge.png"),
        standalone("volcanic-forge", "Volcanic Forge", "25_volcanic_forge.png"),
        standalone("glacier-fissure", "Glacier Fissure", "26_glacier_fissure.png"),
        standalone("frozen-lake-camp", "Frozen Lake Camp", "27_frozen_lake_camp.png"),
        standalone("dwarven-hall", "Dwarven Hall", "28_dwarven_hall.png"),
        standalone("clockwork-foundry", "Clockwork Foundry", "29_clockwork_foundry.png"),
        standalone("astral-observatory", "Astral Observatory", "30_astral_observatory.png"),
        Entry(
            id = "stormwatch-keep",
            name = "Stormwatch Keep",
            fileName = "01_stormwatch_keep_A_holding.png",
            columns = 30,
            rows = 30,
            situations = listOf(
                Situation("Gate breached", "01_stormwatch_keep_B_gate_breached.png"),
                Situation("Inner bailey burning", "01_stormwatch_keep_C_inner_bailey_burning.png"),
            ),
        ),
        Entry(
            id = "flooded-harbor",
            name = "Flooded Harbor",
            fileName = "02_flooded_harbor_A_low_tide.png",
            columns = 30,
            rows = 30,
            situations = listOf(
                Situation("Storm surge", "02_flooded_harbor_B_storm_surge.png"),
                Situation("Flooded harbor", "02_flooded_harbor_C_flooded_harbor.png"),
            ),
        ),
        Entry(
            id = "sinking-jungle-city",
            name = "Sinking Jungle City",
            fileName = "03_sinking_jungle_city_A_dry_season.png",
            columns = 30,
            rows = 30,
            situations = listOf(
                Situation("Monsoon", "03_sinking_jungle_city_B_monsoon.png"),
                Situation("Sunken plaza", "03_sinking_jungle_city_C_sunken_plaza.png"),
            ),
        ),
        Entry(
            id = "great-river-bridge",
            name = "Great River Bridge",
            fileName = "04_great_river_bridge_A_intact.png",
            columns = 30,
            rows = 30,
            situations = listOf(
                Situation("Partial collapse", "04_great_river_bridge_B_partial_collapse.png"),
                Situation("Burning barricades", "04_great_river_bridge_C_burning_barricades.png"),
            ),
        ),
        Entry(
            id = "glacial-vale",
            name = "Glacial Vale",
            fileName = "05_glacial_vale_A_frozen_vale.png",
            columns = 30,
            rows = 30,
            situations = listOf(
                Situation("Avalanche", "05_glacial_vale_B_avalanche.png"),
                Situation("Spring thaw", "05_glacial_vale_C_spring_thaw.png"),
            ),
        ),
        medium("riverford-village", "Riverford Village", "06_riverford_village.png"),
        medium("castle-outer-ward", "Castle Outer Ward", "07_castle_outer_ward.png"),
        medium("desert-caravanserai", "Desert Caravanserai", "08_desert_caravanserai.png"),
        medium("port-town-square", "Port Town Square", "09_port_town_square.png"),
        medium("walled-hill-monastery", "Walled Hill Monastery", "10_walled_hill_monastery.png"),
        medium("old-growth-forest-road", "Old-Growth Forest Road", "11_old_growth_forest_road.png"),
        medium("crossroads-battlefield", "Crossroads Battlefield", "12_crossroads_battlefield.png"),
        medium("grand-cathedral-nave", "Grand Cathedral Nave", "13_grand_cathedral_nave.png"),
        medium("sandstone-arena", "Sandstone Arena", "14_sandstone_arena.png"),
        medium("forgotten-battlefield", "Forgotten Battlefield", "15_forgotten_battlefield.png"),
        medium("deep-mine-complex", "Deep Mine Complex", "16_deep_mine_complex.png"),
        medium("fishing-village", "Fishing Village", "17_fishing_village.png"),
        medium("volcanic-ridge", "Volcanic Ridge", "18_volcanic_ridge.png"),
        medium("feywild-glade", "Feywild Glade", "19_feywild_glade.png"),
        medium("harvest-farmstead", "Harvest Farmstead", "20_harvest_farmstead.png"),
        medium("troll-bridge-ravine", "Troll Bridge Ravine", "21_troll_bridge_ravine.png"),
        medium("valley-river-crossing", "Valley River Crossing", "22_valley_river_crossing.png"),
        medium("hillfort-camp", "Hillfort Camp", "23_hillfort_camp.png"),
        medium("dwarven-market-hall", "Dwarven Market Hall", "24_dwarven_market_hall.png"),
        medium("clockwork-city-plaza", "Clockwork City Plaza", "25_clockwork_city_plaza.png"),
        medium("obsidian-temple-basin", "Obsidian Temple Basin", "26_obsidian_temple_basin.png"),
        medium("giant-mushroom-maze", "Giant Mushroom Maze", "27_giant_mushroom_maze.png"),
        medium("crystal-meteor-crater", "Crystal Meteor Crater", "28_crystal_meteor_crater.png"),
        medium("floating-sky-ruins", "Floating Sky Ruins", "29_floating_sky_ruins.png"),
        medium("high-mountain-pass", "High Mountain Pass", "30_high_mountain_pass.png"),
    )

    fun entryById(id: String): Entry? {
        return entries.firstOrNull { it.id == id }
    }

    private fun standalone(
        id: String,
        name: String,
        fileName: String,
        columns: Int = 20,
        rows: Int = 20,
    ): Entry {
        return Entry(id = id, name = name, fileName = fileName, columns = columns, rows = rows)
    }

    private fun medium(id: String, name: String, fileName: String): Entry {
        return standalone(id = id, name = name, fileName = fileName, columns = 30, rows = 30)
    }
}
