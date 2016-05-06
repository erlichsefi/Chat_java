package Server_Threads;

import java.util.ArrayList;

import Communication.ServerCommunicationTools;
import Communication.SyncClientList;
import Tools.message;

public class QueuesManger extends Thread{
	/**
	 * 
	 * a client Communication Tool
	 */
	private	ServerCommunicationTools  Communication;
	/**
	 *  a list of clients
	 */
	private SyncClientList Clients;

	/**
	 * 
	 * @param _communicationManger a client Communication Tool
	 * @param clients a list of clients
	 */
	public QueuesManger(ServerCommunicationTools communication, SyncClientList clients) {
		super("MessageManger");
		Communication = communication;
		Clients = clients;
	}

	
	/**
	 * run
	 */
	public void run() {
		Communication.AddLog(">>start handling messages");
		while (Communication.getConnectionStatus()){
			message m=Communication.TakeEntry();
			ServerhandleM(m);
		}
		Communication.AddLog(">>stoped handleing messages");
	}



	/**
	 * server function that handle the message 
	 * @param m a message to handle
	 */
	private void ServerhandleM(message m) {
		int m1=m.getType();
		if(m1==Tools.MessageType.GET_LIST){
			Communication.putExit(new message("server",m.getSrc(),Tools.MessageType.HERE_A_LIST,Clients.GetClientList()));
		}
		else if(m1==Tools.MessageType.LETS_CONNECT){
			sendToall(new message(m.getSrc(),null,Tools.MessageType.TO_ALL,m.getSrc()+" was connected"));
				
		}
		else if(m1==Tools.MessageType.TO_ONE){
			if (Clients.findClient(m.getSrc())!=null)
			Communication.putExit(new message(m.getSrc(),m.getDst(),Tools.MessageType.TO_ONE,m.getStringMessage()));
			else
				Communication.putExit(new message("server",m.getSrc(),Tools.MessageType.NO_SUCH_CLIENT,m.getDst()));
		}
		else if(m1==Tools.MessageType.TO_ALL){
			sendToall(new message(m.getSrc(),null,Tools.MessageType.TO_ALL,m.getStringMessage()));
		}
		else if(m1==Tools.MessageType.LETS_DISCONNECT){
			Communication.AddLog(m.getSrc()+" disconnected");
			sendToall(new message(m.getSrc(),null,Tools.MessageType.TO_ALL,m.getSrc()+" as left"));
			send(m.getSrc(),new message("server",m.getSrc(),Tools.MessageType.YOU_HAVE_DISCONNECTED,null));
			disconnectClient(m.getSrc());
		}
		
		else if(m1==Tools.MessageType.RELERSE){
			System.out.println("a message to br relese the take");
			System.out.println(Communication.getConnectionStatus());
		}
	}
	
	/**
	 * disconnect from client
	 * @param name the client to dissconnect
	 */
	public void disconnectClient(String name) {
		ServerCommunicationTools.ClientSocket s=Clients.findClient(name);
		s.disconnect();
	}
	
	/**
	 * send message
	 * @param name the client to send to
	 * @param m the message
	 */
	public void send(String name, message m) {
		ServerCommunicationTools.ClientSocket s=Clients.findClient(name);
		s.SendMassage(m);
	}
	
	/**
	 * send a message to all the clients 
	 * @param m the message to send, the dest client will change
	 */
	private void sendToall(message m){
		ArrayList<String> dst=Clients.GetClientList();
		for (String d:dst){
			Communication.putExit(new message(m,d));
		}
	}





}
