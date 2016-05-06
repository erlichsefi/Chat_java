package Server_Threads;



import Communication.ServerCommunicationTools;
import Communication.SyncClientList;



/**
 * class that Responsible on all of the server issues: Communication, managing
 * the incoming and outgoing data.
 * @author sefi erlich
 *
 */
public class ServerManger {
	/**
	 * the thread responsible on the Communication part of the servers
	 */
	private ServerCommunicationTools CommunicationManger;
	/**
	 * a list contains all client sockets
	 */
	private SyncClientList Clients;

	/**
	 * a thread responsible to send to client
	 */
	private ServerWriter write;
	/**
	 * a thread responsible to handle the queues
	 */
	private QueuesManger message;
	/**
	 * a thread responsible to add new client
	 */
	private connectNewClient NewClients;
	/**
	 * a thread responsible to remove old clients
	 */
	private removeOldclients OldClients;


	/**
	 * Contractor for a default Server
	 */
	public ServerManger() {
		Clients = new SyncClientList();
		CommunicationManger = new ServerCommunicationTools(Clients);
		write=new ServerWriter(CommunicationManger, Clients);
		message=new QueuesManger(CommunicationManger, Clients);
		NewClients = new connectNewClient(CommunicationManger,Clients);
		OldClients=new removeOldclients(Clients, CommunicationManger);

	}


	/**
	 * Contractor for a  Server, with a different port
	 * @param testPort the port to connect to
	 */
	public ServerManger(int testPort) {
		Clients =  new SyncClientList();
		CommunicationManger = new ServerCommunicationTools(Clients,   testPort);
		OldClients=new removeOldclients(Clients, CommunicationManger);

	}



	/**
	 * connect
	 */
	public void connect(){
		if (CommunicationManger.Connect()) {
			NewClients.start();
			OldClients.start();
			//sending
			write.start();
			//Moving data around
			message.start();
		}
	}



	/**
	 * disconnect all the clients
	 */
	public void serverDisconnect() {
		CommunicationManger.ServerDisconnect();
		CommunicationManger.SetConnectionStatus(false);
	}


	/**
	 * get messages to show in the console
	 * @return
	 */
	public String TakeNextFromConsle() {
		if (CommunicationManger.getConnectionStatus())
			return CommunicationManger.TakeLog();
		if (CommunicationManger.IsConsleLog())
			return CommunicationManger.TakeLog();
		return null;
	}





}
