/*
 * AUTOR: Ana Lasheras Mas
 * NIA: 620546
 * AUTOR: Alberto Martínez Menéndez
 * NIA: 681061
 * FICHERO: MessageSystem.java
 * TIEMPO: 3 horas
 * DESCRIPCIÓN: Clase gestora del envio y extraccion de mensajes.
 */
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.net.ConnectException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Scanner;
import java.net.BindException;
import java.util.concurrent.Semaphore;

public class MessageSystem {
	int source; // Id del proceso
	String networkFile; // Fichero con las direcciones
	Thread gestorBuzon; // Thread que gestiona el buzon
	private static Buzon buzon = new Buzon(); // Buzon

	/**
	 * Constructor de la clase MessageSystem.
	 */
	public MessageSystem(int source, String networkFile)
			throws FileNotFoundException, BindException {
		this.source = source;
		this.networkFile = networkFile;
		ServerSocket listenSocket = null;

		// Busco mi puerto
		int miPuerto = buscoPuerto(source);

		try {
			// Creamos el socket de escucha
			listenSocket = new ServerSocket(miPuerto);
		} catch (BindException e) {
			throw e;
		} catch (UnknownHostException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		// Lanzamos el gestor del buzon con el socket de escucha
		gestorBuzon = new Thread(new GestorBuzon(listenSocket, buzon));
		gestorBuzon.start();
	}

	/**
	 * Metodo que convierte el mensaje en un objeto "Envelope" y lo envia al
	 * buzon de la maquina destino.
	 * 
	 * @throws ConnectException
	 */
	public void send(int dst, Serializable message) throws ConnectException {
		// Buscamos el puerto al que queremos conectarnos
		int puertoDestino = buscoPuerto(dst);
		// Creamos un objeto envelope con la informacion que vamos a mandar
		Envelope e = new Envelope(source, dst, message);

		// Creamos el socket
		Socket socket = null;
		try {
			socket = new Socket("localhost", puertoDestino);

			// Enviamos el envelope y cerramos el canal
			ObjectOutputStream oos;
			try {
				oos = new ObjectOutputStream(socket.getOutputStream());
				oos.writeObject(e);
				oos.close();
			} catch (IOException e1) {
				e1.printStackTrace();
			}
		} catch (UnknownHostException e1) {
			e1.printStackTrace();
		} catch (ConnectException e1) {
			throw e1;
		} catch (IOException e1) {
			e1.printStackTrace();
		}
	}

	/**
	 * Metodo que extrae del buzon un mensaje.
	 */
	public Envelope receive() {
		Envelope env = buzon.sacarMensaje();
		return env;
	}

	/**
	 * Metodo que detiene el buzon.
	 */
	public void stopMailbox() {
		// Cerramos el buzon
		gestorBuzon.interrupt();
	}

	/**
	 * Metodo que devuelve el puerto correspondiente a la maquina identificada
	 * con "iD".
	 */
	public int buscoPuerto(int iD) {
		Scanner fichero = null;
		int puerto = 0;
		boolean encontrado = false;
		try {
			// Abrimos el fichero con los puertos
			fichero = new Scanner(new File(networkFile));
			fichero.useDelimiter(":|\r|\n");
		} catch (FileNotFoundException e2) {
			e2.printStackTrace();
		}

		// Leemos el puerto
		while (!encontrado) {
			// Si es esa maquina leemos el puerto, sino seguimos avanzando a la
			// siguiente linea
			if (fichero.nextInt() == iD) {
				encontrado = true;
				puerto = fichero.nextInt();
			} else {
				// Si no es esa maquina saltamos la linea
				fichero.nextLine();
			}
		}
		fichero.close();
		return puerto;
	}

	/**
	 * Metodo que nos informa de si el buzon esta vacio.
	 */
	public boolean buzonVacio() {
		return buzon.buzonVacio();
	}

	/**
	 * Metodo que nos devuelve el numero de mensajes que hay en el buzon.
	 */
	public int totalEnBuzon() {
		return buzon.totalEnBuzon();
	}

	/**
	 * Metodo que nos devuelve el numero de mensajes que se han perdido.
	 */
	public int perdidos() {
		return buzon.mensajesPerdidos();
	}
}
