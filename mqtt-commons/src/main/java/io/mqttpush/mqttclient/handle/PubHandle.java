package io.mqttpush.mqttclient.handle;

import io.mqttpush.mqttclient.service.MessageListener;
import io.mqttpush.mqttserver.beans.ConstantBean;
import io.mqttpush.mqttserver.beans.SendableMsg;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
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
 * 发布消息相关的处理
 * 
 * @author tianzhenjiu
 *
 */
public class PubHandle extends ChannelInboundHandlerAdapter {

	MessageListener messageListener;

	public PubHandle(MessageListener messageListener) {
		super();
		this.messageListener = messageListener;
	}

	@Override
	public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
		if (msg instanceof MqttMessage) {

			MqttMessage message = (MqttMessage) msg;
			MqttMessageType messageType = message.fixedHeader().messageType();

			switch (messageType) {
			case PUBLISH:// 客户端发布普通消息
				MqttPublishMessage messagepub = (MqttPublishMessage) msg;
				pub(ctx, messagepub);
				break;
			case PUBREC:// 客户端发布收到
				pubrec(ctx, message);
				break;
			case PUBREL: // 客户端发布释放
				pubrel(ctx, message);
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

	private void pub(final ChannelHandlerContext ctx, MqttPublishMessage messagepub) {

		MqttQoS mqttQoS = messagepub.fixedHeader().qosLevel();

		MqttFixedHeader fixedHeader = null;
		MqttPublishVariableHeader header = messagepub.variableHeader();
		int ackmsgid = header.packetId();
		switch (mqttQoS) {
		case EXACTLY_ONCE:
			fixedHeader = new MqttFixedHeader(MqttMessageType.PUBREC, false, MqttQoS.EXACTLY_ONCE, false, 0);
			break;
		default:
			fixedHeader = new MqttFixedHeader(MqttMessageType.PUBACK, false, mqttQoS, false, 0);

		}
		MqttMessageIdVariableHeader connectVariableHeader = MqttMessageIdVariableHeader.from(ackmsgid);
		MqttPubAckMessage ackMessage = new MqttPubAckMessage(fixedHeader, connectVariableHeader);
		ctx.writeAndFlush(ackMessage);

		messageListener.onRecivMessage(messagepub);
	}

	/**
	 * 处理客户端过来的发布释放 在消息池里根据客户端标识和消息id面拿到一个消息 并且把这个消息发送出去 现在的 客户端标识是 发送方的
	 */
	private void pubrel(final ChannelHandlerContext ctx, MqttMessage messagepub) {

		MqttMessageIdVariableHeader variableHeader = (MqttMessageIdVariableHeader) messagepub.variableHeader();

		MqttFixedHeader fixedHeader = new MqttFixedHeader(MqttMessageType.PUBCOMP, false, MqttQoS.AT_LEAST_ONCE, false,
				0);
		
		MqttPubAckMessage ackMessage = new MqttPubAckMessage(fixedHeader,
				MqttMessageIdVariableHeader.from(variableHeader.messageId()));
		ctx.writeAndFlush(ackMessage);
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

		MqttFixedHeader fixedHeader = new MqttFixedHeader(MqttMessageType.PUBREL, false, MqttQoS.AT_LEAST_ONCE, false,
				0);

		MqttPubAckMessage ackMessage = new MqttPubAckMessage(fixedHeader,
				MqttMessageIdVariableHeader.from(variableHeader.messageId()));
		ctx.writeAndFlush(ackMessage);
	}

}
