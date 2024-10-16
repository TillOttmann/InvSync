package com.plugin.invsync;

import java.io.File;
import java.sql.Connection;

import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

// Sowohl die Main als auch die DatabaseConnection Klasse sind größtenteils aus TimeLimit
// https://github.com/TillOttmann/TimeLimit (Stand 12.10.24)

public class InvSyncMain extends JavaPlugin {
	private static InvSyncMain instance;
	private static Connection conn;
	private DatabaseConnection DBInstance;

	// Wird aufgerufen, wenn das Plugin aktiviert wird
	@Override
	public void onEnable() {
		instance = this;
		
		if (configFileExists()) {
			sendConsoleMessage("default", "config.yml vorhanden");
			// DatabaseConnection-Object, wird gespeichert um später die Datenbankverbindung bei onDisable()
			// wieder zu schließen. Die eigentliche Verbindung wird in conn gespeichert
			DBInstance = new DatabaseConnection();
			conn = DBInstance.getDatabaseConnection();
			
			if (conn == null) {
				sendConsoleMessage("error", "Datenbankverbindung konnte nicht aufgebaut werden");
				instance.getServer().getPluginManager().disablePlugin(instance);
			} else {
				getServer().getPluginManager().registerEvents(new PlayerListener(), instance);
			}
		} else {
			sendConsoleMessage("warning", "config.yml existiert nicht, wird erstellt");
			// Default config.yml wird erstellt falls nicht vorhanden
			instance.saveDefaultConfig();
			sendConsoleMessage("warning", "Bitte die Datenbank in der config.yml angeben, sonst kann das Plugin nicht funktionieren!");
			instance.getServer().getPluginManager().disablePlugin(instance);
		}
	}

	// Wird aufgerufen, wenn das Plugin deaktiviert wird
	@Override
	public void onDisable() {
		if (DBInstance != null) {
			DBInstance.closeDatabaseConnection();
		}
		sendConsoleMessage("default", "Plugin wird deaktiviert");
	}
	
	// Instance-Getter
	protected static InvSyncMain getInstance() {
		return instance;
	}
	
	// Getter-Funktion: Erlaubt den Zugriff auf die config.yml Datei
	protected static FileConfiguration getFileConfig() {
		return instance.getConfig();
	}

	// Getter-Funktion für die Datenbank, alle Klassen verwenden eine geteilte Verbindung
	protected static Connection getDatabaseConnection() {
		return conn;
	}

	// Sendet eine Nachricht an die Server-Console
	protected static void sendConsoleMessage(String type,String message) {
		org.bukkit.ChatColor color;
		if (type.equals("error")) {
			color = org.bukkit.ChatColor.RED;
		} else 	if (type.equals("warning")) {
			color = org.bukkit.ChatColor.YELLOW;
		} else {
			color = org.bukkit.ChatColor.GREEN;
		}
		Bukkit.getServer().getConsoleSender().sendMessage(color + "[InvSync] " + message);
	}
	
	// Checkt, ob die config.yml Datei existiert
    private boolean configFileExists() {
        File configFile = new File(getDataFolder(), "config.yml");
        return configFile.exists();
    }
}
