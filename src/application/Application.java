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
    private static int PORT = 9000;

    public static void main(String[] args) {
        // TODO code application logic here
        ArrayList<PetitionControllerThread> petitionControllerThreads = new ArrayList<>();
        ServerSocket serverSocket = null;
        PetitionControllerThread petitionControllerThread = null;
        Socket clientSocket = null;
        ObjectOutputStream out;
        DataEncapsulation dataEncapsulaton;
        try {

            while (true) {
                serverSocket = new ServerSocket(PORT);
                clientSocket = serverSocket.accept();
                if (clientSocket != null) {
                    petitionControllerThread = new PetitionControllerThread();
                    petitionControllerThread.setSocket(clientSocket);
                    petitionControllerThreads.add(petitionControllerThread);
                }       
                if(petitionControllerThreads.size()>=10) {
                    out = new ObjectOutputStream(clientSocket.getOutputStream());
                    dataEncapsulaton = new DataEncapsulation();
                    dataEncapsulaton.setMessage(Message.CONNECTION_ERROR);
                    out.writeObject(dataEncapsulaton);
                    out.close();
                } else if(petitionControllerThreads.size()>0){
                    petitionControllerThread.start();
                }
            }

        } catch (IOException ex) {
            Logger.getLogger(Application.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

}
