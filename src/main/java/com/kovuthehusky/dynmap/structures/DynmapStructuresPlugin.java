package com.kovuthehusky.dynmap.structures;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardWatchEventKinds;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.logging.Logger;

import com.kovuthehusky.nbt.NBTReader;
import com.kovuthehusky.nbt.tags.NBT;
import com.kovuthehusky.nbt.tags.NBTByte;
import com.kovuthehusky.nbt.tags.NBTCompound;
import com.kovuthehusky.nbt.tags.NBTInteger;
import com.kovuthehusky.nbt.tags.NBTList;
import com.kovuthehusky.nbt.tags.NBTString;
import com.google.common.base.Joiner;
import org.bstats.bukkit.Metrics;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.World.Environment;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.world.WorldLoadEvent;
import org.bukkit.event.world.WorldUnloadEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.dynmap.DynmapCommonAPI;
import org.dynmap.markers.MarkerAPI;
import org.dynmap.markers.MarkerSet;

@SuppressWarnings("unused")
public class DynmapStructuresPlugin extends JavaPlugin implements Listener {
    private class DynmapStructuresRunnable implements Runnable {
        private final File directory;
        private boolean stop = false;
        private final World world;

        private DynmapStructuresRunnable(World world) {
            this.world = world;
            
            if(configuration.contains("worldSettings." + world.getName())) {
            	directory = new File(configuration.getString("worldSettings." + world.getName()), "data/");
            } else {
            	directory = new File(this.world.getWorldFolder(), "data/");
            }
        }

        @Override
        public void run() {
            logger.info("Adding thread for world '" + world.getName() + "'.");
            Path path = Paths.get(directory.toURI());
            try (WatchService watcher = path.getFileSystem().newWatchService()) {
                path.register(watcher, StandardWatchEventKinds.ENTRY_CREATE, StandardWatchEventKinds.ENTRY_MODIFY);
                while (!stop) {
                    WatchKey key = watcher.take();
                    List<WatchEvent<?>> events = key.pollEvents();
                    if (events.size() == 0)
                        continue;
                    List<String> changed = new ArrayList<>();
                    for (WatchEvent<?> event : events) {
                        String eventFile = event.context().toString();
                        for (String str : enabled)
                            if (str.equalsIgnoreCase(eventFile) && !changed.contains(str))
                                changed.add(str);
                    }
                    if (changed.size() > 0)
                        this.update(changed.toArray(new String[0]));
                    if (!key.reset()) {
                        logger.warning("Something went wrong with the watch service and it must be stopped. Sorry!");
                        stop = true;
                    }
                }
            } catch (Exception e) {
                e.printStackTrace(System.err);
            }
            logger.info("Removing thread for world '" + world.getName() + "'.");
        }

        private void stop() {
            stop = true;
        }

        private void update(String[] changed) {
            logger.info("Updating markers for world '" + world.getName() + "'.");
            logger.info("Updating: " + Joiner.on(", ").join(changed));
            for (String str : changed)
                try {
                    File file = new File(directory, str);
                    if (!file.exists())
                        continue;
                    NBTCompound structures = NBTReader.read(file).<NBTCompound>get("data").get("Features");
                    if (structures == null || structures.getPayload() == null)
                        continue;
                    for (NBT<?> temp : structures) {
                        NBTCompound structure = (NBTCompound) temp;
                        String id = structure.<NBTString>get("id").getPayload();
                        String image = "default";
                        String wn = world.getName();
                        int x = structure.<NBTInteger>get("ChunkX").getPayload();
                        int z = structure.<NBTInteger>get("ChunkZ").getPayload();
                        if (str.equalsIgnoreCase("Village.dat") || str.equalsIgnoreCase("BOPVillage.dat")) {
                            id = configuration.getString("labels.village", "Village");
                            image = "village";
                            // Make sure this Village is actually in the world
                            if (structure.<NBTByte>get("Valid").getPayload() == 0) {
                                continue;
                            } else {
                                boolean placed = false;
                                NBTList children = structure.get("Children");
                                for (NBT child : children) {
                                    if (((NBTCompound) child).<NBTInteger>get("HPos").getPayload() >= 0)
                                        placed = true;
                                }
                                if (!placed) {
                                    continue;
                                }
                            }
                        } else if (str.equalsIgnoreCase("Temple.dat") || str.equalsIgnoreCase("BOPTemple.dat")) {
                            id = configuration.getString("labels.temple", "Temple");
                            image = "temple";
                            // Check if this Temple exists and if it's actually something else
                            NBTCompound children = (NBTCompound) structure.<NBTList>get("Children").get(0);
                            String type = children.<NBTString>get("id").getPayload();
                            // All desert temples spawn at y=64 automatically
                            if (!type.equalsIgnoreCase("TeDP") && children.<NBTInteger>get("HPos").getPayload() < 0)
                                continue;
                            // Check if this Temple is actually an Igloo or Witch
                            if (type.equalsIgnoreCase("Iglu")) {
                                id = configuration.getString("labels.igloo", "Igloo");
                                image = "igloo";
                            } else if (type.equalsIgnoreCase("TeSH")) {
                                id = configuration.getString("labels.witch", "Witch Hut");
                                image = "witch";
                            }
                            // Skip this structure if it is disabled in the configuration
                            if (id.equalsIgnoreCase("Igloo") && !configuration.getBoolean("structures.igloo"))
                                continue;
                            else if (id.equalsIgnoreCase("Temple") && !configuration.getBoolean("structures.temple"))
                                continue;
                            else if (id.equalsIgnoreCase("Witch") && !configuration.getBoolean("structures.witch"))
                                continue;
                        } else if (str.equalsIgnoreCase("Monument.dat")) {
                            id = configuration.getString("labels.monument", "Ocean Monument");
                            image = "monument";
                            // Make sure this Monument is actually in the world
                            if (structure.<NBTList>get("Processed").getPayload().size() == 0)
                                continue;
                        } else if (str.equalsIgnoreCase("Mansion.dat")) {
                            id = configuration.getString("labels.mansion", "Woodland Mansion");
                            image = "mansion";
                        } else if (str.equalsIgnoreCase("Mineshaft.dat")) {
                            id = configuration.getString("labels.mineshaft", "Abandoned Mineshaft");
                            image = "mineshaft";
                        } else if (str.equalsIgnoreCase("Stronghold.dat")) {
                            id = configuration.getString("labels.stronghold", "Stronghold");
                            image = "stronghold";
                        } else if (str.equalsIgnoreCase("Fortress.dat")) {
                            id = configuration.getString("labels.fortress", "Nether Fortress");
                            image = "fortress";
                            // If not Nether try to get it manually
                            if (world.getEnvironment() != Environment.NETHER) {
                                if (Bukkit.getWorld(world.getName() + "_nether") != null && Bukkit.getWorld(world.getName() + "_nether").getEnvironment() == Environment.NETHER) {
                                    wn = world.getName() + "_nether";
                                } else {
                                    continue;
                                }
                            }
                        } else if (str.equalsIgnoreCase("EndCity.dat")) {
                            id = configuration.getString("labels.endcity", "End City");
                            image = "endcity";
                            // If not The End try to get it manually
                            if (world.getEnvironment() != Environment.THE_END) {
                                if (Bukkit.getWorld(world.getName() + "_the_end") != null && Bukkit.getWorld(world.getName() + "_the_end").getEnvironment() == Environment.THE_END) {
                                    wn = world.getName() + "_the_end";
                                } else {
                                    continue;
                                }
                            }
                        }

                        String label = id;
                        if (noLabels)
                            label = "";
                        else if (includeCoordinates)
                            label = id + " [" + x * 16 + "," + z * 16 + "]";
                        set.createMarker(id + "," + x + "," + z, label, wn, x * 16, 64, z * 16, api.getMarkerIcon("structures." + image.toLowerCase(Locale.ROOT).replaceAll("\\s+", "")), false);
                    }
                } catch (IOException e) {
                    e.printStackTrace(System.err);
                }
        }
    }

    private MarkerAPI api;
    private FileConfiguration configuration;
    private String[] enabled;
    private final String[] images = {"EndCity", "Fortress", "Igloo", "Mansion", "Mineshaft", "Monument", "Stronghold", "Temple", "Village", "Witch"};
    private boolean includeCoordinates;
    private Logger logger;
    private boolean noLabels;
    private final HashMap<World, DynmapStructuresRunnable> threads = new HashMap<>();
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
            for (String str : images) {
                InputStream in = this.getClass().getResourceAsStream("/" + str.toLowerCase(Locale.ROOT) + ".png");
                if (in != null)
                    if (api.getMarkerIcon("structures." + str.toLowerCase(Locale.ROOT)) == null)
                        api.createMarkerIcon("structures." + str.toLowerCase(Locale.ROOT), str, in);
                    else
                        api.getMarkerIcon("structures." + str.toLowerCase(Locale.ROOT)).setMarkerIconImage(in);
            }
            // Build an array of files to parse if changed
            List<String> enabled = new ArrayList<>();
            if (configuration.getBoolean("structures.endcity"))
                enabled.add("EndCity.dat");
            if (configuration.getBoolean("structures.fortress"))
                enabled.add("Fortress.dat");
            if (configuration.getBoolean("structures.mansion"))
                enabled.add("Mansion.dat");
            if (configuration.getBoolean("structures.mineshaft"))
                enabled.add("Mineshaft.dat");
            if (configuration.getBoolean("structures.monument"))
                enabled.add("Monument.dat");
            if (configuration.getBoolean("structures.stronghold"))
                enabled.add("Stronghold.dat");
            if (configuration.getBoolean("structures.igloo") || configuration.getBoolean("structures.temple") || configuration.getBoolean("structures.witch")) {
                enabled.add("BOPTemple.dat");
                enabled.add("Temple.dat");
            }
            if (configuration.getBoolean("structures.village")) {
                enabled.add("BOPVillage.dat");
                enabled.add("Village.dat");
            }
            this.enabled = enabled.toArray(new String[0]);
            // Parse the worlds that have already been loaded
            for (World w : Bukkit.getWorlds())
                this.addWorld(w);
        }
    }

    @EventHandler
    public void onWorldLoad(WorldLoadEvent event) {
        this.addWorld(event.getWorld());
    }

    @EventHandler
    public void onWorldUnload(WorldUnloadEvent event) {
        this.removeWorld(event.getWorld());
    }

    private void addWorld(World world) {
        switch (world.getEnvironment()) {
            case NORMAL:
            case NETHER:
            case THE_END:
                if (world.canGenerateStructures()) {
                    // Update markers for this world
                    DynmapStructuresRunnable r = new DynmapStructuresRunnable(world);
                    r.update(enabled);
                    // Add a thread to watch this world for changes
                    Thread t = new Thread(r);
                    t.setPriority(Thread.MIN_PRIORITY);
                    t.start();
                    threads.put(world, r);
                }
                break;
            default:
        }
    }

    private void removeWorld(World world) {
        threads.get(world).stop();
        threads.remove(world);
    }
}
