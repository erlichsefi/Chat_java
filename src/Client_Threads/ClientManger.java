package Client_Threads;

import Communication.ClientCommunicationTools;
import Tools.message;



/**
 * @author Sefi Erlich
 */
public class ClientManger  {

	/**
	 * the helping thread
	 */
	private ClientCommunicationTools communication;


	/**
	 * a boolean to see is the thread is in normal
	 *  run and not waiting to close the connection
	 */
	private boolean Connected;
	/**
	 * thread that reading messages
	 */
	private ClientReader read;
	/**
	 * thread that sending messages
	 */
	private ClientWriter write;
	/**
	 * the client name
	 */
	private String Myname;


	/**
	 * class with all the tools for client management
	 */
	public ClientManger() {
		Connected=false;
		communication = new ClientCommunicationTools( );	
	}

	/**
	 * 
	 * @param Server the server ip
	 * @param port the port
	 */
	public ClientManger(String Server, int port) {
		Connected=false;
		communication = new ClientCommunicationTools(Server, port);
	}



	/**
	 * check of the client connected
	 * @return true if connect, false else
	 */
	public boolean isConnected() {
		return communication.isConnected();
	}


	/**
	 * connect to a server(the default one), with name
	 * @param name the name to connect with
	 * @return true of connected
	 */
	public boolean connect(String name){
		communication = new ClientCommunicationTools();	
		read=new ClientReader(communication);
		write=new ClientWriter(communication);
		if (Connected=communication.Connect(name)) {
			Myname=name;
			//send outgoing messages
			write.start();
			//reading messages
			read.start();
			return true;
		}
		return false;
	}
	/**
	 * connect to a server, with name
	 * @param name the name to connect with
	 * @param ip the ip the connect to
	 * @return true of connected
	 */
	public boolean connect(String name,String ip){
		Tools.mutual.DefaultServerIP=ip;
		communication = new ClientCommunicationTools();	
		if (Connected=communication.Connect(name)) {
			read=new ClientReader(communication);
			write=new ClientWriter(communication);
			communication.getClientID();
			Myname=name;
			//send outgoing messages
			write.start();
			//reading messages
			read.start();
			return true;
		}
		return false;
	}
	/**
	 * get log message, if there is no message-wait
	 * @return a log message
	 */
	public String getNextStringToConsole(){
		if (communication.getConnectionStatus())
			return communication.takeFromConsole();
		else
			return null;
	}

	/**
	 * send a message asking for the list of connected clients 
	 * @return true if add to the queue, false else
	 */
	public boolean SendshowOnline(){
		if (Connected){
			communication.PutExit(new message(Myname,null,Tools.MessageType.GET_LIST,null));
			return true;
		}
		return false;
	}
	/**
	 * send a message asking for disconnect 
	 * @return true if add to the queue, false else
	 */
	public boolean dissconnect(){
		if (Connected){
			communication.SendClose();
		}
		return false;
	}
	/**
	 * send a message the another client
	 * @param clientName the client to send to 
	 * @param message the message to send
	 * @return true if add to the queue, false else
	 */
	public boolean sendToclient(String clientName,String message){
		if (Connected){
			communication.PutExit(new message(Myname,clientName,Tools.MessageType.TO_ONE,message));
			return true;
		}
		return false;
	}

	/**
	 * send a message the all the clients
	 * @param message the message to send
	 * @return true if add to the queue, false else
	 */
	public boolean sendToAll(String message){
		if (Connected){
			communication.PutExit(new message(Myname,null,Tools.MessageType.TO_ALL,message));
			return true;
		}
		return false;
	}












}
