package io.mqttpush.mqttserver.service;

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
import io.netty.handler.codec.mqtt.*;
import io.netty.util.Attribute;
import io.netty.util.AttributeKey;
import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.GenericFutureListener;
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

	ChannelUserService channelUserService;

	public MessagePushService() {

		ServiceBeans serviceBeans = ServiceBeans.getInstance();
		channelUserService = serviceBeans.getChannelUserService();
		topicService = serviceBeans.getTopicService();

	}

	/**
	 * 进行组播
	 * 
	 * @param sendableMsg
	 */
	public void sendMsg(final SendableMsg sendableMsg) {

		BiConsumer<String,MqttQoS> consumer = (deviceId, mqttQos) -> {

			Channel channel = channelUserService.channel(deviceId);
			

			boolean isfail = false;
			if (channel != null && channel.isActive()) {
				try {
					sendMsgForChannel(sendableMsg, channel,mqttQos);
				} catch (Exception e) {
					isfail = true;
				}
			} else {
				isfail = true;
			}

			/**
			 * 只要失败了就暂存消息
			 */
			if (isfail) {
				handleAndStash(new StashMessage(MessageType.STASH, deviceId, System.currentTimeMillis(),
						sendableMsg.getContent()), SendError.CHANNEL_OFF);
			}
		};

		topicService.channelsSend(sendableMsg.getTopname(), consumer);

	}

	/**
	 * 进行单播
	 * 
	 * @param sendableMsg
	 */
	public void sendMsgForChannel(final SendableMsg sendableMsg, Channel channel,MqttQoS mqttQoS) {

		String deviceId = channelUserService.deviceId(channel);
		if (channel != null && channel.isActive()) {
			sendableMsg.getByteBuf().retain();
			ChannelFuture channelFuture = sendMsg(sendableMsg, channel, deviceId,mqttQoS);
			channelFuture.addListener(new GenericFutureListener<Future<? super Void>>() {

				@Override
				public void operationComplete(Future<? super Void> future) throws Exception {

					if (!future.isSuccess()) {
						if (future.cause() instanceof SendException) {

							handleAndStash(
									new StashMessage(MessageType.STASH, deviceId, System.currentTimeMillis(),
											sendableMsg.getContent()),
									((SendException) channelFuture.cause()).getSendError());
						} else {
							handleAndStash(new StashMessage(MessageType.STASH, deviceId, System.currentTimeMillis(),
									sendableMsg.getContent()), SendError.CHANNEL_OFF);
						}

						/**
						 * 如果消息是需要保存的就发给admin去保存起来
						 */
					}else if(sendableMsg.isRetain()){
											
						send2Admin( ByteBufEncodingUtil.getInatance()
								.saveMQByteBuf(ByteBufAllocator.DEFAULT, System.currentTimeMillis(),
										deviceId, sendableMsg.getContent()));
					}
				}

			});

		} else {
			handleAndStash(
					new StashMessage(MessageType.STASH, deviceId, System.currentTimeMillis(), sendableMsg.getContent()),
					SendError.CHANNEL_OFF);
		}

	}

	/**
	 * 实际发送
	 * 
	 * @param sendableMsg 发送的对象
	 * @param channel     接收方的channnel
	 * @param deviceId    发送放deviceid
	 * @return
	 */
	protected ChannelFuture sendMsg(SendableMsg sendableMsg, Channel channel, String deviceId,MqttQoS mqttQoS) {

		ByteBuf byteBuf = sendableMsg.getByteBuf();

		if (channel == null || !channel.isActive()) {
			return channel.newFailedFuture(new SendException(SendError.CHANNEL_OFF));
		}

		if (sendableMsg.getDupTimes() > ConstantBean.MAX_ERROR_SENT) {
			return channel.newFailedFuture(new SendException(SendError.FAIL_MAX_COUNT));
		}

		if (byteBuf != null && byteBuf.isReadable()) {
			ChannelFuture channelFuture = null;
			AttributeKey<MqttQoS> attributeKey = AttributeKey.valueOf(sendableMsg.getTopname());

			MqttQoS qosLevel = MqttQoS.AT_LEAST_ONCE;
			if (channel.hasAttr(attributeKey)) {
				qosLevel = channel.attr(attributeKey).get();

				if (qosLevel == MqttQoS.EXACTLY_ONCE)
					byteBuf.retain();
			}

			MqttFixedHeader mqttFixedHeader = new MqttFixedHeader(MqttMessageType.PUBLISH,
					sendableMsg.getDupTimes() > 0, qosLevel, sendableMsg.isRetain(), 0);

			MqttPublishVariableHeader variableHeader = new MqttPublishVariableHeader(sendableMsg.getTopname(),
					sendableMsg.getMessageid());

			MqttPublishMessage mqttPublishMessage = new MqttPublishMessage(mqttFixedHeader, variableHeader, byteBuf);
			channelFuture = channel.writeAndFlush(mqttPublishMessage);

			/**
			 * 保证通道里面只有一个待发消息。 如果有其他待发消息就发给admin 处理
			 */
			if (channel.hasAttr(ConstantBean.LASTSENT_KEY)) {

				Attribute<SendableMsg> attribute = channel.attr(ConstantBean.LASTSENT_KEY);
				if (attribute != null && (sendableMsg = attribute.get()) != null) {

					handleAndStash(new StashMessage(MessageType.STASH, deviceId, System.currentTimeMillis(),
							sendableMsg.getContent()), SendError.FAIL_MAX_COUNT);
				}
			}

			if (qosLevel == MqttQoS.EXACTLY_ONCE) {
				sendableMsg.setDupTimes(sendableMsg.getDupTimes() + 1);
				sendableMsg.getByteBuf().retain();
				channel.attr(ConstantBean.LASTSENT_KEY).set(sendableMsg);

			}

			return channelFuture;

		} else {
			return channel.newFailedFuture(new SendException(SendError.BUFF_FREED));
		}
	}

	/**
	 *  发送通知给给admin
	 * @param payload
	 */
	public void send2Admin(ByteBuf payload) {

	

		/**
		 * 使用channelgroup 和 组播发送都可以发送给adminTopic
		 */
		SendableMsg sendableMsg=
				new SendableMsg(ConstantBean.adminRecivTopic, ConstantBean.SYSTEM_IDENTIFY, payload);
		topicService.channelsForGroup(ConstantBean.adminRecivTopic, sendableMsg);

	}

	/**
	 * 处理发送不成功的消息，将消息站存起来
	 * 
	 * @param stashMessage 发送的对象
	 * @param error    谁发的
	 */
	public void handleAndStash(StashMessage stashMessage, SendError error) {

		ByteBuf payload = null;
		ByteBufEncodingUtil bufEncodingUtil = ByteBufEncodingUtil.getInatance();
		switch (error) {
		case CHANNEL_OFF:
		case FAIL_MAX_COUNT:
			payload = bufEncodingUtil.stashMQByteBuf(ByteBufAllocator.DEFAULT, stashMessage.getTimestamp(),
					stashMessage.getDeviceId(), stashMessage.getContent());
			break;
		default:
			break;

		}

		if (payload != null) {
			send2Admin(payload);
		}
	}

}
