/**
 *  Name:    ConcurrentSQLiteConnection.java
 *  Created: 17:37:33 - 8 maj 2013
 * 
 *  Author:  Lucas Arnström - LucasEmanuel @ Bukkit forums
 *  Contact: lucasarnstrom(at)gmail(dot)com
 *  
 *
 *  Copyright 2013 Lucas Arnström
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
import org.bukkit.World;
import se.lucasarnstrom.sgmultiverse2.Main;

import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

public class SQLiteInterface {

    enum LocationType {
        MAIN,
        ARENA
    }
	
	private Main plugin;
	private final String folder;
	
	private Connection con;
	
	// Dummy object for locking purpose
	private Object lock = new Object();
	
	public SQLiteInterface(Main instance) {
		plugin = instance;
		
		folder = plugin.getDataFolder().getAbsolutePath();
		
		getConnection();
	}
	
	private void testConnection() {
		synchronized(lock) {
			try {
				con.getCatalog();
			}
			catch(SQLException e) {
				System.out.println("SQLite-connection no longer valid! Trying to re-establish one...");
				getConnection();
			}
		}
	}
	
	private void getConnection() {
		synchronized(lock) {
			try {
				Class.forName("org.sqlite.JDBC");
				con = DriverManager.getConnection("jdbc:sqlite:" + folder + "/data.db");
				
				Statement stmt = con.createStatement();
				
				stmt.execute("CREATE TABLE IF NOT EXISTS signlocations  (worldname VARCHAR(255) NOT NULL, x DOUBLE(255) NOT NULL, y DOUBLE(255) NOT NULL, z DOUBLE(255) NOT NULL)");
				stmt.execute("CREATE TABLE IF NOT EXISTS startlocations (worldname VARCHAR(255) NOT NULL, x DOUBLE(255) NOT NULL, y DOUBLE(255) NOT NULL, z DOUBLE(255) NOT NULL, type VARCHAR(10) NOT NULL)");
				stmt.execute("CREATE TABLE IF NOT EXISTS playerstats (playername VARHCAR(250) NOT NULL PRIMARY KEY, wins INT(10), kills INT(10), deaths INT(10))");
				stmt.execute("CREATE TABLE IF NOT EXISTS inventories (playername VARCHAR(250) NOT NULL PRIMARY KEY, inventory VARCHAR(8000) NOT NULL)");
				
				stmt.close();
			}
			catch(ClassNotFoundException | SQLException e) {
				System.out.println("WARNING! SEVERE ERROR! Could not connect to SQLite-database in plugin-datafolder! This means it cannot load/store important data!");
				System.out.println("Error message: " + e.getMessage());
			}
		}
	}

	public void closeConnection() {
		synchronized(lock) {
			try {
				con.close();
			}
			catch (SQLException e) {
				System.out.println("Error while closing connection, data might have been lost! " +
						"Message: " + e.getMessage());
			}
		}
	}
	
	public ArrayList<HashSet<Location>> getStartLocations(String worldname) {
		synchronized(lock) {
			
			String select = "SELECT * " +
							"FROM startlocations " +
							"WHERE worldname = ? " +
							"AND type = ?";
			try {
				testConnection();
				
				ArrayList<HashSet<Location>> locations = new ArrayList<>();
				
				PreparedStatement stmt_main  = con.prepareStatement(select);
				PreparedStatement stmt_arena = con.prepareStatement(select);
				
				stmt_main.setString(1, worldname);
                stmt_main.setString(2, "main");
				ResultSet rs_main = stmt_main.executeQuery();
				
				stmt_arena.setString(1, worldname);
                stmt_arena.setString(2, "arena");
				ResultSet rs_arena = stmt_arena.executeQuery();
				
				HashSet<Location> main  = new HashSet<>();
				HashSet<Location> arena = new HashSet<>();

                World w = Bukkit.getWorld(worldname);   // It doesn't matter that it is not thread-safe.

                while(rs_main.next()) {
                    double x = rs_main.getDouble(2);
                    double y = rs_main.getDouble(3);
                    double z = rs_main.getDouble(4);
					main.add(new Location(w, x, y, z));
				}
				
				while(rs_arena.next()) {
                    double x = rs_main.getDouble(2);
                    double y = rs_main.getDouble(3);
                    double z = rs_main.getDouble(4);
                    arena.add(new Location(w, x, y, z));
				}
				
				locations.add(main);
				locations.add(arena);
				
				rs_main.close();
				rs_arena.close();
				stmt_main.close();
				stmt_arena.close();
				
				return locations;
			}
			catch(SQLException e) {
				System.out.println("Error while retrieving startlocations for world " + worldname + ". " +
						"Message: " + e.getMessage());
				return null;
			}
		}
	}
	
	public void saveStartLocations(String worldname, LocationType type, Set<Location> locations) {
		synchronized(lock) {
			testConnection();
			
			String insert_s = "INSERT OR REPLACE INTO startlocations " +
							  "VALUES(?,?,?,?,?)";

            String t = null;
            switch(type) {
                case MAIN:
                    t = "main";
                    break;
                case ARENA:
                    t = "arena";
                    break;
            }
			
			try {
				PreparedStatement stmt = con.prepareStatement(insert_s);
				
				for(Location l : locations) {
					stmt.setString(1, worldname);
                    stmt.setDouble(2, l.getX());
                    stmt.setDouble(3, l.getY());
                    stmt.setDouble(4, l.getZ());
					stmt.setString(5, t);
					stmt.addBatch();
				}
				
				stmt.executeBatch();
				stmt.close();
			}
			catch(SQLException e) {
				System.out.println("Error while saving startlocations for world " + worldname + ". " +
						"Message: " + e.getMessage());
			}
		}
	}
	
	public void clearStartLocations(String worldname, String type) {
		synchronized(lock) {
			testConnection();
			
			String delete_s = "DELETE FROM startlocations " +
							  "WHERE worldname = ? " +
							  "AND type = ?";
			try {
				PreparedStatement stmt = con.prepareStatement(delete_s);
				stmt.setString(1, worldname);
				stmt.setString(2, type);
				stmt.execute();
				stmt.close();
			}
			catch (SQLException e) {
				System.out.println("Error while clearing startlocations! " +
						"Message: " + e.getMessage());
			}
		}
	}
	
	public HashMap<String, String> getSignlocations() {
		synchronized(lock) {
			try {
				HashMap<String, String> map = new HashMap<String, String>();
				
				testConnection();
				
				Statement stmt = con.createStatement();
				ResultSet rs = stmt.executeQuery("SELECT * FROM signlocations");
				
				while(rs.next()) {
					
					String serial    = rs.getString("serial_position");
					String worldname = rs.getString("worldname");
					
					map.put(serial, worldname);
				}
				
				rs.close();
				stmt.close();
				
				return map;
			}
			catch (SQLException e) {
				System.out.println("Error while retrieving saved signlocations! Message: " + e.getMessage());
				return null;
			}
		}
	}
	
	public void saveSignLocations(HashMap<SerializedLocation, String> locations) {
		synchronized(lock) {
			testConnection();
			
			String insert_s = "INSERT OR REPLACE INTO signlocations " +
							  "VALUES( ? , ? )";
			
			try {
				PreparedStatement stmt = con.prepareStatement(insert_s);
				
				for(Entry<SerializedLocation, String> entry : locations.entrySet()) {
					stmt.setString(1, entry.getKey().toString());
					stmt.setString(2, entry.getValue());
					stmt.addBatch();
				}
				
				stmt.executeBatch();
				stmt.close();
			}
			catch (SQLException e) {
				System.out.println("Error while saving signs! Message: " + e.getMessage());
			}
		}
	}
	
	public void removeSign(SerializedLocation l) {
		synchronized(lock) {
			testConnection();
			
			String delete = "DELETE FROM signlocations " +
							"WHERE serial_position = ?";
			
			try {
				PreparedStatement stmt = con.prepareStatement(delete);
				stmt.setString(1, l.toString());
				stmt.execute();
				stmt.close();
			}
			catch (SQLException e) {
				System.out.println("Error while removing sign! Message: " + e.getMessage());
			}
		}
	}
	
	public void loadPlayerStats(final String playername) {
		
		final int[] stats = new int[3];
		
		synchronized(lock) {
			testConnection();
			
			String select = "SELECT * " +
							"FROM playerstats " +
							"WHERE playername = ?";
			try {
				PreparedStatement stmt = con.prepareStatement(select);
				
				stmt.setString(1, playername);
				
				ResultSet rs = stmt.executeQuery();
				
				if(rs.next()) {
					stats[0] = rs.getInt("wins");
					stats[1] = rs.getInt("kills");
					stats[2] = rs.getInt("deaths");
				}
				
				rs.close();
				stmt.close();
			}
			catch (SQLException e) {
				System.out.println("Error while loading stats for player= " + playername + "! " +
						"Message: " + e.getMessage());
			}
		}
		
		plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {
			public void run() {
				plugin.statsManager.addWinPoints(playername, stats[0], false);
				plugin.statsManager.addKillPoints(playername, stats[1], false);
				plugin.statsManager.addDeathPoints(playername, stats[2], false);
			}
		});
	}

	public void addScore(String playername, int wins, int kills, int deaths) {
		synchronized(lock) {
			testConnection();
			
			String select_s = "SELECT * FROM playerstats WHERE playername = ?";
			
			String update_s = "UPDATE playerstats " +
							  "SET wins = wins + ? , kills = kills + ? , deaths = deaths + ? " +
							  "WHERE playername = ?";
			
			String insert_s = "INSERT INTO playerstats " +
							  "VALUES( ? , ? , ? , ? )";
			
			PreparedStatement select = null;
			PreparedStatement update = null;
			PreparedStatement insert = null;
			
			try {
				select = con.prepareStatement(select_s);
				select.setString(1, playername);
				
				ResultSet rs = select.executeQuery();
				
				if(rs.next()) {
					update = con.prepareStatement(update_s);
					update.setInt(1, wins);
					update.setInt(2, kills);
					update.setInt(3, deaths);
					update.setString(4, playername);
					update.execute();
					update.close();
				}
				else {
					insert = con.prepareStatement(insert_s);
					insert.setString(1, playername);
					insert.setInt(2, wins);
					insert.setInt(3, kills);
					insert.setInt(4, deaths);
					insert.execute();
					insert.close();
				}
				
				rs.close();
				select.close();
			}
			catch (SQLException e) {
				System.out.println("Error while saving stats for player= " + playername + "! " +
								   "Message: " + e.getMessage());
			}
		}
	}

	public void saveInventory(String playername, String serial) {
		synchronized(lock) {
			testConnection();
			
			String insert_s = "INSERT OR REPLACE INTO inventories " +
							  "VALUES( ? , ? )";
			try {
				PreparedStatement stmt = con.prepareStatement(insert_s);
				stmt.setString(1, playername);
				stmt.setString(2, serial);
				stmt.execute();
				stmt.close();
			}
			catch (SQLException e) {
				System.out.println("Error while saving inventory for player=" + playername + "! " +
								   "Message: " + e.getMessage());
			}
		}
	}
	
	public String loadInventory(String playername) {
		synchronized(lock) {
			testConnection();
			
			String select_s = "SELECT inventory FROM inventories WHERE playername = ? ";
			String delete_s = "DELETE FROM inventories WHERE playername = ? ";
			
			try {
				String serial = null;
				
				PreparedStatement select = con.prepareStatement(select_s);
				select.setString(1, playername);
				
				ResultSet rs = select.executeQuery();
				if(rs.next()) {
					serial = rs.getString("inventory");
					PreparedStatement delete = con.prepareStatement(delete_s);
					delete.setString(1, playername);
					delete.execute();
					delete.close();
				}
				
				rs.close();
				select.close();
				
				return serial;
			}
			catch (SQLException e) {
				System.out.println("Error while loading inventory for player=" + playername + "! " +
								   "Message: " + e.getMessage());
			}
		}
		
		return null;
	}
}
