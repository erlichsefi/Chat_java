package Tools;

/**
 * all type of messages & bit more
 * @author erlichsefi
 *
 */
public class MessageType {


/**
 * send message to all the clients
 */
public  static  int TO_ALL=1;
/**
 * send message to a client, the name of the client should be in the dst
 */
public  static  int TO_ONE=3;
/**
 * ask for a list of connected clients
 */
public  static  int GET_LIST=2;
/**
 * ask to disconnect
 */
public  static  int LETS_DISCONNECT=4;
/**
 * ask to connect
 */
public  static  int LETS_CONNECT=5;
/**
 * Confirmation on connection
 */
public  static  int YOU_HAVE_CONNECTED=6;
/**
 * Confirmation on disconnection
 */
public  static  int YOU_HAVE_DISCONNECTED=7;
/**
 * the data if a client list
 */
public  static  int HERE_A_LIST=8;
/**
 * you have send to a client with wrong name
 */
public static final int NO_SUCH_CLIENT = 10;
/**
 * a message to send to release the queues (OF YOUR SLEF)
 */
public static final int RELERSE = 11;
/**
 * the name ois taken
 */
public static final int NameTaken = 9;


}
