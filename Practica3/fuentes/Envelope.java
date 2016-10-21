/*
 * AUTOR: Ana Lasheras Mas
 * NIA: 620546
 * AUTOR: Alberto Martínez Menéndez
 * NIA: 681061
 * FICHERO: Envelope.java
 * TIEMPO: 0 (ya estaba hecho)
 * DESCRIPCIÓN: Clase que implementa la estructura "Envelope" cuyo contenido seran
 * los datos enviados como mensaje (emisor, destinatario y contenido). 
 */
import java.io.Serializable;

public class Envelope implements Serializable {
	private static final long serialVersionUID = 1L;
	private int source;
	private int destination;
	private Serializable payload;

	public Envelope(int s, int d, Serializable p) {
		source = s;
		destination = d;
		payload = p;
	}

	public int getSource() {
		return source;
	}

	public int getDestination() {
		return destination;
	}

	public Serializable getPayload() {
		return payload;
	}
}
