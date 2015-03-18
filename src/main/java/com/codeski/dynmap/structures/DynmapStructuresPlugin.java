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
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.event.Listener;
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
	private FileConfiguration configuration;
	private final String[] dat = { "Fortress.dat", "Mineshaft.dat", "Monument.dat", "Stronghold.dat", "Temple.dat", "Village.dat" };
	private final String[] img = { "Fortress", "Mineshaft", "Monument", "Stronghold", "Temple", "Village", "Witch" };
	MarkerAPI api;
	File data;
	MarkerSet set;

	@Override
	public void onDisable() {
		//
	}

	@Override
	public void onEnable() {
		this.saveDefaultConfig();
		configuration = this.getConfig();
		configuration.options().copyDefaults(true);
		this.saveConfig();
		if (Bukkit.getPluginManager().isPluginEnabled("dynmap")) {
			api = ((DynmapCommonAPI) Bukkit.getPluginManager().getPlugin("dynmap")).getMarkerAPI();
			set = api.createMarkerSet(configuration.getString("layer.name").toLowerCase(), configuration.getString("layer.name"), null, false);
			set.setHideByDefault(configuration.getBoolean("layer.hidebydefault"));
			set.setLayerPriority(configuration.getInt("layer.layerprio"));
			// set.setLabelShow(!configuration.getBoolean("layer.nolabels"));
			set.setMinZoom(configuration.getInt("layer.minzoom"));
			for (String str : img) {
				InputStream in = this.getClass().getResourceAsStream("/" + str.toLowerCase() + ".png");
				if (in != null)
					if (api.getMarkerIcon("structures." + str.toLowerCase()) == null)
						api.createMarkerIcon("structures." + str.toLowerCase(), str, in);
					else
						api.getMarkerIcon("structures." + str.toLowerCase()).setMarkerIconImage(in);
			}
			data = new File(Bukkit.getWorld("world").getWorldFolder(), "data/");
			Bukkit.getLogger().info("Updating: " + Joiner.on(", ").join(dat));
			this.update(dat);
			Runnable r = new Runnable() {
				@Override
				public void run() {
					Path dir = Paths.get(data.toURI());
					try {
						WatchService watcher = dir.getFileSystem().newWatchService();
						dir.register(watcher, StandardWatchEventKinds.ENTRY_CREATE, StandardWatchEventKinds.ENTRY_MODIFY);
						WatchKey key = watcher.take();
						for (;;) {
							List<WatchEvent<?>> events = key.pollEvents();
							if (events.size() == 0)
								continue;
							List<String> filesChanged = new ArrayList<String>();
							for (WatchEvent event : events)
								if (event.kind() == StandardWatchEventKinds.ENTRY_CREATE || event.kind() == StandardWatchEventKinds.ENTRY_MODIFY)
									for (String str : dat)
										if (str.equalsIgnoreCase(event.context().toString()))
											filesChanged.add(str);
							if (filesChanged.size() == 0)
								continue;
							Bukkit.getLogger().info("Updating: " + Joiner.on(", ").join(filesChanged));
							DynmapStructuresPlugin.this.update(filesChanged.toArray(new String[filesChanged.size()]));
						}
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			};
			new Thread(r).start();
		}
	}

	private void update(String[] dat) {
		for (String str : dat)
			try {
				File file = new File(data, str);
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
						if (id.equals("Fortress") && configuration.getBoolean("structures.fortress"))
							set.createMarker(id + "," + x + "," + z, configuration.getBoolean("layer.nolabels") ? "" : id, "world_nether", x * 16, 64, z * 16, api.getMarkerIcon("structures." + id.toLowerCase()), false);
						else if (isWitch && configuration.getBoolean("structures.witch"))
							set.createMarker(id + "," + x + "," + z, configuration.getBoolean("layer.nolabels") ? "" : "Witch Hut", "world", x * 16, 64, z * 16, api.getMarkerIcon("structures.witch"), false);
						else if (configuration.getBoolean("structures." + id.toLowerCase()))
							set.createMarker(id + "," + x + "," + z, configuration.getBoolean("layer.nolabels") ? "" : id, "world", x * 16, 64, z * 16, api.getMarkerIcon("structures." + id.toLowerCase()), false);
					}
			} catch (IOException e) {
				e.printStackTrace();
			}
	}
}
