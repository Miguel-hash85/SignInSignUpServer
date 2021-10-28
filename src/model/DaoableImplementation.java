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

/**
 *
 * @author 2dam
 */
public class DaoableImplementation implements Signable{
    
    //private BasicConnectionPool pool=new BasicConnectionPool();
    private PreparedStatement stmt;
    private ResultSet rs;
    private Connection con;
    private final String insert=("Insert into user values(?,?,?,?,?,?,?)");
    private final String select=("Select * from user where login=?");

    @Override
    public void signUp(User user) throws UserAlreadyExistException, ConnectionRefusedException,Exception {
        //con=pool.getConnection();
        
        try{
         stmt = con.prepareStatement(select);
         stmt.setString(1, user.getLogin());
         rs = stmt.executeQuery();
         if(rs.next()){
             throw new UserAlreadyExistException();
         }else{
            stmt = con.prepareStatement(insert);
            stmt.setString(1, user.getLogin());
            stmt.setString(2, user.getEmail());
            stmt.setString(3, user.getFullname());
            stmt.setInt(4, user.getStatus().ordinal());
            stmt.setInt(5, user.getStatus().ordinal());
            stmt.setString(6, user.getPassword());
            stmt.setTimestamp(7, Timestamp.valueOf(user.getLastPasswordChange()));
        }
        }catch(SQLException ex){
            throw new ConnectionRefusedException();
        }
        
        
        //pool.releaseConnection(con);
    }

    @Override
    public User signIn(User user) throws UserNotFoundException, IncorrectPasswordException, ConnectionRefusedException,Exception {
         //pool.getConnection();
         try{
         stmt = con.prepareStatement(select);
         stmt.setString(1, user.getLogin());
         rs = stmt.executeQuery();
         if(rs.next()){
             user=new User();
             user.setFullname(rs.getString("id"));
             user.setEmail(rs.getString("email"));
             user.setLogin(rs.getString("login"));
         }
         if(rs != null)
			rs.close();
         }catch(SQLException ex){
        }
        //pool.releaseConnection(con);
        return user;
    }
    
    
    
}
