package Server_Threads;


import Communication.ServerCommunicationTools;
import Communication.SyncClientList;

/**
 * this thread responsible of removing old client
 * @author erlichsefi
 *
 */
public class removeOldclients extends Thread {
	/**
	 *  a list of clients
	 */
	private SyncClientList Clients;
	/**
	 * 
	 * a client Communication Tool
	 */
	private  ServerCommunicationTools Communication;

	/**
	 * 
	 * @param _communicationManger a server Communication Tool
	 * @param clients a list of clients
	 */
	public removeOldclients(SyncClientList clients, ServerCommunicationTools communication) {
		super();
		Clients = clients;
		Communication = communication;
	}

	/**
	 * run
	 */
	public void run(){
		Communication.AddLog(">>>>started listing to old clients");
		System.out.println(">>>>started listing to old clients");
		
		//handle remove of old client
		while (Communication.getConnectionStatus()){
		
			String removed=Clients.RemoveOldClient();
			System.out.println(">>>>old clients thread removed "+removed);

		}
		
		System.out.println(">>>>Stoped listing to old clients");
		Communication.AddLog(">>>>Stoped listing to old clients");

	}
	
}
