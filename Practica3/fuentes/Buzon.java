/*
 * AUTOR: Ana Lasheras Mas
 * NIA: 620546
 * AUTOR: Alberto Martínez Menéndez
 * NIA: 681061
 * FICHERO: Buzon.java
 * TIEMPO: 10 minutos
 * DESCRIPCIÓN: Clase que actua como Buzon, almacenando mensajes hasta que el
 * 				usuario pide leer uno.
 */
import java.util.ArrayList;
import java.util.concurrent.Semaphore;

public class Buzon {
	// Creamos el array de 100 mensajes, inicialmente
	ArrayList<Envelope> arrayMensajes = new ArrayList<Envelope>(100);
	// Semaforo de control de acceso al buzon
	Semaphore acceso = new Semaphore(1);
	// Numero de mensajes que no han podido ser guardados en el buzon
	int seSalen = 0;

	/**
	 * Constructor de la clase Buzon.
	 */
	public Buzon() {
	}

	/**
	 * Metodo que guarda en el array de mensajes el mensaje representado por
	 * "env".
	 * 
	 * @throws InterruptedException
	 */
	public void guardarMensaje(Envelope env) {
		try {
			acceso.acquire();
		} catch (InterruptedException e) {
		}
		if (totalEnBuzon() <= 100) {
			arrayMensajes.add(env);
		} else {
			seSalen++;
		}
		acceso.release();
	}

	/**
	 * Metodo que devuelve el primer mensaje del array de mensajes.
	 * 
	 * @throws InterruptedException
	 */
	public Envelope sacarMensaje() {
		try {
			acceso.acquire();
		} catch (InterruptedException e1) {
		}
		Envelope e = arrayMensajes.remove(0);
		acceso.release();
		return e;

	}

	/**
	 * Metodo que devuelve true si el buzon esta vacio, false si tiene al menos
	 * un mensaje.
	 */
	public boolean buzonVacio() {
		return arrayMensajes.isEmpty();
	}

	/**
	 * Metodo que devuelve el numero de mensaje contenidos en el array de
	 * mensajes.
	 */
	public int totalEnBuzon() {
		return arrayMensajes.size();
	}

	/**
	 * Metodo que devuelve el numero de mensajes que se han perdido al no haber
	 * espacio suficiente en el array de mensajes.
	 */
	public int mensajesPerdidos() {
		return seSalen;
	}
}
