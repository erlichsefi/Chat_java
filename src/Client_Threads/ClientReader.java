package Client_Threads;



import java.util.ArrayList;

import Communication.ClientCommunicationTools;
import Tools.message;

/**
 * this thread gets a client Communication Tools
 * and reading messages to the queues
 * @author erlichsefi
 *
 */
public class ClientReader extends Thread {
	/**
	 * a client Communication Tool
	 */
	private ClientCommunicationTools communication;
	/**
	 * 
	 * @param _communication a client Communication Tool
	 */
	public ClientReader(ClientCommunicationTools _communication ){
		super("ClientReader");
		communication=_communication;

	}

	
	
	/**
	 * run
	 */
	public void run() {		
		System.out.println("Client reader started");

		while (communication.getConnectionStatus()) {
			Object m = communication.Response();
			if (!communication.getConnectionStatus())
				break;
			if (m != null) {
				System.out.println(communication.getName()+" got a message : "+m);
				message s=(message)m;
				handleM(s);
			}
		}
		System.out.println("Client reader end");

	}

	@SuppressWarnings("unchecked")
	public void handleM(message s) {
		int type=s.getType();
		if  (type==Tools.MessageType.HERE_A_LIST){
			communication.addConsole(((ArrayList<String>)s.getObjectMessage()).toString());

		}
		else if  (type==Tools.MessageType.YOU_HAVE_DISCONNECTED){
			communication.disconnect();
			communication.SetConnectionStatus(false);
			communication.addConsole("disconnected");
			communication.PutExit(new message(null,null,0,"relese from take"));
		}
		else if  (type==Tools.MessageType.YOU_HAVE_CONNECTED){
			communication.addConsole("connected");

		}
		else if  (type==Tools.MessageType.TO_ONE){
			communication.addConsole(s.getSrc()+" :  "+s.getStringMessage());
		}
		else if  (type==Tools.MessageType.TO_ALL){
			communication.addConsole(" broadcast : "+s.getStringMessage());
		}
		else if  (type==Tools.MessageType.NO_SUCH_CLIENT){
			communication.addConsole(" NO SUCH CLIENT : "+s.getStringMessage());
			System.out.println(" NO SUCH CLIENT : : "+s.getStringMessage());
		}
		else{
			communication.addConsole("* GOTEN Worng Message *");

		}
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
