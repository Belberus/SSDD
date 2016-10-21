package practica1;

import java.io.*;
import java.net.Socket;
import java.util.Scanner;
import java.lang.*;
import java.net.URLDecoder;
import java.nio.ByteBuffer;

public class TratamientoSelect{
	private ByteBuffer stream;
	private Socket socketCliente;
	
	public TratamientoSelect(Socket socketCliente, ByteBuffer stream){
		this.stream=stream;
		this.socketCliente=socketCliente;
	}
	
	public void tratar() {
		//Analizo la peticion realizada por el cliente
		HTTPParser bParser = new HTTPParser();
		stream.flip();
		bParser.parseRequest(stream);

		try{
			PrintWriter salida=new PrintWriter(socketCliente.getOutputStream(),true);
			if (bParser.failed()){
				//Devuelve 400 Bad Request
				salida.println("HTTP/1.1 400 Bad Request\nContent-Type: text/html\nContent-Length: 94\n");
				salida.println("<html><head><title>404 Not Found</title></head><body><h1>Not Found</h1></body></html>");		
			}
			else if (bParser.getMethod().contains("GET")){
				//LLamamos a metodo get
				get(bParser.getPath());
			}
			else if (bParser.getMethod().contains("POST")){
				//Llamamos a post
				post(new String((bParser.getBody()).array()));
			}
			else{
				//devuelve 501 Not Implemented
				salida.println("HTTP/1.1 501 Not Implemented\nContent-Type: text/html\nContent-Length: 90\n");
				salida.println("<html><head><title>501 Not Implemented</title></head><body><h1>Not Found</h1></body></html>");
			}
			salida.close();
			socketCliente.close();
		}catch(IOException ex){}		
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
	private void get(String fichero){
		fichero = fichero.substring(1, fichero.length()); //Elimina el directorio raiz, primera '/' del fichero
		FileInputStream f = null; 
		boolean existe = true; 
		byte[] buffer = new byte[1024]; 
		int bytes;
		//Crea un nuevo fichero si existe, sino indica que no existe asignando all booleano existe el valor false
		try{
   		f = new FileInputStream(fichero);
		}
		catch (FileNotFoundException e){
			existe = false; 
		}
		try{
			PrintWriter salida=new PrintWriter(socketCliente.getOutputStream(),true);
			if (existe){
				int ficheroLength=f.available(); //Obtiene el numero de bits aproximados del fichero
				if(fichero.indexOf("/") == -1){
					//Lee fichero a enviar
					try{
					while ((bytes = f.read(buffer)) != -1 ); //MIRAR EXCEPCION, NO FUNCIONA!!!!!
					}catch(ArrayIndexOutOfBoundsException e){
						//MIRAR SI HAY ERROR PARA FICHERO MUY GRANDE
						salida.println("HTTP/1.1 403 Forbidden\nContent-Type: text/html\nContent-Length: 90\n");
						salida.println("<html><head><title>FICHERO MUY GRANDE</title></head><body><h1>EL FICHERO INTRODUCIDO ES DEMASIADO GRANDE</h1></body></html>");
					}
					f.close(); //Cierra FileInputStream
					String tipo=fichero.substring(fichero.lastIndexOf('.'));
					if(tipo.equals(".html")){
						tipo="html";
					}else{
						tipo="plain";
					}
					//Envia mensaje OK si el fichero existe y esta dentro del directorio no tiene '/' en su ruta
					salida.println("HTTP/1.1 200 OK\nContent-Type: text/"+tipo+"\nContent-Length: "+ficheroLength+"\n");
					socketCliente.getOutputStream().write(buffer, 0, ficheroLength);
				}
				else{
					//Envia mensaje FORBIDDEN si la ruta especificada no es el directorio actual
					salida.println("HTTP/1.1 403 Forbidden\nContent-Type: text/html\nContent-Length: 90\n");
					salida.println("<html><head><title>403 FORBIDDEN</title></head><body><h1>Forbidden</h1></body></html>");			
				}				
			}
			else{
				//Envia mensaje NOT FOUND si el fichero no existe
				salida.println("HTTP/1.1 404 Not Found\nContent-Type: text/html\nContent-Length: 94\n");
				salida.println("<html><head><title>404 Not Found</title></head><body><h1>Not Found</h1></body></html>");		
			}
			salida.close(); //Cierra PrintWriter
		}catch(IOException ex){}
	}

	/**  
	 * Decodifica el cuerpo del mensaje recibido, obteniendo el nombre de un fichero y un contenido a escribir en este.
	 *
	 * Si existe: Escribe el contenido en el fichero y devuelve el codigo 200 OK y el contenido escrito en el fichero. 
	 *      				Mostramos en la cabecera Content Length (obligatorio) y content type.
	 * 
	 */
	private void post(String cuerpo){
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
			PrintWriter salida=new PrintWriter(socketCliente.getOutputStream(),true);
			salida.println("HTTP/1.1 200 OK\nContent-Type: text/html\nContent-Length: 173\n");
			salida.println("<html><meta charset=\"UTF-8\"><head><title>¡Éxito!</title></head><body><h1>¡Éxito!</h1>"
					+"<p>Se ha escrito lo siguiente en el fichero "+fname+":</p><pre>"+content+"</pre></body></html>");
			salida.close();
		}catch(IOException ex){}
	}
}
