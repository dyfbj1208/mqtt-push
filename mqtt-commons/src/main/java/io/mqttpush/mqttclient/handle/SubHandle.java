package io.mqttpush.mqttclient.handle;

import org.apache.log4j.Logger;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.codec.mqtt.MqttMessage;
import io.netty.handler.codec.mqtt.MqttMessageType;

/**
 * 订阅
 * @author acer
 *
 */
public class SubHandle extends ChannelInboundHandlerAdapter {

	Logger logger=Logger.getLogger(getClass());
	
	
	
	@Override
	public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
		if (msg instanceof MqttMessage) {

			MqttMessage message = (MqttMessage) msg;
			MqttMessageType messageType = message.fixedHeader().messageType();
			switch (messageType) {
			case SUBACK:
				//订阅成功
				logger.info("订阅成功");
				break;
			case UNSUBACK:
				;// 未实现
				logger.info("取消订阅成功");
			default:
				ctx.fireChannelRead(msg);
				break;
			}

		}

		else
			ctx.close();
	}

}
