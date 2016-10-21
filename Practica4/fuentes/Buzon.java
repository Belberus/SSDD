/**
 * AUTOR: Ana Lasheras Mas
 * NIA: 620546
 * AUTOR: Alberto Martinez Menendez
 * NIA: 681061
 * FICHERO: Buzon.java
 * TIEMPO: 15 minutos
 * DESCRIPCION: Clase que actua como Buzon, almacenando mensajes hasta 
 *               que el usuario pide leer uno.
 */

import java.util.ArrayList;
import java.util.concurrent.Semaphore;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class Buzon {
	final int maxMensajes = 50;
	// Creamos el array de tamano maxMensajes
	ArrayList<Envelope> arrayMensajes = new ArrayList<Envelope>(maxMensajes);

	// Numero de mensajes que no han podido ser guardados en el buzon
	int seSalen = 0;

	/** Candado */
	final Lock mutex = new ReentrantLock();

	/** Condicion buzon vacio */
	final Condition vacio = mutex.newCondition();

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
		mutex.lock();
		if (totalEnBuzon() <= arrayMensajes.size()) {
			arrayMensajes.add(env);
		} else {
			seSalen++;
		}
		vacio.signal();

		mutex.unlock();
	}

	/**
	 * Metodo que devuelve el primer mensaje del array de mensajes.
	 * 
	 * @throws InterruptedException
	 */
	public Envelope sacarMensaje() {
		mutex.lock();

		while (buzonVacio()) {
			try {
				vacio.await();
			} catch (InterruptedException e1) {
				e1.printStackTrace();
			}
		}

		Envelope e = arrayMensajes.remove(0);
		mutex.unlock();
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
