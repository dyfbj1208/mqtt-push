package io.mqttpush.mqttserver.handle;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;

import io.mqttpush.mqttserver.entity.DeviceMq;
import io.mqttpush.mqttserver.entity.MsgRep;
import io.mqttpush.mqttserver.entity.MsgSendRunable;
import io.mqttpush.mqttserver.entity.SendableMsg;
import io.mqttpush.mqttserver.service.AnsyncService;
import io.mqttpush.mqttserver.service.ChannelUserService;
import io.mqttpush.mqttserver.service.CheckUserService;
import io.mqttpush.mqttserver.service.MQManagerService;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandler.Sharable;
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

/**
 * 处理登录 心跳，断开的handle
 * 
 * @author acer
 *
 */
@Sharable
public class ConnectionHandle extends AbstractHandle {

	@Autowired
	CheckUserService checkUserService;

	@Autowired
	ChannelUserService channelUserService;

	ApplicationContext atx;

	@Autowired
	MQManagerService mqservice;

	@Autowired
	AnsyncService ansyncService;

	public ConnectionHandle(ApplicationContext atx) {
		super();
		this.atx = atx;
		channelUserService = atx.getBean(ChannelUserService.class);
		checkUserService = atx.getBean(CheckUserService.class);

		ansyncService = atx.getBean(AnsyncService.class);
		mqservice = atx.getBean(MQManagerService.class);

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

		MqttFixedHeader fixedHeader = new MqttFixedHeader(MqttMessageType.CONNACK, false, MqttQoS.AT_MOST_ONCE, false,
				0);

		//channelUserService.processReLogin(deviceId);

		if ((returnCode = checkUserService.validateUserLogin(connectPayload.userName(),
				connectPayload.password())) == MqttConnectReturnCode.CONNECTION_ACCEPTED) {
			channelUserService.loginSuccess(deviceId, channel);

			ansyncService.runTask(() -> {

				DeviceMq deviceMq = mqservice.getDeviceQueMsg(deviceId);

				while (deviceMq != null && deviceMq.isNext()) {

					logger.debug("登录成功收取未读消息" + deviceMq.getMsgRep().getMessageid());
					MsgRep msgRep = deviceMq.getMsgRep();
					if (msgRep.getByteBuf() != null)
						msgRep.getByteBuf().retain();

					SendableMsg sendableMsg = new SendableMsg(msgRep.getTopname(), null, msgRep.getByteBuf());

					channel.eventLoop().submit(new MsgSendRunable(deviceId, channel, sendableMsg, null));
					deviceMq = mqservice.getDeviceQueMsg(deviceId);
				}
			});
		}

		if (returnCode != null) {
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
	}

	@Override
	public void disconnect(ChannelHandlerContext context) {
		channelUserService.loginout(context.channel());
		super.disconnect(context);
	}

}
