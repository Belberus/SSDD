package practica1;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.Charset;

public class ServidorThreads {
	static DataInputStream entrada=null;
	public static void main(String[] args) {	
		int SERVER_PORT =  Integer.parseInt(args[0]);
	    
		// El servidor escuchará en local
		// en el puerto SERVER_PORT (>= 1024)
		ServerSocket serverSocket = null; //para escuchar
		Socket clientSocket = null;       //uno por cliente

		// Inicializar el socket del cliente con el que se va
		// a comunicar el servidor, es decir se acepta la
		// conexión de un cliente al servidor mediante
		// el método accept()
		serverSocket = creaListenSocket(SERVER_PORT);

		int leido;
		String cad="";
 		byte  bytedata[]=new byte[256];

		// Bucle que crea una conexion y un thread que trate su peticion 
		// por cliente
		while(true){
			try{
				clientSocket = creaClientSocket(serverSocket);
				entrada=new DataInputStream(clientSocket.getInputStream());

				Thread t = new Thread(new Tratamiento(clientSocket, entrada));
				t.start();
			}catch(IOException ex){}	
		}
	}
	
	//Crea un socket de servidor
	//Aborta programa si no lo logra
	private static ServerSocket creaListenSocket(int serverSockNum){
		ServerSocket server = null;

		try{
    		server = new ServerSocket(serverSockNum);
  		} catch (IOException e) {
   			System.err.println("Problems in port: " + 
			                         serverSockNum);
   			System.exit(-1);
   		}
   		return server;
  	}

  	//Establece conexión con server y devuelve socket
  	//Aborta programa si no lo logra
	private static Socket creaClientSocket(ServerSocket server){
  		Socket res = null;

  		try {
			res = server.accept();
		} catch (IOException e) {
			System.err.println("Accept failed.");
			System.exit(1);
		}
		return res;
  	}
}
	
	
	
	
	

