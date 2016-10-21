/*
 * AUTOR: Ana Lasheras Mas
 * NIA: 620546
 * AUTOR: Alberto Martínez Menéndez
 * NIA: 681061
 * FICHERO: WorkerFactoryServer.java
 * TIEMPO: 1 hora
 * DESCRIPCIÓN: Clase que implementa las operaciones de la interfaz WorkerFactory,
 *  permitiendo que los clientes invoquen la función dameWorkers. 
 *  Tambien permite registar un nuevo servidor de tipo asignación mediante rmi.
 */

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.rmi.NotBoundException;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.rmi.*;
import java.rmi.server.*;
import java.rmi.registry.*;
import java.net.MalformedURLException;

public class WorkerFactoryServer implements WorkerFactory {

	private String IP;

	/**
	 * Constructor por defecto del objeto WorkerFactoryServer.
	 */
	public WorkerFactoryServer(){}

	/**
	 * Constructor del objeto WorkerFactoryServer, donde se le indica la 
	 * IP donde se encuentra el servidor rmi.
	 */
	public WorkerFactoryServer(String IP){
		this.IP=IP;
	}

	// Devuelve un vector de hasta n referencias a objetos Worker.
	public ArrayList<Worker> dameWorkers(int n) throws RemoteException{
		ArrayList<Worker> workers = new ArrayList<Worker>();
		try{
			Registry registry = LocateRegistry.getRegistry(IP);
			String [] nameWorker = registry.list();

			ArrayList<String> servidores = new ArrayList<String>();
			for (int i=0; i<nameWorker.length;i++) {
				if (nameWorker[i].contains("WorkerServer")) {
					servidores.add(nameWorker[i]);
				}
			}

			for (int i=0;i<servidores.size() && i<n;i++){
				workers.add((Worker) registry.lookup(servidores.get(i)));
			}
		}catch(NotBoundException e){
			System.out.println(e);
		}
		return workers;	
	}


	/**
	 * Función que se encarga de registrar un servidor de asignación en 
	 * el registro rmi.
	 */
	public void registrar(){
		try{	
			Registry registry = LocateRegistry.getRegistry(IP);

			WorkerFactory  mir = new WorkerFactoryServer();
			WorkerFactory stub = (WorkerFactory) UnicastRemoteObject.exportObject(mir,0);
			registry.rebind("//"+IP+":WorkerFactoryServer",stub);
			System.out.println("Servidor WorkerFactory registrado correctamente.");
		}catch (RemoteException e){
			System.out.println(e);
		}
	}
}
