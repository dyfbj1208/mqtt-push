//package io.mqttpush.mqttserver.beans;
//
//import org.apache.log4j.Logger;
//
//import io.mqttpush.mqttserver.service.MessagePushService;
//import io.netty.buffer.ByteBuf;
//import io.netty.channel.Channel;
//import io.netty.handler.codec.mqtt.MqttFixedHeader;
//import io.netty.handler.codec.mqtt.MqttMessageType;
//import io.netty.handler.codec.mqtt.MqttPublishMessage;
//import io.netty.handler.codec.mqtt.MqttPublishVariableHeader;
//import io.netty.handler.codec.mqtt.MqttQoS;
//import io.netty.util.AttributeKey;
//
///**
// * 消息发送runnable
// * @author acer
// *
// */
//public   class MsgSendRunable implements Runnable{
//
//	String deviceId;//设备号本来可以通过channel获取，但是考虑到channel可能已经失效或者直接传过来
//	Channel channel;
//	SendableMsg sendableMsg;
//	Logger logger=Logger.getLogger(getClass());
//	MessagePushService messagePushService;
//
//	public MsgSendRunable(String deviceId,Channel channel, SendableMsg sendableMsg) {
//		super();
//		this.deviceId=deviceId;
//		this.channel = channel;
//		this.sendableMsg = sendableMsg;
//		messagePushService=ServiceBeans.getInstance().getMessagePushService();
//	}
//
//
//
///**
// * 给某个客户端
// * 实际发送的地方
// * 
// * 首先判断bytebuf是否
// */
//	public void run(){
//		
//
//		final SendableMsg sendableMsg=this.sendableMsg;
//		final Channel channel=this.channel;
//		final String deviceId=this.deviceId;
//		ByteBuf byteBuf=sendableMsg.getByteBuf();
//		
//		
//		if(channel==null||!channel.isActive()){
//			messagePushService.handle(sendableMsg, deviceId);
//			return;
//		}
//		
//		if(sendableMsg.getDupTimes()>ConstantBean.MAX_ERROR_SENT){
//			messagePushService.handle(sendableMsg, deviceId);
//			return;
//		}
//		
//	 	if(byteBuf!=null){
//	 		AttributeKey<MqttQoS> attributeKey=AttributeKey.valueOf(sendableMsg.getTopname());  
//	 		
//	 		MqttQoS qosLevel=MqttQoS.AT_LEAST_ONCE;
//	 		if(channel.hasAttr(attributeKey)){
//	 			qosLevel=channel.attr(attributeKey).get();
//	 			
//	 			if(qosLevel==MqttQoS.EXACTLY_ONCE)
//	 					byteBuf.retain();
//	 		}
//	 		
//	 		MqttFixedHeader mqttFixedHeader=new MqttFixedHeader(
//	 				MqttMessageType.PUBLISH, 
//	 				sendableMsg.getDupTimes()>0, 
//	 				qosLevel, sendableMsg.isRetain(), 0);
//	 		
//	 		MqttPublishVariableHeader variableHeader=
//	 				new MqttPublishVariableHeader(sendableMsg.getTopname(),
//	 						sendableMsg.getShortmsgid());
//	 		
//	 		MqttPublishMessage mqttPublishMessage=
//	 				new MqttPublishMessage(mqttFixedHeader, variableHeader, byteBuf);
//	 		channel.writeAndFlush(mqttPublishMessage);
//	 		
//
//	 		
//	 		if(qosLevel==MqttQoS.EXACTLY_ONCE){
//	 			sendableMsg.setDupTimes(sendableMsg.getDupTimes()+1);
//		 		sendableMsg.getByteBuf().retain();
//				 channel.attr(ConstantBean.LASTSENT_KEY).set(sendableMsg);
//		 		
//	 		}
//	 		
//	 	}
//	 	else{
//	 		logger.warn("send  msg error msg is null");
//	 	}
//	}
//	
//}
