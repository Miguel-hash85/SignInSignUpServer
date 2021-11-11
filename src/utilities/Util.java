package utilities;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class Util {

    public static String introducirCadena() {
        String cadena = "";
        boolean error = false;
        InputStreamReader entrada = new InputStreamReader(System.in);
        BufferedReader teclado = new BufferedReader(entrada);
        do {
            try {
                cadena = teclado.readLine();
            } catch (IOException e) {
                error = true;
                System.out.println("Error en la entrada de datos, introduzca los datos de nuevo");
            }
        } while (error);
        return cadena;
    }


    public static int leerInt(String mensaje) {
        int num = 0;
        boolean error;
        System.out.println(mensaje);
        do {
            error = false;
            try {
                num = Integer.parseInt(introducirCadena());
            } catch (NumberFormatException e) {
                System.out.println("Error, el dato no es numérico. Introduce de nuevo: ");
                error = true;
            }
        } while (error);
        return num;
    }
}
