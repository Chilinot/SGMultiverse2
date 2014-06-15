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
 */


package se.lucasarnstrom.sgmultiverse2.managers;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import se.lucasarnstrom.lucasutils.ConsoleLogger;
import se.lucasarnstrom.sgmultiverse2.Main;
import se.lucasarnstrom.sgmultiverse2.libs.IconMenu;

public class Commands implements CommandExecutor {

    private final ConsoleLogger logger = new ConsoleLogger("CommandManager");
    private final Main plugin;

    //TODO Transfer all strings to Language

    public Commands(Main p) {
        plugin = p;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {

        //TODO Use iconmenus for the commands
        //TODO Commands: sgleave, sgstats, sgplayers, sgspectate

        String command = cmd.getName();

        switch(command) {
            case "sginfo":
                return sginfo(sender);
            case "sgjoin":
                return sgjoin(sender, args);
            case "sgqueue":
                return sgqueue(sender);
            case "sglocation":
                return sglocation(sender, args);
            case "sgtp":
                return sgtp(sender, args);
            default:
                logger.severe("Unsupported command \"" + command + "\" recieved!");
        }

        return false;
    }

    private boolean sginfo(CommandSender s) {

        Language msg = Language.COMMAND_SGINFO_INFO_MAIN;
        msg.INFO = plugin.getDescription().getVersion();
        s.sendMessage(msg.getMessage());

        s.sendMessage(Language.COMMAND_SGINFO_INFO_REGISTERED.getMessage());
        for(String name : plugin.worldManager.getRegisteredWorldnames()) {
            msg = Language.COMMAND_SGINFO_INFO_REGISTEREDWORLD;
            msg.INFO = name;
            s.sendMessage(msg.getMessage());
        }

        if(s instanceof Player) {
            Player p = (Player) s;
            s.sendMessage("You are in the world \"" + ChatColor.GOLD + p.getWorld().getName() + ChatColor.WHITE + "\".");
        }

        return true;
    }

    private boolean sgtp(CommandSender sender, String[] args) {

        if(!(sender instanceof Player)) {
            sender.sendMessage(Language.COMMAND_ERROR_NOTAPLAYER.getMessage());
            return true;
        }
        else if(args.length != 1) {
            sender.sendMessage(Language.COMMAND_ERROR_MISSINGARGUMENTS.getMessage());
            return false;
        }

        Player p = (Player) sender;
        String wname = args[0];

        World w = Bukkit.getWorld(wname);

        if(w != null) {
            Language msg = Language.COMMAND_SGTP_INFO_SENDING;
            msg.INFO = wname;
            p.sendMessage(msg.getMessage());
            p.teleport(w.getSpawnLocation());
        }
        else {
            p.sendMessage(Language.COMMAND_SGTP_ERROR_NOTAVALIDWORLD.getMessage());
        }

        return true;
    }

    private boolean sgjoin(CommandSender sender, String[] args) {

        if(!(sender instanceof Player)) {
            sender.sendMessage(Language.COMMAND_ERROR_NOTAPLAYER.getMessage());
            return true;
        }

        final Player p = (Player) sender;

        final WorldManager wm = plugin.worldManager;

        if(args.length == 1) {
            if(wm.isRegistered(args[0])) {
                wm.sendPlayerToLobby(args[0], p);
            }
            else {
                p.sendMessage(Language.COMMAND_SGJOIN_ERROR_NOTAGAME.getMessage());
            }
        }
        else {
            //TODO This might produce one extra empty row if there are n % 9 = 0 amounts of registered worlds.
            int amount = (9 * (wm.getRegisteredWorldnames().length / 9)) + 9;

            IconMenu menu = new IconMenu(Language.COMMAND_SGJOIN_MENU_TITLE.getMessage(), amount, new IconMenu.OptionClickEventHandler() {
                @Override
                public void onOptionClick(IconMenu.OptionClickEvent event) {
                    wm.sendPlayerToLobby(event.getName(), p);
                    event.setWillClose(true);
                    event.setWillDestroy(true);
                }
            }, plugin);

            int i = 0;
            for(String w : wm.getRegisteredWorldnames()) {
                Language msg = Language.COMMAND_SGJOIN_MENU_CHOICE;
                msg.INFO = w;
                menu.setOption(i++, new ItemStack(Material.MAP), w, msg.getMessage());
            }

            menu.open(p);
        }

        return true;
    }

    private boolean sgqueue(CommandSender sender) {
        if(!(sender instanceof Player)) {
            sender.sendMessage(Language.COMMAND_ERROR_NOTAPLAYER.getMessage());
            return true;
        }

        Player p = (Player) sender;
        WorldManager wm = plugin.worldManager;

        if(wm.isInLobby(p)) {
            // The player is told about this in the Lobby object.
            wm.addToQueue(p.getWorld().getName(), p);
        }
        else {
            p.sendMessage(Language.COMMAND_SGQUEUE_ERROR_NOTINLOBBY.getMessage());
        }

        return true;
    }

    private boolean sglocation(CommandSender sender, String[] args) {

        if(!(sender instanceof Player)) {
            sender.sendMessage(Language.COMMAND_ERROR_NOTAPLAYER.getMessage());
        }

        Player p = (Player) sender;
        String worldname = p.getWorld().getName();

        if(!plugin.worldManager.isRegistered(worldname)) {
            p.sendMessage(Language.COMMAND_SGLOCATION_ERROR_NOTINREGISTEREDWORLD.getMessage());
            return true;
        }

        switch(args.length) {

            case 0:
                sender.sendMessage(Language.COMMAND_ERROR_MISSINGARGUMENTS.getMessage());
                return false;

            case 1:
                switch(args[0].toLowerCase()) {

                    case "info":
                        p.sendMessage(" - " + ChatColor.GOLD + "Number of locations for this world" + ChatColor.WHITE + " - ");
                        p.sendMessage(" - MAIN   : " + ChatColor.GREEN + plugin.worldManager.getNumberOfMainLocations(worldname));
                        p.sendMessage(" - ARENA : " + ChatColor.GREEN + plugin.worldManager.getNumberOfArenaLocations(worldname));
                        return true;

                    case "save":
                        p.sendMessage(ChatColor.GREEN + "Saving locations for this world!");
                        plugin.worldManager.saveLocations(worldname);
                        return true;

                    default:
                        // Argument not supported.
                        return false;
                }

            case 2:
                switch(args[0].toLowerCase()) {

                    case "add":
                        switch(args[1].toLowerCase()) {

                            case "main":
                                plugin.worldManager.addMainLocation(worldname, p.getLocation());
                                break; // Send message below

                            case "arena":
                                plugin.worldManager.addArenaLocation(worldname, p.getLocation());
                                break; // Send message below

                            case "lobby":
                                plugin.worldManager.setLobbyLocation(worldname, p.getLocation(), true);
                                p.sendMessage(ChatColor.GREEN + "You have successfully set the lobby location for this world!");
                                return true; // Don't send the message

                            default:
                                // Argument not supported.
                                return false;
                        }

                        // Message the player
                        p.sendMessage(
                                ChatColor.GREEN +
                                        "Added " +
                                        ChatColor.GOLD +
                                        args[1].toUpperCase() +
                                        ChatColor.GREEN +
                                        " location! Remember to save if you want the locations to be permanent!"
                        );

                        return true;

                    default:
                        // Argument not supported.
                        return false;
                }

            default:
                // Unsupported amount of arguments
                return false;
        }
    }
}