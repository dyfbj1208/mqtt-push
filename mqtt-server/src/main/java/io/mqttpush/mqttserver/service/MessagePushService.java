package io.mqttpush.mqttserver.service;

import java.util.function.Consumer;

import org.apache.log4j.Logger;

import io.mqttpush.mqttserver.beans.ConstantBean;
import io.mqttpush.mqttserver.beans.SendableMsg;
import io.mqttpush.mqttserver.beans.ServiceBeans;
import io.mqttpush.mqttserver.exception.SendException;
import io.mqttpush.mqttserver.exception.SendException.SendError;
import io.mqttpush.mqttserver.util.AdminMessage.MessageType;
import io.mqttpush.mqttserver.util.ByteBufEncodingUtil;
import io.mqttpush.mqttserver.util.StashMessage;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.handler.codec.mqtt.MqttFixedHeader;
import io.netty.handler.codec.mqtt.MqttMessageType;
import io.netty.handler.codec.mqtt.MqttPublishMessage;
import io.netty.handler.codec.mqtt.MqttPublishVariableHeader;
import io.netty.handler.codec.mqtt.MqttQoS;
import io.netty.util.AttributeKey;

/**
 * message 实际发送服务
 * 
 * @author tzj
 * 
 */
public class MessagePushService {

	final int retimes = 3;

	Logger logger = Logger.getLogger(getClass());

	MQManagerService mqservice;

	TopicService topicService;

	ChannelUserService channelUserService;

	public MessagePushService() {

		ServiceBeans serviceBeans = ServiceBeans.getInstance();
		channelUserService = serviceBeans.getChannelUserService();
		topicService = serviceBeans.getTopicService();
		mqservice = serviceBeans.getManagerService();

	}

	/**
	 * 进行组播
	 * 
	 * @param sendableMsg
	 */
	public void sendMsg(final SendableMsg sendableMsg) {

		Consumer<String> consumer= (deviceId) -> {

			Channel channel = channelUserService.channel(deviceId);
			sendMsgForChannel(sendableMsg, channel);
		};
		
		topicService.channelsSend(sendableMsg.getTopname(),consumer);

	}

	/**
	 * 进行单播
	 * 
	 * @param sendableMsg
	 */
	public void sendMsgForChannel(final SendableMsg sendableMsg, Channel channel) {

		String deviceId = channelUserService.deviceId(channel);
		try {
			if (channel != null && channel.isActive()) {
				sendableMsg.getByteBuf().retain();
				ChannelFuture channelFuture=sendMsg(sendableMsg, channel, deviceId);
				if(!channelFuture.isSuccess()) {
					if(channelFuture.cause() instanceof SendException) {
					
						handleAndStash(new StashMessage(MessageType.STASH, deviceId, System.currentTimeMillis(), sendableMsg.getContent()),
								((SendException)channelFuture.cause()).getSendError());
					}else {
						handleAndStash(new StashMessage(MessageType.STASH, deviceId, System.currentTimeMillis(), sendableMsg.getContent()),SendError.CHANNEL_OFF);
					}
					
				}
			} else {
				handleAndStash(new StashMessage(MessageType.STASH, deviceId, System.currentTimeMillis(), sendableMsg.getContent()),SendError.CHANNEL_OFF);
			}
		} catch (Exception e) {
			logger.debug("发送异常", e);
		}

	}
	
	
	/**
	 * 实际发送
	 * @param sendableMsg 发送的对象
	 * @param channel  接收方的channnel
	 * @param deviceId 发送放deviceid
	 * @return
	 */
	protected  ChannelFuture sendMsg(SendableMsg sendableMsg,Channel channel,String deviceId){
		

	
		ByteBuf byteBuf=sendableMsg.getByteBuf();
		
	
	
		if(channel==null||!channel.isActive()){
			return 	channel.newFailedFuture(new SendException(SendError.CHANNEL_OFF));
		}
		
		if(sendableMsg.getDupTimes()>ConstantBean.MAX_ERROR_SENT){
			return 	channel.newFailedFuture(new SendException(SendError.FAIL_MAX_COUNT));
		}
		
	 	if(byteBuf!=null&&byteBuf.isReadable()){
	 		ChannelFuture channelFuture=null;
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
	 		channelFuture=channel.writeAndFlush(mqttPublishMessage);
	 		

	 		
	 		if(qosLevel==MqttQoS.EXACTLY_ONCE){
	 			sendableMsg.setDupTimes(sendableMsg.getDupTimes()+1);
		 		sendableMsg.getByteBuf().retain();
				 channel.attr(ConstantBean.LASTSENT_KEY).set(sendableMsg);
		 		
	 		}
	 		
	 		return  channelFuture;
	 		
	 	}
	 	else{
	 		return channel.newFailedFuture(new SendException(SendError.BUFF_FREED));
	 	}
	}

	/**
	 * 发送通知给给admin
	 * @param topicname
	 * @param mqttPublishMessage
	 */
	public void send2Admin(ByteBuf payload) {

		MqttFixedHeader mqttFixedHeader=new MqttFixedHeader(
 				MqttMessageType.PUBLISH, false, 
 				MqttQoS.AT_LEAST_ONCE, false, 0);
 		
 		MqttPublishVariableHeader variableHeader=
 				new MqttPublishVariableHeader(ConstantBean.adminTopic,payload.hashCode());
		
 		MqttPublishMessage mqttPublishMessage=new MqttPublishMessage(mqttFixedHeader, variableHeader, payload);
 		
 		Consumer<String> consumer= (deviceId) -> {

			Channel channel = channelUserService.channel(deviceId);
			channel.writeAndFlush(mqttPublishMessage);
		};
 		//topicService.channelsSend(ConstantBean.adminTopic, consumer);
		
		topicService.channelsForGroup(ConstantBean.adminTopic, mqttPublishMessage);
 	
	}

	/**
	 * 处理发送不成功的消息，将消息站存起来
	 * @param sendableMsg 发送的对象
	 * @param deviceId  谁发的
	 */
	public void handleAndStash(StashMessage stashMessage,SendError error) {
		
	
 		ByteBuf payload=null;
 		ByteBufEncodingUtil bufEncodingUtil=ByteBufEncodingUtil.getInatance();
 		switch (error) {
		case CHANNEL_OFF:
		case FAIL_MAX_COUNT:
			payload=bufEncodingUtil.stashMQByteBuf(ByteBufAllocator.DEFAULT,
						stashMessage.getTimestamp(), stashMessage.getDeviceId(), stashMessage.getContent());
			break;
		default:
			break;
		
		}
 		
 		
 		if(payload!=null) {
 			send2Admin( payload);
 		}
	}

}
