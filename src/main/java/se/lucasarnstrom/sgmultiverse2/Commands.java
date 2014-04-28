package se.lucasarnstrom.sgmultiverse2;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import se.lucasarnstrom.sgmultiverse2.managers.WorldManager;

public class Commands implements CommandExecutor {
	
	private Main plugin;
	
	public Commands(Main p) {
		plugin = p;
	}

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		
		String command = cmd.getName();
		
		switch(command) {
			case "sgjoin": return sgjoin(sender, args);
			case "sglocation": return sglocation(sender, args);
		}
		
		return false;
	}

	private boolean sgjoin(CommandSender sender, String[] args) {
		
		if(!(sender instanceof Player)) {
			sender.sendMessage(ChatColor.RED + "You have to be a player to use this command!");
			return true;
		}
		
		Player p = (Player) sender;
		
		WorldManager wm = plugin.worldManager;
		
		if(wm.allowPlayerJoin(p.getWorld().getName())) {
			wm.addPlayer(p.getWorld().getName(), p);
		}
		
		return true;
	}
	
	private boolean sglocation(CommandSender sender, String[] args) {
		
		if(!(sender instanceof Player)) {
			sender.sendMessage(ChatColor.RED + "You need to be a player to use this command!");
		}
		
		Player p = (Player) sender;
        String worldname = p.getWorld().getName();
		
		if(!plugin.worldManager.isRegistered(worldname)) {
			p.sendMessage(ChatColor.RED + "You are not in a registered gameworld!");
			return true;
		}
		else if(args.length == 0) {
			sender.sendMessage(ChatColor.RED + "You need to provide atleast one argument to the command!");
			return true;
		}
		else if(args.length == 1 && args[0].equalsIgnoreCase("info")) {
			p.sendMessage(" - " + ChatColor.GOLD + "Number of locations for this world" + ChatColor.WHITE + " - ");
			p.sendMessage(" - MAIN  : " + ChatColor.GREEN + plugin.worldManager.getNumberOfMainLocations(worldname));
			p.sendMessage(" - ARENA : " + ChatColor.GREEN + plugin.worldManager.getNumberOfArenaLocations(worldname));
            return true;
		}
        else if(args.length == 1 && args[0].equalsIgnoreCase("save")) {
            p.sendMessage(ChatColor.GREEN + "Saving locations for this world!");
            plugin.worldManager.saveLocations(worldname);
            return true;
        }
        else if(args.length == 2 && args[0].equalsIgnoreCase("add")) {
            if(args[1].equalsIgnoreCase("main")) {
                plugin.worldManager.addMainLocation(worldname, p.getLocation());
            }
            else if(args[1].equalsIgnoreCase("arena")) {
                plugin.worldManager.addArenaLocation(worldname, p.getLocation());
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
