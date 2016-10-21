/*
 * AUTOR: Ana Lasheras Mas
 * NIA: 620546
 * AUTOR: Alberto Martínez Menéndez
 * NIA: 681061
 * FICHERO: MaquinaReceptora.java
 * TIEMPO: 30 minutos
 * DESCRIPCIÓN: Clase que se encarga de enviar mensajes a la maquina 0 para
 * 				comprobar las trazas de ejecucion.
 */
import java.io.FileNotFoundException;
import java.net.BindException;
import java.net.ConnectException;
import java.util.Scanner;

public class MaquinaEmisora {
	public static void main(int id, String fichero) throws BindException,
			FileNotFoundException {
		int myID = id; // Mi identificador
		String networkFile = fichero; // Fichero de puertos
		int destinatario = 0; // Maquina destino
		String mensaje = ""; // Mensaje a enviar
		Scanner continuar = new Scanner(System.in);

		try {
			MessageSystem ms = new MessageSystem(myID, networkFile);
			System.out.println("ENVIO INCIADO (" + myID + ").");
		
			// Cada maquina enviara 30 mensajes
			for (int i = 0; i < 30; i++) {
				mensaje = "Soy (" + myID + "): mensaje numero " + i;
				try {
					ms.send(destinatario, mensaje);
				} catch (ConnectException e) {
					System.err.println("\tEl destinatario no esta conectado.");
				}
			}

			System.out.println("No quedan mas mensajes para enviar.");
			System.exit(0);

		} catch (BindException e) {
			throw e;
		}

	}
}
