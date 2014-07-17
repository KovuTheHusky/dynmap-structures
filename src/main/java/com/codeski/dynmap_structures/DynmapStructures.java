package com.codeski.dynmap_structures;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.World;
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

public class DynmapStructures extends JavaPlugin implements Listener
{
	private FileConfiguration configuration;

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
			DynmapCommonAPI api = (DynmapCommonAPI) Bukkit.getPluginManager().getPlugin("dynmap");
			MarkerAPI markerapi = api.getMarkerAPI();
			MarkerSet set = markerapi.createMarkerSet(configuration.getString("layer.name").toLowerCase(), configuration.getString("layer.name"), null, false);
			set.setHideByDefault(configuration.getBoolean("layer.hidebydefault"));
			set.setLayerPriority(configuration.getInt("layer.layerprio"));
			set.setLabelShow(!configuration.getBoolean("layer.nolabels"));
			set.setMinZoom(configuration.getInt("layer.minzoom"));
			InputStream in = this.getClass().getResourceAsStream("/fortress.png");
			markerapi.createMarkerIcon("structures.fortress", "Fortress", in);
			in = this.getClass().getResourceAsStream("/mineshaft.png");
			markerapi.createMarkerIcon("structures.mineshaft", "Mineshaft", in);
			in = this.getClass().getResourceAsStream("/stronghold.png");
			markerapi.createMarkerIcon("structures.stronghold", "Stronghold", in);
			in = this.getClass().getResourceAsStream("/temple.png");
			markerapi.createMarkerIcon("structures.temple", "Temple", in);
			in = this.getClass().getResourceAsStream("/village.png");
			markerapi.createMarkerIcon("structures.village", "Village", in);
			in = this.getClass().getResourceAsStream("/witch.png");
			markerapi.createMarkerIcon("structures.witch", "Witch", in);
			World world = Bukkit.getWorld("world");
			File dataFolder = new File(world.getWorldFolder(), "data");
			for (File f : dataFolder.listFiles())
				if (!f.getName().equals("villages.dat"))
					try {
						NBTReader r = new NBTReader(f);
						NBTCompound root = r.readNBT();
						NBTCompound features = (NBTCompound) ((NBTCompound) root.get("data")).get("Features");
						for (NBT temp : features.getPayload()) {
							NBTCompound entries = (NBTCompound) temp;
							String id = "";
							int x = Integer.MIN_VALUE, z = Integer.MIN_VALUE;
							boolean isWitch = false;
							for (NBT entry : entries.getPayload())
								if (entry.getName().equals("ChunkX"))
									x = ((NBTInteger) entry).getPayload();
								else if (entry.getName().equals("ChunkZ"))
									z = ((NBTInteger) entry).getPayload();
								else if (entry.getName().equals("id"))
									id = ((NBTString) entry).getPayload();
								else if (f.getName().equals("Temple.dat") && entry.getName().equals("Children")) {
									List<NBT> children = ((NBTList) entry).getPayload();
									if (children.size() > 0 && children.get(0) instanceof NBTCompound)
										for (NBT child : ((NBTCompound) children.get(0)).getPayload())
											if (child.getName().equals("Witch"))
												isWitch = ((NBTByte) child).getPayload() > 0;
								}
							if (x != Integer.MIN_VALUE && z != Integer.MIN_VALUE && id != "")
								if (id.equals("Fortress") && configuration.getBoolean("structures.fortress"))
									set.createMarker(id + "," + x + "," + z, id, "world_nether", x * 16, 64, z * 16, markerapi.getMarkerIcon("structures." + id.toLowerCase()), false);
								else if (isWitch && configuration.getBoolean("structures.witch"))
									set.createMarker(id + "," + x + "," + z, "Witch Hut", "world", x * 16, 64, z * 16, markerapi.getMarkerIcon("structures.witch"), false);
								else if (configuration.getBoolean("structures." + id.toLowerCase()))
									set.createMarker(id + "," + x + "," + z, id, "world", x * 16, 64, z * 16, markerapi.getMarkerIcon("structures." + id.toLowerCase()), false);
						}
					} catch (IOException e) {
						e.printStackTrace();
					}
		}
	}
}
