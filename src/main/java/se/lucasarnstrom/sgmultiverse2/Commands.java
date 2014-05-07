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
import org.bukkit.ChatColor;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import se.lucasarnstrom.sgmultiverse2.managers.WorldManager;

public class Commands implements CommandExecutor {

	private final Main plugin;

	public Commands(Main p) {
		plugin = p;
	}

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {

		//TODO Use iconmenus for the commands
		//TODO Commands: sgleave, sgstats

		String command = cmd.getName();

		switch (command) {
			case "sginfo":
				return sginfo(sender);
			case "sgjoin":
				return sgjoin(sender, args);
			case "sglocation":
				return sglocation(sender, args);
			case "sgtp":
				return sgtp(sender, args);
		}

		return false;
	}

	private boolean sginfo(CommandSender s) {
		s.sendMessage(ChatColor.GREEN + "SGMultiverse2 version " + plugin.getDescription().getVersion() + " is up and running!");

		s.sendMessage("Currently these worlds are registered as gameworlds:");
		for (String name : plugin.worldManager.getRegisteredWorldnames()) {
			s.sendMessage(" - " + ChatColor.GOLD + name);
		}

		if (s instanceof Player) {
			Player p = (Player) s;
			s.sendMessage("You are in the world \"" + ChatColor.GOLD + p.getWorld().getName() + ChatColor.WHITE + "\".");
		}

		return true;
	}

	private boolean sgtp(CommandSender sender, String[] args) {

		if (!(sender instanceof Player)) {
			sender.sendMessage(ChatColor.RED + "You have to be a player to use this command!");
			return true;
		} else if (args.length != 1) {
			sender.sendMessage(ChatColor.RED + "You have to specify the name of the world you want to go to!");
			return false;
		}

		Player p = (Player) sender;
		String wname = args[0];

		World w = Bukkit.getWorld(wname);

		if (w != null) {
			p.sendMessage(ChatColor.GREEN + "Sending you to \"" + wname + "\"!");
			p.teleport(w.getSpawnLocation());
		} else {
			p.sendMessage(ChatColor.RED + "There is no world with that name on this server!");
		}

		return true;
	}

	private boolean sgjoin(CommandSender sender, String[] args) {

		if (!(sender instanceof Player)) {
			sender.sendMessage(ChatColor.RED + "You have to be a player to use this command!");
			return true;
		} else if (args.length != 1) {
			return false;
		}

		Player p = (Player) sender;

		WorldManager wm = plugin.worldManager;

		if (wm.isRegistered(args[0])) {
			if (wm.allowPlayerJoin(args[0])) {
				wm.addPlayer(args[0], p);
			} else {
				p.sendMessage(ChatColor.RED + "That game is full!");
			}
		} else {
			p.sendMessage(ChatColor.RED + "There is no game with that name!");
		}

		return true;
	}

	private boolean sglocation(CommandSender sender, String[] args) {

		if (!(sender instanceof Player)) {
			sender.sendMessage(ChatColor.RED + "You need to be a player to use this command!");
		}

		Player p = (Player) sender;
		String worldname = p.getWorld().getName();

		if (!plugin.worldManager.isRegistered(worldname)) {
			p.sendMessage(ChatColor.RED + "You are not in a registered gameworld!");
			return true;
		}
		else if (args.length == 0) {
			sender.sendMessage(ChatColor.RED + "You need to provide at least one argument to the command!");
			return false;
		}
		else if (args.length == 1 && args[0].equalsIgnoreCase("info")) {
			p.sendMessage(" - " + ChatColor.GOLD + "Number of locations for this world" + ChatColor.WHITE + " - ");
			p.sendMessage(" - MAIN   : " + ChatColor.GREEN + plugin.worldManager.getNumberOfMainLocations(worldname));
			p.sendMessage(" - ARENA : " + ChatColor.GREEN + plugin.worldManager.getNumberOfArenaLocations(worldname));
			return true;
		}
		else if (args.length == 1 && args[0].equalsIgnoreCase("save")) {
			p.sendMessage(ChatColor.GREEN + "Saving locations for this world!");
			plugin.worldManager.saveLocations(worldname);
			return true;
		}
		else if (args.length == 2 && args[0].equalsIgnoreCase("add")) {

			if (args[1].equalsIgnoreCase("main")) {
				plugin.worldManager.addMainLocation(worldname, p.getLocation());
			}
			else if (args[1].equalsIgnoreCase("arena")) {
				plugin.worldManager.addArenaLocation(worldname, p.getLocation());
			}
			else if(args[1].equalsIgnoreCase("lobby")) {
				plugin.worldManager.setLobbyLocation(worldname, p.getLocation(), true);
			}
			else {
				return false;
			}

			p.sendMessage(ChatColor.GREEN + "Added " + ChatColor.GOLD + args[1].toUpperCase() + ChatColor.GREEN + " location! " +
					"Remember to save if you want the locations to be permanent!");

			return true;
		}

		return false;
	}
}
