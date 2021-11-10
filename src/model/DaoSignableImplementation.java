/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package model;

import classes.User;
import exceptions.ConnectionRefusedException;
import exceptions.IncorrectPasswordException;
import exceptions.UserAlreadyExistException;
import exceptions.UserNotFoundException;
import interfaces.Signable;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.logging.Logger;

/**
 * Class that manages the DAO operations.
 * @author Miguel Sanchez, Aitor Ruiz de Gauna.
 */
public class DaoSignableImplementation implements Signable {
    // Connection pool.
    private static BasicConnectionPool pool = new BasicConnectionPool();
    private PreparedStatement stmt;
    private PreparedStatement stmtSignIn;
    private ResultSet rs;
    private ResultSet rsSignIn;
    private Connection con;
    // Query to add user to database, (signUp to the application)l.
    private final String insert = ("Insert into user (login,email,fullname,status,privilege,password,lastpasswordchange) values(?,?,?,?,?,?,?)");
    // Query to Select a user to let him sign in.
    private final String select = ("Select * from user where login=?");
    // Query to count the number of signIns of an user.
    private final String countSignIns = ("Select count(lastSignIn) as \"count\" from signin where id_user=?");
    // Query to add signIn of an user to database.
    private final String insertSignInDate = ("Insert into signin (lastSignIn, id_user) values (?,?)");
    // Query to manage the last 10 logins of an user.
    private final String selectForUpdate = "Select * from signin where id_user=? order by lastSignIn";
    // Logger to record the events and trace out errors.
    private static final Logger LOGGER = Logger.getLogger("model.BasicConnectionPool.class");
    
    
    /**
     * Method that add the user to the database.
     * @param user, receives an user to add it the database.
     * @throws UserAlreadyExistException will be thrown When it gets a message from server that the user already exist.
     * @throws ConnectionRefusedException will be thrown when connection to server get refused.
     * @throws Exception This Method can throws Exception
     */
    
   
    
    
    @Override
    public void signUp(User user) throws UserAlreadyExistException, ConnectionRefusedException, Exception {
        // A connection from connection pool taken.
        con = pool.getConnection();
        try {
            stmt = con.prepareStatement(select);
            stmt.setString(1, user.getLogin());
            rs = stmt.executeQuery();
            LOGGER.info("User searched");
            if (rs.next()) {
                LOGGER.info("User already exist");
                throw new UserAlreadyExistException();
            } else {
                stmt = con.prepareStatement(insert);
                stmt.setString(1, user.getLogin());
                stmt.setString(2, user.getEmail());
                stmt.setString(3, user.getFullname());
                stmt.setInt(4, user.getStatus().ordinal());
                stmt.setInt(5, user.getStatus().ordinal());
                stmt.setString(6, user.getPassword());
                stmt.setTimestamp(7, Timestamp.valueOf(user.getLastPasswordChange()));
                stmt.executeUpdate();
                LOGGER.info("User inserted in the database");
            }
        } catch (SQLException ex) {
             throw new Exception();
        } finally {
            rs.close();
            // Connection returned to connection pool
            pool.releaseConnection(con);
        }

    }
/**
 * Method that look for required user in database and return that user and manages the user's signIns record.
 * @param user, receives an user to search from database.
 * @return user, if user is found in database.
 * @throws UserNotFoundException will be thrown incase of user does not exist in database.
 * @throws IncorrectPasswordException will be thrown in case of user exist but received password does not match to one in database,
 * @throws ConnectionRefusedException will be thrown in case of connection to server is refused.
 * @throws Exception This Method can throws Exception
 */
    @Override
    public User signIn(User user) throws UserNotFoundException, IncorrectPasswordException, ConnectionRefusedException, Exception {

        con = pool.getConnection();
        try {
            LOGGER.info("User searched");
            stmt = con.prepareStatement(select);
            stmt.setString(1, user.getLogin());
            rs = stmt.executeQuery();
            if (rs.next()) {
                if (user.getPassword().equals(rs.getString("password"))) {
                    user = new User();
                    user.setFullname(rs.getString("fullname"));
                    user.setEmail(rs.getString("email"));
                    user.setLogin(rs.getString("login"));
                    stmtSignIn = con.prepareStatement(countSignIns);
                    stmtSignIn.setInt(1, rs.getInt("id"));
                    rsSignIn = stmtSignIn.executeQuery();
                    if (rsSignIn.next()) {
                        if (rsSignIn.getInt("count") == 10) {
                            // in case of signIns record of an user reached to 10, update of 1st (oldest) to the time of signIn.
                            stmtSignIn = con.prepareStatement(selectForUpdate, ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_UPDATABLE);
                            stmtSignIn.setInt(1, rs.getInt("id"));
                            rsSignIn = stmtSignIn.executeQuery();
                            rsSignIn.first();
                            rsSignIn.updateTimestamp("lastSignIn", Timestamp.valueOf(LocalDateTime.now()));
                            rsSignIn.updateRow();
                        } else {
                            // Insert of signIn record of the user.
                            stmtSignIn = con.prepareStatement(insertSignInDate);
                            stmtSignIn.setTimestamp(1, Timestamp.valueOf(LocalDateTime.now()));
                            stmtSignIn.setInt(2, rs.getInt("id"));
                            stmtSignIn.executeUpdate();
                        }
                    }
                } else {
                    LOGGER.info("Incorrect password");
                    throw new IncorrectPasswordException();
                }
            } else {
                LOGGER.info("User not found");
                throw new UserNotFoundException();
            }
        } catch (SQLException ex) {
            throw new Exception();
        } finally {
            rs.close();
            pool.releaseConnection(con);
        }
        LOGGER.info("User info returned to the client");
        return user;
    }

}
