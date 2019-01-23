package com.kovuthehusky.dynmap.structures;

import java.io.*;
import java.util.*;
import java.util.logging.Logger;

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
    private MarkerAPI api;
    private Set<Chunk> chunks = new HashSet<>();
    private FileConfiguration configuration;
    private boolean includeCoordinates;
    private Logger logger;
    private Set<Marker> markers = new HashSet<>();
    private boolean noLabels;
    private MarkerSet set;

    private static final Map<String, String> LABELS = new HashMap<String, String>() {{
        put("buriedtreasure", "Buried Treasure");
        put("desertpyramid", "Desert Temple");
        put("endcity", "End City");
        put("fortress", "Nether Fortress");
        put("igloo", "Igloo");
        put("junglepyramid", "Jungle Temple");
        put("mansion", "Woodland Mansion");
        put("mineshaft", "Abandoned Mineshaft");
        put("monument", "Ocean Monument");
        put("oceanruin", "Underwater Ruins");
        put("shipwreck", "Shipwreck");
        put("stronghold", "Stronghold");
        put("swamphut", "Witch Hut");
        put("village", "Village");
    }};
    private static final Set<Biome> BIOMES_VILLAGE = new HashSet<Biome>() {{
        add(PLAINS);
        add(DESERT);
        add(SAVANNA);
        add(TAIGA);
        add(SUNFLOWER_PLAINS);
        add(DESERT_HILLS);
        add(DESERT_LAKES);
        add(SAVANNA_PLATEAU);
        add(SHATTERED_SAVANNA);
        add(SHATTERED_SAVANNA_PLATEAU);
        add(TAIGA_HILLS);
        add(TAIGA_MOUNTAINS);
    }};
    private static final Set<Biome> BIOMES_DESERT_TEMPLE = new HashSet<Biome>() {{
       add(DESERT);
       add(DESERT_HILLS);
       add(DESERT_LAKES);
    }};
    private static final Set<Biome> BIOMES_JUNGLE_TEMPLE = new HashSet<Biome>() {{
        add(JUNGLE);
        add(JUNGLE_EDGE);
        add(JUNGLE_HILLS);
        add(MODIFIED_JUNGLE);
        add(MODIFIED_JUNGLE_EDGE);
    }};

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
        // Save the logger for convenience
        logger = this.getLogger();
        // Check if Dynmap is even enabled
        if (Bukkit.getPluginManager().isPluginEnabled("dynmap")) {
            // Set up our Dynmap layer
            api = ((DynmapCommonAPI) Bukkit.getPluginManager().getPlugin("dynmap")).getMarkerAPI();
            set = api.createMarkerSet(configuration.getString("layer.name").toLowerCase(Locale.ROOT), configuration.getString("layer.name"), null, false);
            set.setHideByDefault(configuration.getBoolean("layer.hidebydefault"));
            set.setLayerPriority(configuration.getInt("layer.layerprio"));
            noLabels = configuration.getBoolean("layer.nolabels");
            int minZoom = configuration.getInt("layer.minzoom");
            if (minZoom > 0)
                set.setMinZoom(minZoom);
            includeCoordinates = configuration.getBoolean("layer.inc-coord");
            // Create the marker icons
            for (StructureType type : StructureType.getStructureTypes().values()) {
                String str = type.getName().toLowerCase(Locale.ROOT).replaceAll("_", "");
                InputStream in = this.getClass().getResourceAsStream("/" + str + ".png");
                if (in != null)
                    if (api.getMarkerIcon("structures." + str) == null)
                        api.createMarkerIcon("structures." + str, str, in);
                    else
                        api.getMarkerIcon("structures." + str).setMarkerIconImage(in);
            }
            // Set up ignored chunks
            try {
                File uri = new File(this.getDataFolder(), "chunks.dat");
                if (uri.exists()) {
                    FileInputStream file = new FileInputStream(uri);
                    ObjectInputStream reader = new ObjectInputStream(file);
                    while (true) {
                        try {
                            this.chunks = (Set<Chunk>) reader.readObject();
                        } catch (Exception ex) {
                            break;
                        }
                    }
                }
            } catch (Exception ex) {
                System.err.println("Failed to read: " + ex);
                ex.printStackTrace();
            }
            // Set up existing markers
            try {
                File uri = new File(this.getDataFolder(), "markers.dat");
                if (uri.exists()) {
                    FileInputStream file = new FileInputStream(uri);
                    ObjectInputStream reader = new ObjectInputStream(file);
                    while (true) {
                        try {
                            this.markers = (Set<Marker>) reader.readObject();
                        } catch (Exception ex) {
                            break;
                        }
                    }
                }
            } catch (Exception ex) {
                System.err.println("Failed to read: " + ex);
                ex.printStackTrace();
            }
            for (Marker marker : this.markers) {
                this.pin(marker);
            }
        }
    }

    @EventHandler
    public void onChunkLoad(ChunkLoadEvent event) {
        Location loc = new Location(event.getWorld(), event.getChunk().getX() << 4, 64, event.getChunk().getZ() << 4);
        if (chunks.contains(new Chunk(loc.getWorld().getName(), loc.getBlockX(), loc.getBlockZ()))) {
            return;
        } else {
            chunks.add(new Chunk(loc.getWorld().getName(), loc.getBlockX(), loc.getBlockZ()));
            try {
                File uri = new File(this.getDataFolder(), "chunks.dat");
                FileOutputStream file = new FileOutputStream(uri);
                ObjectOutputStream writer = new ObjectOutputStream(file);
                writer.writeObject(chunks);
                writer.close();
                file.close();
            } catch (Exception ex) {
                System.err.println("Failed to write: " + ex);
                ex.printStackTrace();
            }
        }
        if (configuration.getBoolean("structures.mineshaft")) {
            Location structure = event.getWorld().locateNearestStructure(loc, MINESHAFT, 1, false);
            if (structure != null)
                this.pin(MINESHAFT, structure);
        }
        if (configuration.getBoolean("structures.buriedtreasure")) {
            Location structure = event.getWorld().locateNearestStructure(loc, BURIED_TREASURE, 1, false);
            if (structure != null)
                this.pin(BURIED_TREASURE, structure);
        }
        if (configuration.getBoolean("structures.desertpyramid")) {
            Biome biome = event.getWorld().getBiome(loc.getBlockX(), loc.getBlockZ());
            if (BIOMES_DESERT_TEMPLE.contains(biome)) {
                Location structure = event.getWorld().locateNearestStructure(loc, DESERT_PYRAMID, 1, false);
                if (structure != null)
                    this.pin(DESERT_PYRAMID, structure);
            }
        }
        if (configuration.getBoolean("structures.endcity")) {
            if (event.getWorld().getEnvironment() == Environment.THE_END) {
                Location structure = event.getWorld().locateNearestStructure(loc, END_CITY, 1, false);
                if (structure != null)
                    this.pin(END_CITY, structure);
            }
        }
        if (configuration.getBoolean("structures.igloo")) {
            Biome biome = event.getWorld().getBiome(loc.getBlockX(), loc.getBlockZ());
            if (biome == SNOWY_TAIGA || biome == SNOWY_TUNDRA) {
                Location structure = event.getWorld().locateNearestStructure(loc, IGLOO, 1, false);
                if (structure != null)
                    this.pin(IGLOO, structure);
            }
        }
        if (configuration.getBoolean("structures.junglepyramid")) {
            Biome biome = event.getWorld().getBiome(loc.getBlockX(), loc.getBlockZ());
            if (BIOMES_JUNGLE_TEMPLE.contains(biome)) {
                Location structure = event.getWorld().locateNearestStructure(loc, JUNGLE_PYRAMID, 1, false);
                if (structure != null)
                    this.pin(JUNGLE_PYRAMID, structure);
            }
        }
        if (configuration.getBoolean("structures.fortress")) {
            if (event.getWorld().getEnvironment() == Environment.NETHER) {
                Location structure = event.getWorld().locateNearestStructure(loc, NETHER_FORTRESS, 1, false);
                if (structure != null)
                    this.pin(NETHER_FORTRESS, structure);
            }
        }
        if (configuration.getBoolean("structures.monument")) {
            Location structure = event.getWorld().locateNearestStructure(loc, OCEAN_MONUMENT, 1, false);
            if (structure != null)
                this.pin(OCEAN_MONUMENT, structure);
        }
        if (configuration.getBoolean("structures.shipwreck")) {
            Location structure = event.getWorld().locateNearestStructure(loc, SHIPWRECK, 1, false);
            if (structure != null)
                this.pin(SHIPWRECK, structure);
        }
        if (configuration.getBoolean("structures.stronghold")) {
            Location structure = event.getWorld().locateNearestStructure(loc, STRONGHOLD, 1, false);
            if (structure != null)
                this.pin(STRONGHOLD, structure);
        }
        if (configuration.getBoolean("structures.oceanruins")) {
            Location structure = event.getWorld().locateNearestStructure(loc, OCEAN_RUIN, 1, false);
            if (structure != null)
                this.pin(OCEAN_RUIN, structure);
        }
        if (configuration.getBoolean("structures.village")) {
            Biome biome = event.getWorld().getBiome(loc.getBlockX(), loc.getBlockZ());
            if (BIOMES_VILLAGE.contains(biome)) {
                Location structure = event.getWorld().locateNearestStructure(loc, VILLAGE, 1, false);
                if (structure != null)
                    this.pin(VILLAGE, structure);
            }
        }
        if (configuration.getBoolean("structures.swamphut")) {
            Biome biome = event.getWorld().getBiome(loc.getBlockX(), loc.getBlockZ());
            if (biome == SWAMP) {
                Location structure = event.getWorld().locateNearestStructure(loc, SWAMP_HUT, 1, false);
                if (structure != null)
                    this.pin(SWAMP_HUT, structure);
            }
        }
        if (configuration.getBoolean("structures.mansion")) {
            Biome biome = event.getWorld().getBiome(loc.getBlockX(), loc.getBlockZ());
            if (biome == DARK_FOREST || biome == DARK_FOREST_HILLS) {
                Location structure = event.getWorld().locateNearestStructure(loc, WOODLAND_MANSION, 1, false);
                if (structure != null)
                    this.pin(WOODLAND_MANSION, structure);
            }
        }
    }

    private void pin(StructureType type, Location location) {
        String id = type.getName().toLowerCase(Locale.ROOT).replaceAll("_", "");
        String world = location.getWorld().getName();
        int x = location.getBlockX();
        int z = location.getBlockZ();

        // Add to marker file
        Marker marker = new Marker(id, world, x, z);
        if (markers.contains(marker)) {
            return;
        } else {
            markers.add(marker);
            try {
                File uri = new File(this.getDataFolder(), "markers.dat");
                FileOutputStream file = new FileOutputStream(uri);
                ObjectOutputStream writer = new ObjectOutputStream(file);
                writer.writeObject(markers);
                writer.close();
                file.close();
            } catch (Exception ex) {
                System.err.println("Failed to write: " + ex);
                ex.printStackTrace();
            }
            // Actually create the marker
            this.pin(marker);
        }
    }

    private void pin(Marker marker) {
        String id = marker.id;
        String world = marker.world;
        int x = marker.x;
        int z = marker.z;

        String label = "";
        if (!noLabels) {
            label = configuration.getString("labels." + id, LABELS.get(id));
            if (includeCoordinates)
                label = label + " [" + x * 16 + "," + z * 16 + "]";
        }

        set.createMarker(id + "," + x + "," + z, label, world, x, 64, z, api.getMarkerIcon("structures." + id), false);
    }
}
