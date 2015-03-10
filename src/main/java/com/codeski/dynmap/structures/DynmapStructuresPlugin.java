package com.codeski.dynmap.structures;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
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

public class DynmapStructuresPlugin extends JavaPlugin implements Listener
{
	private FileConfiguration configuration;
	private final String[] dat = { "Fortress.dat", "Mineshaft.dat", "Monument.dat", "Stronghold.dat", "Temple.dat", "Village.dat" };
	private final String[] img = { "Fortress", "Mineshaft", "Monument", "Stronghold", "Temple", "Village", "Witch" };

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
			MarkerAPI api = ((DynmapCommonAPI) Bukkit.getPluginManager().getPlugin("dynmap")).getMarkerAPI();
			MarkerSet set = api.createMarkerSet(configuration.getString("layer.name").toLowerCase(), configuration.getString("layer.name"), null, false);
			set.setHideByDefault(configuration.getBoolean("layer.hidebydefault"));
			set.setLayerPriority(configuration.getInt("layer.layerprio"));
			// set.setLabelShow(!configuration.getBoolean("layer.nolabels"));
			set.setMinZoom(configuration.getInt("layer.minzoom"));
			for (String str : img) {
				InputStream in = this.getClass().getResourceAsStream("/" + str + ".png");
				api.createMarkerIcon("structures." + str.toLowerCase(), str, in);
			}
			for (String str : dat)
				try {
					File file = new File(Bukkit.getWorld("world").getWorldFolder(), "data/" + str);
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
}
