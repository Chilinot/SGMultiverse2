/**
 *  Name: Main.java
 *  Date: 23:32:50 - 5 apr 2014
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
 *  This is the main class of the plugin. This initiates all everything that needs
 *  to be setup for the plugin to be fully working.
 * 
 */

package se.lucasarnstrom.sgmultiverse2;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.WorldCreator;
import org.bukkit.plugin.java.JavaPlugin;

import se.lucasarnstrom.lucasutils.ConsoleLogger;
import se.lucasarnstrom.sgmultiverse2.managers.ChestManager;

public class Main extends JavaPlugin {
	
	private ConsoleLogger logger;
	
	public ChestManager chestManager;
	
	public void onEnable() {
		
		// Init config
		Config.init(this);
		
		// Load defaults
		Config.checkDefaults();
		
		// Initiate main logger
		ConsoleLogger.init(this);
		
		logger = new ConsoleLogger("Main");
		logger.debug("Running onEnable()...");
		
		
		// Initiate listeners
		logger.debug("Initiating listeners...");
		
		
		
		// Initiate managers
		logger.debug("Initiating managers...");
		
		chestManager = new ChestManager(this);
		
		
		
		// Load worlds
		logger.debug("Loading worlds...");
		
		for(String name : getConfig().getConfigurationSection("worlds").getKeys(false)) {
			
			logger.debug("Loading world \"" + name + "\".");
			
			World w = Bukkit.createWorld(new WorldCreator(name));
			
			
		}
		
		logger.debug("onEnable() is finished!");
	}
}
