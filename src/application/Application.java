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
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import logic.PetitionControllerThread;

/**
 *
 * @author 2dam
 */
public class Application {

    /**
     * @param args the command line arguments
     */
    private final static int PORT = Integer.parseInt(ResourceBundle.getBundle("config.configuration").getString("PORT"));
    private final static short MAX_CONNECTIONS = Short.parseShort(ResourceBundle.getBundle("config.configuration").getString("MAXCONNECTIONS"));
    private static ArrayList<PetitionControllerThread> petitionControllerThreads = new ArrayList<>();
    private static final Logger LOGGER = Logger.getLogger("application.Application.class");

    public static void main(String[] args) {
        // TODO code application logic here
        //petitionControllerThreads = new ArrayList<>();
        LOGGER.info("The server accept connections and execute petitions");
        ServerSocket serverSocket = null;
        PetitionControllerThread petitionControllerThread = null;
        Socket clientSocket = null;
        ObjectOutputStream out = null;
        DataEncapsulation dataEncapsulaton;
        try {
            serverSocket = new ServerSocket(PORT);
            while (true) {
                clientSocket = serverSocket.accept();
                if (clientSocket != null) {
                    petitionControllerThread = new PetitionControllerThread();
                    petitionControllerThread.setSocket(clientSocket);

                }
                if (petitionControllerThreads.size() >= MAX_CONNECTIONS) {
                    out = new ObjectOutputStream(clientSocket.getOutputStream());
                    dataEncapsulaton = new DataEncapsulation();
                    dataEncapsulaton.setMessage(Message.CONNECTION_ERROR);
                    out.writeObject(dataEncapsulaton);

                } else {
                    synchronized (petitionControllerThreads) {
                        petitionControllerThreads.add(petitionControllerThread);
                        petitionControllerThread.start();
                    }

                }
            }

        } catch (IOException ex) {
            Logger.getLogger(Application.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                out.close();

            } catch (IOException ex) {
                Logger.getLogger(Application.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    public static synchronized void closeThread(PetitionControllerThread thread) {
        LOGGER.info("Connections release");
        petitionControllerThreads.remove(thread);
    }

}
