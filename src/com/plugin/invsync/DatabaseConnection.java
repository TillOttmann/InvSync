package com.plugin.invsync;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

import org.bukkit.configuration.file.FileConfiguration;

class DatabaseConnection {

	private Connection conn;
	private String DBName;

	// Konstruktor, der die angegebenen Daten aus der config.yml benutzt, um die
	// Datenbankverbindung aufzubauen
	protected DatabaseConnection() {

		FileConfiguration config = InvSyncMain.getFileConfig();
		Connection connection = null;
		try {
			DBName = config.getString("DB_Name");
			String DBUrl = "jdbc:mysql://" + config.getString("DB_Url");
			String DBUser = config.getString("DB_User");
			String DBPw = config.getString("DB_Pw");
			connection = DriverManager.getConnection(DBUrl, DBUser, DBPw);

			InvSyncMain.sendConsoleMessage("default", "SQL-Serververbindung erfolgreich aufgebaut");

		} catch (SQLException e) {
			e.printStackTrace();
		}
		
		configureDatabase(connection);
		
		try { 
			connection.close();
			InvSyncMain.sendConsoleMessage("default", "SQL-Serververbindung erfolgreich geschlossen");
		} catch (SQLException e) {
			e.printStackTrace();
			InvSyncMain.sendConsoleMessage("error", "Fehler beim schließen der SQL-Serververbindung");
		}
		
		try {
			String DBUrl = "jdbc:mysql://" + config.getString("DB_Url") + "/" + DBName + "?autoReconnect=true";
			String DBUser = config.getString("DB_User");
			String DBPw = config.getString("DB_Pw");
			conn = DriverManager.getConnection(DBUrl, DBUser, DBPw);

			InvSyncMain.sendConsoleMessage("default", "Datenbankverbindung erfolgreich aufgebaut");

		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	// Gibt die Datenbankverbindung als Connection Objekt weiter
	protected Connection getDatabaseConnection() {
		return conn;
	}

	private void configureDatabase(Connection connection) {

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
			
			String[] tableArray =  {
					"playerAdvancements", "playerEffects", "playerEnderChests", "playerHealthValues",
					"playerInventories", "playerLevels", "playerCurrentSlots", "playerSaturationValues",
					"playerFoodLevels"
					};
			String[] columnArray = {
					"advancements MEDIUMTEXT", "effects MEDIUMTEXT", "enderchest MEDIUMTEXT", 
					"health DOUBLE", "inventory MEDIUMTEXT", "level INT", "currentslot TINYINT",
					"saturation FLOAT", "foodlevel TINYINT"
			};
			for (int i = 0; i < tableArray.length; i++) {
				String createTableIfNotExists = "CREATE TABLE IF NOT EXISTS "
						+ tableArray[i] + " (uuid VARCHAR(36) PRIMARY KEY, "
						+ columnArray[i] +")";
				stmt.executeUpdate(createTableIfNotExists);

				if (stmt.getWarnings() != null) {
					InvSyncMain.sendConsoleMessage("default", "Tabelle '" + tableArray[i] + "' existiert bereits, Erstellung wird übersprungen");

				} else {
					InvSyncMain.sendConsoleMessage("warning", "Tabelle '" + tableArray[i] + "' erfolgreich erstellt");
				}
				stmt.clearWarnings();
			}

			// Schliesst das Statement
			stmt.close();

		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	// Schliesst die Datenbankverbindung, wird durch onDisable() in InvSyncMain
	// aufgerufen
	protected void closeDatabaseConnection() {
		
		if (conn != null) {
			try {
				conn.close();
				InvSyncMain.sendConsoleMessage("default", "Datenbankverbindung erfolgreich geschlossen");

			} catch (SQLException e) {
				e.printStackTrace();

			}
		}
	}
}