/*
 * AUTOR: Ana Lasheras Mas
 * NIA: 620546
 * AUTOR: Alberto Martínez Menéndez
 * NIA: 681061
 * FICHERO: Start.java
 * TIEMPO: 15 minutos
 * DESCRIPCIÓN: Clase que dependiendo de los argumentos introducidos lanza 
 *  una clase servidor de calculo, servidor de asignación o un cliente.
 */

import java.lang.ArrayIndexOutOfBoundsException;

public class Start{

	public static void main(String[] args) {
		String mensajeError = "\nIntroduzca: -c [IP_registro] para lanzar servidor de calculo"+
				"\n            -a [IP_registro] para lanzar servidor de asignacion"+
				"\n            -u min max n [IP_registro] para lanzar cliente\n";
		try{		
			String opcion=args[0];
			if(opcion.equals("-c")){
				//Si el parametro introducido es -c se lanza el servidor de calculo
				WorkerServer w;
				if (args.length==1){
					//Indica que la IP  a usar por el WorkerServer es localhost
					w = new WorkerServer("localhost");
				}else{
					//Indica que la IP  a usar por el WorkerServer esta en el parametro 1
					w = new WorkerServer(args[1]);
				}
				w.registrar();
			}else if(opcion.equals("-a")){
				//Si el parametro introducido es -a se lanza el servidor de asignacion
				WorkerFactoryServer wf;
				if(args.length==1){
					//Indica que la IP  a usar por el WorkerFactoryServer es localhost
					wf= new WorkerFactoryServer("localhost");
				}else{
					//Indica que la IP  a usar por el WorkerFactoryServer esta en el parametro 1
					wf = new WorkerFactoryServer(args[1]);
				}
				wf.registrar();
			}else if(opcion.equals("-u")){
				//Si el parametro introducido es -u se lanza el cliente
				int min = Integer.parseInt(args[1]);
				int max = Integer.parseInt(args[2]);
				int n = Integer.parseInt(args[3]);

				Cliente c;
				if(args.length==4){
					//Indica que la IP  a usar por el Cliente es localhost, y los parametros min, max y n indicados en los args 1-3
					c=new Cliente("localhost",min,max,n);
				}else{
					//Indica que la IP  a usar por el Cliente es la indicada en el parametro 4, y los parametros min, max y n indicados en los args 1-3
					c=new Cliente(args[4],min,max,n);
				}
				c.start();
			}else{				
				//Si no se ha introducido una opcion de servidor correcta, se informa de las posibilidades
				System.out.println(mensajeError);
			}
		}catch(NumberFormatException e){
			//Si no se ha introducido una opcion de servidor correcta, se informa de las posibilidades
			System.out.println(mensajeError);
		}
		catch (ArrayIndexOutOfBoundsException e){
			//Si no se ha introducido una opcion de servidor correcta, se informa de las posibilidades
			System.out.println(mensajeError);
		}	
	}
}
