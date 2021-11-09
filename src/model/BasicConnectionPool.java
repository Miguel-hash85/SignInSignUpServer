/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package model;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author 2dam
 */
public class BasicConnectionPool {

    private String url;
    private String user;
    private String password;
    // Collection of connections.
    private ArrayList<Connection> connectionPool = new ArrayList<>();
    // Collection of used connections.
    private ArrayList<Connection> usedConnections = new ArrayList<>();
    // Declaration of size of the connection pool, the size value is decalared in configuration file.
    private final short INITIAL_POOL_SIZE = Short.parseShort(ResourceBundle.getBundle("config.configuration").getString("MAXCONNECTIONS"));
    // Logger to record the events and trace out errors.
    private static final Logger LOGGER = Logger.getLogger("model.BasicConnectionPool.class");


    /**
     *
     */
    public BasicConnectionPool() {
        LOGGER.info("ConnectionPool created");
        // Name of the user that is read from configuration file from package config.
        user = ResourceBundle.getBundle("config.configuration").getString("USER");
        // URL to the database that is read from configuration file from package config.
        url = ResourceBundle.getBundle("config.configuration").getString("URL");
        // password for user to login into database is read from configuration file from package config.
        password = ResourceBundle.getBundle("config.configuration").getString("PASSWORD");
        
        // Adding connections to the connection pool.
        for (int i = 0; i < INITIAL_POOL_SIZE; i++) {
            try {
                connectionPool.add(createConnection(url, user, password));
            } catch (SQLException ex) {
                Logger.getLogger(BasicConnectionPool.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
    
    /**
     *
     * @return a connection, when a connection is required it would be taken from the connectionPool and added to usedConnections collection.
     */
    public Connection getConnection() {
        LOGGER.info("Connection getted from the connectionPool");
        Connection connection = connectionPool.remove(connectionPool.size() - 1);
        usedConnections.add(connection);
        return connection;
    }
    
    /**
     *
     * @param connection, an object of connection.
     * @return confirmation of if connection is added to pool.
     * When is connection is free and not being used it would again added to the pool, and would be removed
     * from the collection of usedConnections.
     */
    public boolean releaseConnection(Connection connection) {
        LOGGER.info("Connection released and introduced again in the connectionPool");
        connectionPool.add(connection);
        return usedConnections.remove(connection);
    }
    /**
     * 
     * @param url
     * @param user
     * @param password
     * @return creates and return the connection to the connection pool.
     * @throws SQLException 
     */
    private static Connection createConnection(String url, String user, String password) throws SQLException {
        LOGGER.info("Connection with the database created");
        return DriverManager.getConnection(url, user, password);
    }
    
    /**
     *
     * @return calculate and return the number of available connections.
     */
    public int getSize() {
        LOGGER.info("Available connections calculated");
        return connectionPool.size() + usedConnections.size();
    }
    
}
