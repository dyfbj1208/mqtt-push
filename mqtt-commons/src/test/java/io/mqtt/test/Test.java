package io.mqtt.test;
import java.util.Properties;
import java.util.Scanner;

import io.mqttpush.mqttclient.conn.Connetor;
import io.mqttpush.mqttclient.service.DefaultApiService;
import io.mqttpush.mqttclient.service.DefaultMessageListener;
import io.netty.handler.codec.mqtt.MqttQoS;

public class Test {

	public static void main(String[] args) throws Exception {
		
		Connetor connetor=new Connetor();
		final DefaultApiService apiService=new DefaultApiService();
		final DefaultMessageListener defaultMessageListener=new DefaultMessageListener();
		Properties properties=new Properties();
		
		properties.put("host", "localhost");
		properties.put("port", 10000);
		properties.put("username", "user");
		properties.put("password", "user123456");
		
		properties.put("pingtime", 60);
		properties.put("recontimes", 5);
		properties.put("deviceId", "123456");
		new Thread(){
			
			public  void run(){
				
				while(true){
					
					
					try {
						Scanner scanner=new Scanner(System.in);
						
						apiService.pubMsg("/root",scanner.nextLine().getBytes() ,MqttQoS.EXACTLY_ONCE);
					} catch (Exception e) {
						e.printStackTrace();
					}
					
				} 
			}
		}.start();
		connetor.connection(properties,apiService,defaultMessageListener).sync();
	}

}
