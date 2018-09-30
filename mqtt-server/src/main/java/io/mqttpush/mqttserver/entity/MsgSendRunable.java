package io.mqttpush.mqttserver.entity;

import java.util.concurrent.TimeUnit;

import org.apache.log4j.Logger;

import io.mqttpush.mqttserver.service.MessagePushService.MessagePushFailHandle;
import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import io.netty.handler.codec.mqtt.MqttFixedHeader;
import io.netty.handler.codec.mqtt.MqttMessageType;
import io.netty.handler.codec.mqtt.MqttPublishMessage;
import io.netty.handler.codec.mqtt.MqttPublishVariableHeader;
import io.netty.handler.codec.mqtt.MqttQoS;
import io.netty.util.AttributeKey;
import io.netty.util.concurrent.Future;

/**
 * 消息发送runnable
 * @author acer
 *
 */
public   class MsgSendRunable implements Runnable{

	String deviceId;//设备号本来可以通过channel获取，但是考虑到channel可能已经失效或者直接传过来
	Channel channel;
	SendableMsg sendableMsg;
	MessagePushFailHandle failHandle;
	Logger logger=Logger.getLogger(getClass());

	public MsgSendRunable(String deviceId,Channel channel, SendableMsg sendableMsg,MessagePushFailHandle failHandle) {
		super();
		this.deviceId=deviceId;
		this.channel = channel;
		this.sendableMsg = sendableMsg;
		this.failHandle=failHandle;
	}



/**
 * 给某个客户端
 * 实际发送的地方
 * 
 * 首先判断bytebuf是否
 */
	public void run(){
		

		final SendableMsg sendableMsg=this.sendableMsg;
		final Channel channel=this.channel;
		final String deviceId=this.deviceId;
		ByteBuf byteBuf=sendableMsg.getByteBuf();
		
		
		if(channel==null||!channel.isActive()){
			failHandle.handle(sendableMsg, deviceId);
			return;
		}
		
		if(sendableMsg.getDupTimes()>2){
			failHandle.handle(sendableMsg, deviceId);
			return;
		}
		
	 	if(byteBuf!=null){
	 		AttributeKey<MqttQoS> attributeKey=AttributeKey.valueOf(sendableMsg.getTopname());  
	 		
	 		MqttQoS qosLevel=MqttQoS.AT_LEAST_ONCE;
	 		if(channel.hasAttr(attributeKey)){
	 			qosLevel=channel.attr(attributeKey).get();
	 			
	 			if(qosLevel==MqttQoS.EXACTLY_ONCE)
	 					byteBuf.retain();
	 		}
	 		
	 		MqttFixedHeader mqttFixedHeader=new MqttFixedHeader(
	 				MqttMessageType.PUBLISH, 
	 				sendableMsg.getDupTimes()>0, 
	 				qosLevel, sendableMsg.isRetain(), 0);
	 		
	 		MqttPublishVariableHeader variableHeader=
	 				new MqttPublishVariableHeader(sendableMsg.getTopname(),
	 						sendableMsg.getShortmsgid());
	 		
	 		MqttPublishMessage mqttPublishMessage=
	 				new MqttPublishMessage(mqttFixedHeader, variableHeader, byteBuf);
	 		channel.writeAndFlush(mqttPublishMessage);
	 		
	 		//logger.warn("send  msg msgid:"+variableHeader.messageId());
	 		if(qosLevel==MqttQoS.EXACTLY_ONCE){
	 			sendableMsg.setDupTimes(sendableMsg.getDupTimes()+1);
		 		sendableMsg.getByteBuf().retain();
		 		Future future= channel.eventLoop().schedule(new MsgSendRunable(deviceId,channel, sendableMsg,failHandle),1,TimeUnit.MINUTES);
		 		AttributeKey<Future> taskSend=AttributeKey.valueOf("recv:"+sendableMsg.getShortmsgid());
		 		channel.attr(taskSend).set(future);
		 		
	 		}
	 		
	 	}
	 	else{
	 		logger.warn("send  msg error msg is null");
	 	}
	}
	
}
