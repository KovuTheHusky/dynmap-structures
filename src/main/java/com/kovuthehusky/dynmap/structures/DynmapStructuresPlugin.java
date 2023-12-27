package com.kovuthehusky.dynmap.structures;

import java.io.*;
import java.util.*;

import org.bstats.bukkit.Metrics;
import org.bukkit.*;
import org.bukkit.block.Biome;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.world.ChunkLoadEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.dynmap.DynmapCommonAPI;
import org.dynmap.markers.Marker;
import org.dynmap.markers.MarkerAPI;
import org.dynmap.markers.MarkerSet;

import static org.bukkit.StructureType.*;
import static org.bukkit.block.Biome.*;

@SuppressWarnings("unused")
public class DynmapStructuresPlugin extends JavaPlugin implements Listener {
    private static final StructureType[][] BIOMES = new StructureType[Biome.values().length][];
    private static final Map<StructureType, String> LABELS = new HashMap<StructureType, String>() {{
//        if (StructureType.getStructureTypes().containsKey("ancient_city")) {
//            put(ANCIENT_CITY, "Ancient City");
//        }
        if (StructureType.getStructureTypes().containsKey("bastion_remnant")) {
            put(BASTION_REMNANT, "Bastion Remnant");
        }
        put(BURIED_TREASURE, "Buried Treasure");
        put(DESERT_PYRAMID, "Desert Pyramid");
        put(END_CITY, "End City");
        put(NETHER_FORTRESS, "Nether Fortress");
        put(IGLOO, "Igloo");
        put(JUNGLE_PYRAMID, "Jungle Pyramid");
        put(WOODLAND_MANSION, "Woodland Mansion");
        put(MINESHAFT, "Mineshaft");
        if (StructureType.getStructureTypes().containsKey("nether_fossil")) {
            put(NETHER_FOSSIL, "Nether Fossil");
        }
        put(OCEAN_MONUMENT, "Ocean Monument");
        put(OCEAN_RUIN, "Ocean Ruins");
        if (StructureType.getStructureTypes().containsKey("pillager_outpost")) {
            put(PILLAGER_OUTPOST, "Pillager Outpost");
        }
        if (StructureType.getStructureTypes().containsKey("ruined_portal")) {
            put(RUINED_PORTAL, "Ruined Portal");
        }
        put(SHIPWRECK, "Shipwreck");
        put(STRONGHOLD, "Stronghold");
        put(SWAMP_HUT, "Swamp Hut");
        put(VILLAGE, "Village");
    }};
    private static final Map<StructureType, Boolean> STRUCTURES = new HashMap<>();

    private MarkerAPI api;
    private MarkerSet set;
    private boolean noLabels;
    private boolean includeCoordinates;

    @Override
    public void onEnable() {
        // Set up the metrics
        new Metrics(this, 605);
        // Set up the configuration
        this.saveDefaultConfig();
        FileConfiguration configuration = this.getConfig();
        configuration.options().copyDefaults(true);
        this.saveConfig();
        // Fill in biome data structure
        BIOMES[OCEAN.ordinal()] = new StructureType[]{BURIED_TREASURE, MINESHAFT, OCEAN_RUIN, SHIPWRECK, STRONGHOLD};
        BIOMES[PLAINS.ordinal()] = new StructureType[]{MINESHAFT, STRONGHOLD, VILLAGE};
        BIOMES[DESERT.ordinal()] = new StructureType[]{DESERT_PYRAMID, MINESHAFT, STRONGHOLD, VILLAGE};
        try {
            BIOMES[Biome.valueOf("MOUNTAINS").ordinal()] = new StructureType[]{MINESHAFT, STRONGHOLD};
        } catch (IllegalArgumentException e) {
        }
        try {
            Biome.valueOf("WINDSWEPT_HILLS");
            BIOMES[WINDSWEPT_HILLS.ordinal()] = new StructureType[]{MINESHAFT, STRONGHOLD};
        } catch (IllegalArgumentException e) {
            getLogger().warning("WINDSWEPT_HILLS not supported.");
        }
        BIOMES[FOREST.ordinal()] = new StructureType[]{MINESHAFT, STRONGHOLD};
        BIOMES[TAIGA.ordinal()] = new StructureType[]{MINESHAFT, STRONGHOLD, VILLAGE};
        BIOMES[SWAMP.ordinal()] = new StructureType[]{MINESHAFT, STRONGHOLD, SWAMP_HUT};
        BIOMES[RIVER.ordinal()] = new StructureType[]{MINESHAFT, STRONGHOLD};
        try {
            BIOMES[Biome.valueOf("NETHER").ordinal()] = new StructureType[]{NETHER_FORTRESS};
        } catch (IllegalArgumentException e) {
        }
        try {
            Biome.valueOf("NETHER_WASTES");
            BIOMES[NETHER_WASTES.ordinal()] = new StructureType[]{NETHER_FORTRESS};
        } catch (IllegalArgumentException e) {
            getLogger().warning("NETHER_WASTES not supported.");
        }
        BIOMES[THE_END.ordinal()] = new StructureType[]{END_CITY};
        BIOMES[FROZEN_OCEAN.ordinal()] = new StructureType[]{BURIED_TREASURE, MINESHAFT, OCEAN_RUIN, SHIPWRECK, STRONGHOLD};
        BIOMES[FROZEN_RIVER.ordinal()] = new StructureType[]{MINESHAFT, STRONGHOLD};
        try {
            BIOMES[Biome.valueOf("SNOWY_TUNDRA").ordinal()] = new StructureType[]{IGLOO, MINESHAFT, STRONGHOLD, VILLAGE};
        } catch (IllegalArgumentException e) {
        }
        try {
            Biome.valueOf("SNOWY_PLAINS");
            BIOMES[SNOWY_PLAINS.ordinal()] = new StructureType[]{IGLOO, MINESHAFT, STRONGHOLD, VILLAGE};
        } catch (IllegalArgumentException e) {
            getLogger().warning("SNOWY_PLAINS not supported.");
        }
        try {
            BIOMES[Biome.valueOf("SNOWY_MOUNTAINS").ordinal()] = new StructureType[]{MINESHAFT, STRONGHOLD, VILLAGE};
        } catch (IllegalArgumentException e) {
        }
        BIOMES[MUSHROOM_FIELDS.ordinal()] = new StructureType[]{MINESHAFT, STRONGHOLD};
        try {
            BIOMES[Biome.valueOf("MUSHROOM_FIELD_SHORE").ordinal()] = new StructureType[]{MINESHAFT, STRONGHOLD};
        } catch (IllegalArgumentException e) {
        }
        BIOMES[BEACH.ordinal()] = new StructureType[]{BURIED_TREASURE, MINESHAFT, OCEAN_RUIN, SHIPWRECK, STRONGHOLD};
        try {
            BIOMES[Biome.valueOf("DESERT_HILLS").ordinal()] = new StructureType[]{DESERT_PYRAMID, MINESHAFT, STRONGHOLD, VILLAGE};
        } catch (IllegalArgumentException e) {
        }
        try {
            BIOMES[Biome.valueOf("WOODED_HILLS").ordinal()] = new StructureType[]{MINESHAFT, STRONGHOLD};
        } catch (IllegalArgumentException e) {
        }
        try {
            BIOMES[Biome.valueOf("TAIGA_HILLS").ordinal()] = new StructureType[]{MINESHAFT, STRONGHOLD, VILLAGE};
        } catch (IllegalArgumentException e) {
        }
        try {
            BIOMES[Biome.valueOf("MOUNTAIN_EDGE").ordinal()] = new StructureType[]{MINESHAFT, STRONGHOLD};
        } catch (IllegalArgumentException e) {
        }
        BIOMES[JUNGLE.ordinal()] = new StructureType[]{JUNGLE_PYRAMID, MINESHAFT, STRONGHOLD};
        try {
            BIOMES[Biome.valueOf("JUNGLE_HILLS").ordinal()] = new StructureType[]{JUNGLE_PYRAMID, MINESHAFT, STRONGHOLD};
        } catch (IllegalArgumentException e) {
        }
        try {
            BIOMES[Biome.valueOf("JUNGLE_EDGE").ordinal()] = new StructureType[]{JUNGLE_PYRAMID, MINESHAFT, STRONGHOLD};
        } catch (IllegalArgumentException e) {
        }
        try {
            Biome.valueOf("SPARSE_JUNGLE");
            BIOMES[SPARSE_JUNGLE.ordinal()] = new StructureType[]{JUNGLE_PYRAMID, MINESHAFT, STRONGHOLD};
        } catch (IllegalArgumentException e) {
            getLogger().warning("SPARSE_JUNGLE not supported.");
        }
        BIOMES[DEEP_OCEAN.ordinal()] = new StructureType[]{BURIED_TREASURE, MINESHAFT, OCEAN_MONUMENT, OCEAN_RUIN, SHIPWRECK, STRONGHOLD};
        try {
            BIOMES[Biome.valueOf("STONE_SHORE").ordinal()] = new StructureType[]{BURIED_TREASURE, MINESHAFT, OCEAN_RUIN, SHIPWRECK, STRONGHOLD};
        } catch (IllegalArgumentException e) {
        }
        try {
            Biome.valueOf("STONY_SHORE");
            BIOMES[STONY_SHORE.ordinal()] = new StructureType[]{BURIED_TREASURE, MINESHAFT, OCEAN_RUIN, SHIPWRECK, STRONGHOLD};
        } catch (IllegalArgumentException e) {
            getLogger().warning("STONY_SHORE not supported.");
        }
        BIOMES[SNOWY_BEACH.ordinal()] = new StructureType[]{BURIED_TREASURE, MINESHAFT, OCEAN_RUIN, SHIPWRECK, STRONGHOLD};
        BIOMES[BIRCH_FOREST.ordinal()] = new StructureType[]{MINESHAFT, STRONGHOLD};
        try {
            BIOMES[Biome.valueOf("BIRCH_FOREST_HILLS").ordinal()] = new StructureType[]{MINESHAFT, STRONGHOLD};
        } catch (IllegalArgumentException e) {
        }
        BIOMES[DARK_FOREST.ordinal()] = new StructureType[]{MINESHAFT, STRONGHOLD, WOODLAND_MANSION};
        BIOMES[SNOWY_TAIGA.ordinal()] = new StructureType[]{IGLOO, MINESHAFT, STRONGHOLD, VILLAGE};
        try {
            BIOMES[Biome.valueOf("SNOWY_TAIGA_HILLS").ordinal()] = new StructureType[]{MINESHAFT, STRONGHOLD, VILLAGE};
        } catch (IllegalArgumentException e) {
        }
        try {
            BIOMES[Biome.valueOf("GIANT_TREE_TAIGA").ordinal()] = new StructureType[]{MINESHAFT, STRONGHOLD};
        } catch (IllegalArgumentException e) {
        }
        try {
            Biome.valueOf("OLD_GROWTH_PINE_TAIGA");
            BIOMES[OLD_GROWTH_PINE_TAIGA.ordinal()] = new StructureType[]{MINESHAFT, STRONGHOLD};
        } catch (IllegalArgumentException e) {
            getLogger().warning("OLD_GROWTH_PINE_TAIGA not supported.");
        }
        try {
            BIOMES[Biome.valueOf("GIANT_TREE_TAIGA_HILLS").ordinal()] = new StructureType[]{MINESHAFT, STRONGHOLD};
        } catch (IllegalArgumentException e) {
        }
        try {
            BIOMES[Biome.valueOf("WOODED_MOUNTAINS").ordinal()] = new StructureType[]{MINESHAFT, STRONGHOLD};
        } catch (IllegalArgumentException e) {
        }
        try {
            Biome.valueOf("WINDSWEPT_FOREST");
            BIOMES[WINDSWEPT_FOREST.ordinal()] = new StructureType[]{MINESHAFT, STRONGHOLD};
        } catch (IllegalArgumentException e) {
            getLogger().warning("WINDSWEPT_FOREST not supported.");
        }
        BIOMES[SAVANNA.ordinal()] = new StructureType[]{MINESHAFT, STRONGHOLD, VILLAGE};
        BIOMES[SAVANNA_PLATEAU.ordinal()] = new StructureType[]{MINESHAFT, STRONGHOLD, VILLAGE};
        BIOMES[BADLANDS.ordinal()] = new StructureType[]{MINESHAFT, STRONGHOLD};
        try {
            BIOMES[Biome.valueOf("WOODED_BADLANDS_PLATEAU").ordinal()] = new StructureType[]{MINESHAFT, STRONGHOLD};
        } catch (IllegalArgumentException e) {
        }
        try {
            Biome.valueOf("WOODED_BADLANDS");
            BIOMES[WOODED_BADLANDS.ordinal()] = new StructureType[]{MINESHAFT, STRONGHOLD};
        } catch (IllegalArgumentException e) {
            getLogger().warning("WOODED_BADLANDS not supported.");
        }
        try {
            BIOMES[Biome.valueOf("BADLANDS_PLATEAU").ordinal()] = new StructureType[]{MINESHAFT, STRONGHOLD};
        } catch (IllegalArgumentException e) {
        }
        BIOMES[SMALL_END_ISLANDS.ordinal()] = new StructureType[]{END_CITY};
        BIOMES[END_MIDLANDS.ordinal()] = new StructureType[]{END_CITY};
        BIOMES[END_HIGHLANDS.ordinal()] = new StructureType[]{END_CITY};
        BIOMES[END_BARRENS.ordinal()] = new StructureType[]{END_CITY};
        BIOMES[WARM_OCEAN.ordinal()] = new StructureType[]{BURIED_TREASURE, MINESHAFT, OCEAN_RUIN, SHIPWRECK, STRONGHOLD};
        BIOMES[LUKEWARM_OCEAN.ordinal()] = new StructureType[]{BURIED_TREASURE, MINESHAFT, OCEAN_RUIN, SHIPWRECK, STRONGHOLD};
        BIOMES[COLD_OCEAN.ordinal()] = new StructureType[]{BURIED_TREASURE, MINESHAFT, OCEAN_RUIN, SHIPWRECK, STRONGHOLD};
        try {
            BIOMES[Biome.valueOf("DEEP_WARM_OCEAN").ordinal()] = new StructureType[]{BURIED_TREASURE, MINESHAFT, OCEAN_MONUMENT, OCEAN_RUIN, SHIPWRECK, STRONGHOLD};
        } catch (IllegalArgumentException e) {
        }
        BIOMES[DEEP_LUKEWARM_OCEAN.ordinal()] = new StructureType[]{BURIED_TREASURE, MINESHAFT, OCEAN_MONUMENT, OCEAN_RUIN, SHIPWRECK, STRONGHOLD};
        BIOMES[DEEP_COLD_OCEAN.ordinal()] = new StructureType[]{BURIED_TREASURE, MINESHAFT, OCEAN_MONUMENT, OCEAN_RUIN, SHIPWRECK, STRONGHOLD};
        BIOMES[DEEP_FROZEN_OCEAN.ordinal()] = new StructureType[]{BURIED_TREASURE, MINESHAFT, OCEAN_MONUMENT, OCEAN_RUIN, SHIPWRECK, STRONGHOLD};
        BIOMES[THE_VOID.ordinal()] = new StructureType[]{};
        BIOMES[SUNFLOWER_PLAINS.ordinal()] = new StructureType[]{MINESHAFT, STRONGHOLD, VILLAGE};
        try {
            BIOMES[Biome.valueOf("DESERT_LAKES").ordinal()] = new StructureType[]{DESERT_PYRAMID, MINESHAFT, STRONGHOLD, VILLAGE};
        } catch (IllegalArgumentException e) {
        }
        try {
            BIOMES[Biome.valueOf("GRAVELLY_MOUNTAINS").ordinal()] = new StructureType[]{MINESHAFT, STRONGHOLD};
        } catch (IllegalArgumentException e) {
        }
        try {
            Biome.valueOf("WINDSWEPT_GRAVELLY_HILLS");
            BIOMES[WINDSWEPT_GRAVELLY_HILLS.ordinal()] = new StructureType[]{MINESHAFT, STRONGHOLD};
        } catch (IllegalArgumentException e) {
            getLogger().warning("WINDSWEPT_GRAVELLY_HILLS not supported.");
        }
        BIOMES[FLOWER_FOREST.ordinal()] = new StructureType[]{MINESHAFT, STRONGHOLD};
        try {
            BIOMES[Biome.valueOf("TAIGA_MOUNTAINS").ordinal()] = new StructureType[]{MINESHAFT, STRONGHOLD, VILLAGE};
        } catch (IllegalArgumentException e) {
        }
        try {
            BIOMES[Biome.valueOf("SWAMP_HILLS").ordinal()] = new StructureType[]{MINESHAFT, STRONGHOLD, SWAMP_HUT};
        } catch (IllegalArgumentException e) {
        }
        BIOMES[ICE_SPIKES.ordinal()] = new StructureType[]{MINESHAFT, STRONGHOLD, VILLAGE};
        try {
            BIOMES[Biome.valueOf("MODIFIED_JUNGLE").ordinal()] = new StructureType[]{JUNGLE_PYRAMID, MINESHAFT, STRONGHOLD};
        } catch (IllegalArgumentException e) {
        }
        try {
            BIOMES[Biome.valueOf("MODIFIED_JUNGLE_EDGE").ordinal()] = new StructureType[]{JUNGLE_PYRAMID, MINESHAFT, STRONGHOLD};
        } catch (IllegalArgumentException e) {
        }
        try {
            BIOMES[Biome.valueOf("TALL_BIRCH_FOREST").ordinal()] = new StructureType[]{MINESHAFT, STRONGHOLD};
        } catch (IllegalArgumentException e) {
        }
        try {
            Biome.valueOf("OLD_GROWTH_BIRCH_FOREST");
            BIOMES[OLD_GROWTH_BIRCH_FOREST.ordinal()] = new StructureType[]{MINESHAFT, STRONGHOLD};
        } catch (IllegalArgumentException e) {
            getLogger().warning("OLD_GROWTH_BIRCH_FOREST not supported.");
        }
        try {
            BIOMES[Biome.valueOf("TALL_BIRCH_HILLS").ordinal()] = new StructureType[]{MINESHAFT, STRONGHOLD};
        } catch (IllegalArgumentException e) {
        }
        try {
            BIOMES[Biome.valueOf("DARK_FOREST_HILLS").ordinal()] = new StructureType[]{MINESHAFT, STRONGHOLD, WOODLAND_MANSION};
        } catch (IllegalArgumentException e) {
        }
        try {
            BIOMES[Biome.valueOf("SNOWY_TAIGA_MOUNTAINS").ordinal()] = new StructureType[]{MINESHAFT, STRONGHOLD, VILLAGE};
        } catch (IllegalArgumentException e) {
        }
        try {
            BIOMES[Biome.valueOf("GIANT_SPRUCE_TAIGA").ordinal()] = new StructureType[]{MINESHAFT, STRONGHOLD};
        } catch (IllegalArgumentException e) {
        }
        try {
            Biome.valueOf("OLD_GROWTH_SPRUCE_TAIGA");
            BIOMES[OLD_GROWTH_SPRUCE_TAIGA.ordinal()] = new StructureType[]{MINESHAFT, STRONGHOLD};
        } catch (IllegalArgumentException e) {
            getLogger().warning("OLD_GROWTH_SPRUCE_TAIGA not supported.");
        }
        try {
            BIOMES[Biome.valueOf("GIANT_SPRUCE_TAIGA_HILLS").ordinal()] = new StructureType[]{MINESHAFT, STRONGHOLD};
        } catch (IllegalArgumentException e) {
        }
        try {
            BIOMES[Biome.valueOf("MODIFIED_GRAVELLY_MOUNTAINS").ordinal()] = new StructureType[]{MINESHAFT, STRONGHOLD};
        } catch (IllegalArgumentException e) {
        }
        try {
            BIOMES[Biome.valueOf("SHATTERED_SAVANNA").ordinal()] = new StructureType[]{MINESHAFT, STRONGHOLD, VILLAGE};
        } catch (IllegalArgumentException e) {
        }
        try {
            Biome.valueOf("WINDSWEPT_SAVANNA");
            BIOMES[WINDSWEPT_SAVANNA.ordinal()] = new StructureType[]{MINESHAFT, STRONGHOLD, VILLAGE};
        } catch (IllegalArgumentException e) {
            getLogger().warning("WINDSWEPT_SAVANNA not supported.");
        }
        try {
            BIOMES[Biome.valueOf("SHATTERED_SAVANNA_PLATEAU").ordinal()] = new StructureType[]{MINESHAFT, STRONGHOLD, VILLAGE};
        } catch (IllegalArgumentException e) {
        }
        BIOMES[ERODED_BADLANDS.ordinal()] = new StructureType[]{MINESHAFT, STRONGHOLD};
        try {
            BIOMES[Biome.valueOf("MODIFIED_WOODED_BADLANDS_PLATEAU").ordinal()] = new StructureType[]{MINESHAFT, STRONGHOLD};
        } catch (IllegalArgumentException e) {
        }
        try {
            BIOMES[Biome.valueOf("MODIFIED_BADLANDS_PLATEAU").ordinal()] = new StructureType[]{MINESHAFT, STRONGHOLD};
        } catch (IllegalArgumentException e) {
        }
        try {
            Biome.valueOf("BAMBOO_JUNGLE");
            BIOMES[BAMBOO_JUNGLE.ordinal()] = new StructureType[]{JUNGLE_PYRAMID, MINESHAFT, STRONGHOLD};
        } catch (IllegalArgumentException e) {
            getLogger().warning("BAMBOO_JUNGLE not supported.");
        }
        try {
            BIOMES[Biome.valueOf("BAMBOO_JUNGLE_HILLS").ordinal()] = new StructureType[]{JUNGLE_PYRAMID, MINESHAFT, STRONGHOLD};
        } catch (IllegalArgumentException e) {
        }
        try {
            Biome.valueOf("SOUL_SAND_VALLEY");
            BIOMES[SOUL_SAND_VALLEY.ordinal()] = new StructureType[]{NETHER_FORTRESS};
        } catch (IllegalArgumentException e) {
            getLogger().warning("SOUL_SAND_VALLEY not supported.");
        }
        try {
            Biome.valueOf("CRIMSON_FOREST");
            BIOMES[CRIMSON_FOREST.ordinal()] = new StructureType[]{NETHER_FORTRESS};
        } catch (IllegalArgumentException e) {
            getLogger().warning("CRIMSON_FOREST not supported.");
        }
        try {
            Biome.valueOf("WARPED_FOREST");
            BIOMES[WARPED_FOREST.ordinal()] = new StructureType[]{NETHER_FORTRESS};
        } catch (IllegalArgumentException e) {
            getLogger().warning("WARPED_FOREST not supported.");
        }
        try {
            Biome.valueOf("BASALT_DELTAS");
            BIOMES[BASALT_DELTAS.ordinal()] = new StructureType[]{NETHER_FORTRESS};
        } catch (IllegalArgumentException e) {
            getLogger().warning("BASALT_DELTAS not supported.");
        }
        try {
            Biome.valueOf("DRIPSTONE_CAVES");
            BIOMES[DRIPSTONE_CAVES.ordinal()] = new StructureType[]{MINESHAFT, STRONGHOLD, VILLAGE};
        } catch (IllegalArgumentException e) {
            getLogger().warning("DRIPSTONE_CAVES not supported.");
        }
        try {
            Biome.valueOf("LUSH_CAVES");
            BIOMES[LUSH_CAVES.ordinal()] = new StructureType[]{MINESHAFT, STRONGHOLD, VILLAGE};
        } catch (IllegalArgumentException e) {
            getLogger().warning("LUSH_CAVES not supported.");
        }
        try {
            Biome.valueOf("MEADOW");
            BIOMES[MEADOW.ordinal()] = new StructureType[]{MINESHAFT, STRONGHOLD, VILLAGE};
        } catch (IllegalArgumentException e) {
            getLogger().warning("MEADOW not supported.");
        }
        try {
            Biome.valueOf("GROVE");
            BIOMES[GROVE.ordinal()] = new StructureType[]{MINESHAFT, STRONGHOLD};
        } catch (IllegalArgumentException e) {
            getLogger().warning("GROVE not supported.");
        }
        try {
            Biome.valueOf("SNOWY_SLOPES");
            BIOMES[SNOWY_SLOPES.ordinal()] = new StructureType[]{IGLOO, MINESHAFT, STRONGHOLD};
        } catch (IllegalArgumentException e) {
            getLogger().warning("SNOWY_SLOPES not supported.");
        }
        try {
            Biome.valueOf("FROZEN_PEAKS");
            BIOMES[FROZEN_PEAKS.ordinal()] = new StructureType[]{MINESHAFT, STRONGHOLD};
        } catch (IllegalArgumentException e) {
            getLogger().warning("FROZEN_PEAKS not supported.");
        }
        try {
            Biome.valueOf("JAGGED_PEAKS");
            BIOMES[JAGGED_PEAKS.ordinal()] = new StructureType[]{MINESHAFT, STRONGHOLD};
        } catch (IllegalArgumentException e) {
            getLogger().warning("JAGGED_PEAKS not supported.");
        }
        try {
            Biome.valueOf("STONY_PEAKS");
            BIOMES[STONY_PEAKS.ordinal()] = new StructureType[]{MINESHAFT, STRONGHOLD};
        } catch (IllegalArgumentException e) {
            getLogger().warning("STONY_PEAKS not supported.");
        }
        try {
            Biome.valueOf("DEEP_DARK");
            BIOMES[DEEP_DARK.ordinal()] = new StructureType[]{MINESHAFT, STRONGHOLD};
        } catch (IllegalArgumentException e) {
            getLogger().warning("DEEP_DARK not supported.");
        }
        try {
            Biome.valueOf("MANGROVE_SWAMP");
            BIOMES[MANGROVE_SWAMP.ordinal()] = new StructureType[]{MINESHAFT, STRONGHOLD, SWAMP_HUT};
        } catch (IllegalArgumentException e) {
            getLogger().warning("MANGROVE_SWAMP not supported.");
        }
        try {
            Biome.valueOf("CUSTOM");
            BIOMES[CUSTOM.ordinal()] = new StructureType[]{BASTION_REMNANT, BURIED_TREASURE, DESERT_PYRAMID, END_CITY, NETHER_FORTRESS, IGLOO, JUNGLE_PYRAMID, WOODLAND_MANSION, MINESHAFT, NETHER_FOSSIL, OCEAN_MONUMENT, OCEAN_RUIN, PILLAGER_OUTPOST, RUINED_PORTAL, SHIPWRECK, STRONGHOLD, SWAMP_HUT, VILLAGE};
        } catch (IllegalArgumentException e) {
            getLogger().warning("CUSTOM not supported.");
        }
        // Add pillager outposts if supported
        if (StructureType.getStructureTypes().containsKey("pillager_outpost")) {
            ArrayList<Biome> biomes = new ArrayList<Biome>() {{
                add(PLAINS);
                add(DESERT);
                add(TAIGA);
                try {
                    add(Biome.valueOf(("SNOWY_TUNDRA")));
                } catch (IllegalArgumentException e) {
                }
                try {
                    Biome.valueOf("SNOWY_PLAINS");
                    add(SNOWY_PLAINS);
                } catch (IllegalArgumentException e) {
                }
                try {
                    add(Biome.valueOf(("SNOWY_MOUNTAINS")));
                } catch (IllegalArgumentException e) {
                }
                try {
                    add(Biome.valueOf(("DESERT_HILLS")));
                } catch (IllegalArgumentException e) {
                }
                try {
                    add(Biome.valueOf(("TAIGA_HILLS")));
                } catch (IllegalArgumentException e) {
                }
                add(SNOWY_TAIGA);
                try {
                    add(Biome.valueOf(("SNOWY_TAIGA_HILLS")));
                } catch (IllegalArgumentException e) {
                }
                add(SAVANNA);
                add(SAVANNA_PLATEAU);
                add(SUNFLOWER_PLAINS);
                try {
                    add(Biome.valueOf(("DESERT_LAKES")));
                } catch (IllegalArgumentException e) {
                }
                try {
                    add(Biome.valueOf(("TAIGA_MOUNTAINS")));
                } catch (IllegalArgumentException e) {
                }
                add(ICE_SPIKES);
                try {
                    add(Biome.valueOf(("SNOWY_TAIGA_MOUNTAINS")));
                } catch (IllegalArgumentException e) {
                }
                try {
                    add(Biome.valueOf(("SHATTERED_SAVANNA")));
                } catch (IllegalArgumentException e) {
                }
                try {
                    Biome.valueOf("WINDSWEPT_SAVANNA");
                    add(WINDSWEPT_SAVANNA);
                } catch (IllegalArgumentException e) {
                }
                try {
                    add(Biome.valueOf(("SHATTERED_SAVANNA_PLATEAU")));
                } catch (IllegalArgumentException e) {
                }
                try {
                    Biome.valueOf("MEADOW");
                    add(MEADOW);
                } catch (IllegalArgumentException e) {
                }
                try {
                    Biome.valueOf("GROVE");
                    add(GROVE);
                } catch (IllegalArgumentException e) {
                }
                try {
                    Biome.valueOf("SNOWY_SLOPES");
                    add(SNOWY_SLOPES);
                } catch (IllegalArgumentException e) {
                }
                try {
                    Biome.valueOf("FROZEN_PEAKS");
                    add(FROZEN_PEAKS);
                } catch (IllegalArgumentException e) {
                }
                try {
                    Biome.valueOf("JAGGED_PEAKS");
                    add(JAGGED_PEAKS);
                } catch (IllegalArgumentException e) {
                }
                try {
                    Biome.valueOf("STONY_PEAKS");
                    add(STONY_PEAKS);
                } catch (IllegalArgumentException e) {
                }
            }};
            for (Biome biome : biomes) {
                StructureType[] temp = new StructureType[BIOMES[biome.ordinal()].length + 1];
                System.arraycopy(BIOMES[biome.ordinal()], 0, temp, 0, BIOMES[biome.ordinal()].length);
                temp[temp.length - 1] = PILLAGER_OUTPOST;
                BIOMES[biome.ordinal()] = temp;
            }
        }
        // Add bastion remnant if supported
        registerStructure("bastion_remnant", BASTION_REMNANT, new Biome[]{NETHER_WASTES, SOUL_SAND_VALLEY, CRIMSON_FOREST, WARPED_FOREST});

        // Add nether fossils if supported
        registerStructure("nether_fossil", NETHER_FOSSIL, new Biome[]{SOUL_SAND_VALLEY});

        // Add ruined portals if supported
        registerStructure("ruined_portal", RUINED_PORTAL, Biome.values());

        // Add ancient cities if supported
//        if (StructureType.getStructureTypes().containsKey("ancient_city")) {
//            ArrayList<Biome> biomes = new ArrayList<Biome>() {{
//                try {
//                    add(Biome.valueOf(("DEEP_DARK")));
//                } catch (IllegalArgumentException e) {
//                }
//            }};
//            for (Biome biome : biomes) {
//                StructureType[] temp = new StructureType[BIOMES[biome.ordinal()].length + 1];
//                System.arraycopy(BIOMES[biome.ordinal()], 0, temp, 0, BIOMES[biome.ordinal()].length);
//                temp[temp.length - 1] = ANCIENT_CITY;
//                BIOMES[biome.ordinal()] = temp;
//            }
//        }
        // Fill in id and label data structures
        for (StructureType type : StructureType.getStructureTypes().values()) {
            String id = type.getName().toLowerCase(Locale.ROOT).replace("_", "");
            STRUCTURES.put(type, configuration.getBoolean("structures." + id));
            String label = configuration.getString("labels." + id);
            if (label != null) {
                LABELS.put(type, label);
            }
        }
        // Register for events
        this.getServer().getPluginManager().registerEvents(this, this);
        // Check if Dynmap is even enabled
        if (Bukkit.getPluginManager().isPluginEnabled("dynmap")) {
            // Set up our Dynmap api
            try {
                DynmapCommonAPI plugin = (DynmapCommonAPI) Bukkit.getPluginManager().getPlugin("dynmap");
                if (plugin != null) {
                    api = plugin.getMarkerAPI();
                }
            } catch (NullPointerException e) {
                return;
            }
            // Set up our Dynmap layer
            String layer = configuration.getString("layer.name");
            if (layer == null) {
                layer = "Structures";
            }
            set = api.getMarkerSet(layer.toLowerCase(Locale.ROOT));
            if (set == null) {
                set = api.createMarkerSet(layer.toLowerCase(Locale.ROOT), layer, null, true);
            }
            set.setHideByDefault(configuration.getBoolean("layer.hidebydefault"));
            set.setLayerPriority(configuration.getInt("layer.layerprio"));
            noLabels = configuration.getBoolean("layer.noLabels");
            int minZoom = configuration.getInt("layer.minzoom");
            if (minZoom > 0) {
                set.setMinZoom(minZoom);
            }
            includeCoordinates = configuration.getBoolean("layer.inc-coord");
            // Create the marker icons
            for (StructureType type : StructureType.getStructureTypes().values()) {
                String str = type.getName().toLowerCase(Locale.ROOT).replaceAll("_", "");
                InputStream in = this.getClass().getResourceAsStream("/" + str + ".png");
                if (in != null) {
                    if (api.getMarkerIcon("structures." + str) == null) {
                        api.createMarkerIcon("structures." + str, str, in);
                    } else {
                        api.getMarkerIcon("structures." + str).setMarkerIconImage(in);
                    }
                }
            }
            // Remove any markers for disabled types
            List<String> disabled = new ArrayList<>();
            for (StructureType type : StructureType.getStructureTypes().values()) {
                String id = type.getName().toLowerCase(Locale.ROOT).replace("_", "");
                if (!configuration.getBoolean("structures." + id)) {
                    disabled.add(id);
                }
            }
            for (Marker marker : set.getMarkers()) {
                for (String id : disabled) {
                    if (marker.getMarkerID().startsWith(id)) {
                        marker.deleteMarker();
                        break;
                    }
                }
            }
        }
    }

    private void registerStructure(String structureKey, StructureType structureType, Biome[] biomes) {
        if (StructureType.getStructureTypes().containsKey(structureKey)) {
            for (Biome biome : biomes) {
                StructureType[] oldStructures = BIOMES[biome.ordinal()];
                if (oldStructures == null) {
                    BIOMES[biome.ordinal()] = new StructureType[]{structureType};
                } else {
                    StructureType[] newStructures = new StructureType[oldStructures.length + 1];
                    System.arraycopy(oldStructures, 0, newStructures, 0, oldStructures.length);
                    newStructures[newStructures.length - 1] = structureType;
                    BIOMES[biome.ordinal()] = newStructures;
                }
            }

        }
    }

    @EventHandler
    public void onChunkLoad(ChunkLoadEvent event) {
        if (event.getWorld().canGenerateStructures()) {
            Bukkit.getScheduler().runTask(this, new DynmapStructuresRunnable(event.getChunk()));
        }
    }

    private class DynmapStructuresRunnable implements Runnable {
        private final Chunk chunk;

        private DynmapStructuresRunnable(Chunk chunk) {
            this.chunk = chunk;
        }

        @Override
        public void run() {
            Location location = new Location(chunk.getWorld(), chunk.getX() << 4, 64, chunk.getZ() << 4);
            World world = location.getWorld();
            if (world != null) {
                Biome biome;
                try {
                    Biome.class.getMethod("getBiome", int.class, int.class, int.class);
                    biome = world.getBiome(location.getBlockX(), location.getBlockY(), location.getBlockZ());
                } catch (NoSuchMethodException e) {
                    biome = world.getBiome(location.getBlockX(), location.getBlockZ());
                }
                if (biome != null) {
                    for (StructureType type : BIOMES[biome.ordinal()]) {
                        if (STRUCTURES.get(type)) {
                            Location structure;
                            try {
                                structure = location.getWorld().locateNearestStructure(location, type, 1, false);
                            } catch (ConcurrentModificationException e) {
                                getLogger().warning("Skipping locate at ([" + location.getWorld().getName() + "], " + location.getBlockX() + ", " + location.getBlockZ() + ") due to concurrent modification exception.");
                                return;
                            } catch (NullPointerException e) {
                                getLogger().warning("Skipping locate at ([" + location.getWorld().getName() + "], " + location.getBlockX() + ", " + location.getBlockZ() + ") due to null pointer exception.");
                                return;
                            }
                            if (structure != null) {
                                String id = type.getName().toLowerCase(Locale.ROOT).replace("_", "");
                                int x = structure.getBlockX();
                                int z = structure.getBlockZ();
                                String label = "";
                                if (!noLabels) {
                                    label = LABELS.get(type);
                                    if (includeCoordinates) {
                                        label = label + " [" + x + "," + z + "]";
                                    }
                                }
                                set.createMarker(id + "," + x + "," + z, label, world.getName(), x, 64, z, api.getMarkerIcon("structures." + id), true);
                            }
                        }
                    }
                }
            }
        }
    }
}
