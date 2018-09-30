package io.mqttpush.mqttserver;

import io.mqttpush.mqttserver.middle.MqttTcpServer;

public class BootServer {

	public static void main(String[] args) {
		MqttTcpServer mqttServer=new MqttTcpServer();
		try {
			mqttServer.start(10000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		
	}

}
