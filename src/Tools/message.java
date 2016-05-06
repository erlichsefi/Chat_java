package Tools;

import java.io.Serializable;

public class message implements Serializable{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	/**
	 * the source client
	 */
	private String src;
	/**
	 * the destination client
	 */
	private String dest;
	/**
	 * the message type
	 */
	private int messageType;
	/*
	 * the data to send
	 */
	private Object data; 

	/**
	 * new message
	 * @param src src name
	 * @param dest dest name
	 * @param messageType the type
	 * @param data the data
	 */
	public message(String src, String dest, int messageType, Object data) {
		super();
		this.dest = dest;
		this.src = src;
		this.messageType = messageType;
		this.data = data;
	}




	/**
	 * Constructor to replace the name of the dst
	 * @param m the message
	 * @param dst the new dst
	 */
	public message(message m, String dst) {
		this.dest = dst;
		this.src = m.src;
		this.messageType = m.messageType;
		this.data = m.data;
	}




	/**
	 * get as a String
	 * @return
	 */
	public String getStringMessage() {
		if (data!=null){
			return (String)data;
		}
		return null;
	}
	
	/**
	 * get the data as a object
	 * @return
	 */
	public Object getObjectMessage() {
		return data;
	}


	/**
	 * get the type
	 * @return
	 */
	public int getType() {
		return messageType;
	}


	/**
	 * get dst
	 * @return
	 */
	public String getDst() {
		return dest;
	}


	/**
	 * get src
	 * @return
	 */
	public String getSrc() {
		return src;
	}

	@Override
	public String toString() {
		return "message [src=" + src + ", dest=" + dest + ", messageType=" + messageType + ", data=" + data + "]";
	}


}
