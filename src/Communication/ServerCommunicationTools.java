package Communication;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Semaphore;

import Tools.message;


/**
 * responsible on all the Communication of the client.
 * @author sefi erlich
 *
 */
public class ServerCommunicationTools {
	
	/**
	 * the port to run on
	 */
	private int Port;
	/**
	 * the server socket to first connect to
	 */
	private ServerSocket serverSocket;
	/**
	 * a list contains all client sockets
	 */
	private SyncClientList Clients;
	/**
	 * queue for all the questions that comes from the user
	 */
	private BlockingQueue<message> Entry;

	/**
	 * queue for all the Messages that need to be send
	 */
	private BlockingQueue<message> Exit;
	
	/**
	 * a list of string to show to server
	 */
	private BlockingQueue<String> Console;
	/**
	 * the server status
	 */
    public boolean ServerRunning;
	/**
	 * mutex to server running
	 */
    public Semaphore mutextRunning;

    
    /**
     * Constructor
     * @param _Clients a list of clients
     */
	public ServerCommunicationTools(SyncClientList _Clients) {
		mutextRunning= new Semaphore(1,true);
		Clients = _Clients;
		Entry = new ArrayBlockingQueue<message>(Tools.mutual.MaxNumberOfM);
		Exit = new ArrayBlockingQueue<message>(Tools.mutual.MaxNumberOfM);
		Port = Tools.mutual.DefaultServerPort;
		ServerRunning = false;
		Console= new ArrayBlockingQueue<String>(Tools.mutual.MaxNumberOfM);
	}

	/**
	 * Constructor
	 * @param _Clients a list of clients
	 * @param port a port to listening to
	 */
	public ServerCommunicationTools(SyncClientList _Clients, int port) {
		mutextRunning= new Semaphore(1,true);
		Clients = _Clients;
		Entry = new ArrayBlockingQueue<message>(Tools.mutual.MaxNumberOfM);
		Exit = new ArrayBlockingQueue<message>(Tools.mutual.MaxNumberOfM);
		Port = port;
		ServerRunning = false;
		Console= new ArrayBlockingQueue<String>(Tools.mutual.MaxNumberOfM);

	}

	

	
	/**
	 * add a log 
	 * @param str a string message to log
	 */
	public void AddLog(String str){
		try {
			Console.put(str);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * take a log message
	 * @return a log message
	 */
	public String TakeLog(){
		String s=null;
		try {
			s=Console.take();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		return s;
	}
	/**
	 * check of there is a log string
	 * @return
	 */
	public boolean IsConsleLog() {
		return !Console.isEmpty();
	}

	

	

	/**
	 * start the server socket
	 * @return true if successfully, false else
	 */
	public boolean Connect() {
		try {
			serverSocket = new ServerSocket(Port);
			ServerRunning=true;
			return true;
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}
	}
	

	/**
	 * put a M for exit
	 * @param m a message to add
	 * @return true if added, else false
	 */
	public boolean putExit(message m) {
		try {
			Exit.put(m);
			return true;
		} catch (InterruptedException e) {
			e.printStackTrace();
			return false;
		}
	}

	/**
	 * will get a message from the Message Queue
	 * @return M the Message that removed from the queue
	 */
	public message TakeExit() {
		message m=null;
		try {
			m = Exit.take();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		return m;
	}
	/**
	 * get a M for entry
	 * @return M if succeed, null if failed
	 */
	public message TakeEntry() {
		try {
			message q = Entry.take();
			return q;
		} catch (InterruptedException e) {
			e.printStackTrace();
			return null;
		}

	}
	/**
	 * 	 add a Q to the entry queue
	 * @param q a question to add
	 * @return true if was added, false else
	 */

	public boolean putEntry(message m) {
		try {
			Entry.put(m);
			return true;
		} catch (InterruptedException e) {
			e.printStackTrace();
			return false;
		}
	}

	/**
	 * get the connection status
	 * @return the connection status
	 */
	public boolean getConnectionStatus(){
		boolean ans;
		try {
			mutextRunning.acquire();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		ans= ServerRunning;
		mutextRunning.release();
		return ans;
	}
	
	/**
	 * set the connection status 
	 * @param status the new status
	 */
	public void SetConnectionStatus(boolean status){
		try {
			mutextRunning.acquire();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		ServerRunning=status;
		mutextRunning.release();
		
		
		putEntry(new message("server","server",Tools.MessageType.RELERSE,null));
		putExit(new message("server","server",Tools.MessageType.RELERSE,null));
		Clients.addToOldClients("end");
	}


	/**
	 * Disconnect the server, Disconnect all the clients, and close all server socket
	 */
	public void ServerDisconnect() {
		try {
			Clients.DisconnectAllClients();
			serverSocket.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}


	/**
	 * accept and wait
	 * @return the new socket
	 */
	public Socket accept() {
		Socket s=null;
		try {
			s= serverSocket.accept();
		} catch (IOException e) {
			if (!getConnectionStatus())
				return null;
			e.printStackTrace();
		}
		return s;
	}

	






	/**
	 * this class control the server communication  for one client to be hold by the server.
	 * this thread as all the function to manage a client , and listing to incoming messages
	 * @author Sefi Erlich
	 */
	 public class ClientSocket{
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
		//private ServerCommunicationTools Communication;
		
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
		 * client socket
		 * @param socket the socket after accept
		 * @param id the id
		 */
		public ClientSocket(Socket socket, int id) {
			connection = socket;
			ClientId = id;
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
					outputStream.writeObject(new message(name,null,Tools.MessageType.YOU_HAVE_CONNECTED,null));
					outputStream.writeObject(ClientId);
				}else{
					outputStream.writeObject(new message(name,null,Tools.MessageType.NameTaken,null));
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
		/**
		 * get the client status
		 * @return
		 */
		public boolean getConnectionStatus(){
			return running;
		}

		/**
		 * the the status of the client
		 * @param status the new status
		 */
		public void SetConnectionStatus(boolean status){
			running=status;
		}

		/**
		 * 	 add a m to the entry queue
		 * @param m a question to add
		 * @return true if was added, false else
		 */

		public boolean putEntry(message m) {
			try {
				Entry.put(m);
				return true;
			} catch (InterruptedException e) {
				e.printStackTrace();
				return false;
			}
		}

	
		/**
		 * getting the client id
		 * @return the client id that associate with the Object
		 */
		public int getClientId() {
			return ClientId;
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
				if (!isConnectionAlive()) {
					disconnect();
					AddLog(name+" disconnected");
				} else {
					sendHeartBeat(); // Send a heart beat to the client
				}
				return null;
			} catch (IOException e) {
				disconnect();
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
		 * get the client name
		 * @return client name
		 */
		public String getClientName() {
			return name;
		}
		/**
		 * check if the socket is closed
		 * @return 
		 */
		public boolean isClose() {
			return connection.isClosed();
		}
		/**
		 * set the last read time
		 */
		public void SetLastRead() {
         lastReadTime=System.currentTimeMillis();			
		}



	}



}