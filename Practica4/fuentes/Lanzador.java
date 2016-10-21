/**
 * AUTOR: Ana Lasheras Mas
 * NIA: 620546
 * AUTOR: Alberto Martinez Menendez
 * NIA: 681061
 * FICHERO: Lanzador.java
 * TIEMPO: 1 hora
 * DESCRIPCION: Clase que lanza la interfaz de entrada de mensajes,
 *               ChatDialog, se encarga de la gestion de eventos 
 *               capturados por el ActionListener, del envio de mensajes
 *               sin bloquear la ventana de chat, y de mostrar los 
 *               mensajes recibidos en la pantalla de chat.
 */

import java.awt.Component;
import java.awt.event.*;
import java.io.FileNotFoundException;
import java.net.BindException;
import java.net.ConnectException;

import javax.swing.SwingUtilities;

public class Lanzador {
	private static ChatDialog chat;
	private static MessageSystem msystem;
	private static TotalOrderMulticast totalOrderM;

	public static void main(boolean debug, int id, String fichero)
			throws FileNotFoundException, BindException {
		msystem = new MessageSystem(id, fichero);
		totalOrderM = new TotalOrderMulticast(msystem);

		chat = new ChatDialog(new ActionListener() {
			// Gestion de eventos al pulsar boton de envio o pulsar enter
			public void actionPerformed(ActionEvent e) {
				final String message = chat.text();

				if (message != "" || message != null) {
					// Gestionar envio de mensajes sin bloquear la
					// ventana de chat
					SwingUtilities.invokeLater(new Runnable() {
						public void run() {
							try {
								totalOrderM.sendMulticast(message);
							} catch (ConnectException e) {
								// e.printStackTrace();

							}
						}
					});
				}
			}
		});

		chat.setTitle("Proceso " + id);

		while (true) {
			Envelope env = null;
			try {
				env = totalOrderM.receiveMulticast();
			} catch (ConnectException e1) {
				e1.printStackTrace();
			}

			// Si el mensaje es que un proceso se ha desconectado, nos
			// desconectamos.
			if ((env.getPayload().toString())
					.contains("FIN DEL CHAT. SE HA DESCONECTADO EL PROCESO ")) {
				msystem.stopMailbox();

			}

			// Si el debug esta activado, ademas del mensaje mostramos la
			// estampilla.
			if (debug) {
				if (env.getSource() == id) {
					chat.addMessage("Yo: (" + env.getEstampilla() + ")  "
							+ env.getPayload());

				} else {
					chat.addMessage("  " + env.getSource() + ": ("
							+ env.getEstampilla() + ")  " + env.getPayload());
				}
			} else {
				if (env.getSource() == id) {
					chat.addMessage("Yo: " + env.getPayload());

				} else {
					chat.addMessage("  " + env.getSource() + ": "
							+ env.getPayload());
				}
			}
		}

	}
}
