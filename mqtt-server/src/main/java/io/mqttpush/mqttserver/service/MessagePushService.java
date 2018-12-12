package io.mqttpush.mqttserver.service;

import io.mqttpush.mqttserver.beans.ConstantBean;
import io.mqttpush.mqttserver.beans.SendableMsg;
import io.mqttpush.mqttserver.beans.ServiceBeans;
import io.mqttpush.mqttserver.exception.SendException;
import io.mqttpush.mqttserver.exception.SendException.SendError;
import io.mqttpush.mqttserver.util.AdminMessage.MessageType;
import io.mqttpush.mqttserver.util.ByteBufEncodingUtil;
import io.mqttpush.mqttserver.util.StashMessage;
import io.mqttpush.mqttserver.util.thread.MyHashRunnable;
import io.mqttpush.mqttserver.util.thread.SingleThreadPool;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.handler.codec.mqtt.*;
import io.netty.util.Attribute;
import io.netty.util.AttributeKey;
import org.apache.log4j.Logger;

import java.util.function.BiConsumer;

/**
 * message 实际发送服务
 * 
 * @author tzj
 * 
 */
public class MessagePushService {

	final int retimes = 3;

	Logger logger = Logger.getLogger(getClass());

	TopicService topicService;

	SingleThreadPool singleThreadPool;
	
	ChannelUserService channelUserService;

	public MessagePushService() {

		ServiceBeans serviceBeans = ServiceBeans.getInstance();
		channelUserService = serviceBeans.getChannelUserService();
		topicService = serviceBeans.getTopicService();
		singleThreadPool=serviceBeans.getSingleThreadPool();

	}

	/**
	 * 进行组播
	 * 
	 * @param sendableMsg
	 */
	public void sendMsg(final SendableMsg sendableMsg) {

		

		BiConsumer<String, MqttQoS> consumer = (deviceId, mqttQos) -> {

			
		
			Runnable sendrunnable=()->{
				
				Channel channel = channelUserService.channel(deviceId);

				boolean isfail = false;
				if (channel != null && channel.isActive()) {
					try {
						sendMsgForChannel(sendableMsg, channel, mqttQos);
					} catch (Exception e) {
						isfail = true;
						logger.warn("发送"+deviceId+"失败",e);
					}
				} else {
					isfail = true;
					logger.warn("设备"+deviceId+"通道关闭");
				}

				/**
				 * 只要失败了就暂存消息
				 */
				if (isfail) {
					handleAndStash(new StashMessage(MessageType.STASH, deviceId, System.currentTimeMillis(),
							sendableMsg.getByteForContent()), SendError.CHANNEL_OFF);
				}
			};
			
	
			singleThreadPool.execute(new MyHashRunnable(deviceId, sendrunnable, 0));
			
		};

		topicService.channelsSend(sendableMsg.getTopName(), consumer);

	}

	/**
	 * 进行单播
	 * 
	 * @param sendableMsg
	 */
	public void sendMsgForChannel(final SendableMsg sendableMsg, Channel channel, MqttQoS mqttQoS) {

		String deviceId = channelUserService.deviceId(channel);

		final  boolean isRetain=sendableMsg.isRetain();
		if(isRetain){
			sendableMsg.getByteForContent();
		}

		
		if (channel != null && channel.isActive()) {
			ChannelFuture channelFuture = sendMsg(sendableMsg, channel, deviceId, mqttQoS);
			channelFuture.addListener((ChannelFuture future) -> {

				/**
				 * 发送成功的话只要有保留标志就得发给admin
				 */
				if(sendableMsg.isRetain()){

					if(!future.isSuccess()){
						handleAndStash(
								new StashMessage(MessageType.STASH, deviceId, System.currentTimeMillis(),
										sendableMsg.getByteForContent()),
								((SendException) channelFuture.cause()).getSendError());
					}else{

						send2Admin(ByteBufEncodingUtil.getInatance().saveMQByteBuf(ByteBufAllocator.DEFAULT,
								System.currentTimeMillis(), deviceId, Unpooled.wrappedBuffer(sendableMsg.getByteForContent())));
					}

				}



			});

		} else if(sendableMsg.isRetain()){

			handleAndStash(new StashMessage(MessageType.STASH, deviceId, System.currentTimeMillis(),
					sendableMsg.getByteForContent()), SendError.CHANNEL_OFF);
		}

		if(logger.isDebugEnabled()){
			logger.debug(sendableMsg.getSendDeviceId()+"--->"+deviceId);
		}

	}

	/**
	 * 实际发送
	 * 
	 * @param sendableMsg
	 *            发送的对象
	 * @param channel
	 *            接收方的channnel
	 * @param deviceId
	 *            发送放deviceid
	 * @return
	 */
	protected ChannelFuture sendMsg(SendableMsg sendableMsg, Channel channel, String deviceId, MqttQoS mqttQoS) {

		ByteBuf sendBuf = sendableMsg.getMsgContent();
		Attribute<SendableMsg> attribute = channel.attr(ConstantBean.UnConfirmedKey);
		
		if (channel == null || !channel.isActive()) {
			return channel.newFailedFuture(new SendException(SendError.CHANNEL_OFF));
		}

		if (sendableMsg.getDupTimes() > ConstantBean.MAX_ERROR_SENT) {
			
			/**
			 * 当超过最大次数以后就清楚了重发机制
			 */
			channel.attr(ConstantBean.UnConfirmedKey).set(null);
			return channel.newFailedFuture(new SendException(SendError.FAIL_MAX_COUNT));
		}

		if (sendBuf == null || !sendBuf.isReadable()) {
			return channel.newFailedFuture(new SendException(SendError.BUFF_FREED));
		}

		AttributeKey<MqttQoS> attributeKey = AttributeKey.valueOf(sendableMsg.getTopName());

		/**
		 * 默认选择发送方的服务质量
		 * 如果接收方的channel上面包含了服务质量就用channel的服务质量
		 */
		MqttQoS qosLevel = mqttQoS;
		if (channel.hasAttr(attributeKey)) {
			qosLevel = channel.attr(attributeKey).get();
		}

	

		/**
		 * 保证通道里面只有一个待发消息。 如果有其他待发消息就发给admin 处理
		 */
		if (channel.hasAttr(ConstantBean.UnConfirmedKey)) {

			SendableMsg oldsendableMsg=null;
		
			if (attribute != null && (oldsendableMsg = attribute.get()) != null) {

				handleAndStash(new StashMessage(MessageType.STASH, deviceId, System.currentTimeMillis(),
						oldsendableMsg.getByteForContent()), SendError.FAIL_MAX_COUNT);
				
				/**
				 * 清楚原有的send对象
				 */
				channel.attr(ConstantBean.UnConfirmedKey).set(null);
			}
		}

		if (qosLevel == MqttQoS.EXACTLY_ONCE
				||qosLevel==MqttQoS.AT_LEAST_ONCE) {

			sendableMsg.setDup(true);
			sendableMsg.setDupTimes(sendableMsg.getDupTimes() + 1);
			channel.attr(ConstantBean.UnConfirmedKey).set(sendableMsg);

		}
		
		
		
		MqttFixedHeader mqttFixedHeader = new MqttFixedHeader(MqttMessageType.PUBLISH,sendableMsg.isDup(),
				qosLevel, sendableMsg.isRetain(), 0);

		MqttPublishVariableHeader variableHeader = new MqttPublishVariableHeader(sendableMsg.getTopName(),
				sendableMsg.getMessageId());

		MqttPublishMessage mqttPublishMessage = new MqttPublishMessage(mqttFixedHeader, variableHeader, sendBuf);

		
		ChannelFuture channelFuture = channel.writeAndFlush(mqttPublishMessage);

		return channelFuture;

	}

	/**
	 * 发送通知给给admin
	 * 
	 * @param payload
	 */
	public void send2Admin(ByteBuf payload) {

		/**
		 * 使用channelgroup 和 组播发送都可以发送给adminTopic
		 */
		

		

		MqttFixedHeader mqttFixedHeader = new MqttFixedHeader(MqttMessageType.PUBLISH,false,
				MqttQoS.AT_LEAST_ONCE,false, 0);

		MqttPublishVariableHeader variableHeader = new MqttPublishVariableHeader(ConstantBean.adminRecivTopic,
				payload.hashCode());

		MqttPublishMessage mqttPublishMessage = new MqttPublishMessage(mqttFixedHeader, variableHeader, payload);
		
		
		topicService.channelsForGroup(ConstantBean.adminRecivTopic, mqttPublishMessage);

	}

	/**
	 * 处理发送不成功的消息，将消息站存起来
	 * 
	 * @param stashMessage
	 *            发送的对象
	 * @param error
	 *            谁发的
	 */
	public void handleAndStash(StashMessage stashMessage, SendError error) {

		String deviceId=stashMessage.getDeviceId();
		
		if(channelUserService.isAdmin(deviceId)) {
			logger.warn("注意，是否admin已经离线?");
			return;
		}
		
		if(stashMessage==null||stashMessage.getContent()==null) {
			return;
		}
		ByteBuf payload = null;
		ByteBufEncodingUtil bufEncodingUtil = ByteBufEncodingUtil.getInatance();
		switch (error) {
		case CHANNEL_OFF:
		case FAIL_MAX_COUNT:
			payload = bufEncodingUtil.stashMQByteBuf(ByteBufAllocator.DEFAULT, stashMessage.getTimestamp(),
					stashMessage.getDeviceId(), Unpooled.wrappedBuffer(stashMessage.getContent()));
			break;
		default:
			break;

		}

		if (payload != null) {
			send2Admin(payload);
		}
	}

}
