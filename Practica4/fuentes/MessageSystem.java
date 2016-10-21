/**
 * AUTOR: Ana Lasheras Mas
 * NIA: 620546
 * AUTOR: Alberto Martinez Menendez
 * NIA: 681061
 * FICHERO: MessageSystem.java
 * TIEMPO: 3 horas
 * DESCRIPCION: Clase gestora del envio y extraccion de mensajes.
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

public class MessageSystem {
	private int source; // Id del proceso
	private String networkFile; // Fichero con las direcciones
	private Thread gestorBuzon; // Thread que gestiona el buzon
	private static Buzon buzon = new Buzon(); // Buzon
	private final int NPARTICIPANTES = 4; // Num. Participantes
	private int estampilla = 1; // Estampilla temporal

	/**
	 * Constructor de la clase MessageSystem.
	 */
	public MessageSystem(int source, String networkFile)
			throws FileNotFoundException, BindException {
		this.source = source;
		this.networkFile = networkFile;
		ServerSocket listenSocket = null;

		// Busco mi puerto
		int miPuerto = Integer.parseInt(buscoPuertoDireccion(source)[0]);

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
		String[] puertoDireccion = buscoPuertoDireccion(dst);
		int puerto = Integer.parseInt(puertoDireccion[0]);
		String direccion = puertoDireccion[1];

		// Creamos un objeto envelope con la informacion que vamos a mandar
		Envelope e = new Envelope(source, dst, message, estampilla);

		// Creamos el socket
		Socket socket = null;
		try {
			socket = new Socket(direccion, puerto);

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
	 * Metodo que convierte el mensaje en un objeto "Envelope" y lo envia al
	 * buzon de todas las maquinas participantes.
	 * 
	 * @throws ConnectException
	 */
	public void sendMulticast(Serializable message) throws ConnectException {
		int i;
		for (i = 1; i <= NPARTICIPANTES; i++) {
			if ((message instanceof REQ && i != source)
					|| !(message instanceof REQ)) {

				// Buscamos el puerto al que queremos conectarnos
				String[] puertoDireccion = buscoPuertoDireccion(i);
				int puertoDestino = Integer.parseInt(puertoDireccion[0]);
				String direccionDestino = puertoDireccion[1];

				// Creamos un objeto envelope con la informacion que vamos a
				// mandar
				Envelope e = new Envelope(source, i, message, estampilla);

				// Creamos el socket
				Socket socket = null;
				try {
					socket = new Socket(direccionDestino, puertoDestino);

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
					// throw e1;
					for (int j = 1; j <= NPARTICIPANTES; j++) {
						if (j != i) {
							send(j,
									"FIN DEL CHAT. SE HA DESCONECTADO EL PROCESO "
											+ i);
						}
					}
				} catch (IOException e1) {
					e1.printStackTrace();
				}
			}
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
	public String[] buscoPuertoDireccion(int iD) {
		Scanner fichero = null;
		String puertoDireccion[] = new String[2];
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
				puertoDireccion[0] = fichero.next();
				puertoDireccion[1] = fichero.next();
			} else {
				// Si no es esa maquina saltamos la linea
				fichero.nextLine();
			}
		}
		fichero.close();
		return puertoDireccion;
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

	/**
	 * Metodo que nos devuelve el id del proceso que utiliza esta clase
	 */
	public int getSource() {
		return source;
	}

	/**
	 * Metodo que nos devuelve la estampilla temporal actual
	 */
	public int getEstampilla() {
		return estampilla;
	}

	/**
	 * Metodo que permite modificar la estampilla temporal
	 */
	public void setEstampilla() {
		estampilla++;
	}

	/**
	 * Metodo que permite modificar la estampilla temporal
	 */
	public void setEstampilla(int newEstampilla) {
		estampilla = newEstampilla;
	}
}
