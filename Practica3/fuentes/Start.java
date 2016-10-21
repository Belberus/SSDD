/*
 * AUTOR: Ana Lasheras Mas
 * NIA: 620546
 * AUTOR: Alberto Mart�nez Men�ndez
 * NIA: 681061
 * FICHERO: Start.java
 * TIEMPO: 30 minutos
 * DESCRIPCI�N: Clase lanzadora de maquinas.
 */
import java.io.FileNotFoundException;
import java.net.BindException;

public class Start {
	public static void main(String[] args) throws FileNotFoundException {
		String mensajeError = "\nIntroduzca: [-d] identificador fichero"
				+ "\n			   -d: activa modo Debug en esa maquina."
				+ "\n			   identificador: N� de maquina (1,2,3,4)."
				+ "\n			   fichero: fichero.txt con los identificadores y los puertos de las maquinas."
				+ "\n			 Para probar las trazas de ejecucion: "
				+ "\n			    Receptor de mensajes: [-d] 0 fichero "
				+ "\n			    Lanzamos el Script: ./pruebaTrazas.sh";

		if (args.length < 2) {
			System.out.println(mensajeError);
		} else if (args.length == 2) {
			int iD = Integer.parseInt(args[0]);
			String fichero = args[1];
			try {
				Maquina.main(false, iD, fichero);
			} catch (BindException e) {
				System.err
						.println("\tSeleccione otra m�quina, la seleccionada ya se encuentra en ejecuci�n.");
			}
		} else if (args.length == 3) {
			if (args[0].equals("-d")) {
				int iD = Integer.parseInt(args[1]);
				String fichero = args[2];
				try {
					Maquina.main(true, iD, fichero);
				} catch (BindException e) {
					System.err
							.println("\tSeleccione otra m�quina, la seleccionada ya se encuentra en ejecuci�n.");
				}
			} else if (args[0].equals("-t")) {
				int iD = Integer.parseInt(args[1]);
				String fichero = args[2];
				try {
					MaquinaEmisora.main(iD, fichero);
				} catch (BindException e) {
					System.err
							.println("\tSeleccione otra m�quina, la seleccionada ya se encuentra en ejecuci�n.");
				}
			} else {
				System.out.println(mensajeError);
			}
		} else {
			System.out.println(mensajeError);
		}
	}
}
