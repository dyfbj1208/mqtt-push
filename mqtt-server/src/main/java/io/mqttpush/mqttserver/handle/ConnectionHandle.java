package io.mqttpush.mqttserver.handle;

import io.mqttpush.mqttserver.beans.ConstantBean;
import io.mqttpush.mqttserver.beans.SendableMsg;
import io.mqttpush.mqttserver.beans.ServiceBeans;
import io.mqttpush.mqttserver.service.ChannelUserService;
import io.mqttpush.mqttserver.service.CheckUserService;
import io.mqttpush.mqttserver.service.MessagePushService;
import io.mqttpush.mqttserver.util.thread.MyHashRunnable;
import io.mqttpush.mqttserver.util.thread.SingleThreadPool;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.mqtt.*;
import io.netty.util.Attribute;
import org.apache.log4j.Logger;

/**
 * 处理登录 心跳，断开的handle
 * 
 * @author acer
 *
 */

public class ConnectionHandle extends AbstractHandle {

	
	Logger logger = Logger.getLogger(getClass());
	/**
	 * 校验用户是否可以登录
	 */
	CheckUserService checkUserService;

	/**
	 * 提供信道和用户关联信息
	 */
	ChannelUserService channelUserService;

	/**
	 * 消息发送
	 */
	MessagePushService messagePushService;

	/**
	 * 根据hash标志亲缘线程池
	 */
	SingleThreadPool signelThreadPoll;

	public ConnectionHandle() {
		super();

		ServiceBeans serviceBeans = ServiceBeans.getInstance();
		channelUserService = serviceBeans.getChannelUserService();
		checkUserService = serviceBeans.getCheckUserService();
		messagePushService = serviceBeans.getMessagePushService();
		signelThreadPoll = serviceBeans.getSingleThreadPool();
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
				
				if(logger.isDebugEnabled()) {
					logger.debug("收到关闭报文,将会关闭连接"+ctx.channel().remoteAddress());
				}
				ctx.close();
				break;
			default:
				/**
				 * 就算没登陆也继续走，没登录关闭统一放在了10秒超时处关闭
				 */
				ctx.fireChannelRead(msg);
				break;
			}
		}

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
	void ack(final ChannelHandlerContext ctx, final MqttConnectMessage connectMessage) {

		MqttConnectPayload connectPayload = connectMessage.payload();
		String deviceId = connectPayload.clientIdentifier();
		Channel channel = ctx.channel();
		Runnable runnable = () -> {
			ackDevice(deviceId, channel, connectPayload.userName(), connectPayload.password());
		};

		signelThreadPoll.execute(new MyHashRunnable(deviceId, runnable, 0));

		logger.info("设备接入" + deviceId);
	}

	/**
	 * 响应设备登录 必须保证在亲缘线程里面执行
	 * 
	 * @param channel
	 * @param deviceId
	 * @param username
	 * @param password
	 */
	private void ackDevice(String deviceId, Channel channel, String username, String password) {

		MqttConnectReturnCode returnCode = null;

		
		logger.info(deviceId + "->连接"+channel.remoteAddress());
		/**
		 * 如果这个链路已经登录只是重连的话直接过
		 * 
		 */
		if (channelUserService.isLogin(channel)) {
			returnCode = MqttConnectReturnCode.CONNECTION_ACCEPTED;
			logger.info(deviceId + "重连");
		}

		else if ((returnCode = checkUserService.checkUserReturnCode(username,
				password)) == MqttConnectReturnCode.CONNECTION_ACCEPTED) {
			channelUserService.processLoginSuccess(deviceId, channel);
		}
		
		logger.info(deviceId + "->连接->"+returnCode.name());

		MqttFixedHeader fixedHeader = new MqttFixedHeader(MqttMessageType.CONNACK, false, MqttQoS.AT_MOST_ONCE,
				false, 0);
		MqttConnAckVariableHeader connectVariableHeader = new MqttConnAckVariableHeader(returnCode, false);
		MqttConnAckMessage mqttConnAckMessage = new MqttConnAckMessage(fixedHeader, connectVariableHeader);

		channel.writeAndFlush(mqttConnAckMessage);
	}

	/**
	 * 处理心跳ping
	 * 
	 * @param ctx
	 */
	private void pong(ChannelHandlerContext ctx) {

		ctx.write(
				new MqttMessage(new MqttFixedHeader(MqttMessageType.PINGRESP, false, MqttQoS.AT_MOST_ONCE, false, 0)));

		Channel channel = ctx.channel();
		/**
		 * 当心跳上来了检测到没用发送的成功接收到的消息
		 */
		if (channel.hasAttr(ConstantBean.UnConfirmedKey)) {

			/**
			 * 凡是传输都是 bytebuff, 凡是存储都是 byte[];
			 */
			SendableMsg sendableMsg = null;
			Attribute<SendableMsg> attribute = channel.attr(ConstantBean.UnConfirmedKey);
			if (attribute != null && (sendableMsg = attribute.get()) != null) {
				if (sendableMsg.getByteForContent() == null) {
					if (logger.isDebugEnabled()) {
						logger.debug("重发消息已经丢失了->" + sendableMsg.getSendDeviceId() + ":" + sendableMsg.getTopName());
					}
					return;
				}

				sendableMsg.setMsgContent(Unpooled.wrappedBuffer(sendableMsg.getByteForContent()));
				messagePushService.sendMsgForChannel(sendableMsg, channel, MqttQoS.EXACTLY_ONCE);
				if (logger.isDebugEnabled()) {
					logger.debug("重发消息->" + sendableMsg.getSendDeviceId() + ":" + sendableMsg.getTopName());
				}
			}
		}
	}

	@Override
	public void disconnect(ChannelHandlerContext context) {
		
		if(logger.isDebugEnabled()) {
			logger.debug("调用disconnect"+context.channel().remoteAddress());
		}
		
		channelUserService.loginout(context.channel());
	}

}
