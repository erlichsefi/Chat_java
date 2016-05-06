package Communication;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Semaphore;

import Server_Threads.ListeningThread;

/**
 * class of client list
 * @author erlichsefi
 *
 */
public class SyncClientList {
	/**
	 * a map form name to the socket
	 */
	private Map<String,ServerCommunicationTools.ClientSocket> Clients;
	/**
	 * a map from the name to the listening threads
	 */
	private Map<String,ListeningThread> ListeningThreads;
	/**
	 * a list of clients to remove
	 */
	private ArrayList<String> Old_Client;
	/**
	 * Semaphore used  as mutex to control 
	 * the changing of the client list
	 */
	private Semaphore ClientListMutext;
	/**
	 * a tool the constrol the thread pf removing old clients
	 */
	private Semaphore OldClientListEvent;

	/**
	 * 
	 */
	public SyncClientList(){
		Clients=new HashMap<String,ServerCommunicationTools.ClientSocket>();
		ListeningThreads=new HashMap<String,ListeningThread>();
		ClientListMutext = new Semaphore(1, true);
		OldClientListEvent=new Semaphore(0, true);
		Old_Client=new ArrayList<String>(Tools.mutual.MaxNumberOfM);
	}

	
	
	/**
	 * get a pointer to the client corresponding with the name.
	 * @param ClientId  Client Id to look for
	 * @return the ClientSocket if client exist, null else
	 */
	public ServerCommunicationTools.ClientSocket findClient(String name) {
		Down();
		ServerCommunicationTools.ClientSocket ans = Clients.get(name);
		Up();
		return ans;
	}
	

	/**
	 * get the a list of the connected clients
	 * @return a list of the connected clients
	 */
	public ArrayList<String> GetClientList() {
		Down();
		ArrayList<String> ans = new ArrayList<String>();
		for (int i = 0; i < Clients.size(); i++) {
			if (Clients.get(i).isConnected())
				ans.add(Clients.get(i).getClientName());
		}
		Up();
		return ans;
	}
	
	
	/**
	 * Disconnect all client and live server socket open
	 */
	public void DisconnectAllClients() {
		Down();
		int size = Clients.size();
		for (int i = 0; i < size; i++) {
			if (Clients.get(i).getConnectionStatus()){
				Clients.get(i).disconnect();
			}
		}
		Up();
	}

	/**
	 * add client to list
	 * @param client an open socket
	 * @param listen an listen thread of the socket
	 */
	public void addClient(ServerCommunicationTools.ClientSocket client,ListeningThread listen) {
		Down();
		String name=client.getClientName();
		Clients.put(name, client);
		this.ListeningThreads.put(name, listen);
		Up();
	}

	/**
	 * add a client to be remove 
	 * @param clientId the client name
	 */
	public void addToOldClients(String clientId) {
			Old_Client.add(clientId);
			OldClientListEvent.release();
			
	}
	
	/**
	 * remove a client from a lisr
	 */
	public void RemoveOldClient() {
		try {
			OldClientListEvent.acquire();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		String name=Old_Client.remove(0);
		Down();
		Clients.remove(name);
		Old_Client.remove(name);
		Up();
		 
	}
	
	/**
	 * release access to the client list
	 */
	private void Up() {
		ClientListMutext.release();
	}

	/**
	 * take access to the client list
	 */
	private void Down() {
		try {
			ClientListMutext.acquire();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

}
