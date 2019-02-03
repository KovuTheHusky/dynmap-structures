package com.kovuthehusky.dynmap.structures;

import java.io.*;
import java.util.*;

import org.bstats.bukkit.Metrics;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.StructureType;
import org.bukkit.World.Environment;
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
    private static final Map<Environment, Set<StructureType>> ENVIRONMENTS = new HashMap<Environment, Set<StructureType>>() {{
        put(Environment.NORMAL, new HashSet<StructureType>() {{
            add(BURIED_TREASURE);
            add(DESERT_PYRAMID);
            add(IGLOO);
            add(JUNGLE_PYRAMID);
            add(WOODLAND_MANSION);
            add(MINESHAFT);
            add(OCEAN_MONUMENT);
            add(OCEAN_RUIN);
            add(SHIPWRECK);
            add(STRONGHOLD);
            add(SWAMP_HUT);
            add(VILLAGE);
        }});
        put(Environment.NETHER, new HashSet<StructureType>() {{
            add(NETHER_FORTRESS);
        }});
        put(Environment.THE_END, new HashSet<StructureType>() {{
            add(END_CITY);
        }});
    }};
    private static final Map<StructureType, Set<Biome>> BIOMES = new HashMap<StructureType, Set<Biome>>() {{
        put(BURIED_TREASURE, new HashSet<Biome>() {{
            add(BEACH);
            add(STONE_SHORE);
            add(SNOWY_BEACH);
            add(OCEAN);
            add(DEEP_OCEAN);
            add(FROZEN_OCEAN);
            add(DEEP_FROZEN_OCEAN);
            add(COLD_OCEAN);
            add(DEEP_COLD_OCEAN);
            add(LUKEWARM_OCEAN);
            add(DEEP_LUKEWARM_OCEAN);
            add(WARM_OCEAN);
            add(DEEP_WARM_OCEAN);
        }});
        put(DESERT_PYRAMID, new HashSet<Biome>() {{
            add(DESERT);
            add(DESERT_HILLS);
            add(DESERT_LAKES);
        }});
        put(IGLOO, new HashSet<Biome>() {{
            add(SNOWY_TAIGA);
            add(SNOWY_TUNDRA);
        }});
        put(JUNGLE_PYRAMID, new HashSet<Biome>() {{
            add(JUNGLE);
            add(JUNGLE_EDGE);
            add(JUNGLE_HILLS);
            add(MODIFIED_JUNGLE);
            add(MODIFIED_JUNGLE_EDGE);
        }});
        put(OCEAN_MONUMENT, new HashSet<Biome>() {{
            add(DEEP_OCEAN);
            add(DEEP_FROZEN_OCEAN);
            add(DEEP_COLD_OCEAN);
            add(DEEP_LUKEWARM_OCEAN);
            add(DEEP_WARM_OCEAN);
        }});
        put(OCEAN_RUIN, new HashSet<Biome>() {{
            add(BEACH);
            add(STONE_SHORE);
            add(SNOWY_BEACH);
            add(OCEAN);
            add(DEEP_OCEAN);
            add(FROZEN_OCEAN);
            add(DEEP_FROZEN_OCEAN);
            add(COLD_OCEAN);
            add(DEEP_COLD_OCEAN);
            add(LUKEWARM_OCEAN);
            add(DEEP_LUKEWARM_OCEAN);
            add(WARM_OCEAN);
            add(DEEP_WARM_OCEAN);
        }});
        put(SHIPWRECK, new HashSet<Biome>() {{
            add(BEACH);
            add(STONE_SHORE);
            add(SNOWY_BEACH);
            add(OCEAN);
            add(DEEP_OCEAN);
            add(FROZEN_OCEAN);
            add(DEEP_FROZEN_OCEAN);
            add(COLD_OCEAN);
            add(DEEP_COLD_OCEAN);
            add(LUKEWARM_OCEAN);
            add(DEEP_LUKEWARM_OCEAN);
            add(WARM_OCEAN);
            add(DEEP_WARM_OCEAN);
        }});
        put(SWAMP_HUT, new HashSet<Biome>() {{
            add(SWAMP);
            add(SWAMP_HILLS);
        }});
        put(VILLAGE, new HashSet<Biome>() {{
            add(PLAINS);
            add(SUNFLOWER_PLAINS);
            add(DESERT);
            add(DESERT_HILLS);
            add(DESERT_LAKES);
            add(SAVANNA);
            add(SAVANNA_PLATEAU);
            add(SHATTERED_SAVANNA);
            add(SHATTERED_SAVANNA_PLATEAU);
            add(TAIGA);
            add(TAIGA_HILLS);
            add(TAIGA_MOUNTAINS);
            add(SNOWY_TAIGA);
            add(SNOWY_TAIGA_HILLS);
            add(SNOWY_TAIGA_MOUNTAINS);
            add(SNOWY_TUNDRA);
            add(SNOWY_MOUNTAINS);
            add(ICE_SPIKES);
        }});
        put(WOODLAND_MANSION, new HashSet<Biome>() {{
            add(DARK_FOREST);
            add(DARK_FOREST_HILLS);
        }});
    }};

    private MarkerAPI api;
    private FileConfiguration configuration;
    private MarkerSet set;

    @Override
    public void onEnable() {
        // Set up the metrics
        new Metrics(this);
        // Set up the configuration
        this.saveDefaultConfig();
        configuration = this.getConfig();
        configuration.options().copyDefaults(true);
        this.saveConfig();
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
            int minZoom = configuration.getInt("layer.minzoom");
            if (minZoom > 0) {
                set.setMinZoom(minZoom);
            }
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
        Location chunk = new Location(event.getWorld(), event.getChunk().getX() << 4, 64, event.getChunk().getZ() << 4);
        for (StructureType type : ENVIRONMENTS.get(event.getWorld().getEnvironment())) {
            String id = type.getName().toLowerCase(Locale.ROOT).replaceAll("_", "");
            if (configuration.getBoolean("structures." + id)) {
                Location structure = null;
                if (BIOMES.containsKey(type)) {
                    Biome biome = event.getWorld().getBiome(chunk.getBlockX(), chunk.getBlockZ());
                    if (BIOMES.get(type).contains(biome)) {
                        structure = event.getWorld().locateNearestStructure(chunk, type, 1, false);
                    }
                } else {
                    structure = event.getWorld().locateNearestStructure(chunk, type, 1, false);
                }
                if (structure != null) {
                    String world = structure.getWorld().getName();
                    int x = structure.getBlockX();
                    int z = structure.getBlockZ();
                    String label = "";
                    if (!configuration.getBoolean("layer.nolabels")) {
                        label = configuration.getString("labels." + id, LABELS.get(type));
                        if (configuration.getBoolean("layer.inc-coord")) {
                            label = label + " [" + x * 16 + "," + z * 16 + "]";
                        }
                    }
                    set.createMarker(id + "," + x + "," + z, label, world, x, 64, z, api.getMarkerIcon("structures." + id), true);
                }
            }
        }
    }
}
