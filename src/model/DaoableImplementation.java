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

/**
 *
 * @author 2dam
 */
public class DaoableImplementation implements Signable {

    private static BasicConnectionPool pool = new BasicConnectionPool();
    private PreparedStatement stmt;
    private PreparedStatement stmtSignIn;
    private ResultSet rs;
    private ResultSet rsSignIn;
    private Connection con;
    private final String insert = ("Insert into user (login,email,fullname,status,privilege,password,lastpasswordchange) values(?,?,?,?,?,?,?)");
    private final String select = ("Select * from user where login=?");
    private final String countSignIns=("Select count(lastSignIn) as \"count\" from signin where id_user=?");
    private final String insertSignInDate=("Insert into signin (lastSignIn, id_user) values (?,?)");
    private final String selectForUpdate="Select * from signin where id_user=? order by lastSignIn";

    @Override
    public void signUp(User user) throws UserAlreadyExistException, ConnectionRefusedException, Exception {

        con = pool.getConnection();

        try {
            stmt = con.prepareStatement(select);
            stmt.setString(1, user.getLogin());
            rs = stmt.executeQuery();
            if (rs.next()) {
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
            }
        } catch (SQLException ex) {
            throw new ConnectionRefusedException();
        } finally {
            rs.close();
        }

        pool.releaseConnection(con);
    }

    @Override
    public User signIn(User user) throws UserNotFoundException, IncorrectPasswordException, ConnectionRefusedException, Exception {

        con = pool.getConnection();
        try {
            stmt = con.prepareStatement(select);
            stmt.setString(1, user.getLogin());
            rs = stmt.executeQuery();
            if (rs.next()) {
                if (user.getPassword().equals(rs.getString("password"))) {
                    user = new User();
                    user.setFullname(rs.getString("fullname"));
                    user.setEmail(rs.getString("email"));
                    user.setLogin(rs.getString("login"));
                    stmtSignIn=con.prepareStatement(countSignIns);
                    stmtSignIn.setInt(1, rs.getInt("id"));
                    rsSignIn=stmtSignIn.executeQuery();
                    if(rsSignIn.next()){
                        if(rsSignIn.getInt("count")==2){
                            stmtSignIn=con.prepareStatement(selectForUpdate, ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_UPDATABLE);
                            stmtSignIn.setInt(1, rs.getInt("id"));
                            rsSignIn=stmtSignIn.executeQuery();
                            rsSignIn.first();
                            rsSignIn.updateTimestamp("lastSignIn", Timestamp.valueOf(LocalDateTime.now()));
                            rsSignIn.updateRow();
                        }else{
                            stmtSignIn=con.prepareStatement(insertSignInDate);
                            stmtSignIn.setTimestamp(1, Timestamp.valueOf(LocalDateTime.now()));
                            stmtSignIn.setInt(2, rs.getInt("id"));
                            stmtSignIn.executeUpdate();
                        }
                    }
                }
                else
                    throw new IncorrectPasswordException();
            } else {
                throw new UserNotFoundException();
            }
        } catch (SQLException ex) {
            throw new ConnectionRefusedException();
            
        } finally {
            rs.close();
        }
        pool.releaseConnection(con);
        return user;
    }

}
