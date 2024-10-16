package com.plugin.invsync;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

import org.bukkit.configuration.file.FileConfiguration;

class DatabaseConnection {

	private Connection connection;

	// Konstruktor, der die angegebenen Daten aus der config.yml benutzt, um die
	// Datenbankverbindung aufzubauen
	protected DatabaseConnection() {

		FileConfiguration config = InvSyncMain.getFileConfig();

		try {
			String DBName = config.getString("DB_Name");
			String DBUrl = "jdbc:mysql://" + config.getString("DB_Url");
			String DBUser = config.getString("DB_User");
			String DBPw = config.getString("DB_Pw");
			connection = DriverManager.getConnection(DBUrl, DBUser, DBPw);

			InvSyncMain.sendConsoleMessage("default", "Datenbankverbindung erfolgreich aufgebaut");
			configureDatabase(DBName);

		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	// Gibt die Datenbankverbindung als Connection Objekt weiter
	protected Connection getDatabaseConnection() {
		return connection;
	}

	private void configureDatabase(String DBName) {

		try {
			Statement stmt = connection.createStatement();

			// Erstellt die Datenbank, falls diese nicht existiert
			// Der Name sollte, falls mehrere Synchronisierungen über denselben Data Server laufen,
			// in der config.yml angegeben sein und sich von den Namen der anderen Synchronisierungen
			// unterscheiden
			String createDatabaseIfNotExists = "CREATE DATABASE IF NOT EXISTS " + DBName;
			stmt.executeUpdate(createDatabaseIfNotExists);

			if (stmt.getWarnings() != null) {
				InvSyncMain.sendConsoleMessage("default", "Datenbank '" + DBName + "' existiert bereits, Erstellung wird übersprungen");

			} else {
				InvSyncMain.sendConsoleMessage("warning", "Datenbank '" + DBName + "' erfolgreich erstellt");
			}

			// Wählt die Datenbank aus
			stmt.execute("USE " + DBName);
			stmt.clearWarnings();
			
			// Inventare
			String createPlayerInventoryTableIfNotExists = "CREATE TABLE IF NOT EXISTS playerInventories "
				+ "(uuid VARCHAR(36) PRIMARY KEY, inventory MEDIUMTEXT)";
			stmt.executeUpdate(createPlayerInventoryTableIfNotExists);

			if (stmt.getWarnings() != null) {
				InvSyncMain.sendConsoleMessage("default", "Tabelle 'playerInventories' existiert bereits, Erstellung wird übersprungen");

			} else {
				InvSyncMain.sendConsoleMessage("warning", "Tabelle 'playerInventories' erfolgreich erstellt");
			}
			stmt.clearWarnings();
			
			// Advancements
			String createPlayerAdvancementsTableIfNotExists = "CREATE TABLE IF NOT EXISTS playerAdvancements "
				+ "(uuid VARCHAR(36) PRIMARY KEY, advancements MEDIUMTEXT)";
			stmt.executeUpdate(createPlayerAdvancementsTableIfNotExists);

			if (stmt.getWarnings() != null) {
				InvSyncMain.sendConsoleMessage("default", "Tabelle 'playerAdvancements' existiert bereits, Erstellung wird übersprungen");

			} else {
				InvSyncMain.sendConsoleMessage("warning", "Tabelle 'playerAdvancements' erfolgreich erstellt");
			}
			stmt.clearWarnings();
			
			// Exp-Level
			String createPlayerLevelsTableIfNotExists = "CREATE TABLE IF NOT EXISTS playerLevels "
				+ "(uuid VARCHAR(36) PRIMARY KEY, level INT)";
			stmt.executeUpdate(createPlayerLevelsTableIfNotExists);

			if (stmt.getWarnings() != null) {
				InvSyncMain.sendConsoleMessage("default", "Tabelle 'playerLevels' existiert bereits, Erstellung wird übersprungen");

			} else {
				InvSyncMain.sendConsoleMessage("warning", "Tabelle 'playerLevels' erfolgreich erstellt");
			}
			stmt.clearWarnings();
			
			// HP (Leben)
			String createPlayerHealthValuesTableIfNotExists = "CREATE TABLE IF NOT EXISTS playerHealthValues "
				+ "(uuid VARCHAR(36) PRIMARY KEY, health DOUBLE)";
			stmt.executeUpdate(createPlayerHealthValuesTableIfNotExists);

			if (stmt.getWarnings() != null) {
				InvSyncMain.sendConsoleMessage("default", "Tabelle 'playerHealthValues' existiert bereits, Erstellung wird übersprungen");

			} else {
				InvSyncMain.sendConsoleMessage("warning", "Tabelle 'playerHealthValues' erfolgreich erstellt");
			}
			stmt.clearWarnings();
			
			// Effekte
			String createPlayerEffectssTableIfNotExists = "CREATE TABLE IF NOT EXISTS playerEffects "
				+ "(uuid VARCHAR(36) PRIMARY KEY, effects MEDIUMTEXT)";
			stmt.executeUpdate(createPlayerEffectssTableIfNotExists);

			if (stmt.getWarnings() != null) {
				InvSyncMain.sendConsoleMessage("default", "Tabelle 'playerEffects' existiert bereits, Erstellung wird übersprungen");

			} else {
				InvSyncMain.sendConsoleMessage("warning", "Tabelle 'playerEffects' erfolgreich erstellt");
			}
			stmt.clearWarnings();
			
			// Effekte
			String createPlayerEnderChestsTableIfNotExists = "CREATE TABLE IF NOT EXISTS playerEnderChests "
				+ "(uuid VARCHAR(36) PRIMARY KEY, enderchest MEDIUMTEXT)";
			stmt.executeUpdate(createPlayerEnderChestsTableIfNotExists);

			if (stmt.getWarnings() != null) {
				InvSyncMain.sendConsoleMessage("default", "Tabelle 'playerEnderChests' existiert bereits, Erstellung wird übersprungen");

			} else {
				InvSyncMain.sendConsoleMessage("warning", "Tabelle 'playerEnderChests' erfolgreich erstellt");
			}
			stmt.clearWarnings();

			// Schliesst das Statement
			stmt.close();

		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	// Schliesst die Datenbankverbindung, wird durch onDisable() in InvSyncMain
	// aufgerufen
	protected void closeDatabaseConnection() {
		
		if (connection != null) {
			try {
				connection.close();
				InvSyncMain.sendConsoleMessage("default", "Datenbankverbindung erfolgreich geschlossen");

			} catch (SQLException e) {
				e.printStackTrace();

			}
		}
	}
}