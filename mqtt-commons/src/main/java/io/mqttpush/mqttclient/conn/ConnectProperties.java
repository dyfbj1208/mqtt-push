package io.mqttpush.mqttclient.conn;

public class ConnectProperties {

	
	private String host="127.0.0.1";
	private Integer port=10000;
	private String subTopic;
	private String deviceId="000";
	private String username="user";
	private String password="user123456";
	private Integer pingtime=60;
	
	public ConnectProperties(String host, Integer port, String deviceId, String username,
			String password, Integer pingtime) {
		super();
		this.host = host;
		this.port = port;
		this.deviceId = deviceId;
		this.username = username;
		this.password = password;
		this.pingtime = pingtime;
	}
	
	public String getHost() {
		return host;
	}
	public Integer getPort() {
		return port;
	}
	public String getSubTopic() {
		return subTopic;
	}
	public String getDeviceId() {
		return deviceId;
	}
	public String getUsername() {
		return username;
	}
	public String getPassword() {
		return password;
	}
	public Integer getPingtime() {
		return pingtime;
	}
	public void setHost(String host) {
		this.host = host;
	}
	public void setPort(Integer port) {
		this.port = port;
	}
	public void setSubTopic(String subTopic) {
		this.subTopic = subTopic;
	}
	public void setDeviceId(String deviceId) {
		this.deviceId = deviceId;
	}
	public void setUsername(String username) {
		this.username = username;
	}
	public void setPassword(String password) {
		this.password = password;
	}
	public void setPingtime(Integer pingtime) {
		this.pingtime = pingtime;
	}

	
	
	
	
}
