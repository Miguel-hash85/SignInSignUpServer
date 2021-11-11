/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package application;

import static java.lang.System.exit;
import utilities.Util;

/**
 *
 * @author Aitor
 */
public class Application {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        //Server thread initiated to accept connections.
        Server server=new Server();
        server.start();
        while(true){
            int end;
            //Read a message, when this message is 0 the server loop will end.
            end = Util.leerInt("Introduce 0 to stop the server");
            if(end==0){
                server.setEndServer(true);
                break;
            }
        }
        //Ends the application.
        exit(0);
    }
    
}
