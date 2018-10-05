package io.mqttpush.mqttserver.exception;

/**
 * 
 * @author tianzhenjiu
 *
 */
public class SendException extends RuntimeException{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	SendError sendError;
	
	public static enum SendError{
		
		CHANNEL_OFF,BUFF_FREED,FAIL_MAX_COUNT
	}

	public SendException(SendError sendError) {
		super();
		this.sendError = sendError;
	}

	/**
	 * @return the sendError
	 */
	public SendError getSendError() {
		return sendError;
	}

	/**
	 * @param sendError the sendError to set
	 */
	public void setSendError(SendError sendError) {
		this.sendError = sendError;
	}
	
	
}
