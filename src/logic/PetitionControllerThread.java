/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package logic;

import static application.Application.closeThread;
import classes.DataEncapsulation;
import classes.Message;
import exceptions.ConnectionRefusedException;
import exceptions.IncorrectPasswordException;
import exceptions.UserAlreadyExistException;
import exceptions.UserNotFoundException;
import interfaces.Signable;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.logging.Level;
import java.util.logging.Logger;
import model.DaoableFactory;

/**
 *
 * @author Aitor Ruiz de Gauna.
 */
public class PetitionControllerThread extends Thread{
    private Socket socket;
    private DataEncapsulation dataEncapsulation;
    private DaoableFactory daoableFactory;
    private Signable signable;
    

    public Socket getSocket() {
        return socket;
    }

    public void setSocket(Socket socket) {
        this.socket = socket;
    }

    public DataEncapsulation getDataEncapsulation() {
        return dataEncapsulation;
    }

    public void setDataEncapsulation(DataEncapsulation dataEncapsulation) {
        this.dataEncapsulation = dataEncapsulation;
    }

    public DaoableFactory getDaoableFactory() {
        return daoableFactory;
    }

    public void setDaoableFactory(DaoableFactory daoableFactory) {
        this.daoableFactory = daoableFactory;
    }

    public Signable getSignable() {
        return signable;
    }

    public void setSignable(Signable signable) {
        this.signable = signable;
    }
    
    
    @Override
    public void run(){
        daoableFactory=new DaoableFactory();
        signable=daoableFactory.getSignableImplementation();
        ObjectInputStream in = null;
        ObjectOutputStream out = null; 
        try{
            in= new ObjectInputStream(socket.getInputStream());
             out = new ObjectOutputStream(socket.getOutputStream());
            dataEncapsulation=(DataEncapsulation) in.readObject();
            if(dataEncapsulation.getMessage().equals(Message.SIGNIN)){
                dataEncapsulation.setUser(signable.signIn(dataEncapsulation.getUser()));
                dataEncapsulation.setMessage(Message.OK);
                out.writeObject(dataEncapsulation);
            }else if(dataEncapsulation.getMessage().equals(Message.SIGNUP)){
                signable.signUp(dataEncapsulation.getUser());
                dataEncapsulation.setMessage(Message.OK);
                out.writeObject(dataEncapsulation);
            }
        } catch (IOException ex) {
            Logger.getLogger(PetitionControllerThread.class.getName()).log(Level.SEVERE, null, ex);
        } catch (ClassNotFoundException ex) {
            Logger.getLogger(PetitionControllerThread.class.getName()).log(Level.SEVERE, null, ex);
        }catch (UserAlreadyExistException aex) {
            dataEncapsulation.setMessage(Message.EXISTING_USERNAME);
            sendMessage(dataEncapsulation, out);
        }catch (ConnectionRefusedException cex) {
            dataEncapsulation.setMessage(Message.CONNECTION_ERROR);
            sendMessage(dataEncapsulation, out);
        }catch (IncorrectPasswordException cex) {
            dataEncapsulation.setMessage(Message.INCORRECT_PASSWORD);
            sendMessage(dataEncapsulation, out);
        }catch (UserNotFoundException cex) {
            dataEncapsulation.setMessage(Message.USER_NOTFOUND);
            sendMessage(dataEncapsulation, out);
        }  catch (Exception ex) {
            Logger.getLogger(PetitionControllerThread.class.getName()).log(Level.SEVERE, null, ex);
        }finally{
            try {
                in.close();
                out.close();
                socket.close();
                closeThread(this);
                interrupt();
            } catch (IOException ex) {
                Logger.getLogger(PetitionControllerThread.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
    private void sendMessage(DataEncapsulation data, ObjectOutputStream out){
        
        try {
                out.writeObject(dataEncapsulation);
            } catch (IOException ex) {
                Logger.getLogger(PetitionControllerThread.class.getName()).log(Level.SEVERE, null, ex);
            }
    }
}
