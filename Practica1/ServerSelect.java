package practica1;

import java.io.*;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.channels.spi.SelectorProvider;
import java.nio.charset.Charset;
import java.util.Iterator;
import java.util.Scanner;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class ServerSelect {
	private static Charset charset = Charset.forName("ISO-8859-1");
	private static HTTPParser bParser = new HTTPParser();
	private static ByteBuffer buffer = ByteBuffer.allocate(1024);
	
	public static void main(String[] args) throws IOException {
		//Leo puerto en el que escuchara el servidor
		int SERVER_PORT =  Integer.parseInt(args[0]);	
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
		serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT,bParser);
		
		//Comenzamos el bucle de escucha	
		while(true){
			//El selector obtiene las claves de los canales preparados para operaciones de E/S
			selector.select();
			//Añades las claves obtenidas por el selector
            Set<SelectionKey> selectedKeys = selector.selectedKeys();
            //Obtienes la siguiente clave a analizar
            Iterator<SelectionKey> it = selectedKeys.iterator();          
            //String msg = new String();
            //Mientras el iterador tenga una o mas claves que analizar (hay algun cliente) continuamos tratando claves
              	while (it.hasNext()) {
              		//Elegimos una clave
                    SelectionKey key = (SelectionKey) it.next();
                    //Entramos si el cliente pide conexion
                    if ((key.readyOps() & SelectionKey.OP_ACCEPT) == SelectionKey.OP_ACCEPT) {
                        //Acepta la nueva conexion
                        ServerSocketChannel newServerSocketChannel = (ServerSocketChannel) key.channel();
                        SocketChannel socketChannel = newServerSocketChannel.accept();
                        socketChannel.configureBlocking(false);
                        //Añade la nueva conexion al selector, esta vez de lectura                  
                        socketChannel.register(selector, SelectionKey.OP_READ,bParser);
                        //Añade el socketchanel a la lista
                        tablaSocketChannel.put(socketChannel.hashCode(), socketChannel);
                        //Elimina la conexion de aceptacion, porque el cliente ya ha sido aceptado por el servidor
                        it.remove();
                    }
                    //Si la operacion a analizar es de lectura
                    else if ((key.readyOps() & SelectionKey.OP_READ) == SelectionKey.OP_READ){
                        //Lee los datos
                        SocketChannel socketChannel = (SocketChannel) key.channel();
                        try{
    						int bytes=0;
    						bytes=socketChannel.read(buffer);
    						buffer.flip();
    						bParser.parseRequest(buffer);
    						//Si ya hemos leido todo el buffer
    						if(bytes == 0 || bytes == -1){
    							//Añade la nueva conexion al selector, esta vez de escritura 
    							socketChannel.register(selector, SelectionKey.OP_WRITE,bParser);
    							//Añade el socketchanel a la lista
    	                        tablaSocketChannel.put(socketChannel.hashCode(), socketChannel);
    	                        //Elimina la conexion de lectura ya que el cliente ya ha terminado de leer
    	                        it.remove();		
    						}
    					}catch(IOException e){}           
                        tablaSocketChannel.remove(socketChannel.hashCode());
                        socketChannel.close();          		
                    }
                    //Si la operacion a analizar es escritura
                    
                    else if ((key.readyOps() & SelectionKey.OP_WRITE) == SelectionKey.OP_WRITE){
                    	 SocketChannel socketChannel = (SocketChannel) key.channel();
                    	try{
                    		if (bParser.failed()){
                				//Devuelve 400 Bad Request
                    			CharBuffer cbuf = buffer.asCharBuffer();
                    			cbuf.put("HTTP/1.1 400 Bad Request\nContent-Type: text/html\nContent-Length: 94\n");
                    			buffer = charset.encode(cbuf);
                    			socketChannel.write(buffer);
                    			if(buffer.toString().length() == 0 || buffer.toString().length() == -1){
                    				cbuf.put("<html><head><title>404 Not Found</title></head><body><h1>Not Found</h1></body></html>");
                        			buffer = charset.encode(cbuf);
                        			socketChannel.write(buffer);
                        			if(buffer.toString().length() == 0 || buffer.toString().length() == -1){
                        				it.remove();			
                        				socketChannel.close();
                        			}		   
                    			}
                    			             			        				
                			}
                    		else if (bParser.getMethod().contains("GET")){
                				//LLamamos a metodo get
                				get(bParser.getPath(),socketChannel,it);
                			}
                			else if (bParser.getMethod().contains("POST")){
                				//Llamamos a post
                				post(new String((bParser.getBody()).array()),socketChannel,it);
                			}
                			else{
                				//devuelve 501 Not Implemented
                				CharBuffer cbuf = buffer.asCharBuffer();
                    			cbuf.put("HTTP/1.1 501 Not Implemented\nContent-Type: text/html\nContent-Length: 90\n");
                    			buffer = charset.encode(cbuf);
                    			socketChannel.write(buffer); 
                    			if(buffer.toString().length() == 0 || buffer.toString().length() == -1){
                    				cbuf.put("<html><head><title>501 Not Implemented</title></head><body><h1>Not Found</h1></body></html>");
                        			buffer = charset.encode(cbuf);
                        			socketChannel.write(buffer);
                        			if(buffer.toString().length() == 0 || buffer.toString().length() == -1){
                        				it.remove();			
                        				socketChannel.close();
                        			}
                    			}		   
                    			
                			}                 		
                		}catch(IOException ex){} 	
                    } 
             
              	}
		}
	
	}
	
	/**  
	 * Mirar si el fichero introducido como parametro existe en el directorio actual.
	 *
	 * Si existe: Devuelve el codigo 200 OK y el contenido del fichero en el cuerpo del mensaje. 
	 *      				Mostramos en la cabecera Content Length (obligatorio) y content type.
     *
	 * Si existe pero la ruta incluye algun directorio: Devuelve 403 FORBIDDEN.
     *
	 * Si no existe: devuelve error 404 NOT FOUND.
	 * 
	 */
	private static void get(String fichero, SocketChannel socketChannel, Iterator<SelectionKey> it){
		fichero = fichero.substring(1, fichero.length()); //Elimina el directorio raiz, primera '/' del fichero
		FileInputStream f = null; 
		boolean existe = true; 
		byte[] buffer1 = new byte[1024]; 
		int bytes;
		//Crea un nuevo fichero si existe, sino indica que no existe asignando all booleano existe el valor false
		try{
   		f = new FileInputStream(fichero);
		}
		catch (FileNotFoundException e){
			existe = false; 
		}
		try{
			if (existe){
				int ficheroLength=f.available(); //Obtiene el numero de bits aproximados del fichero
				if(fichero.indexOf("/") == -1){
					//Lee fichero a enviar
					try{
					bytes = f.read(buffer1);
					if(bytes == 0 || bytes == -1){
					//ya hemos leido todo el fichero						
					}
					}catch(ArrayIndexOutOfBoundsException e){
						//MIRAR SI HAY ERROR PARA FICHERO MUY GRANDE
						CharBuffer cbuf = buffer.asCharBuffer();
            			cbuf.put("HTTP/1.1 403 Forbidden\nContent-Type: text/html\nContent-Length: 90\n");
            			buffer = charset.encode(cbuf);
            			socketChannel.write(buffer);
            			if(buffer.toString().length() == 0 || buffer.toString().length() == -1){
            				cbuf.put("<html><head><title>FICHERO MUY GRANDE</title></head><body><h1>EL FICHERO INTRODUCIDO ES DEMASIADO GRANDE</h1></body></html>");
                			buffer = charset.encode(cbuf);
                			socketChannel.write(buffer);
                			if(buffer.toString().length() == 0 || buffer.toString().length() == -1){
                				it.remove();			
                				socketChannel.close();
                			}
            			}
            			
					}
					
					f.close(); //Cierra FileInputStream
					String tipo=fichero.substring(fichero.lastIndexOf('.'));
					if(tipo.equals(".html")){
						tipo="html";
					}else{
						tipo="plain";
					}
					//Envia mensaje OK si el fichero existe y esta dentro del directorio no tiene '/' en su ruta
					CharBuffer cbuf = buffer.asCharBuffer();
        			cbuf.put("HTTP/1.1 200 OK\nContent-Type: text/"+tipo+"\nContent-Length: "+ficheroLength+"\n");
        			buffer = charset.encode(cbuf);
        			socketChannel.write(buffer);
        			cbuf.put("<html><head><title>FICHERO MUY GRANDE</title></head><body><h1>EL FICHERO INTRODUCIDO ES DEMASIADO GRANDE</h1></body></html>");
        			buffer = charset.encode(cbuf);
        			socketChannel.write(buffer);
        			if(buffer.toString().length() == 0 || buffer.toString().length() == -1){
        				ByteBuffer buf = ByteBuffer.wrap(buffer1);
            			socketChannel.write(buf);
            			if(buffer1.toString().length() == 0 || buffer1.toString().length() == -1){
            				it.remove();			
            				socketChannel.close();
            			}
        			}		
				}
				else{
					//Envia mensaje FORBIDDEN si la ruta especificada no es el directorio actual
					CharBuffer cbuf = buffer.asCharBuffer();
        			cbuf.put("HTTP/1.1 403 Forbidden\nContent-Type: text/html\nContent-Length: 90\n");
        			buffer = charset.encode(cbuf);
        			socketChannel.write(buffer);
        			if(buffer.toString().length() == 0 || buffer.toString().length() == -1){
        				cbuf.put("<html><head><title>403 FORBIDDEN</title></head><body><h1>Forbidden</h1></body></html>");
            			buffer = charset.encode(cbuf);
            			socketChannel.write(buffer);
            			if(buffer.toString().length() == 0 || buffer.toString().length() == -1){
            				it.remove();			
            				socketChannel.close();
            			}
        			}			
				}				
			}
			else{
				//Envia mensaje NOT FOUND si el fichero no existe
				CharBuffer cbuf = buffer.asCharBuffer();
    			cbuf.put("HTTP/1.1 404 Not Found\nContent-Type: text/html\nContent-Length: 94\n");
    			buffer = charset.encode(cbuf);
    			socketChannel.write(buffer);
    			if(buffer.toString().length() == 0 || buffer.toString().length() == -1){
    				cbuf.put("<html><head><title>404 Not Found</title></head><body><h1>Not Found</h1></body></html>");
        			buffer = charset.encode(cbuf);
        			socketChannel.write(buffer);
        			if(buffer.toString().length() == 0 || buffer.toString().length() == -1){
        				it.remove();			
        				socketChannel.close();
        			}
    			}					
			}
		}catch(IOException ex){}
	}
	
	/**  
	 * Decodifica el cuerpo del mensaje recibido, obteniendo el nombre de un fichero y un contenido a escribir en este.
	 *
	 * Si existe: Escribe el contenido en el fichero y devuelve el codigo 200 OK y el contenido escrito en el fichero. 
	 *      				Mostramos en la cabecera Content Length (obligatorio) y content type.
	 * 
	 */
	private static void post(String cuerpo, SocketChannel socketChannel, Iterator<SelectionKey> it){
		cuerpo= java.net.URLDecoder.decode(cuerpo); //Decodifico el mensaje enviado en el cuerpo
		//Obtengo fname y content, separando atributos por el delimitador & y eliminando el tipo de atributo
		Scanner s=new Scanner (cuerpo).useDelimiter("\\s*&\\s*"); 
		String fname=s.next(); 
		String content=s.next();
		s.close();
		fname=fname.replace("fname=",""); 
		content=content.replace("content=","");

		//Escribe en el fichero introducido, modificando su contenido si existe. Y creando el fichero e inicializandolo
		// con el contenido introducido si no existe.
		FileWriter fw = null;
		PrintWriter pw = null;
		try{
			fw = new FileWriter(fname);
			pw = new PrintWriter(fw);
			
			pw.println(content);
			fw.close();
			pw.close();
		}
		catch (IOException e) {}

		//Muestro mensaje de exito en la pagina web si existe el mensaje. Sino error 
		try{		
			CharBuffer cbuf = buffer.asCharBuffer();
			cbuf.put("HTTP/1.1 200 OK\nContent-Type: text/html\nContent-Length: 173\n");
			buffer = charset.encode(cbuf);
			socketChannel.write(buffer);
			if(buffer.toString().length() == 0 || buffer.toString().length() == -1){
				cbuf.put("<html><meta charset=\"UTF-8\"><head><title>¡Éxito!</title></head><body><h1>¡Éxito!</h1>"
						+"<p>Se ha escrito lo siguiente en el fichero "+fname+":</p><pre>"+content+"</pre></body></html>");
    			buffer = charset.encode(cbuf);
    			socketChannel.write(buffer);
    			if(buffer.toString().length() == 0 || buffer.toString().length() == -1){
    				it.remove();			
    				socketChannel.close();
    			}
			}					
		}catch(IOException ex){}
	}
	
	

	
}
