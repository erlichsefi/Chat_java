package Server_Threads;


import Communication.ServerCommunicationTools;
import Communication.SyncClientList;
import Tools.message;

public class ServerWriter extends Thread {
	/**
	 * 
	 * a client Communication Tool
	 */
	private ServerCommunicationTools  Communication;
	/**
	 *  a list of clients
	 */
	private SyncClientList Clients;



	/**
	 * 
	 * @param communication a server Communication Tool
	 * @param clients a list of clients
	 */
	public ServerWriter(ServerCommunicationTools communication, SyncClientList clients) {
		super("ServerReader");
		Communication = communication;
		Clients = clients;
	}


	/**
	 * run
	 */
	public void run() {
		Communication.AddLog(">start sending messages");
		System.out.println(">>start sending messages");
		
		while (Communication.getConnectionStatus()) {
			
			message m = Communication.TakeExit();
			if (!Communication.getConnectionStatus()) {
				break;
			}
			
			ServerCommunicationTools.ClientSocket client = Clients.findClient(m.getDst());
			System.out.println("Server send to id:"+client.getClientId() +" with name "+client.getClientName()+" message"+m);

			if (client != null && client.isConnected()){
			client.SendMassage(m);
			}

		}
		
		
		System.out.println(">>ended sending messages");
		Communication.AddLog(">Stop sending messages");
		Communication.ServerDisconnect();
	}



}
