/**
 *  Author:  Lucas Arnstr�m - LucasEmanuel @ Bukkit forums
 *  Contact: lucasarnstrom(at)gmail(dot)com
 *
 *
 *  Copyright 2014 Lucas Arnstr�m
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


package se.lucasarnstrom.sgmultiverse2.databases;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.scheduler.BukkitRunnable;
import se.lucasarnstrom.lucasutils.ConsoleLogger;
import se.lucasarnstrom.sgmultiverse2.Main;

import java.sql.*;
import java.util.HashSet;

public class SQLiteInterface {

	public enum LocationType {
		MAIN,
		ARENA
	}

	private Main plugin;
	private final String folder;

	private ConsoleLogger logger = new ConsoleLogger("SQLiteInterface");

	private Connection con;

	// Dummy object for locking purpose
	final private Object lock = new Object();

	public SQLiteInterface(Main instance) {
		plugin = instance;

		folder = plugin.getDataFolder().getAbsolutePath();

		getConnection();
	}

	private void testConnection() {
		synchronized (lock) {
			try {
				con.getCatalog();
			} catch (SQLException e) {
				System.out.println("SQLite-connection no longer valid! Trying to re-establish one...");
				getConnection();
			}
		}
	}

	private void getConnection() {
		synchronized (lock) {
			try {
				Class.forName("org.sqlite.JDBC");
				con = DriverManager.getConnection("jdbc:sqlite:" + folder + "/data.db");

				Statement stmt = con.createStatement();

				stmt.execute(
						"CREATE TABLE IF NOT EXISTS signlocations  (" +
								"worldname    VARCHAR(255) NOT NULL PRIMARY KEY, " +
								"placed_world VARCHAR(255) NOT NULL, " +
								"x            DOUBLE(255)  NOT NULL, " +
								"y            DOUBLE(255)  NOT NULL, " +
								"z            DOUBLE(255)  NOT NULL" +
								")"
				);

				stmt.execute(
						"CREATE TABLE IF NOT EXISTS startlocations (" +
								"worldname VARCHAR(255) NOT NULL, " +
								"x         DOUBLE(255)  NOT NULL, " +
								"y         DOUBLE(255)  NOT NULL, " +
								"z         DOUBLE(255)  NOT NULL, " +
								"type      VARCHAR(10)  NOT NULL" +
								")"
				);

				stmt.execute(
						"CREATE TABLE IF NOT EXISTS playerstats (" +
								"UUID   STRING(250) NOT NULL PRIMARY KEY, " +
								"wins   INT(10), " +
								"kills  INT(10), " +
								"deaths INT(10)" +
								")"
				);

				stmt.execute(
						"CREATE TABLE IF NOT EXISTS inventories (" +
								"UUID      VARCHAR(250)  NOT NULL PRIMARY KEY, " +
								"inventory VARCHAR(8000) NOT NULL" +
								")"
				);

				stmt.close();
			} catch (ClassNotFoundException | SQLException e) {
				logger.severe("WARNING! SEVERE ERROR! Could not connect to SQLite-database in plugin-datafolder! This means it cannot load/store important data!");
				logger.severe("Error message: " + e.getMessage());
			}
		}
	}

	public void closeConnection() {
		synchronized (lock) {
			try {
				con.close();
			} catch (SQLException e) {
				logger.severe("Error while closing connection, data might have been lost! " +
						"Message: " + e.getMessage());
			}
		}
	}

	public void loadLocations(final String worldname) {

		logger.debug("Loading locations for world \"" + worldname + "\"!");

		final HashSet[] locations = {
				new HashSet<double[]>(), // Main
				new HashSet<double[]>()  // Arena
		};

		final String select =
				"SELECT * " +
						"FROM startlocations " +
						"WHERE worldname = ? " +
						"AND type = ?";

		synchronized (lock) {
			try {
				testConnection();

				PreparedStatement stmt_main = con.prepareStatement(select);
				PreparedStatement stmt_arena = con.prepareStatement(select);

				stmt_main.setString(1, worldname);
				stmt_main.setString(2, LocationType.MAIN.toString());
				ResultSet rs_main = stmt_main.executeQuery();

				stmt_arena.setString(1, worldname);
				stmt_arena.setString(2, LocationType.ARENA.toString());
				ResultSet rs_arena = stmt_arena.executeQuery();

				HashSet<double[]> main = new HashSet<>();
				HashSet<double[]> arena = new HashSet<>();

				while (rs_main.next()) {
					double[] a = new double[3];
					a[0] = rs_main.getDouble(2);
					a[1] = rs_main.getDouble(3);
					a[2] = rs_main.getDouble(4);
					main.add(a);
				}

				while (rs_arena.next()) {
					double[] a = new double[3];
					a[0] = rs_arena.getDouble(2);
					a[1] = rs_arena.getDouble(3);
					a[2] = rs_arena.getDouble(4);
					arena.add(a);
				}

				locations[0] = main;
				locations[1] = arena;

				rs_main.close();
				rs_arena.close();
				stmt_main.close();
				stmt_arena.close();
			} catch (SQLException e) {
				logger.severe("Error while retrieving startlocations for world \"" + worldname + "\". " +
						"Message: " + e.getMessage());
				return;
			}
		}

		new BukkitRunnable() {
			@Override
			public void run() {
				for (double[] a : (HashSet<double[]>) locations[0]) {
					plugin.worldManager.addMainLocation(worldname, new Location(Bukkit.getWorld(worldname), a[0], a[1], a[2]));
				}
				for (double[] a : (HashSet<double[]>) locations[1]) {
					plugin.worldManager.addArenaLocation(worldname, new Location(Bukkit.getWorld(worldname), a[0], a[1], a[2]));
				}
			}
		}.runTask(plugin);
	}

	public void storeStartLocations(String worldname, LocationType type, Location[] locations) {
		synchronized (lock) {
			testConnection();

			String insert_s =
					"INSERT INTO startlocations " +
							"VALUES(?,?,?,?,?)";

			try {
				PreparedStatement stmt = con.prepareStatement(insert_s);

				for (Location l : locations) {
					stmt.setString(1, worldname);
					stmt.setDouble(2, l.getX());
					stmt.setDouble(3, l.getY());
					stmt.setDouble(4, l.getZ());
					stmt.setString(5, type.toString());
					stmt.addBatch();
				}

				stmt.executeBatch();
				stmt.close();
			} catch (SQLException e) {
				logger.severe("Error while saving startlocations for world " + worldname + ". " +
						"Message: " + e.getMessage());
			}
		}
	}

	public void clearStartLocations(String worldname, LocationType type) {
		synchronized (lock) {
			testConnection();

			String delete_s =
					"DELETE FROM startlocations " +
							"WHERE worldname = ? " +
							"AND type = ?";
			try {
				PreparedStatement stmt = con.prepareStatement(delete_s);
				stmt.setString(1, worldname);
				stmt.setString(2, type.toString());
				stmt.execute();
				stmt.close();
			} catch (SQLException e) {
				logger.severe("Error while clearing startlocations! " +
						"Message: " + e.getMessage());
			}
		}
	}

	public void loadSignLocation(final String wname) {

		logger.debug("Loading info-sign for world \"" + wname + "\"!");

		final String load_s =
				"SELECT placed_world, x, y, z " +
						"FROM signlocations " +
						"WHERE worldname = ? ";

		synchronized (lock) {
			testConnection();

			try {
				PreparedStatement stmt = con.prepareStatement(load_s);
				stmt.setString(1, wname);

				ResultSet rs = stmt.executeQuery();
				if (rs.next()) {
					final String w = rs.getString(1);
					final double x = rs.getDouble(2);
					final double y = rs.getDouble(3);
					final double z = rs.getDouble(4);

					new BukkitRunnable() {
						@Override
						public void run() {
							plugin.worldManager.setSignLocation(wname, new Location(Bukkit.getWorld(w), x, y, z));
						}
					}.runTask(plugin);
				} else {
					logger.warning("No info-sign saved for world \"" + wname + "\"!");
				}

				rs.close();
				stmt.close();
			} catch (SQLException e) {
				logger.severe("Error while loading signlocation for world \"" + wname + "\"! " +
						"Message: " + e.getMessage());
			}
		}
	}

	public void storeSignLocation(final String wname, final Location l) {

		logger.debug("Storing sign for world \"" + wname + "\"!");

		final String store_s =
				"INSERT OR REPLACE INTO signlocations " +
						"VALUES(?,?,?,?,?)";

		synchronized (lock) {
			testConnection();

			try {
				PreparedStatement stmt = con.prepareStatement(store_s);
				stmt.setString(1, wname);
				stmt.setString(2, l.getWorld().getName());
				stmt.setDouble(3, l.getX());
				stmt.setDouble(4, l.getY());
				stmt.setDouble(5, l.getZ());
				stmt.execute();
				stmt.close();
			} catch (SQLException e) {
				logger.severe("Error while storing signlocation for world \"" + wname + "\"! " +
						"Message: " + e.getMessage());
			}
		}
	}
}