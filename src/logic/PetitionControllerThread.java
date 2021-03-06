/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package logic;

import static application.Server.closeThread;
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
/**
 * This class creates a thread so client can register it`s petition.
 */
public class PetitionControllerThread extends Thread {
    //Declaration of the socket.
    private Socket socket;
    //Declaration of the dataEncapsulation.
    private DataEncapsulation dataEncapsulation;
    //Declaration of the signableFactory.
    private SignableFactory signableFactory;
    //Declaration of the signable interface;
    private Signable signable;
    // Logger to record the events and trace out errors.
    private static final Logger LOGGER = Logger.getLogger("logic.PetitionControllerThread.class");

    /**
     * This method return the socket.
     * @return an object of socket
     */
    public Socket getSocket() {
        return socket;
    }

    /**
     * This method receives a socket.
     * @param socket receives a socket.
     */
    public void setSocket(Socket socket) {
        LOGGER.info("Socket set");
        this.socket = socket;
    }

    /**
     * This method return a dataEncapsulation object.
     * @return an object of dataEnacpsulation.
     */
    public DataEncapsulation getDataEncapsulation() {
        return dataEncapsulation;
    }

    /**
     * This method receives a dataEncapsulation object.
     * @param dataEncapsulation, receives an object of dataEncapsulation.
     */
    public void setDataEncapsulation(DataEncapsulation dataEncapsulation) {
        this.dataEncapsulation = dataEncapsulation;
    }

    /**
     * This method return a signableFactory object.
     * @return an object of signableFactory.
     */
    public SignableFactory getSignableFactory() {
        return signableFactory;
    }

    /**
     * This method receives a signableFactory.
     * @param signableFactory, receives an object of daoableFactory
     */
    public void setSignableFactory(SignableFactory signableFactory) {
        this.signableFactory = signableFactory;
    }

    /**
     * This method return an object of interface sigbable.
     * @return an object of interface signable.
     */
    public Signable getSignable() {
        return signable;
    }

    /**
     * This method receives an object of interface signable.
     * @param signable receives an object of interface signable.
     */
    public void setSignable(Signable signable) {
        this.signable = signable;
    }
   
    /**
     * This method run the petition to process.
     */
    @Override
    public void run() {
        LOGGER.info("Petitions of signIn and signUp done");
        //signableFactory instanced.
        signableFactory = new SignableFactory();
        //signable value set with the getSignableImplementation of the signableFactory
        signable = signableFactory.getSignableImplementation();
        ObjectInputStream in = null;
        ObjectOutputStream out = null;
        try {
            //Read stream created
            in = new ObjectInputStream(socket.getInputStream());
            //Write stream created
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
                //close the read and write streams.
                in.close();
                out.close();
                //close the socket
                socket.close();
                //delete the thread from the collection of petitionControllerThread.
                closeThread(this);
                interrupt();
            } catch (IOException ex) {
                Logger.getLogger(PetitionControllerThread.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
/**
 * This method send the response to client.
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
