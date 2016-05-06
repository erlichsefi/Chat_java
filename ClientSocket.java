package Server_Threads;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.concurrent.Semaphore;

import Communication.ServerCommunicationTools;
import Tools.message;



/**
 * this class control the server communication  for one client to be hold by the server.
 * this thread as all the function to manage a client , and listing to incoming messages
 * @author Sefi Erlich
 *
 */
public class ClientSocket extends Thread{
	/**
	 * a Socket to one client
	 */
	private Socket connection;
	/**
	 * a ObjectInputStream to one client
	 */
	private ObjectInputStream inputStream = null;
	/**
	 * a ObjectOutputStream to one client
	 */
	private ObjectOutputStream outputStream = null;

	/**
	 * queue for all the questions that comes from the user
	 */
	private ServerCommunicationTools Communication;
	
	private String name;
	/**
	 * a Semaphore to know when there is answers waiting 
	 */
	/**
	 * Semaphore used  as mutex to control 
	 * the writing to the stream
	 */
	private Semaphore ControlOnWriteToStream;
	/**
	 * the client id of this client
	 */
	private int ClientId;
	/**
	 * a boolean mark to run this thread
	 */
	private boolean running;
	/**
	 * a value to set if the connection was open or closed
	 */
	private boolean IsConnected;
	/**
	 * value to check if client disconnected Unannounced
	 * the last time got a message form the client
	 */
	private long lastReadTime;
	/**
	 * the maximum time to wait from the last read
	 * until disconnection
	 */
	private long maxTimeout;


	/**
	 * a constructor for this class
	 * @param socket  a open socket to the client
	 * @param id  the id of the client connected to the socket, given by the server
	 * @param _entryQ   a pointer to the entry Q queue
	 * @param _Question   a Semaphore to control the entry Q queue
	 * @param _entryA    a pointer to the entry A queue
	 * @param _Answers    a Semaphore to control the entry A queue
	 */
	public ClientSocket(Socket socket, int id, ServerCommunicationTools _entryQ) {
		super("ClientSocket : "+id);
		connection = socket;
		ClientId = id;
		Communication = _entryQ;
		IsConnected = false;
		ControlOnWriteToStream=new Semaphore(1,true);

	}

	/**
	 * change maxTimeOut form default
	 * @param _maxTimeout   a new time
	 */
	public void setMaxTimeout(long _maxTimeout) {
		maxTimeout = _maxTimeout;
	}

	/**
	 * open stream for client
	 * @return true if the stream opened correctly, false else
	 */

	public boolean OpenStream(ArrayList<String> clientlist) {
		try {
			IsConnected = true;
			outputStream = new ObjectOutputStream(connection.getOutputStream());
			inputStream = new ObjectInputStream(connection.getInputStream());
			name=((message)inputStream.readObject()).getSrc();
			if (!clientlist.contains(name)){
				outputStream.writeObject(new message(name,null,Tools.Static.YOU_HAVE_CONNECTED,null));
				outputStream.writeObject(ClientId);
			}else{
				outputStream.writeObject(new message(name,null,Tools.Static.NameTaken,null));
				disconnect();
				return false;
			}
			System.out.println("open stream to :"+name);
			running=true;
			return true;
		} catch (UnknownHostException e) {
			e.printStackTrace();
			return false;
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
			return false;
		}

	}

	public boolean getConnectionStatus(){
		return running;
	}

	public void SetConnectionStatus(boolean status){
		running=status;
	}


	/**
	 * 	 add a Q to the entry queue
	 * @param q a question to add
	 * @return true if was added, false else
	 */




	/**
	 * getting the client id
	 * @return the client id that associate with the Object
	 */
	public long getClientId() {
		return ClientId;
	}

	/**
	 * do the following things: 1. reading the messages that comes form the client
	 * and handling them in the right way 2. at the and of the thread disconnecting
	 */
	public void run() {
		while (getConnectionStatus()) {
			Object m = ReadResponsed();
			if (!getConnectionStatus()) break;
			if (m != null) {
				System.out.println("socket of "+name+"got a message : "+m);
				this.lastReadTime=System.currentTimeMillis();
				message s=(message)m;
				Communication.AddEntry(s);
			}
		}
		System.out.println("stop lithing to client: "+name);
	}



	/**
	 * send a massage to the client
	 * @param m  the message to be sent
	 * @return true id the M was sent, else false
	 */
	public boolean SendMassage(message m) {
		try {
			ControlOnWriteToStream.acquire();
			outputStream.writeObject(m);
			outputStream.flush();
			ControlOnWriteToStream.release();
			return true;
		} catch (IOException e) {
			if (running){
				e.printStackTrace();
				return false;
			}
			return true;
		}catch (InterruptedException e) {
			e.printStackTrace();
			return false;
		}
	}

	/**
	 * reading messages from the client
	 * @return M if got M , null if not
	 */
	public Object ReadResponsed() {
		try {
			return inputStream.readObject();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
			return null;
		}catch (SocketException e) {

			System.err.println("lost connection to client: "+name+" sending heart beat");
			if (!isConnectionAlive()) {
				disconnect();
				Communication.AddLog(name+" disconnected");
			} else {
				sendHeartBeat(); // Send a heart beat to the client
			}

			return null;
		} catch (IOException e) {
			disconnect();
			//			
			return null;

		}

	}


	/**
	 * send a ping to the client
	 * @return true of the message was send, false else
	 */
	private boolean sendHeartBeat() {
		try {
			ControlOnWriteToStream.acquire();
			outputStream.writeObject("Ping");
			outputStream.flush();
			ControlOnWriteToStream.release();
			return true;
		} catch (InterruptedException e) {
			e.printStackTrace();
			return false;
		}catch (IOException e) {
			e.printStackTrace();
			return false;
		}

	}

	/**
	 * stop thread and send a goodbye message to the client
	 * @return true if handled, false else
	 */
	public boolean StopThread(){
		running = true;
		IsConnected = false;
		try {
			ControlOnWriteToStream.acquire();
			outputStream.writeObject(new String("FIN"));
			outputStream.flush();
			ControlOnWriteToStream.release();
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}catch (InterruptedException e) {
			e.printStackTrace();
			return false;
		}
		return true;
	}


	/**
	 * Disconnect client for the server
	 * @return true if disconnect successfully, false else
	 */
	public boolean disconnect() {
		try {
			running=false;
			ControlOnWriteToStream.acquire();
			outputStream.close();
			inputStream.close();
			connection.close();
			ControlOnWriteToStream.release();
			return true;
		} catch (IOException e) {
			return false;
		} catch (InterruptedException e) {
			e.printStackTrace();
			return false;
		}
	}

	/**
	 * check if the the client didn't responded for a while
	 * 
	 * @return true if passed to much time
	 */
	public boolean isConnectionAlive() {
		return System.currentTimeMillis() - lastReadTime < maxTimeout;
	}


	/**
	 * check if the socket is still connected
	 * @return true if connected, false else
	 */
	public boolean isConnected() {
		return connection.isConnected() && IsConnected;
	}

	

	
	/**
	 * return if  all Connections are closed
	 * @return true if closed, flase else
	 */
	public boolean isAllConnectionsClosed() {
		return connection.isClosed() && connection.isInputShutdown()
				&& connection.isOutputShutdown();
	}

	/**
	 * get details from client ip and port
	 * @return the client details as: "ip,port"
	 */
	public String getDetail() {
		return connection.getInetAddress() + "," + connection.getPort();
	}

	public String getClientName() {
		return name;
	}

	public boolean isClose() {
		return connection.isClosed();
	}

	public void SetLastRead() {
		lastReadTime=System.currentTimeMillis();		
	}



}
