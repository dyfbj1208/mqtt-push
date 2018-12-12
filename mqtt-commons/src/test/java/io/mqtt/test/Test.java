package io.mqtt.test;
import java.util.Scanner;

import io.mqttpush.mqttclient.conn.ConnectProperties;
import io.mqttpush.mqttclient.conn.Connetor;
import io.mqttpush.mqttclient.service.DefaultApiService;
import io.mqttpush.mqttclient.service.DefaultMessageListener;
import io.netty.handler.codec.mqtt.MqttQoS;

public class Test {

	public static void main(String[] args) throws Exception {
		
		
		final DefaultApiService apiService=new DefaultApiService();
		final DefaultMessageListener defaultMessageListener=new DefaultMessageListener();

		ConnectProperties  properties=new ConnectProperties("192.168.31.120",8008,"admintest","user","user123456",5);
	
		new Thread(){
			
			public  void run(){
				
				while(true){
					
					
					try {
						Scanner scanner=new Scanner(System.in);
						
						apiService.pubMsg("/root/chat/one2one/agent_test",scanner.nextLine().getBytes() ,MqttQoS.EXACTLY_ONCE);
					} catch (Exception e) {
						e.printStackTrace();
					}
					
				} 
			}
		}.start();
		
		Connetor connetor=new Connetor(properties,apiService,defaultMessageListener);
		connetor.connection();
	}

}
