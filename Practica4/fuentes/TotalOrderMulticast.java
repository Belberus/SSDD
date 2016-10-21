/**
 * AUTOR: Ana Lasheras Mas
 * NIA: 620546
 * AUTOR: Alberto Martinez Menendez
 * NIA: 681061
 * FICHERO: TotalOrderMulticast.java
 * TIEMPO: 2 horas y media
 * DESCRIPCION: Clase que implementa el envio y recepcion de mensajes
 *               multicast asincronos de forma ordenada, utilizando el
 *               algoritmo de Ricart y Agrawala utilizando estampillas 
 *               temporales asignadas por el sistema de mensajes 
 *               como numero de secuencia.
 */

import java.util.ArrayList;
import java.io.Serializable;
import java.net.ConnectException;
import java.util.concurrent.Semaphore;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class TotalOrderMulticast {
	final int maxMensajes = 50;
	private final int NPARTICIPANTES = 4; // Num. Participantes
	private MessageSystem msystem; // MessageSystem
	private int nAcks = 0; // Numero de Acks
	// Indicador de si queremos o no enviar mensaje
	private boolean quieroEnviar = false;
	private Serializable mssg; // Mensaje
	private Lock mutex = new ReentrantLock(); // Mutex
	// Condicion "ocupado" del mutex
	private Condition ocupado = mutex.newCondition();
	// Array donde se almacenan los req que necesitaran Acks posteriormente
	ArrayList<Envelope> acksEnEspera = new ArrayList<Envelope>(maxMensajes);

	public TotalOrderMulticast(MessageSystem ms) {
		msystem = ms;
	}

	/**
	 * Metodo que se encarga de indicar que el proceso quiere enviar un mensaje
	 * y de solicitar mediante el envio de REQUESTS el permiso para enviarlo.
	 */
	public void sendMulticast(Serializable message) throws ConnectException {
		mutex.lock();

		while (quieroEnviar) {
			try {
				ocupado.await();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}

		mssg = message;
		quieroEnviar = true;

		msystem.sendMulticast(new REQ());

		mutex.unlock();
	}

	/**
	 * Metodo que extrae del buzon un mensaje, posteriormente lo analiza para
	 * saber si se trata de un REQUEST, de un ACK o de un mensaje en sí. Este
	 * metodo se encargara de la gestion de dicho mensaje.
	 */
	public Envelope receiveMulticast() throws ConnectException {
		while (true) {
			Envelope e = msystem.receive();
			if (e.getSource() != msystem.getSource()) {
				if (e.getEstampilla() > msystem.getEstampilla()) {
					msystem.setEstampilla(e.getEstampilla());
				}
			}

			if (!quieroEnviar) {
				if (e.getPayload() instanceof REQ) {
					msystem.send(e.getSource(), new ACK());
				} else {
					return e;
				}
			} else {
				if (e.getPayload() instanceof ACK) {
					nAcks++;
					if (nAcks == (NPARTICIPANTES - 1)) {
						mutex.lock();
						msystem.sendMulticast(mssg);
						// Tras enviar mensaje, actualizar estampilla temporal
						msystem.setEstampilla();
						quieroEnviar = false;
						nAcks = 0;
						while (!acksEnEspera.isEmpty()) {
							Envelope env = acksEnEspera.remove(0);
							msystem.send(env.getSource(), new ACK());

						}
						ocupado.signal();
						mutex.unlock();
					}
				} else if (e.getPayload() instanceof REQ) {
					if (e.getEstampilla() < msystem.getEstampilla()) {
						msystem.send(e.getSource(), new ACK());
					} else if (e.getEstampilla() == msystem.getEstampilla()) {
						if (e.getSource() < e.getDestination()) {
							msystem.send(e.getSource(), new ACK());
						} else {
							acksEnEspera.add(e);
						}
					} else {
						acksEnEspera.add(e);
					}
				} else {
					return e;
				}
			}
		}
	}
}
