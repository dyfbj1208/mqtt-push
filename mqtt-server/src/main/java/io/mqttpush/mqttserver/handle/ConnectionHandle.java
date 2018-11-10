package io.mqttpush.mqttserver.handle;

import io.mqttpush.mqttserver.beans.ConstantBean;
import io.mqttpush.mqttserver.beans.SendableMsg;
import io.mqttpush.mqttserver.beans.ServiceBeans;
import io.mqttpush.mqttserver.service.ChannelUserService;
import io.mqttpush.mqttserver.service.CheckUserService;
import io.mqttpush.mqttserver.service.MessagePushService;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.mqtt.MqttConnAckMessage;
import io.netty.handler.codec.mqtt.MqttConnAckVariableHeader;
import io.netty.handler.codec.mqtt.MqttConnectMessage;
import io.netty.handler.codec.mqtt.MqttConnectPayload;
import io.netty.handler.codec.mqtt.MqttConnectReturnCode;
import io.netty.handler.codec.mqtt.MqttFixedHeader;
import io.netty.handler.codec.mqtt.MqttMessage;
import io.netty.handler.codec.mqtt.MqttMessageType;
import io.netty.handler.codec.mqtt.MqttQoS;
import io.netty.util.Attribute;

/**
 * 处理登录 心跳，断开的handle
 * 
 * @author acer
 *
 */

public class ConnectionHandle extends AbstractHandle {

	
	/**
	 * 校验用户是否可以登录
	 */
	CheckUserService checkUserService;

	/**
	 * 提供信道和用户关联信息
	 */
	ChannelUserService channelUserService;

	
	
	MessagePushService messagePushService;
	

	public ConnectionHandle() {
		super();
		
		ServiceBeans serviceBeans=ServiceBeans.getInstance();
		
		channelUserService = serviceBeans.getChannelUserService();
		checkUserService =serviceBeans.getCheckUserService();

		messagePushService=serviceBeans.getMessagePushService();
	}

	@Override
	public void onMessage(ChannelHandlerContext ctx, MqttMessage msg) {

		if (msg instanceof MqttMessage) {

			MqttMessage message = (MqttMessage) msg;
			MqttFixedHeader fixedHeader = message.fixedHeader();
			MqttMessageType messageType = fixedHeader.messageType();

			switch (messageType) {
			case CONNECT:
				ack(ctx, (MqttConnectMessage) message);
				break;
			case PINGREQ:
				pong(ctx);
				break;

			case DISCONNECT:
				disconnect(ctx);
				break;
			default:
				/**
				 * 此处应该判断是否登录，如果没有就关闭连接
				 */
				if (channelUserService.isLogin(ctx.channel())) {
					ctx.fireChannelRead(msg);
				} else {
					ctx.close();
					logger.debug("没有登录呢，你想干嘛？" + messageType);
				}
				break;
			}
		} else
			ctx.close();
	}

	/**
	 * 验证用户连接合法性
	 * 
	 * 首先检查用户是否登录过了，如果登录了就拒绝重登。 检查当前需要登录验证的链接的数量，如果过多就缓存到队列 队列满了去数据库一次执行
	 * 如果比较少就一个一个验证
	 * 
	 * @param ctx
	 * @param connectMessage
	 */
	private void ack(final ChannelHandlerContext ctx, final MqttConnectMessage connectMessage) {

		MqttConnAckVariableHeader connectVariableHeader = null;
		MqttConnectReturnCode returnCode = null;
		MqttConnectPayload connectPayload = connectMessage.payload();
		String deviceId = connectPayload.clientIdentifier();
		Channel channel = ctx.channel();


		if ((returnCode = checkUserService.checkUserReturnCode(connectPayload.userName(),
				connectPayload.password())) == MqttConnectReturnCode.CONNECTION_ACCEPTED) {
				channelUserService.processLoginSuccess(deviceId, channel);
		}

		if (returnCode != null) {
			
			MqttFixedHeader fixedHeader = new MqttFixedHeader(MqttMessageType.CONNACK, false, MqttQoS.AT_MOST_ONCE, false,
					0);
			
			connectVariableHeader = new MqttConnAckVariableHeader(returnCode, false);
			MqttConnAckMessage mqttConnAckMessage = new MqttConnAckMessage(fixedHeader, connectVariableHeader);

			ctx.writeAndFlush(mqttConnAckMessage);

		}

	}

	/**
	 * 处理心跳ping
	 * 
	 * @param ctx
	 */
	private void pong(ChannelHandlerContext ctx) {

		ctx.write(
				new MqttMessage(new MqttFixedHeader(MqttMessageType.PINGRESP, false, MqttQoS.AT_MOST_ONCE, false, 0)));
		
		
		Channel channel=ctx.channel();
		/**
		 * 当心跳上来了检测到没用发送的成功接收到的消息
		 */
		if(channel.hasAttr(ConstantBean.LASTSENT_KEY)) {	
			
			
			/**
			 * 凡是传输都是 bytebuff, 凡是占存都是 byte[];
			 */
			SendableMsg sendableMsg=null;
			Attribute<SendableMsg> attribute=channel.attr(ConstantBean.LASTSENT_KEY);
			if(attribute!=null&&(sendableMsg=attribute.get())!=null) {
				if(sendableMsg.getByteForContent()!=null){
					sendableMsg.setMsgContent(Unpooled.wrappedBuffer(sendableMsg.getByteForContent()));
				}
				messagePushService.sendMsgForChannel(sendableMsg,channel,MqttQoS.EXACTLY_ONCE);
			}
		}
	}

	@Override
	public void disconnect(ChannelHandlerContext context) {
		channelUserService.loginout(context.channel());
		super.disconnect(context);
	}

}
