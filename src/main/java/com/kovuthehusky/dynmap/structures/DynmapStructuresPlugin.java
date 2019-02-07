package com.kovuthehusky.dynmap.structures;

import java.io.*;
import java.util.*;

import org.bstats.bukkit.Metrics;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.StructureType;
import org.bukkit.block.Biome;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.world.ChunkLoadEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.dynmap.DynmapCommonAPI;
import org.dynmap.markers.MarkerAPI;
import org.dynmap.markers.MarkerSet;

import static org.bukkit.StructureType.*;
import static org.bukkit.block.Biome.*;

@SuppressWarnings("unused")
public class DynmapStructuresPlugin extends JavaPlugin implements Listener {
    private static final Map<Biome, Set<StructureType>> BIOMES = new HashMap<Biome, Set<StructureType>>() {{
        put(OCEAN, new HashSet<StructureType>() {{
            add(BURIED_TREASURE);
            add(MINESHAFT);
            add(OCEAN_RUIN);
            add(SHIPWRECK);
            add(STRONGHOLD);
        }});
        put(PLAINS, new HashSet<StructureType>() {{
            add(MINESHAFT);
            add(STRONGHOLD);
            add(VILLAGE);
        }});
        put(DESERT, new HashSet<StructureType>() {{
            add(DESERT_PYRAMID);
            add(MINESHAFT);
            add(STRONGHOLD);
            add(VILLAGE);
        }});
        put(MOUNTAINS, new HashSet<StructureType>() {{
            add(MINESHAFT);
            add(STRONGHOLD);
        }});
        put(FOREST, new HashSet<StructureType>() {{
            add(MINESHAFT);
            add(STRONGHOLD);
        }});
        put(TAIGA, new HashSet<StructureType>() {{
            add(MINESHAFT);
            add(STRONGHOLD);
            add(VILLAGE);
        }});
        put(SWAMP, new HashSet<StructureType>() {{
            add(MINESHAFT);
            add(STRONGHOLD);
            add(SWAMP_HUT);
        }});
        put(RIVER, new HashSet<StructureType>() {{
            add(MINESHAFT);
            add(STRONGHOLD);
        }});
        put(NETHER, new HashSet<StructureType>() {{
            add(NETHER_FORTRESS);
        }});
        put(THE_END, new HashSet<StructureType>() {{
            add(END_CITY);
        }});
        put(FROZEN_OCEAN, new HashSet<StructureType>() {{
            add(BURIED_TREASURE);
            add(MINESHAFT);
            add(OCEAN_RUIN);
            add(SHIPWRECK);
            add(STRONGHOLD);
        }});
        put(FROZEN_RIVER, new HashSet<StructureType>() {{
            add(MINESHAFT);
            add(STRONGHOLD);
        }});
        put(SNOWY_TUNDRA, new HashSet<StructureType>() {{
            add(IGLOO);
            add(MINESHAFT);
            add(STRONGHOLD);
            add(VILLAGE);
        }});
        put(SNOWY_MOUNTAINS, new HashSet<StructureType>() {{
            add(MINESHAFT);
            add(STRONGHOLD);
            add(VILLAGE);
        }});
        put(MUSHROOM_FIELDS, new HashSet<StructureType>() {{
            add(MINESHAFT);
            add(STRONGHOLD);
        }});
        put(MUSHROOM_FIELD_SHORE, new HashSet<StructureType>() {{
            add(MINESHAFT);
            add(STRONGHOLD);
        }});
        put(BEACH, new HashSet<StructureType>() {{
            add(BURIED_TREASURE);
            add(MINESHAFT);
            add(OCEAN_RUIN);
            add(SHIPWRECK);
            add(STRONGHOLD);
        }});
        put(DESERT_HILLS, new HashSet<StructureType>() {{
            add(DESERT_PYRAMID);
            add(MINESHAFT);
            add(STRONGHOLD);
            add(VILLAGE);
        }});
        put(WOODED_HILLS, new HashSet<StructureType>() {{
            add(MINESHAFT);
            add(STRONGHOLD);
        }});
        put(TAIGA_HILLS, new HashSet<StructureType>() {{
            add(MINESHAFT);
            add(STRONGHOLD);
            add(VILLAGE);
        }});
        put(MOUNTAIN_EDGE, new HashSet<StructureType>() {{
            add(MINESHAFT);
            add(STRONGHOLD);
        }});
        put(JUNGLE, new HashSet<StructureType>() {{
            add(JUNGLE_PYRAMID);
            add(MINESHAFT);
            add(STRONGHOLD);
        }});
        put(JUNGLE_HILLS, new HashSet<StructureType>() {{
            add(JUNGLE_PYRAMID);
            add(MINESHAFT);
            add(STRONGHOLD);
        }});
        put(JUNGLE_EDGE, new HashSet<StructureType>() {{
            add(JUNGLE_PYRAMID);
            add(MINESHAFT);
            add(STRONGHOLD);
        }});
        put(DEEP_OCEAN, new HashSet<StructureType>() {{
            add(BURIED_TREASURE);
            add(MINESHAFT);
            add(OCEAN_MONUMENT);
            add(OCEAN_RUIN);
            add(SHIPWRECK);
            add(STRONGHOLD);
        }});
        put(STONE_SHORE, new HashSet<StructureType>() {{
            add(BURIED_TREASURE);
            add(MINESHAFT);
            add(OCEAN_RUIN);
            add(SHIPWRECK);
            add(STRONGHOLD);
        }});
        put(SNOWY_BEACH, new HashSet<StructureType>() {{
            add(BURIED_TREASURE);
            add(MINESHAFT);
            add(OCEAN_RUIN);
            add(SHIPWRECK);
            add(STRONGHOLD);
        }});
        put(BIRCH_FOREST, new HashSet<StructureType>() {{
            add(MINESHAFT);
            add(STRONGHOLD);
        }});
        put(BIRCH_FOREST_HILLS, new HashSet<StructureType>() {{
            add(MINESHAFT);
            add(STRONGHOLD);
        }});
        put(DARK_FOREST, new HashSet<StructureType>() {{
            add(MINESHAFT);
            add(STRONGHOLD);
            add(WOODLAND_MANSION);
        }});
        put(SNOWY_TAIGA, new HashSet<StructureType>() {{
            add(IGLOO);
            add(MINESHAFT);
            add(STRONGHOLD);
            add(VILLAGE);
        }});
        put(SNOWY_TAIGA_HILLS, new HashSet<StructureType>() {{
            add(MINESHAFT);
            add(STRONGHOLD);
            add(VILLAGE);
        }});
        put(GIANT_TREE_TAIGA, new HashSet<StructureType>() {{
            add(MINESHAFT);
            add(STRONGHOLD);
        }});
        put(GIANT_TREE_TAIGA_HILLS, new HashSet<StructureType>() {{
            add(MINESHAFT);
            add(STRONGHOLD);
        }});
        put(WOODED_MOUNTAINS, new HashSet<StructureType>() {{
            add(MINESHAFT);
            add(STRONGHOLD);
        }});
        put(SAVANNA, new HashSet<StructureType>() {{
            add(MINESHAFT);
            add(STRONGHOLD);
            add(VILLAGE);
        }});
        put(SAVANNA_PLATEAU, new HashSet<StructureType>() {{
            add(MINESHAFT);
            add(STRONGHOLD);
            add(VILLAGE);
        }});
        put(BADLANDS, new HashSet<StructureType>() {{
            add(MINESHAFT);
            add(STRONGHOLD);
        }});
        put(WOODED_BADLANDS_PLATEAU, new HashSet<StructureType>() {{
            add(MINESHAFT);
            add(STRONGHOLD);
        }});
        put(BADLANDS_PLATEAU, new HashSet<StructureType>() {{
            add(MINESHAFT);
            add(STRONGHOLD);
        }});
        put(SMALL_END_ISLANDS, new HashSet<StructureType>() {{
            add(END_CITY);
        }});
        put(END_MIDLANDS, new HashSet<StructureType>() {{
            add(END_CITY);
        }});
        put(END_HIGHLANDS, new HashSet<StructureType>() {{
            add(END_CITY);
        }});
        put(END_BARRENS, new HashSet<StructureType>() {{
            add(END_CITY);
        }});
        put(WARM_OCEAN, new HashSet<StructureType>() {{
            add(BURIED_TREASURE);
            add(MINESHAFT);
            add(OCEAN_RUIN);
            add(SHIPWRECK);
            add(STRONGHOLD);
        }});
        put(LUKEWARM_OCEAN, new HashSet<StructureType>() {{
            add(BURIED_TREASURE);
            add(MINESHAFT);
            add(OCEAN_RUIN);
            add(SHIPWRECK);
            add(STRONGHOLD);
        }});
        put(COLD_OCEAN, new HashSet<StructureType>() {{
            add(BURIED_TREASURE);
            add(MINESHAFT);
            add(OCEAN_RUIN);
            add(SHIPWRECK);
            add(STRONGHOLD);
        }});
        put(DEEP_WARM_OCEAN, new HashSet<StructureType>() {{
            add(BURIED_TREASURE);
            add(MINESHAFT);
            add(OCEAN_MONUMENT);
            add(OCEAN_RUIN);
            add(SHIPWRECK);
            add(STRONGHOLD);
        }});
        put(DEEP_LUKEWARM_OCEAN, new HashSet<StructureType>() {{
            add(BURIED_TREASURE);
            add(MINESHAFT);
            add(OCEAN_MONUMENT);
            add(OCEAN_RUIN);
            add(SHIPWRECK);
            add(STRONGHOLD);
        }});
        put(DEEP_COLD_OCEAN, new HashSet<StructureType>() {{
            add(BURIED_TREASURE);
            add(MINESHAFT);
            add(OCEAN_MONUMENT);
            add(OCEAN_RUIN);
            add(SHIPWRECK);
            add(STRONGHOLD);
        }});
        put(DEEP_FROZEN_OCEAN, new HashSet<StructureType>() {{
            add(BURIED_TREASURE);
            add(MINESHAFT);
            add(OCEAN_MONUMENT);
            add(OCEAN_RUIN);
            add(SHIPWRECK);
            add(STRONGHOLD);
        }});
        put(THE_VOID, new HashSet<StructureType>() {{
        }});
        put(SUNFLOWER_PLAINS, new HashSet<StructureType>() {{
            add(MINESHAFT);
            add(STRONGHOLD);
            add(VILLAGE);
        }});
        put(DESERT_LAKES, new HashSet<StructureType>() {{
            add(DESERT_PYRAMID);
            add(MINESHAFT);
            add(STRONGHOLD);
            add(VILLAGE);
        }});
        put(GRAVELLY_MOUNTAINS, new HashSet<StructureType>() {{
            add(MINESHAFT);
            add(STRONGHOLD);
        }});
        put(FLOWER_FOREST, new HashSet<StructureType>() {{
            add(MINESHAFT);
            add(STRONGHOLD);
        }});
        put(TAIGA_MOUNTAINS, new HashSet<StructureType>() {{
            add(MINESHAFT);
            add(STRONGHOLD);
            add(VILLAGE);
        }});
        put(SWAMP_HILLS, new HashSet<StructureType>() {{
            add(MINESHAFT);
            add(STRONGHOLD);
            add(SWAMP_HUT);
        }});
        put(ICE_SPIKES, new HashSet<StructureType>() {{
            add(MINESHAFT);
            add(STRONGHOLD);
            add(VILLAGE);
        }});
        put(MODIFIED_JUNGLE, new HashSet<StructureType>() {{
            add(JUNGLE_PYRAMID);
            add(MINESHAFT);
            add(STRONGHOLD);
        }});
        put(MODIFIED_JUNGLE_EDGE, new HashSet<StructureType>() {{
            add(JUNGLE_PYRAMID);
            add(MINESHAFT);
            add(STRONGHOLD);
        }});
        put(TALL_BIRCH_FOREST, new HashSet<StructureType>() {{
            add(MINESHAFT);
            add(STRONGHOLD);
        }});
        put(TALL_BIRCH_HILLS, new HashSet<StructureType>() {{
            add(MINESHAFT);
            add(STRONGHOLD);
        }});
        put(DARK_FOREST_HILLS, new HashSet<StructureType>() {{
            add(MINESHAFT);
            add(STRONGHOLD);
            add(WOODLAND_MANSION);
        }});
        put(SNOWY_TAIGA_MOUNTAINS, new HashSet<StructureType>() {{
            add(MINESHAFT);
            add(STRONGHOLD);
            add(VILLAGE);
        }});
        put(GIANT_SPRUCE_TAIGA, new HashSet<StructureType>() {{
            add(MINESHAFT);
            add(STRONGHOLD);
        }});
        put(GIANT_SPRUCE_TAIGA_HILLS, new HashSet<StructureType>() {{
            add(MINESHAFT);
            add(STRONGHOLD);
        }});
        put(MODIFIED_GRAVELLY_MOUNTAINS, new HashSet<StructureType>() {{
            add(MINESHAFT);
            add(STRONGHOLD);
        }});
        put(SHATTERED_SAVANNA, new HashSet<StructureType>() {{
            add(MINESHAFT);
            add(STRONGHOLD);
            add(VILLAGE);
        }});
        put(SHATTERED_SAVANNA_PLATEAU, new HashSet<StructureType>() {{
            add(MINESHAFT);
            add(STRONGHOLD);
            add(VILLAGE);
        }});
        put(ERODED_BADLANDS, new HashSet<StructureType>() {{
            add(MINESHAFT);
            add(STRONGHOLD);
        }});
        put(MODIFIED_WOODED_BADLANDS_PLATEAU, new HashSet<StructureType>() {{
            add(MINESHAFT);
            add(STRONGHOLD);
        }});
        put(MODIFIED_BADLANDS_PLATEAU, new HashSet<StructureType>() {{
            add(MINESHAFT);
            add(STRONGHOLD);
        }});
    }};
    private static final Map<StructureType, String> LABELS = new HashMap<StructureType, String>() {{
        put(BURIED_TREASURE, "Buried Treasure");
        put(DESERT_PYRAMID, "Desert Pyramid");
        put(END_CITY, "End City");
        put(NETHER_FORTRESS, "Nether Fortress");
        put(IGLOO, "Igloo");
        put(JUNGLE_PYRAMID, "Jungle Pyramid");
        put(WOODLAND_MANSION, "Woodland Mansion");
        put(MINESHAFT, "Abandoned Mineshaft");
        put(OCEAN_MONUMENT, "Ocean Monument");
        put(OCEAN_RUIN, "Underwater Ruins");
        put(SHIPWRECK, "Shipwreck");
        put(STRONGHOLD, "Stronghold");
        put(SWAMP_HUT, "Witch Hut");
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
        new Metrics(this);
        // Set up the configuration
        this.saveDefaultConfig();
        FileConfiguration configuration = this.getConfig();
        configuration.options().copyDefaults(true);
        this.saveConfig();
        // Fill in data structures
        for (StructureType s : StructureType.getStructureTypes().values()) {
            String id = s.getName().toLowerCase(Locale.ROOT).replace("_", "");
            STRUCTURES.put(s, configuration.getBoolean("structures." + id));
            String label = configuration.getString("labels." + id);
            if (label != null) {
                LABELS.put(s, label);
            }
        }
        // Register for events
        this.getServer().getPluginManager().registerEvents(this, this);
        // Check if Dynmap is even enabled
        if (Bukkit.getPluginManager().isPluginEnabled("dynmap")) {
            // Set up our Dynmap layer
            api = ((DynmapCommonAPI) Bukkit.getPluginManager().getPlugin("dynmap")).getMarkerAPI();
            String layer = configuration.getString("layer.name");
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
        }
    }

    @EventHandler
    public void onChunkLoad(ChunkLoadEvent event) {
        this.process(new Location(event.getChunk().getWorld(), event.getChunk().getX() << 4, 64, event.getChunk().getZ() << 4));
    }

    private void process(Location location) {
        Biome biome = location.getWorld().getBiome(location.getBlockX(), location.getBlockZ());
        for (StructureType type : BIOMES.get(biome)) {
            if (STRUCTURES.get(type)) {
                Location structure = location.getWorld().locateNearestStructure(location, type, 1, false);
                if (structure != null) {
                    String id = type.getName().toLowerCase(Locale.ROOT).replace("_", "");
                    String world = structure.getWorld().getName();
                    int x = structure.getBlockX();
                    int z = structure.getBlockZ();
                    String label = "";
                    if (!noLabels) {
                        label = LABELS.get(type);
                        if (includeCoordinates) {
                            label = label + " [" + x * 16 + "," + z * 16 + "]";
                        }
                    }
                    set.createMarker(id + "," + x + "," + z, label, world, x, 64, z, api.getMarkerIcon("structures." + id), true);
                }
            }
        }
    }
}
