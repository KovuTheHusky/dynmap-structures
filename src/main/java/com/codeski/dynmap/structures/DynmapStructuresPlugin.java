package com.codeski.dynmap.structures;

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

import com.codeski.nbt.NBTReader;
import com.codeski.nbt.tags.NBT;
import com.codeski.nbt.tags.NBTByte;
import com.codeski.nbt.tags.NBTCompound;
import com.codeski.nbt.tags.NBTInteger;
import com.codeski.nbt.tags.NBTList;
import com.codeski.nbt.tags.NBTString;
import com.google.common.base.Joiner;

public class DynmapStructuresPlugin extends JavaPlugin implements Listener {
    private class DynmapStructuresRunnable implements Runnable {
        private final File directory;
        private boolean stop = false;
        private final World world;

        public DynmapStructuresRunnable(World world) {
            this.world = world;
            directory = new File(this.world.getWorldFolder(), "data/");
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
                    List<String> changed = new ArrayList<String>();
                    for (WatchEvent<?> event : events) {
                        String eventFile = event.context().toString();
                        for (String str : enabled)
                            if (str.equalsIgnoreCase(eventFile) && !changed.contains(str))
                                changed.add(str);
                    }
                    if (changed.size() > 0)
                        this.update(changed.toArray(new String[changed.size()]));
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

        public void stop() {
            stop = true;
        }

        public void update(String[] changed) {
            logger.info("Updating markers for world '" + world.getName() + "'.");
            logger.info("Updating: " + Joiner.on(", ").join(changed));
            for (String str : changed)
                try {
                    File file = new File(directory, str);
                    if (!file.exists())
                        continue;
                    NBTCompound structures = NBTReader.read(file).<NBTCompound> get("data").<NBTCompound> get("Features");
                    if (structures == null || structures.getPayload() == null)
                        continue;
                    for (NBT<?> temp : structures.getPayload()) {
                        NBTCompound structure = (NBTCompound) temp;
                        String id = structure.<NBTString> get("id").getPayload();
                        String wn = world.getName();
                        int x = structure.<NBTInteger> get("ChunkX").getPayload();
                        int z = structure.<NBTInteger> get("ChunkZ").getPayload();
                        if (str.equalsIgnoreCase("Village.dat") || str.equalsIgnoreCase("BOPVillage.dat")) {
                            // Make sure this Village is actually in the world
                            if (structure.<NBTByte> get("Valid") == null || structure.<NBTByte> get("Valid").getPayload() == 0)
                                continue;
                        } else if (str.equalsIgnoreCase("Temple.dat") || str.equalsIgnoreCase("BOPTemple.dat")) {
                            // Check if this Temple is from Biomes O Plenty
                            if (id.equalsIgnoreCase("BOPTemple"))
                                id = "Temple";
                            // Check if this Temple is actually a Witch
                            List<NBT<?>> children = structure.<NBTList> get("Children").getPayload();
                            if (children.size() > 0 && children.get(0) instanceof NBTCompound)
                                for (NBT<?> child : ((NBTCompound) children.get(0)).getPayload())
                                    if (child.getName().equalsIgnoreCase("Witch"))
                                        if (((NBTByte) child).getPayload() > 0)
                                            id = "Witch";
                        } else if (str.equalsIgnoreCase("Monument.dat")) {
                            // Make sure this Monument is actually in the world
                            if (structure.<NBTList> get("Processed").getPayload().size() == 0)
                                continue;
                        } else if (str.equalsIgnoreCase("Fortress.dat"))
                            // If this world is not Nether try to get one that is
                            if (world.getEnvironment() != Environment.NETHER && Bukkit.getWorld(world.getName() + "_nether") != null && Bukkit.getWorld(world.getName() + "_nether").getEnvironment() == Environment.NETHER)
                                wn = world.getName() + "_nether";
                            else
                                continue;
                        String label = id;
                        if (noLabels)
                            label = "";
                        else if (includeCoordinates)
                            label = id + " [" + x * 16 + "," + z * 16 + "]";
                        set.createMarker(id + "," + x + "," + z, label, wn, x * 16, 64, z * 16, api.getMarkerIcon("structures." + id.toLowerCase(Locale.ROOT)), false);
                    }
                } catch (IOException e) {
                    e.printStackTrace(System.err);
                }
        }
    }

    private MarkerAPI api;
    private FileConfiguration configuration;
    private String[] enabled;
    private final String[] images = { "Fortress", "Mineshaft", "Monument", "Stronghold", "Temple", "Village", "Witch" };
    private boolean includeCoordinates;
    private Logger logger;
    private boolean noLabels;
    private final HashMap<World, DynmapStructuresRunnable> runnables = new HashMap<World, DynmapStructuresRunnable>();
    private MarkerSet set;
    private final HashMap<World, Thread> threads = new HashMap<World, Thread>();

    @Override
    public void onDisable() {
    }

    @Override
    public void onEnable() {
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
            List<String> enabled = new ArrayList<String>();
            if (configuration.getBoolean("structures.fortress"))
                enabled.add("Fortress.dat");
            if (configuration.getBoolean("structures.mineshaft"))
                enabled.add("Mineshaft.dat");
            if (configuration.getBoolean("structures.monument"))
                enabled.add("Monument.dat");
            if (configuration.getBoolean("structures.stronghold"))
                enabled.add("Stronghold.dat");
            if (configuration.getBoolean("structures.temple") || configuration.getBoolean("structures.witch")) {
                enabled.add("BOPTemple.dat");
                enabled.add("Temple.dat");
            }
            if (configuration.getBoolean("structures.village")) {
                enabled.add("BOPVillage.dat");
                enabled.add("Village.dat");
            }
            this.enabled = enabled.toArray(new String[enabled.size()]);
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
                if (world.canGenerateStructures()) {
                    // Update markers for this world
                    DynmapStructuresRunnable r = new DynmapStructuresRunnable(world);
                    r.update(enabled);
                    // Add a thread to watch this world for changes
                    Thread t = new Thread(r);
                    t.setPriority(Thread.MIN_PRIORITY);
                    t.start();
                    runnables.put(world, r);
                    threads.put(world, t);
                }
                break;
            default:
        }
    }

    private void removeWorld(World world) {
        runnables.get(world).stop();
        runnables.remove(world);
        threads.remove(world);
    }
}
