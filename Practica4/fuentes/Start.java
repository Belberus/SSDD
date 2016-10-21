/**
 * AUTOR: Ana Lasheras Mas
 * NIA: 620546
 * AUTOR: Alberto Martinez Menendez
 * NIA: 681061
 * FICHERO: Start.java
 * TIEMPO: 30 minutos
 * DESCRIPCION: Clase lanzadora de ventanas de chat.
 */

import java.io.FileNotFoundException;
import java.net.BindException;

public class Start {
	public static void main(String[] args) throws FileNotFoundException {
		String mensajeError = "\nPara chat interactivo introduzca: [-d] identificador fichero"
				+ "\n            -d: activa modo Debug en esa maquina."
				+ "\n            identificador: N de maquina (1,2,3,4)."
				+ "\n            fichero: fichero txt con formato (id:puerto:direccion)"
				+ "\n\nPara prueba de no entrelazado introduzca: -p";

		if (args.length < 2) {
			System.out.println(mensajeError);
		} else if (args.length == 2) {
			int iD = Integer.parseInt(args[0]);
			String fichero = args[1];
			try {
				Lanzador.main(false, iD, fichero);
			} catch (BindException e) {
				System.err
						.println("\tSeleccione otra maquina, la seleccionada ya se encuentra en ejecucion.");
			}
		} else if (args.length == 3) {
			int iD = Integer.parseInt(args[1]);
			String fichero = args[2];
			if (args[0].equals("-d")) {
				try {
					Lanzador.main(true, iD, fichero);
				} catch (BindException e) {
					System.err
							.println("\tSeleccione otra maquina, la seleccionada ya se encuentra en ejecucion.");
				}
			} else if (args[0].equals("-p")) {
				try {
					PruebaEnvio.main(true, iD, fichero);
				} catch (BindException e) {
					System.err
							.println("\tSeleccione otra maquina, la seleccionada ya se encuentra en ejecucion.");
				}
			} else
				System.out.println(mensajeError);
		} else
			System.out.println(mensajeError);
	}
}
