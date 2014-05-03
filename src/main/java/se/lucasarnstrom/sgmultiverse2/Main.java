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

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.WorldCreator;
import org.bukkit.plugin.java.JavaPlugin;
import org.mcstats.Metrics;
import se.lucasarnstrom.lucasutils.ConsoleLogger;
import se.lucasarnstrom.sgmultiverse2.databases.SQLiteInterface;
import se.lucasarnstrom.sgmultiverse2.listeners.Blocks;
import se.lucasarnstrom.sgmultiverse2.listeners.Misc;
import se.lucasarnstrom.sgmultiverse2.listeners.Players;
import se.lucasarnstrom.sgmultiverse2.managers.ChestManager;
import se.lucasarnstrom.sgmultiverse2.managers.WorldManager;

import java.io.IOException;

public class Main extends JavaPlugin {

	private ConsoleLogger logger;

	public ChestManager chestManager;
	public WorldManager worldManager;

	public SQLiteInterface sqlite;

	public void onEnable() {

		// Init config
		Config.init(this);

		// Load defaults
		Config.checkDefaults();

		// Initiate main logger
		ConsoleLogger.init(this);

		logger = new ConsoleLogger("Main");
		logger.debug("Running onEnable()...");

		// Initiating storage
		sqlite = new SQLiteInterface(this);

		// Register commands
		logger.debug("Registering commands...");
		Commands c = new Commands(this);

		getCommand("sgjoin").setExecutor(c);
		getCommand("sglocation").setExecutor(c);
		getCommand("sgtp").setExecutor(c);

		// Initiate listeners
		logger.debug("Initiating listeners...");

		getServer().getPluginManager().registerEvents(new Blocks(this), this);
		getServer().getPluginManager().registerEvents(new Misc(this), this);
		getServer().getPluginManager().registerEvents(new Players(this), this);

		//TODO start on the listeners

		// Initiate managers
		logger.debug("Initiating managers...");

		chestManager = new ChestManager(this);
		worldManager = new WorldManager(this);


		// Load worlds
		logger.debug("Loading worlds...");

		for (String name : getConfig().getConfigurationSection("worlds").getKeys(false)) {

			logger.debug("Loading world \"" + name + "\".");

			World w = Bukkit.createWorld(new WorldCreator(name));

			chestManager.addWorld(name);
			worldManager.addWorld(w);
		}

		// Metrics
		if (getConfig().getBoolean("metrics")) {
			try {
				logger.debug("Initiating metrics...");
				Metrics metrics = new Metrics(this);
				metrics.start();
			} catch (IOException e) {
				logger.severe("Failed to submit stats to MCStats.org! Please contact author of this plugin!");
			}
		}

		logger.debug("onEnable() is finished!");
	}

	public void onDisable() {
		logger.debug("Closing SQLite connection.");
		sqlite.closeConnection();
	}
}
