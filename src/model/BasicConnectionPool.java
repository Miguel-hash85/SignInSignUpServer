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
    private ArrayList<Connection> connectionPool = new ArrayList<>();
    private ArrayList<Connection> usedConnections = new ArrayList<>();
    private final short INITIAL_POOL_SIZE = Short.parseShort(ResourceBundle.getBundle("config.configuration").getString("MAXCONNECTIONS"));
    private static final Logger logger = Logger.getLogger("model.BasicConnectionPool.class");

    // standard constructors  
    public BasicConnectionPool() {
        logger.info("ConnectionPool created");
        user = ResourceBundle.getBundle("config.configuration").getString("USER");
        url = ResourceBundle.getBundle("config.configuration").getString("URL");
        password = ResourceBundle.getBundle("config.configuration").getString("PASSWORD");
        for (int i = 0; i < INITIAL_POOL_SIZE; i++) {
            try {
                connectionPool.add(createConnection(url, user, password));
            } catch (SQLException ex) {
                Logger.getLogger(BasicConnectionPool.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
    
    public Connection getConnection() {
        logger.info("Connection getted from the connectionPool");
        Connection connection = connectionPool.remove(connectionPool.size() - 1);
        usedConnections.add(connection);
        return connection;
    }
    
    public boolean releaseConnection(Connection connection) {
        logger.info("Connection released and introduced again in the connectionPool");
        connectionPool.add(connection);
        return usedConnections.remove(connection);
    }
    
    private static Connection createConnection(String url, String user, String password) throws SQLException {
        logger.info("Connection with the database created");
        return DriverManager.getConnection(url, user, password);
    }
    
    public int getSize() {
        logger.info("Available connections calculated");
        return connectionPool.size() + usedConnections.size();
    }
    
}
