package se.lucasarnstrom.sgmultiverse2;

import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import se.lucasarnstrom.lucasutils.ConsoleLogger;

import java.io.File;
import java.io.IOException;

public enum Language {

	PLAYER_KICK     ("&6#player#&f has been kicked from the game!"),
	PLAYER_QUIT     ("&6#player#&f left the game."),
	PLAYER_TELEPORT ("&6#player#&f teleported out of this world and was removed!"),
	PLAYER_DEATH    ("&6#player#&f was killed."),
	PLAYER_KILLED   ("&6#player#&f was killed by &6#killer#&f!");

	private String msg;
	private String player = null;
	private String killer = null;

	private static FileConfiguration config = null;
	private static ConsoleLogger logger = new ConsoleLogger("Language");

	private Language(String r) {
		msg = r;
	}

	public static void setConfig(File file) {

		config = YamlConfiguration.loadConfiguration(file);

		// Store the defaults in the config.
		boolean save = false;

		for(Language l : Language.values()) {
			if(!config.contains(l.name())) {
				config.set(l.name(), l.msg);
				save = true;
			}
		}

		if(save) {
			try {
				config.save(file);
			} catch (IOException e) {
				logger.severe("Error while trying to save defaults to the config!");
				e.printStackTrace();
			}
		}
	}

	public void setPlayer(String s) {
		player = s;
	}

	public void setKiller(String s) {
		killer = s;
	}

	public String getMessage() {

		if (config != null) {
			msg = config.getString(this.name(), msg);
		}

		// Variables
		if (player != null) {
			msg = msg.replace("#player#", player);
		}
		if (killer != null) {
			msg = msg.replace("#killer#", killer);
		}

		return ChatColor.translateAlternateColorCodes('&', msg);
	}
}