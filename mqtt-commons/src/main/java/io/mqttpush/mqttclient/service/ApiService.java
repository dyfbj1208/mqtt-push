package io.mqttpush.mqttclient.service;

import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.handler.codec.mqtt.MqttQoS;

/**
 * 定义所有的操作
 * @author acer
 *
 */
public interface ApiService {

	public  ChannelFuture  login(String deviceId,String username,String password);
	
	public ChannelFuture subscribe(String topname,MqttQoS mqttQoS);
	
	public ChannelFuture unsub(String topname);
	
	public ChannelFuture pubMsg(String topname,byte[] bs,MqttQoS qoS);
	
	public Channel getChannel();

	public void setChannel(Channel channel);
}
