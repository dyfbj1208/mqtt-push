package io.mqttpush.mqttserver.mqttclient.handle;

import org.apache.log4j.Logger;

import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.mqtt.MqttFixedHeader;
import io.netty.handler.codec.mqtt.MqttMessage;
import io.netty.handler.codec.mqtt.MqttMessageType;
import io.netty.handler.codec.mqtt.MqttQoS;
import io.netty.handler.timeout.ReadTimeoutHandler;

/**
 * 链路空闲发送心跳
 * @author acer
 *
 */
public class Iden2PingHandle extends ReadTimeoutHandler{

	Logger logger=Logger.getLogger(getClass());
	
	public Iden2PingHandle(int timeoutSeconds) {
		super(timeoutSeconds);
	}

	@Override
	protected void readTimedOut(ChannelHandlerContext ctx) throws Exception {
		
		logger.info("发送心跳");
		ctx.writeAndFlush(
				new MqttMessage(new MqttFixedHeader(MqttMessageType.PINGREQ,
						false, MqttQoS.AT_MOST_ONCE, false, 0)));
	}
	
	
}
