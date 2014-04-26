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
		
		if(!plugin.worldManager.isRegistered(p.getWorld().getName())) {
			p.sendMessage(ChatColor.RED + "You are not in a registered gameworld!");
			return true;
		}
		else if(args.length == 0) {
			sender.sendMessage(ChatColor.RED + "You need to provide atleast one argument to the command!");
			return true;
		}
		else if(args.length == 1 && args[0].equalsIgnoreCase("info")) {
			p.sendMessage(" - " + ChatColor.GOLD + "Number of locations for this world" + ChatColor.WHITE + " - ");
			p.sendMessage(" - MAIN  : " + ChatColor.GREEN + plugin.worldManager.getNumberOfMainLocations(p.getWorld().getName()));
			p.sendMessage(" - ARENA : " + ChatColor.GREEN + plugin.worldManager.getNumberOfArenaLocations(p.getWorld().getName()));
		}
		
		return true;
	}
}
