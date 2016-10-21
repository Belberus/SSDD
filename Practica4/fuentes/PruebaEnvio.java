/**
 * AUTOR: Ana Lasheras Mas
 * NIA: 620546
 * AUTOR: Alberto Martinez Menendez
 * NIA: 681061
 * FICHERO: Lanzador.java
 * TIEMPO: 1 hora
 * DESCRIPCION: Clase de prueba de trazas que se encarga del envio de 10 mensajes.
 */

import java.awt.event.*;
import java.io.FileNotFoundException;
import java.net.BindException;
import java.net.ConnectException;

import javax.swing.SwingUtilities;

public class PruebaEnvio {
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
				// Para esta prueba de ejecucion, no se permite la interaccion
				// con el boton "enviar" ni con el ENTER en la ventana de chat
			}
		});

		chat.setTitle("Proceso " + id);

		int nMensa = 1;
		Envelope env = null;
		while (true) {
			if (nMensa <= 10) {
				if (env == null || (env != null && env.getSource() == id)) {
					try {
						totalOrderM.sendMulticast("Mensaje numero " + nMensa);
					} catch (ConnectException e2) {
						e2.printStackTrace();
					}
					nMensa++;
				}
			}

			try {
				env = totalOrderM.receiveMulticast();
			} catch (ConnectException e1) {
				e1.printStackTrace();
			}
			if (env.getSource() == id) {
				chat.addMessage("Yo: (" + env.getEstampilla() + ")  "
						+ env.getPayload());

			} else {
				chat.addMessage("  " + env.getSource() + ": ("
						+ env.getEstampilla() + ")  " + env.getPayload());
			}
		}

	}
}
