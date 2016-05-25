package Client_Threads;

import Communication.ClientCommunicationTools;
import Tools.message;

/**
 * this thread gets a client Communication Tools
 * and writing the messages in the queues to the socket
 * @author erlichsefi
 *
 */
public class ClientWriter extends Thread{
	/**
	 * a client Communication Tool
	 */
	private  ClientCommunicationTools communication;
	
	
	public ClientWriter(ClientCommunicationTools _con){
		super("ClientWriter");
		communication=_con;
	}
	
	
	
	/**
	 * run
	 */
	public void run() {
		System.out.println("Client writer started");
		
		while (communication.getConnectionStatus()) {
			message m = communication.getExitMessage();
			
			System.out.println(communication.getName()+" send a message : "+m);
		
			if (!communication.getConnectionStatus()) {
				break;
			}
			communication.Send(m);
		}
		
		
		System.out.println("Client writer exit");

		synchronized(this){
		     notify();
		}

	}
	
	/**
	 * Stopping the thread
	 * @return true if performed
	 */
	public boolean stopThreadandWait() {
		if (communication.getConnectionStatus()){
			communication.SetConnectionStatus(false);
		synchronized(this){
			try {
				wait();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		}
		return true;
	}



	

}
