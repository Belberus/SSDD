/*
 * AUTOR: Ana Lasheras Mas
 * NIA: 620546
 * AUTOR: Alberto Martínez Menéndez
 * NIA: 681061
 * FICHERO: GestorBuzon.java
 * TIEMPO: 30 minutos
 * DESCRIPCIÓN: Clase (thread) que se encarga de recibir los mensajes de los otros usuarios
 * 				y almacenarlos en el Buzon.
 */
import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.ServerSocket;
import java.net.Socket;

public class GestorBuzon implements Runnable {
	ServerSocket listenSocket = null;
	Buzon buzon;

	/**
	 * Constructor de la clase GestorBuzon.
	 */
	public GestorBuzon(ServerSocket socket, Buzon buzon) {
		listenSocket = socket; // Socket de escucha
		this.buzon = buzon; // Buzon de mensajes
	}

	/**
	 * Metodo Run() de clase GestorBuzon.
	 */
	public void run() {
		Socket socketEntrada = null;
		ObjectInputStream ois = null;
		Envelope e = null;
		while (true) {
			try {
				// Aceptamos la conexion entrante
				socketEntrada = listenSocket.accept();
				// Leemos el envelope que nos han enviado
				ois = new ObjectInputStream(socketEntrada.getInputStream());
				e = (Envelope) ois.readObject();
				ois.close();
			} catch (IOException exception) {
				exception.printStackTrace();
			} catch (ClassNotFoundException e1) {
				e1.printStackTrace();
			}
			// Añadimos el mensaje a la lista de mensajes recibidos
			buzon.guardarMensaje(e);
		}

	}
}
