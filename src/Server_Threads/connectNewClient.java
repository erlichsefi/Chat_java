package Server_Threads;


import java.net.Socket;

import Communication.ServerCommunicationTools;
import Communication.SyncClientList;
import Communication.ServerCommunicationTools.ClientSocket;
import Tools.message;

/**
 * this thread responsible of connecting new client
 * and adding them to the client list
 * @author erlichsefi
 *
 */
public class connectNewClient extends Thread {
	/**
	 * 
	 * a client Communication Tool
	 */
	private ServerCommunicationTools communicationManger;
	/**
	 *  client id to give away
	 */
	private int ClientsIds=1;
	/**
	 *  a list of clients
	 */
	private SyncClientList Clients;

	/**
	 * 
	 * @param _communicationManger a server Communication Tool
	 * @param clients a list of clients
	 */
	public connectNewClient(ServerCommunicationTools _communicationManger, SyncClientList clients2) {
		super("Connect New Clients");
		Clients = clients2;
		communicationManger=_communicationManger;
	}

	/**
	 * run
	 */
	public void run() {
		
		communicationManger.AddLog(">>>started listing to new clients");
		System.out.println(">>>started listing to new clients");
		
		while (communicationManger.getConnectionStatus()) {
			Socket socket=communicationManger.accept();
			
			if (socket!=null){
			
				if (!communicationManger.getConnectionStatus())
				break;
			
			 ClientSocket Client = communicationManger.new ClientSocket(socket,
					ClientsIds++);

  
			 if (Client.OpenStream(Clients.GetClientList())) {
				ListeningThread listen=new ListeningThread(Client,Clients);
				listen.start();
				communicationManger.putExit(new message("SERVER",Client.getClientName(),Tools.MessageType.YOU_HAVE_CONNECTED,null));
				Clients.addClient(Client,listen);
				communicationManger.AddLog(Client.getClientName()+" connected");
			}
			 else{
				 Client.disconnect();
			 }
		
			System.out.println("new client was add : "+Client.getClientName()+" with id "+Client.getClientId());
			
		}
		}
		
		System.out.println(">>>Stoped listing to new clients");
		communicationManger.AddLog(">>>Stoped listing to new clients");

	}
}
