/*
 * AUTOR: Ana Lasheras Mas
 * NIA: 620546
 * AUTOR: Alberto Martínez Menéndez
 * NIA: 681061
 * FICHERO: ThreadCalculo.java
 * TIEMPO: 1 hora
 * DESCRIPCIÓN: Clase sobre la que se ejecutan los hilos de ejecución creados 
 *  por el cliente y donde se calculan los primos del intervalo obtenido en la función.
 */

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.lang.*;

public class ThreadCalculo implements Runnable{

	private ArrayList<Integer> primos;
	private int[] intervalo;
	private Worker worker;
	private DivisorCarga divisor;

	/**
	 * Constructor del objeto ThredCalculo al que se le pasa un divisor 
	 * de carga y el worker (servidor cálculo) donde buscar primos.
	 * Crea una tabla donde se almacenara el intervalo donde buscar, 
	 * devuelto por el divisor de carga, y un arraylist donde se almacenaran
	 * los números primos encontrados.
	 */
	public ThreadCalculo(DivisorCarga divisor, Worker w){
		this.divisor=divisor;
		this.worker = w;
		this.intervalo = new int[2];
		this.primos  = new ArrayList<Integer>();
	}

	/**
	 * Busca un intervalo de números donde buscar primos e invoca
	 * el método que busca esos números primos. Esta operación se repite
	 * mientras haya números dentro del intervalo indicado por el 
	 * cliente para buscar primos.
	 * 
	 * El tiempo de ejecución de esta busqueda de primos se mide y se 
	 * muestra por pantalla permitiendo comprobar si la división de 
	 * trabajo entre servidores es la correcta.
	 */
	public void run (){
		//LLama al metodo encuentraPrimos del worker y guarda ese arrayList
		try{
			long inicio=System.currentTimeMillis();
			while((intervalo = divisor.dividir()) != null) {
				primos.addAll(worker.encuentraPrimos(intervalo[0],intervalo[1]));
			}
			long fin=System.currentTimeMillis();
			System.out.println("Tiempo de : " + (fin - inicio)+"milliseconds");		
		}catch(RemoteException e){
			System.out.println(e);
		}
	}

	/**
	 * Devuelve un ArrayList de enteros donde se encuentan los 
	 * números primos encontrados. 
	 */
	public ArrayList<Integer> getPrimos(){
		return primos;
	}
}
