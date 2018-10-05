package io.mqttpush.mqttserver.handle;



import org.apache.log4j.Logger;

import io.netty.channel.ChannelInitializer;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.mqtt.MqttDecoder;
import io.netty.handler.codec.mqtt.MqttEncoder;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;

/**
 * 链路初始化
 * @author acer
 *
 */
public class MyChannelInitializer extends ChannelInitializer<SocketChannel>{

	

	Logger logger=Logger.getLogger(getClass());
	
	@Override
	protected void initChannel(SocketChannel ch) throws Exception {
		
		logger.debug(ch+"链路链接");
		
		//addWebSocket(ch.pipeline())
		
	    ch.pipeline().addLast(new LoggingHandler(LogLevel.DEBUG))
	    .addLast(MqttEncoder.INSTANCE)
	    .addLast(new MqttDecoder())
		.addLast(
				new ConnectionHandle(),
				new SubServiceHandle(),
				new PushServiceHandle());
		
	}

}
