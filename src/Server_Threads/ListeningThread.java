package Server_Threads;

import Communication.SyncClientList;
import Communication.ServerCommunicationTools.ClientSocket;
import Tools.message;

/**
 * this client get a socket to listen to
 * and adding the incoming messages to the queue
 * @author erlichsefi
 *
 */
public class ListeningThread extends Thread {
	/**
	 * a client socket to listen to 
	 */
	private ClientSocket socket;
	/**
	 *  a list of clients
	 */
	private SyncClientList Clients;
	
	/**
	 * 
	 * @param client socket with an open streams & socket
	 * @param c1 a list of clients
	 */
	public ListeningThread(ClientSocket client,SyncClientList cl) {
		super("Server Thread for "+client.getClientName());
		this.socket = client;
		Clients=cl;
	}



	/**
	 * do the following things: 1. reading the messages that comes form the client
	 * and handling them in the right way 2. at the and of the thread disconnecting
	 */
	public void run() {
		while (socket.getConnectionStatus()) {
			Object m = socket.ReadResponsed();
			if (!socket.getConnectionStatus()) break;
			if (m != null) {
				socket.SetLastRead();
				message s=(message)m;
				socket.putEntry(s);
			}
		}
		
		Clients.addToOldClients(socket.getClientName());
	}

	
	
	
	
	
	
	
	
}
