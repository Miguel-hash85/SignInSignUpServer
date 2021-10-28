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
public class BasicConnectionPool{
    private String url;
    private String user;
    private String password;
    private ArrayList<Connection> connectionPool;
    private ArrayList<Connection> usedConnections=new ArrayList<>();
    private final short INITIAL_POOL_SIZE=10;

   public BasicConnectionPool create(){
        user=ResourceBundle.getBundle("config.configuration").getString("USER");
        url=ResourceBundle.getBundle("config.configuration").getString("URL");
        password=ResourceBundle.getBundle("config.configuration").getString("PASSWORD");
        ArrayList<Connection> pool = new ArrayList<>(INITIAL_POOL_SIZE);
        for (int i = 0; i < INITIAL_POOL_SIZE; i++) {
            try {
                pool.add(createConnection(url, user, password));
            } catch (SQLException ex) {
                Logger.getLogger(BasicConnectionPool.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return new BasicConnectionPool(url, user, password, pool);
    }
    
    // standard constructors

    public BasicConnectionPool(String url, String user, String password, ArrayList<Connection> connectionPool) {
        this.url = url;
        this.user = user;
        this.password = password;
        this.connectionPool = connectionPool;
    }
    
   
    public BasicConnectionPool() {
    }

    public Connection getConnection() {
        Connection connection = connectionPool
                .remove(connectionPool.size() - 1);
        usedConnections.add(connection);
        return connection;
    }
      
    public boolean releaseConnection(Connection connection) {
        connectionPool.add(connection);
        return usedConnections.remove(connection);
    }
    
    private static Connection createConnection(
      String url, String user, String password) 
      throws SQLException {
        return DriverManager.getConnection(url, user, password);
    }
    
    public int getSize() {
        return connectionPool.size() + usedConnections.size();
    }

}
