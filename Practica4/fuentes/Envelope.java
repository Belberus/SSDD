/**
 * AUTOR: Ana Lasheras Mas
 * NIA: 620546
 * AUTOR: Alberto Martinez Menendez
 * NIA: 681061
 * FICHERO: Envelope.java
 * TIEMPO: 5 minutos
 * DESCRIPCION: Clase que implementa la estructura "Envelope" cuyo 
 *               contenido seran los datos enviados como mensaje 
 *               (emisor, destinatario y contenido). 
 */

import java.io.Serializable;

public class Envelope implements Serializable {
	private static final long serialVersionUID = 1L;
	private int source; // Emisor
	private int destination; // Destinatario
	private Serializable payload; // Contenido
	private int estampilla; // Estampilla temporal

	/**
	 * Metodo constructor de la clase envelope.
	 */
	public Envelope(int s, int d, Serializable p, int e) {
		source = s;
		destination = d;
		payload = p;
		estampilla = e;
	}

	/**
	 * Metodo que devuelve el emisor del mensaje.
	 */
	public int getSource() {
		return source;
	}

	/**
	 * Metodo que devuelve el destinatario del mensaje.
	 */
	public int getDestination() {
		return destination;
	}

	/**
	 * Metodo que devuelve el contenido del mensaje.
	 */
	public Serializable getPayload() {
		return payload;
	}

	/**
	 * Metodo que devuelve la estampilla temporal del mensaje.
	 */
	public int getEstampilla() {
		return estampilla;
	}
}
