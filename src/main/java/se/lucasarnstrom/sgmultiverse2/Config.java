/**
 *  Name: Config.java
 *  Date: 12:56:03 PM - Apr 22, 2014
 * 
 *  Author: Lucas Arnström
 *  
 *  
 *  Copyright 2014 Lucas Arnström
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program. If not, see <http://www.gnu.org/licenses/>.
 *  
 *
 *  Filedescription:
 *  
 *  
 * 
 */

package se.lucasarnstrom.sgmultiverse2;

import java.util.HashMap;
import java.util.Map.Entry;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

public class Config {
	
	private static JavaPlugin plugin;
	private static FileConfiguration config;
	
	public static void init(JavaPlugin plugin) {
		Config.plugin = plugin;
		Config.config = plugin.getConfig();
	}
	
	@SuppressWarnings("serial")
	public static void checkDefaults() {
		
		boolean save = false;
		
		// General defaults
		HashMap<String, Object> defaults = new HashMap<String, Object>() {{
			
			// General
			put("debug", false);
			put("metrics", true);
		}};
		
		for(Entry<String, Object> e : defaults.entrySet()) {
			if(!config.contains(e.getKey())) {
				config.set(e.getKey(), e.getValue());
				save = true;
			}
		}
		
		// World defaults
		HashMap<String, Object> world_defaults = new HashMap<String, Object>() {{
			
			// Time
			put("time.start", 120);
			put("time.arena", 300);
			put("time.end",   120);
			
			// Itemlist
			put("lootlist", "itemlist"); // Allows server operators to define different lootlists for different worlds.
			
		}};
		
		if(!config.contains("worlds")) {
			config.set("worlds.sgmworld1.time.start", 120);
			config.set("worlds.sgmworld2.time.start", 120);
		}
		
		for(String name : config.getConfigurationSection("worlds").getKeys(false)) {
			for(Entry<String, Object> entry : world_defaults.entrySet()) {
				String key = "worlds." + name + "." + entry.getKey();
				if(!config.contains(key)) {
					config.set(key, entry.getValue());
					save = true;
				}
			}
		}
		
		if(save) {
			plugin.saveConfig();
		}
	}
}
