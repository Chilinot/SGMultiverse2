package se.lucasarnstrom.sgmultiverse2;

import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;

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

    Language(String r) {
        msg = r;
    }

    public static void setConfig(File file) {
        config = YamlConfiguration.loadConfiguration(file);
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
        if (player != null) {
            msg = msg.replace("#player#", player);
        }
        if (killer != null) {
            msg = msg.replace("#killer#", killer);
        }

        return ChatColor.translateAlternateColorCodes('&', msg);
    }
}