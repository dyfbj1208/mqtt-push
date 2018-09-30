package io.mqttpush.mqttserver.handle;



import org.apache.log4j.Logger;
import org.springframework.context.ApplicationContext;

import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.codec.http.websocketx.WebSocketServerProtocolHandler;
import io.netty.handler.codec.mqtt.MqttDecoder;
import io.netty.handler.codec.mqtt.MqttEncoder;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.handler.stream.ChunkedWriteHandler;

/**
 * 链路初始化
 * @author acer
 *
 */
public class MyChannelInitializer extends ChannelInitializer<SocketChannel>{

	ApplicationContext applicationContext;

	Logger logger=Logger.getLogger(getClass());
	
	public MyChannelInitializer(ApplicationContext applicationContext) {
		this.applicationContext=applicationContext;
	}
	@Override
	protected void initChannel(SocketChannel ch) throws Exception {
		
		logger.debug(ch+"链路链接");
		
		//addWebSocket(ch.pipeline())
		
	    ch.pipeline().addLast(new LoggingHandler(LogLevel.DEBUG))
	    .addLast(MqttEncoder.INSTANCE)
	    .addLast(new MqttDecoder())
		.addLast(
				new ConnectionHandle(applicationContext),
				new SubServiceHandle(applicationContext),
				new PushServiceHandle(applicationContext));
		
	}
	

	ChannelPipeline  addWebSocket(ChannelPipeline pipeline) {
		return pipeline.addLast("httpServerCodec", new HttpServerCodec())
	        //ChunkedWriteHandler分块写处理，文件过大会将内存撑爆
	        .addLast("chunkedWriteHandler", new ChunkedWriteHandler())
	        /**
	         * 作用是将一个Http的消息组装成一个完成的HttpRequest或者HttpResponse，那么具体的是什么
	         * 取决于是请求还是响应, 该Handler必须放在HttpServerCodec后的后面
	         */
	        .addLast("httpObjectAggregator", new HttpObjectAggregator(8192))
		    .addLast(new WebSocketServerProtocolHandler("/ws"));
	}

}
