package io.mqttpush.mqttserver.handle;

import org.apache.log4j.Logger;

import io.mqttpush.mqttserver.beans.ConstantBean;
import io.mqttpush.mqttserver.beans.SendableMsg;
import io.mqttpush.mqttserver.beans.ServiceBeans;
import io.mqttpush.mqttserver.service.ChannelUserService;
import io.mqttpush.mqttserver.service.MessagePushService;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.mqtt.MqttFixedHeader;
import io.netty.handler.codec.mqtt.MqttMessage;
import io.netty.handler.codec.mqtt.MqttMessageIdVariableHeader;
import io.netty.handler.codec.mqtt.MqttMessageType;
import io.netty.handler.codec.mqtt.MqttPubAckMessage;
import io.netty.handler.codec.mqtt.MqttPublishMessage;
import io.netty.handler.codec.mqtt.MqttPublishVariableHeader;
import io.netty.handler.codec.mqtt.MqttQoS;
import io.netty.util.Attribute;
import io.netty.util.ReferenceCountUtil;

/**
 * 处理消息发布，发布释放，发布完成的hannel
 * 
 * @author tzj
 *
 */
public class PushServiceHandle extends AbstractHandle {

	Logger logger = Logger.getLogger(getClass());

	ChannelUserService channelUserService;

	MessagePushService messagePushService;

	public PushServiceHandle() {

		ServiceBeans serviceBeans = ServiceBeans.getInstance();

		channelUserService = serviceBeans.getChannelUserService();

		messagePushService = serviceBeans.getMessagePushService();

	}

	@Override
	public void onMessage(ChannelHandlerContext ctx, MqttMessage msg) {

		if (msg instanceof MqttMessage) {

			MqttMessage message = (MqttMessage) msg;
			MqttMessageType messageType = message.fixedHeader().messageType();

			switch (messageType) {
			case PUBLISH:// 客户端发布普通消息
				MqttPublishMessage messagepub = (MqttPublishMessage) msg;
				pub(ctx, messagepub);
				break;
			case PUBREL: // 客户端发布释放
				pubrel(ctx, message);
				break;
			case PUBREC:// 客户端发布收到
				pubrec(ctx, message);
				break;
			case PUBCOMP:
			case PUBACK:
				ReferenceCountUtil.release(message);
				break;
			default:
				ctx.fireChannelRead(msg);
				break;
			}

		}

		else
			ctx.channel().close();
	}

	/**
	 * 处理客户端的发布请求 根据客户端请求的QOS级别 发送相应的响应 把相应的消息存储在数据库 现在的 客户端标识是 发送方的
	 * 
	 * @param ctx
	 * @param messagepub
	 */
	private void pub(final ChannelHandlerContext ctx, MqttPublishMessage messagepub) {

		MqttQoS mqttQoS = messagepub.fixedHeader().qosLevel();

		MqttFixedHeader fixedHeader = null;
		MqttPublishVariableHeader header = messagepub.variableHeader();
		int ackmsgid = header.packetId();

		switch (mqttQoS) {
		case EXACTLY_ONCE:
			fixedHeader = new MqttFixedHeader(MqttMessageType.PUBREC, false, MqttQoS.EXACTLY_ONCE, false, 0);
			MqttMessageIdVariableHeader connectVariableHeader = MqttMessageIdVariableHeader.from(ackmsgid);
			MqttPubAckMessage ackMessage = new MqttPubAckMessage(fixedHeader, connectVariableHeader);
			ctx.write(ackMessage);
			break;
		default:
			fixedHeader = new MqttFixedHeader(MqttMessageType.PUBACK, false, mqttQoS, false, 0);
			MqttMessage message = new MqttMessage(fixedHeader);
			ctx.write(message);
			break;

		}
		
		SendableMsg sendableMsg = new SendableMsg(header.topicName(), channelUserService.deviceId(ctx.channel()),
				messagepub.content());
		/**
		 * 添加消息的保留标志，一般来说，只有保留标志的消息才会被保存
		 */
		sendableMsg.setRetain(messagepub.fixedHeader().isRetain());
		
		ready2Send(sendableMsg,ctx.channel());

	}

	/**
	 * 准备去发送
	 * 
	 * @param channel
	 * @param topname
	 * @param messageid
	 * @param content
	 */
	private void ready2Send(SendableMsg sendableMsg,Channel channel) {
		
		String topicname=sendableMsg.getTopname();
		
		/**
		 * 如果是点对点发送，直接从主题里面取出deviceid 发送
		 * 
		 * 如果是订阅发布发送，则需要走路由
		 */
		if(topicname.startsWith(ConstantBean.ONE2ONE_CHAT_PREFIX)) {
				
				String deviceId=topicname.substring(ConstantBean.ONE2ONE_CHAT_PREFIX.length());
				Channel toChannel=channelUserService.channel(deviceId);
				if(toChannel!=null) {
					messagePushService.sendMsgForChannel(sendableMsg, toChannel,MqttQoS.EXACTLY_ONCE);
					//点对点发送的时候会记录最后发送对端的设备id
					channel.attr(ConstantBean.LASTSENT_DEVICEID).set(deviceId);
				}
		
		}
		
		/**
		 * 让下面继续走，保证多端的情况也可以收到消息
		 */
		messagePushService.sendMsg(sendableMsg);
	}

	/**
	 * 处理客户端过来的发布释放 在消息池里根据客户端标识和消息id面拿到一个消息 并且把这个消息发送出去 现在的 客户端标识是 发送方的
	 */
	private void pubrel(final ChannelHandlerContext ctx, MqttMessage messagepub) {

		MqttMessageIdVariableHeader variableHeader = (MqttMessageIdVariableHeader) messagepub.variableHeader();

		MqttFixedHeader fixedHeader = new MqttFixedHeader(MqttMessageType.PUBCOMP, false, MqttQoS.EXACTLY_ONCE, false,
				0);

		MqttPubAckMessage ackMessage = new MqttPubAckMessage(fixedHeader,
				MqttMessageIdVariableHeader.from(variableHeader.messageId()));
		ctx.write(ackMessage);

		Channel channel = ctx.channel();

		if (channel.hasAttr(ConstantBean.LASTSENT_KEY)) {
			Attribute<SendableMsg> attribute = channel.attr(ConstantBean.LASTSENT_KEY);
			if (attribute != null) {
				attribute.set(null);
			}

		}
	}

	/**
	 * 处理客户端 发布收到
	 * 
	 * 对客户端发送发布释放 根据 客户端收到的messageid 找到相应的message 并且 存储到消息记录里面 现在的 客户端标识是 接受方的
	 * 现在的messageid是数据库里面的主键id 最后移除重发队列 防止消息重发
	 * 
	 * @param ctx
	 * @param messagepub
	 */
	private void pubrec(final ChannelHandlerContext ctx, MqttMessage messagepub) {

		MqttMessageIdVariableHeader variableHeader = (MqttMessageIdVariableHeader) messagepub.variableHeader();

		MqttFixedHeader fixedHeader = new MqttFixedHeader(MqttMessageType.PUBREL, false, MqttQoS.EXACTLY_ONCE, false,
				0);
		;

		MqttPubAckMessage ackMessage = new MqttPubAckMessage(fixedHeader,
				MqttMessageIdVariableHeader.from(variableHeader.messageId()));
		ctx.write(ackMessage);

		

	}

}
