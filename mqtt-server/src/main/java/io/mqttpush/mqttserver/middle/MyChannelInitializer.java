package io.mqttpush.mqttserver.middle;



import org.apache.log4j.Logger;

import io.mqttpush.mqttserver.handle.ConnectionHandle;
import io.mqttpush.mqttserver.handle.PushServiceHandle;
import io.mqttpush.mqttserver.handle.SubServiceHandle;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.mqtt.MqttDecoder;
import io.netty.handler.codec.mqtt.MqttEncoder;

/**
 * 链路初始化
 * @author acer
 *
 */
public class MyChannelInitializer extends ChannelInitializer<SocketChannel>{

	

	Logger logger=Logger.getLogger(getClass());
	
	@Override
	protected void initChannel(SocketChannel ch) throws Exception {
		

	    ch.pipeline()
	    //.addLast(new LoggingHandler(LogLevel.DEBUG))
	    .addLast(MqttEncoder.INSTANCE)
	    .addLast(new MqttDecoder())
		.addLast(
				new ConnectionHandle(),
				new SubServiceHandle(),
				new PushServiceHandle());
		
	}

}
