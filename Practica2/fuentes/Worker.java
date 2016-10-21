/*
 * AUTOR: Ana Lasheras Mas
 * NIA: 620546
 * AUTOR: Alberto Martínez Menéndez
 * NIA: 681061
 * FICHERO: Worker.java
 * TIEMPO: 1 minuto
 * DESCRIPCIÓN: Interfaz que define el método encuentraPrimos. 
 */

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.ArrayList;

/*
 * Interface con los métodos que queremos que se puedan llamar remotamente.
 */
public interface Worker extends Remote {
	// Devuelve un vector con los primos entre min y max.
	ArrayList<Integer> encuentraPrimos(int min, int max) throws RemoteException;
}

