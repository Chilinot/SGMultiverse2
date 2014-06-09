/**
 *  Author:  Lucas Arnström - LucasEmanuel @ Bukkit forums
 *  Contact: lucasarnstrom(at)gmail(dot)com
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
 *
 *  Filedescription:
 *
 *
 */


package se.lucasarnstrom.sgmultiverse2;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashMap;
import java.util.Map.Entry;

public class Config {

    private static JavaPlugin        plugin;
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
            put("auto-update", true);
        }};

        for(Entry<String, Object> e : defaults.entrySet()) {
            if(!config.contains(e.getKey())) {
                config.set(e.getKey(), e.getValue());
                save = true;
            }
        }

        // World defaults
        HashMap<String, Object> world_defaults = new HashMap<String, Object>() {{

            // General
            put("blockfilter", "2, 3, 5, 12, 13, 16, 17, 18, 20, 24, 31, 32, 35, 37, 38, 39, 40, 46, 50, 54, 58, 61, 85, 102");

            // Time
            put("time.start", 120);
            put("time.arena", 300);
            put("time.end", 120);

            // Itemlist
            put("lootlist", "default"); // Allows server operators to define different lootlists for different worlds.

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
