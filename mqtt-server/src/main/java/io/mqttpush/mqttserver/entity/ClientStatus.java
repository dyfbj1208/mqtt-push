package io.mqttpush.mqttserver.entity;

/**
 * 客户端状态
 * @author acer
 *
 */
public class ClientStatus {

	private String deviceId;
	
	private  LineStatus status;
	
	
	public String getDeviceId() {
		return deviceId;
	}



	public void setDeviceId(String deviceId) {
		this.deviceId = deviceId;
	}



	public LineStatus getStatus() {
		return status;
	}



	public void setStatus(LineStatus status) {
		this.status = status;
	}



	/**
	 * 在线状态
	 * @author acer
	 *
	 */
	public  static enum LineStatus{
		
		online(0),unline(1);
		
		private final int value;
		
		LineStatus(int value){
    		this.value=value;
    	}
	}
}
