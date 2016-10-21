/*
 * AUTOR: Ana Lasheras Mas
 * NIA: 620546
 * AUTOR: Alberto Martínez Menéndez
 * NIA: 681061
 * FICHERO: DivisorCarga.java
 * TIEMPO: 1 hora
 * DESCRIPCIÓN: Clase que divide el intervalo inicial donde calcular primos
 * 	en intervalos más pequeños permitiendo dividir la carga entre los servidores
 *  de la manera más equitativa posible
 */

import java.lang.Math;

public class DivisorCarga{
	private int min;
	private int max;
	private int n;
	private static int factor;

	/**
	 * Constructor al que se le pasa el intervalo (min,max) que se
	 * tiene que dividir en subintervalos para ajustar la carga de trabajo
	 * entre servidores, y el número n que indica el número de servidores
	 * que utiliza el cliente.
	 */
	public DivisorCarga(int min, int max, int n){
		this.min=min;
		this.max=max;
		this.n=n;
		/* Indica la cantidad de números a colocar en el intervalo, 
		   en un principio. Se reajusta despues si es demasiado grande.*/
		this.factor=500; 
	}

	/**
	 * Si el intervalo a dividir tiene elementos, devuelve tabla 
	 * con dos elementos que representan un intervalo. En la primera 
	 * posición del vector se encontrara el mínimo elemento del 
	 * intervalo, y en el segundo el máximo.
	 * Sino, devuelve null.
	 */
	public synchronized int[] dividir (){
		int [] primos = new int[2];
		if(quedanNumeros()){
			//Si no se puede enviar el número de elementos predefinido al proceso, se cambia ese numero de elementos.
			while(((max-min)/n)<factor){
				factor=factor/2;
			}
			primos[0]=min;
			primos[1]=min+factor;
			this.min+=(factor+1);

			return primos;
		}else return null;
	}

	/**
	 * Devuelve un booleano que indica si quedan números en a true 
	 * el intervalo donde buscar primos. El booleano sera true si quedan
	 * números en el intervalo, sino false.
	 */ 
	private synchronized boolean quedanNumeros(){
		return (min<max);	
	}
}
