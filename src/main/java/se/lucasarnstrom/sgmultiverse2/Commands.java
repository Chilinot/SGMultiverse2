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

}
