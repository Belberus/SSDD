/*
 * AUTOR: Ana Lasheras Mas
 * NIA: 620546
 * AUTOR: Alberto Martínez Menéndez
 * NIA: 681061
 * FICHERO: WorkerServer.java
 * TIEMPO: 1 hora
 * DESCRIPCIÓN: Clase que implementa las operaciones de la interfaz Worker,
 *  permitiendo que los clientes invoquen la función encuentraPrimos. 
 *  Tambien permite registar un nuevo servidor de tipo cálculo mediante rmi.
 */

import java.util.ArrayList;
import java.lang.Math;
import java.rmi.*;
import java.rmi.server.UnicastRemoteObject;
import java.rmi.RemoteException;
import java.rmi.registry.*;

public class WorkerServer implements Worker{

	private String IP;

	/**
	 * Constructor por defecto del objeto WorkerServer.
	 */
	public WorkerServer(){}

	/**
	 * Constructor del objeto WorkerServer, donde se le indica la IP
	 * donde se encuentra el servidor rmi.
	 */
	public WorkerServer(String IP){
		this.IP=IP;
	}

	/**
	 * Función que se encarga de encontrar y devolver en un arraylist 
	 * de enteros los números primos que se encuentran en el intervalo 
	 * introducido como parámetros.
	 */
	public ArrayList<Integer> encuentraPrimos(int min, int max) throws RemoteException{
		int i=2;
		int n=min;
		boolean divisorEncontrado=false;
		ArrayList<Integer> primos = new ArrayList<Integer>();

		while(n<=max){
			while(i<=(int) Math.sqrt(n) && !divisorEncontrado){
				divisorEncontrado=(n%i==0);
				i++;
			}
			if(!divisorEncontrado){
				primos.add(n);
			}
			n++;
			i=2;
			divisorEncontrado=false;
		}
		return primos;
	}

	/**
	 * Función que se encarga de registrar un servidor de cálculo en el
	 * registro rmi, el registro se realiza con un nombre distinto a los
	 * servidores de cálculo ya resgistrados anteriormente.
	 */
	public void registrar(){
		try{	
			Registry registry = LocateRegistry.getRegistry(IP);	

			Worker  mir = new WorkerServer();
			Worker stub = (Worker) UnicastRemoteObject.exportObject(mir,0);
			String [] nombres = registry.list();
			int servidores=0;
			for (int i=0; i<nombres.length;i++) {
				if (nombres[i].contains("WorkerServer")) {
					servidores++;
				}
			}
			registry.rebind("//"+IP+":WorkerServer"+servidores,stub);
			System.out.println("Servidor Worker"+servidores+" registrado correctamente.");
		}catch (RemoteException e){
			System.out.println(e);
		}
	}
}
