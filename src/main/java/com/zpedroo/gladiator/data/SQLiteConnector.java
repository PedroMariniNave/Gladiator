package com.zpedroo.gladiator.data;

import com.zpedroo.gladiator.Main;

import java.io.File;
import java.io.IOException;
import java.util.logging.*;
import java.sql.*;
import java.util.*;

public class SQLiteConnector {

    Connection connection;
    private Logger logger;
    private String TABLE_NAME;

    public SQLiteConnector(String TABLE_NAME) {
        this.TABLE_NAME = TABLE_NAME;
        connection = null;
        (logger = Main.get().getLogger()).info("Connecting to the SQLite database...");
        long ms = System.currentTimeMillis();
        try {
            Class.forName("org.sqlite.JDBC");
            String url = "jdbc:sqlite:" + loadFile("/players.db").getAbsolutePath();
            connection = DriverManager.getConnection(url);
            logger.info("Connection to SQLite has been established in " + (System.currentTimeMillis() - ms) + "ms.");
            checkTable();
        } catch (SQLException | ClassNotFoundException ex) {
            logger.severe("Failed to connect to the SQLite database:");
            ex.printStackTrace();
        }
    }

    private void checkTable() throws SQLException {
        try (Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery("SELECT * FROM sqlite_master WHERE type='table' AND name='" + TABLE_NAME + "';")) {
            Long l = null;
            if (!resultSet.next()) {
                logger.info("Database table does not exist, creating...");
                l = System.currentTimeMillis();
            }
            statement.execute("CREATE TABLE IF NOT EXISTS `" + TABLE_NAME + "` (`uuid` VARCHAR(255) NOT NULL, `trusted` BOOLEAN NOT NULL DEFAULT FALSE,  PRIMARY KEY (`uuid`));");
            if (l != null) {
                logger.info("Created table in " + (System.currentTimeMillis() - l) + "ms.");
            }
        }
    }

    public PlayerData loadPlayer(UUID uuid) {
        try {
            Statement statement = connection.createStatement();
            ResultSet resultSet = statement.executeQuery("SELECT * FROM '" + TABLE_NAME + "' where uuid='" + uuid.toString() + "';");
            if (resultSet.next()) {
                boolean trusted = resultSet.getBoolean("trusted");
                return new PlayerData(uuid, trusted);
            }
            return new PlayerData(uuid, false);
        } catch (SQLException e) {
            e.printStackTrace();
            return new PlayerData(uuid, false);
        }
    }

    public boolean savePlayer(PlayerData playerData) {
        try (Statement statement = connection.createStatement()) {
            statement.execute("INSERT OR REPLACE INTO '" + TABLE_NAME + "' VALUES (\"" + playerData.getUUID().toString() + "\", " + (playerData.isTrusted() ? 1 : 0) + ");");
            return true;
        }
        catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public void closeConnection() {
        try {
            connection.close();
        } catch (Exception e) {}
    }

    private File loadFile(String filen) {
        File file = new File(Main.get().getDataFolder(), filen);
        if (!file.exists()) {
            try {
                if (file.getParent() != null) {
                    file.getParentFile().mkdirs();
                }
                file.createNewFile();
            }
            catch (IOException e) {
                e.printStackTrace();
            }
        }
        return file;
    }
}
