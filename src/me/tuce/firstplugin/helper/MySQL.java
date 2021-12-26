package me.tuce.firstplugin.helper;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class MySQL {
    private String host;
    private String port;
    private String database;
    private String username;
    private String password;

    private Connection connection;

    public MySQL(String host, String port, String database, String username, String password) {
        this.host = host;
        this.port = port;
        this.database = database;
        this.username = username;
        this.password = password;
    }

    public void connect() throws ClassNotFoundException, SQLException {
        if(!isConnected())
            connection = DriverManager.getConnection("jdbc:mysql://" + host + ":" + port + "/" + database +  "?useSSL=false", username, password);
    }

    public void disconnect() {
        if(isConnected()) {
            try {
                connection.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    public boolean isConnected() {
        return (connection == null ? false : true);
    }

    public Connection getConnection() {
        return connection;
    }

    public void createTables() {
        PreparedStatement ps;
        try {
            ps = connection.prepareStatement(
                    "CREATE TABLE IF NOT EXISTS onsale " +
                            "(id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY, uuid VARCHAR(255), material VARCHAR(32), amount SMALLINT, priceItem VARCHAR(32), priceAmount SMALLINT, stack TINYINT);"
            );
            ps.executeUpdate();
            ps = connection.prepareStatement(
                    "CREATE TABLE IF NOT EXISTS offlineItems (id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY, uuid VARCHAR(255), diamond INT, diamond_block INT)"
            );
            ps.executeUpdate();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
