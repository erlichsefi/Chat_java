package Communication;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Semaphore;

import Tools.message;



/**
 * this class control the communication with the server,
 * contains all the communication function 
 * @author sefi erlich
 *
 */
public class ClientCommunicationTools  {

	/**
	 * the Socket to the server
	 */
	private Socket connection;
	/**
	 * the ObjectInputStream to the server
	 */
	private ObjectInputStream inputStream = null;
	/**
	 * the ObjectOutputStream to the server
	 */
	private ObjectOutputStream outputStream = null;

	/**
	 *the server ip to connect to
	 */
	private String ServerIp;
	/**
	 * 	the server port to connect to
	 */
	private int ServerPort;
	/**
	 * the client id, that will get when connect to the server
	 */
	private int MyId = 0;

	/**
	 * a value to set if the connection was open or closed
	 */
	private boolean IsConnected;

	/**
	 *  queue to control the entering questions
	 * 
	 */
	private BlockingQueue<message> EntryString;
	/**
	 *  queue to control the exit messages
	 */
	private BlockingQueue<message> ExitMessages;
	/**
	 * Semaphore used  as mutex to control 
	 * the writing to the stream
	 */
	private Semaphore ControlOnWriteToStream;

	private Semaphore ISconnectedMutex;
	/**
	 * the name of the client
	 */
	private String MyName;
	/**
	 * a list of String to print to console
	 */
	private BlockingQueue<String> Console;



	/**
	 * Constructor
	 */
	public ClientCommunicationTools() {
		connection = null;
		MyId = 0;
		EntryString  = new ArrayBlockingQueue<message>(Tools.mutual.MaxNumberOfM);
		ExitMessages = new ArrayBlockingQueue<message>(Tools.mutual.MaxNumberOfM);
		IsConnected = false;
		ServerIp = Tools.mutual.DefaultServerIP;
		ServerPort = Tools.mutual.DefaultServerPort;
		Console=new ArrayBlockingQueue<String>(Tools.mutual.MaxNumberOfM);
		ControlOnWriteToStream=new Semaphore(1,true);
		ISconnectedMutex=new Semaphore(1,true);

	}

	/**
	 * Constructor
	 * @param server the server ip
	 * @param port the server port
	 */
	public ClientCommunicationTools( String server, int port) {
		connection = null;
		MyId = 0;
		ServerIp=Tools.mutual.DefaultServerIP;
		ServerPort=Tools.mutual.DefaultServerPort;
		EntryString  = new ArrayBlockingQueue<message>(Tools.mutual.MaxNumberOfM);
		ExitMessages = new ArrayBlockingQueue<message>(Tools.mutual.MaxNumberOfM);
		IsConnected = false;
		ServerIp = server;
		ServerPort = port;
		Console=new ArrayBlockingQueue<String>(Tools.mutual.MaxNumberOfM);
		ControlOnWriteToStream=new Semaphore(1,true);

	}



	/**
	 * set the status of the connection
	 * @param status the new status
	 */
	public void SetConnectionStatus(boolean status){
		try {
			ISconnectedMutex.acquire();
			IsConnected=status;
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		finally{
			ISconnectedMutex.release();

		}

	}

	/**
	 * check of the client connected
	 * connected is: socket connected and the client still using it.
	 * @return true if connect, false else
	 */
	public boolean isConnected() {
		boolean status=false;
		try {
			ISconnectedMutex.acquire();
			status=  IsConnected;
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		finally{
			ISconnectedMutex.release();

		}
		return status;
	}



	/**
	 * connect to the server at: ServerIP and ServerPort
	 * @param name my name
	 * @return true if connected successfully, false else
	 * @throws IOException 
	 */

	public boolean Connect(String name) throws IOException {
		try {

			MyName=name;
			connection = new Socket(ServerIp, ServerPort);
			try {
				ISconnectedMutex.acquire();
				IsConnected = true;
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			finally{
				ISconnectedMutex.release();

			}
			outputStream = new ObjectOutputStream(connection.getOutputStream());
			inputStream = new ObjectInputStream(connection.getInputStream());
			outputStream.writeObject(new message(MyName,null,Tools.MessageType.LETS_CONNECT,null));
			outputStream.flush();
			message c=(message)inputStream.readObject();
			if (c.getType()==Tools.MessageType.OK){
				MyId = (Integer) inputStream.readObject();
				MyName=name;
				return true;
			}
			else{
				addConsole("NAME TAKEN");
				return false;
			}
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
			return false;
		} 
	}




	/**
	 * 
	 * @param m  a message to be sent
	 * @return true if the message as got to the server
	 */
	public boolean Send(message m) {
		try {
			ControlOnWriteToStream.acquire();
			outputStream.writeObject(m);
			outputStream.flush();
			return true;
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		} catch (InterruptedException e) {
			System.err.println(e.getStackTrace());
			return false;
		}
		finally{
			ControlOnWriteToStream.release();

		}

	}
	public boolean FastSend(message message) {
		try {
			ControlOnWriteToStream.acquire();
			outputStream.writeObject(message);
			outputStream.flush();
			return true;
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		} catch (InterruptedException e) {
			System.err.println(e.getStackTrace());
			return false;
		}
		finally{
			ControlOnWriteToStream.release();

		}

	}



	/**
	 * Close all connections
	 * @return true is the connection been closed
	 */
	public boolean SendClose() {
		try {
			ControlOnWriteToStream.acquire();
			outputStream.writeObject(new message(MyName,null,Tools.MessageType.LETS_DISCONNECT,null));
			outputStream.flush();
			return true;
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		} catch (InterruptedException e) {
			e.printStackTrace();
			return false;

		}
		finally{
			ControlOnWriteToStream.release();

		}
	}

	/**
	 * get the Object form the stream
	 * @return Object form the client, return null if the read fail, wait
	 *         until a new Message is arrived
	 */
	public Object Response() {
		try {
			return inputStream.readObject();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
			return null;
		} catch (IOException e){
			return null;
		}

	}

	/**
	 * getting a M from the queue
	 * @return the answer in the head of the queue
	 */
	public message getExitMessage() {
		message m=null;
		try {
			m = ExitMessages.take();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		return m;
	}

	/**
	 * add a Message to the end of the queue
	 * @param m aMessage to be add
	 * @return true if as add successfully,false else 
	 */
	public boolean PutExit(message m) {
		try {
			ExitMessages.put(m);
			return true;
		} catch (InterruptedException e) {
			System.err.println(e.getStackTrace());
			return false;
		}
	}

	/**
	 * add a Q to the entry queue
	 * @param q    the Q to be add to
	 * @return true if as add successfully,false else 
	 */

	public boolean AddEntryMessage(message q) {
		try {
			EntryString.put(q);
			return true;
		} catch (InterruptedException e) {
			System.err.println(e.getStackTrace());
			return false;
		}
	}

	/**
	 * getting a M from the queue
	 * @return the answer in the head of the queue
	 */
	public message getEntry() {
		message m=null;
		try {
			m = EntryString.take();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		return m;
	}


	/**
	 * get the client id
	 * @return client id
	 */
	public int getClientID() {
		return MyId;
	}
	/**
	 * the the name
	 * @return
	 */
	public String getName() {
		return MyName;
	}

	/**
	 * Disconnect
	 * @return
	 */
	public boolean disconnect() {
		try {
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
	 * get the status of the connection
	 * @return the status
	 */
	public boolean getConnectionStatus() {
		boolean status=false;
		try {
			ISconnectedMutex.acquire();
			status=  IsConnected;
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		finally{
			ISconnectedMutex.release();

		}
		return status;
	}
	/**
	 * to string to shoe in the console
	 * @return
	 */
	public String takeFromConsole() {
		String c=null;
		try {
			c= Console.take();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		return c;
	}


	/**
	 * add to console
	 * @param m a String message to show
	 * @return
	 */
	public boolean addConsole(String m) {
		try {
			Console.put(m);
			return true;
		} catch (InterruptedException e) {
			e.printStackTrace();
			return false;
		}		
	}

	public boolean IsNotEmpty() {
		return !Console.isEmpty();
	}


}
