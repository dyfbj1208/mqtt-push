package io.mqttpush.mqttserver.mqttclient.service;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.handler.codec.mqtt.MqttConnectMessage;
import io.netty.handler.codec.mqtt.MqttConnectPayload;
import io.netty.handler.codec.mqtt.MqttConnectVariableHeader;
import io.netty.handler.codec.mqtt.MqttFixedHeader;
import io.netty.handler.codec.mqtt.MqttMessageIdVariableHeader;
import io.netty.handler.codec.mqtt.MqttMessageType;
import io.netty.handler.codec.mqtt.MqttPublishMessage;
import io.netty.handler.codec.mqtt.MqttPublishVariableHeader;
import io.netty.handler.codec.mqtt.MqttQoS;
import io.netty.handler.codec.mqtt.MqttSubscribeMessage;
import io.netty.handler.codec.mqtt.MqttSubscribePayload;
import io.netty.handler.codec.mqtt.MqttTopicSubscription;
import io.netty.handler.codec.mqtt.MqttUnsubscribeMessage;
import io.netty.handler.codec.mqtt.MqttUnsubscribePayload;
import io.netty.handler.codec.mqtt.MqttVersion;
import io.netty.util.AttributeKey;

/**
 * 
 * @author tianzhenjiu
 *
 */
public class DefaultApiService implements ApiService{

	Channel channel;
	
	Logger logger=Logger.getLogger(getClass());
	
	
	static DefaultApiService defaultApiService;
	
	public DefaultApiService() {
		
	}
	
	/**
	 * 单例模式
	 * @return
	 */
	public  static DefaultApiService intanceof(){
		
		if(defaultApiService==null){
			defaultApiService=new DefaultApiService();
		}
		return defaultApiService;
	}
	
	@Override
	public void login(String deviceId,String username, String password) {
		
		MqttFixedHeader mqttFixedHeader=new MqttFixedHeader(MqttMessageType.CONNECT,
				false, MqttQoS.AT_MOST_ONCE, false, 0);
		
		MqttConnectVariableHeader variableHeader=new MqttConnectVariableHeader(
				MqttVersion.MQTT_3_1.protocolName(), MqttVersion.MQTT_3_1.protocolLevel(), true, true, false, 0,false, false, 10);
		
		MqttConnectPayload payload=new MqttConnectPayload(deviceId, null, null, username, password.getBytes());
		MqttConnectMessage connectMessage=new MqttConnectMessage(mqttFixedHeader, variableHeader, payload);
		channel.writeAndFlush(connectMessage);
	}

	@Override
	public void subscribe(String topname, MqttQoS qoS) {
		
		
		if(!islogin()){
			logger.info("未登录");
			return;
		}
		
		List<MqttTopicSubscription> topicSubscriptions=new ArrayList<>();
		topicSubscriptions.add(new MqttTopicSubscription(topname, qoS));
		
		
		MqttFixedHeader mqttFixedHeader=
				new MqttFixedHeader(MqttMessageType.SUBSCRIBE,false, MqttQoS.AT_LEAST_ONCE,false , 0);
		
		MqttMessageIdVariableHeader variableHeader=
				MqttMessageIdVariableHeader.from(Math.abs(Integer.valueOf(topname.hashCode()).shortValue()));
		MqttSubscribePayload payload=new MqttSubscribePayload(topicSubscriptions);
		MqttSubscribeMessage mqttSubscribeMessage=new MqttSubscribeMessage(mqttFixedHeader, variableHeader, payload);
		
		getChannel().writeAndFlush(mqttSubscribeMessage);
	}

	@Override
	public void unsub(String topname) {
		if(!islogin()){
			logger.info("未登录");
			return;
		}
		
		List<String> topicSubscriptions=new ArrayList<>();
		topicSubscriptions.add(topname);
		
		
		MqttFixedHeader mqttFixedHeader=
				new MqttFixedHeader(MqttMessageType.UNSUBSCRIBE,false, MqttQoS.AT_LEAST_ONCE,false , 0);
		
		MqttMessageIdVariableHeader variableHeader=
				MqttMessageIdVariableHeader.from(Math.abs(Integer.valueOf(topname.hashCode()).shortValue()));
		MqttUnsubscribePayload payload=new MqttUnsubscribePayload(topicSubscriptions);
		MqttUnsubscribeMessage mqttSubscribeMessage=new MqttUnsubscribeMessage(mqttFixedHeader, variableHeader, payload);
		getChannel().writeAndFlush(mqttSubscribeMessage);
	}

	@Override
	public void pubMsg(String topname, byte[] bs, MqttQoS qoS) {
		
		if(!islogin()){
			logger.info("未登录");
			return;
		}
			
		ByteBuf byteBuf=Unpooled.wrappedBuffer(bs);
		
		int messageid=Math.abs(Integer.valueOf(bs.hashCode()).shortValue());
		MqttFixedHeader mqttFixedHeader=
				new MqttFixedHeader(MqttMessageType.PUBLISH,false, qoS,false , 0);
 		
 		MqttPublishVariableHeader variableHeader=
 				new MqttPublishVariableHeader(topname,messageid);
 		
 		MqttPublishMessage mqttPublishMessage=
 				new MqttPublishMessage(mqttFixedHeader, variableHeader, byteBuf);
 		getChannel().writeAndFlush(mqttPublishMessage);
	}

	public Channel getChannel() {
		return channel;
	}

	public void setChannel(Channel channel) {
		this.channel = channel;
	}

	/**
	 * 判断是否登录
	 * @return
	 */
	boolean islogin(){
		
		Channel channel=getChannel();
		AttributeKey<Boolean> loginKey = AttributeKey.valueOf("login");
		return channel.hasAttr(loginKey)&&channel.attr(loginKey).get();
		
	}
}
