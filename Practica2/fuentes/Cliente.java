/*
 * AUTOR: Ana Lasheras Mas
 * NIA: 620546
 * AUTOR: Alberto Martínez Menéndez
 * NIA: 681061
 * FICHERO: Cliente.java
 * TIEMPO: 2 hora
 * DESCRIPCIÓN: Clase que se encarga de obtener los servidores de cálculo
 *  pedidos al crear el cliente, o los máximo posibles, y que lanza tantos clientes
 *  como servidores de cálculo se han solicitado para calculas los primos.
 *  Tras obtener los primos, se muestran por pantalla.
 */

import java.rmi.server.*;
import java.util.ArrayList;
import java.rmi.RemoteException;
import java.rmi.NotBoundException;
import java.lang.*;
import java.rmi.registry.*;
import java.util.Collections;

public class Cliente {

	private String IP;
	private int min;
	private int max;
	private int n;

	/**
	 * Constructor del objeto Cliente, donde se le indica la 
	 * IP donde se encuentra el servidor rmi, el valor mínimo (min) y 
	 * máximo (max) del intervalo de números donde buscar primos, y el
	 * número de servidores de cálculo a utilizar.
	 */	
	public Cliente(String IP, int min, int max, int n){
		this.IP=IP;
		this.min=min;
		this.max=max;
		this.n=n;
	}

	/**
	 * Función que obtiene los servidores de cálculo solicitados, a 
	 * partir de servidor de asignación y tras ello crea un objeto 
	 * DivisorCarga que utilizaran todos los procesos servidor lanzados
	 * como hilos para recorrer el intervalo donde buscar primos. 
	 * Finalmente, muestra los primos obtenidos como resultado.
	 */ 
	public void start () {
		ArrayList<Worker> workers = new ArrayList<Worker>();
		try{
			Registry registry = LocateRegistry.getRegistry(IP);	

			//Obtenemos servidor de asignacion
			WorkerFactory mir = (WorkerFactory) registry.lookup("//"+IP+":WorkerFactoryServer");

			//Llamando a su metodo obtenemos el array de workers
			workers = mir.dameWorkers(n); 
		}catch(RemoteException e){
			System.out.println(e);
		}catch(NotBoundException e){
			System.out.println(e);	
		}

		/*Crea un objeto de tipo Divisorarga, que se encargara de dividir el intervalo en subintervaloes 
		   para balancear la carga de trabajo entre servidores de cálculo*/
		DivisorCarga divisor = new DivisorCarga(min,max,n);

		//Creamos los threads para que los numeros primos se busquen por tantos servidores como indica n		
		ThreadCalculo [] t = new ThreadCalculo [workers.size()];
		Thread [] thread = new Thread[workers.size()];

		for (int i =0; i<workers.size(); i++){
			//Lanzamos un thread de cada encuentraPrimos con su respectivo intervalo
			t[i]= new ThreadCalculo(divisor,workers.get(i));
			thread[i] = new Thread(t[i]);
			System.out.println("Iniciando calculo de divisores por parte del worker "+i);
			thread[i].start();
		}

		for (int i =0; i<workers.size(); i++){
			try{
				thread[i].join();
			}catch(InterruptedException e){
				System.out.println(e);
			}
		}	
		mostrarPrimos(t);
	}

	/**
	 * Muestra los números primos calculados por pantalla.
	 */
	private static void mostrarPrimos (ThreadCalculo [] t){
		ArrayList<Integer> primos = ordenarPrimos(t);
		for(int j=0;j<primos.size();j++){
			System.out.printf("%d  ", primos.get(j));
		}
	}

	/**
	 * Ordena en un arrayList de mayor a menor los primos obtenidos.
	 */
	private static ArrayList<Integer> ordenarPrimos (ThreadCalculo [] t){
		ArrayList<Integer> primos = new ArrayList<Integer>(); 
		for (int i=0; i<t.length;i++){
			primos.addAll(t[i].getPrimos());	
		}
		Collections.sort(primos);
		return primos;
	}
}
