package io.mqttpush.mqttserver;

import io.mqttpush.mqttserver.middle.MqttTcpServer;

public class BootServer {

	public static void main(String[] args) {
		
		
		Integer port=null;
		if(args!=null&&args.length>0) {
			
			try {
				port=Integer.parseInt(args[0]);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		MqttTcpServer mqttServer=new MqttTcpServer();
		try {
			mqttServer.start(port);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		
	}

}
