/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package application;

import classes.DataEncapsulation;
import classes.Message;
import java.io.IOException;
import java.io.ObjectOutputStream;
import static java.lang.System.exit;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import logic.PetitionControllerThread;
import static model.DaoSignableImplementation.POOL;
import utilities.Util;

/**
 * This is the principal class of application server.
 *
 * @author Zeeshan Yaqoob
 */
public class Server extends Thread{

    // An object of port that is getting its value from configuration file from package config.
    private final static int PORT = Integer.parseInt(ResourceBundle.getBundle("config.configuration").getString("PORT"));
    // An object of max_connections that is getting its value from configuration file from package config, this value indicated the max connection we can have with database.
    private final static short MAX_CONNECTIONS = Short.parseShort(ResourceBundle.getBundle("config.configuration").getString("MAXCONNECTIONS"));
    // A collection of PetitionControllerThread to controll the max number of clients
    private static ArrayList<PetitionControllerThread> petitionControllerThreads = new ArrayList<>();
    // Logger to record the events and trace out errors.
    private static final Logger LOGGER = Logger.getLogger("application.Application.class");
    private boolean endServer=false;
    /**
     * This method runs the application.
     *
     * @param args Applications entry point.
     */
    
    @Override
    public void run() {
        // TODO code application logic here
        //petitionControllerThreads = new ArrayList<>();
        LOGGER.info("The server accept connections and execute petitions");
        // Declaration of server socket.
        ServerSocket serverSocket = null;
        PetitionControllerThread petitionControllerThread = null;
        // Declaration of client socket.
        Socket clientSocket = null;
        // Decalation of input and output streams to send and receive messages.
        ObjectOutputStream out = null;
        DataEncapsulation dataEncapsulaton;
        try {
            serverSocket = new ServerSocket(PORT);
            while (!endServer) {
                // Connection between server and client is established
                clientSocket = serverSocket.accept();
                if (clientSocket != null) {
                    petitionControllerThread = new PetitionControllerThread();
                    petitionControllerThread.setSocket(clientSocket);

                }
                // incase of number of clients is reached to max.
                if (petitionControllerThreads.size() >= MAX_CONNECTIONS) {
                    out = new ObjectOutputStream(clientSocket.getOutputStream());
                    dataEncapsulaton = new DataEncapsulation();
                    dataEncapsulaton.setMessage(Message.CONNECTION_ERROR);
                    out.writeObject(dataEncapsulaton);

                } else {
                    // client would be addded to collection of threads.
                    synchronized (petitionControllerThreads) {
                        petitionControllerThreads.add(petitionControllerThread);
                        petitionControllerThread.start();
                    }

                }       
            }
            clientSocket.close();
            POOL.closeConnections();
            closeThreads();
            exit(0);
        } catch (IOException ex) {
            Logger.getLogger(Server.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                out.close();

            } catch (IOException ex) {
                Logger.getLogger(Server.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
    /**
     * Method that set a value to the boolean end server.
     * @param endServer used to continue or stop the server.
     */
    public void setEndServer(boolean endServer) {
        this.endServer = endServer;
    }

    /**
     * This method synchronize and close the client´s petitions.
     *
     * @param thread, method to remove client (PetitionControllerThread) once it
     * has done the action.
     */
    public static synchronized void closeThread(PetitionControllerThread thread) {
        LOGGER.info("Connections release");
        petitionControllerThreads.remove(thread);
    }
    public synchronized void closeThreads(){
        for(int i=0;i<petitionControllerThreads.size();i++){
            petitionControllerThreads.get(i).interrupt();
        }
    }
}
