package practica1;

import java.io.*;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.channels.spi.SelectorProvider;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class ServidorSelect {
	public static void main(String[] args) throws IOException {
		//Leo puerto en el que escuchara el servidor
		int SERVER_PORT =  Integer.parseInt(args[0]);
		ByteBuffer buffer = ByteBuffer.allocate(1024);
		//Tabla donde se almacenan los canales de sockets existentes
		ConcurrentHashMap<Integer, SocketChannel> tablaSocketChannel = new ConcurrentHashMap<Integer, SocketChannel>();

		//Creamos un nuevo selector
		Selector selector = Selector.open();
			
		//Abrimos un canal de escucha en el puerto
		ServerSocketChannel serverSocketChannel = ServerSocketChannel.open();
		//Configuramos que el canal no sea bloqueante
		serverSocketChannel.configureBlocking(false);
		//Creamos un server socket sobre el canal de escucha ligado al puerto introducido
		ServerSocket serverSocket = serverSocketChannel.socket();            
		InetSocketAddress address = new InetSocketAddress(SERVER_PORT);   
		serverSocket.bind(address);
		//Asignamos al canal de escucha el selector, preparandolo para aceptar peticiones
		serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);
		
		//Comenzamos el bucle de escucha	
		while(true){
			//El selector obtiene las claves de los canales preparados para operaciones de E/S
			selector.select();
			//Añades las claves obtenidas por el selector
            Set<SelectionKey> selectedKeys = selector.selectedKeys();
            //Obtienes la siguiente clave a analizar
            Iterator<SelectionKey> it = selectedKeys.iterator();          
            //String msg = new String();
              System.out.println(it.hasNext());
            //Mientras el iterador tenga una o mas claves que analizar (hay algun cliente) continuamos tratando claves
            while (it.hasNext()) {
				//Elegimos una clave
                SelectionKey key = (SelectionKey) it.next();
                System.out.println(key);
                //Entramos si el cliente pide conexion
                if ((key.readyOps() & SelectionKey.OP_ACCEPT) == SelectionKey.OP_ACCEPT) {
                    //Acepta la nueva conexion
                    ServerSocketChannel newServerSocketChannel = (ServerSocketChannel) key.channel();
                    SocketChannel socketChannel = newServerSocketChannel.accept();
                    socketChannel.configureBlocking(false);
                    //Añade la nueva conexion al selector, esta vez de lectura                  
                    socketChannel.register(selector, SelectionKey.OP_READ);
                    //Añade el socketchanel a la lista
                    tablaSocketChannel.put(socketChannel.hashCode(), socketChannel);
                    //Elimina la conexion de aceptacion, porque el cliente ya ha sido aceptado por el servidor
                    it.remove();
                }
                //Si la operacion a analizar es de lectura
                else if ((key.readyOps() & SelectionKey.OP_READ) == SelectionKey.OP_READ){
                    //Lee los datos
                    SocketChannel socketChannel = (SocketChannel) key.channel();
                    Socket socketCliente = socketChannel.socket();
                    try{
						int bytes=0;
						while((bytes=socketChannel.read(buffer))!=0);
						TratamientoSelect t= new TratamientoSelect(socketCliente,buffer);
						t.tratar();
					}catch(IOException e){}           
                    tablaSocketChannel.remove(socketChannel.hashCode());
                    socketChannel.close();                            
                }
            }
		}
	}
}
