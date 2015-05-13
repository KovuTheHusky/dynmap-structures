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

public class DynmapStructuresPlugin extends JavaPlugin implements Listener
{
	private class DynmapStructuresRunnable implements Runnable {
		private final File directory;
		private boolean stop = false;
		private final World world;

		public DynmapStructuresRunnable(World world) {
			super();
			this.world = world;
			directory = new File(this.world.getWorldFolder(), "data/");
		}

		@Override
		public void run() {
			logger.info("Adding thread for world '" + world.getName() + "'.");
			Path path = Paths.get(directory.toURI());
			try {
				WatchService watcher = path.getFileSystem().newWatchService();
				path.register(watcher, StandardWatchEventKinds.ENTRY_CREATE, StandardWatchEventKinds.ENTRY_MODIFY);
				WatchKey key = watcher.take();
				for (; !stop;) {
					List<WatchEvent<?>> events = key.pollEvents();
					if (events.size() == 0)
						continue;
					List<String> changed = new ArrayList<String>();
					for (WatchEvent event : events)
						if (event.kind() == StandardWatchEventKinds.ENTRY_CREATE || event.kind() == StandardWatchEventKinds.ENTRY_MODIFY)
							for (String str : files)
								if (str.equalsIgnoreCase(event.context().toString()))
									changed.add(str);
					if (changed.size() == 0)
						continue;
					this.update(changed.toArray(new String[changed.size()]));
				}
			} catch (Exception e) {
				e.printStackTrace();
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
					if (structures != null && structures.getPayload() != null)
						for (NBT temp : structures.getPayload()) {
							NBTCompound structure = (NBTCompound) temp;
							String id = structure.<NBTString> get("id").getPayload();
							int x = structure.<NBTInteger> get("ChunkX").getPayload();
							int z = structure.<NBTInteger> get("ChunkZ").getPayload();
							boolean isVillage = structure.<NBTByte> get("Valid") != null && structure.<NBTByte> get("Valid").getPayload() > 0;
							if (str.equalsIgnoreCase("Village.dat") && !isVillage)
								continue;
							boolean isWitch = false;
							if (str.equals("Temple.dat")) {
								List<NBT> children = structure.<NBTList> get("Children").getPayload();
								if (children.size() > 0 && children.get(0) instanceof NBTCompound)
									for (NBT child : ((NBTCompound) children.get(0)).getPayload())
										if (child.getName().equals("Witch"))
											isWitch = ((NBTByte) child).getPayload() > 0;
							}
							if (str.equalsIgnoreCase("Monument.dat"))
								if (structure.<NBTList> get("Processed").getPayload().size() == 0)
									continue;
							if (id.equals("Fortress") && configuration.getBoolean("structures.fortress")) {
								String fortressWorld = null;
								if (world.getEnvironment() == Environment.NETHER)
									fortressWorld = world.getName();
								else if (Bukkit.getWorld(world.getName() + "_nether") != null)
									fortressWorld = world.getName() + "_nether";
								if (fortressWorld != null)
									set.createMarker(id + "," + x + "," + z, configuration.getBoolean("layer.nolabels") ? "" : id, fortressWorld, x * 16, 64, z * 16, api.getMarkerIcon("structures." + id.toLowerCase()), false);
							} else if (isWitch && configuration.getBoolean("structures.witch"))
								set.createMarker(id + "," + x + "," + z, configuration.getBoolean("layer.nolabels") ? "" : "Witch Hut", world.getName(), x * 16, 64, z * 16, api.getMarkerIcon("structures.witch"), false);
							else if (configuration.getBoolean("structures." + id.toLowerCase()))
								set.createMarker(id + "," + x + "," + z, configuration.getBoolean("layer.nolabels") ? "" : id, world.getName(), x * 16, 64, z * 16, api.getMarkerIcon("structures." + id.toLowerCase()), false);
						}
				} catch (IOException e) {
					e.printStackTrace();
				}
		}
	}

	private MarkerAPI api;
	private FileConfiguration configuration;
	private final String[] files = { "Fortress.dat", "Mineshaft.dat", "Monument.dat", "Stronghold.dat", "Temple.dat", "Village.dat" };
	private final String[] images = { "Fortress", "Mineshaft", "Monument", "Stronghold", "Temple", "Village", "Witch" };
	private Logger logger;
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
			set = api.createMarkerSet(configuration.getString("layer.name").toLowerCase(), configuration.getString("layer.name"), null, false);
			set.setHideByDefault(configuration.getBoolean("layer.hidebydefault"));
			set.setLayerPriority(configuration.getInt("layer.layerprio"));
			// set.setLabelShow(!configuration.getBoolean("layer.nolabels"));
			set.setMinZoom(configuration.getInt("layer.minzoom"));
			// Create the marker icons
			for (String str : images) {
				InputStream in = this.getClass().getResourceAsStream("/" + str.toLowerCase() + ".png");
				if (in != null)
					if (api.getMarkerIcon("structures." + str.toLowerCase()) == null)
						api.createMarkerIcon("structures." + str.toLowerCase(), str, in);
					else
						api.getMarkerIcon("structures." + str.toLowerCase()).setMarkerIconImage(in);
			}
			// Parse the worlds that have already been loaded
			for (World w : Bukkit.getWorlds())
				switch (w.getEnvironment()) {
					case NORMAL:
					case NETHER:
						if (w.canGenerateStructures()) {
							// Update markers for this world
							DynmapStructuresRunnable r = new DynmapStructuresRunnable(w);
							r.update(files);
							// Add a thread to watch this world for changes
							Thread t = new Thread(r);
							t.setPriority(Thread.MIN_PRIORITY);
							t.start();
							runnables.put(w, r);
							threads.put(w, t);
						}
						break;
					default:
				}
		}
	}

	@EventHandler
	public void onWorldLoad(WorldLoadEvent event) {
		switch (event.getWorld().getEnvironment()) {
			case NORMAL:
			case NETHER:
				if (event.getWorld().canGenerateStructures()) {
					// Update markers for this world
					DynmapStructuresRunnable r = new DynmapStructuresRunnable(event.getWorld());
					r.update(files);
					// Add a thread to watch this world for changes
					Thread t = new Thread(r);
					t.setPriority(Thread.MIN_PRIORITY);
					t.start();
					runnables.put(event.getWorld(), r);
					threads.put(event.getWorld(), t);
				}
				break;
			default:
		}
	}

	@EventHandler
	public void onWorldUnload(WorldUnloadEvent event) {
		runnables.get(event.getWorld()).stop();
		runnables.remove(event.getWorld());
		threads.remove(event.getWorld());
	}
}
