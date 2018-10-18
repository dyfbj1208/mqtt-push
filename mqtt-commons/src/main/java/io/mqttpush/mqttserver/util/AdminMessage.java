package io.mqttpush.mqttserver.util;

/**
 * 消息对象
 * @author tianzhenjiu
 *
 */
public class AdminMessage {

	MessageType type;
	String deviceId;
	
	
	/**
	 *上线，下线，暂存此消息,
	 * @author tianzhenjiu
	 *
	 */
	public static enum MessageType{
		ONLINE,OFFLINE,STASH,SAVEMSG
	}
	
	
	public AdminMessage(MessageType type, String deviceId) {
		super();
		this.type = type;
		this.deviceId = deviceId;
	}
	
	/**
	 * @return the deviceId
	 */
	public String getDeviceId() {
		return deviceId;
	}
	
	/**
	 * @param deviceId the deviceId to set
	 */
	public void setDeviceId(String deviceId) {
		this.deviceId = deviceId;
	}
	
	@Override
	public String toString() {
		return "AdminMessage [type=" + type + ", deviceId=" + deviceId + "]";
	}

	/**
	 * @return the type
	 */
	public MessageType getType() {
		return type;
	}

	/**
	 * @param type the type to set
	 */
	public void setType(MessageType type) {
		this.type = type;
	}
	
}
