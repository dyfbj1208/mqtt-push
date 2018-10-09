package io.mqttpush.mqttserver.mqttclient.service;

import io.netty.channel.Channel;
import io.netty.handler.codec.mqtt.MqttQoS;

/**
 * 定义所有的操作
 * @author acer
 *
 */
public interface ApiService {

	public  void  login(String deviceId,String username,String password);
	
	public void subscribe(String topname,MqttQoS mqttQoS);
	
	public void unsub(String topname);
	
	public void pubMsg(String topname,byte[] bs,MqttQoS qoS);
	
	public Channel getChannel();

	public void setChannel(Channel channel);
}
