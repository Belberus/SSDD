/*
 * AUTOR: Ana Lasheras Mas
 * NIA: 620546
 * AUTOR: Alberto Martínez Menéndez
 * NIA: 681061
 * FICHERO: WorkerFactory.java
 * TIEMPO: 1 minuto
 * DESCRIPCIÓN: Interfaz que define el método dameWorkers. 
 */

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.ArrayList;

/*
 * Interface con los métodos que queremos que se puedan llamar remotamente.
 */
public interface WorkerFactory extends Remote {
	// Devuelve un vector de hasta n referencias a objetos Worker.
	ArrayList<Worker> dameWorkers(int n) throws RemoteException;
}

