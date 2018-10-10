package io.mqttpush.mqttserver.util;

public class OfflineMessage extends ChatMessage{

	String lastChatDeviceId;
	
	
	public OfflineMessage(MessageType type, String deviceId) {
		super(type, deviceId);
	}


	public OfflineMessage(MessageType type, String deviceId, String lastChatDeviceId) {
		super(type, deviceId);
		this.lastChatDeviceId = lastChatDeviceId;
	}


	/**
	 * @return the lastChatDeviceId
	 */
	public String getLastChatDeviceId() {
		return lastChatDeviceId;
	}


	/**
	 * @param lastChatDeviceId the lastChatDeviceId to set
	 */
	public void setLastChatDeviceId(String lastChatDeviceId) {
		this.lastChatDeviceId = lastChatDeviceId;
	}


	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return super.toString()+"OfflineMessage [lastChatDeviceId=" + lastChatDeviceId + "]";
	}
	
	

	
}
