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
import model.SignableFactory;

/**
 *
 * @author Aitor Ruiz de Gauna.
 */
public class PetitionControllerThread extends Thread {

    private Socket socket;
    private DataEncapsulation dataEncapsulation;
    private SignableFactory daoableFactory;
    private Signable signable;
    // Logger to record the events and trace out errors.
    private static final Logger LOGGER = Logger.getLogger("logic.PetitionControllerThread.class");

    /**
     *
     * @return an object of socket
     */
    public Socket getSocket() {
        return socket;
    }

    /**
     *
     * @param socket receives a socket.
     */
    public void setSocket(Socket socket) {
        LOGGER.info("Socket set");
        this.socket = socket;
    }

    /**
     *
     * @return an object of dataEnacpsulation.
     */
    public DataEncapsulation getDataEncapsulation() {
        return dataEncapsulation;
    }

    /**
     *
     * @param dataEncapsulation, receives an object of dataEncapsulation.
     */
    public void setDataEncapsulation(DataEncapsulation dataEncapsulation) {
        this.dataEncapsulation = dataEncapsulation;
    }

    /**
     *
     * @return an object of daoableFactory.
     */
    public SignableFactory getDaoableFactory() {
        return daoableFactory;
    }

    /**
     *
     * @param daoableFactory, receives an object of daoableFactory
     */
    public void setDaoableFactory(SignableFactory daoableFactory) {
        this.daoableFactory = daoableFactory;
    }

    /**
     *
     * @return an object of interface signable.
     */
    public Signable getSignable() {
        return signable;
    }

    /**
     *
     * @param signable receives an object of interface signable.
     */
    public void setSignable(Signable signable) {
        this.signable = signable;
    }

    @Override
    public void run() {
        LOGGER.info("Petitions of signIn and signUp done");
        daoableFactory = new SignableFactory();
        signable = daoableFactory.getSignableImplementation();
        ObjectInputStream in = null;
        ObjectOutputStream out = null;
        try {
            in = new ObjectInputStream(socket.getInputStream());
            out = new ObjectOutputStream(socket.getOutputStream());
            //reading dataEncapsulation to know the type of message.
            dataEncapsulation = (DataEncapsulation) in.readObject();
            // if message is SIGNIN, call to interface to recieve the user from database.
            if (dataEncapsulation.getMessage().equals(Message.SIGNIN)) {
                dataEncapsulation.setUser(signable.signIn(dataEncapsulation.getUser()));
                dataEncapsulation.setMessage(Message.OK);
                out.writeObject(dataEncapsulation);
                // if messsage is SIGNUP, call to interface to add user to database.
            } else if (dataEncapsulation.getMessage().equals(Message.SIGNUP)) {
                signable.signUp(dataEncapsulation.getUser());
                dataEncapsulation.setMessage(Message.OK);
                out.writeObject(dataEncapsulation);
            }
            // above two calls can throw these exceptions.
        } catch (IOException ex) {
            Logger.getLogger(PetitionControllerThread.class.getName()).log(Level.SEVERE, null, ex);
        } catch (ClassNotFoundException ex) {
            Logger.getLogger(PetitionControllerThread.class.getName()).log(Level.SEVERE, null, ex);
        } catch (UserAlreadyExistException aex) {
            dataEncapsulation.setMessage(Message.EXISTING_USERNAME);
            sendMessage(dataEncapsulation, out);
        } catch (ConnectionRefusedException cex) {
            dataEncapsulation.setMessage(Message.CONNECTION_ERROR);
            sendMessage(dataEncapsulation, out);
        } catch (IncorrectPasswordException cex) {
            dataEncapsulation.setMessage(Message.INCORRECT_PASSWORD);
            sendMessage(dataEncapsulation, out);
        } catch (UserNotFoundException cex) {
            dataEncapsulation.setMessage(Message.USER_NOTFOUND);
            sendMessage(dataEncapsulation, out);
        } catch (Exception ex) {
            Logger.getLogger(PetitionControllerThread.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
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
/**
 * 
 * @param data, dataEncapsulation that received by call to database, 
 * @param out , output stream to send dataEncapsulation back to the client.
 */
    private void sendMessage(DataEncapsulation data, ObjectOutputStream out) {
        LOGGER.info("Petition results sent to the client");
        try {
            out.writeObject(dataEncapsulation);
        } catch (IOException ex) {
            Logger.getLogger(PetitionControllerThread.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
