package io.mqttpush.mqttserver.util;

import java.util.Arrays;

/**
 * 暂存消息对象
 * @author tianzhenjiu
 *
 */
public class StashMessage extends ChatMessage{

	
	Long timestamp;
	byte[] content;
	
	
	public StashMessage(MessageType type, String deviceId) {
		super(type, deviceId);
	}

	

	public StashMessage(MessageType type, String deviceId, Long timestamp, byte[] content) {
		super(type, deviceId);
		this.timestamp = timestamp;
		this.content = content;
	}



	/**
	 * @return the timestamp
	 */
	public Long getTimestamp() {
		return timestamp;
	}


	/**
	 * @return the content
	 */
	public byte[] getContent() {
		return content;
	}


	/**
	 * @param timestamp the timestamp to set
	 */
	public void setTimestamp(Long timestamp) {
		this.timestamp = timestamp;
	}


	/**
	 * @param content the content to set
	 */
	public void setContent(byte[] content) {
		this.content = content;
	}


	
	@Override
	public String toString() {
		return super.toString()+"StashMessage [timestamp=" + timestamp + ", content=" + Arrays.toString(content) + "]";
	}
	
	
	

}
