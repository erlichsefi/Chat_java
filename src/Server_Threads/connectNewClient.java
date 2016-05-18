package Server_Threads;


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
		while (communicationManger.getConnectionStatus()) {
			ClientSocket Client = null;
			Client = communicationManger.new ClientSocket(communicationManger.accept(),
					ClientsIds++);
			if (!communicationManger.getConnectionStatus())
				break;
			if (Client.OpenStream(Clients.GetClientList())) {
				ListeningThread listen=new ListeningThread(Client,Clients);
				listen.start();
				communicationManger.putExit(new message("SERVER",Client.getClientName(),Tools.MessageType.YOU_HAVE_CONNECTED,null));
				Clients.addClient(Client,listen);
				communicationManger.AddLog(Client.getClientName()+" connected");
			}

		}
		communicationManger.AddLog(">>>Stoped listing to new clients");

	}
}
