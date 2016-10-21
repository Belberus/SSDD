/*
 * AUTOR: Ana Lasheras Mas
 * NIA: 620546
 * AUTOR: Alberto Martínez Menéndez
 * NIA: 681061
 * FICHERO: Maquina.java
 * TIEMPO: 30 minutos
 * DESCRIPCIÓN: Clase que se encarga de gestionar las acciones del usuario
 * 				(leer y enviar mensajes). 
 */
import java.io.FileNotFoundException;
import java.net.ConnectException;
import java.util.Scanner;
import java.net.BindException;

public class Maquina {

	public static void main(boolean d, int iD, String fichero)
			throws FileNotFoundException, BindException {
		// creara el buzon
		boolean debug = d;
		int myID = iD; // ID de mi maquina
		String networkFile = fichero; // Nombre del fichero con puertos
		Scanner entrada = new Scanner(System.in); // Lector de teclado
		String mensaje = ""; // Mensaje a enviar
		String accion = ""; // Accion a realizar
		int destinatario = 0; // ID de la maquina destino
		Envelope env;
		int recibidos[] = new int[3]; // Contador de mensajes
		int iDEmisor = 0; // Id del emisor del mensaje recibido

		// Creamos un MessageSystem para gestionar los mensajes
		try {
			MessageSystem ms = new MessageSystem(myID, networkFile);

			System.out.println("SESION INICIADA");

			while (true) {
				accion = "";
				destinatario = 0;
				mensaje = "";
				String linea = "";
				while (!(accion.equals("enviar") | accion.equals("leer")
						| accion.equals("finalizar") | accion.equals("mostrar"))) {
					System.out
							.print("("
									+ myID
									+ ")Introduzca la accion que desea realizar (enviar | leer | finalizar | mostrar): ");
					// Leemos la accion solicitada por el cliente
					accion = entrada.next();
					// Si la accion no esta registrada mostramos mensaje
					if (!(accion.equals("enviar") | accion.equals("leer")
							| accion.equals("finalizar") | accion
								.equals("mostrar"))) {
						System.out
								.println("\tAccion no registrada, por favor seleccione otra.");
					}
				}
				if (accion.equals("enviar")) {
					// Si la accion es "enviar" continuamos para el envio
					while (!(destinatario >= 1 && destinatario <= 4)) {
						System.out.print("(" + myID
								+ ")Introduzca ID del destinatario(1,2,3,4): ");
						linea = entrada.next();

						entrada.nextLine();
						if (!((linea.equals("1")) | (linea.equals("2"))
								| (linea.equals("3")) | (linea.equals("4")))) {
							System.out
									.println("\tDestinatario no registrado, introduzca otro.");
						} else {
							destinatario = Integer.parseInt(linea);
						}
					}
					System.out.println("(" + myID
							+ ")Introduzca mensaje a enviar: ");
					mensaje = entrada.nextLine();

					// Enviamos el mensaje
					try {
						ms.send(destinatario, mensaje);
						// Si debug esta activado mostramos toda la
						// informacion
						if (debug) {
							System.out.println("\tEnviado desde: " + myID);
							System.out.println("\tRecibido por: "
									+ destinatario);
							System.out.println("\tMensaje: " + mensaje);
						}

					} catch (ConnectException e) {
						System.err
								.println("\tEl destinatario no esta conectado.");
					}
				} else if (accion.equals("leer")) {
					// Si la accion es "leer" intentaremos leer un mensaje de
					// nuestro buzon (si hay)
					if (ms.buzonVacio()) {
						// Si el buzon esta vacio informamos de que no hay
						// mensajes
						System.out
								.println("\tNo tiene mensajes pendientes de leer.");
					} else {
						// Leemos el siguiente mensaje
						env = ms.receive();
						if (!debug) {
							// Mostramos el mensaje
							System.out.println("\tMensaje: "
									+ env.getPayload().toString());
						} else {
							// Si debug esta activado mostramos todos los campos
							System.out.println("\tEnviado desde: "
									+ env.getSource());
							System.out.println("\tRecibido por: "
									+ env.getDestination());
							System.out.println("\tMensaje: "
									+ env.getPayload().toString());
						}
						System.out.println("\tTiene " + ms.totalEnBuzon()
								+ " mensaje(s) pendientes en el buzon.");
						System.out.println("\tSe han perdido " + ms.perdidos()
								+ " mensajes debido a que no");
						System.out
								.println("\thabia espacio suficiente en el buzon de recepcion.");
					}
				} else if (accion.equals("finalizar")) {
					// Si la accion es finalizar cerramos el buzon y acabamos
					ms.stopMailbox();
					System.out.println("SESION CERRADA.");
					System.exit(0);
				} else {
					// Si la accion es mostrar
					// Mostramos por pantalla la traza de mensajes recibidos
					for (int i=0; i<30; i++) {
						env = ms.receive();
						iDEmisor = env.getSource();
						recibidos[iDEmisor - 1]++;
						System.out.println(env.getPayload());
					}

					// Mostramos por pantalla el numero de mensajes recibidos de
					// cada
					// proceso
					for (int i = 0; i < recibidos.length; i++) {
						System.out.println("\nNum mensajes recibidos de "
								+ (i + 1) + " -> " + recibidos[i]);
					}
					System.out
							.println("\nNo quedan mas mensajes en el buzon, fin de la traza.");
				}
			}
		} catch (BindException e) {
			throw e;
		}
	}
}
